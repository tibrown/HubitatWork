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
        section("<b>═══════════════════════════════════════</b>\n<b>DESK LIGHTING</b>\n<b>═══════════════════════════════════════</b>") {
            input "deskMotion", "capability.motionSensor", title: "Desk Motion Sensor", required: false
            input "deskButton", "capability.pushableButton", title: "Desk Button", required: false
            input "deskLight", "capability.switchLevel", title: "Desk Light (with CT/Color)", required: false
            paragraph ""
            input "deskBrightLevel", "number", title: "Desk Bright Level (0-100)", defaultValue: 100, range: "0..100", required: false
            input "deskDimLevel", "number", title: "Desk Dim Level (0-100)", defaultValue: 5, range: "0..100", required: false
            input "deskColorTemp", "number", title: "Desk Color Temperature (K)", defaultValue: 2700, range: "2000..6500", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>LIGHT STRIPS - DEVICES</b>\n<b>═══════════════════════════════════════</b>") {
            input "lightStrip", "capability.colorControl", title: "Main Light Strip", required: false
            input "lanStrip", "capability.colorControl", title: "LAN Strip", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>LIGHT STRIPS - MORNING SETTINGS</b>\n<b>═══════════════════════════════════════</b>") {
            input "lightStripMorningColor", "enum", title: "Light Strip Morning Color", options: ["Blue", "Soft White", "Yellow", "Green", "Red", "White", "Custom"], defaultValue: "Soft White", required: false
            input "lightStripMorningCustomColor", "color", title: "Light Strip Morning Custom Color (if Custom selected)", required: false
            input "lightStripMorningLevel", "number", title: "Light Strip Morning Brightness (0-100) - applies to all colors", defaultValue: 50, range: "0..100", required: false
            paragraph ""
            input "lanStripMorningColor", "enum", title: "LAN Strip Morning Color", options: ["Blue", "Soft White", "Yellow", "Green", "Red", "White", "Custom"], defaultValue: "Yellow", required: false
            input "lanStripMorningCustomColor", "color", title: "LAN Strip Morning Custom Color (if Custom selected)", required: false
            input "lanStripMorningLevel", "number", title: "LAN Strip Morning Brightness (0-100) - applies to all colors", defaultValue: 96, range: "0..100", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>MORNING COLOR SWITCHES</b>\n<b>═══════════════════════════════════════</b>") {
            input "morningColorSwitches", "capability.switch", title: "Morning Color Switches (when turned on, set strips to morning colors)", multiple: true, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>LIGHT STRIPS - EVENING SETTINGS</b>\n<b>═══════════════════════════════════════</b>") {
            input "lightStripEveningColor", "enum", title: "Light Strip Evening Color", options: ["Blue", "Soft White", "Yellow", "Green", "Red", "White", "Custom"], defaultValue: "Soft White", required: false
            input "lightStripEveningCustomColor", "color", title: "Light Strip Evening Custom Color (if Custom selected)", required: false
            input "lightStripEveningLevel", "number", title: "Light Strip Evening Brightness (0-100) - applies to all colors", defaultValue: 50, range: "0..100", required: false
            paragraph ""
            input "lanStripEveningColor", "enum", title: "LAN Strip Evening Color", options: ["Blue", "Soft White", "Yellow", "Green", "Red", "White", "Custom"], defaultValue: "Yellow", required: false
            input "lanStripEveningCustomColor", "color", title: "LAN Strip Evening Custom Color (if Custom selected)", required: false
            input "lanStripEveningLevel", "number", title: "LAN Strip Evening Brightness (0-100) - applies to all colors", defaultValue: 96, range: "0..100", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>EVENING COLOR SWITCHES</b>\n<b>═══════════════════════════════════════</b>") {
            input "eveningColorSwitches", "capability.switch", title: "Evening Color Switches (when turned on, set strips to evening colors)", multiple: true, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>LIGHT STRIPS - NIGHT SETTINGS</b>\n<b>═══════════════════════════════════════</b>") {
            input "lightStripNightColor", "enum", title: "Light Strip Night Color", options: ["Blue", "Soft White", "Yellow", "Green", "Red", "White", "Custom"], defaultValue: "Blue", required: false
            input "lightStripNightCustomColor", "color", title: "Light Strip Night Custom Color (if Custom selected)", required: false
            input "lightStripNightLevel", "number", title: "Light Strip Night Brightness (0-100) - applies to all colors", defaultValue: 30, range: "0..100", required: false
            paragraph ""
            input "lanStripNightColor", "enum", title: "LAN Strip Night Color", options: ["Blue", "Soft White", "Yellow", "Green", "Red", "White", "Custom"], defaultValue: "Blue", required: false
            input "lanStripNightCustomColor", "color", title: "LAN Strip Night Custom Color (if Custom selected)", required: false
            input "lanStripNightLevel", "number", title: "LAN Strip Night Brightness (0-100) - applies to all colors", defaultValue: 30, range: "0..100", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>NIGHT COLOR SWITCHES</b>\n<b>═══════════════════════════════════════</b>") {
            input "nightColorSwitches", "capability.switch", title: "Night Color Switches (when turned on, set strips to night colors)", multiple: true, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>GENERIC SWITCHES & OUTLETS</b>\n<b>═══════════════════════════════════════</b>") {
            input "genericSwitches", "capability.switch", title: "Generic Switches/Outlets (mode-controlled)", multiple: true, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>FLOOD LIGHTS</b>\n<b>═══════════════════════════════════════</b>") {
            input "floodRear", "capability.switch", title: "Rear Flood Light", required: false
            input "floodSide", "capability.switch", title: "Side Flood Light", required: false
            input "floodShower", "capability.switch", title: "Shower Flood Light", required: false
            input "floodWoodshed", "capability.switch", title: "Woodshed Flood Light", required: false
            input "floodOffice", "capability.switch", title: "Office Flood Light", required: false
            input "floodCarport", "capability.switch", title: "Carport Flood Light", required: false
            input "turnFloodsOn", "capability.switch", title: "Turn Floods On Switch (turns on all floods)", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>FLOOD MOTION SENSORS</b>\n<b>═══════════════════════════════════════</b>") {
            input "rearFloodMotion", "capability.motionSensor", title: "Rear Flood Motion Sensor", required: false
            input "sideFloodMotion", "capability.motionSensor", title: "Side Flood Motion Sensor", required: false
            input "officeFloodMotion", "capability.motionSensor", title: "Office Flood Motion Sensor", required: false
            paragraph ""
            input "floodTimeout", "number", title: "Flood Light Motion Timeout (minutes)", defaultValue: 5, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>MASTER LIGHT CONTROLS</b>\n<b>═══════════════════════════════════════</b>") {
            input "allLights", "capability.switch", title: "All Lights Group", multiple: true, required: false
            input "allLightsSwitch", "capability.switch", title: "All Lights Master Switch", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>CONDITION SWITCHES</b>\n<b>═══════════════════════════════════════</b>") {
            input "onPTO", "capability.switch", title: "On PTO Switch", required: false
            paragraph "<small><i>Note: PTO switch automatically turns ON at sunset on Fridays and OFF at sunrise on Sundays</i></small>"
            input "holiday", "capability.switch", title: "Holiday Switch", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>CROSS-APP COMMUNICATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "emergencyLightTrigger", "capability.switch", title: "Emergency Light Trigger (receives from other apps)", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>MODE TIMING ADJUSTMENTS</b>\n<b>═══════════════════════════════════════</b>") {
            input "eveningModeAdvance", "number", title: "Evening Mode Advance (minutes)", description: "Turn on Evening lights this many minutes BEFORE mode changes to Evening", defaultValue: 0, range: "0..60", required: false
            input "dayModeDelay", "number", title: "Day Mode Delay (minutes)", description: "Wait time after mode becomes Day before adjusting lights", defaultValue: 0, range: "0..60", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>LOGGING</b>\n<b>═══════════════════════════════════════</b>") {
            input "logEnable", "bool", title: "Enable debug logging", defaultValue: true
            input "infoEnable", "bool", title: "Enable info logging", defaultValue: true
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>ADVANCED SETTINGS</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "<i>Fine-tune device control timing to optimize reliability for your mesh network. Default values work for most setups.</i>"
            input "batchDelay", "number", title: "Batch Delay (milliseconds)", description: "Delay between each device command when controlling multiple devices (prevents mesh flooding)", defaultValue: 300, required: false
            input "verificationWait", "number", title: "Verification Wait (milliseconds)", description: "Time to wait after sending commands before verifying device states", defaultValue: 3000, required: false
            input "retryDelay", "number", title: "Retry Delay (milliseconds)", description: "Time to wait before retrying failed devices", defaultValue: 2000, required: false
            input "enableDiagnostics", "bool", title: "Enable Diagnostic Logging?", description: "Detailed logging to troubleshoot device control issues", defaultValue: false, required: false
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
    
    // Apply current mode lighting settings immediately when Done is clicked
    applyCurrentModeLighting()
}

