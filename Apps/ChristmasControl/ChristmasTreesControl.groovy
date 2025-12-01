definition(
    name: "Christmas Control",
    namespace: "hubitat",
    author: "Hubitat User",
    description: "Unified control for Christmas trees and lights.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: ""
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Christmas Control Settings", install: true, uninstall: true) {
        section("Triggers") {
            input "masterSwitch", "capability.switch", title: "Master Switch (Optional)", required: false, multiple: false, description: "A switch to manually control all Christmas devices."
            input "enableSchedule", "bool", title: "Enable Schedule?", submitOnChange: true
            if (enableSchedule) {
                input "startMonth", "number", title: "Start Month (e.g. 11)", defaultValue: 11
                input "startDay", "number", title: "Start Day (e.g. 23)", defaultValue: 23
                input "endMonth", "number", title: "End Month (e.g. 1)", defaultValue: 1
                input "endDay", "number", title: "End Day (e.g. 2)", defaultValue: 2
                input "onTimeType", "enum", title: "Turn On At", options: ["Sunset", "Time"], defaultValue: "Sunset", submitOnChange: true
                if (onTimeType == "Time") {
                    input "onTimeVal", "time", title: "On Time"
                }
                input "offTimeVal", "time", title: "Off Time", defaultValue: "22:00"
            }
        }
        section("Indoor Devices (Trees)") {
            input "treeSwitches", "capability.switch", title: "Tree Switches", required: false, multiple: true
        }
        section("Outdoor Devices (Lights)") {
            input "mainLights", "capability.switch", title: "Main Christmas Lights", required: false, multiple: true
            input "secondaryLights", "capability.switch", title: "Secondary Christmas Lights", required: false, multiple: true
            input "porchLights", "capability.switch", title: "Porch Lights", required: false, multiple: true
            input "rainSensor", "capability.switch", title: "Rain Sensor", required: false, multiple: false
            input "notificationDevices", "capability.notification", title: "Notification Devices", required: false, multiple: true
        }
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    log.debug "initialize called"
    if (masterSwitch) subscribe(masterSwitch, "switch", switchHandler)
    
    if (enableSchedule) {
        log.debug "Schedule enabled. On Type: $onTimeType"
        if (onTimeType == "Sunset") {
            subscribe(location, "sunset", sunsetHandler)
        } else if (onTimeVal) {
            schedule(onTimeVal, scheduledTurnOn)
        }
        
        if (offTimeVal) {
            schedule(offTimeVal, scheduledTurnOff)
        }

        // Check if we should be on right now (Catch-up logic)
        checkIfShouldBeOn()
    } else {
        log.debug "Schedule disabled"
    }
}

def checkIfShouldBeOn() {
    log.debug "Checking if lights should be on now..."
    if (!checkDate()) {
        log.debug "Date not within range."
        return
    }

    def now = new Date()
    def startTime
    def endTime = offTimeVal ? timeToday(offTimeVal) : timeToday("22:00")
    
    if (onTimeType == "Sunset") {
        def sunTimes = getSunriseAndSunset()
        startTime = sunTimes.sunset
    } else {
        startTime = onTimeVal ? timeToday(onTimeVal) : null
    }

    if (startTime && endTime) {
        // If end time is earlier than start time, assume it's the next day
        if (endTime < startTime) {
            endTime = endTime + 1
        }
        
        // Check if now is between start and end
        // Note: This simple check handles the "Sunset just happened" case.
        // It might not handle the "It's 1AM and I should still be on from yesterday" case perfectly without more complex logic,
        // but it solves the immediate issue of missed events.
        if (now >= startTime && now < endTime) {
            log.debug "Current time is within schedule window. Turning on."
            scheduledTurnOn()
        } else {
            log.debug "Current time is NOT within schedule window."
        }
    }
}

def switchHandler(evt) {
    if (evt.value == "on") {
        activateChristmas()
    } else {
        deactivateChristmas()
    }
}

def sunsetHandler(evt) {
    log.debug "Sunset event received"
    scheduledTurnOn()
}

def scheduledTurnOn() {
    log.debug "scheduledTurnOn called"
    if (checkDate()) {
        log.debug "Date is within range, turning on"
        activateChristmas()
    } else {
        log.debug "Date is outside range, not turning on"
    }
}

def scheduledTurnOff() {
    deactivateChristmas()
}

def activateChristmas() {
    log.debug "activateChristmas called"
    // Check Rain for Outdoor Lights
    if (rainSensor && rainSensor.currentValue("switch") == "on") {
        log.debug "Rain sensor is on, skipping outdoor lights"
        if (notificationDevices) notificationDevices.deviceNotification("Christmas lights not coming on because it is raining")
        // Ensure outdoor lights are off if it's raining
        if (mainLights) mainLights.off()
        if (secondaryLights) secondaryLights.off()
        return
    }

    // 3. Turn on Outdoor Lights (if not raining)
    if (mainLights) mainLights.on()
    if (secondaryLights) secondaryLights.on()
    
    // 4. Handle Porch Lights
    runIn(300, turnOffPorch)
}

def deactivateChristmas() {
    // Turn off Outdoor Lights
    if (mainLights) mainLights.off()
    if (secondaryLights) secondaryLights.off()

    // 3. Restore Porch Lights
    if (porchLights) porchLights.on()
}

def turnOffPorch() {
    if (porchLights) porchLights.off()
}

def checkDate() {
    def now = new Date()
    def tz = location.timeZone ?: TimeZone.getDefault()
    def currentMonth = now.format("M", tz).toInteger()
    def currentDay = now.format("d", tz).toInteger()
    def curr = currentMonth * 100 + currentDay
    
    // Use defaults if settings are null
    def sMonth = startMonth != null ? startMonth : 11
    def sDay = startDay != null ? startDay : 23
    def eMonth = endMonth != null ? endMonth : 1
    def eDay = endDay != null ? endDay : 2
    
    def s = sMonth * 100 + sDay
    def e = eMonth * 100 + eDay
    
    log.debug "checkDate: Current: $curr (Month: $currentMonth, Day: $currentDay), Start: $s, End: $e"
    
    if (s > e) { // Wraps year
        return (curr >= s || curr <= e)
    } else {
        return (curr >= s && curr <= e)
    }
}
