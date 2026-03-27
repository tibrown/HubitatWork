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
    namespace: "timbrown",
    author: "Tim Brown",
    description: "Manages indoor camera power based on phone presence - cameras on when you leave, off when you arrive",
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
        section("<b>═══════════════════════════════════════</b>\n<b>CAMERAS</b>\n<b>═══════════════════════════════════════</b>") {
            input "indoorCameras", "capability.switch", 
                  title: "Indoor Camera Power Outlets", 
                  description: "Select switches that control indoor camera power",
                  multiple: true, 
                  required: true
            paragraph ""
            input "outdoorCameras", "capability.switch",
                  title: "Outdoor Camera Power Outlets (Optional)",
                  description: "Select switches that control outdoor camera power",
                  multiple: true,
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>PRESENCE DETECTION</b>\n<b>═══════════════════════════════════════</b>") {
            input "phonePresenceSwitch", "capability.switch",
                  title: "Phone Presence Switch",
                  description: "Switch that turns OFF when your phone leaves (e.g., from geofence)",
                  required: true
            paragraph ""
            input "hubVar_PrivacyDelay", "number",
                  title: "Privacy Delay",
                  description: "Delay before turning cameras off when phone arrives (minutes). Sets PrivacyDelay hub variable.",
                  defaultValue: 2,
                  range: "0..60",
                  required: false
            
            input "hubVar_EnableDelay", "number",
                  title: "Enable Delay",
                  description: "Delay before turning cameras on when phone leaves (minutes). Sets EnableDelay hub variable.",
                  defaultValue: 1,
                  range: "0..60",
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>OVERRIDE SWITCHES</b>\n<b>═══════════════════════════════════════</b>") {
            input "travelingSwitch", "capability.switch",
                  title: "Traveling Switch",
                  description: "When ON, your phone leaving won't trigger cameras (spouse still home)",
                  required: false
            paragraph ""
            input "manualOverride", "capability.switch",
                  title: "Manual Override Switch (Optional)",
                  description: "Switch to manually force cameras off",
                  required: false
            
            input "hubVar_ManualOverrideDuration", "number",
                  title: "Manual Override Duration",
                  description: "How long manual camera control lasts before reverting to automatic (hours). Sets ManualOverrideDuration hub variable.",
                  defaultValue: 4,
                  range: "1..24",
                  required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>HUB VARIABLES</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Configuration values above are stored as hub variables for cross-app sharing:"
            paragraph "• PrivacyDelay - Camera off delay when arriving"
            paragraph "• EnableDelay - Camera on delay when leaving"
            paragraph "• ManualOverrideDuration - Manual override timeout"
            paragraph "Hub variables are automatically synced when this app is updated."
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>LOGGING</b>\n<b>═══════════════════════════════════════</b>") {
            input "logLevel", "enum", title: "Log Level", options: ["None","Info","Debug","Trace"], defaultValue: "Info", required: false
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
    unschedule()
    initialize()
    syncHubVariables()
}

def initialize() {
    logInfo "Initializing Camera Privacy Manager"
    
    // Subscribe to phone presence switch
    subscribe(settings.phonePresenceSwitch, "switch", phonePresenceHandler)
    
    // Subscribe to manual override switch if configured
    if (settings.manualOverride) {
        subscribe(settings.manualOverride, "switch", manualOverrideHandler)
    }
    
    // Check current state and apply
    checkPresenceState()
    
    logDebug "Subscribed to phone presence switch: ${settings.phonePresenceSwitch}"
}

// ============================================================================
// EVENT HANDLERS
// ============================================================================

def phonePresenceHandler(evt) {
    logInfo "Phone presence switch changed to: ${evt.value}"
    
    // Cancel any pending jobs
    unschedule(camerasOn)
    unschedule(camerasOff)
    
    if (evt.value == "off") {
        // Phone left - turn cameras ON (unless traveling)
        handlePhoneLeft()
    } else if (evt.value == "on") {
        // Phone arrived - turn cameras OFF for privacy
        handlePhoneArrived()
    }
}