def applyCurrentModeLighting() {
    String currentMode = location.mode
    logInfo "Applying lighting settings for current mode: ${currentMode}"
    
    switch(currentMode) {
        case "Night":
            handleNightMode()
            break
        case "Evening":
            executeEveningModeLighting()
            break
        case "Morning":
            handleMorningMode()
            break
        case "Day":
            // Day mode turns lights off, so don't reapply unless user wants that
            logDebug "Current mode is Day - not reapplying (lights would turn off)"
            break
        default:
            logDebug "No specific lighting to apply for mode: ${currentMode}"
    }
}

def initialize() {
    logInfo "Initializing Lights Automation Manager"
    
    // Mode changes
    subscribe(location, "mode", modeHandler)
    
    // Schedule Evening mode advance if configured
    scheduleEveningAdvance()
    
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
    
    // Turn floods on switch
    if (turnFloodsOn) subscribe(turnFloodsOn, "switch.on", turnFloodsOnHandler)
    
    // Color mode switches
    if (morningColorSwitches) subscribe(morningColorSwitches, "switch.on", morningColorSwitchHandler)
    if (eveningColorSwitches) subscribe(eveningColorSwitches, "switch.on", eveningColorSwitchHandler)
    if (nightColorSwitches) subscribe(nightColorSwitches, "switch.on", nightColorSwitchHandler)
    
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
    // Prevent cascading calls within 10 seconds
    if (atomicState.lastNightMode && (now() - atomicState.lastNightMode) < 10000) {
        logDebug "Ignoring handleNightMode - already executed recently"
        return
    }
    atomicState.lastNightMode = now()
    
    logInfo "Executing Night mode lighting"
    def diagnostics = settings.enableDiagnostics != null ? settings.enableDiagnostics : false
    
    // Turn off generic switches (exclude light strips to avoid conflicts)
    if (genericSwitches) {
        def switchesToControl = genericSwitches.findAll { it.id != lightStrip?.id && it.id != lanStrip?.id }
        if (diagnostics) logDeviceStates(switchesToControl, "GenericSwitches-Night")
        turnOffDevicesWithRetry(switchesToControl, "GenericSwitches")
        logDebug "Turned off ${switchesToControl.size()} generic switches (excluded strips)"
    }
    
    // Set light strips with individual settings - with safe defaults
    def lightStripColorInfo = resolveColor("lightStripNightColor", "lightStripNightCustomColor", "Blue")
    Integer lightStripLevel = (settings.lightStripNightLevel ?: 30) as Integer
    def lanStripColorInfo = resolveColor("lanStripNightColor", "lanStripNightCustomColor", "Blue")
    Integer lanStripLevel = (settings.lanStripNightLevel ?: 30) as Integer
    
    logInfo "Night mode - Light strip: ${lightStripColorInfo.name}@${lightStripLevel}%, LAN strip: ${lanStripColorInfo.name}@${lanStripLevel}%"
    
    setStripWithColor(lightStrip, lightStripColorInfo, lightStripLevel)
    setStripWithColor(lanStrip, lanStripColorInfo, lanStripLevel)
    
    // Set desk light to dimmest
    setDeskLight(settings.deskDimLevel as Integer)
    
    // Turn off all flood lights for night mode (they will activate on motion detection)
    def floodLights = [
        floodRear,
        floodSide,
        floodShower,
        floodWoodshed,
        floodOffice,
        floodCarport
    ].findAll { it != null }
    
    if (floodLights) {
        if (diagnostics) logDeviceStates(floodLights, "Floods-Night")
        turnOffDevicesWithRetry(floodLights, "Floods")
        logInfo "Turned off ${floodLights.size()} flood light(s) for night mode (will activate on motion)"
    }
}

