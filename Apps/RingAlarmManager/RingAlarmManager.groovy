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
            input "repeatCount", "number", title: "Extra ON Sends", required: true, defaultValue: 2, range: "1..5",
                description: "How many additional OFF→ON cycles to send after the first (total sends = this + 1)"
            input "repeatDelay", "number", title: "Delay Before Each Repeat (seconds)", required: true, defaultValue: 15, range: "5..120",
                description: "Seconds to wait after the previous ON before starting the next OFF→ON cycle"
            input "offDelay", "number", title: "OFF-to-ON Delay Within Each Repeat (seconds)", required: true, defaultValue: 5, range: "1..30",
                description: "Seconds to hold the switch OFF before sending ON in each repeat cycle"
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
    initialize()
}

def initialize() {
    logInfo "Initializing Ring Alarm Manager (repeatCount=${repeatCount}, repeatDelay=${repeatDelay}s)"
    subscribe(ringModeOnOff, "switch.on", handleRingModeOn)
    subscribe(ringModeOnOff, "switch.off", handleRingModeOff)
}

// ========================================
// EVENT HANDLERS
// ========================================

def handleRingModeOn(evt) {
    // Guard against self-triggered events: calling ringModeOnOff.on() inside sendNextRingOn
    // may cause some virtual switch drivers to fire a fresh switch.on event. If repeat sends
    // are already pending, this ON was self-generated — ignore it.
    if (state.repeatSendsPending) {
        logDebug "Received switch.on while repeat sends are in progress — ignoring (likely self-generated)"
        return
    }

    Integer count = (repeatCount ?: 2) as Integer
    Integer delay = (repeatDelay ?: 15) as Integer
    logInfo "RingModeOnOff turned ON — scheduling ${count} repeat send(s) at ${delay}s intervals"

    state.repeatSendsPending = true
    state.repeatsRemaining = count
    unschedule(sendNextRingOn)
    runIn(delay, sendNextRingOn)
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
    Integer total = (repeatCount ?: 2) as Integer
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
    Integer total = (repeatCount ?: 2) as Integer
    Integer sendNumber = total - remaining + 1

    logInfo "Repeat ${sendNumber} of ${total}: sending ON"
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
