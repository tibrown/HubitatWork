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
                input "onTimeType", "enum", title: "Turn On At", options: ["Sunset", "Time", "Mode"], defaultValue: "Sunset", submitOnChange: true
                if (onTimeType == "Time") {
                    input "onTimeVal", "time", title: "On Time"
                } else if (onTimeType == "Mode") {
                    input "onMode", "mode", title: "On Mode", multiple: true
                }
                input "offTimeType", "enum", title: "Turn Off At", options: ["Time", "Sunrise", "Sunset", "Mode"], defaultValue: "Time", submitOnChange: true
                if (offTimeType == "Time") {
                    input "offTimeVal", "time", title: "Off Time", defaultValue: "22:00"
                } else if (offTimeType == "Mode") {
                    input "offMode", "mode", title: "Off Mode", multiple: true
                }
            }
        }
        section("Virtual Control Switches") {
            input "christmasTreesSwitch", "capability.switch", title: "ChristmasTrees Virtual Switch", required: false, multiple: false, description: "Virtual switch to control all trees"
            input "christmasLightsSwitch", "capability.switch", title: "ChristmasLights Virtual Switch", required: false, multiple: false, description: "Virtual switch to control all outdoor lights"
        }
        section("Indoor Devices (Trees)") {
            input "treeSwitches", "capability.switch", title: "Tree Switches", required: false, multiple: true
        }
        section("Outdoor Devices (Lights)") {
            input "mainLights", "capability.switch", title: "Main Christmas Lights", required: false, multiple: true
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
    
    // Subscribe to master switch
    if (masterSwitch) subscribe(masterSwitch, "switch", switchHandler)
    
    // Subscribe to virtual control switches for manual override
    if (christmasTreesSwitch) subscribe(christmasTreesSwitch, "switch", treeSwitchHandler)
    if (christmasLightsSwitch) subscribe(christmasLightsSwitch, "switch", lightsSwitchHandler)
    
    if (enableSchedule) {
        log.debug "Schedule enabled. On Type: $onTimeType"
        if (onTimeType == "Sunset") {
            subscribe(location, "sunset", sunsetHandler)
        } else if (onTimeType == "Mode") {
            subscribe(location, "mode", modeHandler)
        } else if (onTimeVal) {
            schedule(onTimeVal, scheduledTurnOn)
        }
        
        if (offTimeType == "Sunset") {
            subscribe(location, "sunset", scheduledTurnOff)
        } else if (offTimeType == "Sunrise") {
            subscribe(location, "sunrise", scheduledTurnOff)
        } else if (offTimeType == "Mode") {
            if (onTimeType != "Mode") subscribe(location, "mode", modeHandler)
        } else if (offTimeVal) {
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

    // Check Mode Logic first
    if (onTimeType == "Mode") {
        if (onMode && location.mode in onMode) {
            log.debug "Current mode matches On Mode. Turning on."
            scheduledTurnOn()
        }
        return
    }
    
    // If Off is Mode, and we are in that mode, ensure off?
    if (offTimeType == "Mode") {
        if (offMode && location.mode in offMode) {
            log.debug "Current mode matches Off Mode. Ensuring off."
            scheduledTurnOff()
            return
        }
    }

    def now = new Date()
    def startTime
    def endTime
    
    if (offTimeType == "Sunset") {
        endTime = getSunriseAndSunset().sunset
    } else if (offTimeType == "Sunrise") {
        endTime = getSunriseAndSunset().sunrise
    } else if (offTimeType == "Mode") {
        // No specific end time.
        endTime = null
    } else {
        endTime = offTimeVal ? timeToday(offTimeVal) : timeToday("22:00")
    }
    
    if (onTimeType == "Sunset") {
        def sunTimes = getSunriseAndSunset()
        startTime = sunTimes.sunset
    } else if (onTimeType == "Mode") {
        startTime = null
    } else {
        startTime = onTimeVal ? timeToday(onTimeVal) : null
    }

    if (startTime && endTime) {
        // If end time is earlier than start time, assume it's the next day
        if (endTime < startTime) {
            endTime = endTime + 1
        }
        
        // Check if now is between start and end
        if (now >= startTime && now < endTime) {
            log.debug "Current time is within schedule window. Turning on."
            scheduledTurnOn()
        } else {
            log.debug "Current time is NOT within schedule window."
        }
    } else if (startTime && offTimeType == "Mode") {
        // Started by time, ends by mode. 
        // We already checked if we are in Off Mode above.
        // If we are here, we are NOT in Off Mode.
        // So if we are past start time, we should probably be on?
        if (now >= startTime) {
             log.debug "Past start time and not in Off Mode. Turning on."
             scheduledTurnOn()
        }
    }
}

def modeHandler(evt) {
    log.debug "Mode changed to ${evt.value}"
    
    if (onTimeType == "Mode" && onMode && evt.value in onMode) {
        log.debug "Mode matches On Mode. Turning on."
        scheduledTurnOn()
    }
    
    if (offTimeType == "Mode" && offMode && evt.value in offMode) {
        log.debug "Mode matches Off Mode. Turning off."
        scheduledTurnOff()
    }
}

def switchHandler(evt) {
    if (evt.value == "on") {
        activateChristmas()
    } else {
        deactivateChristmas()
    }
}

def treeSwitchHandler(evt) {
    // Manual control of trees via virtual switch
    if (atomicState.syncingTreesSwitch) {
        log.debug "Ignoring trees switch event caused by app sync"
        return
    }
    log.debug "ChristmasTrees switch manually turned ${evt.value}"
    if (evt.value == "on") {
        if (treeSwitches) treeSwitches.on()
    } else {
        if (treeSwitches) treeSwitches.off()
    }
}

def lightsSwitchHandler(evt) {
    // Manual control of lights via virtual switch
    if (atomicState.syncingLightsSwitch) {
        log.debug "Ignoring lights switch event caused by app sync"
        return
    }
    log.debug "ChristmasLights switch manually turned ${evt.value}"
    if (evt.value == "on") {
        // Check rain sensor before turning on outdoor lights
        if (rainSensor && rainSensor.currentValue("switch") == "on") {
            log.debug "Rain sensor is on, not turning on outdoor lights"
            if (notificationDevices) notificationDevices.deviceNotification("Cannot turn on Christmas lights - it is raining")
            // Turn the switch back off
            atomicState.syncingLightsSwitch = true
            christmasLightsSwitch.off()
            runIn(2, clearLightsSwitchSync)
            return
        }
        if (mainLights) mainLights.on()
        if (porchLights) {
            runIn(300, turnOffPorch)
        }
    } else {
        if (mainLights) mainLights.off()
        if (porchLights) porchLights.on()
    }
}

def clearTreesSwitchSync() {
    atomicState.syncingTreesSwitch = false
}

def clearLightsSwitchSync() {
    atomicState.syncingLightsSwitch = false
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
    
    // Turn on trees and sync the virtual switch
    if (treeSwitches) treeSwitches.on()
    if (christmasTreesSwitch && christmasTreesSwitch.currentValue("switch") != "on") {
        atomicState.syncingTreesSwitch = true
        christmasTreesSwitch.on()
        runIn(2, clearTreesSwitchSync)
    }
    
    // Check Rain for Outdoor Lights
    if (rainSensor && rainSensor.currentValue("switch") == "on") {
        log.debug "Rain sensor is on, skipping outdoor lights"
        if (notificationDevices) notificationDevices.deviceNotification("Christmas lights not coming on because it is raining")
        // Ensure outdoor lights and virtual switch are off if it's raining
        if (mainLights) mainLights.off()
        if (christmasLightsSwitch && christmasLightsSwitch.currentValue("switch") != "off") {
            atomicState.syncingLightsSwitch = true
            christmasLightsSwitch.off()
            runIn(2, clearLightsSwitchSync)
        }
        return
    }

    // Turn on Outdoor Lights (if not raining) and sync the virtual switch
    if (mainLights) mainLights.on()
    if (christmasLightsSwitch && christmasLightsSwitch.currentValue("switch") != "on") {
        atomicState.syncingLightsSwitch = true
        christmasLightsSwitch.on()
        runIn(2, clearLightsSwitchSync)
    }
    
    // Handle Porch Lights
    runIn(300, turnOffPorch)
}

def deactivateChristmas() {
    // Turn off trees and sync the virtual switch
    if (treeSwitches) treeSwitches.off()
    if (christmasTreesSwitch && christmasTreesSwitch.currentValue("switch") != "off") {
        atomicState.syncingTreesSwitch = true
        christmasTreesSwitch.off()
        runIn(2, clearTreesSwitchSync)
    }
    
    // Turn off Outdoor Lights and sync the virtual switch
    if (mainLights) mainLights.off()
    if (christmasLightsSwitch && christmasLightsSwitch.currentValue("switch") != "off") {
        atomicState.syncingLightsSwitch = true
        christmasLightsSwitch.off()
        runIn(2, clearLightsSwitchSync)
    }

    // Restore Porch Lights
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
