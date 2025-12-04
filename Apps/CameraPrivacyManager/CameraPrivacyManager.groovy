/**
 *  CameraPrivacyManager
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
    name: "Camera Privacy Manager",
    namespace: "tibrown",
    author: "Tim Brown",
    description: "Manages indoor/outdoor camera power and privacy settings based on presence and mode",
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
    dynamicPage(name: "mainPage", title: "Camera Privacy Manager", install: true, uninstall: true) {
        section("Indoor Cameras") {
            input "indoorCameras", "capability.switch", 
                  title: "Indoor Camera Power Outlets", 
                  description: "Select switches that control indoor camera power",
                  multiple: true, 
                  required: true
            
            input "outdoorCameras", "capability.switch",
                  title: "Outdoor Camera Power Outlets (Optional)",
                  description: "Select switches that control outdoor camera power",
                  multiple: true,
                  required: false
        }
        
        section("Privacy Mode Settings") {
            input "homeModes", "mode",
                  title: "Privacy Modes (Cameras Off)",
                  description: "Select modes when indoor cameras should be OFF for privacy",
                  multiple: true,
                  required: true
            
            input "awayModes", "mode",
                  title: "Security Modes (Cameras On)",
                  description: "Select modes when cameras should be ON for security",
                  multiple: true,
                  required: true
            
            input "privacyDelayMinutes", "number",
                  title: "Privacy Mode Delay (minutes)",
                  description: "Delay before turning cameras off when arriving home",
                  defaultValue: 2,
                  range: "0..60",
                  required: false
            
            input "enableDelayMinutes", "number",
                  title: "Enable Delay (minutes)",
                  description: "Delay before turning cameras on when leaving",
                  defaultValue: 1,
                  range: "0..60",
                  required: false
        }
        
        section("Manual Override") {
            input "manualOverride", "capability.switch",
                  title: "Manual Override Switch (Optional)",
                  description: "Switch to manually control camera privacy",
                  required: false
            
            input "overrideDurationHours", "number",
                  title: "Manual Override Duration (hours)",
                  description: "How long manual override lasts before reverting to mode control",
                  defaultValue: 4,
                  range: "1..24",
                  required: false
        }
        
        section("Hub Variables Support") {
            paragraph "This app supports the following hub variables for dynamic configuration:"
            paragraph "• <b>PrivacyModeDelay</b> - Override camera off delay (minutes)\n" +
                     "• <b>EnableDelay</b> - Override camera on delay (minutes)\n" +
                     "• <b>ManualOverrideDuration</b> - Override manual override timeout (hours)"
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
    logInfo "Camera Privacy Manager installed"
    initialize()
}

def updated() {
    logInfo "Camera Privacy Manager updated"
    unsubscribe()
    initialize()
}

def initialize() {
    logInfo "Initializing Camera Privacy Manager"
    
    // Subscribe to mode changes
    subscribe(location, "mode", modeChangeHandler)
    
    // Subscribe to manual override switch if configured
    if (settings.manualOverride) {
        subscribe(settings.manualOverride, "switch", manualOverrideHandler)
    }
    
    // Check current state and apply
    checkPrivacyMode()
}

// ============================================================================
// EVENT HANDLERS
// ============================================================================

def modeChangeHandler(evt) {
    logInfo "Mode changed to: ${evt.value}"
    
    // Cancel any pending jobs
    unschedule(camerasOn)
    unschedule(camerasOff)
    
    // Handle mode change with delays
    handleModeChange(evt.value)
}

def manualOverrideHandler(evt) {
    logInfo "Manual override switch: ${evt.value}"
    
    if (evt.value == "on") {
        // Manual override ON - turn cameras off immediately
        camerasOff()
        
        // Schedule revert to automatic mode
        def duration = getConfigValue("overrideDurationHours", "ManualOverrideDuration") as Integer
        logInfo "Manual override active for ${duration} hours"
        runIn(duration * 3600, revertToAutomatic)
    } else {
        // Manual override OFF - return to mode-based control
        unschedule(revertToAutomatic)
        checkPrivacyMode()
    }
}

// ============================================================================
// CORE LOGIC
// ============================================================================

def handleModeChange(String newMode) {
    // Check if manual override is active
    if (settings.manualOverride?.currentValue("switch") == "on") {
        logInfo "Manual override active, ignoring mode change"
        return
    }
    
    if (settings.homeModes?.contains(newMode)) {
        // Privacy mode - turn cameras off with delay
        def delay = getConfigValue("privacyDelayMinutes", "PrivacyModeDelay") as Integer
        logInfo "Privacy mode activated, cameras will turn off in ${delay} minutes"
        runIn(delay * 60, camerasOff)
    } else if (settings.awayModes?.contains(newMode)) {
        // Security mode - turn cameras on with delay
        def delay = getConfigValue("enableDelayMinutes", "EnableDelay") as Integer
        logInfo "Security mode activated, cameras will turn on in ${delay} minutes"
        runIn(delay * 60, camerasOn)
    } else {
        logDebug "Mode ${newMode} not configured for camera control"
    }
}

def checkPrivacyMode() {
    // Check if manual override is active
    if (settings.manualOverride?.currentValue("switch") == "on") {
        logInfo "Manual override active, cameras remain off"
        camerasOff()
        return
    }
    
    // Get current mode
    def currentMode = location.currentMode.toString()
    
    if (settings.homeModes?.contains(currentMode)) {
        logInfo "Currently in privacy mode (${currentMode})"
        camerasOff()
    } else if (settings.awayModes?.contains(currentMode)) {
        logInfo "Currently in security mode (${currentMode})"
        camerasOn()
    } else {
        logDebug "Current mode (${currentMode}) not configured for camera control"
    }
}

def camerasOn() {
    logInfo "Turning cameras ON"
    
    // Turn on all configured cameras
    settings.indoorCameras?.each { camera ->
        camera.on()
        logDebug "Turned on: ${camera.displayName}"
    }
    
    settings.outdoorCameras?.each { camera ->
        camera.on()
        logDebug "Turned on: ${camera.displayName}"
    }
}

def camerasOff() {
    logInfo "Turning indoor cameras OFF for privacy"
    
    // Turn off indoor cameras only
    settings.indoorCameras?.each { camera ->
        camera.off()
        logDebug "Turned off: ${camera.displayName}"
    }
    
    // Keep outdoor cameras on for security
    logDebug "Outdoor cameras remain ON"
}

def revertToAutomatic() {
    logInfo "Manual override duration expired, reverting to automatic mode control"
    
    // Turn off manual override switch
    settings.manualOverride?.off()
    
    // Check and apply current mode
    checkPrivacyMode()
}

// ============================================================================
// HELPER METHODS
// ============================================================================

/**
 * Get configuration value from hub variable or fall back to app setting
 */
def getConfigValue(String settingName, String hubVarName) {
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
    logDebug "Using app setting ${settingName}: ${settingValue}"
    return settingValue
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
    log.info "[Camera Privacy Manager] ${msg}"
}

def logDebug(String msg) {
    if (settings.logEnable) {
        log.debug "[Camera Privacy Manager] ${msg}"
    }
}

def logWarn(String msg) {
    log.warn "[Camera Privacy Manager] ${msg}"
}

def logError(String msg) {
    log.error "[Camera Privacy Manager] ${msg}"
}
