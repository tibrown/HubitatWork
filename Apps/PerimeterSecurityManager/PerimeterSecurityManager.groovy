/**
 *  PerimeterSecurityManager
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
    name: "Perimeter Security Manager",
    namespace: "tibrown",
    author: "Tim Brown",
    description: "Monitors gates, fences, shock sensors, and perimeter security devices with Ring person detection",
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
    dynamicPage(name: "mainPage", title: "Perimeter Security Manager", install: true, uninstall: true) {
        section("Gate Sensors") {
            input "frontGate", "capability.contactSensor",
                  title: "Front Gate Sensor",
                  required: false
            
            input "sideYardGate", "capability.contactSensor",
                  title: "Side Yard Gate Sensor",
                  required: false
            
            input "gateAlertDelaySeconds", "number",
                  title: "Gate Alert Delay (seconds)",
                  description: "Delay before alerting on gate open",
                  defaultValue: 30,
                  range: "0..300",
                  required: false
        }
        
        section("Shock & Tamper Sensors") {
            input "rearGateShock", "capability.shockSensor",
                  title: "Rear Gate Shock Sensor",
                  required: false
            
            input "shockSensitivity", "number",
                  title: "Shock Sensitivity (1-10)",
                  description: "Higher = more sensitive",
                  defaultValue: 5,
                  range: "1..10",
                  required: false
        }
        
        section("Additional Perimeter Sensors") {
            input "outsidePenSensor", "capability.motionSensor",
                  title: "Outside Pen Motion Sensor",
                  required: false
            
            input "gunCabinet", "capability.contactSensor",
                  title: "Gun Cabinet Sensor",
                  required: false
        }
        
        section("Carport Beam") {
            input "carportBeam", "capability.contactSensor",
                  title: "Carport Beam Sensor",
                  description: "Infrared beam sensor (closed = beam broken)",
                  required: false
            
            input "carportMotion", "capability.motionSensor",
                  title: "Carport Front Motion Sensor",
                  description: "Used to verify beam breaks",
                  required: false
            
            input "frontDoorRingMotion", "capability.switch",
                  title: "Front Door Ring Motion (Switch)",
                  description: "Ring doorbell motion as verification",
                  required: false
        }
        
        section("Carport Beam Condition Switches") {
            input "silentSwitch", "capability.switch",
                  title: "Silent Switch",
                  description: "Global silent mode suppresses alerts",
                  required: false
            
            input "silentCarport", "capability.switch",
                  title: "Silent Carport Switch",
                  description: "Carport-specific silent mode",
                  required: false
            
            input "pauseCarportBeam", "capability.switch",
                  title: "Pause Carport Beam Switch",
                  description: "Temporarily disable beam alerts",
                  required: false
        }
        
        section("Carport Beam Timing") {
            input "carportBeamPauseDuration", "number",
                  title: "Carport Beam Pause Duration (seconds)",
                  description: "How long to pause after beam break in Day mode",
                  defaultValue: 300,
                  range: "60..600",
                  required: false
            
            input "silentCarportTimeout", "number",
                  title: "Silent Carport Auto-Off (seconds)",
                  description: "How long silent carport stays on in Morning mode",
                  defaultValue: 120,
                  range: "30..300",
                  required: false
        }
        
        section("Ring Person Detection") {
            input "ringFrontDoor", "capability.switch",
                  title: "RPD Front Door Switch",
                  required: false
            
            input "ringBackDoor", "capability.switch",
                  title: "RPD Back Door Switch",
                  required: false
            
            input "ringBirdHouse", "capability.switch",
                  title: "RPD Bird House Switch",
                  required: false
            
            input "ringCPen", "capability.switch",
                  title: "RPD Chicken Pen Switch",
                  required: false
            
            input "ringGarden", "capability.switch",
                  title: "RPD Garden Switch",
                  required: false
            
            input "ringPersonTimeout", "number",
                  title: "Ring Person Detection Timeout (seconds)",
                  description: "How long to wait for person detection before alerting",
                  defaultValue: 30,
                  range: "10..300",
                  required: false
        }
        
        section("Mode-Based Behavior") {
            input "awayModes", "mode",
                  title: "Away Modes",
                  description: "Modes when enhanced security is active",
                  multiple: true,
                  required: false
            
            input "eveningMode", "mode",
                  title: "Evening Mode",
                  description: "Mode for evening-specific alerts",
                  required: false
        }
        
        section("Notification Settings") {
            input "notificationDevices", "capability.notification",
                  title: "Notification Devices",
                  description: "Devices to receive perimeter alerts",
                  multiple: true,
                  required: false
            
            input "alexaDevices", "capability.speechSynthesis",
                  title: "Alexa Devices for Announcements",
                  multiple: true,
                  required: false
        }
        
        section("Monitoring") {
            input "perimeterCheckInterval", "number",
                  title: "Perimeter Check Interval (minutes)",
                  description: "How often to check perimeter status (0 = disabled)",
                  defaultValue: 0,
                  range: "0..60",
                  required: false
        }
        
        section("Hub Variables Support") {
            paragraph "This app supports the following hub variables for dynamic configuration:"
            paragraph "• <b>GateAlertDelay</b> - Override gate alert delay (seconds)\n" +
                     "• <b>ShockSensitivity</b> - Override shock sensitivity (1-10)\n" +
                     "• <b>PerimeterCheckInterval</b> - Override check interval (minutes)\n" +
                     "• <b>AwayModeAlertEnabled</b> - Enable/disable away alerts (true/false)\n" +
                     "• <b>RingPersonTimeout</b> - Override Ring timeout (seconds)\n" +
                     "• <b>GunCabinetAlertEnabled</b> - Enable/disable cabinet alerts (true/false)\n" +
                     "• <b>CarportBeamPauseDuration</b> - Override beam pause duration (seconds)\n" +
                     "• <b>SilentCarportTimeout</b> - Override silent carport timeout (seconds)"
        }
        
        section("Logging") {
            input "logEnable", "bool",
                  title: "Enable Debug Logging",
                  defaultValue: false,
                  required: false
        }
    }
}

def installed() {
    logInfo "Perimeter Security Manager installed"
    initialize()
}

def updated() {
    logInfo "Perimeter Security Manager updated"
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    logInfo "Initializing Perimeter Security Manager"
    
    // Subscribe to gate sensors
    if (settings.frontGate) subscribe(settings.frontGate, "contact", gateHandler)
    if (settings.sideYardGate) subscribe(settings.sideYardGate, "contact", gateHandler)
    
    // Subscribe to shock sensors
    if (settings.rearGateShock) subscribe(settings.rearGateShock, "shock.detected", shockHandler)
    
    // Subscribe to additional sensors
    if (settings.outsidePenSensor) subscribe(settings.outsidePenSensor, "motion.active", motionHandler)
    if (settings.gunCabinet) subscribe(settings.gunCabinet, "contact.open", gunCabinetHandler)
    
    // Subscribe to carport beam
    if (settings.carportBeam) subscribe(settings.carportBeam, "contact", carportBeamHandler)
    
    // Subscribe to Ring devices (switches that turn on when person detected)
    if (settings.ringFrontDoor) subscribe(settings.ringFrontDoor, "switch.on", ringHandler)
    if (settings.ringBackDoor) subscribe(settings.ringBackDoor, "switch.on", ringHandler)
    if (settings.ringBirdHouse) subscribe(settings.ringBirdHouse, "switch.on", ringHandler)
    if (settings.ringCPen) subscribe(settings.ringCPen, "switch.on", ringHandler)
    if (settings.ringGarden) subscribe(settings.ringGarden, "switch.on", ringHandler)
    
    // Schedule perimeter checks if enabled
    def interval = getConfigValue("perimeterCheckInterval", "PerimeterCheckInterval") as Integer
    if (interval > 0) {
        schedule("0 */${interval} * * * ?", checkPerimeter)
        logInfo "Scheduled perimeter checks every ${interval} minutes"
    }
}

