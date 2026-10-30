/**
 *  Ring Person Detection Manager
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
    name: "Ring Person Detection Manager",
    namespace: "timbrown",
    author: "Tim Brown",
    description: "Monitors RPD switches and takes mode-based actions for Ring person detection",
    category: "Security",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    singleThreaded: true
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Ring Person Detection Manager", install: true, uninstall: true) {
        section("<b>═══════════════════════════════════════</b>\n<b>NIGHT MODE SWITCHES / MODES</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Select Ring Person Detection virtual switches that will only send notifications during Night Modes. The alert message sent for each camera is the device's <b>Label</b> (set in the Device Info tab in Hubitat)."
            input "rpdSwitches", "capability.switch", title: "Night Mode Switches", multiple: true, required: false
            input "nightModes", "mode", title: "Night Modes", multiple: true, required: false,
                description: "Modes for enhanced night security actions (lights, EchoMessage, whisper)"
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>NIGHT MODE SOFT SWITCHES</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Select Ring Person Detection virtual switches that will send notifications to their own notification devices during Night Modes, without triggering lights."
            input "nightModeSoftSwitches", "capability.switch", title: "Night Mode Soft Switches", multiple: true, required: false
            input "nightModeSoftNotificationDevices", "capability.notification", title: "Night Mode Soft Notification Devices", multiple: true, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>NOTIFICATION ONLY SWITCHES / MODES</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Select additional Ring Person Detection virtual switches and the modes during which they will send notifications. Silent switches still suppress all notifications."
            input "notificationOnlySwitches", "capability.switch", title: "Notification Only Switches", multiple: true, required: false
            input "notificationOnlyModes", "mode", title: "Active Modes", multiple: true, required: false,
                description: "Modes during which these switches will send notifications"
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>NOTIFICATION DEVICES</b>\n<b>═══════════════════════════════════════</b>") {
            input "notificationDevices", "capability.notification", title: "Notification Devices", 
                multiple: true, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>CONTROL SWITCHES</b>\n<b>═══════════════════════════════════════</b>") {
            input "silentSwitch", "capability.switch", title: "Silent Mode Switch", required: false
            input "silenceOfficeSwitch", "capability.switch", title: "Silence Office Switch", required: false
            input "allLightsSwitch", "capability.switch", title: "All Lights ON Switch", required: false
            input "pauseBDAlarmSwitch", "capability.switch", title: "Pause Backdoor Alarm Switch", required: false,
                description: "While this switch is ON, person detections from the Backdoor RPD Switches below are suppressed (no notification, no lights). Ring's own announcement is not affected."
            input "backdoorRPDSwitches", "capability.switch", title: "Backdoor RPD Switches", multiple: true, required: false,
                description: "RPD switches that count as 'backdoor'. Suppressed entirely while the Pause Backdoor Alarm Switch is ON."
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>TIMING CONFIGURATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "notificationCooldownSeconds", "enum", title: "Notification Cooldown (seconds)",
                description: "Minimum time between repeated notifications for the same camera. Prevents duplicate alerts when Ring fires multiple events for one person.",
                options: ["5", "10", "15", "20", "30", "45", "60", "90"],
                defaultValue: "60", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>LOGGING</b>\n<b>═══════════════════════════════════════</b>") {
            input "logLevel", "enum", title: "Logging Level", 
                options: ["None", "Info", "Debug", "Trace"], 
                defaultValue: "Info", required: true
        }
    }
}

def installed() {
    logInfo "Ring Person Detection Manager installed"
    initialize()
}

def updated() {
    logInfo "Ring Person Detection Manager updated"
    unsubscribe()
    initialize()
}

def initialize() {
    logInfo "Initializing Ring Person Detection Manager"
    
    // Reset all RPD switches to off on startup (safety net in case one was
    // stuck "on" across a hub restart — the switches normally self-reset)
    def resetOnStartup = { sw ->
        if (sw.currentValue("switch") == "on") {
            sw.off()
            logDebug "Reset ${sw.displayName} to off on init"
        }
    }
    rpdSwitches?.each resetOnStartup
    notificationOnlySwitches?.each resetOnStartup
    nightModeSoftSwitches?.each resetOnStartup
    
    // Subscribe to each RPD switch - react when they turn on
    rpdSwitches?.each { sw ->
        subscribe(sw, "switch.on", handleRPD)
        logDebug "Subscribed to ${sw.displayName}"
    }
    notificationOnlySwitches?.each { sw ->
        subscribe(sw, "switch.on", handleNotificationOnlyRPD)
        logDebug "Subscribed to ${sw.displayName}"
    }
    nightModeSoftSwitches?.each { sw ->
        subscribe(sw, "switch.on", handleNightModeSoftRPD)
        logDebug "Subscribed to ${sw.displayName}"
    }
    
    logInfo "Subscriptions complete"
}

// ==================== RPD Switch Handler ====================

def handleRPD(evt) {
    def device = evt.device
    def message = device.label

    logInfo "Person detected: ${message}"
    if (!shouldSendNotification(device.id)) return
    if (isBackdoorPaused(device)) return
    if (isNightMode()) {
        if (sendNotification(message)) {
            state["notifCooldown_${device.id}"] = now()
        }
        if (allLightsSwitch) allLightsSwitch.on()
    }
}

// ==================== Night Mode Soft Switch Handler ====================

def handleNightModeSoftRPD(evt) {
    def device = evt.device
    def message = device.label

    logInfo "Person detected (night mode soft): ${message}"
    if (!shouldSendNotification(device.id)) return
    if (isBackdoorPaused(device)) return
    if (isNightMode()) {
        if (sendSoftNotification(message)) {
            state["notifCooldown_${device.id}"] = now()
        }
    }
}

// ==================== Notification Only Switch Handler ====================

def handleNotificationOnlyRPD(evt) {
    def device = evt.device
    def message = device.label

    logInfo "Person detected (notification only): ${message}"
    if (!shouldSendNotification(device.id)) return
    if (isNotificationOnlyMode()) {
        if (sendNotification(message)) {
            state["notifCooldown_${device.id}"] = now()
        }
    }
}

// ==================== Helper Methods ====================

/**
 * Debounce check — returns false if notification should be skipped.
 * Cooldown period starts from the last handled switch.on event.
 */
