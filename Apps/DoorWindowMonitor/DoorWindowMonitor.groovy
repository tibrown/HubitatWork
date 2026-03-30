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
    namespace: "timbrown",
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
        section("<b>═══════════════════════════════════════</b>\n<b>EXTERIOR DOORS</b>\n<b>═══════════════════════════════════════</b>") {
            input "useFrontDoor", "bool", title: "Use Front Door (Living Room)?", defaultValue: false, submitOnChange: true
            if (settings.useFrontDoor) {
                input "frontDoor", "capability.contactSensor", title: "Front Door (Living Room) Device", required: true
            }
            input "useDiningRoomDoor", "bool", title: "Use Dining Room Front Door?", defaultValue: false, submitOnChange: true
            if (settings.useDiningRoomDoor) {
                input "diningRoomDoor", "capability.contactSensor", title: "Dining Room Front Door Device", required: true
            }
            input "useFrenchDoors", "bool", title: "Use Living Room French Doors?", defaultValue: false, submitOnChange: true
            if (settings.useFrenchDoors) {
                input "frenchDoors", "capability.contactSensor", title: "Living Room French Doors Device", required: true
            }
            input "useBackdoor", "bool", title: "Use Backdoor (Lanai)?", defaultValue: false, submitOnChange: true
            if (settings.useBackdoor) {
                input "backdoor", "capability.contactSensor", title: "Backdoor (Lanai) Device", required: true
            }
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>SHED & STORAGE DOORS</b>\n<b>═══════════════════════════════════════</b>") {
            input "useBirdHouseDoor", "bool", title: "Use Bird House Door (She Shed)?", defaultValue: false, submitOnChange: true
            if (settings.useBirdHouseDoor) {
                input "birdHouseDoor", "capability.contactSensor", title: "Bird House Door (She Shed) Device", required: true
            }
            input "useBirdHouseScreen", "bool", title: "Use Bird House Screen Door?", defaultValue: false, submitOnChange: true
            if (settings.useBirdHouseScreen) {
                input "birdHouseScreen", "capability.contactSensor", title: "Bird House Screen Door Device", required: true
            }
            input "useConcreteShedDoor", "bool", title: "Use Concrete Shed Door?", defaultValue: false, submitOnChange: true
            if (settings.useConcreteShedDoor) {
                input "concreteShedDoor", "capability.contactSensor", title: "Concrete Shed Door Device", required: true
            }
            input "useWoodshedDoor", "bool", title: "Use Woodshed Door?", defaultValue: false, submitOnChange: true
            if (settings.useWoodshedDoor) {
                input "woodshedDoor", "capability.contactSensor", title: "Woodshed Door Device", required: true
            }
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>SPECIAL DOORS</b>\n<b>═══════════════════════════════════════</b>") {
            input "freezerDoors", "capability.contactSensor", title: "Freezer Doors", multiple: true, required: false
            input "useSafeDoor", "bool", title: "Use Safe Door?", defaultValue: false, submitOnChange: true
            if (settings.useSafeDoor) {
                input "safeDoor", "capability.contactSensor", title: "Safe Door Device", required: true
            }
            input "useSuppressSafeDoorAlert", "bool", title: "Use Suppress Safe Door Alert Switch?", defaultValue: false, submitOnChange: true
            if (settings.useSuppressSafeDoorAlert) {
                input "suppressSafeDoorAlert", "capability.switch", title: "Suppress Safe Door Alert Switch (when ON, safe door alerts are disabled)", required: true
            }
            input "useOfficeDoor", "bool", title: "Use Office Door?", defaultValue: false, submitOnChange: true
            if (settings.useOfficeDoor) {
                input "officeDoor", "capability.contactSensor", title: "Office Door Device", required: true
            }
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>WINDOWS</b>\n<b>═══════════════════════════════════════</b>") {
            input "useLrWindow1", "bool", title: "Use Living Room Window 1?", defaultValue: false, submitOnChange: true
            if (settings.useLrWindow1) {
                input "lrWindow1", "capability.contactSensor", title: "Living Room Window 1 Device", required: true
            }
            input "useLrWindow2", "bool", title: "Use Living Room Window 2?", defaultValue: false, submitOnChange: true
            if (settings.useLrWindow2) {
                input "lrWindow2", "capability.contactSensor", title: "Living Room Window 2 Device", required: true
            }
            input "useLrWindow3", "bool", title: "Use Living Room Window 3?", defaultValue: false, submitOnChange: true
            if (settings.useLrWindow3) {
                input "lrWindow3", "capability.contactSensor", title: "Living Room Window 3 Device", required: true
            }
            input "useLrWindow4", "bool", title: "Use Living Room Window 4?", defaultValue: false, submitOnChange: true
            if (settings.useLrWindow4) {
                input "lrWindow4", "capability.contactSensor", title: "Living Room Window 4 Device", required: true
            }
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>PAUSE DR DOOR MOTION</b>\n<b>═══════════════════════════════════════</b>") {
            input "usePauseDRDoorAlarm", "bool", title: "Use Pause DR Door Alarm Switch?", defaultValue: false, submitOnChange: true
            if (settings.usePauseDRDoorAlarm) {
                input "pauseDRDoorAlarm", "capability.switch", title: "Pause DR Door Alarm Switch", required: true
            }
            input "useDrMotionSensor", "bool", title: "Use DR Motion Sensor (auto-activates pause)?", defaultValue: false, submitOnChange: true
            if (settings.useDrMotionSensor) {
                input "drMotionSensor", "capability.motionSensor", title: "DR Motion Sensor Device", required: true
            }
            paragraph "When dining room motion is detected in active modes, the pause switch will be turned ON. After the configured pause duration, the switch will automatically turn OFF."
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>PAUSE BACKDOOR MOTION</b>\n<b>═══════════════════════════════════════</b>") {
            input "usePauseBDAlarm", "bool", title: "Use Pause Backdoor Alarm Switch?", defaultValue: false, submitOnChange: true
            if (settings.usePauseBDAlarm) {
                input "pauseBDAlarm", "capability.switch", title: "Pause Backdoor Alarm Switch", required: true
            }
            input "useMotionSensor", "bool", title: "Use Backdoor Motion Sensor (auto-activates pause)?", defaultValue: false, submitOnChange: true
            if (settings.useMotionSensor) {
                input "motionSensor", "capability.motionSensor", title: "Backdoor Motion Sensor Device", required: true
            }
            input "motionLights", "capability.switch", title: "Lights to Control (turn ON when motion detected)", multiple: true, required: false
            input "useMotionPauseSwitch", "bool", title: "Use Additional Pause Switch to Activate (optional)?", defaultValue: false, submitOnChange: true
            if (settings.useMotionPauseSwitch) {
                input "motionPauseSwitch", "capability.switch", title: "Additional Pause Switch Device", required: true
            }
            input "motionTimeout", "number", title: "Auto-Off Delay (minutes after motion detected)", defaultValue: 30, required: false
            input "motionDebounceSeconds", "number", title: "Minimum Motion Hold Time (seconds)",
                description: "Minimum time a motion state must be held before acting. Filters rapid/noisy sensor transitions caused by low battery or interference.",
                defaultValue: 3, range: "1..30", required: false
            paragraph "When backdoor motion is detected in active modes, the configured lights will turn ON. The pause switch and optional additional pause switch will also be turned ON. After the delay period, lights and switches will automatically turn OFF."
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>MODE CONFIGURATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "useSilentSwitch", "bool", title: "Use Silent Switch (when ON, ALL alerts are suppressed - takes precedence)?", defaultValue: false, submitOnChange: true
            if (settings.useSilentSwitch) {
                input "silentSwitch", "capability.switch", title: "Silent Switch Device", required: true
                input "silenceOfficeSwitch", "capability.switch", title: "Silence Office Switch", required: false
            }
            input "birdHouseSilentModes", "mode", title: "Modes to suppress Bird House alerts", multiple: true, required: false
            input "leftOpenSilentModes", "mode", title: "Modes to suppress left-open alerts", multiple: true, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>NOTIFICATIONS</b>\n<b>═══════════════════════════════════════</b>") {
            input "notificationDevices", "capability.notification", title: "Notification Devices", multiple: true, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>PAUSE NOTIFICATIONS</b>\n<b>═══════════════════════════════════════</b>") {
            input "pauseNotificationDevices", "capability.notification", title: "Pause Alarm Notification Devices", multiple: true, required: false
            paragraph "These devices will receive notifications when pause alarms are manually activated or when the auto-unpause occurs. Leave empty to disable pause notifications."
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>ALERT THRESHOLDS</b>\n<b>═══════════════════════════════════════</b>") {
            input "usePauseDoorAjarSwitch", "bool", title: "Use Pause Door Ajar Switch (when ON, left-open notifications to devices are suppressed — hub logs and phone alerts still sent)?", defaultValue: false, submitOnChange: true
            if (settings.usePauseDoorAjarSwitch) {
                input "pauseDoorAjarSwitch", "capability.switch", title: "Pause Door Ajar Switch Device", required: true
            }
            input "useLeftOpenPhoneDevice", "bool", title: "Use Phone Device for Left-Open Alerts (always notified regardless of Pause Door Ajar switch)?", defaultValue: false, submitOnChange: true
            if (settings.useLeftOpenPhoneDevice) {
                input "leftOpenPhoneDevice", "capability.notification", title: "Phone Device for Left-Open Alerts", required: true
            }
            input "hubVar_DoorOpenThreshold", "number", title: "Door Left Open Alert", description: "Alert after door left open this long (minutes). Sets DoorOpenThreshold hub variable.", defaultValue: 5, required: false
            input "hubVar_WindowOpenThreshold", "number", title: "Window Left Open Alert", description: "Alert after window left open this long (minutes). Sets WindowOpenThreshold hub variable.", defaultValue: 10, required: false
            input "hubVar_FreezerDoorThreshold", "number", title: "Freezer Door Left Open Alert", description: "Alert after freezer door left open this long (minutes). Sets FreezerDoorThreshold hub variable.", defaultValue: 2, required: false
            input "hubVar_CheckInterval", "number", title: "Check Interval", description: "How often to check for doors/windows left open (minutes). Sets CheckInterval hub variable.", defaultValue: 1, required: false
            input "hubVar_TamperAlertEnabled", "bool", title: "Tamper Alert Enabled", description: "Enable alerts when sensors detect tampering. Sets TamperAlertEnabled hub variable.", defaultValue: true, required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>PAUSE CONFIGURATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "hubVar_PauseDuration", "number", title: "Auto Pause Duration", description: "How long to pause alerts when pause button pressed (minutes). Sets PauseDuration hub variable.", defaultValue: 5, required: false
            input "pauseMotionActiveModes", "mode", title: "Pause Motion Active Modes (motion triggers pause only in these modes)", multiple: true, required: false
            paragraph "Configure when motion sensors should trigger pause alarms. The pause duration applies to both manual and motion-triggered pauses."
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>HUB VARIABLES</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Configuration values above are stored as hub variables for cross-app sharing:"
            paragraph "• DoorOpenThreshold, WindowOpenThreshold, FreezerDoorThreshold - Alert thresholds"
            paragraph "• CheckInterval - Periodic check frequency"
            paragraph "• PauseDuration - Pause duration for alerts"
            paragraph "• TamperAlertEnabled - Tamper detection control"
            paragraph "Hub variables are automatically synced when this app is updated."
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>LOGGING</b>\n<b>═══════════════════════════════════════</b>") {
            input "logLevel", "enum", title: "Log Level", options: ["None","Info","Debug","Trace"], defaultValue: "Info"
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
    syncHubVariables()
}

