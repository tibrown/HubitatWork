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
    description: "Centralized management of Ring doorbell/camera motion and person detection with location-based responses",
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
        section("Ring Devices") {
            input "ringBackdoor", "capability.motionSensor", title: "Ring Backdoor Camera", required: false
            input "ringBirdHouse", "capability.motionSensor", title: "Ring Birdhouse Camera", required: false
            input "ringRearGate", "capability.motionSensor", title: "Ring Rear Gate Camera", required: false
            input "ringPen", "capability.motionSensor", title: "Ring Pen Camera", required: false
            input "ringGarden", "capability.motionSensor", title: "Ring Garden Camera", required: false
            input "ringFrontDoor", "capability.motionSensor", title: "Ring Front Door Camera", required: false
        }
        
        section("Motion Detection Settings") {
            input "motionResetDelay", "number", title: "Motion Auto-Reset Delay (seconds)", 
                defaultValue: 60, range: "10..600", required: false,
                description: "Hub variable: motionResetDelay"
            input "enableMotionReset", "bool", title: "Enable Automatic Motion Reset", 
                defaultValue: true, required: false
        }
        
        section("Person Detection Settings") {
            input "personDetectionTimeout", "number", title: "Person Detection Timeout (seconds)", 
                defaultValue: 120, range: "30..600", required: false,
                description: "Hub variable: personDetectionTimeout"
            input "notificationDelay", "number", title: "Notification Delay (seconds)", 
                defaultValue: 5, range: "0..60", required: false,
                description: "Hub variable: notificationDelay"
            input "cooldownPeriod", "number", title: "Notification Cooldown Period (minutes)", 
                defaultValue: 5, range: "1..60", required: false,
                description: "Hub variable: cooldownPeriod"
            input "sensitivityLevel", "number", title: "Detection Sensitivity Level (1-10)", 
                defaultValue: 5, range: "1..10", required: false,
                description: "Hub variable: sensitivityLevel"
        }
        
        section("Night Mode Settings") {
            input "nightModeEnabled", "bool", title: "Enable Night Mode Detection", 
                defaultValue: true, required: false,
                description: "Hub variable: nightModeEnabled (true/false)"
            input "nightModes", "mode", title: "Night Modes", multiple: true, required: false,
                description: "Modes considered 'night' for enhanced security"
        }
        
        section("Notification Settings") {
            input "notificationDevices", "capability.notification", title: "Push Notification Devices", 
                multiple: true, required: false
            input "alexaDevice", "capability.speechSynthesis", title: "Alexa Device for Announcements", 
                required: false
            input "silentSwitch", "capability.switch", title: "Silent Mode Switch (disables audible alerts)", 
                required: false
        }
        
        section("Security Integration") {
            input "nightSecurityAlert", "capability.switch", title: "Night Security Alert Switch", 
                required: false,
                description: "Trigger night security actions for person detection"
            input "alarmTrigger", "capability.switch", title: "Alarm Trigger Switch", 
                required: false,
                description: "Trigger alarm system for critical detections"
        }
        
        section("Logging") {
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
    
    // Subscribe to all Ring devices
    if (ringBackdoor) {
        subscribe(ringBackdoor, "motion", handleMotionBackdoor)
        subscribe(ringBackdoor, "personDetected", handlePersonBackdoor)
    }
    if (ringBirdHouse) {
        subscribe(ringBirdHouse, "motion", handleMotionBirdHouse)
        subscribe(ringBirdHouse, "personDetected", handlePersonBirdHouse)
    }
    if (ringRearGate) {
        subscribe(ringRearGate, "motion", handleMotionRearGate)
        subscribe(ringRearGate, "personDetected", handlePersonRearGate)
    }
    if (ringPen) {
        subscribe(ringPen, "motion", handleMotionPen)
        subscribe(ringPen, "personDetected", handlePersonPen)
    }
    if (ringGarden) {
        subscribe(ringGarden, "motion", handleMotionGarden)
        subscribe(ringGarden, "personDetected", handlePersonGarden)
    }
    if (ringFrontDoor) {
        subscribe(ringFrontDoor, "motion", handleMotionFrontDoor)
        subscribe(ringFrontDoor, "personDetected", handlePersonFrontDoor)
    }
    
    logInfo "Subscriptions complete for ${getAllRingDevices().size()} Ring devices"
}