Boolean shouldSendNotification(String deviceId) {
    Integer cooldownSecs = (notificationCooldownSeconds ?: 60) as Integer
    Long cooldownMs = cooldownSecs * 1000L
    String cooldownKey = "notifCooldown_${deviceId}"

    if (state[cooldownKey]) {
        Long elapsed = now() - state[cooldownKey]
        if (elapsed < cooldownMs) {
            logDebug "Notification for device ${deviceId} in cooldown — ${elapsed}ms since reset (min ${cooldownMs}ms)"
            return false
        }
    }

    return true
}

/**
 * Send notification to all configured devices.
 * Returns true if a notification was actually delivered, false if suppressed.
 */
Boolean sendNotification(String message) {
    if (isSilent()) {
        logInfo "Silent switch is ON - suppressing notification: ${message}"
        return false
    }
    
    logInfo "Sending notification: ${message}"
    
    if (notificationDevices) {
        notificationDevices.each { device ->
            device.deviceNotification(message)
        }
    }
    return true
}

/**
 * Send notification to Night Mode Soft notification devices.
 * Returns true if a notification was actually delivered, false if suppressed.
 */
Boolean sendSoftNotification(String message) {
    if (isSilent()) {
        logInfo "Silent switch is ON - suppressing soft notification: ${message}"
        return false
    }
    
    logInfo "Sending soft notification: ${message}"
    
    if (nightModeSoftNotificationDevices) {
        nightModeSoftNotificationDevices.each { device ->
            device.deviceNotification(message)
        }
    }
    return true
}

def isNightMode() {
    if (!nightModes) return false
    return location.mode in nightModes
}

def isNotificationOnlyMode() {
    if (!notificationOnlyModes) return false
    return location.mode in notificationOnlyModes
}

def isSilent() {
    if (silentSwitch?.currentValue("switch") == "on") return true
    if (silenceOfficeSwitch?.currentValue("switch") == "on") return true
    return false
}

/**
 * True when the backdoor alarm is paused (PauseBDAlarm switch ON) AND the
 * detected device is one of the configured Backdoor RPD Switches. Suppresses
 * the notification + all-lights path for backdoor detections while someone is
 * intentionally coming/going through the back door (wife scenario). Ring's own
 * cloud-side announcement still fires — only Hubitat's extra warnings/lights
 * are skipped.
 */
Boolean isBackdoorPaused(def device) {
    if (!pauseBDAlarmSwitch || !backdoorRPDSwitches) return false
    Boolean paused = pauseBDAlarmSwitch.currentValue("switch")?.toLowerCase() == "on"
    if (!paused) return false
    Boolean isBackdoor = backdoorRPDSwitches.any { it.id == device.id }
    if (isBackdoor) {
        logInfo "PauseBDAlarm ON + backdoor detection — suppressing warning & lights: ${device.label}"
    }
    return isBackdoor
}

// ==================== Logging Methods ====================

def logInfo(String msg) {
    if (logLevel in ["Info", "Debug", "Trace"]) {
        log.info msg
    }
}

def logDebug(String msg) {
    if (logLevel in ["Debug", "Trace"]) {
        log.debug msg
    }
}

def logTrace(String msg) {
    if (logLevel == "Trace") {
        log.trace msg
    }
}