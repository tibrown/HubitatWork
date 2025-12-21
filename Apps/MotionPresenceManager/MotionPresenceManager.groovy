/**
 *  Motion Presence Manager
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
    name: "Motion Presence Manager",
    namespace: "hubitat",
    author: "Tim Brown",
    description: "Comprehensive motion detection and presence management. Handles zone-based motion sensors, phone presence detection, arrival grace periods, and time-based motion responses. Consolidates motion detection and ArriveGraceTurnsOn functionality.",
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
    dynamicPage(name: "mainPage", title: "Motion Presence Manager", install: true, uninstall: true) {
        section("<b>═══════════════════════════════════════</b>\n<b>MOTION SENSORS BY ZONE</b>\n<b>═══════════════════════════════════════</b>") {
            input "carportMotion", "capability.motionSensor", title: "Carport Motion Sensor", required: false
            input "backDoorMotion", "capability.motionSensor", title: "Back Door Motion Sensor", required: false
            input "sideYardMotion", "capability.motionSensor", title: "Side Yard Motion (AMC)", required: false
            input "rvMotion", "capability.motionSensor", title: "RV Motion Sensor", required: false
            input "officeMotion", "capability.motionSensor", title: "Office Motion Sensor", required: false
            input "rearCarportMotion", "capability.motionSensor", title: "Rear Carport Motion Sensor", required: false
            input "carportFrontMotion", "capability.motionSensor", title: "Carport Front Motion Sensor", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>PRESENCE SENSORS</b>\n<b>═══════════════════════════════════════</b>") {
            input "phonePresence", "capability.presenceSensor", title: "Phone Presence Sensor", required: false
            input "marjiPhone", "capability.presenceSensor", title: "Marji's Phone Presence", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>ARRIVAL GRACE PERIOD</b>\n<b>═══════════════════════════════════════</b>") {
            input "arriveGracePeriodSwitch", "capability.switch", title: "Arrive Grace Period Switch", required: false
            input "graceDuration", "number", title: "Grace Period Duration (minutes)", defaultValue: 30, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>CONTROL SWITCHES</b>\n<b>═══════════════════════════════════════</b>") {
            input "alarmsEnabled", "capability.switch", title: "Alarms Enabled Switch", required: false
            input "silentMode", "capability.switch", title: "Silent Mode Switch", required: false
            input "silentBackdoorSwitch", "capability.switch", title: "Silent Backdoor Switch", required: false
            input "rearCarportActive", "capability.switch", title: "Rear Carport Active Switch", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>NOTIFICATIONS</b>\n<b>═══════════════════════════════════════</b>") {
            input "notificationDevices", "capability.notification", title: "General Notification Devices", multiple: true, required: false
            input "backDoorNotificationDevices", "capability.notification", title: "Back Door Notification Devices", multiple: true, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>MOTION CONFIGURATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "motionTimeout", "number", title: "Motion Detection Timeout (seconds)", defaultValue: 60, required: false
            input "enableDayMotion", "bool", title: "Enable Day Mode Motion Detection", defaultValue: true
            input "enableNightMotion", "bool", title: "Enable Night Mode Motion Detection", defaultValue: false
            input "generalMotionModes", "enum", title: "General Motion Active Modes", options: ["Day", "Morning", "Evening", "Night"], multiple: true, required: false
            input "backDoorMotionModes", "enum", title: "Back Door Motion Active Modes", options: ["Day", "Morning", "Evening", "Night"], multiple: true, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>PRESENCE CONFIGURATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "arrivalNotification", "bool", title: "Send Arrival Notifications", defaultValue: true
            input "departureNotification", "bool", title: "Send Departure Notifications", defaultValue: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>HUB VARIABLE OVERRIDES</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "This app supports hub variable overrides for flexible configuration:"
            paragraph "• GracePeriodDuration - Override grace period duration (minutes)"
            paragraph "• MotionTimeout - Override motion sensor timeout (seconds)"
            paragraph "• EnableDayMotion - Enable/disable day motion detection (true/false)"
            paragraph "• EnableNightMotion - Enable/disable night motion detection (true/false)"
            paragraph "• ArrivalNotifications - Enable/disable arrival notifications (true/false)"
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>LOGGING</b>\n<b>═══════════════════════════════════════</b>") {
            input "logEnable", "bool", title: "Enable debug logging", defaultValue: true
            input "infoEnable", "bool", title: "Enable info logging", defaultValue: true
        }
    }
}

def installed() {
    logInfo "Motion Presence Manager installed"
    initialize()
}

def updated() {
    logInfo "Motion Presence Manager updated"
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    logInfo "Initializing Motion Presence Manager"
    
    // Subscribe to motion sensors
    if (carportMotion) subscribe(carportMotion, "motion.active", handleCarportMotion)
    if (backDoorMotion) subscribe(backDoorMotion, "motion.active", handleBackDoorMotion)
    if (sideYardMotion) subscribe(sideYardMotion, "motion.active", handleSideYardMotion)
    if (rvMotion) subscribe(rvMotion, "motion.active", handleRVMotion)
    if (officeMotion) subscribe(officeMotion, "motion.active", handleOfficeMotion)
    if (rearCarportMotion) subscribe(rearCarportMotion, "motion.active", handleRearCarportMotion)
    if (carportFrontMotion) subscribe(carportFrontMotion, "motion.active", handleCarportFrontMotion)
    
    // Subscribe to presence sensors
    if (phonePresence) {
        subscribe(phonePresence, "presence.present", handlePhoneArrival)
        subscribe(phonePresence, "presence.not present", handlePhoneDeparture)
    }
    if (marjiPhone) {
        subscribe(marjiPhone, "presence.present", handleMarjiArrival)
        subscribe(marjiPhone, "presence.not present", handleMarjiDeparture)
    }
    
    // Subscribe to grace period switch
    if (arriveGracePeriodSwitch) {
        subscribe(arriveGracePeriodSwitch, "switch.on", handleGracePeriodActivation)
    }
}

// ========================================
// MOTION DETECTION HANDLERS
// ========================================

def handleCarportMotion(evt) {
    String mode = location.mode
    logInfo "Carport motion detected in ${mode} mode"
    
    if (!isGeneralMotionActiveInMode(mode)) {
        logDebug "Motion detection disabled for current mode"
        return
    }
    
    sendNotification("Motion detected in carport")
}

def handleBackDoorMotion(evt) {
    String mode = location.mode
    logInfo "Back door motion detected in ${mode} mode"
    
    // Check if silent backdoor switch is on
    if (silentBackdoorSwitch?.currentValue("switch") == "on") {
        logDebug "Back door motion ignored - silent backdoor switch is on"
        return
    }
    
    // Check if current mode is in backdoor motion modes list
    if (!isBackDoorMotionActiveInMode(mode)) {
        logDebug "Back door motion ignored - mode ${mode} not in active modes list"
        return
    }
    
    sendBackDoorNotification("Motion detected at back door")
}

def handleSideYardMotion(evt) {
    String mode = location.mode
    logInfo "Side yard (AMC) motion detected in ${mode} mode"
    
    if (!isGeneralMotionActiveInMode(mode)) {
        logDebug "Motion detection disabled for current mode"
        return
    }
    
    sendNotification("Motion detected in side yard")
}

def handleRVMotion(evt) {
    String mode = location.mode
    logInfo "RV motion detected in ${mode} mode"
    
    if (!isGeneralMotionActiveInMode(mode)) {
        logDebug "Motion detection disabled for current mode"
        return
    }
    
    sendNotification("Motion detected in RV")
}

def handleOfficeMotion(evt) {
    String mode = location.mode
    logInfo "Office motion detected in ${mode} mode"
    
    if (!isGeneralMotionActiveInMode(mode)) {
        logDebug "Motion detection disabled for current mode"
        return
    }
    
    // Office motion is primarily for desk light control (handled by LightsAutomationManager)
    logDebug "Office motion event logged"
}

def handleRearCarportMotion(evt) {
    String mode = location.mode
    logInfo "Rear carport motion detected in ${mode} mode"
    
    if (!isGeneralMotionActiveInMode(mode)) {
        logDebug "Motion detection disabled for current mode"
        return
    }
    
    // Activate rear carport active switch
    rearCarportActive?.on()
    
    sendNotification("Motion detected in rear carport")
    
    // Auto-reset after timeout
    Integer timeout = getConfigValue("motionTimeout", "MotionTimeout") as Integer
    runIn(timeout, resetRearCarportActive)
}

def handleCarportFrontMotion(evt) {
    String mode = location.mode
    logInfo "Carport front motion detected in ${mode} mode"
    
    if (!isGeneralMotionActiveInMode(mode)) {
        logDebug "Motion detection disabled for current mode"
        return
    }
    
    logDebug "Carport front motion event logged"
}

def resetRearCarportActive() {
    logDebug "Resetting rear carport active switch"
    rearCarportActive?.off()
}

def shouldProcessMotion(String mode) {
    Boolean dayMotionEnabled = getConfigValue("enableDayMotion", "EnableDayMotion") as Boolean
    Boolean nightMotionEnabled = getConfigValue("enableNightMotion", "EnableNightMotion") as Boolean
    
    if (mode == "Day" || mode == "Morning" || mode == "Evening") {
        return dayMotionEnabled
    } else if (mode == "Night") {
        return nightMotionEnabled
    }
    
    return true
}

def isGeneralMotionActiveInMode(String mode) {
    // If generalMotionModes is configured, use it
    if (generalMotionModes) {
        return generalMotionModes.contains(mode)
    }
    
    // Fall back to legacy shouldProcessMotion logic
    return shouldProcessMotion(mode)
}

def isBackDoorMotionActiveInMode(String mode) {
    // If backDoorMotionModes is configured, use it
    if (backDoorMotionModes) {
        return backDoorMotionModes.contains(mode)
    }
    
    // Fall back to general motion modes or legacy logic
    return isGeneralMotionActiveInMode(mode)
}

// ========================================
// PRESENCE DETECTION HANDLERS
// ========================================

def handlePhoneArrival(evt) {
    String mode = location.mode
    logInfo "Phone arrived in ${mode} mode"
    
    Boolean notifyArrival = getConfigValue("arrivalNotification", "ArrivalNotifications") as Boolean
    
    if (notifyArrival) {
        if (mode == "Day") {
            sendNotification("Phone has arrived (Day)")
        } else if (mode == "Night" || mode == "Evening" || mode == "Morning") {
            sendNotification("Phone has arrived (Late)")
        }
    }
    
    // Activate grace period if configured
    if (arriveGracePeriodSwitch) {
        logInfo "Activating arrival grace period"
        arriveGracePeriodSwitch.on()
    }
}

def handlePhoneDeparture(evt) {
    logInfo "Phone departed"
    
    Boolean notifyDeparture = getConfigValue("departureNotification", "ArrivalNotifications") as Boolean
    
    if (notifyDeparture) {
        sendNotification("Phone has left")
    }
}

def handleMarjiArrival(evt) {
    logInfo "Marji's phone arrived"
    
    Boolean notifyArrival = getConfigValue("arrivalNotification", "ArrivalNotifications") as Boolean
    
    if (notifyArrival) {
        sendNotification("Marji is home")
    }
}

def handleMarjiDeparture(evt) {
    logInfo "Marji's phone departed"
    
    Boolean notifyDeparture = getConfigValue("departureNotification", "ArrivalNotifications") as Boolean
    
    if (notifyDeparture) {
        sendNotification("Marji has left")
    }
}

// ========================================
// ARRIVAL GRACE PERIOD HANDLER
// (Absorbed from ArriveGraceTurnsOn app)
// ========================================

def handleGracePeriodActivation(evt) {
    Integer duration = getConfigValue("graceDuration", "GracePeriodDuration") as Integer
    
    logInfo "Arrival grace period activated for ${duration} minutes"
    
    // 1. Disable alarms
    if (alarmsEnabled) {
        alarmsEnabled.off()
        logDebug "Alarms disabled for grace period"
    }
    
    // 2. Enable silent mode
    if (silentMode) {
        silentMode.on()
        logDebug "Silent mode enabled for grace period"
    }
    
    // 3. Schedule grace period end
    Integer durationSeconds = duration * 60
    runIn(durationSeconds, endGracePeriod)
    
    sendNotification("Arrival grace period started (${duration} minutes)")
}

def endGracePeriod() {
    logInfo "Grace period ended - restoring normal settings"
    
    // 1. Re-enable alarms
    if (alarmsEnabled) {
        alarmsEnabled.on()
        logDebug "Alarms re-enabled"
    }
    
    // 2. Disable silent mode
    if (silentMode) {
        silentMode.off()
        logDebug "Silent mode disabled"
    }
    
    // 3. Turn off grace period switch
    if (arriveGracePeriodSwitch) {
        arriveGracePeriodSwitch.off()
        logDebug "Grace period switch reset"
    }
    
    sendNotification("Arrival grace period ended - alarms restored")
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

def sendNotification(String message) {
    if (notificationDevices) {
        notificationDevices.each { device ->
            device.deviceNotification(message)
        }
        logInfo "Notification: ${message}"
    }
}

def sendBackDoorNotification(String message) {
    if (backDoorNotificationDevices) {
        backDoorNotificationDevices.each { device ->
            device.deviceNotification(message)
        }
        logInfo "Back Door Notification: ${message}"
    } else if (notificationDevices) {
        // Fall back to general notification devices if backdoor devices not configured
        notificationDevices.each { device ->
            device.deviceNotification(message)
        }
        logInfo "Notification (fallback): ${message}"
    }
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
