/**
 *  Door Window Monitor
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
    name: "Door Window Monitor",
    namespace: "hubitat",
    author: "Tim Brown",
    description: "Comprehensive door and window monitoring system. Alerts on door/window opens, detects left-open conditions, monitors freezer and safe, provides pause functionality, and tamper detection.",
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
    dynamicPage(name: "mainPage", title: "Door Window Monitor", install: true, uninstall: true) {
        section("Exterior Doors") {
            input "frontDoor", "capability.contactSensor", title: "Front Door (Living Room)", required: false
            input "diningRoomDoor", "capability.contactSensor", title: "Dining Room Front Door", required: false
            input "frenchDoors", "capability.contactSensor", title: "Living Room French Doors", required: false
            input "backdoor", "capability.contactSensor", title: "Backdoor (Lanai)", required: false
        }
        
        section("Shed & Storage Doors") {
            input "birdHouseDoor", "capability.contactSensor", title: "Bird House Door (She Shed)", required: false
            input "birdHouseScreen", "capability.contactSensor", title: "Bird House Screen Door", required: false
            input "concreteShedDoor", "capability.contactSensor", title: "Concrete Shed Door", required: false
            input "woodshedDoor", "capability.contactSensor", title: "Woodshed Door", required: false
        }
        
        section("Special Doors") {
            input "freezerDoor", "capability.contactSensor", title: "Freezer Door", required: false
            input "safeDoor", "capability.contactSensor", title: "Safe Door", required: false
        }
        
        section("Windows") {
            input "lrWindow", "capability.contactSensor", title: "Living Room Window", required: false
        }
        
        section("Pause Switches") {
            input "pauseDRDoorAlarm", "capability.switch", title: "Pause DR Door Alarm Switch", required: false
            input "pauseBDAlarm", "capability.switch", title: "Pause Backdoor Alarm Switch", required: false
        }
        
        section("Notifications") {
            input "notificationDevices", "capability.notification", title: "Notification Devices", multiple: true, required: false
        }
        
        section("Alert Thresholds") {
            input "doorOpenThreshold", "number", title: "Door Left Open Alert (minutes)", defaultValue: 5, required: false
            input "windowOpenThreshold", "number", title: "Window Left Open Alert (minutes)", defaultValue: 10, required: false
            input "freezerDoorThreshold", "number", title: "Freezer Door Left Open Alert (minutes)", defaultValue: 2, required: false
            input "checkInterval", "number", title: "Check Interval for Left Open (minutes)", defaultValue: 1, required: false
        }
        
        section("Pause Configuration") {
            input "pauseDuration", "number", title: "Auto Pause Duration (minutes)", defaultValue: 5, required: false
        }
        
        section("Hub Variable Overrides") {
            paragraph "This app supports hub variable overrides for flexible configuration:"
            paragraph "• DoorOpenThreshold - Override time before alerting on open door (minutes)"
            paragraph "• WindowOpenThreshold - Override time before alerting on open window (minutes)"
            paragraph "• FreezerDoorThreshold - Override freezer door open threshold (minutes)"
            paragraph "• PauseDuration - Override alarm pause duration (minutes)"
            paragraph "• CheckInterval - Override periodic check interval (minutes)"
            paragraph "• TamperAlertEnabled - Enable/disable tamper detection (true/false)"
        }
        
        section("Logging") {
            input "logEnable", "bool", title: "Enable debug logging", defaultValue: true
            input "infoEnable", "bool", title: "Enable info logging", defaultValue: true
        }
    }
}

def installed() {
    logInfo "Door Window Monitor installed"
    initialize()
}

def updated() {
    logInfo "Door Window Monitor updated"
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    logInfo "Initializing Door Window Monitor"
    
    // Subscribe to all doors and windows
    if (frontDoor) subscribe(frontDoor, "contact", handleContact)
    if (diningRoomDoor) subscribe(diningRoomDoor, "contact", handleContact)
    if (frenchDoors) subscribe(frenchDoors, "contact", handleContact)
    if (backdoor) subscribe(backdoor, "contact", handleContact)
    if (birdHouseDoor) subscribe(birdHouseDoor, "contact", handleContact)
    if (birdHouseScreen) subscribe(birdHouseScreen, "contact", handleContact)
    if (concreteShedDoor) subscribe(concreteShedDoor, "contact", handleContact)
    if (woodshedDoor) subscribe(woodshedDoor, "contact", handleContact)
    if (freezerDoor) subscribe(freezerDoor, "contact", handleContact)
    if (safeDoor) subscribe(safeDoor, "contact", handleContact)
    if (lrWindow) subscribe(lrWindow, "contact", handleContact)
    
    // Subscribe to pause switches
    if (pauseDRDoorAlarm) subscribe(pauseDRDoorAlarm, "switch.on", handlePauseDRDoor)
    if (pauseBDAlarm) subscribe(pauseBDAlarm, "switch.on", handlePauseBD)
    
    // Schedule periodic left-open check
    Integer interval = getConfigValue("checkInterval", "CheckInterval") as Integer
    schedule("0 */${interval} * * * ?", checkLeftOpen)
}

