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
        section("<b>═══════════════════════════════════════</b>\n<b>RPD SWITCHES</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Select the Ring Person Detection virtual switches."
            input "rpdBackDoor", "capability.switch", title: "RPD BackDoor Switch", required: false
            input "rpdBirdHouse", "capability.switch", title: "RPD BirdHouse Switch", required: false
            input "rpdFrontDoor", "capability.switch", title: "RPD FrontDoor Switch", required: false
            input "rpdGarden", "capability.switch", title: "RPD Garden Switch", required: false
            input "rpdCPen", "capability.switch", title: "RPD CPen Switch", required: false
            input "rpdRearGate", "capability.switch", title: "RPD RearGate Switch", required: false
        }

        section("<b>═══════════════════════════════════════</b>\n<b>ALERT MESSAGES</b>\n<b>═══════════════════════════════════════</b>") {
            input "backdoorPersonMessage", "text", title: "Back Door Alert Message",
                defaultValue: "Alert, person detected at Back Door", required: false
            input "birdhousePersonMessage", "text", title: "Bird House Alert Message",
                defaultValue: "Person detected at Bird House", required: false
            input "frontDoorPersonMessage", "text", title: "Front Door Alert Message",
                defaultValue: "Person detected at Front Door", required: false
            input "gardenPersonMessage", "text", title: "Garden Alert Message",
                defaultValue: "Person detected at Garden", required: false
            input "cPenPersonMessage", "text", title: "Chicken Pen Alert Message",
                defaultValue: "Person detected at Chicken Pen", required: false
            input "rearGatePersonMessage", "text", title: "Rear Gate Alert Message",
                defaultValue: "Person detected at Rear Gate", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>NIGHT MODE CONFIGURATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "nightModes", "mode", title: "Night Modes", multiple: true, required: false,
                description: "Modes for enhanced night security actions (lights, EchoMessage, whisper)"
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>NOTIFICATION DEVICES</b>\n<b>═══════════════════════════════════════</b>") {
            input "notificationDevices", "capability.notification", title: "Notification Devices", 
                multiple: true, required: false
            input "alexaDevice", "capability.speechSynthesis", title: "Alexa Device for Announcements", 
                required: false
            input "guestRoomEcho", "capability.notification", title: "Guest Room Echo (for whisper)", 
                required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>CONTROL SWITCHES</b>\n<b>═══════════════════════════════════════</b>") {
            input "silentSwitch", "capability.switch", title: "Silent Mode Switch", required: false
            input "allLightsSwitch", "capability.switch", title: "All Lights ON Switch", required: false
            input "rearGateActiveSwitch", "capability.switch", title: "Rear Gate Active Switch", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>TIMING CONFIGURATION</b>\n<b>═══════════════════════════════════════</b>") {
            input "backdoorResetDelay", "number", title: "Backdoor Reset Delay (seconds)", 
                defaultValue: 3, range: "1..30", required: false
            input "frontDoorResetDelay", "number", title: "Front Door Reset Delay (seconds)", 
                defaultValue: 10, range: "1..60", required: false
            input "generalResetDelay", "number", title: "General Reset Delay (seconds, for all other RPD switches)",
                defaultValue: 3, range: "1..30", required: false
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>HUB VARIABLES</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "The following hub variables will be set when person detection occurs:"
            paragraph "• <b>LRPBackDoor</b> - Last Ring Person BackDoor timestamp\n" +
                      "• <b>LRPBirdHouse</b> - Last Ring Person BirdHouse timestamp\n" +
                      "• <b>LRPFrontDoor</b> - Last Ring Person FrontDoor timestamp\n" +
                      "• <b>LRPGarden</b> - Last Ring Person Garden timestamp\n" +
                      "• <b>LRPCPen</b> - Last Ring Person CPen timestamp\n" +
                      "• <b>LRPRearGate</b> - Last Ring Person RearGate timestamp"
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>LOGGING</b>\n<b>═══════════════════════════════════════</b>") {
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
    
    // Reset all RPD switches to off on startup
    [rpdBackDoor, rpdBirdHouse, rpdFrontDoor, rpdGarden, rpdCPen, rpdRearGate].each { sw ->
        if (sw && sw.currentValue("switch") == "on") {
            sw.off()
            logDebug "Reset ${sw.displayName} to off on init"
        }
    }
    
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
    setLastPersonTime("BackDoor", "LRPBackDoor")
    runIn(backdoorResetDelay ?: 3, "resetRPDBackDoor")
    sendNotification(backdoorPersonMessage ?: "Alert, person detected at Back Door")
}

