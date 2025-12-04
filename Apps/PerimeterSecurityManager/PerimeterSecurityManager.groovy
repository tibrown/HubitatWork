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
            
            input "rearGate", "capability.contactSensor",
                  title: "Rear Gate Sensor",
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
        
        section("Ring Person Detection") {
            input "ringFrontDoor", "device.RingVirtualMotionSensor",
                  title: "Ring Front Door",
                  required: false
            
            input "ringBackDoor", "device.RingVirtualMotionSensor",
                  title: "Ring Back Door",
                  required: false
            
            input "ringBirdHouse", "device.RingVirtualMotionSensor",
                  title: "Ring Bird House",
                  required: false
            
            input "ringCPen", "device.RingVirtualMotionSensor",
                  title: "Ring Chicken Pen",
                  required: false
            
            input "ringGarden", "device.RingVirtualMotionSensor",
                  title: "Ring Garden",
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
                     "• <b>GunCabinetAlertEnabled</b> - Enable/disable cabinet alerts (true/false)"
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
    if (settings.rearGate) subscribe(settings.rearGate, "contact", gateHandler)
    if (settings.sideYardGate) subscribe(settings.sideYardGate, "contact", gateHandler)
    
    // Subscribe to shock sensors
    if (settings.rearGateShock) subscribe(settings.rearGateShock, "shock.detected", shockHandler)
    
    // Subscribe to additional sensors
    if (settings.outsidePenSensor) subscribe(settings.outsidePenSensor, "motion.active", motionHandler)
    if (settings.gunCabinet) subscribe(settings.gunCabinet, "contact.open", gunCabinetHandler)
    
    // Subscribe to Ring devices
    if (settings.ringFrontDoor) subscribe(settings.ringFrontDoor, "motion.active", ringHandler)
    if (settings.ringBackDoor) subscribe(settings.ringBackDoor, "motion.active", ringHandler)
    if (settings.ringBirdHouse) subscribe(settings.ringBirdHouse, "motion.active", ringHandler)
    if (settings.ringCPen) subscribe(settings.ringCPen, "motion.active", ringHandler)
    if (settings.ringGarden) subscribe(settings.ringGarden, "motion.active", ringHandler)
    
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
    if (settings.rearGate?.currentValue("contact") == "open") {
        openGates << "Rear Gate"
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