// ============================================================================
// EVENT HANDLERS
// ============================================================================

def gateHandler(evt) {
    def gateName = evt.displayName
    def state = evt.value
    
    logInfo "Gate event: ${gateName} - ${state}"
    
    if (state == "open") {
        handleGateOpen(evt.device, gateName)
    } else {
        handleGateClose(evt.device, gateName)
    }
}

def shockHandler(evt) {
    def sensorName = evt.displayName
    logInfo "Shock detected: ${sensorName}"
    
    def sensitivity = getConfigValue("shockSensitivity", "ShockSensitivity") as Integer
    
    // Check if shock is significant enough to alert
    if (sensitivity >= 5) {
        sendAlert("⚠️ Shock detected on ${sensorName}")
        announceAlexa("Alert! Shock detected on rear gate!")
    } else {
        logDebug "Shock detected but below sensitivity threshold"
    }
}

def motionHandler(evt) {
    def sensorName = evt.displayName
    logInfo "Motion detected: ${sensorName}"
    
    if (isAwayMode()) {
        sendAlert("🚨 Motion detected: ${sensorName} (Away Mode)")
        announceAlexa("Alert! Motion detected at ${sensorName}")
    }
}

def gunCabinetHandler(evt) {
    def enabled = getConfigValue("gunCabinetAlertEnabled", "GunCabinetAlertEnabled", true)
    
    if (enabled) {
        logInfo "Gun cabinet opened!"
        sendAlert("🔓 Gun Cabinet Opened")
        announceAlexa("Alert! Gun cabinet has been opened!")
    }
}

