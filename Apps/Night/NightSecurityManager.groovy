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
    namespace: "hubitat",
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
        section("Sensors") {
            input "doorBHScreen", "capability.contactSensor", title: "BH Screen Door", required: true
            input "carportBeam", "capability.contactSensor", title: "Carport Beam", required: true
            input "carportFrontMotion", "capability.motionSensor", title: "Carport Front Motion", required: true
            input "concreteShedZooz", "capability.contactSensor", title: "Concrete Shed Door", required: true
            input "doorDiningRoom", "capability.contactSensor", title: "Dining Room Door", required: true
            input "doorLivingRoomFrench", "capability.contactSensor", title: "Living Room French Doors", required: true
            input "doorFront", "capability.contactSensor", title: "Front Door", required: true
            input "woodshedDoor", "capability.contactSensor", title: "Woodshed Door", required: true
            input "rpdFrontDoor", "capability.switch", title: "RPD Front Door (Switch)", required: true
            input "rpdBirdHouse", "capability.switch", title: "RPD Bird House (Switch)", required: true
            input "rpdGarden", "capability.switch", title: "RPD Garden (Switch)", required: true
            input "rpdCPen", "capability.switch", title: "RPD Rear Gate (Switch)", required: true
            input "chickenPenOutside", "capability.motionSensor", title: "Chicken Pen Outside Motion", required: true
            input "doorBirdHouse", "capability.contactSensor", title: "She Shed Door (BirdHouse)", required: true
            input "outsideBackdoor", "capability.motionSensor", title: "Outside Backdoor Motion", required: true
            input "floodSide", "capability.motionSensor", title: "Flood Side Motion", required: true
            input "doorLanai", "capability.contactSensor", title: "Lanai Door (Backdoor)", required: true
        }
        
        section("Switches & Controls") {
            input "traveling", "capability.switch", title: "Traveling Switch", required: true
            input "silent", "capability.switch", title: "Silent Switch", required: true
            input "highAlert", "capability.switch", title: "High Alert Switch", required: true
            input "alarmsEnabled", "capability.switch", title: "Alarms Enabled Switch", required: true
            input "pauseDRDoorAlarm", "capability.switch", title: "Pause DR Door Alarm", required: true
            input "pauseBDAlarm", "capability.switch", title: "Pause Backdoor Alarm", required: true
            input "rearGateActive", "capability.switch", title: "Rear Gate Active Switch", required: true
            input "allLightsOn", "capability.switch", title: "All Lights ON Switch", required: true
        }

        section("Notification Devices") {
            input "notificationDevices", "capability.notification", title: "Notification Devices", multiple: true, required: true
        }

        section("Actions / Outputs") {
            input "sirens", "capability.alarm", title: "Sirens", multiple: true, required: true
            input "allLights", "capability.switch", title: "All Lights", multiple: true, required: true
            input "guestRoomEcho", "capability.notification", title: "Guest Room Echo", required: true
        }

        section("Restrictions") {
            input "restrictedModes", "mode", title: "Only run in these modes", multiple: true, required: true
        }
        
        section("Cross-App Communication") {
            input "alarmTriggerSwitch", "capability.switch", title: "Alarm Trigger Switch (sends to SecurityAlarmManager)", required: false
            input "alarmStopSwitch", "capability.switch", title: "Alarm Stop Switch (sends to SecurityAlarmManager)", required: false
            input "emergencyLightsSwitch", "capability.switch", title: "Emergency Lights Switch (sends to LightsAutomationManager)", required: false
        }
        
        section("Time Window Configuration") {
            input "nightAlertStartTime", "time", title: "Night Alert Start Time (ends when mode changes to Morning)", defaultValue: "20:00", required: false
        }
        
        section("Alert Configuration") {
            input "alertDelay", "number", title: "Delay before triggering alarms (seconds)", defaultValue: 5, required: false
            input "sirenDuration", "number", title: "Siren duration (seconds)", defaultValue: 300, required: false
            input "beamLogging", "bool", title: "Enable detailed carport beam logging", defaultValue: true, required: false
        }
        
        section("Hub Variable Overrides") {
            paragraph "This app supports hub variable overrides for flexible configuration:"
            paragraph "• NightAlertStartTime - Override night alert start time (HH:mm format)"
            paragraph "• AlertDelay - Override delay before alerting (seconds)"
            paragraph "• BeamLogEnabled - Enable/disable detailed beam logging (true/false)"
            paragraph "• AlarmsEnabled - Read alarm status from SecurityAlarmManager (read-only)"
            paragraph "• AlarmActive - Check if alarms are currently active (read-only)"
        }
        
        section("Logging") {
            input "logEnable", "bool", title: "Enable debug logging", defaultValue: true
            input "infoEnable", "bool", title: "Enable info logging", defaultValue: true
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
    initialize()
}

