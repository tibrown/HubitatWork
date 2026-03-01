/**
 *  Security Alarm Manager
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
    name: "Security Alarm Manager",
    namespace: "hubitat",
    author: "Tim Brown",
    description: "Centralized security alarm, siren, and alert management system. Handles alarm arming/disarming, multiple siren sound patterns, button controls, and cross-app alarm triggering.",
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
    dynamicPage(name: "mainPage", title: "Security Alarm Manager", install: true, uninstall: true) {
        section("<b>═══════════════════════════════════════</b>\n<b>SIREN DEVICES</b>\n<b>═══════════════════════════════════════</b>") {
            input "siren1", "capability.alarm", title: "Siren 1 (Office)", required: true
            input "siren2", "capability.alarm", title: "Siren 2", required: true
            input "siren3", "capability.alarm", title: "Siren 3 (Chime)", required: true
            input "alarmPlug", "capability.switch", title: "Alarm Plug Switch", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>CONTROL SWITCHES</b>\n<b>═══════════════════════════════════════</b>") {
            input "alarmsEnabledSwitch", "capability.switch", title: "Alarms Enabled Switch", required: true
            input "audibleAlarmsSwitch", "capability.switch", title: "Audible Alarms On Switch", required: true
            input "silentSwitch", "capability.switch", title: "Silent Mode Switch", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>CONTROL BUTTONS</b>\n<b>═══════════════════════════════════════</b>") {
            input "alarmOffButton", "capability.pushableButton", title: "Alarm Off Button", required: false
            input "alarmOffBackup", "capability.pushableButton", title: "Alarm Off Backup Button", required: false
            input "alarmOnButton", "capability.pushableButton", title: "Alarm On Button", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>CROSS-APP COMMUNICATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "alarmTriggerSwitch", "capability.switch", title: "Alarm Trigger Switch (receives from other apps)", required: false
            input "alarmStopSwitch", "capability.switch", title: "Alarm Stop Switch (receives from other apps)", required: false
            input "panicButtonSwitch", "capability.switch", title: "Panic Button Switch (receives from other apps)", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>CAMERA INTEGRATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "indoorCamsSwitch", "capability.switch", title: "Indoor Cameras Switch", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>ALARM CONFIGURATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "hubVar_AlarmVolume", "number", title: "Alarm Volume", description: "Siren volume level (0-100). Sets AlarmVolume hub variable.", range: "0..100", defaultValue: 80, required: false
            input "hubVar_AlarmDuration", "number", title: "Alarm Duration", description: "How long alarms sound before auto-stopping (seconds). Sets AlarmDuration hub variable.", defaultValue: 300, required: false
            input "hubVar_ArmDelay", "number", title: "Arm Delay", description: "Delay before arming alarms (seconds). Sets ArmDelay hub variable.", defaultValue: 0, required: false
            input "hubVar_DisarmDelay", "number", title: "Disarm Delay", description: "Delay before disarming alarms (seconds). Sets DisarmDelay hub variable.", defaultValue: 0, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>MODE CONFIGURATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "alarmEnabledModes", "mode", title: "Modes that enable alarms", multiple: true, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>NOTIFICATIONS</b>\n<b>═══════════════════════════════════════</b>") {
            input "notificationDevices", "capability.notification", title: "Notification Devices", multiple: true, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>HUB VARIABLES</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Configuration values above are stored as hub variables for cross-app sharing:"
            paragraph "• AlarmVolume, AlarmDuration, ArmDelay, DisarmDelay - Configuration variables"
            paragraph "• AlarmActive - Status variable (written by this app, read by others)"
            paragraph "• AlarmsEnabled - Status variable (written by this app, read by others)"
            paragraph "Hub variables are automatically synced when this app is updated."
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>LOGGING</b>\n<b>═══════════════════════════════════════</b>") {
            input "logEnable", "bool", title: "Enable debug logging", defaultValue: true
            input "infoEnable", "bool", title: "Enable info logging", defaultValue: true
        }
    }
}

def installed() {
    logInfo "Security Alarm Manager installed"
    initialize()
}

def updated() {
    logInfo "Security Alarm Manager updated"
    unsubscribe()
    initialize()
    syncHubVariables()
}

def initialize() {
    logInfo "Initializing Security Alarm Manager"
    
    // Subscribe to mode changes
    subscribe(location, "mode", handleModeChange)
    
    // Subscribe to control switches
    if (alarmsEnabledSwitch) subscribe(alarmsEnabledSwitch, "switch", handleAlarmsEnabledSwitch)
    
    // Subscribe to buttons
    if (alarmOffButton) subscribe(alarmOffButton, "pushed", handleAlarmOffButton)
    if (alarmOffBackup) subscribe(alarmOffBackup, "pushed", handleAlarmOffButton)
    if (alarmOnButton) subscribe(alarmOnButton, "pushed", handleAlarmOnButton)
    
    // Subscribe to cross-app communication switches
    if (alarmTriggerSwitch) subscribe(alarmTriggerSwitch, "switch.on", handleAlarmTrigger)
    if (alarmStopSwitch) subscribe(alarmStopSwitch, "switch.on", handleAlarmStop)
    if (panicButtonSwitch) subscribe(panicButtonSwitch, "switch.on", handlePanicButton)
    
    // Set initial hub variable states
    setHubVar("AlarmsEnabled", alarmsEnabledSwitch?.currentValue("switch") == "on" ? "true" : "false")
    setHubVar("AlarmActive", "false")
}

// ========================================
// MODE CHANGE HANDLERS
// ========================================

def handleModeChange(evt) {
    logDebug "Mode changed to: ${evt.value}"
    
    if (alarmEnabledModes && alarmEnabledModes.contains(evt.value)) {
        logInfo "Mode ${evt.value} enables alarms"
        armAlarms()
    } else if (alarmEnabledModes) {
        logInfo "Mode ${evt.value} disables alarms"
        disarmAlarms()
    }
}

// ========================================
// SWITCH HANDLERS
// ========================================

def handleAlarmsEnabledSwitch(evt) {
    logDebug "AlarmsEnabled switch changed to: ${evt.value}"
    
    if (evt.value == "off") {
        stopAllAlarms()
    }
    
    setHubVar("AlarmsEnabled", evt.value == "on" ? "true" : "false")
}

// ========================================
// BUTTON HANDLERS
// ========================================

def handleAlarmOffButton(evt) {
    logInfo "Alarm Off button pressed"
    disarmAlarms()
    stopAllAlarms()
}

def handleAlarmOnButton(evt) {
    logInfo "Alarm On button pressed"
    armAlarms()
}

// ========================================
// CROSS-APP COMMUNICATION HANDLERS
// ========================================

def handleAlarmTrigger(evt) {
    logInfo "Alarm trigger received from ${evt.displayName}"
    executeAlarms()
    
    // Auto-reset the trigger switch
    runIn(2, resetTriggerSwitch)
}

def handleAlarmStop(evt) {
    logInfo "Alarm stop received from ${evt.displayName}"
    stopAllAlarms()
    
    // Auto-reset the stop switch
    runIn(2, resetStopSwitch)
}

def handlePanicButton(evt) {
    logInfo "PANIC BUTTON activated from ${evt.displayName}"
    playPanicAlert()
    
    // Auto-reset the panic switch
    runIn(2, resetPanicSwitch)
}

def resetTriggerSwitch() {
    alarmTriggerSwitch?.off()
}

def resetStopSwitch() {
    alarmStopSwitch?.off()
}

def resetPanicSwitch() {
    panicButtonSwitch?.off()
}

// ========================================
// ALARM CONTROL METHODS
// ========================================

def armAlarms() {
    logInfo "Arming alarms"
    
    Integer delay = getConfigValue("armDelay", "ArmDelay") as Integer
    
    if (delay > 0) {
        logDebug "Arm delay: ${delay} seconds"
        runIn(delay, doArmAlarms)
    } else {
        doArmAlarms()
    }
}

def doArmAlarms() {
    alarmsEnabledSwitch?.on()
    setHubVar("AlarmsEnabled", "true")
    
    // Play armed chime
    playSound(6, [siren2])
    
    // Notify via Alexa or notification device
    String message = "Alarms are armed"
    sendNotification(message)
    
    logInfo message
}

def disarmAlarms() {
    logInfo "Disarming alarms"
    
    Integer delay = getConfigValue("disarmDelay", "DisarmDelay") as Integer
    
    if (delay > 0) {
        logDebug "Disarm delay: ${delay} seconds"
        runIn(delay, doDisarmAlarms)
    } else {
        doDisarmAlarms()
    }
}

def doDisarmAlarms() {
    alarmsEnabledSwitch?.off()
    setHubVar("AlarmsEnabled", "false")
    
    // Play disarmed chime
    playSound(5, [siren1, siren2])
    
    logInfo "Alarms disarmed"
}

def executeAlarms() {
    logInfo "Executing alarms"
    
    // Check if alarms are enabled
    if (alarmsEnabledSwitch?.currentValue("switch") != "on") {
        logDebug "Alarms are disabled, skipping execution"
        return
    }
    
    // Set alarm active status
    setHubVar("AlarmActive", "true")
    
    // Turn on indoor cameras
    indoorCamsSwitch?.on()
    
    // Check if audible alarms are enabled
    if (audibleAlarmsSwitch?.currentValue("switch") == "on") {
        // Play alarm sound
        playSound(8, [siren1, siren2])
        
        // Activate full siren with strobe
        siren1?.both()
        siren2?.both()
        siren3?.both()
        alarmPlug?.on()
        
        logInfo "Full alarm activated (sound + strobe)"
        
        // Auto-stop after configured duration
        Integer duration = getConfigValue("alarmDuration", "AlarmDuration") as Integer
        if (duration > 0) {
            logDebug "Alarm will auto-stop in ${duration} seconds"
            runIn(duration, autoStopAlarms)
        }
    } else {
        logInfo "Audible alarms disabled (silent mode)"
    }
}

def autoStopAlarms() {
    logInfo "Auto-stopping alarms after timeout"
    stopAllAlarms()
}

def stopAllAlarms() {
    logInfo "Stopping all alarms"
    
    // Cancel any pending auto-stop
    unschedule(autoStopAlarms)
    
    // Turn off all sirens
    siren1?.off()
    siren2?.off()
    siren3?.off()
    alarmPlug?.off()
    
    // Set alarm inactive status
    setHubVar("AlarmActive", "false")
    
    logInfo "All alarms stopped"
}

// ========================================
// SOUND PLAYBACK METHODS
// ========================================

def playSound(Integer soundNumber, List devices) {
    logDebug "Playing sound ${soundNumber} on ${devices.size()} device(s)"
    
    Integer volume = getConfigValue("alarmVolume", "AlarmVolume") as Integer
    
    devices.each { device ->
        device?.playSound(soundNumber, volume)
    }
}

def playBearAlert() {
    logInfo "Playing bear alert"
    playSound(33, [siren1, siren2, siren3])
}

def playDogBark() {
    logInfo "Playing dog bark (repeating)"
    
    // Repeat 4 times every 1 second
    for (int i = 0; i < 4; i++) {
        runIn(i, "playDogBarkSound")
    }
}

def playDogBarkSound() {
    playSound(12, [siren1, siren2, siren3])
}

def playDogsBarking() {
    logInfo "Playing dogs barking"
    playSound(10, [siren1, siren2, siren3])
}

def playDoorbell() {
    logInfo "Playing doorbell"
    playSound(34, [siren1, siren2])
}

def playDoorbellAlternate() {
    logInfo "Playing alternate doorbell"
    playSound(25, [siren3])
    runIn(3, "playDoorbellAlternateSecond")
}

def playDoorbellAlternateSecond() {
    playSound(25, [siren3])
}

def playPanicAlert() {
    logInfo "Playing PANIC alert"
    playSound(21, [siren1, siren2])
}

def playSiren() {
    logInfo "Playing siren sound"
    playSound(32, [siren1, siren2])
}

def playTrainHorn() {
    logInfo "Playing train horn"
    playSound(8, [siren1, siren2, siren3])
}

def playTuningBell() {
    logInfo "Playing tuning bell"
    playSound(8, [siren1, siren2])
}

// ========================================
// HELPER METHODS
// ========================================

def syncHubVariables() {
    setHubVar("AlarmVolume", (hubVar_AlarmVolume ?: 80).toString())
    setHubVar("AlarmDuration", (hubVar_AlarmDuration ?: 300).toString())
    setHubVar("ArmDelay", (hubVar_ArmDelay ?: 0).toString())
    setHubVar("DisarmDelay", (hubVar_DisarmDelay ?: 0).toString())
    logInfo "Hub variables synced from app settings"
}

def getConfigValue(String settingName, String hubVarName) {
    // Get value from hub variable
    def hubVarValue = getHubVar(hubVarName)
    if (hubVarValue != null) {
        logDebug "Using hub variable '${hubVarName}' = ${hubVarValue}"
        return hubVarValue
    }
    
    logDebug "Hub variable '${hubVarName}' not set"
    return null
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
