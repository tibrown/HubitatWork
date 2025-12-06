/**
 *  Special Automations Manager
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
    name: "Special Automations Manager",
    namespace: "timbrown",
    author: "Tim Brown",
    description: "Manages miscellaneous automations including pet monitoring, work reminders, power monitoring, and communication routing",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    singleThreaded: true
)

preferences {
    page(name: "mainPage")
    page(name: "petMonitoringPage")
    page(name: "workRemindersPage")
    page(name: "powerMonitoringPage")
    page(name: "communicationPage")
    page(name: "miscellaneousPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Special Automations Manager", install: true, uninstall: true) {
        section("Configuration Sections") {
            href "petMonitoringPage", title: "Pet Monitoring", description: "Dog tracking and feeding reminders"
            href "workRemindersPage", title: "Work Reminders", description: "Meetings, PTO, wake-up alarms"
            href "powerMonitoringPage", title: "Power Monitoring", description: "Mains power and utility monitoring"
            href "communicationPage", title: "Communication", description: "Slack, Alexa, phone notifications"
            href "miscellaneousPage", title: "Miscellaneous", description: "Coffee, safe, delays, and more"
        }
        
        section("Logging") {
            input "logLevel", "enum", title: "Logging Level", 
                options: ["None", "Info", "Debug", "Trace"], 
                defaultValue: "Info", required: true
        }
    }
}

def petMonitoringPage() {
    dynamicPage(name: "petMonitoringPage", title: "Pet Monitoring", install: false, uninstall: false) {
        section("Dog Sensors & Switches") {
            input "dogFloorSensor", "capability.contactSensor", title: "Dog Floor Sensor", required: false
            input "dogOnFloorSwitch", "capability.switch", title: "Dog On Floor Switch", required: false
            input "dogsOutsideSwitch", "capability.switch", title: "Dogs Outside Switch", required: false
            input "dogsFedSwitch", "capability.switch", title: "Dogs Fed Switch", required: false
            input "carportZoneActive", "capability.switch", title: "Carport Zone Active (dog detection)", required: false
            input "carportSiren", "capability.alarm", title: "Carport Siren", required: false
        }
        
        section("Dog Feeding Reminders") {
            input "dogFeedingReminderTime", "time", title: "Feeding Reminder Time", required: false,
                description: "Hub variable: dogFeedingReminderTime (HH:mm format)"
            input "enableFeedingReminder", "bool", title: "Enable Feeding Reminders", 
                defaultValue: true, required: false
            input "fedResetTime", "time", title: "Daily Fed Status Reset Time", required: false
        }
        
        section("Dog Outside Monitoring") {
            input "dogOutsideTimeout", "number", title: "Dog Outside Timeout (minutes)", 
                defaultValue: 30, range: "5..120", required: false,
                description: "Hub variable: dogOutsideTimeout"
            input "enableOutsideAlerts", "bool", title: "Enable Outside Timeout Alerts", 
                defaultValue: true, required: false
        }
    }
}

def workRemindersPage() {
    dynamicPage(name: "workRemindersPage", title: "Work Reminders", install: false, uninstall: false) {
        section("Meeting Reminders") {
            input "calendarDevice", "capability.sensor", title: "Calendar Device", required: false
            input "meetingReminderAdvance", "number", title: "Meeting Reminder Advance Time (minutes)", 
                defaultValue: 15, range: "5..60", required: false,
                description: "Hub variable: meetingReminderAdvance"
            input "meetingTimeSwitch", "capability.switch", title: "Meeting Time Switch", required: false
            input "enableMeetingReminders", "bool", title: "Enable Meeting Reminders", 
                defaultValue: true, required: false
        }
        
        section("PTO Management") {
            input "ptoSwitch", "capability.switch", title: "PTO Switch", required: false
            input "enablePtoMode", "bool", title: "Enable PTO Mode Features", 
                defaultValue: true, required: false
        }
        
        section("Wake-Up Alarm") {
            input "wakeUpTime", "time", title: "Wake-Up Time", required: false,
                description: "Hub variable: wakeUpTime (HH:mm format)"
            input "wakeUpDays", "enum", title: "Wake-Up Days", 
                options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], 
                multiple: true, required: false
            input "enableWakeUpAlarm", "bool", title: "Enable Wake-Up Alarm", 
                defaultValue: true, required: false
        }
    }
}

def powerMonitoringPage() {
    dynamicPage(name: "powerMonitoringPage", title: "Power Monitoring", install: false, uninstall: false) {
        section("Mains Power Monitoring") {
            input "mainsPowerSensor", "capability.powerSource", title: "Mains Power Sensor", required: false
            input "onMainsSwitch", "capability.switch", title: "On Mains Switch", required: false
            input "powerMonitorDelay", "number", title: "Power Alert Delay (seconds)", 
                defaultValue: 30, range: "10..300", required: false,
                description: "Hub variable: powerMonitorDelay"
            input "enableMainsMonitoring", "bool", title: "Enable Mains Power Monitoring", 
                defaultValue: true, required: false
        }
        
        section("Water Monitoring") {
            input "waterSwitch", "capability.switch", title: "Water Is On Switch", required: false
        }
    }
}

def communicationPage() {
    dynamicPage(name: "communicationPage", title: "Communication", install: false, uninstall: false) {
        section("Notification Devices") {
            input "notificationDevices", "capability.notification", title: "Push Notification Devices", 
                multiple: true, required: false
            input "alexaDevice", "capability.speechSynthesis", title: "Alexa Device", required: false
            input "phoneDevice", "capability.notification", title: "Phone Notification Device", required: false
        }
        
        section("Slack Integration") {
            input "slackWebhook", "text", title: "Slack Webhook URL", required: false
            input "enableSlackNotifications", "bool", title: "Enable Slack Notifications", 
                defaultValue: false, required: false
        }
        
        section("Communication Routing") {
            input "seeSlackSwitch", "capability.switch", title: "See Slack Switch (check Slack messages)", 
                required: false
        }
    }
}

def miscellaneousPage() {
    dynamicPage(name: "miscellaneousPage", title: "Miscellaneous Automations", install: false, uninstall: false) {
        section("Safe Monitoring") {
            input "safeSensor", "capability.lockCodes", title: "Safe Lock Sensor", required: false
            input "safeCheckInterval", "number", title: "Safe Check Interval (hours)", 
                defaultValue: 24, range: "1..168", required: false,
                description: "Hub variable: safeCheckInterval"
            input "enableSafeChecks", "bool", title: "Enable Safe Lock Checks", 
                defaultValue: true, required: false
        }
        
        section("Mode Changes") {
            input "setAwayDelaySwitch", "capability.switch", title: "Set Away Delay Switch", required: false
            input "awayModeDelay", "number", title: "Away Mode Delay (minutes)", 
                defaultValue: 5, range: "1..60", required: false,
                description: "Hub variable: awayModeDelay"
        }
        
        section("Audio Playback") {
            input "audioDevice", "capability.audioNotification", title: "Audio Playback Device", required: false
            input "enableAudioNotifications", "bool", title: "Enable Audio Notifications", 
                defaultValue: true, required: false
        }
    }
}

def installed() {
    logInfo "Special Automations Manager installed"
    initialize()
}

def updated() {
    logInfo "Special Automations Manager updated"
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    logInfo "Initializing Special Automations Manager"
    
    // Pet Monitoring
    initializePetMonitoring()
    
    // Work Reminders
    initializeWorkReminders()
    
    // Power Monitoring
    initializePowerMonitoring()
    
    // Communication
    initializeCommunication()
    
    // Miscellaneous
    initializeMiscellaneous()
    
    logInfo "Initialization complete"
}

// ========================================
// Pet Monitoring
// ========================================

def initializePetMonitoring() {
    if (dogFloorSensor) {
        subscribe(dogFloorSensor, "contact", dogFloorHandler)
    }
    
    if (dogsOutsideSwitch) {
        subscribe(dogsOutsideSwitch, "switch.on", dogsOutsideHandler)
    }
    
    if (carportZoneActive) {
        subscribe(carportZoneActive, "switch.on", carportZoneActiveHandler)
    }
    
    // Schedule feeding reminder
    if (enableFeedingReminder && dogFeedingReminderTime) {
        schedule(dogFeedingReminderTime, checkDogsFed)
    }
    
    // Schedule daily fed reset
    if (fedResetTime) {
        schedule(fedResetTime, resetDogsFed)
    }
    
    logDebug "Pet monitoring initialized"
}

def dogFloorHandler(evt) {
    if (evt.value == "open") {
        logInfo "Dog detected on floor"
        dogOnFloorSwitch?.on()
    } else {
        logDebug "Dog sensor closed"
        dogOnFloorSwitch?.off()
    }
}

def dogsOutsideHandler(evt) {
    logInfo "Dogs went outside"
    
    if (enableOutsideAlerts) {
        Integer timeout = getConfigValue("dogOutsideTimeout", "dogOutsideTimeout") ?: 30
        runIn(timeout * 60, dogsOutsideTimeout)
        logDebug "Scheduled outside timeout alert for ${timeout} minutes"
    }
}

def dogsOutsideTimeout() {
    if (dogsOutsideSwitch?.currentValue("switch") == "on") {
        logInfo "Dogs have been outside for extended period"
        notify("notification", "Dogs have been outside for a while")
    }
}

def checkDogsFed() {
    if (dogsFedSwitch?.currentValue("switch") != "on") {
        logInfo "Dogs not fed yet - sending reminder"
        notify("alexa", "Reminder: Time to feed the dogs")
        notify("notification", "Reminder: Time to feed the dogs")
    } else {
        logDebug "Dogs already fed"
    }
}

def resetDogsFed() {
    logInfo "Resetting daily dogs fed status"
    dogsFedSwitch?.off()
}

def carportZoneActiveHandler(evt) {
    logInfo "Dog detected in carport zone"
    
    // Sound siren to deter dog
    if (carportSiren) {
        carportSiren.siren()
        runIn(5, stopCarportSiren)
    }
    
    notify("notification", "Dog detected in carport zone")
}

def stopCarportSiren() {
    carportSiren?.off()
}

// ========================================
// Work Reminders
// ========================================

def initializeWorkReminders() {
    if (calendarDevice && enableMeetingReminders) {
        subscribe(calendarDevice, "nextEvent", meetingEventHandler)
    }
    
    if (ptoSwitch) {
        subscribe(ptoSwitch, "switch", ptoHandler)
    }
    
    // Schedule wake-up alarm
    if (enableWakeUpAlarm && wakeUpTime) {
        schedule(wakeUpTime, wakeUpAlarm)
    }
    
    logDebug "Work reminders initialized"
}

def meetingEventHandler(evt) {
    logDebug "Calendar event updated: ${evt.value}"
    
    if (enableMeetingReminders) {
        Integer advance = getConfigValue("meetingReminderAdvance", "meetingReminderAdvance") ?: 15
        // Schedule reminder based on next event
        // Note: Actual calendar integration would parse event time
        logInfo "Meeting reminder scheduled for ${advance} minutes before event"
    }
}

def meetingReminder() {
    logInfo "Meeting starting soon"
    meetingTimeSwitch?.on()
    notify("alexa", "Your meeting is starting in 15 minutes")
    notify("notification", "📅 Meeting starting in 15 minutes")
    
    // Auto-reset meeting time switch
    runIn(60 * 60, resetMeetingTime) // Reset after 1 hour
}

def resetMeetingTime() {
    meetingTimeSwitch?.off()
}

def ptoHandler(evt) {
    if (evt.value == "on") {
        logInfo "PTO mode activated"
        notify("notification", "🏖️ PTO mode is now active")
    } else {
        logInfo "PTO mode deactivated"
        notify("notification", "👔 Back to work mode")
    }
}

def wakeUpAlarm() {
    String today = new Date().format("EEEE")
    
    if (wakeUpDays && today in wakeUpDays) {
        logInfo "Wake-up alarm triggered for ${today}"
        notify("alexa", "Good morning! Time to wake up")
        notify("notification", "Wake-up alarm")
        
        // Play audio if configured
        if (audioDevice && enableAudioNotifications) {
            audioDevice.playTrack("wake_up_sound")
        }
    } else {
        logDebug "Wake-up alarm skipped for ${today}"
    }
}

// ========================================
// Power Monitoring
// ========================================

def initializePowerMonitoring() {
    if (mainsPowerSensor && enableMainsMonitoring) {
        subscribe(mainsPowerSensor, "powerSource", powerSourceHandler)
    }
    
    logDebug "Power monitoring initialized"
}

def powerSourceHandler(evt) {
    String source = evt.value
    logInfo "Power source changed to: ${source}"
    
    if (source == "mains") {
        handleMainsRestored()
    } else if (source == "battery") {
        handleMainsDown()
    }
}

def handleMainsDown() {
    logInfo "ALERT: Mains power is DOWN"
    
    // Update state switch
    onMainsSwitch?.off()
    
    // Delay alert to prevent false alarms
    Integer delay = getConfigValue("powerMonitorDelay", "powerMonitorDelay") ?: 30
    runIn(delay, sendMainsDownAlert)
}

def sendMainsDownAlert() {
    if (onMainsSwitch?.currentValue("switch") == "off") {
        notify("notification", "ALERT: Mains power is DOWN - running on battery")
        notify("alexa", "Alert: Main power is down. Running on battery backup")
        notify("slack", "POWER ALERT: Mains power down - hub on battery")
    }
}

def handleMainsRestored() {
    logInfo "Mains power restored"
    onMainsSwitch?.on()
    notify("notification", "Mains power restored")
    notify("slack", "Power restored - back on mains")
}

// ========================================
// Communication
// ========================================

def initializeCommunication() {
    if (seeSlackSwitch) {
        subscribe(seeSlackSwitch, "switch.on", seeSlackHandler)
    }
    
    logDebug "Communication initialized"
}

def seeSlackHandler(evt) {
    logInfo "See Slack reminder triggered"
    notify("alexa", "You have Slack messages to check")
    notify("notification", "💬 Check Slack messages")
    
    // Auto-reset switch
    runIn(5, resetSeeSlack)
}

def resetSeeSlack() {
    seeSlackSwitch?.off()
}

def notify(String destination, String message) {
    logDebug "Sending notification to ${destination}: ${message}"
    
    switch(destination) {
        case "notification":
        case "push":
            if (notificationDevices) {
                notificationDevices.each { device ->
                    device.deviceNotification(message)
                }
            }
            break
            
        case "alexa":
        case "speech":
            if (alexaDevice) {
                alexaDevice.speak(message)
            }
            break
            
        case "phone":
            if (phoneDevice) {
                phoneDevice.deviceNotification(message)
            }
            break
            
        case "slack":
            if (enableSlackNotifications && slackWebhook) {
                sendSlackMessage(message)
            }
            break
            
        default:
            logDebug "Unknown notification destination: ${destination}"
    }
}

def sendSlackMessage(String message) {
    if (!slackWebhook) {
        logDebug "Slack webhook not configured"
        return
    }
    
    try {
        def params = [
            uri: slackWebhook,
            body: [text: message],
            requestContentType: "application/json"
        ]
        
        httpPost(params) { response ->
            logDebug "Slack message sent: ${response.status}"
        }
    } catch (Exception e) {
        log.error "Failed to send Slack message: ${e.message}"
    }
}

// ========================================
// Miscellaneous
// ========================================

def initializeMiscellaneous() {
    // Schedule safe checks
    if (enableSafeChecks && safeSensor) {
        Integer interval = getConfigValue("safeCheckInterval", "safeCheckInterval") ?: 24
        runEvery1Hour(checkSafeLocked)
    }
    
    // Away delay switch
    if (setAwayDelaySwitch) {
        subscribe(setAwayDelaySwitch, "switch.on", setAwayDelayHandler)
    }
    
    logDebug "Miscellaneous automations initialized"
}

def checkSafeLocked() {
    if (!safeSensor) return
    
    String lockState = safeSensor.currentValue("lock")
    
    if (lockState != "locked") {
        logInfo "WARNING: Safe is not locked!"
        notify("notification", "WARNING: Safe is not locked")
        notify("alexa", "Warning: The safe is not locked")
    } else {
        logDebug "Safe is locked"
    }
}

def setAwayDelayHandler(evt) {
    Integer delay = getConfigValue("awayModeDelay", "awayModeDelay") ?: 5
    logInfo "Away mode change scheduled in ${delay} minutes"
    
    notify("notification", "Switching to Away mode in ${delay} minutes")
    runIn(delay * 60, setAwayMode)
}

def setAwayMode() {
    logInfo "Setting location mode to Away"
    location.setMode("Away")
    setAwayDelaySwitch?.off()
    notify("notification", "Mode changed to Away")
}

// ========================================
// Helper Methods
// ========================================

def getConfigValue(String settingName, String hubVarName) {
    // Try to get value from hub variable first (if hubVarName provided)
    if (hubVarName) {
        def hubVar = getGlobalVar(hubVarName)
        if (hubVar != null) {
            logDebug "Using hub variable ${hubVarName}: ${hubVar}"
            return convertValue(hubVar, settingName)
        }
    }
    
    // Fall back to app setting
    def settingValue = settings[settingName]
    logTrace "Using app setting ${settingName}: ${settingValue}"
    return settingValue
}

def convertValue(value, String settingName) {
    // Convert hub variable value to appropriate type based on setting name
    if (value == null) return null
    
    // Boolean settings
    if (settingName in ["enableFeedingReminder", "enableOutsideAlerts", "enableMeetingReminders", 
                        "enablePtoMode", "enableWakeUpAlarm", "enableMainsMonitoring", 
                        "enableSlackNotifications", "enableSafeChecks", 
                        "enableAudioNotifications"]) {
        if (value instanceof Boolean) return value
        return value.toString().toLowerCase() in ["true", "1", "yes", "on"]
    }
    
    // Number settings
    if (settingName in ["dogOutsideTimeout", "meetingReminderAdvance", "powerMonitorDelay", 
                        "safeCheckInterval", "awayModeDelay"]) {
        if (value instanceof Number) return value
        return value.toString().toInteger()
    }
    
    // Time settings (HH:mm format)
    if (settingName in ["dogFeedingReminderTime", "wakeUpTime"]) {
        return value.toString()
    }
    
    // Default: return as string
    return value.toString()
}

// ========================================
// Logging Methods
// ========================================

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
