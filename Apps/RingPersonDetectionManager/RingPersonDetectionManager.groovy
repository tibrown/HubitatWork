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
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>TIMING CONFIGURATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "generalResetDelay", "number", title: "Reset Delay (seconds)",
                defaultValue: 3, range: "1..60", required: false
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
    
    if (!state.hubHandledDeviceIds) {
        state.hubHandledDeviceIds = [] as Set
    }
    
    // Reset all RPD switches to off on startup
    rpdSwitches?.each { sw ->
        if (sw.currentValue("switch") == "on") {
            sw.off()
            logDebug "Reset ${sw.displayName} to off on init"
        }
    }
    notificationOnlySwitches?.each { sw ->
        if (sw.currentValue("switch") == "on") {
            sw.off()
            logDebug "Reset ${sw.displayName} to off on init"
        }
    }
    nightModeSoftSwitches?.each { sw ->
        if (sw.currentValue("switch") == "on") {
            sw.off()
            logDebug "Reset ${sw.displayName} to off on init"
        }
    }
    
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
    
    // Subscribe to hub variable set by the Android app
    subscribe(location, "variable:RingPersonDetected", handleRingPersonDetected)
    logDebug "Subscribed to hub variable RingPersonDetected"
    
    logInfo "Subscriptions complete"
}

// ==================== Hub Variable Handler ====================

def handleRingPersonDetected(evt) {
    def notificationText = evt.value
    logInfo "RingPersonDetected variable set: \"${notificationText}\""

    // Strip trailing timestamp (e.g., "[1717603200]") appended by Ring app to avoid duplicate-value suppression
    notificationText = notificationText.replaceAll(/\s*\[\d+\]\s*$/, '').trim()
    logDebug "After stripping timestamp: \"${notificationText}\""

    // Extract location from Ring message: "There is a person at your <location>"
    def searchText = notificationText
    def matcher = notificationText =~ /(?i)at your\s+(.+)/
    if (matcher) {
        searchText = matcher[0][1].trim()
        logDebug "Extracted location: \"${searchText}\""
    }

    def searchLower = searchText.toLowerCase().replaceAll(/\s+/, '')

    def matchedRpd = rpdSwitches?.find { sw ->
        def label = sw.label?.toLowerCase()?.replaceAll(/\s+/, '')
        label && label.contains(searchLower)
    }
    def matchedNightSoft = nightModeSoftSwitches?.find { sw ->
        def label = sw.label?.toLowerCase()?.replaceAll(/\s+/, '')
        label && label.contains(searchLower)
    }
    def matchedNotifOnly = notificationOnlySwitches?.find { sw ->
        def label = sw.label?.toLowerCase()?.replaceAll(/\s+/, '')
        label && label.contains(searchLower)
    }
    
    boolean actionTaken = false

    // 1. Check Night Mode
    if (matchedRpd && isNightMode()) {
        logInfo "Matched night mode switch \"${matchedRpd.label}\" — turning on"
        state.hubHandledDeviceIds.add(matchedRpd.id)
        matchedRpd.on()
        sendNotification(matchedRpd.label)
        if (allLightsSwitch) allLightsSwitch.on()
        actionTaken = true
    }

    // 2. Check Night Soft Mode (if Night Mode didn't trigger)
    if (!actionTaken && matchedNightSoft && isNightMode()) {
        logInfo "Matched night mode soft switch \"${matchedNightSoft.label}\" — turning on"
        state.hubHandledDeviceIds.add(matchedNightSoft.id)
        matchedNightSoft.on()
        sendSoftNotification(matchedNightSoft.label)
        actionTaken = true
    }

    // 3. Check Notification Only Mode (if neither Night Mode triggered)
    if (!actionTaken && matchedNotifOnly && isNotificationOnlyMode()) {
        logInfo "Matched notification only switch \"${matchedNotifOnly.label}\" — turning on"
        state.hubHandledDeviceIds.add(matchedNotifOnly.id)
        matchedNotifOnly.on()
        sendNotification(matchedNotifOnly.label)
        actionTaken = true
    }

    // 4. Catch-all for logging
    if (!actionTaken) {
        if (matchedRpd || matchedNightSoft || matchedNotifOnly) {
            logInfo "Device matched a list, but current mode (${location.mode}) does not allow activation — skipping."
        } else {
            logInfo "No configured switch matched for location: \"${searchText}\""
        }
    }
}