def initialize() {
    logInfo "Initializing Door Window Monitor"
    
    // Subscribe to all doors and windows
    if (settings.useFrontDoor && frontDoor) subscribe(frontDoor, "contact", handleContact)
    if (settings.useDiningRoomDoor && diningRoomDoor) subscribe(diningRoomDoor, "contact", handleContact)
    if (settings.useFrenchDoors && frenchDoors) subscribe(frenchDoors, "contact", handleContact)
    if (settings.useBackdoor && backdoor) subscribe(backdoor, "contact", handleContact)
    if (settings.useBirdHouseDoor && birdHouseDoor) subscribe(birdHouseDoor, "contact", handleContact)
    if (settings.useBirdHouseScreen && birdHouseScreen) subscribe(birdHouseScreen, "contact", handleContact)
    if (settings.useConcreteShedDoor && concreteShedDoor) subscribe(concreteShedDoor, "contact", handleContact)
    if (settings.useWoodshedDoor && woodshedDoor) subscribe(woodshedDoor, "contact", handleContact)
    if (freezerDoors) freezerDoors.each { subscribe(it, "contact", handleContact) }
    if (settings.useSafeDoor && safeDoor) subscribe(safeDoor, "contact", handleContact)
    if (settings.useOfficeDoor && officeDoor) subscribe(officeDoor, "contact", handleContact)
    if (settings.useLrWindow1 && lrWindow1) subscribe(lrWindow1, "contact", handleContact)
    if (settings.useLrWindow2 && lrWindow2) subscribe(lrWindow2, "contact", handleContact)
    if (settings.useLrWindow3 && lrWindow3) subscribe(lrWindow3, "contact", handleContact)
    if (settings.useLrWindow4 && lrWindow4) subscribe(lrWindow4, "contact", handleContact)
    
    // Subscribe to pause switches
    if (settings.usePauseDRDoorAlarm && pauseDRDoorAlarm) subscribe(pauseDRDoorAlarm, "switch.on", handlePauseDRDoor)
    if (settings.usePauseBDAlarm && pauseBDAlarm) subscribe(pauseBDAlarm, "switch.on", handlePauseBD)
    
    // Subscribe to pause motion sensors
    if (settings.useDrMotionSensor && drMotionSensor) subscribe(drMotionSensor, "motion.active", handleDRMotion)
    if (settings.useMotionSensor && motionSensor) subscribe(motionSensor, "motion", handleMotion)
    
    // Subscribe to mode changes to check doors when entering Night mode
    subscribe(location, "mode", handleModeChange)
    
    // Schedule periodic left-open check
    Integer interval = getConfigValue("checkInterval", "CheckInterval") as Integer
    schedule("0 */${interval} * * * ?", checkLeftOpen)
}

