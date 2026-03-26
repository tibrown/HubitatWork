/**
 *  SilentCheck
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
    name: "SilentCheck",
    namespace: "timbrown",
    author: "Tim Brown",
    description: "Alerts once per hour between configured times if the silent switch is on or Ring is disarmed",
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
    dynamicPage(name: "mainPage", title: "SilentCheck", install: true, uninstall: true) {
        section("<b>═══════════════════════════════════════</b>\n<b>CHECK WINDOW</b>\n<b>═══════════════════════════════════════</b>") {
            input "windowStart", "time", title: "Window Start Time", required: true,
                description: "Checks begin at this time each day"
            input "windowEnd", "time", title: "Window End Time", required: true,
                description: "Checks stop at this time each day"
        }

        section("<b>═══════════════════════════════════════</b>\n<b>SWITCHES TO MONITOR</b>\n<b>═══════════════════════════════════════</b>") {
            input "silentSwitch", "capability.switch", title: "Silent Mode Switch", required: false,
                description: "Alert when this switch is ON during the check window"
            input "ringModeSwitch", "capability.switch", title: "Ring Mode Switch (RingModeOnOff)", required: false,
                description: "Alert when this switch is OFF (Ring disarmed) during the check window"
        }

        section("<b>═══════════════════════════════════════</b>\n<b>NOTIFICATION DEVICES</b>\n<b>═══════════════════════════════════════</b>") {
            input "notificationDevices", "capability.notification", title: "Notification Devices",
                multiple: true, required: false
        }

        section("<b>═══════════════════════════════════════</b>\n<b>LOGGING</b>\n<b>═══════════════════════════════════════</b>") {
            input "logLevel", "enum", title: "Logging Level",
                options: ["None", "Info", "Debug", "Trace"],
                defaultValue: "Info", required: true
        }
    }
}

def installed() {
    logInfo "SilentCheck installed"
    initialize()
}

def updated() {
    logInfo "SilentCheck updated"
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    logInfo "Initializing SilentCheck"

    // Schedule hourly check
    runEvery1Hour(performChecks)
    logDebug "Scheduled hourly checks"

    // Run immediately only if we're already inside the check window
    if (isInCheckWindow()) {
        logInfo "Currently inside check window - running initial check"
        performChecks()
    } else {
        logDebug "Outside check window - skipping initial check"
    }
}

// ==================== Check Logic ====================

def performChecks() {
    if (!isInCheckWindow()) {
        logDebug "performChecks: outside check window, skipping"
        return
    }

    logDebug "performChecks: inside window, checking switch states"

    if (silentSwitch && silentSwitch.currentValue("switch") == "on") {
        logInfo "Silent switch is ON during check window - sending alert"
        sendNotification("Warning: Silent is still engaged")
    }

    if (ringModeSwitch && ringModeSwitch.currentValue("switch") == "off") {
        logInfo "Ring mode switch is OFF (disarmed) during check window - sending alert"
        sendNotification("Ring is Disarmed")
    }
}

/**
 * Returns true if the current time is within the configured window
 */
def isInCheckWindow() {
    if (!windowStart || !windowEnd) {
        logDebug "isInCheckWindow: window times not configured"
        return false
    }
    def now = new Date()
    def start = timeToday(windowStart, location.timeZone)
    def end   = timeToday(windowEnd,   location.timeZone)
    logTrace "isInCheckWindow: now=${now}, start=${start}, end=${end}"
    return timeOfDayIsBetween(start, end, now, location.timeZone)
}

// ==================== Notification ====================

def sendNotification(String message) {
    logInfo "Sending notification: ${message}"

    if (notificationDevices) {
        notificationDevices.each { device ->
            device.deviceNotification(message)
        }
    }
}

// ==================== Logging ====================

def logInfo(String msg) {
    if (logLevel in ["Info", "Debug", "Trace"]) {
        log.info msg
    }
}

def logDebug(String msg) {
    if (logLevel in ["Debug", "Trace"]) {
        log.debug msg
    }
}

def logTrace(String msg) {
    if (logLevel == "Trace") {
        log.trace msg
    }
}
