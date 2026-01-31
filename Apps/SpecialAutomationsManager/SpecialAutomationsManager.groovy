/**
 *  Mains Power Monitor
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
    name: "Mains Power Monitor",
    namespace: "timbrown",
    author: "Tim Brown",
    description: "Monitors mains power status, alerts on power outages, and safely shuts down hub during extended outages",
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
    dynamicPage(name: "mainPage", title: "Mains Power Monitor", install: true, uninstall: true) {
        section("<b>POWER SOURCE DEVICE</b>") {
            input "mainsPowerSensor", "capability.powerSource", title: "Mains Power Sensor", required: true
            input "enableMainsMonitoring", "bool", title: "Enable Mains Power Monitoring", 
                defaultValue: true, required: false
        }
        
        section("<b>STATUS & CONTROL DEVICES</b>") {
            input "onMainsSwitch", "capability.switch", title: "On Mains Status Switch", required: false,
                description: "Virtual switch that tracks mains status (ON = on mains, OFF = on battery)"
            input "ignoreMainsCheckSwitch", "capability.switch", title: "Ignore Mains Check Switch", required: false,
                description: "When ON, mains monitoring alerts and shutdown are disabled"
            input "hubController", "capability.actuator", title: "Hub Controller Device", required: false,
                description: "Hubitat Hub Controller device for shutdown command"
        }
        
        section("<b>TIMING SETTINGS</b>") {
            input "mainsStayDuration", "number", title: "Battery Stay Duration Before Alert (minutes)", 
                defaultValue: 5, range: "1..30", required: false
            input "hubShutdownDelay", "number", title: "Hub Shutdown Delay After Alert (minutes)", 
                defaultValue: 30, range: "5..120", required: false
            input "batteryReminderInterval", "number", title: "Battery Reminder Interval (minutes)", 
                defaultValue: 5, range: "1..30", required: false
        }
        
        section("<b>NOTIFICATIONS</b>") {
            input "notificationDevices", "capability.notification", title: "Notification Devices", 
                multiple: true, required: false
        }
        
        section("<b>LOGGING</b>") {
            input "logLevel", "enum", title: "Logging Level", 
                options: ["None", "Info", "Debug", "Trace"], 
                defaultValue: "Info", required: true
        }
        
        section("<b>CURRENT STATUS</b>") {
            if (mainsPowerSensor) {
                def currentSource = mainsPowerSensor.currentValue("powerSource")
                def batteryLevel = mainsPowerSensor.currentValue("battery")
                paragraph "powerSource: <b>${currentSource}</b>"
                paragraph "battery: <b>${batteryLevel}%</b>"
            }
        }
    }
}

def installed() {
    logInfo "Mains Power Monitor installed"
    initialize()
}

def updated() {
    logInfo "Mains Power Monitor updated"
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    logInfo "Initializing Mains Power Monitor"
    initializePowerMonitoring()
    logInfo "Initialization complete"
}

// ========================================
// Power Monitoring
// ========================================

def initializePowerMonitoring() {
    if (mainsPowerSensor && enableMainsMonitoring) {
        subscribe(mainsPowerSensor, "powerSource", powerSourceHandler)
        logInfo "Subscribed to powerSource events from ${mainsPowerSensor.displayName}"
        
        // Set initial status switch state
        def currentSource = mainsPowerSensor.currentValue("powerSource")?.toLowerCase()
        logInfo "Current powerSource: '${currentSource}'"
        
        if (currentSource == "mains") {
            onMainsSwitch?.on()
        } else if (currentSource == "battery") {
            onMainsSwitch?.off()
        }
    }
}

def powerSourceHandler(evt) {
    String source = evt.value?.toLowerCase()
    
    logInfo "POWER SOURCE EVENT: '${source}'"
    
    if (source == "mains") {
        logInfo "MAINS RESTORED"
        onMainsSwitch?.on()
        handleMainsRestored()
    } else if (source == "battery") {
        logInfo "ON BATTERY - mains power lost"
        onMainsSwitch?.off()
        
        Integer stayDuration = (settings.mainsStayDuration ?: 5) * 60
        logInfo "Will alert if stays on battery for ${settings.mainsStayDuration ?: 5} minutes"
        runIn(stayDuration, handleMainsStayedOnBattery)
    }
}

def handleMainsStayedOnBattery() {
    String currentSource = mainsPowerSensor?.currentValue("powerSource")?.toLowerCase()
    if (currentSource != "battery") {
        logInfo "Power restored - cancelling alert"
        return
    }
    
    if (ignoreMainsCheckSwitch?.currentValue("switch") == "on") {
        logInfo "Mains check bypassed"
        return
    }
    
    logInfo "ALERT: Mains power DOWN for ${settings.mainsStayDuration ?: 5} minutes"
    state.mainsAlertStartTime = now()
    
    sendNotification("ALERT: Mains power is DOWN. Hub will shut down in ${settings.hubShutdownDelay ?: 30} minutes if not restored.")
    
    Integer shutdownDelay = (settings.hubShutdownDelay ?: 30) * 60
    runIn(shutdownDelay, shutdownHub)
    
    Integer reminderInterval = (settings.batteryReminderInterval ?: 5) * 60
    runIn(reminderInterval, sendBatteryReminder)
}

def sendBatteryReminder() {
    if (mainsPowerSensor?.currentValue("powerSource")?.toLowerCase() != "battery") {
        return
    }
    
    Long alertStartTime = state.mainsAlertStartTime ?: now()
    Long shutdownDelayMs = (settings.hubShutdownDelay ?: 30) * 60 * 1000
    Long remainingMs = shutdownDelayMs - (now() - alertStartTime)
    Integer remainingMinutes = Math.max(1, Math.round(remainingMs / 60000) as Integer)
    
    sendNotification("Still on battery - hub shutdown in ${remainingMinutes} minutes")
    
    if (remainingMs > 0) {
        Integer reminderInterval = (settings.batteryReminderInterval ?: 5) * 60
        if (remainingMs > (reminderInterval * 1000)) {
            runIn(reminderInterval, sendBatteryReminder)
        }
    }
}

def shutdownHub() {
    if (mainsPowerSensor?.currentValue("powerSource")?.toLowerCase() != "battery") {
        logInfo "Power restored - aborting shutdown"
        return
    }
    
    if (ignoreMainsCheckSwitch?.currentValue("switch") == "on") {
        logInfo "Shutdown bypassed"
        return
    }
    
    logInfo "INITIATING HUB SHUTDOWN"
    sendNotification("Hub shutting down due to extended power outage")
    
    if (hubController) {
        hubController.shutdown()
    } else {
        log.error "Hub Controller not configured"
    }
}

def handleMainsRestored() {
    logInfo "Mains power restored"
    
    unschedule(handleMainsStayedOnBattery)
    unschedule(shutdownHub)
    unschedule(sendBatteryReminder)
    state.remove("mainsAlertStartTime")
    
    onMainsSwitch?.on()
    sendNotification("Mains power restored")
}

// ========================================
// Notifications
// ========================================

def sendNotification(String message) {
    if (notificationDevices) {
        notificationDevices.each { it.deviceNotification(message) }
    }
}

// ========================================
// Logging
// ========================================

def logInfo(String msg) {
    if (logLevel in ["Info", "Debug", "Trace"]) log.info msg
}

def logDebug(String msg) {
    if (logLevel in ["Debug", "Trace"]) log.debug msg
}

def logTrace(String msg) {
    if (logLevel == "Trace") log.trace msg
}