def handleEveningMode() {
    // Prevent cascading calls within 10 seconds
    if (atomicState.lastEveningMode && (now() - atomicState.lastEveningMode) < 10000) {
        logDebug "Ignoring handleEveningMode - already executed recently"
        return
    }
    atomicState.lastEveningMode = now()
    
    logInfo "Executing Evening mode lighting"
    
    // Cancel any pending advance execution (in case mode changed manually before scheduled time)
    unschedule("executeEveningModeLighting")
    
    executeEveningModeLighting()
}

def executeEveningModeLighting() {
    logInfo "Executing Evening mode lighting adjustments"
    def diagnostics = settings.enableDiagnostics != null ? settings.enableDiagnostics : false
    
    // Turn on generic switches (exclude light strips to avoid conflicts)
    if (genericSwitches) {
        def switchesToControl = genericSwitches.findAll { it.id != lightStrip?.id && it.id != lanStrip?.id }
        if (diagnostics) logDeviceStates(switchesToControl, "GenericSwitches-Evening")
        turnOnDevicesWithRetry(switchesToControl, "GenericSwitches")
        logDebug "Turned on ${switchesToControl.size()} generic switches (excluded strips)"
    }
    
    // Set light strips with individual settings - with safe defaults
    def lightStripColorInfo = resolveColor("lightStripEveningColor", "lightStripEveningCustomColor", "Soft White")
    Integer lightStripLevel = (settings.lightStripEveningLevel ?: 50) as Integer
    def lanStripColorInfo = resolveColor("lanStripEveningColor", "lanStripEveningCustomColor", "Yellow")
    Integer lanStripLevel = (settings.lanStripEveningLevel ?: 96) as Integer
    
    logInfo "Evening mode - Light strip: ${lightStripColorInfo.name}@${lightStripLevel}%, LAN strip: ${lanStripColorInfo.name}@${lanStripLevel}%"
    
    setStripWithColor(lightStrip, lightStripColorInfo, lightStripLevel)
    setStripWithColor(lanStrip, lanStripColorInfo, lanStripLevel)
    
    // Turn on all flood lights for evening security
    def floodLights = [
        floodRear,
        floodSide,
        floodShower,
        floodWoodshed,
        floodOffice,
        floodCarport
    ].findAll { it != null }
    
    if (floodLights) {
        if (diagnostics) logDeviceStates(floodLights, "Floods-Evening")
        // Turn on floods - if they support level, set to 100%
        floodLights.each { flood ->
            if (flood.hasCapability("SwitchLevel")) {
                flood.setLevel(100)
            } else {
                flood.on()
            }
        }
        logInfo "Turned on ${floodLights.size()} flood light(s) for evening mode"
    }
}