// ========================================
// MODE CHANGE HANDLER
// ========================================

def handleModeChange(evt) {
    logDebug "Mode changed to: ${evt.value}"
    
    if (evt.value == "Night") {
        logInfo "Entering Night mode - checking for open doors"
        checkDoorsForNightMode()
    }
}

def checkDoorsForNightMode() {
    def openDoors = []
    
    // Check all doors except birdhouse door and office door
    if (settings.useFrontDoor && frontDoor?.currentValue("contact") == "open") openDoors.add("Front door")
    if (settings.useDiningRoomDoor && diningRoomDoor?.currentValue("contact") == "open") openDoors.add("Dining room door")
    if (settings.useFrenchDoors && frenchDoors?.currentValue("contact") == "open") openDoors.add("French doors")
    if (settings.useBackdoor && backdoor?.currentValue("contact") == "open") openDoors.add("Backdoor")
    if (settings.useBirdHouseScreen && birdHouseScreen?.currentValue("contact") == "open") openDoors.add("Birdhouse screen door")
    if (settings.useConcreteShedDoor && concreteShedDoor?.currentValue("contact") == "open") openDoors.add("Concrete shed door")
    if (settings.useWoodshedDoor && woodshedDoor?.currentValue("contact") == "open") openDoors.add("Woodshed door")
    // Note: Excluded birdHouseDoor and officeDoor as requested
    
    if (openDoors.size() > 0) {
        String doorList = openDoors.join(", ")
        logInfo "Night mode alert: Open doors detected: ${doorList}"
        sendNotification("NIGHT MODE ALERT: The following doors are open: ${doorList}")
    } else {
        logInfo "Night mode: All monitored doors are closed"
    }
}

