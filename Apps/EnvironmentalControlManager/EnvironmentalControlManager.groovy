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
            input "greenhouseEnabled", "bool",
                  title: "Enable Greenhouse Controls",
                  description: "Enable or disable all greenhouse automation",
                  defaultValue: false,
                  required: false
            
            input "greenhouseTempSensors", "capability.temperatureMeasurement",
                  title: "Greenhouse Temperature Sensors",
                  description: "Select one or more sensors (most recent reading will be used)",
                  multiple: true,
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
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>OFFICE CONTROLS</b>\n<b>═══════════════════════════════════════</b>") {
            input "officeEnabled", "bool",
                  title: "Enable Office Controls",
                  description: "Enable or disable all office automation",
                  defaultValue: false,
                  required: false
            
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
            
            input "skeeterTempSensors", "capability.temperatureMeasurement",
                  title: "Temperature Sensors for Skeeter Control",
                  description: "Select one or more outdoor temperature sensors (lowest reading used when all active)",
                  multiple: true,
                  required: false
            
            input "skeeterTempThreshold", "decimal",
                  title: "Minimum Temperature for Skeeter (°F)",
                  description: "Do not run skeeter when temperature is at or below this value",
                  defaultValue: 50.0,
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
                  description: "Sensor for illuminance-based control",
                  required: false
            
            input "skeeterIlluminanceModes", "mode",
                  title: "Modes Using Illuminance Control",
                  description: "Use illuminance to control skeeter in these modes (overrides On/Off mode rules above)",
                  multiple: true,
                  required: false
            
            input "skeeterIlluminanceThreshold", "number",
                  title: "Illuminance Threshold (lux)",
                  description: "Turn skeeter ON when illuminance drops below this threshold",
                  defaultValue: 200,
                  required: false
            
            input "skeeterCheckInterval", "number",
                  title: "Illuminance Check Interval (minutes)",
                  description: "How often to check illuminance in selected modes",
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
        
        section("<b>═══════════════════════════════════════</b>\n<b>NWS WEATHER BACKUP</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Optionally configure a NWS Weather device to provide backup temperature readings. " +
                     "When configured, the app will use the <b>lower</b> of your local sensor or NWS temperature " +
                     "for freeze-related decisions. This helps compensate for sensors that read high due to solar heating."
            
            input "nwsTempDevice", "capability.temperatureMeasurement",
                  title: "NWS Weather Device (Optional)",
                  description: "Select the NWS Weather Driver device for backup temperature",
                  required: false
            
            input "nwsStaleMinutes", "number",
                  title: "NWS Data Staleness Threshold (minutes)",
                  description: "Ignore NWS data older than this (default: 45 minutes)",
                  defaultValue: 45,
                  range: "15..120",
                  required: false
            
            input "localPreferenceMinutes", "number",
                  title: "Local Sensor Preference (minutes)",
                  description: "Prefer local sensor readings within this age over NWS (default: 5 minutes)",
                  defaultValue: 5,
                  range: "1..30",
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
    
    // Subscribe to greenhouse temperature sensors (if enabled)
    if (settings.greenhouseEnabled && settings.greenhouseTempSensors) {
        settings.greenhouseTempSensors.each { sensor ->
            subscribe(sensor, "temperature", greenhouseTempHandler)
        }
    }
    
    // Subscribe to skeeter temperature sensors (independent of greenhouse)
    if (settings.skeeterTempSensors && settings.skeeterKiller) {
        settings.skeeterTempSensors.each { sensor ->
            subscribe(sensor, "temperature", skeeterTempHandler)
        }
        def sensorNames = settings.skeeterTempSensors.collect { it.displayName }.join(", ")
        logInfo "Subscribed to skeeter temperature sensor(s): ${sensorNames}"
    }
    
    // Subscribe to office temperature sensor (if enabled)
    if (settings.officeEnabled && settings.officeTempSensor) {
        subscribe(settings.officeTempSensor, "temperature", officeTempHandler)
    }
    
    // Subscribe to water valve to track manual operations
    if (settings.waterValve) {
        subscribe(settings.waterValve, "switch.on", waterOnHandler)
    }
    
    // Subscribe to water reset switch
    if (settings.waterResetSwitch) {
        subscribe(settings.waterResetSwitch, "switch.on", waterResetHandler)
    }
    
    // Subscribe to illuminance sensor for skeeter control in configured modes
    if (settings.skeeterIlluminanceSensor && settings.skeeterIlluminanceModes) {
        subscribe(settings.skeeterIlluminanceSensor, "illuminance", illuminanceHandler)
        logInfo "Subscribed to illuminance sensor for modes: ${settings.skeeterIlluminanceModes}"
    }
    
    // Subscribe to mode changes for mosquito control
    subscribe(location, "mode", modeChangeHandler)
    
    // Initial temperature check and mode-based skeeter control
    checkAllTemperatures()
    handleCurrentMode()
    
    // Initial illuminance check if in a configured illuminance mode
    def currentMode = location.currentMode.toString()
    if (currentMode in (settings.skeeterIlluminanceModes ?: []) && settings.skeeterIlluminanceSensor) {
        checkIlluminance()
        scheduleIlluminanceCheck()
        logInfo "Starting illuminance control in ${currentMode} mode"
    }
}