// Motion Handlers
def handleMotionBackdoor(evt) {
    handleMotion(evt, "Backdoor", ringBackdoor)
}

def handleMotionBirdHouse(evt) {
    handleMotion(evt, "Birdhouse", ringBirdHouse)
}

def handleMotionRearGate(evt) {
    handleMotion(evt, "Rear Gate", ringRearGate)
}

def handleMotionPen(evt) {
    handleMotion(evt, "Pen", ringPen)
}

def handleMotionGarden(evt) {
    handleMotion(evt, "Garden", ringGarden)
}

def handleMotionFrontDoor(evt) {
    handleMotion(evt, "Front Door", ringFrontDoor)
}

def handleMotion(evt, String location, device) {
    if (evt.value != "active") {
        logDebug "Motion inactive at ${location}"
        return
    }
    
    logInfo "Motion detected at ${location}"
    
    // Store motion time for debouncing
    String stateKey = "lastMotion_${location.replaceAll(' ', '')}"
    state[stateKey] = now()
    
    // Schedule auto-reset if enabled
    if (getConfigValue("enableMotionReset", null)) {
        Integer delay = getConfigValue("motionResetDelay", "motionResetDelay") ?: 60
        runIn(delay, "resetMotion", [data: [location: location, device: device]])
        logDebug "Scheduled motion reset for ${location} in ${delay} seconds"
    }
}

def resetMotion(data) {
    String location = data.location
    def device = data.device
    
    logDebug "Auto-resetting motion for ${location}"
    
    // Clear motion state
    String stateKey = "lastMotion_${location.replaceAll(' ', '')}"
    state.remove(stateKey)
    
    logInfo "Motion reset complete for ${location}"
}

// Person Detection Handlers
def handlePersonBackdoor(evt) {
    handlePerson(evt, "Backdoor", ringBackdoor, true)
}

def handlePersonBirdHouse(evt) {
    handlePerson(evt, "Birdhouse", ringBirdHouse, false)
}

def handlePersonRearGate(evt) {
    handlePerson(evt, "Rear Gate", ringRearGate, true)
}

def handlePersonPen(evt) {
    handlePerson(evt, "Pen", ringPen, false)
}

def handlePersonGarden(evt) {
    handlePerson(evt, "Garden", ringGarden, false)
}

def handlePersonFrontDoor(evt) {
    handlePerson(evt, "Front Door", ringFrontDoor, true)
}

def handlePerson(evt, String location, device, Boolean criticalLocation) {
    if (evt.value != "detected") {
        logDebug "Person detection cleared at ${location}"
        return
    }
    
    logInfo "Person detected at ${location}"
    
    // Check cooldown period to prevent notification spam
    if (!shouldNotify(location)) {
        logDebug "Person detection at ${location} within cooldown period, skipping notification"
        return
    }
    
    // Store detection time
    String stateKey = "lastPerson_${location.replaceAll(' ', '')}"
    state[stateKey] = now()
    
    // Check if we should notify based on mode and settings
    String currentMode = location.mode
    Boolean isNightMode = isNightMode()
    
    // Build notification message
    String message = "Person detected at ${location}"
    if (isNightMode) {
        message = "⚠️ NIGHT ALERT: ${message}"
    }
    
    // Delay notification if configured
    Integer delay = getConfigValue("notificationDelay", "notificationDelay") ?: 0
    if (delay > 0) {
        runIn(delay, "sendPersonNotification", 
            [data: [location: location, message: message, critical: criticalLocation, nightMode: isNightMode]])
    } else {
        sendPersonNotification([location: location, message: message, critical: criticalLocation, nightMode: isNightMode])
    }
}

