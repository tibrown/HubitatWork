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
            def threshold = settings.freezeThreshold ?: 32.0
            if (currentTemp <= threshold && !state.heatLampCycling) {
                logInfo "Initialization: Temperature ${currentTemp}°F <= ${threshold}°F and heat lamp enabled - starting cycling"
                startHeatLampCycling()
            }
        }
    }
    
    logInfo "Monitoring for temperatures at or below ${settings.freezeThreshold ?: 32.0}°F"
}

def tempHandler(evt) {
    def temp = evt.value as BigDecimal
    def threshold = settings.freezeThreshold ?: 32.0
    
    logDebug "Temperature event: ${temp}°F (threshold: ${threshold}°F)"
    
    if (temp <= threshold) {
        logWarn "Temperature ${temp}°F at or below freeze threshold ${threshold}°F"
        sendFreezeAlert(temp)
        
        // Check if heat lamp should start cycling
        if (settings.heatLampEnabled && settings.heatLampSwitch) {
            if (settings.heatLampEnabled.currentValue("switch") == "on" && !state.heatLampCycling) {
                logInfo "Temperature dropped to ${temp}°F - starting heat lamp cycling"
                startHeatLampCycling()
            }
        }
    } else {
        // Temperature above threshold - stop heat lamp cycling if active
        if (state.heatLampCycling) {
            logInfo "Temperature rose to ${temp}°F (above ${threshold}°F) - stopping heat lamp cycling"
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
        def threshold = settings.freezeThreshold ?: 32.0
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