// ============================================================================
// EVENT HANDLERS
// ============================================================================

def greenhouseTempHandler(evt) {
    if (!settings.greenhouseEnabled) return
    
    // Get the most recent reading from all configured sensors
    def reading = getMostRecentGreenhouseReading()
    if (!reading) return
    
    def temp = reading.temp
    logDebug "Greenhouse temperature: ${temp}°F from ${reading.sensorName} (${reading.ageMinutes.toInteger()} min old)"
    
    controlGreenhouseFan(temp)
    controlGreenhouseHeater(temp)
    checkFreezeRisk(temp)
}

def officeTempHandler(evt) {
    if (!settings.officeEnabled) return
    
    def temp = evt.value as BigDecimal
    logDebug "Office temperature: ${temp}°F"
    
    controlOfficeHeater(temp)
}

def skeeterTempHandler(evt) {
    // Use effective temperature (considers all sensors + NWS backup)
    def temp = getSkeeterEffectiveTemperature()
    if (temp == null) {
        logDebug "Skeeter temp handler: Unable to get effective temperature"
        return
    }
    
    def threshold = settings.skeeterTempThreshold ?: 50.0
    
    logDebug "Skeeter temperature check: ${temp}°F (threshold: ${threshold}°F)"
    
    // If temp drops to or below threshold, turn off skeeter
    if (temp <= threshold) {
        def anyOn = settings.skeeterKiller?.any { it.currentValue("switch") == "on" }
        if (anyOn) {
            logInfo "Temperature ${temp}°F <= ${threshold}°F, turning skeeter killers OFF"
            settings.skeeterKiller?.each { device ->
                if (device.currentValue("switch") == "on") {
                    device.off()
                }
            }
        }
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
    
    // Cancel periodic checks when leaving an illuminance-controlled mode
    def oldMode = evt.oldValue ?: ""
    def newMode = evt.value
    
    if (oldMode in (settings.skeeterIlluminanceModes ?: []) && !(newMode in (settings.skeeterIlluminanceModes ?: []))) {
        unschedule(periodicIlluminanceCheck)
        logDebug "Stopped illuminance checks (left ${oldMode} mode)"
    }
    
    // Start periodic checks when entering an illuminance-controlled mode
    if (newMode in (settings.skeeterIlluminanceModes ?: []) && settings.skeeterIlluminanceSensor) {
        scheduleIlluminanceCheck()
        logDebug "Started illuminance checks (entered ${newMode} mode)"
    }
    
    handleSkeeterMode(newMode)
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

def controlGreenhouseHeater(BigDecimal localTemp) {
    def heaterOn = getConfigValue("heaterOnTemp", "GreenhouseHeaterOnTemp") as BigDecimal
    def heaterOff = getConfigValue("heaterOffTemp", "GreenhouseHeaterOffTemp") as BigDecimal
    
    if (!settings.greenhouseHeater) return
    
    // Use effective temperature (compares local vs NWS based on freshness) for heater control
    def temp = getEffectiveTemperature()
    if (temp == null) {
        temp = localTemp
    }
    
    def currentState = settings.greenhouseHeater.currentValue("switch")
    
    if (temp <= heaterOn && currentState != "on") {
        logInfo "Greenhouse effective temp ${temp}°F (local: ${localTemp}°F) <= ${heaterOn}°F, turning heater ON"
        settings.greenhouseHeater.on()
        announceAlexa("Greenhouse heater turned on, temperature is ${temp.round(0)} degrees")
        sendAlert("Greenhouse heater activated (${temp}°F)")
    } else if (temp >= heaterOff && currentState == "on") {
        logInfo "Greenhouse effective temp ${temp}°F (local: ${localTemp}°F) >= ${heaterOff}°F, turning heater OFF"
        settings.greenhouseHeater.off()
        announceAlexa("Greenhouse heater turned off, temperature is ${temp.round(0)} degrees")
    }
}

def checkFreezeRisk(BigDecimal localTemp) {
    def freezeTemp = getConfigValue("freezeAlertTemp", "FreezeAlertThreshold") as BigDecimal
    
    // Use effective temperature (compares local vs NWS based on freshness) for freeze risk
    def temp = getEffectiveTemperature()
    if (temp == null) {
        temp = localTemp
    }
    
    if (temp <= freezeTemp) {
        logWarn "FREEZE ALERT: Greenhouse effective temp ${temp}°F (local: ${localTemp}°F) at or below ${freezeTemp}°F"
        sendAlert("FREEZE ALERT: Greenhouse at ${temp}°F!")
        announceAlexa("Freeze alert! Greenhouse temperature is ${temp.round(0)} degrees!")
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
    
    // Check if this mode uses illuminance-based control
    // If so, illuminance control takes priority over mode-based rules
    if (mode in (settings.skeeterIlluminanceModes ?: []) && settings.skeeterIlluminanceSensor) {
        logDebug "${mode} mode: using illuminance-based skeeter control (overrides mode On/Off rules)"
        checkIlluminance()
        return
    }
    
    // For modes NOT using illuminance control, use mode-based On/Off rules
    if (settings.skeeterOnModes?.contains(mode)) {
        // Check temperature before turning on
        if (!isSkeeterTempOk()) {
            logInfo "Mode ${mode} would trigger mosquito killer ON, but temperature is too low"
            return
        }
        
        logInfo "Mode ${mode} triggers mosquito killer ON (mode-based rule)"
        settings.skeeterKiller?.each { device ->
            if (device.currentValue("switch") != "on") {
                logDebug "Turning ON ${device.displayName}"
                device.on()
            }
        }
    } else if (settings.skeeterOffModes?.contains(mode)) {
        logInfo "Mode ${mode} triggers mosquito killer OFF (mode-based rule)"
        settings.skeeterKiller?.each { device ->
            if (device.currentValue("switch") != "off") {
                logDebug "Turning OFF ${device.displayName}"
                device.off()
            }
        }
    } else {
        logDebug "Mode ${mode} has no mosquito killer action"
    }
}

def illuminanceHandler(evt) {
    def currentMode = location.currentMode.toString()
    
    // Only process illuminance changes if in a configured illuminance mode
    if (!(currentMode in (settings.skeeterIlluminanceModes ?: []))) {
        logDebug "Illuminance change ignored - ${currentMode} not in illuminance-controlled modes"
        return
    }
    
    logDebug "Illuminance changed to ${evt.value} lux (in ${currentMode} mode)"
    checkIlluminance()
}

def periodicIlluminanceCheck() {
    def currentMode = location.currentMode.toString()
    if (currentMode in (settings.skeeterIlluminanceModes ?: []) && settings.skeeterIlluminanceSensor) {
        logDebug "Periodic illuminance check triggered in ${currentMode} mode"
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
    
    def currentMode = location.currentMode.toString()
    if (!(currentMode in (settings.skeeterIlluminanceModes ?: []))) {
        logDebug "Not in an illuminance-controlled mode, skipping illuminance check"
        return
    }
    
    def illuminance = settings.skeeterIlluminanceSensor.currentValue("illuminance")
    def threshold = getConfigValue("skeeterIlluminanceThreshold", "SkeeterIlluminanceThreshold") ?: 200
    
    logInfo "Illuminance check in ${currentMode} mode: ${illuminance} lux vs threshold ${threshold} lux"
    
    if (illuminance < threshold) {
        // Check temperature before turning on
        if (!isSkeeterTempOk()) {
            logInfo "Illuminance ${illuminance} lux < ${threshold} lux would trigger skeeter ON, but temperature is too low"
            return
        }
        
        logInfo "Illuminance ${illuminance} lux < ${threshold} lux, turning skeeter ON"
        settings.skeeterKiller?.each { device ->
            if (device.currentValue("switch") != "on") {
                logDebug "Turning ON ${device.displayName}"
                device.on()
            }
        }
    } else {
        logInfo "Illuminance ${illuminance} lux >= ${threshold} lux, turning skeeter OFF"
        settings.skeeterKiller?.each { device ->
            if (device.currentValue("switch") != "off") {
                logDebug "Turning OFF ${device.displayName}"
                device.off()
            }
        }
    }
}

// ============================================================================
// SKEETER TEMPERATURE METHODS
// ============================================================================

/**
 * Get the most recent attribute update time from a sensor, checking
 * temperature, illuminance, and humidity attributes to determine if sensor is active.
 * @param sensor The sensor device to check
 * @return The most recent State object across all checked attributes, or null if none found
 */
def getMostRecentSensorActivity(sensor) {
    def attributesToCheck = ["temperature", "illuminance", "humidity"]
    def mostRecentState = null
    def mostRecentTime = 0
    
    attributesToCheck.each { attrName ->
        if (sensor.hasAttribute(attrName)) {
            def attrState = sensor.currentState(attrName)
            if (attrState && attrState.date.time > mostRecentTime) {
                mostRecentTime = attrState.date.time
                mostRecentState = attrState
            }
        }
    }
    
    return mostRecentState
}

/**
 * Get the best temperature reading from configured skeeter temperature sensors.
 * If all sensors are "active" (any attribute updated within the preference window),
 * returns the LOWEST temperature for cold-weather safety.
 * Otherwise, falls back to the most recent temperature reading.
 * @return Map with keys: temp, ageMinutes, sensorName; or null if no readings
 */
def getMostRecentSkeeterReading() {
    if (!settings.skeeterTempSensors) {
        return null
    }
    
    def nowMs = now()
    def preferenceWindowMs = (settings.localPreferenceMinutes ?: 5) * 60000
    def validReadings = []
    def allWithinWindow = true
    
    // First pass: collect all valid readings and check if all sensors are active
    settings.skeeterTempSensors.each { sensor ->
        def tempState = sensor.currentState("temperature")
        if (tempState) {
            // Get most recent activity from ANY attribute to determine if sensor is active
            def mostRecentActivity = getMostRecentSensorActivity(sensor)
            def activityAgeMs = mostRecentActivity ? (nowMs - mostRecentActivity.date.time) : Long.MAX_VALUE
            def activityAttr = mostRecentActivity?.name ?: "unknown"
            
            // Use temperature state for temp value and age
            def tempAgeMs = nowMs - tempState.date.time
            def tempAgeMinutes = tempAgeMs / 60000
            def temp = tempState.value as BigDecimal
            
            // Sensor is "active" if ANY attribute was updated within the window
            def isActive = activityAgeMs <= preferenceWindowMs
            
            validReadings << [
                temp: temp,
                ageMinutes: tempAgeMinutes,
                ageMs: tempAgeMs,
                activityAgeMs: activityAgeMs,
                activityAttr: activityAttr,
                sensorName: sensor.displayName,
                withinWindow: isActive
            ]
            
            if (!isActive) {
                allWithinWindow = false
                logDebug "Skeeter ${sensor.displayName}: temp ${tempAgeMinutes.toInteger()} min old, most recent activity (${activityAttr}) ${(activityAgeMs/60000).toInteger()} min ago - outside window"
            } else {
                logDebug "Skeeter ${sensor.displayName}: active (last ${activityAttr} update ${(activityAgeMs/60000).toInteger()} min ago), temp: ${temp}°F"
            }
        }
    }
    
    if (validReadings.isEmpty()) {
        return null
    }
    
    def bestReading = null
    
    if (allWithinWindow && validReadings.size() > 0) {
        // All sensors active - use LOWEST temperature for cold-weather safety
        def sortedByTemp = validReadings.sort { it.temp }
        bestReading = sortedByTemp.first()
        
        // Log all sensor temps for debugging
        def allTemps = validReadings.collect { "${it.sensorName}: ${it.temp}°F" }.join(", ")
        logDebug "Skeeter: All sensors active - selecting lowest temp. Sensors: [${allTemps}]"
    } else {
        // Not all sensors active - fall back to most recent temperature reading
        def sortedByAge = validReadings.sort { it.ageMs }
        bestReading = sortedByAge.first()
        logDebug "Skeeter: Not all sensors active - using most recent reading from ${bestReading.sensorName}"
    }
    
    return [
        temp: bestReading.temp,
        ageMinutes: bestReading.ageMinutes,
        sensorName: bestReading.sensorName
    ]
}

/**
 * Get the effective temperature for skeeter threshold decisions.
 * Uses dedicated skeeter temp sensors with NWS backup.
 * @return The effective temperature to use for decisions, or null if unavailable
 */
def getSkeeterEffectiveTemperature() {
    // Get most recent reading from skeeter temp sensors
    def bestLocal = getMostRecentSkeeterReading()
    if (bestLocal == null) {
        logDebug "No skeeter temperature sensors configured or no readings available"
        return null
    }
    
    def localTemp = bestLocal.temp
    def localAgeMinutes = bestLocal.ageMinutes
    def localSensorName = bestLocal.sensorName
    
    // If no NWS device configured, just use local
    if (!settings.nwsTempDevice) {
        logDebug "Skeeter: Using local temperature ${localTemp}°F from ${localSensorName} (${localAgeMinutes.toInteger()} min old)"
        return localTemp
    }
    
    try {
        // Prefer local sensor if reading is within configured preference time
        def localPrefMinutes = settings.localPreferenceMinutes ?: 5
        if (localAgeMinutes <= localPrefMinutes) {
            logDebug "Skeeter: Using local temperature ${localTemp}°F from ${localSensorName} (${localAgeMinutes.toInteger()} min old, within ${localPrefMinutes} min preference)"
            return localTemp
        }
        
        // Check if NWS data is stale
        if (isNwsDataStale()) {
            logDebug "Skeeter: NWS data is stale - using local sensor ${localSensorName} (${localAgeMinutes.toInteger()} min old)"
            return localTemp
        }
        
        // Get NWS temperature
        def nwsTemp = settings.nwsTempDevice.currentValue("temperature")
        if (nwsTemp == null) {
            logDebug "Skeeter: NWS temperature unavailable - using local sensor only"
            return localTemp
        }
        
        nwsTemp = nwsTemp as BigDecimal
        logDebug "Skeeter: Local reading older than preference (${localAgeMinutes.toInteger()} min) - using NWS temperature ${nwsTemp}°F"
        return nwsTemp
        
    } catch (Exception e) {
        logWarn "Error getting NWS temperature for skeeter: ${e.message} - using local sensor"
        return localTemp
    }
}

/**
 * Check if temperature is suitable for running skeeter killers
 * @return true if temp is above threshold, false if at or below threshold
 */
def isSkeeterTempOk() {
    if (!settings.skeeterTempSensors) {
        // No temp sensor configured, allow operation
        logDebug "No skeeter temperature sensors configured - allowing operation"
        return true
    }
    
    def temp = getSkeeterEffectiveTemperature()
    if (temp == null) {
        // Can't read temp, allow operation
        logDebug "Cannot get skeeter temperature - allowing operation"
        return true
    }
    
    def threshold = settings.skeeterTempThreshold ?: 50.0
    def tempOk = temp > threshold
    
    if (!tempOk) {
        logDebug "Skeeter temperature check failed: ${temp}°F <= ${threshold}°F"
    }
    
    return tempOk
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
    
    if (settings.greenhouseEnabled && settings.greenhouseTempSensors) {
        def reading = getMostRecentGreenhouseReading()
        if (reading) {
            def temp = reading.temp
            controlGreenhouseFan(temp)
            controlGreenhouseHeater(temp)
            checkFreezeRisk(temp)
        }
    }
    
    if (settings.officeEnabled && settings.officeTempSensor) {
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
// NWS TEMPERATURE HELPER METHODS
// ============================================================================

/**
 * Get the most recent temperature reading from configured greenhouse sensors.
 * @return Map with keys: temp, ageMinutes, sensorName; or null if no readings
 */
def getMostRecentGreenhouseReading() {
    if (!settings.greenhouseTempSensors) {
        return null
    }
    
    def bestReading = null
    def bestAge = 999999
    
    settings.greenhouseTempSensors.each { sensor ->
        def state = sensor.currentState("temperature")
        if (state) {
            def ageMinutes = (now() - state.date.time) / 60000
            if (ageMinutes < bestAge) {
                bestAge = ageMinutes
                bestReading = [
                    temp: state.value as BigDecimal,
                    ageMinutes: ageMinutes,
                    sensorName: sensor.displayName
                ]
            }
        }
    }
    
    return bestReading
}

/**
 * Get the effective temperature for threshold decisions.
 * First selects the most recent reading from local sensors,
 * then compares to NWS if local reading is older than preference threshold.
 *
 * @return The effective temperature to use for decisions, or null if unavailable
 */
def getEffectiveTemperature() {
    // Get most recent reading from local sensors
    def bestLocal = getMostRecentGreenhouseReading()
    if (bestLocal == null) {
        logWarn "No local temperature readings available"
        return null
    }
    
    def localTemp = bestLocal.temp
    def localAgeMinutes = bestLocal.ageMinutes
    def localSensorName = bestLocal.sensorName
    
    // If no NWS device configured, just use local
    if (!settings.nwsTempDevice) {
        logDebug "Using local temperature ${localTemp}°F from ${localSensorName} (${localAgeMinutes.toInteger()} min old)"
        return localTemp
    }
    
    try {
        // Prefer local sensor if reading is within configured preference time
        def localPrefMinutes = settings.localPreferenceMinutes ?: 5
        if (localAgeMinutes <= localPrefMinutes) {
            logDebug "Using local temperature ${localTemp}°F from ${localSensorName} (${localAgeMinutes.toInteger()} min old, within ${localPrefMinutes} min preference)"
            return localTemp
        }
        
        // Check if NWS data is stale
        if (isNwsDataStale()) {
            logDebug "NWS data is stale - using local sensor ${localSensorName} (${localAgeMinutes.toInteger()} min old)"
            return localTemp
        }
        
        // Get NWS temperature
        def nwsTemp = settings.nwsTempDevice.currentValue("temperature")
        if (nwsTemp == null) {
            logDebug "NWS temperature unavailable - using local sensor only"
            return localTemp
        }
        
        nwsTemp = nwsTemp as BigDecimal
        logDebug "Local reading older than preference (${localAgeMinutes.toInteger()} min) - using NWS temperature ${nwsTemp}°F"
        return nwsTemp
        
    } catch (Exception e) {
        logWarn "Error getting NWS temperature: ${e.message} - using local sensor"
        return localTemp
    }
}

/**
 * Check if the NWS weather data is stale (older than threshold)
 * @return true if data is stale or unavailable, false if fresh
 */
def isNwsDataStale() {
    if (!settings.nwsTempDevice) {
        return true
    }
    
    try {
        // First check the dataStale attribute if available
        def dataStale = settings.nwsTempDevice.currentValue("dataStale")
        if (dataStale == "true") {
            return true
        }
        
        // Also check lastUpdate timestamp
        def lastUpdate = settings.nwsTempDevice.currentValue("lastUpdate")
        if (!lastUpdate) {
            logDebug "No lastUpdate attribute from NWS device"
            return true
        }
        
        // Parse the ISO timestamp
        def lastUpdateDate = Date.parse("yyyy-MM-dd'T'HH:mm:ssXXX", lastUpdate.toString())
        def staleMinutes = settings.nwsStaleMinutes ?: 45
        def staleMs = staleMinutes * 60 * 1000
        def age = now() - lastUpdateDate.time
        
        if (age > staleMs) {
            logDebug "NWS data is ${(age / 60000).round(1)} minutes old (threshold: ${staleMinutes} minutes)"
            return true
        }
        
        return false
        
    } catch (Exception e) {
        logWarn "Error checking NWS data staleness: ${e.message}"
        return true
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
