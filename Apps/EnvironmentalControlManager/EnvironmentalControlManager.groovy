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
    namespace: "timbrown",
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
            
            input "hubVar_GreenhouseFanOnTemp", "decimal",
                  title: "Fan On Temperature",
                  description: "Turn greenhouse fan on above this temperature (°F). Sets GreenhouseFanOnTemp hub variable.",
                  defaultValue: 75.0,
                  required: false
            
            input "hubVar_GreenhouseFanOffTemp", "decimal",
                  title: "Fan Off Temperature",
                  description: "Turn greenhouse fan off below this temperature (°F). Sets GreenhouseFanOffTemp hub variable.",
                  defaultValue: 70.0,
                  required: false
            
            input "hubVar_GreenhouseHeaterOnTemp", "decimal",
                  title: "Heater On Temperature",
                  description: "Turn greenhouse heater on below this temperature (°F). Sets GreenhouseHeaterOnTemp hub variable.",
                  defaultValue: 40.0,
                  required: false
            
            input "hubVar_GreenhouseHeaterOffTemp", "decimal",
                  title: "Heater Off Temperature",
                  description: "Turn greenhouse heater off above this temperature (°F). Sets GreenhouseHeaterOffTemp hub variable.",
                  defaultValue: 45.0,
                  required: false
            
            input "hubVar_FreezeAlertThreshold", "decimal",
                  title: "Freeze Alert Temperature",
                  description: "Send freeze warning when temperature drops to this (°F). Sets FreezeAlertThreshold hub variable.",
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
            
            input "hubVar_OfficeHeaterTemp", "decimal",
                  title: "Office Heater Temperature",
                  description: "Target temperature for office heater (°F). Sets OfficeHeaterTemp hub variable.",
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
            
            input "hubVar_SkeeterIlluminanceThreshold", "number",
                  title: "Illuminance Threshold",
                  description: "Turn on mosquito control below this light level (lux). Sets SkeeterIlluminanceThreshold hub variable.",
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
            input "hubVar_WaterTimeout", "number",
                  title: "Water Auto-Off Timeout",
                  description: "Automatically shut off water after this duration (minutes). Sets WaterTimeout hub variable.",
                  defaultValue: 30,
                  range: "1..180",
                  required: false
            
            input "waterResetSwitch", "capability.switch",
                  title: "Water Reset Switch (Optional)",
                  description: "Switch to reset water timeout",
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>COLD WEATHER SENSORS</b>\n<b>═══════════════════════════════════════</b>") {
            input "freezeTempSensors", "capability.temperatureMeasurement",
                  title: "Cold Weather Temperature Sensors",
                  description: "Select one or more outdoor/pen sensors for freeze alerts, heat lamp, and chicken heater control (most recent reading will be used)",
                  multiple: true,
                  required: false
        }

        section("<b>═══════════════════════════════════════</b>\n<b>FREEZE ALERTS</b>\n<b>═══════════════════════════════════════</b>") {
            input "freezeThreshold", "decimal",
                  title: "Freeze Alert Temperature (°F)",
                  description: "Send alert when temperature falls to or below this value",
                  defaultValue: 32.0,
                  required: false
            
            input "freezeAlertMessage", "text",
                  title: "Freeze Alert Message",
                  defaultValue: "Freeze Warning",
                  required: false
            
            input "freezeEchoDevices", "capability.speechSynthesis",
                  title: "Echo Devices for Freeze Announcements",
                  multiple: true,
                  required: false
            
            input "freezeEchoVolume", "number",
                  title: "Echo Volume Level (1-100)",
                  defaultValue: 30,
                  range: "1..100",
                  required: false
            
            input "freezeResetVolume", "number",
                  title: "Reset Volume After Alert (1-100)",
                  description: "Volume to restore after alert completes",
                  defaultValue: 35,
                  range: "1..100",
                  required: false
            
            input "freezeNotificationSwitch", "capability.switch",
                  title: "Notification Control Switch (Optional)",
                  description: "When configured, only send freeze alerts when this switch is ON",
                  required: false
            
            input "freezeCooldownMinutes", "number",
                  title: "Alert Cooldown Period (minutes)",
                  description: "Minimum time between repeated freeze alerts",
                  defaultValue: 5,
                  range: "0..60",
                  required: false
        }

        section("<b>═══════════════════════════════════════</b>\n<b>HEAT LAMP CONTROL</b>\n<b>═══════════════════════════════════════</b>") {
            input "heatLampSwitch", "capability.switch",
                  title: "Heat Lamp Switch",
                  description: "Switch that controls the heat lamp",
                  required: false
            
            input "heatLampEnabled", "capability.switch",
                  title: "Heat Lamp Master Control Switch",
                  description: "Master switch to enable/disable heat lamp cycling (ON = enabled)",
                  required: false
            
            input "heatLampOnMinutes", "number",
                  title: "Heat Lamp ON Duration (minutes)",
                  description: "How long to keep heat lamp on during each cycle",
                  defaultValue: 15,
                  range: "1..60",
                  required: false
            
            input "heatLampOffMinutes", "number",
                  title: "Heat Lamp OFF Duration (minutes)",
                  description: "How long to keep heat lamp off during each cycle",
                  defaultValue: 15,
                  range: "1..60",
                  required: false
            
            input "heatLampThreshold", "decimal",
                  title: "Heat Lamp Trigger Temperature (°F)",
                  description: "Start heat lamp cycling when temperature falls to or below this value",
                  defaultValue: 32.0,
                  required: false
            
            input "cyclingIndicatorSwitch", "capability.switch",
                  title: "Heat Lamp Cycling Indicator (Connector)",
                  description: "Connector switch that shows ON when heat lamp cycling is active",
                  required: false
        }

        section("<b>═══════════════════════════════════════</b>\n<b>CHICKEN HEATER CONTROL</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Temperature-based hysteresis control for chicken pen heater. Uses the Cold Weather Temperature Sensors configured above."
            
            input "chickenHeaterSwitch", "capability.switch",
                  title: "Chicken Heater Switch",
                  description: "Switch that controls the chicken heater",
                  required: false
            
            input "chickenHeaterModes", "mode",
                  title: "Chicken Heater Active Modes",
                  description: "Chicken heater will only operate when hub is in one of these modes",
                  multiple: true,
                  required: false
            
            input "chickenMinTemp", "decimal",
                  title: "Heater ON Temperature (°F)",
                  description: "Turn heater ON when temperature falls to or below this value",
                  defaultValue: 46.0,
                  required: false
            
            input "chickenMaxTemp", "decimal",
                  title: "Heater OFF Temperature (°F)",
                  description: "Turn heater OFF when temperature rises to or above this value",
                  defaultValue: 48.0,
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
        
        section("<b>═══════════════════════════════════════</b>\n<b>HUB VARIABLES</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Configuration values above are stored as hub variables for cross-app sharing:"
            paragraph "• GreenhouseFanOnTemp, GreenhouseFanOffTemp - Greenhouse fan control"
            paragraph "• GreenhouseHeaterOnTemp, GreenhouseHeaterOffTemp - Greenhouse heater control"
            paragraph "• FreezeAlertThreshold - Freeze warning temperature"
            paragraph "• OfficeHeaterTemp - Office heater target"
            paragraph "• WaterTimeout - Water shutoff timeout"
            paragraph "• SkeeterIlluminanceThreshold - Mosquito control light threshold"
            paragraph "Hub variables are automatically synced when this app is updated."
            paragraph "Cold weather controls (freeze alerts, heat lamp, chicken heater) use app settings directly and do not require hub variables."
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>LOGGING</b>\n<b>═══════════════════════════════════════</b>") {
            input "logLevel", "enum", title: "Log Level", options: ["None","Info","Debug","Trace"], defaultValue: "Info", required: false
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
    syncHubVariables()
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
    
    // Subscribe to cold weather (freeze) sensors
    if (settings.freezeTempSensors) {
        settings.freezeTempSensors.each { sensor ->
            subscribe(sensor, "temperature", freezeTempHandler)
        }
        def sensorNames = settings.freezeTempSensors.collect { it.displayName }.join(", ")
        logInfo "Subscribed to ${settings.freezeTempSensors.size()} cold weather sensor(s): ${sensorNames}"
    }
    
    // Subscribe to heat lamp master control switch
    if (settings.heatLampEnabled) {
        subscribe(settings.heatLampEnabled, "switch", heatLampEnabledHandler)
        logInfo "Subscribed to heat lamp control switch: ${settings.heatLampEnabled.displayName}"
        
        // Check if we should start cycling on initialization (handles hub reboot)
        if (settings.heatLampEnabled.currentValue("switch") == "on" && settings.freezeTempSensors && settings.heatLampSwitch) {
            def currentTemp = getFreezeEffectiveTemperature()
            def threshold = settings.heatLampThreshold ?: 32.0
            if (currentTemp != null && currentTemp <= threshold && !state.heatLampCycling) {
                logInfo "Initialization: effective temperature ${currentTemp}°F <= ${threshold}°F and heat lamp enabled - starting cycling"
                startHeatLampCycling()
            }
        }
    }
    
    // Initialize heat lamp state (reset cycling on re-initialization)
    state.heatLampCycling = false
    state.heatLampCurrentlyOn = false
    if (cyclingIndicatorSwitch) {
        settings.cyclingIndicatorSwitch?.off()
    }
    
    // Initialize chicken heater announcement tracking
    state.chickenHeaterAnnouncedOn = state.chickenHeaterAnnouncedOn ?: false
    state.chickenHeaterAnnouncedOff = state.chickenHeaterAnnouncedOff ?: true
    
    // Check chicken heater state on startup
    checkChickenHeaterOnStartup()
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
    
    // Chicken heater mode control
    if (settings.chickenHeaterModes) {
        chickenHeaterModeHandler(evt)
    }
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
 * Get the best temperature reading from the configured freeze/cold weather sensors.
 * If all sensors are "active" (any attribute updated within the preference window),
 * returns the LOWEST temperature for freeze safety (coldest reading wins).
 * Otherwise, falls back to the most recent temperature reading.
 * @return Map with keys: temp, ageMinutes, sensorName; or null if no readings
 */
def getMostRecentFreezeSensorReading() {
    if (!settings.freezeTempSensors) {
        return null
    }
    
    def nowMs = now()
    def preferenceWindowMs = (settings.localPreferenceMinutes ?: 5) * 60000
    def validReadings = []
    def allWithinWindow = true
    
    settings.freezeTempSensors.each { sensor ->
        def tempState = sensor.currentState("temperature")
        if (tempState) {
            def mostRecentActivity = getMostRecentSensorActivity(sensor)
            def activityAgeMs = mostRecentActivity ? (nowMs - mostRecentActivity.date.time) : Long.MAX_VALUE
            def activityAttr = mostRecentActivity?.name ?: "unknown"
            
            def tempAgeMs = nowMs - tempState.date.time
            def tempAgeMinutes = tempAgeMs / 60000
            def temp = tempState.value as BigDecimal
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
                logDebug "Freeze sensor ${sensor.displayName}: temp ${tempAgeMinutes.toInteger()} min old, activity (${activityAttr}) ${(activityAgeMs/60000).toInteger()} min ago - outside window"
            } else {
                logDebug "Freeze sensor ${sensor.displayName}: active, temp: ${temp}°F"
            }
        }
    }
    
    if (validReadings.isEmpty()) return null
    
    def bestReading = null
    if (allWithinWindow) {
        def sortedByTemp = validReadings.sort { it.temp }
        bestReading = sortedByTemp.first()
        def allTemps = validReadings.collect { "${it.sensorName}: ${it.temp}°F" }.join(", ")
        logDebug "Freeze sensors: all active - selecting lowest temp. Sensors: [${allTemps}]"
    } else {
        def sortedByAge = validReadings.sort { it.ageMs }
        bestReading = sortedByAge.first()
        logDebug "Freeze sensors: not all active - using most recent reading from ${bestReading.sensorName}"
    }
    
    return [temp: bestReading.temp, ageMinutes: bestReading.ageMinutes, sensorName: bestReading.sensorName]
}

/**
 * Get the effective temperature for freeze/heat lamp/chicken heater decisions.
 * Uses cold weather (freeze) sensors with NWS backup.
 * @return The effective temperature to use, or null if unavailable
 */
def getFreezeEffectiveTemperature() {
    def bestLocal = getMostRecentFreezeSensorReading()
    if (bestLocal == null) {
        logWarn "No cold weather temperature readings available"
        return null
    }
    
    def localTemp = bestLocal.temp
    def localAgeMinutes = bestLocal.ageMinutes
    def localSensorName = bestLocal.sensorName
    
    if (!settings.nwsTempDevice) {
        logDebug "Freeze: Using local temperature ${localTemp}°F from ${localSensorName} (${localAgeMinutes.toInteger()} min old)"
        return localTemp
    }
    
    try {
        def localPreferenceMinutes = settings.localPreferenceMinutes ?: 5
        if (localAgeMinutes <= localPreferenceMinutes) {
            logDebug "Freeze: Using local temperature ${localTemp}°F from ${localSensorName} (within ${localPreferenceMinutes} min preference)"
            return localTemp
        }
        
        if (isNwsDataStale()) {
            logDebug "Freeze: NWS data stale - using local sensor ${localSensorName}"
            return localTemp
        }
        
        def nwsTemp = settings.nwsTempDevice.currentValue("temperature")
        if (nwsTemp == null) {
            logDebug "Freeze: NWS temperature unavailable - using local sensor"
            return localTemp
        }
        
        nwsTemp = nwsTemp as BigDecimal
        logDebug "Freeze: Local reading older than preference (${localAgeMinutes.toInteger()} min) - using NWS ${nwsTemp}°F"
        return nwsTemp
        
    } catch (Exception e) {
        logWarn "Freeze: Error getting NWS temperature: ${e.message} - using local sensor"
        return localTemp
    }
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
    
    // Check freeze sensors for heat lamp and chicken heater
    if (settings.freezeTempSensors) {
        def temp = getFreezeEffectiveTemperature()
        if (temp != null) {
            checkChickenHeater(temp)
            
            if (settings.heatLampEnabled?.currentValue("switch") == "on" && settings.heatLampSwitch) {
                def threshold = settings.heatLampThreshold ?: 32.0
                if (temp <= threshold && !state.heatLampCycling) {
                    startHeatLampCycling()
                } else if (temp > threshold && state.heatLampCycling) {
                    stopHeatLampCycling(false)
                }
            }
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

def syncHubVariables() {
    setGlobalVar("GreenhouseFanOnTemp", (hubVar_GreenhouseFanOnTemp ?: 75.0).toString())
    setGlobalVar("GreenhouseFanOffTemp", (hubVar_GreenhouseFanOffTemp ?: 70.0).toString())
    setGlobalVar("GreenhouseHeaterOnTemp", (hubVar_GreenhouseHeaterOnTemp ?: 40.0).toString())
    setGlobalVar("GreenhouseHeaterOffTemp", (hubVar_GreenhouseHeaterOffTemp ?: 45.0).toString())
    setGlobalVar("FreezeAlertThreshold", (hubVar_FreezeAlertThreshold ?: 32.0).toString())
    setGlobalVar("OfficeHeaterTemp", (hubVar_OfficeHeaterTemp ?: 68.0).toString())
    setGlobalVar("WaterTimeout", (hubVar_WaterTimeout ?: 30).toString())
    setGlobalVar("SkeeterIlluminanceThreshold", (hubVar_SkeeterIlluminanceThreshold ?: 200).toString())
    logInfo "Hub variables synced from app settings"
}

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
    
    logDebug "Hub variable '${hubVarName}' not set"
    return null
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
// FREEZE ALERT METHODS
// ============================================================================

def freezeTempHandler(evt) {
    def freezeThresholdVal = settings.freezeThreshold ?: 32.0
    def heatLampThresholdVal = settings.heatLampThreshold ?: 32.0
    
    def temp = getFreezeEffectiveTemperature()
    if (temp == null) {
        logWarn "Unable to get effective cold weather temperature - skipping"
        return
    }
    
    logDebug "Cold weather temperature event from ${evt.displayName}: ${evt.value}°F, Effective=${temp}°F (freeze: ${freezeThresholdVal}°F, heat lamp: ${heatLampThresholdVal}°F)"
    
    // Check freeze alert threshold
    if (temp <= freezeThresholdVal) {
        logWarn "Effective temperature ${temp}°F at or below freeze threshold ${freezeThresholdVal}°F"
        sendFreezeAlert(temp)
    }
    
    // Check heat lamp threshold
    if (temp <= heatLampThresholdVal) {
        if (settings.heatLampEnabled && settings.heatLampSwitch) {
            if (settings.heatLampEnabled.currentValue("switch") == "on" && !state.heatLampCycling) {
                logInfo "Temperature dropped to ${temp}°F - starting heat lamp cycling"
                startHeatLampCycling()
            }
        }
    } else {
        if (state.heatLampCycling) {
            logInfo "Temperature rose to ${temp}°F (above ${heatLampThresholdVal}°F) - stopping heat lamp cycling"
            stopHeatLampCycling(false)
        }
    }
    
    // Check chicken heater thresholds
    checkChickenHeater(temp)
}

def sendFreezeAlert(BigDecimal temp) {
    if (settings.freezeNotificationSwitch && settings.freezeNotificationSwitch.currentValue("switch") != "on") {
        logDebug "Notification switch is off - skipping freeze alert"
        return
    }
    
    Long cooldownMs = (settings.freezeCooldownMinutes ?: 5) * 60000
    if (state.lastFreezeAlertTime && (now() - state.lastFreezeAlertTime) < cooldownMs) {
        def elapsedMinutes = ((now() - state.lastFreezeAlertTime) / 60000).round(1)
        logDebug "Freeze alert in cooldown (${elapsedMinutes} min elapsed) - skipping"
        return
    }
    
    state.lastFreezeAlertTime = now()
    
    def message = settings.freezeAlertMessage ?: "Freeze Warning"
    def volume = settings.freezeEchoVolume ?: 30
    
    // Add heat lamp warning if configured but not enabled
    if (settings.heatLampSwitch && settings.heatLampEnabled) {
        if (settings.heatLampEnabled.currentValue("switch") != "on") {
            message = "${message}. Warning: Heat lamp is not enabled."
            logWarn "Heat lamp is configured but not enabled during freeze condition"
        }
    }
    
    if (settings.freezeEchoDevices) {
        settings.freezeEchoDevices.each { device ->
            logDebug "Sending freeze alert to ${device.displayName} at volume ${volume}: ${message}"
            device.speak(message, volume)
        }
        logInfo "Freeze alert sent to ${settings.freezeEchoDevices.size()} device(s): ${message} (${temp}°F)"
        runIn(10, 'resetFreezeEchoVolumes')
    }
    
    if (settings.notificationDevices) {
        def notificationMessage = "${message} - Temperature: ${temp}°F"
        settings.notificationDevices.each { device ->
            device.deviceNotification(notificationMessage)
        }
    }
}

def resetFreezeEchoVolumes() {
    def resetVol = settings.freezeResetVolume ?: 35
    settings.freezeEchoDevices?.each { device ->
        logDebug "Resetting volume for ${device.displayName} to ${resetVol}"
        device.setVolume(resetVol)
    }
}

// ============================================================================
// HEAT LAMP CONTROL METHODS
// ============================================================================

def heatLampEnabledHandler(evt) {
    logInfo "Heat lamp master switch changed to: ${evt.value}"
    
    if (evt.value == "on") {
        sendHeatLampNotification("Heat lamp control enabled")
        
        if (settings.freezeTempSensors && settings.heatLampSwitch) {
            def currentTemp = getFreezeEffectiveTemperature()
            def threshold = settings.heatLampThreshold ?: 32.0
            if (currentTemp != null && currentTemp <= threshold) {
                logInfo "Heat lamp enabled and temperature ${currentTemp}°F <= ${threshold}°F - starting cycling"
                startHeatLampCycling()
            } else if (currentTemp != null) {
                logInfo "Heat lamp enabled but temperature ${currentTemp}°F > ${threshold}°F - not cycling yet"
            }
        }
    } else {
        stopHeatLampCycling(true)
        sendHeatLampNotification("Heat lamp control disabled")
    }
}

def startHeatLampCycling() {
    if (state.heatLampCycling) {
        logDebug "Heat lamp cycling already active - skipping start"
        return
    }
    
    if (!settings.heatLampSwitch) {
        logWarn "No heat lamp switch configured - cannot start cycling"
        return
    }
    
    state.heatLampCycling = true
    settings.cyclingIndicatorSwitch?.on()
    logInfo "Starting heat lamp cycling (${settings.heatLampOnMinutes ?: 15} min on / ${settings.heatLampOffMinutes ?: 15} min off)"
    heatLampCycleOn()
}

def stopHeatLampCycling(Boolean sendNotif = false) {
    if (!state.heatLampCycling && !state.heatLampCurrentlyOn) {
        logDebug "Heat lamp cycling not active - skipping stop"
        return
    }
    
    unschedule('heatLampCycleOn')
    unschedule('heatLampCycleOff')
    
    if (settings.heatLampSwitch && state.heatLampCurrentlyOn) {
        settings.heatLampSwitch.off()
        logInfo "Heat lamp turned off"
    }
    
    state.heatLampCycling = false
    state.heatLampCurrentlyOn = false
    settings.cyclingIndicatorSwitch?.off()
    logInfo "Heat lamp cycling stopped"
}

def heatLampCycleOn() {
    if (!state.heatLampCycling) {
        logDebug "Heat lamp cycling disabled - not turning on"
        return
    }
    
    if (settings.heatLampEnabled?.currentValue("switch") != "on") {
        logDebug "Heat lamp master switch is off - stopping cycle"
        stopHeatLampCycling(false)
        return
    }
    
    if (settings.freezeTempSensors) {
        def currentTemp = getFreezeEffectiveTemperature()
        def threshold = settings.heatLampThreshold ?: 32.0
        if (currentTemp != null && currentTemp > threshold) {
            logInfo "Effective temperature ${currentTemp}°F now above threshold - stopping cycling"
            stopHeatLampCycling(false)
            return
        }
    }
    
    if (settings.heatLampSwitch) {
        settings.heatLampSwitch.on()
        state.heatLampCurrentlyOn = true
        def onMinutes = settings.heatLampOnMinutes ?: 15
        logInfo "Heat lamp turned ON - will turn off in ${onMinutes} minutes"
        runIn(onMinutes * 60, 'heatLampCycleOff')
    }
}

def heatLampCycleOff() {
    if (!state.heatLampCycling) {
        logDebug "Heat lamp cycling disabled - not scheduling next cycle"
        if (settings.heatLampSwitch && state.heatLampCurrentlyOn) {
            settings.heatLampSwitch.off()
            state.heatLampCurrentlyOn = false
        }
        return
    }
    
    if (settings.heatLampEnabled?.currentValue("switch") != "on") {
        logDebug "Heat lamp master switch is off - stopping cycle"
        stopHeatLampCycling(false)
        return
    }
    
    if (settings.heatLampSwitch) {
        settings.heatLampSwitch.off()
        state.heatLampCurrentlyOn = false
        def offMinutes = settings.heatLampOffMinutes ?: 15
        logInfo "Heat lamp turned OFF - will turn on in ${offMinutes} minutes"
        runIn(offMinutes * 60, 'heatLampCycleOn')
    }
}

def sendHeatLampNotification(String message) {
    def volume = settings.freezeEchoVolume ?: 30
    
    if (settings.freezeNotificationSwitch && settings.freezeNotificationSwitch.currentValue("switch") != "on") {
        logDebug "Notification switch is off - skipping heat lamp notification"
        return
    }
    
    if (settings.freezeEchoDevices) {
        settings.freezeEchoDevices.each { device ->
            device.speak(message, volume)
        }
        logInfo "Heat lamp notification sent: ${message}"
        runIn(10, 'resetFreezeEchoVolumes')
    }
    
    if (settings.notificationDevices) {
        settings.notificationDevices.each { device ->
            device.deviceNotification(message)
        }
    }
}

// ============================================================================
// CHICKEN HEATER CONTROL METHODS
// ============================================================================

def checkChickenHeater(BigDecimal temp) {
    if (!settings.chickenHeaterSwitch) return
    
    if (!isChickenHeaterModeActive()) {
        logDebug "Chicken heater not active in current mode (${location.mode}) - skipping"
        return
    }
    
    def minTemp = settings.chickenMinTemp ?: 46.0
    def maxTemp = settings.chickenMaxTemp ?: 48.0
    def currentState = settings.chickenHeaterSwitch.currentValue("switch")
    
    if (temp <= minTemp && currentState != "on") {
        logInfo "Temperature ${temp}°F <= ${minTemp}°F - turning chicken heater ON"
        settings.chickenHeaterSwitch.on()
        if (!state.chickenHeaterAnnouncedOn) {
            sendChickenHeaterNotification("Chicken heater turned ON. Temperature: ${temp}°F")
            state.chickenHeaterAnnouncedOn = true
            state.chickenHeaterAnnouncedOff = false
        }
    } else if (temp >= maxTemp && currentState == "on") {
        logInfo "Temperature ${temp}°F >= ${maxTemp}°F - turning chicken heater OFF"
        settings.chickenHeaterSwitch.off()
        if (!state.chickenHeaterAnnouncedOff) {
            sendChickenHeaterNotification("Chicken heater turned OFF. Temperature: ${temp}°F")
            state.chickenHeaterAnnouncedOff = true
            state.chickenHeaterAnnouncedOn = false
        }
    }
}

def chickenHeaterModeHandler(evt) {
    if (isChickenHeaterModeActive()) {
        logInfo "Chicken heater active in mode: ${evt.value}"
        checkChickenHeaterOnStartup()
    } else {
        if (settings.chickenHeaterSwitch?.currentValue("switch") == "on") {
            settings.chickenHeaterSwitch.off()
            logInfo "Chicken heater turned OFF (mode ${evt.value} not in active modes)"
            if (!state.chickenHeaterAnnouncedOff) {
                sendChickenHeaterNotification("Chicken heater turned OFF. Mode changed to ${evt.value}")
                state.chickenHeaterAnnouncedOff = true
                state.chickenHeaterAnnouncedOn = false
            }
        }
    }
}

def isChickenHeaterModeActive() {
    if (!settings.chickenHeaterModes) return true
    return settings.chickenHeaterModes.contains(location.mode)
}

def checkChickenHeaterOnStartup() {
    if (!settings.freezeTempSensors || !settings.chickenHeaterSwitch) {
        logDebug "Chicken heater not fully configured - skipping startup check"
        return
    }
    
    if (!isChickenHeaterModeActive()) {
        logDebug "Chicken heater not active in current mode (${location.mode}) - skipping startup check"
        return
    }
    
    def currentTemp = getFreezeEffectiveTemperature()
    if (currentTemp == null) {
        logDebug "Unable to get temperature - skipping chicken heater startup check"
        return
    }
    
    def minTemp = settings.chickenMinTemp ?: 46.0
    def maxTemp = settings.chickenMaxTemp ?: 48.0
    def currentState = settings.chickenHeaterSwitch.currentValue("switch")
    
    logInfo "Chicken heater startup check: temp=${currentTemp}°F, heater=${currentState}"
    
    if (currentTemp <= minTemp && currentState != "on") {
        logInfo "Startup: Temperature ${currentTemp}°F <= ${minTemp}°F - turning chicken heater ON"
        settings.chickenHeaterSwitch.on()
        if (!state.chickenHeaterAnnouncedOn) {
            sendChickenHeaterNotification("Chicken heater turned ON. Temperature: ${currentTemp}°F")
            state.chickenHeaterAnnouncedOn = true
            state.chickenHeaterAnnouncedOff = false
        }
    } else if (currentTemp >= maxTemp && currentState == "on") {
        logInfo "Startup: Temperature ${currentTemp}°F >= ${maxTemp}°F - turning chicken heater OFF"
        settings.chickenHeaterSwitch.off()
        if (!state.chickenHeaterAnnouncedOff) {
            sendChickenHeaterNotification("Chicken heater turned OFF. Temperature: ${currentTemp}°F")
            state.chickenHeaterAnnouncedOff = true
            state.chickenHeaterAnnouncedOn = false
        }
    }
}

def sendChickenHeaterNotification(String message) {
    def volume = settings.freezeEchoVolume ?: 30
    
    if (settings.freezeNotificationSwitch && settings.freezeNotificationSwitch.currentValue("switch") != "on") {
        logDebug "Notification switch is off - skipping chicken heater notification"
        return
    }
    
    if (settings.freezeEchoDevices) {
        settings.freezeEchoDevices.each { device ->
            device.speak(message, volume)
        }
        logInfo "Chicken heater notification sent: ${message}"
        runIn(10, 'resetFreezeEchoVolumes')
    }
    
    if (settings.notificationDevices) {
        settings.notificationDevices.each { device ->
            device.deviceNotification(message)
        }
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

def uninstalled() {
    logInfo "Environmental Control Manager uninstalled"
    unschedule()
}

// ============================================================================
// LOGGING
// ============================================================================

def logInfo(String msg) {
    if (logLevel in ["Info","Debug","Trace"]) log.info "${app.label}: ${msg}"
}

def logDebug(String msg) {
    if (logLevel in ["Debug","Trace"]) log.debug "${app.label}: ${msg}"
}

void logTrace(String msg) { if (logLevel == "Trace") log.trace "${app.label}: ${msg}" }

def logWarn(String msg) {
    log.warn "[Environmental Control Manager] ${msg}"
}

def logError(String msg) {
    log.error "[Environmental Control Manager] ${msg}"
}
