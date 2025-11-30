definition(
    name: "Night Security Manager",
    namespace: "hubitat",
    author: "Gemini 3 Pro",
    description: "Consolidated Night Security Rules",
    category: "Security",
    iconUrl: "",
    iconX2Url: ""
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Night Security Manager", install: true, uninstall: true) {
        section("Sensors") {
            input "doorBHScreen", "capability.contactSensor", title: "BH Screen Door", required: true
            input "carportBeam", "capability.contactSensor", title: "Carport Beam", required: true
            input "carportFrontMotion", "capability.motionSensor", title: "Carport Front Motion", required: true
            input "concreteShedZooz", "capability.contactSensor", title: "Concrete Shed Door", required: true
            input "doorDiningRoom", "capability.contactSensor", title: "Dining Room Door", required: true
            input "doorLivingRoomFrench", "capability.contactSensor", title: "Living Room French Doors", required: true
            input "doorFront", "capability.contactSensor", title: "Front Door", required: true
            input "woodshedDoor", "capability.contactSensor", title: "Woodshed Door", required: true
            input "rpdFrontDoor", "capability.switch", title: "RPD Front Door (Switch)", required: true
            input "rpdBirdHouse", "capability.switch", title: "RPD Bird House (Switch)", required: true
            input "rpdGarden", "capability.switch", title: "RPD Garden (Switch)", required: true
            input "rpdCPen", "capability.switch", title: "RPD Rear Gate (Switch)", required: true
            input "chickenPenOutside", "capability.motionSensor", title: "Chicken Pen Outside Motion", required: true
            input "doorBirdHouse", "capability.contactSensor", title: "She Shed Door (BirdHouse)", required: true
            input "outsideBackdoor", "capability.motionSensor", title: "Outside Backdoor Motion", required: true
            input "floodSide", "capability.motionSensor", title: "Flood Side Motion", required: true
            input "doorLanai", "capability.contactSensor", title: "Lanai Door (Backdoor)", required: true
        }
        
        section("Switches & Controls") {
            input "traveling", "capability.switch", title: "Traveling Switch", required: true
            input "silent", "capability.switch", title: "Silent Switch", required: true
            input "highAlert", "capability.switch", title: "High Alert Switch", required: true
            input "alarmsEnabled", "capability.switch", title: "Alarms Enabled Switch", required: true
            input "pauseDRDoorAlarm", "capability.switch", title: "Pause DR Door Alarm", required: true
            input "masterOff", "capability.switch", title: "Master Off Switch", required: true
            input "pauseBDAlarm", "capability.switch", title: "Pause Backdoor Alarm", required: true
            input "rearGateActive", "capability.switch", title: "Rear Gate Active Switch", required: true
            input "allLightsOn", "capability.switch", title: "All Lights ON Switch", required: true
        }

        section("Notification Devices") {
            input "notificationDevices", "capability.notification", title: "Notification Devices", multiple: true, required: true
        }

        section("Actions / Outputs") {
            input "sirens", "capability.alarm", title: "Sirens", multiple: true, required: true
            input "allLights", "capability.switch", title: "All Lights", multiple: true, required: true
            input "guestRoomEcho", "capability.notification", title: "Guest Room Echo", required: true
        }

        section("Restrictions") {
            input "restrictedModes", "mode", title: "Only run in these modes", multiple: true, required: true
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
    subscribe(doorBHScreen, "contact", evtHandler)
    subscribe(carportBeam, "contact", evtHandler)
    subscribe(concreteShedZooz, "contact", evtHandler)
    subscribe(doorDiningRoom, "contact", evtHandler)
    subscribe(doorLivingRoomFrench, "contact", evtHandler)
    subscribe(doorFront, "contact", evtHandler)
    subscribe(woodshedDoor, "contact", evtHandler)
    subscribe(rpdFrontDoor, "switch", evtHandler)
    subscribe(rpdBirdHouse, "switch", evtHandler)
    subscribe(rpdGarden, "switch", evtHandler)
    subscribe(rpdCPen, "switch", evtHandler)
    subscribe(doorBirdHouse, "contact", evtHandler)
    subscribe(outsideBackdoor, "motion", evtHandler)
    subscribe(doorLanai, "contact", evtHandler)
}

def evtHandler(evt) {
    log.debug "Event: ${evt.name} ${evt.value} from ${evt.displayName}"
    
    if (restrictedModes && !restrictedModes.contains(location.mode)) {
        log.debug "Skipping event: Mode is ${location.mode}, required: ${restrictedModes}"
        return
    }

    if (evt.deviceId == doorBHScreen.deviceId) handleBHScreen(evt)
    else if (evt.deviceId == carportBeam.deviceId) handleCarportBeam(evt)
    else if (evt.deviceId == concreteShedZooz.deviceId) handleConcreteShed(evt)
    else if (evt.deviceId == doorDiningRoom.deviceId) handleDiningRoomDoor(evt)
    else if (evt.deviceId == doorLivingRoomFrench.deviceId) handleLRFrenchDoors(evt)
    else if (evt.deviceId == doorFront.deviceId) handleFrontDoor(evt)
    else if (evt.deviceId == woodshedDoor.deviceId) handleWoodshed(evt)
    else if (evt.deviceId == rpdFrontDoor.deviceId) handleRPDFrontDoor(evt)
    else if (evt.deviceId == rpdBirdHouse.deviceId) handleRPDBirdHouse(evt)
    else if (evt.deviceId == rpdGarden.deviceId) handleRPDGarden(evt)
    else if (evt.deviceId == rpdCPen.deviceId) handleRPDRearGate(evt)
    else if (evt.deviceId == doorBirdHouse.deviceId) handleSheShed(evt)
    else if (evt.deviceId == outsideBackdoor.deviceId) handleBackdoorMotion(evt)
    else if (evt.deviceId == doorLanai.deviceId) handleIntruderBackdoor(evt)
}

def handleBHScreen(evt) {
    if (evt.value == "open" && traveling.currentSwitch == "off") {
        notificationDevices.each { it.deviceNotification("BH Screen Door Open") }
    }
}

def handleCarportBeam(evt) {
    // Update Time Vars logic omitted as it's usually handled by system time, 
    // but we can implement specific checks if needed.
    
    if (evt.value == "open") { // Beam Broken/Active
         // Check Time Logic (10:30 PM to 6:00 AM) or High Alert
         def now = new Date()
         def start = timeToday("22:30", location.timeZone)
         def end = timeToday("06:00", location.timeZone)
         // Adjust end for next day if needed, or use timeOfDayIsBetween
         
         boolean timeCondition = timeOfDayIsBetween(start, end, now, location.timeZone)
         
         if (silent.currentSwitch == "off" && carportFrontMotion.currentMotion == "active" && (timeCondition || highAlert.currentSwitch == "on")) {
             notificationDevices.each { it.deviceNotification("Alert! Intruder in the carport!") }
             runIn(5, executeAlarmsOn)
         }
    } else if (evt.value == "closed") {
        notificationDevices.each { it.deviceNotification("Beam Broken") }
    }
}

def executeAlarmsOn() {
    if (alarmsEnabled.currentSwitch == "on") {
        sirens.each { it.siren() }
        runIn(300, stopAlarms)
    }
}

def stopAlarms() {
    sirens.each { it.off() }
}

def executeShedSirenOn() {
    sirens.each { it.siren() }
    runIn(4, stopShedSiren)
}

def stopShedSiren() {
    sirens.each { it.off() }
}

def turnAllLightsOnNow() {
    allLights.on()
    allLightsOn.on()
}

def whisperToGuestroomNow() {
    def msg = getGlobalVar("EchoMessage").value
    guestRoomEcho.deviceNotification(msg)
}

def handleConcreteShed(evt) {
    if (evt.value == "open" && alarmsEnabled.currentSwitch == "on" && siren1.currentSwitch == "off") {
        notificationDevices.each { it.deviceNotification("Intruder in the Concrete Shed") }
        executeShedSirenOn()
        turnAllLightsOnNow()
    }
}

def handleDiningRoomDoor(evt) {
    if (evt.value == "open" && alarmsEnabled.currentSwitch == "on" && silent.currentSwitch == "off" && pauseDRDoorAlarm.currentSwitch == "off") {
        turnAllLightsOnNow()
        notificationDevices.each { it.deviceNotification("Intruder at the Dining Room Door") }
        runIn(5, executeAlarmsOn)
    }
}

def handleLRFrenchDoors(evt) {
    if (evt.value == "open" && alarmsEnabled.currentSwitch == "on") {
        turnAllLightsOnNow()
        notificationDevices.each { it.deviceNotification("Intruder at the Living Room French Doors") }
        runIn(5, executeAlarmsOn)
    }
}

def handleFrontDoor(evt) {
    if (evt.value == "open" && alarmsEnabled.currentSwitch == "on" && silent.currentSwitch == "off") {
        turnAllLightsOnNow()
        notificationDevices.each { it.deviceNotification("Intruder at the Front Door") }
        runIn(5, executeAlarmsOn)
    }
}

def handleWoodshed(evt) {
    if (evt.value == "open" && silent.currentSwitch == "off" && masterOff.currentSwitch == "off" && alarmsEnabled.currentSwitch == "on") {
        notificationDevices.each { it.deviceNotification("Intruder in the Woodshed") }
        executeShedSirenOn()
        turnAllLightsOnNow()
    }
}

def handleRPDFrontDoor(evt) {
    if (evt.value == "on") {
        notificationDevices.each { it.deviceNotification("Person at the Front Door") }
        allLightsOn.on()
    }
}

def handleRPDBirdHouse(evt) {
    if (evt.value == "on") {
        notificationDevices.each { it.deviceNotification("Intruder at the Bird House") }
        allLightsOn.on()
        setGlobalVar("EchoMessage", "Intruder at the Bird House")
        whisperToGuestroomNow()
    }
}

def handleRPDGarden(evt) {
    if (evt.value == "on") {
        notificationDevices.each { it.deviceNotification("Intruder in the Garden") }
        allLightsOn.on()
    }
}

def handleRPDRearGate(evt) {
    if (evt.value == "on" && chickenPenOutside.currentMotion == "active") {
         // Time Logic: 8:00 PM to 6:00 AM
         def now = new Date()
         def start = timeToday("20:00", location.timeZone)
         def end = timeToday("06:00", location.timeZone)
         
         if (timeOfDayIsBetween(start, end, now, location.timeZone)) {
             // Set Vars omitted (assuming local usage)
             rearGateActive.on()
             notificationDevices.each { it.deviceNotification("Intruder at the Rear Gate") }
         }
    }
}

def handleSheShed(evt) {
    if (evt.value == "open" && silent.currentSwitch == "off") {
        allLightsOn.on()
        notificationDevices.each { it.deviceNotification("Intruder in the She Shed") }
        executeShedSirenOn()
    }
}

def handleBackdoorMotion(evt) {
    if (evt.value == "active" && floodSide.currentMotion == "active" && highAlert.currentSwitch == "on") {
        turnAllLightsOnNow()
        notificationDevices.each { it.deviceNotification("Intruder at the Backdoor") }
    }
}

def handleIntruderBackdoor(evt) {
    if (evt.value == "open" && pauseBDAlarm.currentSwitch == "off" && silent.currentSwitch == "off" && alarmsEnabled.currentSwitch == "on") {
        setGlobalVar("AlertMessage", "Intruder at the Backdoor")
        executeAlarmsOn()
    }
}