// ========================================
// MAIN CONTACT HANDLER
// ========================================

def handleContact(evt) {
    String deviceName = evt.displayName
    String deviceId = evt.deviceId
    String value = evt.value
    String mode = location.mode

    // Guard against stale subscriptions — submitOnChange does not call updated(),
    // so a toggle turned OFF may still have an active subscription until Done is saved.
    if (!isDeviceEnabled(evt.device)) {
        logDebug "Contact event from disabled device ${deviceName} - ignoring (toggle is OFF)"
        return
    }
    
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

def isDeviceEnabled(device) {
    if (device.id == frontDoor?.id)        return settings.useFrontDoor == true
    if (device.id == diningRoomDoor?.id)   return settings.useDiningRoomDoor == true
    if (device.id == frenchDoors?.id)      return settings.useFrenchDoors == true
    if (device.id == backdoor?.id)         return settings.useBackdoor == true
    if (device.id == birdHouseDoor?.id)    return settings.useBirdHouseDoor == true
    if (device.id == birdHouseScreen?.id)  return settings.useBirdHouseScreen == true
    if (device.id == concreteShedDoor?.id) return settings.useConcreteShedDoor == true
    if (device.id == woodshedDoor?.id)     return settings.useWoodshedDoor == true
    if (device.id == safeDoor?.id)         return settings.useSafeDoor == true
    if (device.id == officeDoor?.id)       return settings.useOfficeDoor == true
    if (device.id == lrWindow1?.id)        return settings.useLrWindow1 == true
    if (device.id == lrWindow2?.id)        return settings.useLrWindow2 == true
    if (device.id == lrWindow3?.id)        return settings.useLrWindow3 == true
    if (device.id == lrWindow4?.id)        return settings.useLrWindow4 == true
    if (freezerDoors?.find { it.id == device.id }) return true  // no per-device toggle for multi-select
    return false
}

// ========================================
// DOOR/WINDOW OPEN HANDLERS
// ========================================

def handleDoorOpen(device, String mode) {
    logInfo "${device.displayName} opened in ${mode} mode"
    
    if (device.id == frontDoor?.id) {
        if (mode == "Morning") sendNotification(device.label)
        return
    }
    if (device.id == safeDoor?.id) {
        log.warn "Door Window Monitor: Safe door match detected - calling handler"
        handleSafeDoorOpen()
        return
    }
    if (device.id == lrWindow1?.id || device.id == lrWindow2?.id ||
        device.id == lrWindow3?.id || device.id == lrWindow4?.id) {
        if (mode in ["Evening", "Night", "Morning"]) sendNotification(device.label)
        return
    }
    if (device.id == birdHouseDoor?.id || device.id == birdHouseScreen?.id) {
        if (!shouldSuppressBirdHouseAlert(mode)) sendNotification(device.label)
        return
    }
    if (freezerDoors?.find { it.id == device.id }) {
        logInfo "${device.displayName} OPENED - Sending immediate notification"
        sendNotification("ALERT: ${device.label} has been opened")
        return
    }
    if (device.id == concreteShedDoor?.id || device.id == woodshedDoor?.id) {
        sendNotification(device.label)
        return
    }
    // DiningRoom, FrenchDoors, Backdoor, Office: log only, no notification
    logDebug "${device.displayName} opened, no alert configured for this device"
}

def handleFrontDoorOpen(String mode) { }

def handleDiningRoomDoorOpen(String mode) { }

def handleFrenchDoorsOpen(String mode) { }

def handleBackdoorOpen(String mode) { }

def handleBirdHouseDoorOpen(String mode) { }

def handleBirdHouseScreenOpen(String mode) { }

def handleConcreteShedOpen(String mode) { }

def handleWoodshedOpen(String mode) { }

def shouldSuppressBirdHouseAlert(String mode) {
    if (!birdHouseSilentModes) return false
    return birdHouseSilentModes.contains(mode)
}

def handleFreezerDoorOpen(device) { }

def handleSafeDoorOpen() {
    logInfo "SAFE DOOR OPENED - Checking alert status"
    
    // Check Silent switch first - takes precedence
    if (isSilentMode()) {
        logDebug "Safe door alert suppressed by Silent switch"
        return
    }
    
    // Check if safe door alerts are suppressed
    if (settings.useSuppressSafeDoorAlert && suppressSafeDoorAlert && suppressSafeDoorAlert.currentValue("switch") == "on") {
        logInfo "Safe door alert suppressed - suppress switch is ON"
        return
    }
    
    // Send notification immediately - no delays
    if (notificationDevices) {
        notificationDevices.each { device ->
            device.deviceNotification("ALERT: Safe door has been opened")
            logInfo "Safe door notification sent to: ${device.displayName}"
        }
    } else {
        log.warn "Door Window Monitor: Safe door opened but NO notification devices configured!"
    }
    
    // Also log to hub for tracking
    log.warn "Door Window Monitor: SAFE DOOR OPENED at ${new Date()}"
}

def handleOfficeDoorOpen() { }

def handleLRWindowOpen(String mode, Integer windowNum) { }

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
    
    // Check if current mode suppresses left-open alerts
    String currentMode = location.mode
    Boolean suppressNonCritical = leftOpenSilentModes && leftOpenSilentModes.contains(currentMode)
    
    if (suppressNonCritical) {
        logDebug "Left-open alerts suppressed in ${currentMode} mode (except freezer)"
    }
    
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
            Boolean isFreezer = freezerDoors && freezerDoors.find { it.id == device.id }
            
            // Skip non-freezer alerts if in silent mode
            if (suppressNonCritical && !isFreezer) {
                return
            }
            
            // Determine threshold based on device type
            Integer threshold = doorThreshold
            if (device.id == lrWindow1?.id || device.id == lrWindow2?.id || 
                device.id == lrWindow3?.id || device.id == lrWindow4?.id) {
                threshold = windowThreshold
            } else if (isFreezer) {
                threshold = freezerThreshold
            }
            
            // Alert if open longer than threshold
            if (duration >= threshold) {
                logInfo "${deviceName} has been open for ${duration / 60000} minutes"
                String alertPrefix = isFreezer ? "CRITICAL" : "ALERT"
                sendLeftOpenAlert("${alertPrefix}: ${deviceName} has been left open!")
                
                // Clear the open time so we don't spam alerts
                // Will reset if door is opened again
                state.remove(key)
            }
        }
    }
}