def ringHandler(evt) {
    def location = evt.displayName
    logInfo "Ring person detected: ${location}"
    
    def currentMode = location.currentMode.toString()
    
    // Special handling for garden in evening mode
    if (evt.device == settings.ringGarden && currentMode == settings.eveningMode?.toString()) {
        logInfo "Ring Garden person detected in evening mode"
        sendAlert("🔔 Person detected at garden (Evening)")
        return
    }
    
    // General person detection
    sendAlert("🔔 Person detected: ${location}")
    announceAlexa("Person detected at ${location}")
}

// ============================================================================
// CORE LOGIC
// ============================================================================

def handleGateOpen(device, gateName) {
    def delay = getConfigValue("gateAlertDelaySeconds", "GateAlertDelay") as Integer
    
    if (isAwayMode()) {
        // Immediate alert in away mode
        sendAlert("🚨 ${gateName} opened (AWAY MODE)")
        announceAlexa("Alert! ${gateName} has been opened!")
    } else {
        // Delayed alert in home mode
        logDebug "Scheduling gate alert for ${gateName} in ${delay} seconds"
        runIn(delay, delayedGateAlert, [data: [gate: gateName, deviceId: device.id]])
    }
}

def handleGateClose(device, gateName) {
    // Cancel any pending delayed alerts for this gate
    unschedule("delayedGateAlert_${device.id}")
    logInfo "${gateName} closed"
}

def delayedGateAlert(data) {
    def gateName = data.gate
    logInfo "Gate alert timeout: ${gateName} remained open"
    sendAlert("⏰ ${gateName} has been open for extended period")
}

def checkPerimeter() {
    logDebug "Running perimeter check"
    
    def openGates = []
    
    if (settings.frontGate?.currentValue("contact") == "open") {
        openGates << "Front Gate"
    }
    if (settings.sideYardGate?.currentValue("contact") == "open") {
        openGates << "Side Yard Gate"
    }
    
    if (openGates.size() > 0) {
        logInfo "Perimeter check found open gates: ${openGates.join(', ')}"
        
        if (isAwayMode()) {
            sendAlert("⚠️ Perimeter Check: Gates open - ${openGates.join(', ')}")
        }
    } else {
        logDebug "Perimeter check: All gates closed"
    }
}

def isAwayMode() {
    if (!settings.awayModes) return false
    
    def currentMode = location.currentMode.toString()
    def isAway = settings.awayModes.contains(currentMode)
    
    // Also check hub variable override
    def awayAlertEnabled = getConfigValue("awayModeAlertEnabled", "AwayModeAlertEnabled", true)
    
    return isAway && awayAlertEnabled
}

// ============================================================================
// CARPORT BEAM HANDLERS
// ============================================================================

def carportBeamHandler(evt) {
    String mode = location.mode
    logDebug "Carport beam event: ${evt.value}, Mode: ${mode}"
    
    // Beam broken = contact closed (reversed wiring)
    if (evt.value == "closed") {
        handleCarportBeamBroken(mode)
    }
}

def handleCarportBeamBroken(String mode) {
    logInfo "Carport beam broken in ${mode} mode"
    
    switch(mode) {
        case "Away":
            handleBeamAway()
            break
        case "Day":
            handleBeamDay()
            break
        case "Evening":
            handleBeamEvening()
            break
        case "Morning":
            handleBeamMorning()
            break
        default:
            logDebug "No carport beam action for mode: ${mode}"
    }
}

def handleBeamAway() {
    // Check if motion is detected for verification
    if (settings.carportMotion && settings.carportMotion.currentValue("motion") == "active") {
        logInfo "Away mode: Beam + motion detected"
        sendAlert("🚨 Alert: Carport Beam Broken (Away Mode)")
        announceAlexa("Alert! Carport beam broken!")
    } else {
        logDebug "Away mode: Beam broken but no motion verification"
    }
}

