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
            input "tempSensor", "capability.temperatureMeasurement",
                  title: "Temperature Sensor",
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
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>CHICKEN HEATER CONTROL</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Temperature-based hysteresis control for chicken pen heater. Heater turns ON when temperature falls to or below the minimum threshold, and turns OFF when temperature rises to or above the maximum threshold."
            
            input "chickenTempSensor", "capability.temperatureMeasurement",
                  title: "Chicken Pen Temperature Sensor",
                  description: "Temperature sensor for chicken pen",
                  required: false
            
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
    
    // Initialize heat lamp state
    state.heatLampCycling = state.heatLampCycling ?: false
    state.heatLampCurrentlyOn = state.heatLampCurrentlyOn ?: false
    
    // Initialize chicken heater announced state (tracks if we've announced the current state)
    state.chickenHeaterAnnouncedOn = state.chickenHeaterAnnouncedOn ?: false
    state.chickenHeaterAnnouncedOff = state.chickenHeaterAnnouncedOff ?: true
    
    if (settings.tempSensor) {
        subscribe(settings.tempSensor, "temperature", tempHandler)
        logInfo "Subscribed to temperature sensor: ${settings.tempSensor.displayName}"
    } else {
        logWarn "No temperature sensor configured"
    }
    
    // Subscribe to heat lamp master control switch
    if (settings.heatLampEnabled) {
        subscribe(settings.heatLampEnabled, "switch", heatLampEnabledHandler)
        logInfo "Subscribed to heat lamp control switch: ${settings.heatLampEnabled.displayName}"
        
        // Check if we should start cycling on initialization (handles hub reboot)
        if (settings.heatLampEnabled.currentValue("switch") == "on" && settings.tempSensor && settings.heatLampSwitch) {
            def currentTemp = settings.tempSensor.currentValue("temperature") as BigDecimal
            def threshold = settings.heatLampThreshold ?: 32.0
            if (currentTemp <= threshold && !state.heatLampCycling) {
                logInfo "Initialization: Temperature ${currentTemp}°F <= ${threshold}°F and heat lamp enabled - starting cycling"
                startHeatLampCycling()
            }
        }
    }
    
    // Subscribe to chicken heater temperature sensor
    if (settings.chickenTempSensor) {
        subscribe(settings.chickenTempSensor, "temperature", chickenTempHandler)
        logInfo "Subscribed to chicken pen sensor: ${settings.chickenTempSensor.displayName}"
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
    def temp = evt.value as BigDecimal
    def freezeThreshold = settings.freezeThreshold ?: 32.0
    def heatLampThreshold = settings.heatLampThreshold ?: 32.0
    
    logDebug "Temperature event: ${temp}°F (freeze alert: ${freezeThreshold}°F, heat lamp: ${heatLampThreshold}°F)"
    
    // Check freeze alert threshold
    if (temp <= freezeThreshold) {
        logWarn "Temperature ${temp}°F at or below freeze threshold ${freezeThreshold}°F"
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
        if (settings.tempSensor && settings.heatLampSwitch) {
            def currentTemp = settings.tempSensor.currentValue("temperature") as BigDecimal
            def threshold = settings.freezeThreshold ?: 32.0
            if (currentTemp <= threshold) {
                logInfo "Heat lamp enabled and temperature ${currentTemp}°F <= ${threshold}°F - starting cycling"
                startHeatLampCycling()
            } else {
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
    
    // Re-check temperature before turning on
    if (settings.tempSensor) {
        def currentTemp = settings.tempSensor.currentValue("temperature") as BigDecimal
        def threshold = settings.heatLampThreshold ?: 32.0
        if (currentTemp > threshold) {
            logInfo "Temperature ${currentTemp}°F now above threshold - stopping cycling"
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

def chickenTempHandler(evt) {
    def temp = evt.value as BigDecimal
    def minTemp = settings.chickenMinTemp ?: 46.0
    def maxTemp = settings.chickenMaxTemp ?: 48.0
    
    logDebug "Chicken pen temperature: ${temp}°F (ON <= ${minTemp}°F, OFF >= ${maxTemp}°F)"
    
    if (!settings.chickenHeaterSwitch) {
        logDebug "No chicken heater switch configured - skipping"
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
        logInfo "Chicken pen temperature ${temp}°F <= ${minTemp}°F - turning heater ON"
        settings.chickenHeaterSwitch.on()
        if (!state.chickenHeaterAnnouncedOn) {
            sendChickenHeaterNotification("Chicken heater turned ON. Temperature: ${temp}°F")
            state.chickenHeaterAnnouncedOn = true
            state.chickenHeaterAnnouncedOff = false
        }
    } else if (temp >= maxTemp && currentState == "on") {
        logInfo "Chicken pen temperature ${temp}°F >= ${maxTemp}°F - turning heater OFF"
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
    if (!settings.chickenTempSensor || !settings.chickenHeaterSwitch) {
        logDebug "Chicken heater not fully configured - skipping startup check"
        return
    }
    
    // Check if current mode allows chicken heater operation
    if (!isChickenHeaterModeActive()) {
        logDebug "Chicken heater not active in current mode (${location.mode}) - skipping startup check"
        return
    }
    
    def currentTemp = settings.chickenTempSensor.currentValue("temperature") as BigDecimal
    def minTemp = settings.chickenMinTemp ?: 46.0
    def maxTemp = settings.chickenMaxTemp ?: 48.0
    def currentState = settings.chickenHeaterSwitch.currentValue("switch")
    
    logInfo "Chicken heater startup check: Temperature ${currentTemp}°F, Heater ${currentState}"
    
    // Apply hysteresis logic on startup
    if (currentTemp <= minTemp && currentState != "on") {
        logInfo "Startup: Chicken pen temperature ${currentTemp}°F <= ${minTemp}°F - turning heater ON"
        settings.chickenHeaterSwitch.on()
        if (!state.chickenHeaterAnnouncedOn) {
            sendChickenHeaterNotification("Chicken heater turned ON. Temperature: ${currentTemp}°F")
            state.chickenHeaterAnnouncedOn = true
            state.chickenHeaterAnnouncedOff = false
        }
    } else if (currentTemp >= maxTemp && currentState == "on") {
        logInfo "Startup: Chicken pen temperature ${currentTemp}°F >= ${maxTemp}°F - turning heater OFF"
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
