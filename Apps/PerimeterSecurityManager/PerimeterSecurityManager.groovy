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
        section("<b>═══════════════════════════════════════</b>\n<b>GATE SENSORS</b>\n<b>═══════════════════════════════════════</b>") {
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
        
        section("<b>═══════════════════════════════════════</b>\n<b>SHOCK & TAMPER SENSORS</b>\n<b>═══════════════════════════════════════</b>") {
            input "rearGateShock", "capability.shockSensor",
                  title: "Rear Gate Shock Sensor",
                  required: false
            
            input "shockSensitivity", "number",
                  title: "Shock Sensitivity (1-10)",
                  description: "Higher = more sensitive",
                  defaultValue: 5,
                  range: "1..10",
                  required: false
            
            input "shockPersonCorrelationSeconds", "number",
                  title: "Shock + Person Detection Window (seconds)",
                  description: "Time window to correlate shock with Ring CPen person detection",
                  defaultValue: 20,
                  range: "5..120",
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>ADDITIONAL PERIMETER SENSORS</b>\n<b>═══════════════════════════════════════</b>") {
            input "outsidePenSensor", "capability.motionSensor",
                  title: "Outside Pen Motion Sensor",
                  required: false
            
            input "gunCabinet", "capability.contactSensor",
                  title: "Gun Cabinet Sensor",
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>CARPORT BEAM</b>\n<b>═══════════════════════════════════════</b>") {
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
        
        section("<b>═══════════════════════════════════════</b>\n<b>CARPORT BEAM CONDITION SWITCHES</b>\n<b>═══════════════════════════════════════</b>") {
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
        
        section("<b>═══════════════════════════════════════</b>\n<b>CARPORT BEAM TIMING</b>\n<b>═══════════════════════════════════════</b>") {
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
        
        section("<b>═══════════════════════════════════════</b>\n<b>RING PERSON DETECTION</b>\n<b>═══════════════════════════════════════</b>") {
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
        
        section("<b>═══════════════════════════════════════</b>\n<b>MODE-BASED BEHAVIOR</b>\n<b>═══════════════════════════════════════</b>") {
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
        
        section("<b>═══════════════════════════════════════</b>\n<b>NOTIFICATION SETTINGS</b>\n<b>═══════════════════════════════════════</b>") {
            input "notificationDevices", "capability.notification",
                  title: "Notification Devices",
                  description: "Devices to receive perimeter alerts",
                  multiple: true,
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>PERIMETER CHECK CONFIGURATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "enablePerimeterChecks", "bool",
                  title: "Enable Perimeter Checks",
                  description: "Enable/disable scheduled perimeter monitoring",
                  defaultValue: false,
                  required: false
            
            input "perimeterCheckMode", "enum",
                  title: "Check Schedule Mode",
                  description: "Choose how to schedule perimeter checks",
                  options: [
                      "interval": "Hourly Interval",
                      "timeRange": "Start/End Time"
                  ],
                  defaultValue: "interval",
                  required: false
            
            input "perimeterCheckInterval", "number",
                  title: "Check Interval (hours)",
                  description: "How often to check perimeter status (1-24 hours)",
                  defaultValue: 1,
                  range: "1..24",
                  required: false,
                  submitOnChange: true
            
            input "perimeterCheckStartTime", "time",
                  title: "Start Time",
                  description: "When to start perimeter checks",
                  required: false,
                  submitOnChange: true
            
            input "perimeterCheckEndTime", "time",
                  title: "End Time",
                  description: "When to stop perimeter checks",
                  required: false,
                  submitOnChange: true
            
            input "perimeterCheckFrequency", "number",
                  title: "Check Frequency During Time Range (minutes)",
                  description: "How often to check between start and end time",
                  defaultValue: 15,
                  range: "5..60",
                  required: false
            
            input "enableModeBasedChecks", "bool",
                  title: "Enable Mode-Based Checking",
                  description: "Only run checks during specific modes",
                  defaultValue: false,
                  required: false
            
            input "perimeterCheckModes", "mode",
                  title: "Active Modes for Perimeter Checks",
                  description: "Only check perimeter when in these modes",
                  multiple: true,
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>NIGHT MODE PREPARATION CHECK</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Schedule a specific check time to ensure all doors/gates are closed before night mode"
            
            input "enableNightPrepCheck", "bool",
                  title: "Enable Night Preparation Check",
                  description: "Run a check at a specific time before night mode",
                  defaultValue: false,
                  required: false
            
            input "nightPrepCheckTime", "time",
                  title: "Night Preparation Check Time",
                  description: "When to verify all perimeter devices are closed",
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>DEVICES TO MONITOR IN PERIMETER CHECKS</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Select which devices should be included in periodic perimeter checks:"
            
            input "checkFrontGate", "bool",
                  title: "Monitor Front Gate",
                  defaultValue: true,
                  required: false
            
            input "checkSideYardGate", "bool",
                  title: "Monitor Side Yard Gate",
                  defaultValue: true,
                  required: false
            
            input "checkGunCabinet", "bool",
                  title: "Monitor Gun Cabinet",
                  defaultValue: true,
                  required: false
            
            input "additionalContactSensors", "capability.contactSensor",
                  title: "Additional Contact Sensors to Monitor",
                  description: "Add any other doors or windows to check",
                  multiple: true,
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>HUB VARIABLES SUPPORT</b>\n<b>═══════════════════════════════════════</b>") {
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
        
        section("<b>═══════════════════════════════════════</b>\n<b>LOGGING</b>\n<b>═══════════════════════════════════════</b>") {
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
    schedulePerimeterChecks()
}

/**
 * Schedule perimeter checks based on user configuration
 */
def schedulePerimeterChecks() {
    // Clear any existing schedules
    unschedule(checkPerimeter)
    unschedule(checkPerimeterIfInTimeRange)
    unschedule(nightPreparationCheck)
    
    // Schedule night preparation check if enabled
    if (settings.enableNightPrepCheck && settings.nightPrepCheckTime) {
        schedule(settings.nightPrepCheckTime, nightPreparationCheck)
        logInfo "Scheduled night preparation check at ${settings.nightPrepCheckTime}"
    }
    
    // Check if perimeter checks are enabled
    if (!settings.enablePerimeterChecks) {
        logInfo "Perimeter checks are disabled"
        return
    }
    
    def mode = settings.perimeterCheckMode ?: "interval"
    
    if (mode == "interval") {
        // Hourly interval mode
        def hours = settings.perimeterCheckInterval ?: 1
        schedule("0 0 */${hours} * * ?", checkPerimeterScheduled)
        logInfo "Scheduled perimeter checks every ${hours} hour(s)"
    } else if (mode == "timeRange") {
        // Start/End time mode
        if (!settings.perimeterCheckStartTime || !settings.perimeterCheckEndTime) {
            logWarn "Start/End time mode selected but times not configured"
            return
        }
        
        def frequency = settings.perimeterCheckFrequency ?: 15
        schedule("0 */${frequency} * * * ?", checkPerimeterIfInTimeRange)
        logInfo "Scheduled perimeter checks every ${frequency} minutes during active time range"
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
    
    // Check if shock is significant enough to process
    if (sensitivity < 5) {
        logDebug "Shock detected but below sensitivity threshold"
        return
    }
    
    // Get the correlation window from app settings
    def correlationWindow = (settings.shockPersonCorrelationSeconds ?: 20) as Integer
    
    if (wasRingCPenRecentlyTriggered(correlationWindow)) {
        // Person was detected at chicken pen within the correlation window
        logInfo "Shock + person correlation confirmed - sending alert"
        sendAlert("Alert: Rear gate shock detected with person at chicken pen")
    } else {
        // No person detected yet - store shock event and wait for person detection
        logInfo "Shock detected, waiting for person detection at chicken pen within ${correlationWindow} seconds"
        state.lastShockTime = now()
        state.lastShockSensor = sensorName
        
        // Schedule a check in case person is detected shortly after
        runIn(correlationWindow, clearPendingShockAlert)
    }
}

/**
 * Check if Ring CPen was triggered within the correlation window
 */
def wasRingCPenRecentlyTriggered(Integer windowSeconds) {
    if (!settings.ringCPen) {
        logDebug "Ring CPen not configured"
        return false
    }
    
    // Check if the switch is currently on (recently triggered)
    if (settings.ringCPen.currentValue("switch") == "on") {
        logDebug "Ring CPen is currently on - person detected"
        return true
    }
    
    // Check if we have a stored recent trigger time
    if (state.lastRingCPenTime) {
        def elapsedSeconds = (now() - state.lastRingCPenTime) / 1000
        if (elapsedSeconds <= windowSeconds) {
            logDebug "Ring CPen was triggered ${elapsedSeconds} seconds ago - within window"
            return true
        }
    }
    
    return false
}

/**
 * Clear pending shock alert if no person was detected in time
 */
def clearPendingShockAlert() {
    if (state.lastShockTime) {
        logDebug "Clearing pending shock alert - no person detected within correlation window"
        state.remove("lastShockTime")
        state.remove("lastShockSensor")
    }
}

def motionHandler(evt) {
    def sensorName = evt.displayName
    logInfo "Motion detected: ${sensorName}"
    
    if (isAwayMode()) {
        sendAlert("Alert: Motion detected outside chicken pen")
    }
}

def gunCabinetHandler(evt) {
    def enabled = getConfigValue("gunCabinetAlertEnabled", "GunCabinetAlertEnabled", true)
    
    if (enabled) {
        logInfo "Gun cabinet opened!"
        sendAlert("Alert: Gun cabinet has been opened")
    }
}

def ringHandler(evt) {
    def locationName = evt.displayName
    logInfo "Ring person detected: ${locationName}"
    
    def currentMode = location.currentMode.toString()
    
    // Track Ring CPen triggers for shock correlation
    if (evt.device == settings.ringCPen) {
        state.lastRingCPenTime = now()
        
        // Check if we have a pending shock alert to correlate
        if (state.lastShockTime) {
            def correlationWindow = (settings.shockPersonCorrelationSeconds ?: 30) as Integer
            def elapsedSeconds = (now() - state.lastShockTime) / 1000
            
            if (elapsedSeconds <= correlationWindow) {
                logInfo "Person at chicken pen + recent shock - sending correlated alert"
                sendAlert("Alert: Rear gate shock detected with person at chicken pen")
                
                // Clear the pending shock
                state.remove("lastShockTime")
                state.remove("lastShockSensor")
                unschedule("clearPendingShockAlert")
                return
            }
        }
    }
    
    // Special handling for garden in evening mode
    if (evt.device == settings.ringGarden && currentMode == settings.eveningMode?.toString()) {
        logInfo "Ring Garden person detected in evening mode"
        sendAlert("Person detected in the garden")
        return
    }
    
    // General person detection - make location names more readable
    def readableLocation = getReadableRingLocation(evt.device)
    sendAlert("Person detected ${readableLocation}")
}

/**
 * Convert Ring device to readable location name
 */
def getReadableRingLocation(device) {
    if (device == settings.ringFrontDoor) return "at the front door"
    if (device == settings.ringBackDoor) return "at the back door"
    if (device == settings.ringBirdHouse) return "near the bird house"
    if (device == settings.ringCPen) return "at the chicken pen"
    if (device == settings.ringGarden) return "in the garden"
    return "on the property"
}

// ============================================================================
// CORE LOGIC
// ============================================================================

def handleGateOpen(device, gateName) {
    def delay = getConfigValue("gateAlertDelaySeconds", "GateAlertDelay") as Integer
    
    // Determine readable gate name
    def readableGate = getReadableGateName(device)
    
    if (isAwayMode()) {
        // Immediate alert in away mode
        sendAlert("Alert: ${readableGate} has been opened while away")
    } else {
        // Delayed alert in home mode
        logDebug "Scheduling gate alert for ${gateName} in ${delay} seconds"
        runIn(delay, delayedGateAlert, [data: [gate: readableGate, deviceId: device.id]])
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
    sendAlert("${gateName} has been left open")
}

/**
 * Convert gate device to readable name
 */
def getReadableGateName(device) {
    if (device == settings.frontGate) return "The front gate"
    if (device == settings.sideYardGate) return "The side yard gate"
    return "A gate"
}

/**
 * Check if current time is within the configured time range
 */
def checkPerimeterIfInTimeRange() {
    if (!isWithinTimeRange()) {
        logDebug "Current time is outside perimeter check time range"
        return
    }
    
    checkPerimeterScheduled()
}

/**
 * Scheduled perimeter check that respects mode restrictions
 */
def checkPerimeterScheduled() {
    // Check if mode-based checking is enabled
    if (settings.enableModeBasedChecks && settings.perimeterCheckModes) {
        def currentMode = location.currentMode.toString()
        if (!settings.perimeterCheckModes.contains(currentMode)) {
            logDebug "Skipping perimeter check - current mode (${currentMode}) not in active modes"
            return
        }
    }
    
    checkPerimeter()
}

/**
 * Night preparation check - always runs regardless of mode restrictions
 */
def nightPreparationCheck() {
    logInfo "Running night preparation check"
    
    def openDevices = []
    
    // Check front gate if enabled
    if (settings.checkFrontGate && settings.frontGate?.currentValue("contact") == "open") {
        openDevices << "front gate"
    }
    
    // Check side yard gate if enabled
    if (settings.checkSideYardGate && settings.sideYardGate?.currentValue("contact") == "open") {
        openDevices << "side yard gate"
    }
    
    // Check gun cabinet if enabled
    if (settings.checkGunCabinet && settings.gunCabinet?.currentValue("contact") == "open") {
        openDevices << "gun cabinet"
    }
    
    // Check additional contact sensors - use their display names
    settings.additionalContactSensors?.each { sensor ->
        if (sensor.currentValue("contact") == "open") {
            openDevices << sensor.displayName.toLowerCase()
        }
    }
    
    if (openDevices.size() > 0) {
        logInfo "Night preparation check found open devices: ${openDevices.join(', ')}"
        def deviceList = formatDeviceList(openDevices)
        sendAlert("Night check: ${deviceList} still open")
    } else {
        logInfo "Night preparation check: All perimeter devices secured"
    }
}

/**
 * Check if current time is between start and end time
 */
def isWithinTimeRange() {
    if (!settings.perimeterCheckStartTime || !settings.perimeterCheckEndTime) {
        return false
    }
    
    def now = new Date()
    def startTime = timeToday(settings.perimeterCheckStartTime, location.timeZone)
    def endTime = timeToday(settings.perimeterCheckEndTime, location.timeZone)
    
    // Handle case where end time is on the next day
    if (endTime < startTime) {
        return now >= startTime || now <= endTime
    } else {
        return now >= startTime && now <= endTime
    }
}

def checkPerimeter() {
    logDebug "Running perimeter check"
    
    def openDevices = []
    
    // Check front gate if enabled
    if (settings.checkFrontGate && settings.frontGate?.currentValue("contact") == "open") {
        openDevices << "front gate"
    }
    
    // Check side yard gate if enabled
    if (settings.checkSideYardGate && settings.sideYardGate?.currentValue("contact") == "open") {
        openDevices << "side yard gate"
    }
    
    // Check gun cabinet if enabled
    if (settings.checkGunCabinet && settings.gunCabinet?.currentValue("contact") == "open") {
        openDevices << "gun cabinet"
    }
    
    // Check additional contact sensors - use their display names
    settings.additionalContactSensors?.each { sensor ->
        if (sensor.currentValue("contact") == "open") {
            openDevices << sensor.displayName.toLowerCase()
        }
    }
    
    if (openDevices.size() > 0) {
        logInfo "Perimeter check found open devices: ${openDevices.join(', ')}"
        
        def deviceList = formatDeviceList(openDevices)
        if (isAwayMode()) {
            sendAlert("Perimeter check: ${deviceList} still open")
        } else {
            // Send notification even in home mode for perimeter checks
            sendAlert("Reminder: ${deviceList} open")
        }
    } else {
        logDebug "Perimeter check: All monitored devices closed"
    }
}

/**
 * Format list of devices into readable text
 */
def formatDeviceList(List devices) {
    if (devices.size() == 1) {
        return "the ${devices[0]} is"
    } else if (devices.size() == 2) {
        return "the ${devices[0]} and ${devices[1]} are"
    } else {
        def lastDevice = devices[-1]
        def otherDevices = devices[0..-2]
        return "the ${otherDevices.join(', ')}, and ${lastDevice} are"
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
    // Check if motion or Ring person is detected for verification (to avoid false positives from animals)
    Boolean verified = false
    if (settings.carportMotion && settings.carportMotion.currentValue("motion") == "active") {
        verified = true
    }
    if (settings.frontDoorRingMotion && settings.frontDoorRingMotion.currentValue("switch") == "on") {
        verified = true
    }
    
    if (verified) {
        logInfo "Away mode: Beam + motion/person detected"
        sendAlert("Alert: Someone in the carport")
    } else {
        logDebug "Away mode: Beam broken but no motion/person verification - avoiding false positive"
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
    sendAlert("Someone in the carport")
}

def handleBeamEvening() {
    if (isSwitchOn(settings.silentSwitch)) {
        logDebug "Evening mode: Silent is on, skipping"
        return
    }
    
    // Check for motion or Ring person verification (to avoid false positives from animals)
    Boolean verified = false
    if (settings.carportMotion && settings.carportMotion.currentValue("motion") == "active") {
        verified = true
    }
    if (settings.frontDoorRingMotion && settings.frontDoorRingMotion.currentValue("switch") == "on") {
        verified = true
    }
    
    if (!verified) {
        logDebug "Evening mode: Beam broken but no motion/person verification - avoiding false positive"
        return
    }
    
    // Check cooldown (use pause switch for consistency)
    if (isSwitchOn(settings.pauseCarportBeam)) {
        logDebug "Evening mode: In cooldown period, skipping"
        return
    }
    
    logInfo "Evening mode: Beam + motion/person verified"
    
    // Activate pause for cooldown
    settings.pauseCarportBeam?.on()
    Integer delay = getConfigValue("carportBeamPauseDuration", "CarportBeamPauseDuration") as Integer
    runIn(delay, turnOffPauseCarportBeam)
    
    sendAlert("Someone in the carport")
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
    sendAlert("Alert: Intruder in the carport!")
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