// ========================================
// MAIN CONTACT HANDLER
// ========================================

def handleContact(evt) {
    String deviceName = evt.displayName
    String deviceId = evt.deviceId
    String value = evt.value
    String mode = location.mode
    
    logDebug "Contact event: ${deviceName} ${value}, Mode: ${mode}"
    
    if (value == "open") {
        handleDoorOpen(evt.device, mode)
        
        // Track open time for left-open detection
        state["${deviceId}_openTime"] = now()
    } else {
        handleDoorClosed(evt.device)
        
        // Clear open time tracking
        state.remove("${deviceId}_openTime")
    }
}

// ========================================
// DOOR/WINDOW OPEN HANDLERS
// ========================================

def handleDoorOpen(device, String mode) {
    String deviceName = device.displayName
    logInfo "${deviceName} opened in ${mode} mode"
    
    // Route to specific handlers based on device
    if (device.id == frontDoor?.id) {
        handleFrontDoorOpen(mode)
    } else if (device.id == diningRoomDoor?.id) {
        handleDiningRoomDoorOpen(mode)
    } else if (device.id == frenchDoors?.id) {
        handleFrenchDoorsOpen(mode)
    } else if (device.id == backdoor?.id) {
        handleBackdoorOpen(mode)
    } else if (device.id == birdHouseDoor?.id) {
        handleBirdHouseDoorOpen(mode)
    } else if (device.id == birdHouseScreen?.id) {
        handleBirdHouseScreenOpen(mode)
    } else if (device.id == concreteShedDoor?.id) {
        handleConcreteShedOpen(mode)
    } else if (device.id == woodshedDoor?.id) {
        handleWoodshedOpen(mode)
    } else if (device.id == freezerDoor?.id) {
        handleFreezerDoorOpen()
    } else if (device.id == safeDoor?.id) {
        handleSafeDoorOpen()
    } else if (device.id == lrWindow?.id) {
        handleLRWindowOpen(mode)
    }
}

def handleFrontDoorOpen(String mode) {
    if (mode == "Day") {
        sendNotification("Front door is open, Front door is open")
    } else if (mode == "Morning") {
        sendNotification("Alert, Intruder at the front door")
    }
}

def handleDiningRoomDoorOpen(String mode) {
    if (mode == "Day") {
        sendNotification("Dining room door is open, dining room door is open")
    }
}

def handleFrenchDoorsOpen(String mode) {
    if (mode == "Day") {
        sendNotification("French doors are open, French doors are open")
    }
}

def handleBackdoorOpen(String mode) {
    // Backdoor alerts are typically handled by NightSecurityManager for night mode
    // Day mode handling
    if (mode == "Day") {
        logDebug "Backdoor opened during day mode"
    }
}

def handleBirdHouseDoorOpen(String mode) {
    sendNotification("Bird House door is open")
}

def handleBirdHouseScreenOpen(String mode) {
    sendNotification("Bird House screen door is open")
}

def handleConcreteShedOpen(String mode) {
    sendNotification("Concrete shed door is open")
}

def handleWoodshedOpen(String mode) {
    sendNotification("Woodshed door is open")
}

def handleFreezerDoorOpen() {
    logInfo "Freezer door opened - will alert if left open"
    // Alert handled by checkLeftOpen periodic scan
}