def handleMorningMode() {
    // Prevent cascading calls within 10 seconds
    if (atomicState.lastMorningMode && (now() - atomicState.lastMorningMode) < 10000) {
        logDebug "Ignoring handleMorningMode - already executed recently"
        return
    }
    atomicState.lastMorningMode = now()
    
    logInfo "Executing Morning mode lighting"
    
    // Check conditions
    Boolean holidayOn = holiday && holiday.currentValue("switch") == "on"
    Boolean ptoOn = onPTO && onPTO.currentValue("switch") == "on"
    
    if (!holidayOn && !ptoOn) {
        logDebug "Morning conditions met (not holiday, not PTO) - turning on lights"
        def diagnostics = settings.enableDiagnostics != null ? settings.enableDiagnostics : false
        
        // Turn on generic switches (exclude light strips to avoid conflicts)
        if (genericSwitches) {
            def switchesToControl = genericSwitches.findAll { it.id != lightStrip?.id && it.id != lanStrip?.id }
            if (diagnostics) logDeviceStates(switchesToControl, "GenericSwitches-Morning")
            turnOnDevicesWithRetry(switchesToControl, "GenericSwitches")
            logDebug "Turned on ${switchesToControl.size()} generic switches (excluded strips)"
        }
        
        // Set light strips with individual settings - with safe defaults
        def lightStripColorInfo = resolveColor("lightStripMorningColor", "lightStripMorningCustomColor", "Soft White")
        Integer lightStripLevel = (settings.lightStripMorningLevel ?: 50) as Integer
        def lanStripColorInfo = resolveColor("lanStripMorningColor", "lanStripMorningCustomColor", "Yellow")
        Integer lanStripLevel = (settings.lanStripMorningLevel ?: 96) as Integer
        
        logInfo "Morning mode - Light strip: ${lightStripColorInfo.name}@${lightStripLevel}%, LAN strip: ${lanStripColorInfo.name}@${lanStripLevel}%"
        
        setStripWithColor(lightStrip, lightStripColorInfo, lightStripLevel)
        setStripWithColor(lanStrip, lanStripColorInfo, lanStripLevel)
    } else {
        logInfo "Morning lights skipped (Holiday: ${holidayOn}, PTO: ${ptoOn})"
        
        // Still update light strip per mode - with safe defaults
        def lightStripColorInfo = resolveColor("lightStripMorningColor", "lightStripMorningCustomColor", "Soft White")
        Integer lightStripLevel = (settings.lightStripMorningLevel ?: 50) as Integer
        
        logInfo "Morning mode (holiday/PTO) - Light strip only: ${lightStripColorInfo.name}@${lightStripLevel}%"
        
        setStripWithColor(lightStrip, lightStripColorInfo, lightStripLevel)
    }
}

def handleDayMode() {
    // Prevent cascading calls within 10 seconds
    if (atomicState.lastDayMode && (now() - atomicState.lastDayMode) < 10000) {
        logDebug "Ignoring handleDayMode - already executed recently"
        return
    }
    atomicState.lastDayMode = now()
    
    logInfo "Executing Day mode lighting"
    
    // Check if delay is configured
    Integer delayMinutes = settings.dayModeDelay ?: 0
    
    if (delayMinutes > 0) {
        logInfo "Day mode: Scheduling light adjustments in ${delayMinutes} minute(s)"
        runIn(delayMinutes * 60, executeDayModeLighting)
    } else {
        // No delay, execute immediately
        executeDayModeLighting()
    }
}

def executeDayModeLighting() {
    logInfo "Executing Day mode lighting adjustments"
    def diagnostics = settings.enableDiagnostics != null ? settings.enableDiagnostics : false
    
    // Turn off all mode-controlled lights
    if (genericSwitches) {
        def switchesToControl = genericSwitches.findAll { it.id != lightStrip?.id && it.id != lanStrip?.id }
        if (diagnostics) logDeviceStates(switchesToControl, "GenericSwitches-Day")
        turnOffDevicesWithRetry(switchesToControl, "GenericSwitches")
        logDebug "Turned off ${switchesToControl.size()} generic switches (excluded strips)"
    }
    
    // Turn off light strips
    if (lightStrip || lanStrip) {
        def strips = [lightStrip, lanStrip].findAll { it != null }
        if (diagnostics) logDeviceStates(strips, "LightStrips-Day")
        turnOffDevicesWithRetry(strips, "LightStrips")
    }
    
    // Turn off all flood lights during day
    def floodLights = [
        floodRear,
        floodSide,
        floodShower,
        floodWoodshed,
        floodOffice,
        floodCarport
    ].findAll { it != null }
    
    if (floodLights) {
        if (diagnostics) logDeviceStates(floodLights, "Floods-Day")
        turnOffDevicesWithRetry(floodLights, "Floods")
        logInfo "Turned off ${floodLights.size()} flood light(s) for day mode"
    }
}

// ========================================
// DESK LIGHTING HANDLERS
// ========================================

def deskMotionHandler(evt) {
    logDebug "Desk motion detected"
    Integer dimLevel = settings.deskDimLevel as Integer
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
        Integer brightLevel = settings.deskBrightLevel as Integer
        setDeskLight(brightLevel)
    } else if (evt.name == "doubleTapped") {
        // Double tap = dim
        Integer dimLevel = settings.deskDimLevel as Integer
        setDeskLight(dimLevel)
    }
}