def handleRPDBirdHouse(evt) {
    logInfo "Person detected at BirdHouse"
    setLastPersonTime("BirdHouse", "LRPBirdHouse")
    runIn(generalResetDelay ?: 3, "resetRPDBirdHouse")
    sendNotification(birdhousePersonMessage ?: "Person detected at Bird House")
    if (isNightMode() && !isSilent()) {
        setGlobalVar("EchoMessage", birdhousePersonMessage ?: "Person detected at Bird House")
        if (allLightsSwitch) allLightsSwitch.on()
        if (guestRoomEcho) guestRoomEcho.deviceNotification(birdhousePersonMessage ?: "Person detected at Bird House")
    }
}

def handleRPDFrontDoor(evt) {
    logInfo "Person detected at FrontDoor"
    setLastPersonTime("FrontDoor", "LRPFrontDoor")
    runIn(frontDoorResetDelay ?: 10, "resetRPDFrontDoor")
    sendNotification(frontDoorPersonMessage ?: "Person detected at Front Door")
    if (isNightMode() && allLightsSwitch) allLightsSwitch.on()
}

def handleRPDGarden(evt) {
    logInfo "Person detected at Garden"
    setLastPersonTime("Garden", "LRPGarden")
    runIn(generalResetDelay ?: 3, "resetRPDGarden")
    sendNotification(gardenPersonMessage ?: "Person detected at Garden")
    if (isNightMode() && allLightsSwitch) allLightsSwitch.on()
}

def handleRPDCPen(evt) {
    logInfo "Person detected at CPen"
    setLastPersonTime("CPen", "LRPCPen")
    runIn(generalResetDelay ?: 3, "resetRPDCPen")
    sendNotification(cPenPersonMessage ?: "Person detected at Chicken Pen")
    if (isNightMode() && rearGateActiveSwitch) rearGateActiveSwitch.on()
}

def handleRPDRearGate(evt) {
    logInfo "Person detected at RearGate"
    setLastPersonTime("RearGate", "LRPRearGate")
    runIn(generalResetDelay ?: 3, "resetRPDRearGate")
    sendNotification(rearGatePersonMessage ?: "Person detected at Rear Gate")
    if (isNightMode() && rearGateActiveSwitch) rearGateActiveSwitch.on()
}

// ==================== Reset Methods ====================

def resetRPDBackDoor() {
    if (rpdBackDoor) {
        rpdBackDoor.off()
        logDebug "Reset RPDBackDoor switch"
    }
}

def resetRPDBirdHouse() {
    if (rpdBirdHouse) {
        rpdBirdHouse.off()
        logDebug "Reset RPDBirdHouse switch"
    }
}

def resetRPDFrontDoor() {
    if (rpdFrontDoor) {
        rpdFrontDoor.off()
        logDebug "Reset RPDFrontDoor switch"
    }
}

def resetRPDGarden() {
    if (rpdGarden) {
        rpdGarden.off()
        logDebug "Reset RPDGarden switch"
    }
}

def resetRPDCPen() {
    if (rpdCPen) {
        rpdCPen.off()
        logDebug "Reset RPDCPen switch"
    }
}

def resetRPDRearGate() {
    if (rpdRearGate) {
        rpdRearGate.off()
        logDebug "Reset RPDRearGate switch"
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
    if (isSilent()) {
        logInfo "Silent switch is ON - suppressing notification: ${message}"
        return
    }
    
    logInfo "Sending notification: ${message}"
    
    if (notificationDevices) {
        notificationDevices.each { device ->
            device.deviceNotification(message)
        }
    }
    
    if (alexaDevice) {
        alexaDevice.speak(message)
    }
}

def isNightMode() {
    if (!nightModes) return false
    return location.mode in nightModes
}

def isSilent() {
    if (!silentSwitch) return false
    return silentSwitch.currentValue("switch") == "on"
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