def findDeviceById(String deviceId) {
    def allDevices = []
    if (settings.useFrontDoor)        allDevices.add(frontDoor)
    if (settings.useDiningRoomDoor)   allDevices.add(diningRoomDoor)
    if (settings.useFrenchDoors)      allDevices.add(frenchDoors)
    if (settings.useBackdoor)         allDevices.add(backdoor)
    if (settings.useBirdHouseDoor)    allDevices.add(birdHouseDoor)
    if (settings.useBirdHouseScreen)  allDevices.add(birdHouseScreen)
    if (settings.useConcreteShedDoor) allDevices.add(concreteShedDoor)
    if (settings.useWoodshedDoor)     allDevices.add(woodshedDoor)
    if (settings.useSafeDoor)         allDevices.add(safeDoor)
    if (settings.useOfficeDoor)       allDevices.add(officeDoor)
    if (settings.useLrWindow1)        allDevices.add(lrWindow1)
    if (settings.useLrWindow2)        allDevices.add(lrWindow2)
    if (settings.useLrWindow3)        allDevices.add(lrWindow3)
    if (settings.useLrWindow4)        allDevices.add(lrWindow4)
    if (freezerDoors)                 allDevices.addAll(freezerDoors)
    
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
    
    sendPauseNotification("Dining room door alarm paused for ${duration / 60} minutes")
}

