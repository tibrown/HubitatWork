/**
 *  Emergency Help Manager
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
    name: "Emergency Help Manager",
    namespace: "hubitat",
    author: "Tim Brown",
    description: "Manages emergency help buttons and assistance requests throughout the home. Provides visual and audio alerts, repeating notifications, and location-specific help coordination.",
    category: "Safety & Security",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    singleThreaded: true
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Emergency Help Manager", install: true, uninstall: true) {
        section("Shower Help Configuration") {
            input "showerHelpButton", "capability.pushableButton", title: "Shower Help Button", required: false
            input "stopShowerHelpSwitch", "capability.switch", title: "Stop Shower Help Switch", required: false
            input "deskButton", "capability.pushableButton", title: "Desk Button (to stop alerts)", required: false
        }
        
        section("Other Help Buttons") {
            input "keyFobButton", "capability.pushableButton", title: "Key Fob Help Button", required: false
            input "birdHouseButton", "capability.pushableButton", title: "Bird House Help Button (NanoMote)", required: false
        }
        
        section("Visual Alert Devices") {
            input "flashLights", "capability.switch", title: "Lights to Flash for Alerts", multiple: true, required: false
            input "deskLight", "capability.switchLevel", title: "Desk Light (for color/dimming)", required: false
        }
        
        section("Silent Mode Switches") {
            input "silentSwitch", "capability.switch", title: "Silent Mode Switch", required: false
            input "silentCarportSwitch", "capability.switch", title: "Silent Carport Switch", required: false
        }
        
        section("Alarm Integration") {
            input "alarmTriggerSwitch", "capability.switch", title: "Alarm Trigger Switch (to trigger alarms)", required: false
        }
        
        section("Alert Configuration") {
            input "helpAlertDuration", "number", title: "Help Alert Duration (seconds)", defaultValue: 300, required: false
            input "repeatCount", "number", title: "Alert Repeat Count", defaultValue: 5, required: false
            input "repeatInterval", "number", title: "Alert Repeat Interval (seconds)", defaultValue: 300, required: false
            input "flashRate", "number", title: "Light Flash Rate (flashes per second)", defaultValue: 2, required: false
            input "silentModeAutoOffTime", "time", title: "Time to Automatically Turn Off Silent Mode", required: false
        }
        
        section("Notifications") {
            input "notificationDevices", "capability.notification", title: "Notification Devices", multiple: true, required: false
        }
        
        section("Hub Variable Overrides") {
            paragraph "This app supports hub variable overrides for flexible configuration:"
            paragraph "• helpAlertDuration - Override help alert duration (seconds)"
            paragraph "• flashRate - Override light flash rate (flashes per second)"
            paragraph "• emergencyVolume - Override emergency siren volume (0-100)"
            paragraph "• silentModeTimeout - Override silent mode timeout (minutes)"
            paragraph "• notificationDelay - Override notification delay (seconds)"
            paragraph "• visualOnlyMode - Enable visual-only alerts (boolean: true/false)"
        }
        
        section("Logging") {
            input "logEnable", "bool", title: "Enable debug logging", defaultValue: true
            input "infoEnable", "bool", title: "Enable info logging", defaultValue: true
        }
    }
}

def installed() {
    logInfo "Emergency Help Manager installed"
    initialize()
}

def updated() {
    logInfo "Emergency Help Manager updated"
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    logInfo "Initializing Emergency Help Manager"
    
    // Subscribe to shower help button
    if (showerHelpButton) {
        subscribe(showerHelpButton, "pushed", handleShowerHelpButton)
        subscribe(showerHelpButton, "held", handleShowerHelpButton)
        subscribe(showerHelpButton, "doubleTapped", handleShowerHelpButton)
    }
    
    // Subscribe to stop shower help switch
    if (stopShowerHelpSwitch) {
        subscribe(stopShowerHelpSwitch, "switch.on", handleStopShowerHelp)
    }
    
    // Subscribe to desk button (for stopping alerts)
    if (deskButton) {
        subscribe(deskButton, "held", handleDeskButtonHeld)
    }
    
    // Subscribe to key fob help button
    if (keyFobButton) {
        subscribe(keyFobButton, "pushed", handleKeyFobHelp)
    }
    
    // Subscribe to bird house help button
    if (birdHouseButton) {
        subscribe(birdHouseButton, "pushed", handleBirdHouseHelp)
        subscribe(birdHouseButton, "held", handleBirdHouseHelp)
    }
    
    // Schedule silent mode auto-off
    if (silentModeAutoOffTime) {
        schedule(silentModeAutoOffTime, turnOffSilentMode)
    }
    
    // Initialize state
    state.showerHelpActive = false
    state.repeatCount = 0
}

// ========================================
// SHOWER HELP HANDLERS
// ========================================

def handleShowerHelpButton(evt) {
    logInfo "Shower help button activated: ${evt.value}"
    
    // Check if stop switch is off (help not cancelled)
    if (stopShowerHelpSwitch?.currentValue("switch") == "on") {
        logDebug "Shower help cancelled, skipping activation"
        return
    }
    
    // Start repeating alert sequence
    state.showerHelpActive = true
    state.repeatCount = 0
    
    Integer repeats = getConfigValue("repeatCount", "helpAlertRepeat") as Integer
    Integer interval = getConfigValue("repeatInterval", "helpAlertInterval") as Integer
    
    logInfo "Starting shower help alert sequence (${repeats} repetitions every ${interval} seconds)"
    
    // Execute first alert immediately
    executeShowerHelp()
    
    // Schedule remaining repetitions
    for (int i = 1; i < repeats; i++) {
        runIn(i * interval, executeShowerHelp)
    }
}

def executeShowerHelp() {
    // Check if help was cancelled
    if (stopShowerHelpSwitch?.currentValue("switch") == "on") {
        logDebug "Shower help cancelled, stopping execution"
        state.showerHelpActive = false
        return
    }
    
    if (!state.showerHelpActive) {
        logDebug "Shower help not active, skipping execution"
        return
    }
    
    state.repeatCount = (state.repeatCount ?: 0) + 1
    logInfo "Executing shower help alert (repetition ${state.repeatCount})"
    
    // Trigger alarm sounds via Security Alarm Manager
    triggerAlarmSound()
    
    // Flash lights for visual alert
    flashLightsForHelp()
    
    // Set desk light to red for emergency indicator
    setDeskLightRed()
    
    // Send notifications
    String message = state.repeatCount > 1 ? 
        "Help Needed In The Shower Still" : 
        "Help Needed In The Shower"
    
    sendNotification(message)
    sendAlexaMessage("Alert! ${message}! Alert! ${message}!")
}

def handleStopShowerHelp(evt) {
    logInfo "Shower help stopped"
    
    state.showerHelpActive = false
    unschedule(executeShowerHelp)
    
    // Set desk light to dimmest level
    deskLight?.setLevel(1)
    
    // Auto-reset stop switch after 5 minutes
    runIn(300, resetStopShowerHelpSwitch)
}

def handleDeskButtonHeld(evt) {
    logInfo "Desk button held - stopping shower alert"
    
    stopShowerHelpSwitch?.on()
}

def resetStopShowerHelpSwitch() {
    logDebug "Resetting stop shower help switch"
    stopShowerHelpSwitch?.off()
}

// ========================================
// KEY FOB HELP HANDLER
// ========================================

def handleKeyFobHelp(evt) {
    logInfo "Key fob help button pressed (button ${evt.value})"
    
    String message = "Help Needed, keyfob pressed"
    sendNotification(message)
    
    // Trigger visual alerts
    flashLightsForHelp()
}

// ========================================
// BIRD HOUSE HELP HANDLER
// ========================================

def handleBirdHouseHelp(evt) {
    logInfo "Bird house help button activated: ${evt.value}"
    
    // Flash specific lights
    flashLightsForHelp()
    
    // Send repeating notifications
    Integer repeats = 3
    Integer interval = 30 // seconds
    
    for (int i = 0; i < repeats; i++) {
        runIn(i * interval, sendBirdHouseHelpNotification)
    }
}

def sendBirdHouseHelpNotification() {
    String message = "Help Needed in the Birdhouse, help needed in the birdhouse"
    sendNotification(message)
}

// ========================================
// SILENT MODE MANAGEMENT
// ========================================

def turnOffSilentMode() {
    logInfo "Turning off silent mode (scheduled)"
    
    silentSwitch?.off()
    silentCarportSwitch?.off()
}

// ========================================
// VISUAL ALERT METHODS
// ========================================

def flashLightsForHelp() {
    if (!flashLights) {
        logDebug "No flash lights configured"
        return
    }
    
    Integer rate = getConfigValue("flashRate", "flashRate") as Integer
    Integer duration = getConfigValue("helpAlertDuration", "helpAlertDuration") as Integer
    
    logDebug "Flashing ${flashLights.size()} lights at ${rate} flashes/sec for ${duration} seconds"
    
    // Flash lights
    flashLights.each { light ->
        light.flash()
    }
}

def setDeskLightRed() {
    if (!deskLight) {
        logDebug "No desk light configured"
        return
    }
    
    logDebug "Setting desk light to red"
    
    // Set color to red (hue: 0, saturation: 100)
    if (deskLight.hasCommand("setColor")) {
        deskLight.setColor([hue: 0, saturation: 100, level: 100])
    } else if (deskLight.hasCommand("setLevel")) {
        deskLight.setLevel(100)
    } else {
        deskLight.on()
    }
}

// ========================================
// ALARM INTEGRATION
// ========================================

def triggerAlarmSound() {
    if (!alarmTriggerSwitch) {
        logDebug "No alarm trigger switch configured"
        return
    }
    
    logDebug "Triggering alarm sound via Security Alarm Manager"
    alarmTriggerSwitch.on()
}

// ========================================
// NOTIFICATION METHODS
// ========================================

def sendNotification(String message) {
    if (notificationDevices) {
        logDebug "Sending notification: ${message}"
        notificationDevices.each { device ->
            device.deviceNotification(message)
        }
    }
}

def sendAlexaMessage(String message) {
    // Set global variable for TellAlexa integration
    setHubVar("EchoMessage", message)
    logDebug "Set Alexa message: ${message}"
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

// ========================================
// LOGGING
// ========================================

def logDebug(msg) {
    if (logEnable) log.debug "${app.label}: ${msg}"
}

def logInfo(msg) {
    if (infoEnable) log.info "${app.label}: ${msg}"
}
