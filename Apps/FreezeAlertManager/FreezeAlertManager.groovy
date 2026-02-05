/**
 *  Freeze Alert Manager
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
    name: "Freeze Alert Manager",
    namespace: "tibrown",
    author: "Tim Brown",
    description: "Monitors temperature sensor and announces freeze warnings via Echo devices when temperature drops below configurable threshold",
    category: "Safety & Security",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    singleThreaded: true
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Freeze Alert Manager", install: true, uninstall: true) {
        section("<b>═══════════════════════════════════════</b>\n<b>TEMPERATURE MONITORING</b>\n<b>═══════════════════════════════════════</b>") {
            input "tempSensors", "capability.temperatureMeasurement",
                  title: "Temperature Sensors",
                  description: "Select one or more local temperature sensors (most recent reading will be used)",
                  multiple: true,
                  required: true
            
            input "freezeThreshold", "decimal",
                  title: "Freeze Alert Temperature (°F)",
                  description: "Send alert when temperature falls to or below this value",
                  defaultValue: 32.0,
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>ALERT CONFIGURATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "echoDevices", "capability.speechSynthesis",
                  title: "Echo Devices for Announcements",
                  multiple: true,
                  required: true
            
            input "alertMessage", "text",
                  title: "Alert Message",
                  defaultValue: "Freeze Warning",
                  required: false
            
            input "echoVolume", "number",
                  title: "Echo Volume Level (1-100)",
                  defaultValue: 30,
                  range: "1..100",
                  required: false
            
            input "resetVolume", "number",
                  title: "Reset Volume After Alert (1-100)",
                  description: "Volume to restore after alert completes",
                  defaultValue: 35,
                  range: "1..100",
                  required: true
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>NOTIFICATION CONTROL</b>\n<b>═══════════════════════════════════════</b>") {
            input "notificationDevices", "capability.notification",
                  title: "Push Notification Devices",
                  multiple: true,
                  required: false
            
            input "notificationSwitch", "capability.switch",
                  title: "Notification Control Switch",
                  description: "Select a switch to control notifications (ON = enabled, OFF = disabled)",
                  required: false
            
            input "cooldownMinutes", "number",
                  title: "Alert Cooldown Period (minutes)",
                  description: "Minimum time between repeated alerts",
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
            paragraph "Temperature-based hysteresis control for chicken pen heater. Uses the same temperature sensors configured above. Heater turns ON when temperature falls to or below the minimum threshold, and turns OFF when temperature rises to or above the maximum threshold."
            
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
        
        section("<b>═══════════════════════════════════════</b>\n<b>NWS WEATHER BACKUP</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Optionally configure a NWS Weather device to provide backup temperature readings. " +
                     "When configured, the app will use the <b>lower</b> of your local sensor or NWS temperature " +
                     "for all threshold decisions. This helps compensate for sensors that read high due to solar heating."
            
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
                  title: "Local Sensor Preference Time (minutes)",
                  description: "Always prefer local sensor if reading is this fresh (default: 5 minutes)",
                  defaultValue: 5,
                  required: false
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
    logInfo "Freeze Alert Manager installed"
    initialize()
}

def updated() {
    logInfo "Freeze Alert Manager updated"
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    logInfo "Initializing Freeze Alert Manager"
    
    // Initialize heat lamp state (reset on re-initialization since schedules are cleared by updated())
    state.heatLampCycling = false
    state.heatLampCurrentlyOn = false
    
    // Sync cycling indicator to current state
    if (state.heatLampCycling) {
        settings.cyclingIndicatorSwitch?.on()
    } else {
        settings.cyclingIndicatorSwitch?.off()
    }
    
    // Initialize chicken heater announced state (tracks if we've announced the current state)
    state.chickenHeaterAnnouncedOn = state.chickenHeaterAnnouncedOn ?: false
    state.chickenHeaterAnnouncedOff = state.chickenHeaterAnnouncedOff ?: true
    
    if (settings.tempSensors) {
        settings.tempSensors.each { sensor ->
            subscribe(sensor, "temperature", tempHandler)
        }
        def sensorNames = settings.tempSensors.collect { it.displayName }.join(", ")
        logInfo "Subscribed to ${settings.tempSensors.size()} temperature sensor(s): ${sensorNames}"
    } else {
        logWarn "No temperature sensors configured"
    }
    
    // Subscribe to heat lamp master control switch
    if (settings.heatLampEnabled) {
        subscribe(settings.heatLampEnabled, "switch", heatLampEnabledHandler)
        logInfo "Subscribed to heat lamp control switch: ${settings.heatLampEnabled.displayName}"
        
        // Check if we should start cycling on initialization (handles hub reboot)
        if (settings.heatLampEnabled.currentValue("switch") == "on" && settings.tempSensors && settings.heatLampSwitch) {
            def currentTemp = getEffectiveTemperature()
            def threshold = settings.heatLampThreshold ?: 32.0
            if (currentTemp != null && currentTemp <= threshold && !state.heatLampCycling) {
                logInfo "Initialization: Effective temperature ${currentTemp}°F <= ${threshold}°F and heat lamp enabled - starting cycling"
                startHeatLampCycling()
            }
        }
    }
    
    // Log NWS weather device configuration
    if (settings.nwsTempDevice) {
        logInfo "NWS Weather Device configured: ${settings.nwsTempDevice.displayName}"
        def nwsTemp = settings.nwsTempDevice.currentValue("temperature")
        if (nwsTemp != null) {
            logInfo "Current NWS temperature: ${nwsTemp}°F"
        }
    } else {
        logDebug "No NWS Weather Device configured - using local sensor only"
    }
    
    // Subscribe to mode changes for chicken heater control
    if (settings.chickenHeaterModes) {
        subscribe(location, "mode", chickenHeaterModeHandler)
        logInfo "Subscribed to mode changes for chicken heater (active modes: ${settings.chickenHeaterModes})"
    }
    
    // Check chicken heater state on startup
    checkChickenHeaterOnStartup()
    
    logInfo "Monitoring for temperatures at or below ${settings.freezeThreshold ?: 32.0}°F"
}

def tempHandler(evt) {
    def freezeThreshold = settings.freezeThreshold ?: 32.0
    def heatLampThreshold = settings.heatLampThreshold ?: 32.0
    
    // Get effective temperature (best local sensor vs NWS)
    def temp = getEffectiveTemperature()
    if (temp == null) {
        logWarn "Unable to get effective temperature - skipping"
        return
    }
    
    logDebug "Temperature event from ${evt.displayName}: ${evt.value}°F, Effective=${temp}°F (freeze alert: ${freezeThreshold}°F, heat lamp: ${heatLampThreshold}°F)"
    
    // Check freeze alert threshold
    if (temp <= freezeThreshold) {
        logWarn "Effective temperature ${temp}°F at or below freeze threshold ${freezeThreshold}°F"
        sendFreezeAlert(temp)
    }
    
    // Check heat lamp threshold (separate from freeze alert)
    if (temp <= heatLampThreshold) {
        if (settings.heatLampEnabled && settings.heatLampSwitch) {
            if (settings.heatLampEnabled.currentValue("switch") == "on" && !state.heatLampCycling) {
                logInfo "Temperature dropped to ${temp}°F - starting heat lamp cycling"
                startHeatLampCycling()
            }
        }
    } else {
        // Temperature above heat lamp threshold - stop heat lamp cycling if active
        if (state.heatLampCycling) {
            logInfo "Temperature rose to ${temp}°F (above ${heatLampThreshold}°F) - stopping heat lamp cycling"
            stopHeatLampCycling(false)  // false = don't send notification (temp-based stop)
        }
    }
    
    // Check chicken heater thresholds
    checkChickenHeater(temp)
}

def sendFreezeAlert(BigDecimal temp) {
    // Check if notifications are enabled via switch
    if (settings.notificationSwitch && settings.notificationSwitch.currentValue("switch") != "on") {
        logDebug "Notification switch is off - skipping alert"
        return
    }
    
    if (!settings.notificationSwitch) {
        logDebug "No notification switch configured - notifications enabled by default"
    }
    
    // Check cooldown period
    Long cooldownMs = (settings.cooldownMinutes ?: 5) * 60000
    if (state.lastAlertTime && (now() - state.lastAlertTime) < cooldownMs) {
        def elapsedMinutes = ((now() - state.lastAlertTime) / 60000).round(1)
        logDebug "Alert in cooldown period (${elapsedMinutes} minutes elapsed) - skipping"
        return
    }
    
    // Update state
    state.lastAlertTime = now()
    
    // Send to Echo devices
    def message = settings.alertMessage ?: "Freeze Warning"
    def volume = settings.echoVolume ?: 30
    
    // Add heat lamp warning if configured but not enabled
    if (settings.heatLampSwitch && settings.heatLampEnabled) {
        if (settings.heatLampEnabled.currentValue("switch") != "on") {
            message = "${message}. Warning: Heat lamp is not enabled."
            logWarn "Heat lamp is configured but not enabled during freeze condition"
        }
    }
    
    if (settings.echoDevices) {
        settings.echoDevices.each { device ->
            logDebug "Sending alert to ${device.displayName} at volume ${volume}: ${message}"
            device.speak(message, volume)
        }
        logInfo "Freeze alert sent to ${settings.echoDevices.size()} device(s): ${message} (Temperature: ${temp}°F)"
        
        // Schedule volume reset after alert completes
        runIn(10, 'resetEchoVolumes')
    } else {
        logWarn "No Echo devices configured - cannot send alert"
    }
    
    // Send push notifications
    if (settings.notificationDevices) {
        def notificationMessage = "${message} - Temperature: ${temp}°F"
        settings.notificationDevices.each { device ->
            logDebug "Sending push notification to ${device.displayName}: ${notificationMessage}"
            device.deviceNotification(notificationMessage)
        }
        logInfo "Push notifications sent to ${settings.notificationDevices.size()} device(s)"
    }
}

def resetEchoVolumes() {
    def resetVol = settings.resetVolume ?: 35
    
    if (settings.echoDevices) {
        settings.echoDevices.each { device ->
            logDebug "Resetting volume for ${device.displayName} to ${resetVol}"
            device.setVolume(resetVol)
        }
        logInfo "Reset volume to ${resetVol} for ${settings.echoDevices.size()} device(s)"
    }
}

// ═══════════════════════════════════════
// HEAT LAMP CONTROL METHODS
// ═══════════════════════════════════════

def heatLampEnabledHandler(evt) {
    logInfo "Heat lamp master switch changed to: ${evt.value}"
    
    if (evt.value == "on") {
        // Send notification that heat lamp control is enabled
        sendHeatLampNotification("Heat lamp control enabled")
        
        // Check if we should start cycling based on current temperature
        if (settings.tempSensors && settings.heatLampSwitch) {
            def currentTemp = getEffectiveTemperature()
            def threshold = settings.heatLampThreshold ?: 32.0
            if (currentTemp != null && currentTemp <= threshold) {
                logInfo "Heat lamp enabled and temperature ${currentTemp}°F <= ${threshold}°F - starting cycling"
                startHeatLampCycling()
            } else if (currentTemp != null) {
                logInfo "Heat lamp enabled but temperature ${currentTemp}°F > ${threshold}°F - not cycling yet"
            }
        }
    } else {
        // Master switch turned off - stop cycling and notify
        stopHeatLampCycling(true)  // true = send notification
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

def stopHeatLampCycling(Boolean sendNotification = false) {
    if (!state.heatLampCycling && !state.heatLampCurrentlyOn) {
        logDebug "Heat lamp cycling not active - skipping stop"
        return
    }
    
    // Unschedule any pending cycle operations
    unschedule('heatLampCycleOn')
    unschedule('heatLampCycleOff')
    
    // Turn off the heat lamp
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
    // Guard checks
    if (!state.heatLampCycling) {
        logDebug "Heat lamp cycling disabled - not turning on"
        return
    }
    
    if (settings.heatLampEnabled?.currentValue("switch") != "on") {
        logDebug "Heat lamp master switch is off - stopping cycle"
        stopHeatLampCycling(false)
        return
    }
    
    // Re-check temperature before turning on (using effective temperature)
    if (settings.tempSensors) {
        def currentTemp = getEffectiveTemperature()
        def threshold = settings.heatLampThreshold ?: 32.0
        if (currentTemp != null && currentTemp > threshold) {
            logInfo "Effective temperature ${currentTemp}°F now above threshold - stopping cycling"
            stopHeatLampCycling(false)
            return
        }
    }
    
    // Turn on the heat lamp
    if (settings.heatLampSwitch) {
        settings.heatLampSwitch.on()
        state.heatLampCurrentlyOn = true
        def onMinutes = settings.heatLampOnMinutes ?: 15
        logInfo "Heat lamp turned ON - will turn off in ${onMinutes} minutes"
        
        // Schedule turn off
        runIn(onMinutes * 60, 'heatLampCycleOff')
    }
}

def heatLampCycleOff() {
    // Guard checks
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
    
    // Turn off the heat lamp
    if (settings.heatLampSwitch) {
        settings.heatLampSwitch.off()
        state.heatLampCurrentlyOn = false
        def offMinutes = settings.heatLampOffMinutes ?: 15
        logInfo "Heat lamp turned OFF - will turn on in ${offMinutes} minutes"
        
        // Schedule turn on
        runIn(offMinutes * 60, 'heatLampCycleOn')
    }
}

def sendHeatLampNotification(String message) {
    def volume = settings.echoVolume ?: 30
    
    // Check if notifications are enabled via switch
    if (settings.notificationSwitch && settings.notificationSwitch.currentValue("switch") != "on") {
        logDebug "Notification switch is off - skipping heat lamp notification"
        return
    }
    
    if (settings.echoDevices) {
        settings.echoDevices.each { device ->
            logDebug "Sending heat lamp notification to ${device.displayName}: ${message}"
            device.speak(message, volume)
        }
        logInfo "Heat lamp notification sent: ${message}"
        
        // Schedule volume reset
        runIn(10, 'resetEchoVolumes')
    }
    
    if (settings.notificationDevices) {
        settings.notificationDevices.each { device ->
            device.deviceNotification(message)
        }
    }
}

// ═══════════════════════════════════════
// CHICKEN HEATER CONTROL METHODS
// ═══════════════════════════════════════

def checkChickenHeater(BigDecimal temp) {
    def minTemp = settings.chickenMinTemp ?: 46.0
    def maxTemp = settings.chickenMaxTemp ?: 48.0
    
    if (!settings.chickenHeaterSwitch) {
        return
    }
    
    // Check if current mode allows chicken heater operation
    if (!isChickenHeaterModeActive()) {
        logDebug "Chicken heater not active in current mode (${location.mode}) - skipping"
        return
    }
    
    def currentState = settings.chickenHeaterSwitch.currentValue("switch")
    
    // Hysteresis control logic
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
    logInfo "Mode changed to: ${evt.value}"
    
    if (isChickenHeaterModeActive()) {
        logInfo "Chicken heater active in mode: ${evt.value}"
        // Check current temperature and set heater state
        checkChickenHeaterOnStartup()
    } else {
        // Mode not in allowed list - turn off heater
        if (settings.chickenHeaterSwitch && settings.chickenHeaterSwitch.currentValue("switch") == "on") {
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
    // If no modes configured, chicken heater is always active
    if (!settings.chickenHeaterModes) {
        return true
    }
    return settings.chickenHeaterModes.contains(location.mode)
}

def checkChickenHeaterOnStartup() {
    if (!settings.tempSensors || !settings.chickenHeaterSwitch) {
        logDebug "Chicken heater not fully configured - skipping startup check"
        return
    }
    
    // Check if current mode allows chicken heater operation
    if (!isChickenHeaterModeActive()) {
        logDebug "Chicken heater not active in current mode (${location.mode}) - skipping startup check"
        return
    }
    
    def currentTemp = getEffectiveTemperature()
    if (currentTemp == null) {
        logDebug "Unable to get temperature - skipping chicken heater startup check"
        return
    }
    
    def minTemp = settings.chickenMinTemp ?: 46.0
    def maxTemp = settings.chickenMaxTemp ?: 48.0
    def currentState = settings.chickenHeaterSwitch.currentValue("switch")
    
    logInfo "Chicken heater startup check: Effective=${currentTemp}°F, Heater ${currentState}"
    
    // Apply hysteresis logic on startup
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
    } else {
        logDebug "Startup: Chicken heater state OK (temp: ${currentTemp}°F, heater: ${currentState})"
    }
}

def sendChickenHeaterNotification(String message) {
    def volume = settings.echoVolume ?: 30
    
    // Check if notifications are enabled via switch
    if (settings.notificationSwitch && settings.notificationSwitch.currentValue("switch") != "on") {
        logDebug "Notification switch is off - skipping chicken heater notification"
        return
    }
    
    if (settings.echoDevices) {
        settings.echoDevices.each { device ->
            logDebug "Sending chicken heater notification to ${device.displayName}: ${message}"
            device.speak(message, volume)
        }
        logInfo "Chicken heater notification sent: ${message}"
        
        // Schedule volume reset
        runIn(10, 'resetEchoVolumes')
    }
    
    if (settings.notificationDevices) {
        settings.notificationDevices.each { device ->
            device.deviceNotification(message)
        }
    }
}

def uninstalled() {
    logInfo "Freeze Alert Manager uninstalled"
    unschedule('resetEchoVolumes')
    unschedule('heatLampCycleOn')
    unschedule('heatLampCycleOff')
}

// ═══════════════════════════════════════
// NWS TEMPERATURE HELPER METHODS
// ═══════════════════════════════════════

/**
 * Get the most recent attribute update time from a sensor, checking
 * temperature, illuminance, and humidity attributes to determine if sensor is active.
 * This handles sensors where temperature may remain stable but other attributes update.
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
 * Get the best temperature reading from configured local sensors.
 * If all sensors are "active" (any attribute updated within the preference window),
 * returns the LOWEST temperature for freeze safety (coldest reading wins regardless of recency).
 * Otherwise, falls back to the most recent temperature reading.
 * @return Map with keys: temp, ageMinutes, sensorName; or null if no readings
 */
