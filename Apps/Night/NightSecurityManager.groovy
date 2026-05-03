/**
 *  Night Security Manager
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
    name: "Night Security Manager",
    namespace: "timbrown",
    author: "Tim Brown",
    description: "Comprehensive nighttime security monitoring and intruder detection system. Handles door/window sensors, motion detection, Ring person detection, and coordinates with alarm and lighting systems.",
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
    dynamicPage(name: "mainPage", title: "Night Security Manager", install: true, uninstall: true) {
        section("<b>═══════════════════════════════════════</b>\n<b>SENSORS</b>\n<b>═══════════════════════════════════════</b>") {
            input "standardIntruderSensors", "capability.contactSensor", title: "Standard Intruder Sensors (e.g. French doors, front door)", multiple: true, required: false
            input "shedSensors", "capability.contactSensor", title: "Shed Sensors (e.g. concrete shed, woodshed, she shed)", multiple: true, required: false
            input "standardRpdSwitches", "capability.switch", title: "Standard RPD Switches (e.g. front door, garden, back door)", multiple: true, required: false
            input "doorBHScreen", "capability.contactSensor", title: "BH Screen Door", required: true
            input "carportBeam", "capability.contactSensor", title: "Carport Beam (closed = beam broken)", required: true
            input "carportFrontMotion", "capability.motionSensor", title: "Carport Front Motion (verification)", required: true
            input "doorDiningRoom", "capability.contactSensor", title: "Dining Room Door", required: true
            input "rpdBirdHouse", "capability.switch", title: "RPD Bird House (Switch)", required: true
            input "chickenPenOutside", "capability.motionSensor", title: "Chicken Pen Outside Motion (temperature only)", required: false
            input "outsideBackdoor", "capability.motionSensor", title: "Outside Backdoor Motion", required: true
            input "floodSide", "capability.motionSensor", title: "Flood Side Motion", required: true
            input "doorLanai", "capability.contactSensor", title: "Lanai Door (Backdoor)", required: true
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>SWITCHES & CONTROLS</b>\n<b>═══════════════════════════════════════</b>") {
            input "traveling", "capability.switch", title: "Traveling Switch", required: true
            input "silent", "capability.switch", title: "Silent Switch", required: true
            input "silenceOffice", "capability.switch", title: "Silence Office Switch", required: false
            input "highAlert", "capability.switch", title: "High Alert Switch", required: true
            input "alarmsEnabled", "capability.switch", title: "Alarms Enabled Switch", required: true
            input "pauseDRDoorAlarm", "capability.switch", title: "Pause DR Door Alarm", required: true
            input "pauseBDAlarm", "capability.switch", title: "Pause Backdoor Alarm", required: true
            input "rearGateActive", "capability.switch", title: "Rear Gate Active Switch", required: true
            input "allLightsOn", "capability.switch", title: "All Lights ON Switch", required: true
            input "ringModeOnOff", "capability.switch", title: "Ring Mode On/Off", required: true
        }

        section("<b>═══════════════════════════════════════</b>\n<b>NOTIFICATIONS</b>\n<b>═══════════════════════════════════════</b>") {
            input "notificationDevices", "capability.notification", title: "Notification Devices", multiple: true, required: true
        }

        section("<b>═══════════════════════════════════════</b>\n<b>ACTIONS / OUTPUTS</b>\n<b>═══════════════════════════════════════</b>") {
            input "sirens", "capability.alarm", title: "Sirens", multiple: true, required: true
            input "allLights", "capability.switch", title: "All Lights", multiple: true, required: true
            input "guestRoomEcho", "capability.notification", title: "Guest Room Echo", required: true
        }

        section("<b>═══════════════════════════════════════</b>\n<b>RESTRICTIONS</b>\n<b>═══════════════════════════════════════</b>") {
            input "restrictedModes", "mode", title: "Only run in these modes", multiple: true, required: true
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>CROSS-APP COMMUNICATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "alarmTriggerSwitch", "capability.switch", title: "Alarm Trigger Switch (sends to SecurityAlarmManager)", required: false
            input "alarmStopSwitch", "capability.switch", title: "Alarm Stop Switch (sends to SecurityAlarmManager)", required: false
            input "emergencyLightsSwitch", "capability.switch", title: "Emergency Lights Switch (sends to LightsAutomationManager)", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>ALERT CONFIGURATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "hubVar_AlertDelay", "number", title: "Alert Delay", description: "Wait this long before triggering security alert (seconds). Sets AlertDelay hub variable.", defaultValue: 5, required: false
            input "hubVar_AlarmDuration", "number", title: "Alarm Duration", description: "How long security alarm sounds (seconds). Sets AlarmDuration hub variable.", defaultValue: 300, required: false
            input "hubVar_BeamLogEnabled", "bool", title: "Beam Logging Enabled", description: "Enable detailed logging of beam sensor activity. Sets BeamLogEnabled hub variable.", defaultValue: true, required: false
            input "motionDebounceSeconds", "number", title: "Minimum Motion Hold Time (seconds)",
                description: "Minimum time a motion state must be held before acting. Filters rapid/noisy sensor transitions caused by low battery or interference.",
                defaultValue: 3, range: "1..30", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>HUB VARIABLES</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Configuration values above are stored as hub variables for cross-app sharing:"
            paragraph "• AlertDelay - Security alert delay"
            paragraph "• AlarmDuration - Alarm sound duration"
            paragraph "• BeamLogEnabled - Beam sensor logging"
            paragraph "• AlarmsEnabled - Alarm status from SecurityAlarmManager (read-only)"
            paragraph "• AlarmActive - Current alarm state (read-only)"
            paragraph "Hub variables are automatically synced when this app is updated."
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>LOGGING</b>\n<b>═══════════════════════════════════════</b>") {
            input "logLevel", "enum", title: "Log Level", options: ["None","Info","Debug","Trace"], defaultValue: "Info"
        }
    }
}

def installed() {
    logInfo "Night Security Manager installed"
    initialize()
}

def updated() {
    logInfo "Night Security Manager updated"
    unsubscribe()
    unschedule()
    initialize()
    syncHubVariables()
}

def initialize() {
    logInfo "Initializing Night Security Manager"
    standardIntruderSensors?.each { subscribe(it, "contact.open", handleStandardIntruder) }
    shedSensors?.each { subscribe(it, "contact.open", handleShedIntruder) }
    standardRpdSwitches?.each { subscribe(it, "switch.on", handleStandardRPD) }
    subscribe(doorBHScreen, "contact.open", handleBHScreen)
    subscribe(carportBeam, "contact", handleCarportBeam)
    subscribe(doorDiningRoom, "contact.open", handleDiningRoomDoor)
    subscribe(rpdBirdHouse, "switch.on", handleRPDBirdHouse)
    subscribe(outsideBackdoor, "motion", handleBackdoorMotion)
    subscribe(doorLanai, "contact.open", handleIntruderBackdoor)
    subscribe(location, "mode", modeChangeHandler)
    subscribe(alarmsEnabled, "switch", handleAlarmsEnabledSwitch)
    subscribe(pauseBDAlarm, "switch", handlePauseBDAlarm)
}

def modeChangeHandler(evt) {
    logDebug "Mode changed from ${evt.value} to ${location.mode}"
    
    // If mode changed away from restricted modes, disable security
    if (restrictedModes && !restrictedModes.contains(location.mode)) {
        logInfo "Mode changed to ${location.mode} - disabling night security"
        disableNightSecurity()
    }
}

def handleAlarmsEnabledSwitch(evt) {
    logDebug "AlarmsEnabled switch changed to: ${evt.value}"
    
    if (evt.value == "off") {
        logInfo "Alarms disabled - stopping all night security sirens"
        stopAlarms()
        // Cancel any scheduled alarm executions
        unschedule(executeAlarmsOn)
        unschedule(executeShedSirenOn)
    }
}

def handleBHScreen(evt) {
    if (!isActiveMode()) return
    if (traveling.currentValue("switch") == "off" && silent.currentValue("switch") == "off" && silenceOffice?.currentValue("switch") != "on") {
        logInfo "Birdhouse screen door opened during night mode"
        notificationDevices.each { it.deviceNotification("Birdhouse screen door is open, birdhouse screen door is open") }
    }
}

def handleCarportBeam(evt) {
    if (!isActiveMode()) return
    if (evt.value == "closed") { // Beam broken (infrared beam interrupted)
         logBeamActivity("Carport beam broken")
         
         // Check if motion or Ring person detection is active (verification to avoid false positives from animals)
         Boolean motionVerified = carportFrontMotion.currentValue("motion") == "active" || standardRpdSwitches?.any { it.currentValue("switch") == "on" }
         
         if (!motionVerified) {
             logDebug "Beam broken but no motion/person verification - skipping alert to avoid false positives"
             return
         }
         
         if (silent.currentValue("switch") == "off" && silenceOffice?.currentValue("switch") != "on") {
             // Check cooldown to avoid repeated alerts
             if (state.lastCarportAlert && (now() - state.lastCarportAlert) < 300000) { // 5 minute cooldown
                 logDebug "Carport alert in cooldown period - skipping"
                 return
             }
             
             state.lastCarportAlert = now()
             logInfo "Intruder detected in carport - motion/person verified"
             notificationDevices.each { it.deviceNotification("Alert! Intruder in the carport!") }
             Integer delay = getConfigValue("alertDelay", "AlertDelay") as Integer
             runIn(delay, executeAlarmsOn)
         }
    } else if (evt.value == "open") { // Beam clear (normal state)
        logBeamActivity("Carport beam cleared")
    }
}

def executeAlarmsOn() {
    if (alarmsEnabled.currentValue("switch") == "on") {
        logInfo "Executing alarms via cross-app communication"
        triggerAlarmExecution()
    } else {
        logDebug "Alarms not enabled, skipping execution"
    }
}

def stopAlarms() {
    sirens.each { it.off() }
}

def executeShedSirenOn() {
    sirens.each { it.siren() }
    runIn(4, stopShedSiren)
}

def stopShedSiren() {
    sirens.each { it.off() }
}

def turnAllLightsOnNow() {
    allLights.on()
    allLightsOn.on()
}

def whisperToGuestroomNow() {
    def msg = getGlobalVar("EchoMessage").value
    guestRoomEcho.deviceNotification(msg)
}

def handleConcreteShed(evt) { }

def handleDiningRoomDoor(evt) {
    if (!isActiveMode()) return
    if (alarmsEnabled.currentValue("switch") == "on" && silent.currentValue("switch") == "off" && silenceOffice?.currentValue("switch") != "on" && pauseDRDoorAlarm.currentValue("switch") == "off") {
        logInfo "Intruder detected at dining room door"
        turnAllLightsOnNow()
        notificationDevices.each { it.deviceNotification("Intruder at the Dining Room Door") }
        Integer delay = getConfigValue("alertDelay", "AlertDelay") as Integer
        runIn(delay, executeAlarmsOn)
    }
}

def handleLRFrenchDoors(evt) { }

def handleFrontDoor(evt) { }

def handleWoodshed(evt) { }

def handleStandardIntruder(evt) {
    if (!isActiveMode()) return
    if (alarmsEnabled.currentValue("switch") != "on" || silent.currentValue("switch") == "on" || silenceOffice?.currentValue("switch") == "on") return
    def message = evt.device.label
    logInfo "Intruder detected: ${message}"
    turnAllLightsOnNow()
    notificationDevices.each { it.deviceNotification(message) }
    Integer delay = getConfigValue("alertDelay", "AlertDelay") as Integer
    runIn(delay, executeAlarmsOn)
}

def handleShedIntruder(evt) {
    if (!isActiveMode()) return
    if (alarmsEnabled.currentValue("switch") != "on" || silent.currentValue("switch") == "on" || silenceOffice?.currentValue("switch") == "on") return
    def message = evt.device.label
    logInfo "Shed intruder detected: ${message}"
    notificationDevices.each { it.deviceNotification(message) }
    executeShedSirenOn()
    turnAllLightsOnNow()
}

def handleStandardRPD(evt) {
    if (!isActiveMode()) return
    if (silent.currentValue("switch") == "on" || silenceOffice?.currentValue("switch") == "on") return
    def message = evt.device.label
    logInfo "Person detected: ${message}"
    notificationDevices.each { it.deviceNotification(message) }
    allLightsOn.on()
}

def handleRPDFrontDoor(evt) { }

def handleRPDBirdHouse(evt) {
    if (!isActiveMode()) return
    allLightsOn.on()
    if (silent.currentValue("switch") == "off" && silenceOffice?.currentValue("switch") != "on") {
        notificationDevices.each { it.deviceNotification(evt.device.label) }
        setGlobalVar("EchoMessage", evt.device.label)
        whisperToGuestroomNow()
    }
}

def handleRPDGarden(evt) { }

def handleRPDBackDoor(evt) { }

def handleSheShed(evt) { }

def handleBackdoorMotion(evt) {
    if (!isActiveMode()) return
    if (!isValidMotionTransition(evt.deviceId.toString(), evt.value)) return
    if (evt.value == "active" && floodSide.currentValue("motion") == "active" && highAlert.currentValue("switch") == "on" && silent.currentValue("switch") == "off" && silenceOffice?.currentValue("switch") != "on") {
        turnAllLightsOnNow()
        notificationDevices.each { it.deviceNotification("Intruder at the Backdoor") }
    }
}

def handleIntruderBackdoor(evt) {
    if (!isActiveMode()) return
    if (pauseBDAlarm.currentValue("switch") == "off" && silent.currentValue("switch") == "off" && silenceOffice?.currentValue("switch") != "on" && alarmsEnabled.currentValue("switch") == "on") {
        setGlobalVar("AlertMessage", "Intruder at the Backdoor")
        logInfo "Intruder detected at backdoor - triggering alarms"
        executeAlarmsOn()
    }
}

def handlePauseBDAlarm(evt) {
    if (evt.value == "on") {
        logInfo "Pause Backdoor Alarm activated - turning Ring Mode OFF"
        ringModeOnOff.off()
    } else if (evt.value == "off") {
        logInfo "Pause Backdoor Alarm deactivated - turning Ring Mode ON"
        ringModeOnOff.on()
    }
}

// ========================================
// CROSS-APP COMMUNICATION METHODS
// ========================================

def triggerAlarmExecution() {
    if (alarmTriggerSwitch) {
        logDebug "Triggering alarm execution via SecurityAlarmManager"
        alarmTriggerSwitch.on()
    } else {
        // Fallback to local siren control
        logDebug "No alarm trigger switch configured, using local sirens"
        sirens.each { it.siren() }
        runIn(getConfigValue("sirenDuration", "AlarmDuration") as Integer, stopAlarms)
    }
}

def triggerAlarmStop() {
    if (alarmStopSwitch) {
        logDebug "Stopping alarms via SecurityAlarmManager"
        alarmStopSwitch.on()
    } else {
        // Fallback to local siren control
        logDebug "No alarm stop switch configured, stopping local sirens"
        stopAlarms()
    }
}

def triggerEmergencyLights() {
    if (emergencyLightsSwitch) {
        logDebug "Triggering emergency lights via LightsAutomationManager"
        emergencyLightsSwitch.on()
    } else {
        // Fallback to turning on all lights
        logDebug "No emergency lights switch configured, using local light control"
        turnAllLightsOnNow()
    }
}

def triggerAction(String switchName) {
    def targetSwitch = null
    
    switch(switchName) {
        case "AlarmTrigger":
            targetSwitch = alarmTriggerSwitch
            break
        case "AlarmStop":
            targetSwitch = alarmStopSwitch
            break
        case "EmergencyLights":
            targetSwitch = emergencyLightsSwitch
            break
    }
    
    if (targetSwitch) {
        logDebug "Activating ${switchName}"
        targetSwitch.on()
        return true
    }
    
    logDebug "Switch ${switchName} not configured"
    return false
}

// ========================================
// BEAM LOGGING METHOD
// ========================================

def logBeamActivity(String activity) {
    Boolean loggingEnabled = getConfigValue("beamLogging", "BeamLogEnabled") as Boolean
    
    if (loggingEnabled) {
        logInfo "BEAM LOG: ${activity}"
        // Could also send to notification device if needed
        // notificationDevices.each { it.deviceNotification("Beam Log: ${activity}") }
    }
}

// ========================================
// NIGHT SECURITY CONTROL
// ========================================

def disableNightSecurity() {
    logInfo "Disabling night security - stopping alarms, turning off alerts"
    
    // Stop any active alarms
    triggerAlarmStop()
    
    // Turn off any active switches
    rearGateActive?.off()
    allLightsOn?.off()
    
    // If pause backdoor alarm was active, turn it off so Ring Mode is restored
    if (pauseBDAlarm.currentValue("switch") == "on") {
        logInfo "Clearing pause backdoor alarm state - restoring Ring Mode ON"
        pauseBDAlarm.off()
    }
    
    // Cancel any pending alarm executions
    unschedule(executeAlarmsOn)
    unschedule(executeShedSirenOn)
    
    logInfo "Night security disabled"
}

// ========================================
// HELPER METHODS
// ========================================

private Boolean isActiveMode() {
    if (!restrictedModes || restrictedModes.contains(location.mode)) return true
    logDebug "Ignoring event: mode ${location.mode} not in ${restrictedModes}"
    return false
}

def syncHubVariables() {
    setHubVar("AlertDelay", (hubVar_AlertDelay ?: 5).toString())
    setHubVar("AlarmDuration", (hubVar_AlarmDuration ?: 300).toString())
    setHubVar("BeamLogEnabled", (hubVar_BeamLogEnabled != null ? hubVar_BeamLogEnabled : true).toString())
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

// ========================================
// MOTION DEBOUNCE
// ========================================

Boolean isValidMotionTransition(String deviceId, String newValue) {
    Integer minHoldSeconds = (settings.motionDebounceSeconds ?: 3)
    String stateKey = "motionDebounce_${deviceId}"
    Map lastEvent = atomicState[stateKey] as Map

    if (lastEvent) {
        Long prevTime = lastEvent.timestamp as Long
        Long elapsed = now() - prevTime
        Long minHoldMs = minHoldSeconds * 1000L

        if (elapsed < minHoldMs) {
            logDebug "Ignoring rapid motion '${newValue}' from device ${deviceId} — only ${elapsed}ms since last event (min ${minHoldMs}ms required)"
            return false
        }
    }

    atomicState[stateKey] = [value: newValue, timestamp: now()]
    return true
}

// ========================================
// LOGGING
// ========================================

def logDebug(msg) {
    if (logLevel in ["Debug","Trace"]) log.debug "${app.label}: ${msg}"
}

def logInfo(msg) {
    if (logLevel in ["Info","Debug","Trace"]) log.info "${app.label}: ${msg}"
}
void logTrace(String msg) { if (logLevel == "Trace") log.trace "${app.label}: ${msg}" }
