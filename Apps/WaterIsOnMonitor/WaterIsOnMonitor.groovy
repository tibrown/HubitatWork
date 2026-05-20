/**
 *  Water Is On Monitor
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
    name: "Water Is On Monitor",
    namespace: "timbrown",
    author: "Tim Brown",
    description: "Monitors the WaterIsOn switch and sends periodic reminder notifications when water has been running longer than a configured threshold",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    singleThreaded: true
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Water Is On Monitor", install: true, uninstall: true) {
        section("<b>WATER SWITCH</b>") {
            input "waterIsOnSwitch", "capability.switch", title: "Water Is On Switch", required: true,
                description: "The switch that indicates water is currently running (e.g. WaterIsOn connector switch)"
        }

        section("<b>TIMING SETTINGS</b>") {
            input "hubVar_WaterTimeout", "number",
                title: "Water Auto-Off Timeout",
                description: "Automatically shut off water after this duration (minutes). Sets WaterTimeout hub variable.",
                defaultValue: 30,
                range: "1..180",
                required: false
            input "repeatInterval", "number", title: "Repeat Notification Interval (minutes)",
                defaultValue: 15, range: "1..120", required: true
        }

        section("<b>NOTIFICATIONS</b>") {
            input "notificationDevices", "capability.notification", title: "Notification Devices",
                multiple: true, required: true
            input "notificationText", "text", title: "Notification Message",
                defaultValue: "Reminder: Water is still on!", required: true
        }

        section("<b>LOGGING</b>") {
            input "logLevel", "enum", title: "Logging Level",
                options: ["None", "Info", "Debug", "Trace"],
                defaultValue: "Info", required: true
        }

        section("<b>CURRENT STATUS</b>") {
            if (waterIsOnSwitch) {
                def currentState = waterIsOnSwitch.currentValue("switch")
                paragraph "Water Is On switch: <b>${currentState?.toUpperCase() ?: 'unknown'}</b>"
            }
        }
    }
}

def installed() {
    logInfo "Water Is On Monitor installed"
    initialize()
}

def updated() {
    logInfo "Water Is On Monitor updated"
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    logInfo "Initializing Water Is On Monitor"
    setGlobalVar("WaterTimeout", (settings.hubVar_WaterTimeout ?: 30).toString())
    if (waterIsOnSwitch) {
        subscribe(waterIsOnSwitch, "switch", waterSwitchHandler)
        logInfo "Subscribed to switch events from ${waterIsOnSwitch.displayName}"
        logDebug "WaterTimeout: ${settings.hubVar_WaterTimeout ?: 30} min, repeat interval: ${settings.repeatInterval ?: 15} min"
    } else {
        logInfo "No water switch configured – nothing to monitor"
    }
    logInfo "Initialization complete"
}

// ========================================
// Event Handler
// ========================================

def waterSwitchHandler(evt) {
    String switchState = evt.value?.toLowerCase()
    logInfo "WATER SWITCH EVENT: '${switchState}'"

    if (switchState == "on") {
        Integer waterTimeoutMinutes = (getGlobalVar("WaterTimeout")?.value as Integer) ?: (settings.hubVar_WaterTimeout ?: 30)
        logInfo "Water turned ON – scheduling notification in ${waterTimeoutMinutes} minute(s) from WaterTimeout"
        unschedule(handleWaterTimerExpired)
        unschedule(sendWaterRepeatReminder)
        Integer delaySecs = ((getGlobalVar("WaterTimeout")?.value as Integer) ?: (settings.hubVar_WaterTimeout ?: 30)) * 60
        runIn(delaySecs, handleWaterTimerExpired)
    } else if (switchState == "off") {
        logInfo "Water turned OFF – cancelling all pending reminders"
        unschedule(handleWaterTimerExpired)
        unschedule(sendWaterRepeatReminder)
    }
}

// ========================================
// Scheduled Handlers
// ========================================

def handleWaterTimerExpired() {
    if (waterIsOnSwitch?.currentValue("switch")?.toLowerCase() != "on") {
        logInfo "Water is no longer on – cancelling notification"
        return
    }

    logInfo "Water timer expired – sending notification"
    sendWaterNotification()

    Integer repeatSecs = (settings.repeatInterval ?: 15) * 60
    logDebug "Scheduling repeat reminder in ${settings.repeatInterval ?: 15} minute(s)"
    runIn(repeatSecs, sendWaterRepeatReminder)
}

def sendWaterRepeatReminder() {
    if (waterIsOnSwitch?.currentValue("switch")?.toLowerCase() != "on") {
        logInfo "Water is no longer on – stopping repeat reminders"
        return
    }

    logInfo "Sending repeat water reminder"
    sendWaterNotification()

    Integer repeatSecs = (settings.repeatInterval ?: 15) * 60
    logDebug "Scheduling next repeat reminder in ${settings.repeatInterval ?: 15} minute(s)"
    runIn(repeatSecs, sendWaterRepeatReminder)
}

// ========================================
// Notifications
// ========================================

def sendWaterNotification() {
    String message = settings.notificationText ?: "Reminder: Water is still on!"
    logInfo "Sending notification: ${message}"
    notificationDevices?.each { device ->
        device.deviceNotification(message)
    }
}

// ========================================
// Logging Helpers
// ========================================

def logInfo(String msg) {
    if (settings.logLevel in ["Info", "Debug", "Trace"]) {
        log.info "[WaterIsOnMonitor] ${msg}"
    }
}

def logDebug(String msg) {
    if (settings.logLevel in ["Debug", "Trace"]) {
        log.debug "[WaterIsOnMonitor] ${msg}"
    }
}

def logTrace(String msg) {
    if (settings.logLevel == "Trace") {
        log.trace "[WaterIsOnMonitor] ${msg}"
    }
}