def getMostRecentLocalReading() {
    if (!settings.tempSensors) {
        return null
    }
    
    def nowMs = now()
    def preferenceWindowMs = (settings.localPreferenceMinutes ?: 5) * 60000
    def validReadings = []
    def allWithinWindow = true
    
    // First pass: collect all valid readings and check if all sensors are active
    settings.tempSensors.each { sensor ->
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
                logDebug "${sensor.displayName}: temp ${tempAgeMinutes.toInteger()} min old, most recent activity (${activityAttr}) ${(activityAgeMs/60000).toInteger()} min ago - outside window"
            } else {
                logDebug "${sensor.displayName}: active (last ${activityAttr} update ${(activityAgeMs/60000).toInteger()} min ago), temp: ${temp}°F"
            }
        }
    }
    
    if (validReadings.isEmpty()) {
        return null
    }
    
    def bestReading = null
    
    if (allWithinWindow && validReadings.size() > 0) {
        // All sensors active - use LOWEST temperature for freeze safety
        def sortedByTemp = validReadings.sort { it.temp }
        bestReading = sortedByTemp.first()
        
        // Log all sensor temps for debugging
        def allTemps = validReadings.collect { "${it.sensorName}: ${it.temp}°F (temp ${it.ageMinutes.toInteger()} min, activity ${(it.activityAgeMs/60000).toInteger()} min)" }.join(", ")
        logDebug "All sensors active - selecting lowest temp for freeze safety. Sensors: [${allTemps}]"
    } else {
        // Not all sensors active - fall back to most recent temperature reading
        def sortedByAge = validReadings.sort { it.ageMs }
        bestReading = sortedByAge.first()
        logDebug "Not all sensors active within preference window - using most recent temperature reading"
    }
    
    return [
        temp: bestReading.temp,
        ageMinutes: bestReading.ageMinutes,
        sensorName: bestReading.sensorName
    ]
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
    def bestLocal = getMostRecentLocalReading()
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
        def localPreferenceMinutes = settings.localPreferenceMinutes ?: 5
        if (localAgeMinutes <= localPreferenceMinutes) {
            logDebug "Using local temperature ${localTemp}°F from ${localSensorName} (${localAgeMinutes.toInteger()} min old, within ${localPreferenceMinutes} min preference)"
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

// ═══════════════════════════════════════
// LOGGING METHODS
// ═══════════════════════════════════════

def logInfo(String msg) {
    log.info "[Freeze Alert Manager] ${msg}"
}

def logDebug(String msg) {
    if (settings.logEnable) {
        log.debug "[Freeze Alert Manager] ${msg}"
    }
}

def logWarn(String msg) {
    log.warn "[Freeze Alert Manager] ${msg}"
}
