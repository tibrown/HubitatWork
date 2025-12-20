/**
 *  EnvironmentalControlManager
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
    name: "Environmental Control Manager",
    namespace: "tibrown",
    author: "Tim Brown",
    description: "Manages temperature control, fans, heaters, and environmental monitoring for greenhouse, office, and outdoor areas",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    singleThreaded: true
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Environmental Control Manager", install: true, uninstall: true) {
        section("<b>═══════════════════════════════════════</b>\n<b>GREENHOUSE CONTROLS</b>\n<b>═══════════════════════════════════════</b>") {
            input "greenhouseTempSensor", "capability.temperatureMeasurement",
                  title: "Greenhouse Temperature Sensor",
                  required: false
            
            input "greenhouseFan", "capability.switch",
                  title: "Greenhouse Fan",
                  required: false
            
            input "greenhouseHeater", "capability.switch",
                  title: "Greenhouse Heater",
                  required: false
            
            input "fanOnTemp", "decimal",
                  title: "Fan On Temperature (°F)",
                  description: "Turn fan on when temperature rises above this",
                  defaultValue: 75.0,
                  required: false
            
            input "fanOffTemp", "decimal",
                  title: "Fan Off Temperature (°F)",
                  description: "Turn fan off when temperature falls below this",
                  defaultValue: 70.0,
                  required: false
            
            input "heaterOnTemp", "decimal",
                  title: "Heater On Temperature (°F)",
                  description: "Turn heater on when temperature falls below this",
                  defaultValue: 40.0,
                  required: false
            
            input "heaterOffTemp", "decimal",
                  title: "Heater Off Temperature (°F)",
                  description: "Turn heater off when temperature rises above this",
                  defaultValue: 45.0,
                  required: false
            
            input "freezeAlertTemp", "decimal",
                  title: "Freeze Alert Temperature (°F)",
                  description: "Send alert when temperature falls below this",
                  defaultValue: 32.0,
                  required: false
            
            input "greenhouseAlexaToggle", "capability.switch",
                  title: "Alexa Toggle Switch for Greenhouse (Optional)",
                  description: "Virtual switch for Alexa voice control",
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>OFFICE CONTROLS</b>\n<b>═══════════════════════════════════════</b>") {
            input "officeTempSensor", "capability.temperatureMeasurement",
                  title: "Office Temperature Sensor",
                  required: false
            
            input "officeHeater", "capability.switch",
                  title: "Office Heater",
                  required: false
            
            input "officeFans", "capability.switch",
                  title: "Office Fans",
                  multiple: true,
                  required: false
            
            input "officeHeaterTemp", "decimal",
                  title: "Office Heater Temperature (°F)",
                  description: "Target temperature for office heater",
                  defaultValue: 68.0,
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>MOSQUITO CONTROL</b>\n<b>═══════════════════════════════════════</b>") {
            input "skeeterKiller", "capability.switch",
                  title: "Mosquito Killer Devices",
                  multiple: true,
                  required: false
            
            input "skeeterOnModes", "mode",
                  title: "Modes to Turn Skeeter ON",
                  description: "Skeeter will be on during these modes",
                  multiple: true,
                  required: false
            
            input "skeeterOffModes", "mode",
                  title: "Modes to Turn Skeeter OFF",
                  description: "Skeeter will be off during these modes",
                  multiple: true,
                  required: false
            
            input "skeeterIlluminanceSensor", "capability.illuminanceMeasurement",
                  title: "Illuminance Sensor (Optional)",
                  description: "Sensor for Day mode illuminance-based control",
                  required: false
            
            input "skeeterIlluminanceThreshold", "number",
                  title: "Illuminance Threshold (lux)",
                  description: "Turn skeeter ON when illuminance drops below this (Day mode only)",
                  defaultValue: 500,
                  required: false
            
            input "skeeterCheckInterval", "number",
                  title: "Illuminance Check Interval (minutes)",
                  description: "How often to check illuminance during Day mode",
                  defaultValue: 5,
                  range: "1..60",
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>WATER CONTROL</b>\n<b>═══════════════════════════════════════</b>") {
            input "waterValve", "capability.switch",
                  title: "Water Control Valve",
                  required: false
            paragraph ""
            input "waterTimeoutMinutes", "number",
                  title: "Water Auto-Off Timeout (minutes)",
                  description: "Automatically turn off water after this duration",
                  defaultValue: 30,
                  range: "1..180",
                  required: false
            
            input "waterResetSwitch", "capability.switch",
                  title: "Water Reset Switch (Optional)",
                  description: "Switch to reset water timeout",
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>NOTIFICATIONS</b>\n<b>═══════════════════════════════════════</b>") {
            input "notificationDevices", "capability.notification",
                  title: "Notification Devices",
                  description: "Devices to receive environmental alerts",
                  multiple: true,
                  required: false
            paragraph ""
            input "alexaDevices", "capability.speechSynthesis",
                  title: "Alexa Devices for Announcements",
                  multiple: true,
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>HUB VARIABLE OVERRIDES</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "This app supports the following hub variables for dynamic configuration:"
            paragraph "• <b>GreenhouseFanOnTemp</b> - Override fan on temperature (°F)\n" +
                     "• <b>GreenhouseFanOffTemp</b> - Override fan off temperature (°F)\n" +
                     "• <b>GreenhouseHeaterOnTemp</b> - Override heater on temperature (°F)\n" +
                     "• <b>GreenhouseHeaterOffTemp</b> - Override heater off temperature (°F)\n" +
                     "• <b>FreezeAlertThreshold</b> - Override freeze warning temperature (°F)\n" +
                     "• <b>OfficeHeaterTemp</b> - Override office heater temperature (°F)\n" +
                     "• <b>WaterTimeout</b> - Override water shutoff timeout (minutes)"
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>LOGGING</b>\n<b>═══════════════════════════════════════</b>") {
            input "logEnable", "bool",
                  title: "Enable Debug Logging",
                  defaultValue: false,
                  required: false
        }
    }
}

def installed() {
    logInfo "Environmental Control Manager installed"
    initialize()
}

def updated() {
    logInfo "Environmental Control Manager updated"
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    logInfo "Initializing Environmental Control Manager"
    
    // Subscribe to temperature sensors
    if (settings.greenhouseTempSensor) {
        subscribe(settings.greenhouseTempSensor, "temperature", greenhouseTempHandler)
    }
    if (settings.officeTempSensor) {
        subscribe(settings.officeTempSensor, "temperature", officeTempHandler)
    }
    
    // Subscribe to Alexa toggle for greenhouse
    if (settings.greenhouseAlexaToggle) {
        subscribe(settings.greenhouseAlexaToggle, "switch", greenhouseAlexaHandler)
    }
    
    // Subscribe to water valve to track manual operations
    if (settings.waterValve) {
        subscribe(settings.waterValve, "switch.on", waterOnHandler)
    }
    
    // Subscribe to water reset switch
    if (settings.waterResetSwitch) {
        subscribe(settings.waterResetSwitch, "switch.on", waterResetHandler)
    }
    
    // Subscribe to illuminance sensor for Day mode skeeter control
    if (settings.skeeterIlluminanceSensor) {
        subscribe(settings.skeeterIlluminanceSensor, "illuminance", illuminanceHandler)
    }
    
    // Subscribe to mode changes for mosquito control
    subscribe(location, "mode", modeChangeHandler)
    
    // Initial temperature check and mode-based skeeter control
    checkAllTemperatures()
    handleCurrentMode()
    
    // Initial illuminance check if in Day mode
    if (location.currentMode == "Day" && settings.skeeterIlluminanceSensor) {
        checkIlluminance()
    }
    
    // Schedule periodic illuminance check during Day mode
    if (location.currentMode == "Day" && settings.skeeterIlluminanceSensor) {
        scheduleIlluminanceCheck()
    }
}

// ============================================================================
// EVENT HANDLERS
// ============================================================================

def greenhouseTempHandler(evt) {
    def temp = evt.value as BigDecimal
    logDebug "Greenhouse temperature: ${temp}°F"
    
    controlGreenhouseFan(temp)
    controlGreenhouseHeater(temp)
    checkFreezeRisk(temp)
}

def officeTempHandler(evt) {
    def temp = evt.value as BigDecimal
    logDebug "Office temperature: ${temp}°F"
    
    controlOfficeHeater(temp)
}

def greenhouseAlexaHandler(evt) {
    logInfo "Greenhouse Alexa toggle: ${evt.value}"
    
    if (evt.value == "on") {
        // Alexa command to turn on greenhouse controls
        def currentTemp = settings.greenhouseTempSensor?.currentValue("temperature") as BigDecimal
        if (currentTemp) {
            controlGreenhouseFan(currentTemp)
            controlGreenhouseHeater(currentTemp)
        }
    } else {
        // Alexa command to turn off greenhouse controls
        settings.greenhouseFan?.off()
        settings.greenhouseHeater?.off()
        logInfo "Greenhouse controls turned off by Alexa"
    }
}

def waterOnHandler(evt) {
    logInfo "Water valve turned on"
    
    def timeout = getConfigValue("waterTimeoutMinutes", "WaterTimeout") as Integer
    logInfo "Scheduling water auto-off in ${timeout} minutes"
    
    runIn(timeout * 60, waterAutoOff)
}

def waterResetHandler(evt) {
    logInfo "Water timeout reset"
    
    // Cancel auto-off
    unschedule(waterAutoOff)
    
    // Restart timeout if valve is on
    if (settings.waterValve?.currentValue("switch") == "on") {
        def timeout = getConfigValue("waterTimeoutMinutes", "WaterTimeout") as Integer
        logInfo "Restarting water auto-off timer: ${timeout} minutes"
        runIn(timeout * 60, waterAutoOff)
    }
    
    // Turn off reset switch
    runIn(2, resetWaterResetSwitch)
}

def modeChangeHandler(evt) {
    logInfo "Mode changed to: ${evt.value}"
    
    // Cancel periodic checks when leaving Day mode
    if (evt.value != "Day") {
        unschedule(periodicIlluminanceCheck)
    }
    // Start periodic checks when entering Day mode with illuminance sensor
    else if (evt.value == "Day" && settings.skeeterIlluminanceSensor) {
        scheduleIlluminanceCheck()
    }
    
    handleSkeeterMode(evt.value)
}

// ============================================================================
// GREENHOUSE CONTROL
// ============================================================================

def controlGreenhouseFan(BigDecimal temp) {
    def fanOn = getConfigValue("fanOnTemp", "GreenhouseFanOnTemp") as BigDecimal
    def fanOff = getConfigValue("fanOffTemp", "GreenhouseFanOffTemp") as BigDecimal
    
    if (!settings.greenhouseFan) return
    
    def currentState = settings.greenhouseFan.currentValue("switch")
    
    if (temp >= fanOn && currentState != "on") {
        logInfo "Greenhouse temperature ${temp}°F >= ${fanOn}°F, turning fan ON"
        settings.greenhouseFan.on()
        announceAlexa("Greenhouse fan turned on, temperature is ${temp} degrees")
    } else if (temp <= fanOff && currentState == "on") {
        logInfo "Greenhouse temperature ${temp}°F <= ${fanOff}°F, turning fan OFF"
        settings.greenhouseFan.off()
        announceAlexa("Greenhouse fan turned off, temperature is ${temp} degrees")
    }
}

def controlGreenhouseHeater(BigDecimal temp) {
    def heaterOn = getConfigValue("heaterOnTemp", "GreenhouseHeaterOnTemp") as BigDecimal
    def heaterOff = getConfigValue("heaterOffTemp", "GreenhouseHeaterOffTemp") as BigDecimal
    
    if (!settings.greenhouseHeater) return
    
    def currentState = settings.greenhouseHeater.currentValue("switch")
    
    if (temp <= heaterOn && currentState != "on") {
        logInfo "Greenhouse temperature ${temp}°F <= ${heaterOn}°F, turning heater ON"
        settings.greenhouseHeater.on()
        announceAlexa("Greenhouse heater turned on, temperature is ${temp} degrees")
        sendAlert("Greenhouse heater activated (${temp}°F)")
    } else if (temp >= heaterOff && currentState == "on") {
        logInfo "Greenhouse temperature ${temp}°F >= ${heaterOff}°F, turning heater OFF"
        settings.greenhouseHeater.off()
        announceAlexa("Greenhouse heater turned off, temperature is ${temp} degrees")
    }
}

def checkFreezeRisk(BigDecimal temp) {
    def freezeTemp = getConfigValue("freezeAlertTemp", "FreezeAlertThreshold") as BigDecimal
    
    if (temp <= freezeTemp) {
        logWarn "FREEZE ALERT: Greenhouse temperature ${temp}°F at or below ${freezeTemp}°F"
        sendAlert("FREEZE ALERT: Greenhouse at ${temp}°F!")
        announceAlexa("Freeze alert! Greenhouse temperature is ${temp} degrees!")
    }
}

// ============================================================================
// OFFICE CONTROL
// ============================================================================

def controlOfficeHeater(BigDecimal temp) {
    def targetTemp = getConfigValue("officeHeaterTemp", "OfficeHeaterTemp") as BigDecimal
    
    if (!settings.officeHeater) return
    
    def currentState = settings.officeHeater.currentValue("switch")
    def hysteresis = 2.0 // Prevent rapid cycling
    
    if (temp < (targetTemp - hysteresis) && currentState != "on") {
        logInfo "Office temperature ${temp}°F < ${targetTemp - hysteresis}°F, turning heater ON"
        settings.officeHeater.on()
    } else if (temp > (targetTemp + hysteresis) && currentState == "on") {
        logInfo "Office temperature ${temp}°F > ${targetTemp + hysteresis}°F, turning heater OFF"
        settings.officeHeater.off()
    }
}

def turnOfficeFansOff() {
    logInfo "Turning office fans OFF"
    settings.officeFans?.each { fan ->
        fan.off()
    }
}

// ============================================================================
// MOSQUITO CONTROL
// ============================================================================

def handleCurrentMode() {
    if (!settings.skeeterKiller) return
    
    def currentMode = location.currentMode.toString()
    handleSkeeterMode(currentMode)
}

def handleSkeeterMode(String mode) {
    if (!settings.skeeterKiller) {
        logDebug "Mosquito killer not configured"
        return
    }
    
    // For Day mode with illuminance sensor, ALWAYS use illuminance-based control
    // This takes priority over mode-based rules
    if (mode == "Day" && settings.skeeterIlluminanceSensor) {
        logDebug "Day mode: using illuminance-based skeeter control (overrides mode rules)"
        checkIlluminance()
        return
    }
    
    // For other modes (or Day without illuminance sensor), use mode-based control
    if (settings.skeeterOnModes?.contains(mode)) {
        logInfo "Mode ${mode} triggers mosquito killer ON"
        settings.skeeterKiller?.each { device ->
            if (device.currentValue("switch") != "on") {
                device.on()
            }
        }
    } else if (settings.skeeterOffModes?.contains(mode)) {
        logInfo "Mode ${mode} triggers mosquito killer OFF"
        settings.skeeterKiller?.each { device ->
            if (device.currentValue("switch") != "off") {
                device.off()
            }
        }
    } else {
        logDebug "Mode ${mode} has no mosquito killer action"
    }
}

def illuminanceHandler(evt) {
    if (location.currentMode != "Day") {
        logDebug "Illuminance change ignored - not in Day mode"
        return
    }
    
    logDebug "Illuminance changed to ${evt.value} lux"
    checkIlluminance()
}

def periodicIlluminanceCheck() {
    if (location.currentMode == "Day" && settings.skeeterIlluminanceSensor) {
        logDebug "Periodic illuminance check triggered"
        checkIlluminance()
    }
}

def scheduleIlluminanceCheck() {
    def interval = settings.skeeterCheckInterval ?: 5
    logInfo "Scheduling illuminance check every ${interval} minute(s)"
    
    switch(interval) {
        case 1:
            runEvery1Minute(periodicIlluminanceCheck)
            break
        case 5:
            runEvery5Minutes(periodicIlluminanceCheck)
            break
        case 10:
            runEvery10Minutes(periodicIlluminanceCheck)
            break
        case 15:
            runEvery15Minutes(periodicIlluminanceCheck)
            break
        case 30:
            runEvery30Minutes(periodicIlluminanceCheck)
            break
        case 60:
            runEvery1Hour(periodicIlluminanceCheck)
            break
        default:
            // For custom intervals, use runIn repeatedly
            schedule("0 */${interval} * ? * *", periodicIlluminanceCheck)
            break
    }
}

