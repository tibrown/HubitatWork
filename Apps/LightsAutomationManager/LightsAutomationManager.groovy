/**
 *  Lights Automation Manager
 *
 *  Copyright 2025 Tim Brown
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

definition(
    name: "Lights Automation Manager",
    namespace: "hubitat",
    author: "Tim Brown",
    description: "Comprehensive lighting automation managing mode-based schedules, motion-activated floods, desk lighting, color strips, and master controls.",
    category: "Lighting",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    singleThreaded: true
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Lights Automation Manager", install: true, uninstall: true) {
        section("Desk Lighting") {
            input "deskMotion", "capability.motionSensor", title: "Desk Motion Sensor", required: false
            input "deskButton", "capability.pushableButton", title: "Desk Button", required: false
            input "deskLight", "capability.switchLevel", title: "Desk Light (with CT/Color)", required: false
        }
        
        section("Light Strips") {
            input "lightStrip", "capability.colorControl", title: "Main Light Strip", required: false
            input "lanStrip", "capability.colorControl", title: "LAN Strip", required: false
        }
        
        section("Generic Switches & Outlets") {
            input "genericSwitches", "capability.switch", title: "Generic Switches/Outlets (mode-controlled)", multiple: true, required: false
        }
        
        section("Flood Lights") {
            input "floodRear", "capability.switch", title: "Rear Flood Light", required: false
            input "floodSide", "capability.switch", title: "Side Flood Light", required: false
            input "floodShower", "capability.switch", title: "Shower Flood Light", required: false
            input "floodWoodshed", "capability.switch", title: "Woodshed Flood Light", required: false
            input "floodOffice", "capability.switch", title: "Office Flood Light", required: false
            input "floodCarport", "capability.switch", title: "Carport Flood Light", required: false
        }
        
        section("Flood Motion Sensors") {
            input "rearFloodMotion", "capability.motionSensor", title: "Rear Flood Motion Sensor", required: false
            input "sideFloodMotion", "capability.motionSensor", title: "Side Flood Motion Sensor", required: false
            input "officeFloodMotion", "capability.motionSensor", title: "Office Flood Motion Sensor", required: false
        }
        
        section("Master Light Groups") {
            input "allLights", "capability.switch", title: "All Lights Group", multiple: true, required: false
            input "allLightsSwitch", "capability.switch", title: "All Lights Master Switch", required: false
        }
        
        section("Condition Switches") {
            input "onPTO", "capability.switch", title: "On PTO Switch", required: false
            paragraph "<small><i>Note: PTO switch automatically turns ON at sunset on Fridays and OFF at sunrise on Sundays</i></small>"
            input "holiday", "capability.switch", title: "Holiday Switch", required: false
        }
        
        section("Cross-App Communication") {
            input "emergencyLightTrigger", "capability.switch", title: "Emergency Light Trigger (receives from other apps)", required: false
        }
        
        section("Lighting Configuration") {
            input "deskBrightLevel", "number", title: "Desk Bright Level (0-100)", defaultValue: 100, range: "0..100", required: false
            input "deskDimLevel", "number", title: "Desk Dim Level (0-100)", defaultValue: 5, range: "0..100", required: false
            input "deskColorTemp", "number", title: "Desk Color Temperature (K)", defaultValue: 2700, range: "2000..6500", required: false
            input "stripNightLevel", "number", title: "Strip Night Mode Level (0-100)", defaultValue: 30, range: "0..100", required: false
            input "stripDayLevel", "number", title: "Strip Day Mode Level (0-100)", defaultValue: 50, range: "0..100", required: false
        }
        
        section("Motion Timeout Configuration") {
            input "floodTimeout", "number", title: "Flood Light Motion Timeout (minutes)", defaultValue: 5, required: false
        }
        
        section("Hub Variable Overrides") {
            paragraph "This app supports hub variable overrides for flexible configuration:"
            paragraph "• FloodTimeout - Override motion-activated flood timeout (minutes)"
            paragraph "• StripColorNight - Override nighttime strip color (color name)"
        }
        
        section("Logging") {
            input "logEnable", "bool", title: "Enable debug logging", defaultValue: true
            input "infoEnable", "bool", title: "Enable info logging", defaultValue: true
        }
    }
}

def installed() {
    logInfo "Lights Automation Manager installed"
    initialize()
}

def updated() {
    logInfo "Lights Automation Manager updated"
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    logInfo "Initializing Lights Automation Manager"
    
    // Mode changes
    subscribe(location, "mode", modeHandler)
    
    // Desk controls
    if (deskMotion) subscribe(deskMotion, "motion.active", deskMotionHandler)
    if (deskButton) {
        subscribe(deskButton, "pushed", deskButtonHandler)
        subscribe(deskButton, "doubleTapped", deskButtonHandler)
    }
    
    // Flood motion sensors
    if (rearFloodMotion) subscribe(rearFloodMotion, "motion.active", rearFloodMotionHandler)
    if (sideFloodMotion) subscribe(sideFloodMotion, "motion.active", sideFloodMotionHandler)
    if (officeFloodMotion) subscribe(officeFloodMotion, "motion.active", officeFloodMotionHandler)
    
    // Cross-app communication
    if (emergencyLightTrigger) subscribe(emergencyLightTrigger, "switch.on", handleEmergencyLightTrigger)
    
    // All lights master switch
    if (allLightsSwitch) subscribe(allLightsSwitch, "switch", allLightsSwitchHandler)
    
    // Schedule PTO weekend automation (on at sunset Friday, off at sunrise Sunday)
    if (onPTO) {
        schedulePTOWeekend()
        subscribe(location, "sunsetTime", sunsetTimeHandler)
        subscribe(location, "sunriseTime", sunriseTimeHandler)
    }
}

// ========================================
// MODE CHANGE HANDLER
// ========================================

def modeHandler(evt) {
    String mode = evt.value?.trim()
    logInfo "Mode changed to: ${mode}"
    
    switch(mode) {
        case "Night":
            handleNightMode()
            break
        case "Evening":
            handleEveningMode()
            break
        case "Morning":
            handleMorningMode()
            break
        case "Day":
            handleDayMode()
            break
        default:
            logDebug "No specific lighting automation for mode: ${mode}"
    }
}

def handleNightMode() {
    logInfo "Executing Night mode lighting"
    
    // Turn off generic switches
    if (genericSwitches) {
        genericSwitches.off()
        logDebug "Turned off ${genericSwitches.size()} generic switches"
    }
    
    // Set light strips to blue at night level
    Integer nightLevel = getConfigValue("stripNightLevel", "StripNightLevel") as Integer
    String nightColor = getConfigValue("stripColorNight", "StripColorNight") ?: "Blue"
    
    setStrip(lightStrip, nightColor, nightLevel)
    setStrip(lanStrip, nightColor, nightLevel)
    
    // Set desk light to dimmest
    setDeskLight(getConfigValue("deskDimLevel", "DeskDimLevel") as Integer)
}

def handleEveningMode() {
    logInfo "Executing Evening mode lighting"
    
    // Turn on generic switches
    if (genericSwitches) {
        genericSwitches.each { device ->
            logDebug "Turning on ${device.displayName}"
            device.on()
        }
    }
    
    // Set light strips
    Integer dayLevel = getConfigValue("stripDayLevel", "StripDayLevel") as Integer
    setStrip(lightStrip, "Soft White", dayLevel)
    setStrip(lanStrip, "Yellow", 96)
}

def handleMorningMode() {
    logInfo "Executing Morning mode lighting"
    
    // Check conditions
    Boolean holidayOn = holiday && holiday.currentValue("switch") == "on"
    Boolean ptoOn = onPTO && onPTO.currentValue("switch") == "on"
    
    if (!holidayOn && !ptoOn) {
        logDebug "Morning conditions met (not holiday, not PTO) - turning on lights"
        
        // Turn on generic switches
        if (genericSwitches) genericSwitches.on()
        
        // Set light strips
        Integer dayLevel = getConfigValue("stripDayLevel", "StripDayLevel") as Integer
        setStrip(lightStrip, "Soft White", dayLevel)
        setStrip(lanStrip, "Yellow", 96)
    } else {
        logInfo "Morning lights skipped (Holiday: ${holidayOn}, PTO: ${ptoOn})"
        
        // Still update light strip per mode
        Integer dayLevel = getConfigValue("stripDayLevel", "StripDayLevel") as Integer
        setStrip(lightStrip, "Soft White", dayLevel)
    }
}

def handleDayMode() {
    logInfo "Executing Day mode lighting"
    
    // Turn off all mode-controlled lights
    if (genericSwitches) genericSwitches.off()
    if (lightStrip) lightStrip.off()
    if (lanStrip) lanStrip.off()
}

// ========================================
// DESK LIGHTING HANDLERS
// ========================================

def deskMotionHandler(evt) {
    logDebug "Desk motion detected"
    Integer dimLevel = getConfigValue("deskDimLevel", "DeskDimLevel") as Integer
    setDeskLight(dimLevel)
}

def deskButtonHandler(evt) {
    logInfo "Desk button event: ${evt.name} - ${evt.value}"
    
    // Only react to Button 1
    if (evt.value != "1") {
        logDebug "Ignoring button ${evt.value}"
        return
    }
    
    if (evt.name == "pushed") {
        // Single push = bright
        Integer brightLevel = getConfigValue("deskBrightLevel", "DeskBrightLevel") as Integer
        setDeskLight(brightLevel)
    } else if (evt.name == "doubleTapped") {
        // Double tap = dim
        Integer dimLevel = getConfigValue("deskDimLevel", "DeskDimLevel") as Integer
        setDeskLight(dimLevel)
    }
}

def setDeskLight(Integer level = null) {
    if (!deskLight) {
        logDebug "No desk light configured"
        return
    }
    
    Integer finalLevel = level ?: (getConfigValue("deskDimLevel", "DeskDimLevel") as Integer)
    Integer colorTemp = getConfigValue("deskColorTemp", "DeskColorTemp") as Integer
    
    logDebug "Setting desk light to level ${finalLevel}, CT ${colorTemp}K"
    
    deskLight.setLevel(finalLevel)
    
    if (deskLight.hasCommand("setColorTemperature")) {
        deskLight.setColorTemperature(colorTemp)
    }
}

def setDeskLightRed() {
    if (!deskLight) {
        logDebug "No desk light configured"
        return
    }
    
    logInfo "Setting desk light to RED (emergency indicator)"
    
    if (deskLight.hasCommand("setColor")) {
        deskLight.setColor([hue: 0, saturation: 100, level: 100])
    } else if (deskLight.hasCommand("setLevel")) {
        deskLight.setLevel(100)
    } else {
        deskLight.on()
    }
}

// ========================================
// LIGHT STRIP HANDLERS
// ========================================

def setStrip(device, String colorName, Integer level) {
    if (!device) {
        logDebug "setStrip called with null device"
        return
    }
    
    logDebug "setStrip: ${device.displayName} -> ${colorName} @ ${level}%"
    
    if (level == 0) {
        device.off()
        return
    }
    
    // Ensure device is on
    if (device.currentValue("switch") != "on") {
        logDebug "Turning on ${device.displayName}"
        device.on()
        pauseExecution(500)
    }
    
    // Set level
    device.setLevel(level)
    
    // Set color
    switch(colorName) {
        case "Blue":
            device.setColor([hue: 66, saturation: 100, level: level])
            break
        case "Soft White":
            if (device.hasCommand("setColorTemperature")) {
                device.setColorTemperature(2700)
            } else {
                device.setColor([hue: 23, saturation: 56, level: level])
            }
            break
        case "Yellow":
            device.setColor([hue: 18, saturation: 19, level: level])
            break
        case "Green":
            device.setColor([hue: 33, saturation: 100, level: level])
            break
        case "Red":
            device.setColor([hue: 0, saturation: 100, level: level])
            break
        case "White":
            if (device.hasCommand("setColorTemperature")) {
                device.setColorTemperature(5000)
            } else {
                device.setColor([hue: 0, saturation: 0, level: level])
            }
            break
        default:
            logDebug "Unknown color: ${colorName}"
    }
}

def setStripColor(String color) {
    logInfo "Setting light strips to ${color}"
    setStrip(lightStrip, color, 50)
    setStrip(lanStrip, color, 50)
}

// ========================================
// FLOOD LIGHT HANDLERS
// ========================================

def rearFloodMotionHandler(evt) {
    logInfo "Rear flood motion detected"
    handleFloodMotion(floodRear, "Rear")
}

def sideFloodMotionHandler(evt) {
    logInfo "Side flood motion detected"
    handleFloodMotion(floodSide, "Side")
}

def officeFloodMotionHandler(evt) {
    logInfo "Office flood motion detected"
    handleFloodMotion(floodOffice, "Office")
}

def handleFloodMotion(floodLight, String location) {
    if (!floodLight) {
        logDebug "No flood light configured for ${location}"
        return
    }
    
    logInfo "Turning on ${location} flood light"
    floodLight.on()
    
    // Schedule auto-off
    Integer timeout = getConfigValue("floodTimeout", "FloodTimeout") as Integer
    Integer timeoutSeconds = timeout * 60
    
    logDebug "${location} flood will turn off in ${timeout} minutes"
    runIn(timeoutSeconds, "turnOffFlood${location}")
}

def turnOffFloodRear() {
    logDebug "Auto-turning off rear flood"
    floodRear?.off()
}

def turnOffFloodSide() {
    logDebug "Auto-turning off side flood"
    floodSide?.off()
}

def turnOffFloodOffice() {
    logDebug "Auto-turning off office flood"
    floodOffice?.off()
}

// ========================================
// MASTER LIGHT CONTROLS
// ========================================

def allLightsSwitchHandler(evt) {
    logInfo "All lights master switch: ${evt.value}"
    
    if (evt.value == "on") {
        allLightsOn()
    } else {
        allLightsOff()
    }
}

def allLightsOn() {
    logInfo "Turning on all lights"
    
    if (allLights) {
        allLights.each { light ->
            logDebug "Turning on ${light.displayName}"
            light.on()
        }
    }
    
    // Also turn on strips
    if (lightStrip) lightStrip.on()
    if (lanStrip) lanStrip.on()
    
    // Desk light
    Integer brightLevel = getConfigValue("deskBrightLevel", "deskBrightLevel") as Integer
    setDeskLight(brightLevel)
}

def allLightsOff() {
    logInfo "Turning off all lights"
    
    if (allLights) {
        allLights.each { light ->
            logDebug "Turning off ${light.displayName}"
            light.off()
        }
    }
    
    // Turn off strips
    if (lightStrip) lightStrip.off()
    if (lanStrip) lanStrip.off()
    
    // Turn off desk
    if (deskLight) deskLight.off()
}

// ========================================
// CROSS-APP COMMUNICATION
// ========================================

def handleEmergencyLightTrigger(evt) {
    logInfo "Emergency light trigger received"
    
    // Turn on all lights for emergency
    allLightsOn()
    
    // Set strips to red
    setStrip(lightStrip, "Red", 100)
    setStrip(lanStrip, "Red", 100)
    
    // Auto-reset trigger
    runIn(2, resetEmergencyTrigger)
}

def resetEmergencyTrigger() {
    emergencyLightTrigger?.off()
}

// ========================================
// PTO WEEKEND SCHEDULING
// ========================================

def schedulePTOWeekend() {
    logInfo "Scheduling PTO weekend automation"
    
    // Schedule sunset check for Fridays
    def sunsetTime = location.sunset
    if (sunsetTime) {
        schedule(sunsetTime, "checkFridaySunset")
        logDebug "Scheduled sunset check at ${sunsetTime}"
    }
    
    // Schedule sunrise check for Sundays
    def sunriseTime = location.sunrise
    if (sunriseTime) {
        schedule(sunriseTime, "checkSundaySunrise")
        logDebug "Scheduled sunrise check at ${sunriseTime}"
    }
}

def sunsetTimeHandler(evt) {
    logDebug "Sunset time updated, rescheduling PTO automation"
    schedulePTOWeekend()
}

def sunriseTimeHandler(evt) {
    logDebug "Sunrise time updated, rescheduling PTO automation"
    schedulePTOWeekend()
}

def checkFridaySunset() {
    def dayOfWeek = new Date().format("EEEE", location.timeZone)
    logDebug "Sunset check - Day of week: ${dayOfWeek}"
    
    if (dayOfWeek == "Friday") {
        logInfo "Friday sunset - turning ON PTO switch"
        onPTO?.on()
    }
    
    // Reschedule for tomorrow's sunset
    schedulePTOWeekend()
}

def checkSundaySunrise() {
    def dayOfWeek = new Date().format("EEEE", location.timeZone)
    logDebug "Sunrise check - Day of week: ${dayOfWeek}"
    
    if (dayOfWeek == "Sunday") {
        logInfo "Sunday sunrise - turning OFF PTO switch"
        onPTO?.off()
    }
    
    // Reschedule for tomorrow's sunrise
    schedulePTOWeekend()
}

// ========================================
// HELPER METHODS
// ========================================

def getConfigValue(String settingName, String hubVarName) {
    // Try to get value from hub variable first
    def hubVarValue = getHubVar(hubVarName)
    if (hubVarValue != null) {
        logDebug "Using hub variable '${hubVarName}' = ${hubVarValue}"
        return hubVarValue
    }
    
    // Fall back to setting value
    def settingValue = settings[settingName]
    logDebug "Using setting '${settingName}' = ${settingValue}"
    return settingValue
}

def getHubVar(String varName, defaultValue = null) {
    try {
        def value = getGlobalVar(varName)?.value
        return value ?: defaultValue
    } catch (e) {
        logDebug "Hub variable '${varName}' not found, using default: ${defaultValue}"
        return defaultValue
    }
}

def setHubVar(String varName, String value) {
    try {
        setGlobalVar(varName, value)
        logDebug "Set hub variable '${varName}' = ${value}"
        return true
    } catch (e) {
        logDebug "Failed to set hub variable '${varName}': ${e.message}"
        return false
    }
}

def isSwitchOn(device) {
    return device && device.currentValue("switch") == "on"
}

// ========================================
// LOGGING
// ========================================

def logDebug(msg) {
    if (logEnable) log.debug "${app.label}: ${msg}"
}

def logInfo(msg) {
    if (infoEnable) log.info "${app.label}: ${msg}"
}