def handleBeamDay() {
    // Check silent switches
    if (isSwitchOn(settings.silentSwitch) || isSwitchOn(settings.silentCarport) || isSwitchOn(settings.pauseCarportBeam)) {
        logDebug "Day mode: Silent or paused, skipping beam action"
        return
    }
    
    // Check for motion verification
    Boolean motionDetected = false
    if (settings.carportMotion && settings.carportMotion.currentValue("motion") == "active") {
        motionDetected = true
    }
    if (settings.frontDoorRingMotion && settings.frontDoorRingMotion.currentValue("switch") == "on") {
        motionDetected = true
    }
    
    if (!motionDetected) {
        logDebug "Day mode: Beam broken but no motion detected"
        return
    }
    
    logInfo "Day mode: Beam + motion detected"
    
    // Activate pause switch to prevent repeated alerts
    settings.pauseCarportBeam?.on()
    
    // Schedule auto-off
    Integer delay = getConfigValue("carportBeamPauseDuration", "CarportBeamPauseDuration") as Integer
    runIn(delay, turnOffPauseCarportBeam)
    
    // Send notification
    sendAlert("🔔 Carport Beam Broken")
}

def handleBeamEvening() {
    if (isSwitchOn(settings.silentSwitch)) {
        logDebug "Evening mode: Silent is on, skipping"
        return
    }
    
    logInfo "Evening mode: Beam broken"
    sendAlert("🔔 Carport Beam Broken")
    announceAlexa("Carport beam broken")
}

def handleBeamMorning() {
    if (isSwitchOn(settings.silentSwitch) || isSwitchOn(settings.silentCarport)) {
        logDebug "Morning mode: Silent is on, skipping"
        return
    }
    
    logInfo "Morning mode: Intruder alert - beam broken"
    
    // Activate silent carport to prevent repeated alerts
    settings.silentCarport?.on()
    
    // Schedule auto-off
    Integer timeout = getConfigValue("silentCarportTimeout", "SilentCarportTimeout") as Integer
    runIn(timeout, turnOffSilentCarport)
    
    // Send notification - more urgent message for morning
    sendAlert("🚨 Intruder in the carport!")
    announceAlexa("Alert! Intruder in the carport!")
}

def turnOffPauseCarportBeam() {
    logDebug "Auto-turning off pause carport beam"
    settings.pauseCarportBeam?.off()
}

def turnOffSilentCarport() {
    logDebug "Auto-turning off silent carport"
    settings.silentCarport?.off()
}

/**
 * Check if a switch is on
 */
def isSwitchOn(device) {
    return device?.currentValue("switch") == "on"
}

// ============================================================================
// NOTIFICATION METHODS
// ============================================================================

def sendAlert(String message) {
    logInfo "Sending alert: ${message}"
    
    settings.notificationDevices?.each { device ->
        device.deviceNotification(message)
    }
}

def announceAlexa(String message) {
    if (!settings.alexaDevices) return
    
    logDebug "Alexa announcement: ${message}"
    
    settings.alexaDevices?.each { alexa ->
        alexa.speak(message)
    }
}

// ============================================================================
// HELPER METHODS
// ============================================================================

/**
 * Get configuration value from hub variable or fall back to app setting
 */
def getConfigValue(String settingName, String hubVarName, defaultValue = null) {
    try {
        def hubVar = getGlobalVar(hubVarName)
        if (hubVar?.value != null) {
            logDebug "Using hub variable ${hubVarName}: ${hubVar.value}"
            return convertValue(hubVar.value, hubVar.type)
        }
    } catch (Exception e) {
        logDebug "Hub variable '${hubVarName}' not found: ${e.message}"
    }
    
    // Fall back to app setting
    def settingValue = settings[settingName]
    if (settingValue != null) {
        logDebug "Using app setting ${settingName}: ${settingValue}"
        return settingValue
    }
    
    // Use default if provided
    if (defaultValue != null) {
        logDebug "Using default value: ${defaultValue}"
        return defaultValue
    }
    
    return null
}

/**
 * Convert hub variable value to appropriate type
 */
def convertValue(value, type) {
    switch(type?.toLowerCase()) {
        case "number":
            return value as Integer
        case "decimal":
            return value as BigDecimal
        case "boolean":
            return value.toString().toBoolean()
        case "string":
        default:
            return value.toString()
    }
}

// ============================================================================
// LOGGING
// ============================================================================

def logInfo(String msg) {
    log.info "[Perimeter Security Manager] ${msg}"
}

def logDebug(String msg) {
    if (settings.logEnable) {
        log.debug "[Perimeter Security Manager] ${msg}"
    }
}

def logWarn(String msg) {
    log.warn "[Perimeter Security Manager] ${msg}"
}

def logError(String msg) {
    log.error "[Perimeter Security Manager] ${msg}"
}