def sendPersonNotification(data) {
    String location = data.location
    String message = data.message
    Boolean critical = data.critical
    Boolean nightMode = data.nightMode
    
    logInfo "Sending notification: ${message}"
    
    // Send push notification
    if (notificationDevices) {
        notificationDevices.each { device ->
            device.deviceNotification(message)
        }
    }
    
    // Alexa announcement (unless in silent mode)
    if (alexaDevice && !isSilentMode()) {
        alexaDevice.speak(message)
    }
    
    // Trigger security actions for critical locations at night
    if (critical && nightMode) {
        triggerNightSecurity(location)
    }
    
    // Trigger alarm for very critical situations
    if (critical && nightMode && location in ["Backdoor", "Front Door"]) {
        triggerAlarm(location)
    }
}

def triggerNightSecurity(String location) {
    if (!nightSecurityAlert) {
        logDebug "Night security alert switch not configured"
        return
    }
    
    logInfo "Triggering night security alert for person at ${location}"
    nightSecurityAlert.on()
    
    // Auto-reset after processing
    runIn(5, resetNightSecurityAlert)
}

def resetNightSecurityAlert() {
    if (nightSecurityAlert) {
        nightSecurityAlert.off()
    }
}

def triggerAlarm(String location) {
    if (!alarmTrigger) {
        logDebug "Alarm trigger switch not configured"
        return
    }
    
    logInfo "ALARM: Person detected at critical location ${location} during night mode"
    alarmTrigger.on()
    
    // Auto-reset after processing
    runIn(5, resetAlarmTrigger)
}

def resetAlarmTrigger() {
    if (alarmTrigger) {
        alarmTrigger.off()
    }
}

// Helper Methods

def shouldNotify(String location) {
    String stateKey = "lastPerson_${location.replaceAll(' ', '')}"
    Long lastDetection = state[stateKey] ?: 0
    
    if (lastDetection == 0) {
        return true
    }
    
    Integer cooldown = getConfigValue("cooldownPeriod", "cooldownPeriod") ?: 5
    Long cooldownMs = cooldown * 60 * 1000
    Long elapsed = now() - lastDetection
    
    return elapsed >= cooldownMs
}

def isNightMode() {
    if (!getConfigValue("nightModeEnabled", "nightModeEnabled")) {
        return false
    }
    
    if (!nightModes) {
        return false
    }
    
    String currentMode = location.mode
    return currentMode in nightModes
}

def isSilentMode() {
    if (!silentSwitch) {
        return false
    }
    return silentSwitch.currentValue("switch") == "on"
}

def getAllRingDevices() {
    def devices = []
    if (ringBackdoor) devices << ringBackdoor
    if (ringBirdHouse) devices << ringBirdHouse
    if (ringRearGate) devices << ringRearGate
    if (ringPen) devices << ringPen
    if (ringGarden) devices << ringGarden
    if (ringFrontDoor) devices << ringFrontDoor
    return devices
}

def getConfigValue(String settingName, String hubVarName) {
    // Try to get value from hub variable first (if hubVarName provided)
    if (hubVarName) {
        def hubVar = getGlobalVar(hubVarName)
        if (hubVar != null) {
            logDebug "Using hub variable ${hubVarName}: ${hubVar}"
            return convertValue(hubVar, settingName)
        }
    }
    
    // Fall back to app setting
    def settingValue = settings[settingName]
    logTrace "Using app setting ${settingName}: ${settingValue}"
    return settingValue
}

def convertValue(value, String settingName) {
    // Convert hub variable value to appropriate type based on setting name
    if (value == null) return null
    
    // Boolean settings
    if (settingName in ["enableMotionReset", "nightModeEnabled"]) {
        if (value instanceof Boolean) return value
        return value.toString().toLowerCase() in ["true", "1", "yes", "on"]
    }
    
    // Number settings
    if (settingName in ["motionResetDelay", "personDetectionTimeout", "notificationDelay", 
                        "cooldownPeriod", "sensitivityLevel"]) {
        if (value instanceof Number) return value
        return value.toString().toInteger()
    }
    
    // Default: return as string
    return value.toString()
}

// Logging Methods

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