def checkIlluminance() {
    if (!settings.skeeterKiller || !settings.skeeterIlluminanceSensor) {
        return
    }
    
    if (location.currentMode != "Day") {
        logDebug "Not in Day mode, skipping illuminance check"
        return
    }
    
    def illuminance = settings.skeeterIlluminanceSensor.currentValue("illuminance")
    def threshold = getConfigValue("skeeterIlluminanceThreshold", "SkeeterIlluminanceThreshold") ?: 500
    
    logDebug "Illuminance check: ${illuminance} lux vs threshold ${threshold} lux"
    
    if (illuminance < threshold) {
        logInfo "Illuminance ${illuminance} lux < ${threshold} lux, turning skeeter ON"
        settings.skeeterKiller?.each { device ->
            if (device.currentValue("switch") != "on") {
                device.on()
            }
        }
    } else if (illuminance >= threshold) {
        logInfo "Illuminance ${illuminance} lux >= ${threshold} lux, turning skeeter OFF"
        settings.skeeterKiller?.each { device ->
            if (device.currentValue("switch") != "off") {
                device.off()
            }
        }
    }
}

// ============================================================================
// WATER CONTROL
// ============================================================================

def waterAutoOff() {
    if (settings.waterValve?.currentValue("switch") == "on") {
        logInfo "Water timeout reached, turning off valve"
        settings.waterValve.off()
        sendAlert("Water valve auto-shutoff activated")
    }
}

