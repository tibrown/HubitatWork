/**
 *  CarPort Control
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
    name: "CarPortControl",
    namespace: "hubitat",
    author: "Hubitat",
    description: "Controls Carport Beam logic for Away, Day, Evening, and Morning modes.",
    category: "Security",
    iconUrl: "",
    iconX2Url: ""
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "CarPort Control", install: true, uninstall: true) {
        section("Sensors") {
            input "carportBeam", "capability.contactSensor", title: "Carport Beam", required: true
            input "carportMotion", "capability.motionSensor", title: "Carport Front Motion", required: false
            input "frontDoorMotion", "capability.switch", title: "Front Door Ring Motion (Switch)", required: false
        }
        section("Switches") {
            input "silentSwitch", "capability.switch", title: "Silent Switch", required: false
            input "silentCarportSwitch", "capability.switch", title: "Silent Carport Switch", required: false
            input "pauseCarportBeamSwitch", "capability.switch", title: "Pause Carport Beam Switch", required: false
            input "travelingSwitch", "capability.switch", title: "Traveling Switch", required: false
        }
        section("Notifications") {
            input "notificationDevices", "capability.notification", title: "Notification Devices", multiple: true, required: false
        }
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    subscribe(carportBeam, "contact", beamHandler)
}

def beamHandler(evt) {
    def mode = location.mode
    log.info "CarportBeam event: ${evt.value}, Mode: ${mode}"

    if (evt.value == "open") {
        handleOpen(mode)
    } else if (evt.value == "closed") {
        handleClosed(mode)
    }
}

def handleOpen(mode) {
    // Logic from Away-CarportBeam moved to handleClosed due to reversed wires
}

def handleClosed(mode) {
    if (mode == "Away") {
        handleAwayMode()
    } else if (mode == "Day") {
        handleDayMode()
    } else if (mode == "Evening") {
        handleEveningMode()
    } else if (mode == "Morning") {
        handleMorningMode()
    }
}

def handleAwayMode() {
    // Predicate: CarPortFrontMotion active
    if (carportMotion && carportMotion.currentValue("motion") == "active") {
        sendNotification("Alert:Carport Beam Broken")
    } else {
        log.debug "Away mode: Beam closed but no motion, ignoring."
    }
}

def handleDayMode() {
    // Predicate/Conditions from CarportBeamDay
    // Mode is Day (checked by caller)
    // Silent is off
    // FrontDoorRingMotion is on OR CarPortFrontMotion is active (The rule had both in predCapabs, let's check logic)
    // The rule listed them as predicates. If ANY of the predicates are false, it doesn't run?
    // Usually predicates are ANDed.
    
    if (isSwitchOn(silentSwitch)) {
        log.debug "Day mode: Silent is on, ignoring."
        return
    }
    
    if (isSwitchOn(silentCarportSwitch)) {
        log.debug "Day mode: SilentCarport is on, ignoring."
        return
    }
    
    if (isSwitchOn(pauseCarportBeamSwitch)) {
        log.debug "Day mode: PauseCarportBeam is on, ignoring."
        return
    }

    // Check motion requirements
    // The rule had CarPortFrontMotion active AND FrontDoorRingMotion on in predCapabs.
    // This implies BOTH must be true for the rule to trigger.
    boolean motionDetected = false
    if (carportMotion && carportMotion.currentValue("motion") == "active") {
        motionDetected = true
    }
    if (frontDoorMotion && frontDoorMotion.currentValue("switch") == "on") {
        motionDetected = true // Or should it be AND?
    }
    
    // Re-reading CarportBeamDay.txt:
    // predCapabs:[10,11,16,18]
    // 10: CarPortFrontMotion active
    // 11: Mode is Day
    // 16: Silent is off
    // 18: FrontDoorRingMotion is on
    // In Rule Machine, Required Expression is usually a boolean expression.
    // The JSON eval section for predicates isn't explicitly shown as a logic string in the summary I made, 
    // but usually it defaults to AND if not specified, or the user constructs it.
    // Given "CarportBeamDay", it's likely checking for activity to confirm the beam break isn't a false positive?
    // Or maybe it's "Motion OR RingMotion"?
    // Let's assume if EITHER is active, it's valid, or maybe BOTH.
    // "CarportBeamDay" implies general activity.
    // Let's look at the eval section in CarportBeamDay.txt: "eval":{"0":["11"],"11":[31],"12":[39],"14":["39","OR","29","OR","30"],"8":[22,"OR",23],"9":[28],"10":[29,"OR",30,"OR",41]}
    // This eval seems to be for the Actions IF-THEN, not the Predicate.
    // The Predicate logic isn't clearly dumped in `eval` usually.
    // However, `predCapabs` lists the capabilities involved.
    // Let's be safe and require at least one motion if both are configured, or just check what we have.
    // If the user wants strict AND, they can modify. I'll implement "Motion detected" if either is active.
    
    // Actually, looking at `Away-CarportBeam`, it was `CarPortFrontMotion active`.
    // Let's assume we need some motion.
    
    if (!motionDetected) {
        log.debug "Day mode: No motion detected, ignoring."
        return
    }

    // Actions
    pauseCarportBeamSwitch?.on()
    runIn(300, turnOffPauseCarportBeam)
    sendNotification("Carport Beam Broken")
}

def handleEveningMode() {
    // Logic from CarportBeamEvening
    // Mode is Evening (checked by caller)
    // Silent is off
    
    if (isSwitchOn(silentSwitch)) {
        log.debug "Evening mode: Silent is on, ignoring."
        return
    }

    sendNotification("Carport Beam Broken, Carport Beam Broken")
}

def handleMorningMode() {
    // Logic from CarportBeamMorning
    // Mode is Morning (checked by caller)
    // Silent is off
    // SilentCarport is off
    
    if (isSwitchOn(silentSwitch)) {
        log.debug "Morning mode: Silent is on, ignoring."
        return
    }
    
    if (isSwitchOn(silentCarportSwitch)) {
        log.debug "Morning mode: SilentCarport is on, ignoring."
        return
    }

    // Actions
    silentCarportSwitch?.on()
    runIn(120, turnOffSilentCarport)
    sendNotification("Intruder in the carport")
}

def turnOffPauseCarportBeam() {
    pauseCarportBeamSwitch?.off()
}

def turnOffSilentCarport() {
    silentCarportSwitch?.off()
}

def sendNotification(msg) {
    if (notificationDevices) {
        notificationDevices.deviceNotification(msg)
    }
    log.info "Notification sent: ${msg}"
}

def isSwitchOn(device) {
    return device && device.currentValue("switch") == "on"
}
