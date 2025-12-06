/**
 *  Ring Person Detection Manager
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
    name: "Ring Person Detection Manager",
    namespace: "timbrown",
    author: "Tim Brown",
    description: "Monitors RPD switches, sets LRP* timestamps, and takes mode-based actions for Ring person detection",
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
    dynamicPage(name: "mainPage", title: "Ring Person Detection Manager", install: true, uninstall: true) {
        section("<b>RPD Switches</b>") {
            paragraph "Select the Ring Person Detection virtual switches."
            input "rpdBackDoor", "capability.switch", title: "RPD BackDoor Switch", required: false
            input "rpdBirdHouse", "capability.switch", title: "RPD BirdHouse Switch", required: false
            input "rpdFrontDoor", "capability.switch", title: "RPD FrontDoor Switch", required: false
            input "rpdGarden", "capability.switch", title: "RPD Garden Switch", required: false
            input "rpdCPen", "capability.switch", title: "RPD CPen Switch", required: false
            input "rpdRearGate", "capability.switch", title: "RPD RearGate Switch", required: false
        }
        
        section("<b>Mode Configuration</b>") {
            input "nightModes", "mode", title: "Night Modes", multiple: true, required: false,
                description: "Modes for enhanced night security actions"
            input "eveningModes", "mode", title: "Evening Modes", multiple: true, required: false,
                description: "Modes for evening-specific actions"
        }
        
        section("<b>Notification Devices</b>") {
            input "notificationDevices", "capability.notification", title: "Notification Devices", 
                multiple: true, required: false
            input "alexaDevice", "capability.speechSynthesis", title: "Alexa Device for Announcements", 
                required: false
            input "guestRoomEcho", "capability.notification", title: "Guest Room Echo (for whisper)", 
                required: false
        }
        
        section("<b>Control Switches</b>") {
            input "silentSwitch", "capability.switch", title: "Silent Mode Switch", required: false
            input "silentBackdoorSwitch", "capability.switch", title: "Silent Backdoor Switch", required: false
            input "allLightsSwitch", "capability.switch", title: "All Lights ON Switch", required: false
            input "rearGateActiveSwitch", "capability.switch", title: "Rear Gate Active Switch", required: false
        }
        
        section("<b>Timing Configuration</b>") {
            input "backdoorResetDelay", "number", title: "Backdoor Reset Delay (seconds)", 
                defaultValue: 3, range: "1..30", required: false
            input "frontDoorResetDelay", "number", title: "Front Door Reset Delay (seconds)", 
                defaultValue: 10, range: "1..60", required: false
        }
        
        section("<b>Hub Variables</b>") {
            paragraph "The following hub variables will be set when person detection occurs:"
            paragraph "• <b>LRPBackDoor</b> - Last Ring Person BackDoor timestamp\n" +
                      "• <b>LRPBirdHouse</b> - Last Ring Person BirdHouse timestamp\n" +
                      "• <b>LRPFrontDoor</b> - Last Ring Person FrontDoor timestamp\n" +
                      "• <b>LRPGarden</b> - Last Ring Person Garden timestamp\n" +
                      "• <b>LRPCPen</b> - Last Ring Person CPen timestamp\n" +
                      "• <b>LRPRearGate</b> - Last Ring Person RearGate timestamp"
        }
        
        section("<b>Logging</b>") {
            input "logLevel", "enum", title: "Logging Level", 
                options: ["None", "Info", "Debug", "Trace"], 
                defaultValue: "Info", required: true
        }
    }
}

def installed() {
    logInfo "Ring Person Detection Manager installed"
    initialize()
}

def updated() {
    logInfo "Ring Person Detection Manager updated"
    unsubscribe()
    initialize()
}

def initialize() {
    logInfo "Initializing Ring Person Detection Manager"
    
    // Subscribe to RPD switches - react when they turn on
    if (rpdBackDoor) {
        subscribe(rpdBackDoor, "switch.on", handleRPDBackDoor)
        logDebug "Subscribed to RPDBackDoor"
    }
    if (rpdBirdHouse) {
        subscribe(rpdBirdHouse, "switch.on", handleRPDBirdHouse)
        logDebug "Subscribed to RPDBirdHouse"
    }
    if (rpdFrontDoor) {
        subscribe(rpdFrontDoor, "switch.on", handleRPDFrontDoor)
        logDebug "Subscribed to RPDFrontDoor"
    }
    if (rpdGarden) {
        subscribe(rpdGarden, "switch.on", handleRPDGarden)
        logDebug "Subscribed to RPDGarden"
    }
    if (rpdCPen) {
        subscribe(rpdCPen, "switch.on", handleRPDCPen)
        logDebug "Subscribed to RPDCPen"
    }
    if (rpdRearGate) {
        subscribe(rpdRearGate, "switch.on", handleRPDRearGate)
        logDebug "Subscribed to RPDRearGate"
    }
    
    logInfo "Subscriptions complete"
}

// ==================== RPD Switch Handlers ====================

def handleRPDBackDoor(evt) {
    logInfo "Person detected at BackDoor"
    
    // Set timestamp (from RPDBackDoor rule - but it didn't set LRP, just reset switch with delay)
    setLastPersonTime("BackDoor", "LRPBackDoor")
    
    // Reset switch after delay (from RPDBackDoor rule: 3 second delay)
    def delay = backdoorResetDelay ?: 3
    runIn(delay, "resetRPDBackDoor")
    
    // Mode-based actions (from RPDBackDoor rule: Night mode, silent switches off)
    if (isNightMode() && !isSilent() && !isSilentBackdoor()) {
        sendNotification("Alert, person detected at the backdoor")
    }
}

def handleRPDBirdHouse(evt) {
    logInfo "Person detected at BirdHouse"
    
    // Set timestamp and reset switch (from RPDBirdHouse rule)
    setLastPersonTime("BirdHouse", "LRPBirdHouse")
    rpdBirdHouse.off()
    
    // Night mode actions (from Night-RPDBirdHouse rule)
    if (isNightMode()) {
        sendNotification("Person Detected at the Bird House")
        setGlobalVar("EchoMessage", "Person detected at the birdhouse")
        
        // Turn on lights
        if (allLightsSwitch) {
            allLightsSwitch.on()
        }
        
        // Whisper to guest room
        if (guestRoomEcho) {
            guestRoomEcho.deviceNotification("Person detected at birdhouse")
        }
    }
}

def handleRPDFrontDoor(evt) {
    logInfo "Person detected at FrontDoor"
    
    // Set timestamp
    setLastPersonTime("FrontDoor", "LRPFrontDoor")
    
    // Reset switch after delay (from RPDFrontDoor rule: 10 second delay)
    def delay = frontDoorResetDelay ?: 10
    runIn(delay, "resetRPDFrontDoor")
    
    // Night mode actions (from Night-PersonAtFrontDoor - if it exists)
    if (isNightMode()) {
        sendNotification("Person detected at the front door")
        if (allLightsSwitch) {
            allLightsSwitch.on()
        }
    }
}

def handleRPDGarden(evt) {
    logInfo "Person detected at Garden"
    
    // Set timestamp and reset switch (from RPDGarden rule)
    setLastPersonTime("Garden", "LRPGarden")
    rpdGarden.off()
    
    // Night mode actions (from Night-RPDGarden rule)
    if (isNightMode()) {
        sendNotification("Person detected at the greenhouse")
        if (allLightsSwitch) {
            allLightsSwitch.on()
        }
    }
    
    // Evening mode actions (from EveningRPDGarden rule)
    if (isEveningMode()) {
        sendNotification("Person detected at the garden")
    }
}

def handleRPDCPen(evt) {
    logInfo "Person detected at CPen"
    
    // Set timestamp and reset switch (from RPDCPen rule)
    setLastPersonTime("CPen", "LRPCPen")
    rpdCPen.off()
    
    // Night mode actions - this location triggers Night-RPDRearGate
    // which checks time since last detection at pen
    if (isNightMode()) {
        // Turn on rear gate active switch
        if (rearGateActiveSwitch) {
            rearGateActiveSwitch.on()
        }
        sendNotification("Someone at rear gate/pen area")
    }
}

def handleRPDRearGate(evt) {
    logInfo "Person detected at RearGate"
    
    // Set timestamp
    setLastPersonTime("RearGate", "LRPRearGate")
    
    // Reset switch
    if (rpdRearGate) {
        rpdRearGate.off()
    }
    
    // Mode-based actions
    if (isNightMode()) {
        if (rearGateActiveSwitch) {
            rearGateActiveSwitch.on()
        }
        sendNotification("Person detected at rear gate")
    }
}

// ==================== Reset Methods ====================

def resetRPDBackDoor() {
    if (rpdBackDoor) {
        rpdBackDoor.off()
        logDebug "Reset RPDBackDoor switch"
    }
}

def resetRPDFrontDoor() {
    if (rpdFrontDoor) {
        rpdFrontDoor.off()
        logDebug "Reset RPDFrontDoor switch"
    }
}

// ==================== Helper Methods ====================

/**
 * Sets the hub variable timestamp for person detection
 */
def setLastPersonTime(String location, String hubVarName) {
    def timestamp = now() / 1000  // Convert to seconds for consistency with existing vars
    
    logInfo "Setting ${hubVarName} = ${timestamp}"
    setGlobalVar(hubVarName, timestamp)
}

/**
 * Send notification to all configured devices
 */
def sendNotification(String message) {
    logInfo "Sending notification: ${message}"
    
    if (notificationDevices) {
        notificationDevices.each { device ->
            device.deviceNotification(message)
        }
    }
    
    // Alexa announcement (unless silent)
    if (alexaDevice && !isSilent()) {
        alexaDevice.speak(message)
    }
}

def isNightMode() {
    if (!nightModes) return false
    return location.mode in nightModes
}

def isEveningMode() {
    if (!eveningModes) return false
    return location.mode in eveningModes
}

def isSilent() {
    if (!silentSwitch) return false
    return silentSwitch.currentValue("switch") == "on"
}

def isSilentBackdoor() {
    if (!silentBackdoorSwitch) return false
    return silentBackdoorSwitch.currentValue("switch") == "on"
}

// ==================== Logging Methods ====================

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