def resetWaterResetSwitch() {
    settings.waterResetSwitch?.off()
}

// ============================================================================
// UTILITY METHODS
// ============================================================================

def checkAllTemperatures() {
    logDebug "Checking all temperatures"
    
    if (settings.greenhouseTempSensor) {
        def temp = settings.greenhouseTempSensor.currentValue("temperature") as BigDecimal
        if (temp) {
            controlGreenhouseFan(temp)
            controlGreenhouseHeater(temp)
            checkFreezeRisk(temp)
        }
    }
    
    if (settings.officeTempSensor) {
        def temp = settings.officeTempSensor.currentValue("temperature") as BigDecimal
        if (temp) {
            controlOfficeHeater(temp)
        }
    }
}

// ============================================================================
// NOTIFICATION METHODS
// ============================================================================

def sendAlert(String message) {
    logInfo "Sending alert: ${message}"
    
    settings.notificationDevices?.each { device ->
        device.deviceNotification(message)
    }
}

def announceAlexa(String message) {
    if (!settings.alexaDevices) return
    
    logDebug "Alexa announcement: ${message}"
    
    settings.alexaDevices?.each { alexa ->
        alexa.speak(message)
    }
}

// ============================================================================
// HELPER METHODS
// ============================================================================

/**
 * Get configuration value from hub variable or fall back to app setting
 */