def handlePauseBD(evt) {
    logInfo "Pausing backdoor alarm"
    
    Integer duration = (getConfigValue("pauseDuration", "PauseDuration") as Integer) * 60
    
    // Schedule auto-unpause
    runIn(duration, unpauseBD)
    
    sendPauseNotification("Backdoor alarm paused for ${duration / 60} minutes")
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
// MOTION-ACTIVATED LIGHTING
// ========================================

def handleMotion(evt) {
    String motionValue = evt.value
    if (!isValidMotionTransition(evt.deviceId.toString(), motionValue)) return
    String currentMode = location.mode
    
    logDebug "Motion event: ${evt.displayName} ${motionValue}, Mode: ${currentMode}"
    
    // Check if motion control is configured
    if (!motionLights) {
        logDebug "No motion lights configured - skipping"
        return
    }
    
    // Check if current mode is in active modes
    if (pauseMotionActiveModes && !pauseMotionActiveModes.contains(currentMode)) {
        logDebug "Motion detected but mode ${currentMode} not in active modes - skipping"
        return
    }
    
    if (motionValue == "active") {
        handleMotionActive()
    }
    // Note: We don't handle inactive - the delay starts immediately on motion detection
}

def handleMotionActive() {
    logInfo "Motion detected - turning ON motion lights"
    
    // Cancel any pending auto-off
    unschedule("autoOffMotionLights")
    
    // Turn on all motion lights
    motionLights.each { light ->
        if (light.currentValue("switch") != "on") {
            light.on()
            logInfo "Turned ON: ${light.displayName}"
        } else {
            logDebug "${light.displayName} already ON"
        }
    }
    
    // Turn on pause switch if configured
    if (settings.useMotionPauseSwitch && motionPauseSwitch && motionPauseSwitch.currentValue("switch") != "on") {
        motionPauseSwitch.on()
        logInfo "Activated pause switch: ${motionPauseSwitch.displayName}"
    }
    
    // Schedule auto-off after configured delay
    Integer delayMinutes = settings.motionTimeout ?: 30
    logInfo "Scheduling auto-off in ${delayMinutes} minutes"
    runIn(delayMinutes * 60, autoOffMotionLights)
}

def autoOffMotionLights() {
    logInfo "Motion delay timeout reached - turning OFF motion lights"
    
    // Turn off lights
    motionLights.each { light ->
        if (light.currentValue("switch") == "on") {
            light.off()
            logInfo "Turned OFF: ${light.displayName}"
        }
    }
    
    // Turn off pause switch if configured
    if (settings.useMotionPauseSwitch && motionPauseSwitch && motionPauseSwitch.currentValue("switch") == "on") {
        motionPauseSwitch.off()
        logInfo "Deactivated pause switch: ${motionPauseSwitch.displayName}"
    }
}

def handleDRMotion(evt) {
    if (!isValidMotionTransition(evt.deviceId.toString(), evt.value)) return
    String currentMode = location.mode
    logInfo "DR motion detected in ${currentMode} mode"
    
    // Check if current mode is in active modes
    if (pauseMotionActiveModes && !pauseMotionActiveModes.contains(currentMode)) {
        logDebug "DR motion detected but mode ${currentMode} not in active modes - skipping"
        return
    }
    
    logInfo "DR motion detected - activating pause alarm"
    
    // Cancel any pending auto-cancel
    unschedule("autoCancelDRPause")
    
    // Activate pause switch if not already on
    if (settings.usePauseDRDoorAlarm && pauseDRDoorAlarm && pauseDRDoorAlarm.currentValue("switch") != "on") {
        pauseDRDoorAlarm.on()
        logInfo "Activated DR door alarm pause switch"
    }
    
    // Schedule auto-cancel after configured delay (using same pauseDuration setting)
    Integer delayMinutes = getConfigValue("pauseDuration", "PauseDuration") as Integer
    logInfo "Scheduling auto-cancel in ${delayMinutes} minutes"
    runIn(delayMinutes * 60, autoCancelDRPause)
}

def autoCancelDRPause() {
    logInfo "DR pause timeout reached - deactivating pause alarm"
    
    // Turn off pause switch
    if (settings.usePauseDRDoorAlarm && pauseDRDoorAlarm && pauseDRDoorAlarm.currentValue("switch") == "on") {
        pauseDRDoorAlarm.off()
        logInfo "Deactivated DR door alarm pause switch"
    }
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

def syncHubVariables() {
    setHubVar("CheckInterval", (hubVar_CheckInterval ?: 1).toString())
    setHubVar("DoorOpenThreshold", (hubVar_DoorOpenThreshold ?: 5).toString())
    setHubVar("WindowOpenThreshold", (hubVar_WindowOpenThreshold ?: 10).toString())
    setHubVar("FreezerDoorThreshold", (hubVar_FreezerDoorThreshold ?: 2).toString())
    setHubVar("PauseDuration", (hubVar_PauseDuration ?: 5).toString())
    setHubVar("TamperAlertEnabled", (hubVar_TamperAlertEnabled != null ? hubVar_TamperAlertEnabled : true).toString())
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

def isSilentMode() {
    if (settings.useSilentSwitch && silentSwitch && silentSwitch.currentValue("switch") == "on") return true
    if (settings.useSilentSwitch && silenceOfficeSwitch && silenceOfficeSwitch.currentValue("switch") == "on") return true
    return false
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
// NOTIFICATIONS
// ========================================

def sendLeftOpenAlert(String message) {
    // Always log — regardless of master switch or silent mode
    log.info "${app.label}: LEFT-OPEN: ${message}"
    
    // Always notify the dedicated phone device — regardless of master switch or silent mode
    if (settings.useLeftOpenPhoneDevice && leftOpenPhoneDevice) {
        leftOpenPhoneDevice.deviceNotification(message)
    }
    
    // Only notify general notification devices if Pause Door Ajar switch is OFF
    if (settings.usePauseDoorAjarSwitch && pauseDoorAjarSwitch && pauseDoorAjarSwitch.currentValue("switch") == "on") {
        logDebug "Left-open notification alerts paused by PauseDoorAjar switch - skipping notificationDevices: ${message}"
        return
    }
    
    // Respect silent switch for notification devices
    if (isSilentMode()) {
        logDebug "Left-open notification suppressed by Silent switch: ${message}"
        return
    }
    
    if (notificationDevices) {
        notificationDevices.each { device ->
            device.deviceNotification(message)
        }
    }
}

def sendNotification(String message) {
    // Check Silent switch first - takes precedence over all other settings
    if (isSilentMode()) {
        logDebug "Notification suppressed by Silent switch: ${message}"
        return
    }
    
    if (notificationDevices) {
        notificationDevices.each { device ->
            device.deviceNotification(message)
        }
        logInfo "Notification: ${message}"
    }
}

def sendPauseNotification(String message) {
    // Check Silent switch first - takes precedence over all other settings
    if (isSilentMode()) {
        logDebug "Pause notification suppressed by Silent switch: ${message}"
        return
    }
    
    if (pauseNotificationDevices) {
        pauseNotificationDevices.each { device ->
            device.deviceNotification(message)
        }
        logInfo "Pause Notification: ${message}"
    }
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