def setDeskLight(Integer level = null) {
    if (!deskLight) {
        logDebug "No desk light configured"
        return
    }
    
    Integer finalLevel = level ?: (settings.deskDimLevel as Integer)
    Integer colorTemp = settings.deskColorTemp as Integer
    
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
    
    // Validate inputs
    if (level == null) {
        logDebug "setStrip: level is null for ${device.displayName}, using default 50"
        level = 50
    }
    if (!colorName) {
        logDebug "setStrip: colorName is null for ${device.displayName}, using default White"
        colorName = "White"
    }
    
    logDebug "setStrip: ${device.displayName} -> ${colorName} @ ${level}%"
    
    if (level == 0) {
        device.off()
        return
    }
    
    // Always ensure device is on and ready before setting color
    // Some devices need a fresh on() command to properly accept color changes
    // even when already on (especially during mode transitions)
    logDebug "Ensuring ${device.displayName} is on and ready"
    device.on()
    pauseExecution(1000) // Wait for device to be ready
    
    // Set color first (which includes level), then confirm level
    // This prevents the setLevel call from being overwritten by setColor
    switch(colorName) {
        case "Blue":
            device.setColor([hue: 66, saturation: 100, level: level])
            break
        case "Soft White":
            if (device.hasCommand("setColorTemperature")) {
                device.setLevel(level)
                pauseExecution(300)
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
                device.setLevel(level)
                pauseExecution(300)
                device.setColorTemperature(5000)
            } else {
                device.setColor([hue: 0, saturation: 0, level: level])
            }
            break
        default:
            logDebug "Unknown color: ${colorName}, defaulting to White"
            if (device.hasCommand("setColorTemperature")) {
                device.setLevel(level)
                pauseExecution(300)
                device.setColorTemperature(5000)
            } else {
                device.setColor([hue: 0, saturation: 0, level: level])
            }
    }
    
    // Small delay to allow command to process
    pauseExecution(300)
    
    // Verify and log the final state
    logDebug "Strip ${device.displayName} final state - Switch: ${device.currentValue('switch')}, Level: ${device.currentValue('level')}"
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
    
    def diagnostics = settings.enableDiagnostics != null ? settings.enableDiagnostics : false
    logInfo "Turning on ${location} flood light"
    if (diagnostics) logDeviceStates([floodLight], "Flood-${location}")
    turnOnDevicesWithRetry([floodLight], "Flood-${location}")
    
    // Schedule auto-off
    Integer timeout = settings.floodTimeout as Integer
    Integer timeoutSeconds = timeout * 60
    
    logDebug "${location} flood will turn off in ${timeout} minutes"
    runIn(timeoutSeconds, "turnOffFlood${location}")
}

def turnOffFloodRear() {
    logDebug "Auto-turning off rear flood"
    if (floodRear) turnOffDevicesWithRetry([floodRear], "Flood-Rear")
}

def turnOffFloodSide() {
    logDebug "Auto-turning off side flood"
    if (floodSide) turnOffDevicesWithRetry([floodSide], "Flood-Side")
}

def turnOffFloodOffice() {
    logDebug "Auto-turning off office flood"
    if (floodOffice) turnOffDevicesWithRetry([floodOffice], "Flood-Office")
}

def turnFloodsOnHandler(evt) {
    logInfo "Turn Floods On switch activated - turning on all flood lights"
    def diagnostics = settings.enableDiagnostics != null ? settings.enableDiagnostics : false
    
    def floodLights = [
        floodRear,
        floodSide,
        floodShower,
        floodWoodshed,
        floodOffice,
        floodCarport
    ].findAll { it != null }
    
    if (floodLights.isEmpty()) {
        logDebug "No flood lights configured"
    } else {
        if (diagnostics) logDeviceStates(floodLights, "AllFloods")
        turnOnDevicesWithRetry(floodLights, "AllFloods")
        logInfo "Turned on ${floodLights.size()} flood light(s)"
    }
    
    // Reset the switch after a short delay
    runIn(2, resetTurnFloodsOnSwitch)
}

def resetTurnFloodsOnSwitch() {
    turnFloodsOn?.off()
    logDebug "Reset Turn Floods On switch"
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
    def diagnostics = settings.enableDiagnostics != null ? settings.enableDiagnostics : false
    
    if (allLights) {
        def lightsToControl = allLights.findAll { 
            it.id != lightStrip?.id && 
            it.id != lanStrip?.id && 
            it.id != floodRear?.id && 
            it.id != floodSide?.id && 
            it.id != floodShower?.id && 
            it.id != floodWoodshed?.id && 
            it.id != floodOffice?.id && 
            it.id != floodCarport?.id 
        }
        if (diagnostics) logDeviceStates(lightsToControl, "AllLights")
        turnOnDevicesWithRetry(lightsToControl, "AllLights")
        logDebug "Turned on ${lightsToControl.size()} lights (excluded strips and floods for separate control)"
    }
    
    // Turn on strips (they'll use their last color/level settings)
    def strips = [lightStrip, lanStrip].findAll { it != null }
    if (strips) {
        if (diagnostics) logDeviceStates(strips, "LightStrips")
        turnOnDevicesWithRetry(strips, "LightStrips")
    }
    
    // Desk light
    Integer brightLevel = settings.deskBrightLevel as Integer
    setDeskLight(brightLevel)
}