// ==================== RPD Switch Handler ====================

def handleRPD(evt) {
    def device = evt.device
    def message = device.label

    logInfo "Person detected: ${message}"
    runIn(generalResetDelay ?: 3, "resetRPDSwitch", [data: [deviceId: device.id]])
    if (state.hubHandledDeviceIds?.contains(device.id)) {
        logDebug "Hub variable handler already sent notification for ${message} — skipping duplicate"
        return
    }
    if (!shouldSendNotification(device.id)) return
    if (isNightMode()) {
        sendNotification(message)
        if (allLightsSwitch) allLightsSwitch.on()
    }
}

// ==================== Night Mode Soft Switch Handler ====================

def handleNightModeSoftRPD(evt) {
    def device = evt.device
    def message = device.label

    logInfo "Person detected (night mode soft): ${message}"
    runIn(generalResetDelay ?: 3, "resetRPDSwitch", [data: [deviceId: device.id]])
    if (state.hubHandledDeviceIds?.contains(device.id)) {
        logDebug "Hub variable handler already sent notification for ${message} — skipping duplicate"
        return
    }
    if (!shouldSendNotification(device.id)) return
    if (isNightMode()) {
        sendSoftNotification(message)
    }
}

// ==================== Notification Only Switch Handler ====================

def handleNotificationOnlyRPD(evt) {
    def device = evt.device
    def message = device.label

    logInfo "Person detected (notification only): ${message}"
    runIn(generalResetDelay ?: 3, "resetRPDSwitch", [data: [deviceId: device.id]])
    if (state.hubHandledDeviceIds?.contains(device.id)) {
        logDebug "Hub variable handler already sent notification for ${message} — skipping duplicate"
        return
    }
    if (!shouldSendNotification(device.id)) return
    if (isNotificationOnlyMode()) {
        sendNotification(message)
    }
}

// ==================== Reset Method ====================

def resetRPDSwitch(data) {
    def sw = rpdSwitches?.find { it.id == data.deviceId }
    if (!sw) sw = notificationOnlySwitches?.find { it.id == data.deviceId }
    if (!sw) sw = nightModeSoftSwitches?.find { it.id == data.deviceId }
    if (sw) {
        sw.off()
        state.hubHandledDeviceIds.remove(data.deviceId)
        state["notifCooldown_${data.deviceId}"] = now()
        logDebug "Reset ${sw.displayName} — cooldown started"
    }
}

// ==================== Helper Methods ====================

/**
 * Debounce check — returns false if notification should be skipped.
 * Cooldown period starts from the last reset (switch turned off).
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
 * Send notification to all configured devices
 */
def sendNotification(String message) {
    if (isSilent()) {
        logInfo "Silent switch is ON - suppressing notification: ${message}"
        return
    }
    
    logInfo "Sending notification: ${message}"
    
    if (notificationDevices) {
        notificationDevices.each { device ->
            device.deviceNotification(message)
        }
    }
    
}

/**
 * Send notification to Night Mode Soft notification devices
 */
def sendSoftNotification(String message) {
    if (isSilent()) {
        logInfo "Silent switch is ON - suppressing soft notification: ${message}"
        return
    }
    
    logInfo "Sending soft notification: ${message}"
    
    if (nightModeSoftNotificationDevices) {
        nightModeSoftNotificationDevices.each { device ->
            device.deviceNotification(message)
        }
    }
    
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