/**
 *  Ring Alarm Manager
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
    name: "Ring Alarm Manager",
    namespace: "timbrown",
    author: "Tim Brown",
    description: "Ensures the Ring alarm reliably arms by sending the ON command multiple times with a configurable delay. Subscribes to the RingModeOnOff switch and re-sends ON as needed. Future versions will add direct status checking via the Ring base station.",
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
    dynamicPage(name: "mainPage", title: "Ring Alarm Manager", install: true, uninstall: true) {

        section("<b>═══════════════════════════════════════</b>\n<b>RING MODE SWITCH</b>\n<b>═══════════════════════════════════════</b>") {
            input "ringModeOnOff", "capability.switch", title: "Ring Mode On/Off Switch (RingModeOnOff)", required: true,
                description: "This app is the ONLY app that should subscribe to this switch's events."
        }

        section("<b>═══════════════════════════════════════</b>\n<b>REPEAT CONFIGURATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "repeatCount", "number", title: "Extra ON Sends", required: true, defaultValue: 2, range: "0..5",
                description: "How many additional OFF→ON cycles to send after the first (total sends = this + 1)"
            input "quickRetryDelay", "number", title: "Quick First Retry Delay (seconds)", required: true, defaultValue: 3, range: "1..30",
                description: "Seconds after the initial ON before the first OFF→ON retry — keep short so Ring arms quickly when the first command fails"
            input "repeatDelay", "number", title: "Delay Before Each Subsequent Repeat (seconds)", required: true, defaultValue: 15, range: "5..120",
                description: "Seconds to wait after the quick retry ON before starting each additional OFF→ON cycle"
            input "offDelay", "number", title: "OFF-to-ON Delay Within Each Repeat (seconds)", required: true, defaultValue: 5, range: "1..30",
                description: "Seconds to hold the switch OFF before sending ON in each repeat cycle"
        }

        section("<b>═══════════════════════════════════════</b>\n<b>DELAYED RE-ARM</b>\n<b>═══════════════════════════════════════</b>") {
            input "enableReArmOnDelay", "bool", title: "Re-arm Ring Mode after it is turned OFF?", defaultValue: false, submitOnChange: true,
                description: "When ON, turns RingModeOnOff back ON automatically after the delay below (unless Backdoor alarm is paused)"
            if (settings.enableReArmOnDelay) {
                input "reArmDelay", "number", title: "Re-arm Delay (seconds)", required: true, defaultValue: 60, range: "1..600",
                    description: "Seconds to wait after the switch is turned OFF before turning it back ON"
                input "allowAutoRearmSwitch", "capability.switch", title: "Allow Auto Re-arm Switch (must be ON for delayed re-arm)", required: false,
                    description: "Delayed re-arm only happens while this switch is ON. Turning it OFF cancels any pending re-arm immediately. Leave unset for always-allowed (feature behaves as before)."
                input "pauseBDAlarmSwitch", "capability.switch", title: "Pause Backdoor Alarm Switch (suppress re-arm when ON)", required: false,
                    description: "The shared PauseBDAlarm switch. When it is ON, re-arm is skipped so an intentional disarm while paused is not overridden."
            }
        }

        section("<b>═══════════════════════════════════════</b>\n<b>NOTIFICATIONS</b>\n<b>═══════════════════════════════════════</b>") {
            input "notifyOnRepeat", "bool", title: "Notify on Each Repeat Send", defaultValue: false, required: false,
                description: "Send a push notification each time a repeat ON is fired"
            input "notificationDevices", "capability.notification", title: "Notification Devices", multiple: true, required: false
        }

        section("<b>═══════════════════════════════════════</b>\n<b>CURRENT STATUS</b>\n<b>═══════════════════════════════════════</b>") {
            if (ringModeOnOff) {
                String currentState = ringModeOnOff.currentValue("switch") ?: "unknown"
                paragraph "Ring Mode Switch: <b>${currentState.toUpperCase()}</b>"
                paragraph "<i>Note: Direct Ring alarm status (armed/disarmed from the Ring base station) is not yet readable via this integration. Future versions will add status verification.</i>"
            } else {
                paragraph "<i>Configure the Ring Mode switch above to see its current state.</i>"
            }
        }

        section("<b>═══════════════════════════════════════</b>\n<b>LOGGING</b>\n<b>═══════════════════════════════════════</b>") {
            input "logLevel", "enum", title: "Log Level", options: ["None", "Info", "Debug", "Trace"], defaultValue: "Info"
        }
    }
}

// ========================================
// LIFECYCLE
// ========================================

def installed() {
    logInfo "Ring Alarm Manager installed"
    initialize()
}

def updated() {
    logInfo "Ring Alarm Manager updated"
    unsubscribe()
    unschedule()
    state.remove("repeatSendsPending")
    state.remove("repeatsRemaining")
    state.remove("intentionalOff")
    state.remove("intentionalOn")
    unschedule(reArmRingMode)
    initialize()
}

def initialize() {
    logInfo "Initializing Ring Alarm Manager (repeatCount=${repeatCount}, quickRetryDelay=${quickRetryDelay}s, repeatDelay=${repeatDelay}s)"
    subscribe(ringModeOnOff, "switch.on", handleRingModeOn)
    subscribe(ringModeOnOff, "switch.off", handleRingModeOff)
    if (allowAutoRearmSwitch) {
        subscribe(allowAutoRearmSwitch, "switch", handleAllowAutoRearm)
        logDebug "Subscribed to AllowAutoRearm switch for re-arm gating/cancellation"
    }
    if (pauseBDAlarmSwitch) {
        subscribe(pauseBDAlarmSwitch, "switch", handlePauseBDAlarm)
        logDebug "Subscribed to PauseBDAlarm switch for re-arm cancellation"
    }
}

// ========================================
// EVENT HANDLERS
// ========================================

def handleRingModeOn(evt) {
    // If WE turned the switch on as part of a repeat cycle, ignore the resulting event.
    // intentionalOn is set immediately before ringModeOnOff.on() in doRingOn() so this
    // guard fires even on the last repeat, after repeatSendsPending has been cleared.
    if (state.intentionalOn) {
        state.intentionalOn = false
        logDebug "Received switch.on — intentional (part of repeat cycle), ignoring"
        return
    }
    // Secondary guard: if a self-generated event somehow arrives without the flag set
    if (state.repeatSendsPending) {
        logDebug "Received switch.on while repeat sends are in progress — ignoring (likely self-generated)"
        return
    }

    Integer count = (repeatCount == null ? 2 : repeatCount) as Integer
    Integer firstDelay = (quickRetryDelay ?: 3) as Integer
    logInfo "RingModeOnOff turned ON — quick retry in ${firstDelay}s, then ${count} more repeat(s) at ${repeatDelay ?: 15}s intervals"

    // A genuine ON (manual, other app, or re-arm) supersedes any pending re-arm
    unschedule(reArmRingMode)

    // Extra ON sends disabled — single ON only, no repeat cycle
    if (count <= 0) {
        logInfo "Extra ON sends disabled (repeatCount=0) — sending single ON only, no repeats"
        return
    }

    state.repeatSendsPending = true
    state.repeatsRemaining = count
    unschedule(sendNextRingOn)
    runIn(firstDelay, sendNextRingOn)
}

def handleRingModeOff(evt) {
    // If WE turned the switch off as part of an OFF→ON repeat cycle, do not cancel the sequence.
    if (state.intentionalOff) {
        logDebug "Received switch.off — intentional (part of repeat cycle), ignoring"
        return
    }
    logInfo "RingModeOnOff turned OFF externally — cancelling any pending repeat sends"
    unschedule(sendNextRingOn)
    unschedule(doRingOn)
    state.repeatSendsPending = false
    state.repeatsRemaining = 0
    state.intentionalOff = false
    state.intentionalOn = false

    // Delayed re-arm (optional): schedule turning the switch back ON.
    // Requires: feature enabled, AllowAutoRearm switch ON (or unset), and no backdoor pause.
    if (enableReArmOnDelay && isAllowAutoRearmOn() && !isPauseBDAlarmOn()) {
        scheduleReArm()
    } else if (enableReArmOnDelay) {
        logDebug "Not scheduling re-arm — AllowAutoRearm OFF or PauseBDAlarm ON (guard active)"
    }
}

// ========================================
// CHAINED REPEAT SEND HANDLERS
// Each repeat is a full OFF → wait → ON cycle.
// sendNextRingOn  : turns switch OFF (intentionally), then schedules doRingOn
// doRingOn        : turns switch ON, chains to next repeat if any remain
// ========================================

def sendNextRingOn() {
    // Safety check: if an external OFF arrived between the schedule and now, abort
    if (!state.repeatSendsPending) {
        logInfo "Repeat send aborted — pending flag cleared (external OFF received)"
        return
    }

    Integer remaining = (state.repeatsRemaining ?: 0) as Integer
    Integer total = (repeatCount == null ? 2 : repeatCount) as Integer
    Integer sendNumber = total - remaining + 1
    Integer offWait = (offDelay ?: 5) as Integer

    logInfo "Repeat ${sendNumber} of ${total}: turning OFF, will send ON in ${offWait}s"
    state.intentionalOff = true
    ringModeOnOff.off()
    runIn(offWait, doRingOn)
}

def doRingOn() {
    state.intentionalOff = false

    // Safety check: if pending flag was cleared while we waited (shouldn't happen, but be safe)
    if (!state.repeatSendsPending) {
        logInfo "doRingOn aborted — pending flag cleared while waiting for OFF delay"
        return
    }

    Integer remaining = (state.repeatsRemaining ?: 0) as Integer
    Integer total = (repeatCount == null ? 2 : repeatCount) as Integer
    Integer sendNumber = total - remaining + 1

    logInfo "Repeat ${sendNumber} of ${total}: sending ON"
    state.intentionalOn = true
    ringModeOnOff.on()

    if (notifyOnRepeat) {
        sendNotification("Ring Mode: repeat ON send ${sendNumber} of ${total}")
    }

    state.repeatsRemaining = remaining - 1

    if (remaining - 1 > 0) {
        Integer delay = (repeatDelay ?: 15) as Integer
        runIn(delay, sendNextRingOn)
    } else {
        logDebug "All repeat OFF→ON cycles complete"
        state.repeatSendsPending = false
    }
}

// ========================================
// DELAYED RE-ARM
// ========================================

// True when the Backdoor alarm is paused (PauseBDAlarm switch ON).
private Boolean isPauseBDAlarmOn() {
    return pauseBDAlarmSwitch?.currentValue("switch")?.toLowerCase() == "on"
}

// True when auto re-arm is permitted. Unconfigured = always allowed (backward compatible).
private Boolean isAllowAutoRearmOn() {
    if (!allowAutoRearmSwitch) return true
    return allowAutoRearmSwitch.currentValue("switch")?.toLowerCase() == "on"
}

private void scheduleReArm() {
    unschedule(reArmRingMode)
    Integer delay = (reArmDelay ?: 60) as Integer
    logInfo "Scheduling Ring Mode re-arm in ${delay}s"
    runIn(delay, reArmRingMode)
}

def reArmRingMode() {
    // Fire-time safety guards: if auto re-arm was disallowed or the Backdoor alarm
    // is now paused, do not re-arm (the subscriptions normally cancel the timer already).
    if (!isAllowAutoRearmOn()) {
        logInfo "Re-arm skipped — AllowAutoRearm is OFF (guard active)"
        return
    }
    if (isPauseBDAlarmOn()) {
        logInfo "Re-arm skipped — PauseBDAlarm is ON (guard active)"
        return
    }
    // Do not re-arm if the switch has already been turned ON since the schedule
    if (ringModeOnOff.currentValue("switch")?.toLowerCase() != "on") {
        logInfo "Re-arming Ring Mode (delayed re-arm)"
        // Deliberately do NOT set state.intentionalOn — we want the normal
        // handleRingModeOn path (and its repeat-ON reliability) to run.
        ringModeOnOff.on()
    } else {
        logDebug "Re-arm fired but switch already ON — skipping"
    }
}

def handlePauseBDAlarm(evt) {
    if (evt.value?.toLowerCase() == "on") {
        // Pause is now in effect — cancel any running re-arm delay immediately.
        // Pause's own behavior (via NightSecurityManager/DoorWindowMonitor) is untouched.
        unschedule(reArmRingMode)
        logInfo "PauseBDAlarm turned ON — cancelled any pending re-arm (pause takes over)"
    }
}

def handleAllowAutoRearm(evt) {
    if (evt.value?.toLowerCase() == "off") {
        // Auto re-arm no longer permitted — cancel any running re-arm delay immediately.
        unschedule(reArmRingMode)
        logInfo "AllowAutoRearm turned OFF — cancelled any pending re-arm"
    }
    // When AllowAutoRearm turns ON, no action here — re-arm scheduling resumes
    // naturally on the next external RingModeOnOff OFF event.
}

// ========================================
// NOTIFICATION
// ========================================

private void sendNotification(String message) {
    logDebug "Sending notification: ${message}"
    notificationDevices?.each { it.deviceNotification(message) }
}

// ========================================
// LOGGING
// ========================================

def logInfo(String msg) {
    if (logLevel in ["Info", "Debug", "Trace"]) log.info "${app.label}: ${msg}"
}

def logDebug(String msg) {
    if (logLevel in ["Debug", "Trace"]) log.debug "${app.label}: ${msg}"
}

def logTrace(String msg) {
    if (logLevel == "Trace") log.trace "${app.label}: ${msg}"
}