def allLightsOff() {
    logInfo "Turning off all lights"
    def diagnostics = settings.enableDiagnostics != null ? settings.enableDiagnostics : false
    
    if (allLights) {
        def lightsToControl = allLights.findAll { 
            it.id != lightStrip?.id && 
            it.id != lanStrip?.id && 
            it.id != floodRear?.id && 
            it.id != floodSide?.id && 
            it.id != floodShower?.id && 
            it.id != floodWoodshed?.id && 
            it.id != floodOffice?.id && 
            it.id != floodCarport?.id 
        }
        if (diagnostics) logDeviceStates(lightsToControl, "AllLights")
        turnOffDevicesWithRetry(lightsToControl, "AllLights")
        logDebug "Turned off ${lightsToControl.size()} lights (excluded strips and floods for separate control)"
    }
    
    // Turn off strips and desk
    def devicesToOff = [lightStrip, lanStrip, deskLight].findAll { it != null }
    if (devicesToOff) {
        if (diagnostics) logDeviceStates(devicesToOff, "StripsAndDesk")
        turnOffDevicesWithRetry(devicesToOff, "StripsAndDesk")
    }
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
// EVENING MODE ADVANCE SCHEDULING
// ========================================

def scheduleEveningAdvance() {
    Integer advanceMinutes = settings.eveningModeAdvance ?: 0
    
    if (advanceMinutes <= 0) {
        logDebug "Evening mode advance disabled (${advanceMinutes} minutes)"
        return
    }
    
    // Get the time when Evening mode typically starts
    // This could be based on sunset or a fixed time depending on your mode manager
    // For now, we'll use a reasonable assumption that Evening mode is tied to sunset
    def sunsetTime = location.sunset
    
    if (sunsetTime) {
        // Calculate the advance time
        def advanceTime = new Date(sunsetTime.time - (advanceMinutes * 60 * 1000))
        
        logInfo "Scheduling Evening lights to turn on at ${advanceTime.format('HH:mm', location.timeZone)} (${advanceMinutes} min before sunset)"
        
        runOnce(advanceTime, "checkAndExecuteEveningAdvance")
    } else {
        logDebug "Unable to determine sunset time for Evening advance scheduling"
    }
}

def checkAndExecuteEveningAdvance() {
    String currentMode = location.mode
    
    logDebug "Evening advance trigger - Current mode: ${currentMode}"
    
    // Only execute if we're not already in Evening or Night mode
    if (currentMode != "Evening" && currentMode != "Night") {
        logInfo "Executing Evening lights in advance of mode change"
        executeEveningModeLighting()
    } else {
        logDebug "Skipping Evening advance - already in ${currentMode} mode"
    }
    
    // Reschedule for tomorrow
    runIn(300, "scheduleEveningAdvance") // Wait 5 minutes then reschedule
}

// ========================================
// HELPER METHODS
// ========================================

// Helper method to log device states for diagnostics
def logDeviceStates(devices, deviceType) {
    if (!devices) return
    def diagnostics = settings.enableDiagnostics != null ? settings.enableDiagnostics : false
    if (!diagnostics) return
    
    def deviceList = devices instanceof List ? devices : [devices]
    deviceList.each { device ->
        def state = device.currentValue("switch")
        logDebug "${deviceType} - ${device.displayName}: ${state}"
    }
}

// Helper method to turn off devices with verification and retry
def turnOffDevicesWithRetry(devices, deviceType, retryCount = 0) {
    if (!devices) return
    
    def batchDelayMs = settings.batchDelay != null ? settings.batchDelay : 300
    def verificationWaitMs = settings.verificationWait != null ? settings.verificationWait : 3000
    def retryDelayMs = settings.retryDelay != null ? settings.retryDelay : 2000
    def diagnostics = settings.enableDiagnostics != null ? settings.enableDiagnostics : false
    
    if (diagnostics) logDebug "turnOffDevicesWithRetry called for ${deviceType}, attempt ${retryCount + 1}"
    
    def deviceList = devices instanceof List ? devices : [devices]
    def failedDevices = []
    
    // Send off commands in batches to prevent mesh flooding
    deviceList.eachWithIndex { device, index ->
        if (diagnostics) logDebug "Turning off ${deviceType}: ${device.displayName}"
        device.off()
        // Small delay between each device to prevent mesh congestion
        if (index < deviceList.size() - 1 && deviceList.size() > 3) {
            pauseExecution(batchDelayMs)
        }
    }
    
    // Wait for commands to process
    pauseExecution(verificationWaitMs)
    
    // Verify all devices turned off
    deviceList.each { device ->
        def currentState = device.currentValue("switch")
        if (currentState != "off") {
            logInfo "WARNING: ${deviceType} ${device.displayName} failed to turn off (state: ${currentState})"
            failedDevices.add(device)
        } else {
            if (diagnostics) logDebug "${deviceType} ${device.displayName} confirmed OFF"
        }
    }
    
    // Retry failed devices once
    if (failedDevices.size() > 0 && retryCount < 1) {
        logInfo "Retrying ${failedDevices.size()} failed ${deviceType} devices"
        pauseExecution(retryDelayMs)
        turnOffDevicesWithRetry(failedDevices, deviceType, retryCount + 1)
    } else if (failedDevices.size() > 0) {
        logInfo "ERROR: ${deviceType} devices still on after retry: ${failedDevices*.displayName.join(', ')}"
    }
}

// Helper method to turn on devices with verification and retry
def turnOnDevicesWithRetry(devices, deviceType, retryCount = 0) {
    if (!devices) return
    
    def batchDelayMs = settings.batchDelay != null ? settings.batchDelay : 300
    def verificationWaitMs = settings.verificationWait != null ? settings.verificationWait : 3000
    def retryDelayMs = settings.retryDelay != null ? settings.retryDelay : 2000
    def diagnostics = settings.enableDiagnostics != null ? settings.enableDiagnostics : false
    
    if (diagnostics) logDebug "turnOnDevicesWithRetry called for ${deviceType}, attempt ${retryCount + 1}"
    
    def deviceList = devices instanceof List ? devices : [devices]
    def failedDevices = []
    
    // Send on commands in batches to prevent mesh flooding
    deviceList.eachWithIndex { device, index ->
        if (diagnostics) logDebug "Turning on ${deviceType}: ${device.displayName}"
        device.on()
        // Small delay between each device to prevent mesh congestion
        if (index < deviceList.size() - 1 && deviceList.size() > 3) {
            pauseExecution(batchDelayMs)
        }
    }
    
    // Wait for commands to process
    pauseExecution(verificationWaitMs)
    
    // Verify all devices turned on
    deviceList.each { device ->
        def currentState = device.currentValue("switch")
        if (currentState != "on") {
            logInfo "WARNING: ${deviceType} ${device.displayName} failed to turn on (state: ${currentState})"
            failedDevices.add(device)
        } else {
            if (diagnostics) logDebug "${deviceType} ${device.displayName} confirmed ON"
        }
    }
    
    // Retry failed devices once
    if (failedDevices.size() > 0 && retryCount < 1) {
        logInfo "Retrying ${failedDevices.size()} failed ${deviceType} devices"
        pauseExecution(retryDelayMs)
        turnOnDevicesWithRetry(failedDevices, deviceType, retryCount + 1)
    } else if (failedDevices.size() > 0) {
        logInfo "ERROR: ${deviceType} devices still off after retry: ${failedDevices*.displayName.join(', ')}"
    }
}

// ========================================
// COLOR SWITCH HANDLERS
// ========================================

def morningColorSwitchHandler(evt) {
    logInfo "Morning color switch activated: ${evt.device.displayName} - setting strips to morning colors"
    
    // Set light strips with morning settings
    def lightStripColorInfo = resolveColor("lightStripMorningColor", "lightStripMorningCustomColor", "Soft White")
    Integer lightStripLevel = (settings.lightStripMorningLevel ?: 50) as Integer
    def lanStripColorInfo = resolveColor("lanStripMorningColor", "lanStripMorningCustomColor", "Yellow")
    Integer lanStripLevel = (settings.lanStripMorningLevel ?: 96) as Integer
    
    logInfo "Setting strips to morning colors - Light strip: ${lightStripColorInfo.name}@${lightStripLevel}%, LAN strip: ${lanStripColorInfo.name}@${lanStripLevel}%"
    
    setStripWithColor(lightStrip, lightStripColorInfo, lightStripLevel)
    setStripWithColor(lanStrip, lanStripColorInfo, lanStripLevel)
    
    // Reset the switches after a short delay
    runIn(2, resetMorningColorSwitches)
}

def eveningColorSwitchHandler(evt) {
    logInfo "Evening color switch activated: ${evt.device.displayName} - setting strips to evening colors"
    
    // Set light strips with evening settings
    def lightStripColorInfo = resolveColor("lightStripEveningColor", "lightStripEveningCustomColor", "Soft White")
    Integer lightStripLevel = (settings.lightStripEveningLevel ?: 50) as Integer
    def lanStripColorInfo = resolveColor("lanStripEveningColor", "lanStripEveningCustomColor", "Yellow")
    Integer lanStripLevel = (settings.lanStripEveningLevel ?: 96) as Integer
    
    logInfo "Setting strips to evening colors - Light strip: ${lightStripColorInfo.name}@${lightStripLevel}%, LAN strip: ${lanStripColorInfo.name}@${lanStripLevel}%"
    
    setStripWithColor(lightStrip, lightStripColorInfo, lightStripLevel)
    setStripWithColor(lanStrip, lanStripColorInfo, lanStripLevel)
    
    // Reset the switches after a short delay
    runIn(2, resetEveningColorSwitches)
}

def nightColorSwitchHandler(evt) {
    logInfo "Night color switch activated: ${evt.device.displayName} - setting strips to night colors"
    
    // Set light strips with night settings
    def lightStripColorInfo = resolveColor("lightStripNightColor", "lightStripNightCustomColor", "Blue")
    Integer lightStripLevel = (settings.lightStripNightLevel ?: 30) as Integer
    def lanStripColorInfo = resolveColor("lanStripNightColor", "lanStripNightCustomColor", "Blue")
    Integer lanStripLevel = (settings.lanStripNightLevel ?: 30) as Integer
    
    logInfo "Setting strips to night colors - Light strip: ${lightStripColorInfo.name}@${lightStripLevel}%, LAN strip: ${lanStripColorInfo.name}@${lanStripLevel}%"
    
    setStripWithColor(lightStrip, lightStripColorInfo, lightStripLevel)
    setStripWithColor(lanStrip, lanStripColorInfo, lanStripLevel)
    
    // Reset the switches after a short delay
    runIn(2, resetNightColorSwitches)
}

def resetMorningColorSwitches() {
    morningColorSwitches?.each { it.off() }
    logDebug "Reset morning color switches"
}

def resetEveningColorSwitches() {
    eveningColorSwitches?.each { it.off() }
    logDebug "Reset evening color switches"
}

def resetNightColorSwitches() {
    nightColorSwitches?.each { it.off() }
    logDebug "Reset night color switches"
}

// ========================================
// COLOR RESOLUTION
// ========================================

def resolveColor(String colorSettingName, String customColorSettingName, String defaultColor) {
    // Get the selected color option
    String selectedColor = settings[colorSettingName] ?: defaultColor
    
    if (selectedColor == "Custom") {
        // Use the custom color from color picker
        String customColorHex = settings[customColorSettingName]
        if (customColorHex) {
            def rgbMap = hexToRgb(customColorHex)
            def hsvMap = rgbToHsv(rgbMap.r, rgbMap.g, rgbMap.b)
            logDebug "Custom color: ${customColorHex} -> HSV(${hsvMap.hue}, ${hsvMap.saturation}, ${hsvMap.value})"
            return [
                name: "Custom (${customColorHex})",
                isCustom: true,
                hue: hsvMap.hue,
                saturation: hsvMap.saturation
            ]
        } else {
            logDebug "Custom color selected but no color picker value, using default: ${defaultColor}"
            return [name: defaultColor, isCustom: false]
        }
    } else {
        return [name: selectedColor, isCustom: false]
    }
}

def hexToRgb(String hex) {
    // Remove # if present
    hex = hex.replaceAll("#", "")
    
    def r = Integer.parseInt(hex.substring(0, 2), 16)
    def g = Integer.parseInt(hex.substring(2, 4), 16)
    def b = Integer.parseInt(hex.substring(4, 6), 16)
    
    return [r: r, g: g, b: b]
}

def rgbToHsv(int r, int g, int b) {
    def rf = r / 255.0
    def gf = g / 255.0
    def bf = b / 255.0
    
    def max = Math.max(rf, Math.max(gf, bf))
    def min = Math.min(rf, Math.min(gf, bf))
    def delta = max - min
    
    def h = 0
    def s = 0
    def v = max
    
    if (delta != 0) {
        s = delta / max
        
        if (rf == max) {
            h = ((gf - bf) / delta) % 6
        } else if (gf == max) {
            h = ((bf - rf) / delta) + 2
        } else {
            h = ((rf - gf) / delta) + 4
        }
        
        h = h * 60
        if (h < 0) h += 360
    }
    
    // Convert to Hubitat's 0-100 scale
    def hue = Math.round(h / 3.6) as Integer  // 0-100
    def saturation = Math.round(s * 100) as Integer  // 0-100
    
    return [hue: hue, saturation: saturation, value: v]
}

def setStripWithColor(device, colorInfo, Integer level) {
    if (!device) {
        logDebug "setStripWithColor called with null device"
        return
    }
    
    if (colorInfo.isCustom) {
        // Use custom HSV values
        setStripCustom(device, colorInfo.hue, colorInfo.saturation, level)
    } else {
        // Use preset color name
        setStrip(device, colorInfo.name, level)
    }
}

def setStripCustom(device, Integer hue, Integer saturation, Integer level) {
    if (!device) {
        logDebug "setStripCustom called with null device"
        return
    }
    
    // Validate inputs
    if (level == null) {
        logDebug "setStripCustom: level is null for ${device.displayName}, using default 50"
        level = 50
    }
    if (hue == null) hue = 0
    if (saturation == null) saturation = 100
    
    logDebug "setStripCustom: ${device.displayName} -> HSV(${hue}, ${saturation}) @ ${level}%"
    
    if (level == 0) {
        device.off()
        return
    }
    
    // Always ensure device is on and ready before setting color
    // Some devices need a fresh on() command to properly accept color changes
    // even when already on (especially during mode transitions)
    logDebug "Ensuring ${device.displayName} is on and ready"
    device.on()
    pauseExecution(1000) // Wait for device to be ready
    
    // Set custom color
    device.setColor([hue: hue, saturation: saturation, level: level])
    
    // Small delay to allow command to process
    pauseExecution(300)
    
    // Verify and log the final state
    logDebug "Strip ${device.displayName} final state - Switch: ${device.currentValue('switch')}, Level: ${device.currentValue('level')}"
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
