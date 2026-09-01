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
            input "silenceOfficeSwitch", "capability.switch", title: "Silence Office Switch", required: false,
                description: "Alert when this switch is ON during the check window"
            input "ringModeSwitch", "capability.switch", title: "Ring Mode Switch (RingModeOnOff)", required: false,
                description: "Alert when this switch is OFF (Ring disarmed) during the check window"
        }

        section("<b>═══════════════════════════════════════</b>\n<b>NOTIFICATION DEVICES</b>\n<b>═══════════════════════════════════════</b>") {
            input "notificationDevices", "capability.notification", title: "Notification Devices",
                multiple: true, required: false
        }

        section("<b>═══════════════════════════════════════</b>\n<b>AI \"WHY\" EXPLAINER</b>\n<b>═══════════════════════════════════════</b>") {
            input "dashboardAiUrl", "text", title: "Dashboard AI Base URL", required: false,
                description: "Hubitat Dashboard base URL, e.g. http://192.168.1.x:3000. When set, alerts include a plain-English \"why\" line from POST /ai/explain-alarm. Leave empty to send original messages only."
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

    if (silenceOfficeSwitch && silenceOfficeSwitch.currentValue("switch") == "on") {
        logInfo "Silence Office switch is ON during check window - sending alert"
        sendNotification("Warning: Silence Office is still engaged")
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

    // Deliver the original alert message IMMEDIATELY and unchanged. This must
    // never depend on the AI call.
    if (notificationDevices) {
        notificationDevices.each { device ->
            device.deviceNotification(message)
        }
    }

    // Fire the best-effort AI "why" follow-up asynchronously. It goes out as a
    // SEPARATE notification only when the explanation is ready.
    sendWhyFollowUp(message)
}

// ==================== AI "Why" Explainer ====================

/**
 * Fire-and-forget AI "why" follow-up. Best-effort: if a summary is returned
 * it's sent to the same notification devices as a SEPARATE "Why: ..."
 * notification. On any failure (timeout, 404, empty reply, exception) it logs
 * and sends nothing else - never an error notification, never the original
 * alert text again.
 */
private void sendWhyFollowUp(String contextMessage) {
    String base = settings.dashboardAiUrl?.toString()?.trim()
    if (!base) {
        logDebug "dashboardAiUrl not configured - skipping why follow-up"
        return
    }
    String url = base.endsWith("/") ? base + "ai/explain-alarm" : base + "/ai/explain-alarm"
    try {
        // Async form: httpPostJson(Map, Closure) returns immediately and the
        // closure runs when the response lands (up to timeout seconds later).
        httpPostJson([ uri: url.toString(), timeout: 120, body: buildAlarmState(contextMessage) ]) { resp ->
            if (resp?.status == 200 && resp.data?.summary) {
                String summary = resp.data.summary.toString()
                logDebug "Why follow-up ready for '${contextMessage}'"
                if (notificationDevices) {
                    notificationDevices.each { device ->
                        device.deviceNotification("Why: " + summary)
                    }
                }
            } else {
                logDebug "AI explain endpoint returned status ${resp?.status} without a summary - no follow-up sent"
            }
        }
    } catch (Exception e) {
        logDebug "AI explain follow-up failed for '${contextMessage}': ${e.message}"
    }
}

/**
 * Builds the alarm payload for /ai/explain-alarm from REAL state: armed comes
 * from the ring mode switch (or the AlarmsEnabled hub var), silent from the
 * silent switch, triggered from the AlarmActive hub var (written by
 * SecurityAlarmManager).
 */
private Map buildAlarmState(String contextMessage) {
    def state = [:]
    // Canonical armed state from SecurityAlarmManager hub var, falling back to
    // the monitored ring mode switch (ON = armed) when the var is missing.
    def armedVar = getGlobalVar("AlarmsEnabled")
    if (armedVar != null && armedVar.toString() != "") {
        state.armed = armedVar.toString().toLowerCase() == "true" ? "armed" : "disarmed"
    } else if (ringModeSwitch) {
        state.armed = ringModeSwitch.currentValue("switch") == "on" ? "armed" : "disarmed"
    }
    // Canonical triggered state from SecurityAlarmManager.
    def triggeredVar = getGlobalVar("AlarmActive")
    if (triggeredVar != null && triggeredVar.toString() != "") {
        state.triggered = triggeredVar.toString().toLowerCase() == "true" ? "triggered" : ""
    }
    // Silent state from the locally monitored silent / silence-office switches.
    boolean silentState = (silentSwitch && silentSwitch.currentValue("switch") == "on") ||
                          (silenceOfficeSwitch && silenceOfficeSwitch.currentValue("switch") == "on")
    state.silent = silentState
    state.source = app.label ?: "SilentCheck"
    return [ alarm: state, context: contextMessage ]
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