def initialize() {
    logInfo "Initializing Night Security Manager"
    subscribe(doorBHScreen, "contact", evtHandler)
    subscribe(carportBeam, "contact", evtHandler)
    subscribe(concreteShedZooz, "contact", evtHandler)
    subscribe(doorDiningRoom, "contact", evtHandler)
    subscribe(doorLivingRoomFrench, "contact", evtHandler)
    subscribe(doorFront, "contact", evtHandler)
    subscribe(woodshedDoor, "contact", evtHandler)
    subscribe(rpdFrontDoor, "switch", evtHandler)
    subscribe(rpdBirdHouse, "switch", evtHandler)
    subscribe(rpdGarden, "switch", evtHandler)
    subscribe(rpdCPen, "switch", evtHandler)
    subscribe(doorBirdHouse, "contact", evtHandler)
    subscribe(outsideBackdoor, "motion", evtHandler)
    subscribe(doorLanai, "contact", evtHandler)
    subscribe(location, "mode", modeChangeHandler)
    subscribe(alarmsEnabled, "switch", handleAlarmsEnabledSwitch)
}

def modeChangeHandler(evt) {
    logDebug "Mode changed from ${evt.value} to ${location.mode}"
    
    // If mode changed away from restricted modes after night alert start time, disable security
    if (restrictedModes && !restrictedModes.contains(location.mode) && isAfterNightAlertStart()) {
        logInfo "Mode changed to ${location.mode} after night alert start time - disabling night security"
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

def evtHandler(evt) {
    logDebug "Event: ${evt.name} ${evt.value} from ${evt.displayName}"
    
    if (restrictedModes && !restrictedModes.contains(location.mode)) {
        logDebug "Skipping event: Mode is ${location.mode}, required: ${restrictedModes}"
        return
    }

    if (evt.deviceId == doorBHScreen.deviceId) handleBHScreen(evt)
    else if (evt.deviceId == carportBeam.deviceId) handleCarportBeam(evt)
    else if (evt.deviceId == concreteShedZooz.deviceId) handleConcreteShed(evt)
    else if (evt.deviceId == doorDiningRoom.deviceId) handleDiningRoomDoor(evt)
    else if (evt.deviceId == doorLivingRoomFrench.deviceId) handleLRFrenchDoors(evt)
    else if (evt.deviceId == doorFront.deviceId) handleFrontDoor(evt)
    else if (evt.deviceId == woodshedDoor.deviceId) handleWoodshed(evt)
    else if (evt.deviceId == rpdFrontDoor.deviceId) handleRPDFrontDoor(evt)
    else if (evt.deviceId == rpdBirdHouse.deviceId) handleRPDBirdHouse(evt)
    else if (evt.deviceId == rpdGarden.deviceId) handleRPDGarden(evt)
    else if (evt.deviceId == rpdCPen.deviceId) handleRPDRearGate(evt)
    else if (evt.deviceId == doorBirdHouse.deviceId) handleSheShed(evt)
    else if (evt.deviceId == outsideBackdoor.deviceId) handleBackdoorMotion(evt)
    else if (evt.deviceId == doorLanai.deviceId) handleIntruderBackdoor(evt)
}

def handleBHScreen(evt) {
    if (evt.value == "open" && traveling.currentSwitch == "off") {
        logInfo "Birdhouse screen door opened during night mode"
        notificationDevices.each { it.deviceNotification("Birdhouse screen door is open, birdhouse screen door is open") }
    }
}

def handleCarportBeam(evt) {
    if (evt.value == "open") { // Beam Broken/Active
         logBeamActivity("Carport beam broken")
         
         // Check if past night alert start time
         boolean timeCondition = isAfterNightAlertStart()
         
         if (silent.currentSwitch == "off" && carportFrontMotion.currentMotion == "active" && (timeCondition || highAlert.currentSwitch == "on")) {
             logInfo "Intruder detected in carport - time condition or high alert active"
             notificationDevices.each { it.deviceNotification("Alert! Intruder in the carport!") }
             Integer delay = getConfigValue("alertDelay", "AlertDelay") as Integer
             runIn(delay, executeAlarmsOn)
         }
    } else if (evt.value == "closed") {
        logBeamActivity("Car port beam broken")
        notificationDevices.each { it.deviceNotification("Car Port Beam Broken") }
    }
}

def executeAlarmsOn() {
    if (alarmsEnabled.currentSwitch == "on") {
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

def handleConcreteShed(evt) {
    if (evt.value == "open" && alarmsEnabled.currentSwitch == "on" && siren1.currentSwitch == "off") {
        logInfo "Intruder detected in concrete shed"
        notificationDevices.each { it.deviceNotification("Intruder in the Concrete Shed") }
        executeShedSirenOn()
        turnAllLightsOnNow()
    }
}

def handleDiningRoomDoor(evt) {
    if (evt.value == "open" && alarmsEnabled.currentSwitch == "on" && silent.currentSwitch == "off" && pauseDRDoorAlarm.currentSwitch == "off") {
        logInfo "Intruder detected at dining room door"
        turnAllLightsOnNow()
        notificationDevices.each { it.deviceNotification("Intruder at the Dining Room Door") }
        Integer delay = getConfigValue("alertDelay", "AlertDelay") as Integer
        runIn(delay, executeAlarmsOn)
    }
}

def handleLRFrenchDoors(evt) {
    if (evt.value == "open" && alarmsEnabled.currentSwitch == "on") {
        logInfo "Intruder detected at living room French doors"
        turnAllLightsOnNow()
        notificationDevices.each { it.deviceNotification("Intruder at the Living Room French Doors") }
        Integer delay = getConfigValue("alertDelay", "AlertDelay") as Integer
        runIn(delay, executeAlarmsOn)
    }
}

def handleFrontDoor(evt) {
    if (evt.value == "open" && alarmsEnabled.currentSwitch == "on" && silent.currentSwitch == "off") {
        logInfo "Intruder detected at front door"
        turnAllLightsOnNow()
        notificationDevices.each { it.deviceNotification("Intruder at the Front Door") }
        Integer delay = getConfigValue("alertDelay", "AlertDelay") as Integer
        runIn(delay, executeAlarmsOn)
    }
}

def handleWoodshed(evt) {
    if (evt.value == "open" && silent.currentSwitch == "off" && alarmsEnabled.currentSwitch == "on") {
        logInfo "Intruder detected in woodshed"
        notificationDevices.each { it.deviceNotification("Intruder in the Woodshed") }
        executeShedSirenOn()
        turnAllLightsOnNow()
    }
}

def handleRPDFrontDoor(evt) {
    if (evt.value == "on") {
        notificationDevices.each { it.deviceNotification("Person at the Front Door") }
        allLightsOn.on()
    }
}

def handleRPDBirdHouse(evt) {
    if (evt.value == "on") {
        notificationDevices.each { it.deviceNotification("Intruder at the Bird House") }
        allLightsOn.on()
        setGlobalVar("EchoMessage", "Intruder at the Bird House")
        whisperToGuestroomNow()
    }
}

def handleRPDGarden(evt) {
    if (evt.value == "on") {
        notificationDevices.each { it.deviceNotification("Intruder in the Garden") }
        allLightsOn.on()
    }
}

def handleRPDRearGate(evt) {
    if (evt.value == "on" && chickenPenOutside.currentMotion == "active" && isAfterNightAlertStart()) {
        rearGateActive.on()
        notificationDevices.each { it.deviceNotification("Intruder at the Rear Gate") }
    }
}

def handleSheShed(evt) {
    if (evt.value == "open" && silent.currentSwitch == "off") {
        logInfo "Intruder detected in She Shed"
        allLightsOn.on()
        notificationDevices.each { it.deviceNotification("Intruder in the She Shed") }
        executeShedSirenOn()
    }
}

def handleBackdoorMotion(evt) {
    if (evt.value == "active" && floodSide.currentMotion == "active" && highAlert.currentSwitch == "on") {
        turnAllLightsOnNow()
        notificationDevices.each { it.deviceNotification("Intruder at the Backdoor") }
    }
}

def handleIntruderBackdoor(evt) {
    if (evt.value == "open" && pauseBDAlarm.currentSwitch == "off" && silent.currentSwitch == "off" && alarmsEnabled.currentSwitch == "on") {
        setGlobalVar("AlertMessage", "Intruder at the Backdoor")
        logInfo "Intruder detected at backdoor - triggering alarms"
        executeAlarmsOn()
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
    
    // Cancel any pending alarm executions
    unschedule(executeAlarmsOn)
    unschedule(executeShedSirenOn)
    
    logInfo "Night security disabled"
}

// ========================================
// HELPER METHODS
// ========================================

def isAfterNightAlertStart() {
    def startTimeStr = getConfigValue("nightAlertStartTime", "NightAlertStartTime") ?: "20:00"
    def startTime = timeToday(startTimeStr, location.timeZone)
    def now = new Date()
    return now.after(startTime)
}

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

// ========================================
// LOGGING
// ========================================

def logDebug(msg) {
    if (logEnable) log.debug "${app.label}: ${msg}"
}

def logInfo(msg) {
    if (infoEnable) log.info "${app.label}: ${msg}"
}