def handleSafeDoorOpen() {
    logInfo "Safe door opened"
    sendNotification("Safe door has been opened")
}

def handleLRWindowOpen(String mode) {
    if (mode == "Day") {
        sendNotification("Living room window is open, living room window is open")
    } else if (mode == "Evening" || mode == "Night" || mode == "Morning") {
        sendNotification("Living room window is open")
    }
}

// ========================================
// DOOR/WINDOW CLOSED HANDLERS
// ========================================

def handleDoorClosed(device) {
    String deviceName = device.displayName
    logDebug "${deviceName} closed"
    
    // No specific actions on close for most doors
    // State tracking is cleared in handleContact
}

// ========================================
// LEFT OPEN DETECTION
// ========================================

def checkLeftOpen() {
    logDebug "Checking for doors/windows left open"
    
    Long currentTime = now()
    Integer doorThreshold = (getConfigValue("doorOpenThreshold", "DoorOpenThreshold") as Integer) * 60000 // Convert to ms
    Integer windowThreshold = (getConfigValue("windowOpenThreshold", "WindowOpenThreshold") as Integer) * 60000
    Integer freezerThreshold = (getConfigValue("freezerDoorThreshold", "FreezerDoorThreshold") as Integer) * 60000
    
    // Check each tracked door/window
    state.each { key, value ->
        if (key.endsWith("_openTime")) {
            String deviceId = key.replace("_openTime", "")
            Long openTime = value as Long
            Long duration = currentTime - openTime
            
            // Find the device
            def device = findDeviceById(deviceId)
            if (!device) return
            
            String deviceName = device.displayName
            
            // Determine threshold based on device type
            Integer threshold = doorThreshold
            if (device.id == lrWindow?.id) {
                threshold = windowThreshold
            } else if (device.id == freezerDoor?.id) {
                threshold = freezerThreshold
            }
            
            // Alert if open longer than threshold
            if (duration >= threshold) {
                logInfo "${deviceName} has been open for ${duration / 60000} minutes"
                sendNotification("ALERT: ${deviceName} has been left open!")
                
                // Clear the open time so we don't spam alerts
                // Will reset if door is opened again
                state.remove(key)
            }
        }
    }
}

def findDeviceById(String deviceId) {
    def allDevices = [
        frontDoor, diningRoomDoor, frenchDoors, backdoor,
        birdHouseDoor, birdHouseScreen, concreteShedDoor, woodshedDoor,
        freezerDoor, safeDoor, lrWindow
    ]
    
    return allDevices.find { it && it.id == deviceId }
}

// ========================================
// PAUSE HANDLERS
// ========================================

def handlePauseDRDoor(evt) {
    logInfo "Pausing dining room door alarm"
    
    Integer duration = (getConfigValue("pauseDuration", "PauseDuration") as Integer) * 60
    
    // Schedule auto-unpause
    runIn(duration, unpauseDRDoor)
    
    sendNotification("Dining room door alarm paused for ${duration / 60} minutes")
}

def handlePauseBD(evt) {
    logInfo "Pausing backdoor alarm"
    
    Integer duration = (getConfigValue("pauseDuration", "PauseDuration") as Integer) * 60
    
    // Schedule auto-unpause
    runIn(duration, unpauseBD)
    
    sendNotification("Backdoor alarm paused for ${duration / 60} minutes")
}

def unpauseDRDoor() {
    logInfo "Auto-unpausing dining room door alarm"
    pauseDRDoorAlarm?.off()
}

def unpauseBD() {
    logInfo "Auto-unpausing backdoor alarm"
    pauseBDAlarm?.off()
}

// ========================================
// TAMPER DETECTION
// ========================================

def handleTamper(device) {
    Boolean tamperEnabled = getConfigValue("tamperAlertEnabled", "TamperAlertEnabled") as Boolean
    
    if (tamperEnabled == false) {
        logDebug "Tamper detection disabled"
        return
    }
    
    logInfo "TAMPER DETECTED: ${device.displayName}"
    sendNotification("SECURITY ALERT: Tamper detected on ${device.displayName}")
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

def sendNotification(String message) {
    if (notificationDevices) {
        notificationDevices.each { device ->
            device.deviceNotification(message)
        }
        logInfo "Notification: ${message}"
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