def manualOverrideHandler(evt) {
    logInfo "Manual override switch: ${evt.value}"
    
    if (evt.value == "on") {
        // Manual override ON - turn cameras off immediately
        unschedule(camerasOn)
        camerasOff()
        
        // Schedule revert to automatic mode
        def duration = getConfigValue("overrideDurationHours", "ManualOverrideDuration") as Integer
        logInfo "Manual override active for ${duration} hours"
        runIn(duration * 3600, revertToAutomatic)
    } else {
        // Manual override OFF - return to presence-based control
        unschedule(revertToAutomatic)
        checkPresenceState()
    }
}

// ============================================================================
// CORE LOGIC
// ============================================================================

def handlePhoneLeft() {
    logInfo "Phone has left"
    
    // Check if manual override is active
    if (settings.manualOverride?.currentValue("switch") == "on") {
        logInfo "Manual override active, ignoring phone departure"
        return
    }
    
    // Check if traveling (user is away but spouse is home)
    if (isTraveling()) {
        logInfo "Traveling switch is ON - spouse still home, cameras stay OFF"
        return
    }
    
    // Turn cameras on with delay
    def delay = getConfigValue("enableDelayMinutes", "EnableDelay") as Integer
    if (delay > 0) {
        logInfo "Cameras will turn on in ${delay} minutes"
        runIn(delay * 60, camerasOn)
    } else {
        camerasOn()
    }
}

def handlePhoneArrived() {
    logInfo "Phone has arrived"
    
    // Cancel any pending camera on action
    unschedule(camerasOn)
    
    // Check if manual override is active
    if (settings.manualOverride?.currentValue("switch") == "on") {
        logInfo "Manual override active, cameras already off"
        return
    }
    
    // Turn cameras off with delay
    def delay = getConfigValue("privacyDelayMinutes", "PrivacyDelay") as Integer
    if (delay > 0) {
        logInfo "Cameras will turn off in ${delay} minutes"
        runIn(delay * 60, camerasOff)
    } else {
        camerasOff()
    }
}

def checkPresenceState() {
    // Check if manual override is active
    if (settings.manualOverride?.currentValue("switch") == "on") {
        logInfo "Manual override active, cameras remain off"
        camerasOff()
        return
    }
    
    def phonePresent = settings.phonePresenceSwitch?.currentValue("switch") == "on"
    
    if (phonePresent) {
        logInfo "Phone is home - ensuring privacy mode"
        camerasOff()
    } else {
        if (isTraveling()) {
            logInfo "Phone is away but Traveling is ON - spouse still home, cameras stay OFF"
            camerasOff()
        } else {
            logInfo "Phone is away - ensuring security mode"
            camerasOn()
        }
    }
}

/**
 * Check if Traveling switch is on
 * When traveling, your phone leaving shouldn't trigger cameras because spouse is still home
 */
def isTraveling() {
    return settings.travelingSwitch?.currentValue("switch") == "on"
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
    logInfo "Manual override duration expired, reverting to automatic presence control"
    
    // Turn off manual override switch
    settings.manualOverride?.off()
    
    // Check and apply current presence state
    checkPresenceState()
}

// ============================================================================
// HELPER METHODS
// ============================================================================

def syncHubVariables() {
    setGlobalVar("PrivacyDelay", (hubVar_PrivacyDelay ?: 2).toString())
    setGlobalVar("EnableDelay", (hubVar_EnableDelay ?: 1).toString())
    setGlobalVar("ManualOverrideDuration", (hubVar_ManualOverrideDuration ?: 4).toString())
    logInfo "Hub variables synced from app settings"
}

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
    
    logDebug "Hub variable '${hubVarName}' not set"
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
    if (logLevel in ["Info","Debug","Trace"]) log.info "${app.label}: ${msg}"
}

def logDebug(String msg) {
    if (logLevel in ["Debug","Trace"]) log.debug "${app.label}: ${msg}"
}

void logTrace(String msg) { if (logLevel == "Trace") log.trace "${app.label}: ${msg}" }

def logWarn(String msg) {
    log.warn "[Camera Privacy Manager] ${msg}"
}

def logError(String msg) {
    log.error "[Camera Privacy Manager] ${msg}"
}