def getConfigValue(String settingName, String hubVarName) {
    try {
        def hubVar = getGlobalVar(hubVarName)
        if (hubVar?.value != null) {
            logDebug "Using hub variable ${hubVarName}: ${hubVar.value}"
            return convertValue(hubVar.value, hubVar.type)
        }
    } catch (Exception e) {
        logDebug "Hub variable '${hubVarName}' not found: ${e.message}"
    }
    
    // Fall back to app setting
    def settingValue = settings[settingName]
    logDebug "Using app setting ${settingName}: ${settingValue}"
    return settingValue
}

/**
 * Convert hub variable value to appropriate type
 */
def convertValue(value, type) {
    switch(type?.toLowerCase()) {
        case "number":
            return value as Integer
        case "decimal":
            return value as BigDecimal
        case "boolean":
            return value.toString().toBoolean()
        case "string":
        default:
            return value.toString()
    }
}

// ============================================================================
// LOGGING
// ============================================================================

def logInfo(String msg) {
    log.info "[Environmental Control Manager] ${msg}"
}

def logDebug(String msg) {
    if (settings.logEnable) {
        log.debug "[Environmental Control Manager] ${msg}"
    }
}

def logWarn(String msg) {
    log.warn "[Environmental Control Manager] ${msg}"
}

def logError(String msg) {
    log.error "[Environmental Control Manager] ${msg}"
}
