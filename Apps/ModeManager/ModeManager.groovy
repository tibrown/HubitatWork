/**
 *  Mode Manager Custom
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
    name: "Mode Manager Custom",
    namespace: "timbrown",
    author: "Tim Brown",
    description: "Automated mode management with time and solar-based scheduling, day-of-week support, and switch-controlled auto-mode toggling.",
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
    dynamicPage(name: "mainPage", title: "Mode Manager", install: true, uninstall: true) {
        
        // Display validation warnings if any
        def warnings = validateSchedules()
        if (warnings) {
            section("<b>⚠️ CONFIGURATION WARNINGS</b>") {
                warnings.each { warning ->
                    paragraph "<span style='color:red'>${warning}</span>"
                }
            }
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>AUTO-CONTROL SWITCH</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "Select a switch to control automatic Night mode changes. When OFF, Night mode changes automatically. When ON, Night mode must be set manually. (Morning, Day, and Evening modes always change automatically unless in Away mode. Away mode prevents ALL automatic mode changes.)"
            input "autoControlSwitch", "capability.switch", 
                  title: "Auto-Control Switch", 
                  required: true
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>STATUS</b>\n<b>═══════════════════════════════════════</b>") {
            def autoControlState = autoControlSwitch ? autoControlSwitch.currentValue("switch") : "not configured"
            def currentMode = location.currentMode
            paragraph "Auto-Control: <b>${autoControlState?.toUpperCase()}</b>"
            paragraph "Current Mode: <b>${currentMode}</b>"
            
            // Display next transition times if auto-control is enabled
            if (autoControlState == "off") {
                paragraph "<b>Next Scheduled Transitions:</b>"
                
                if (settings.morningEnabled) {
                    def nextTime = getNextTransitionTime("morning")
                    paragraph "• Morning → ${settings.morningMode}: ${nextTime ?: 'Not scheduled'}"
                }
                if (settings.dayEnabled) {
                    def nextTime = getNextTransitionTime("day")
                    paragraph "• Day → ${settings.dayMode}: ${nextTime ?: 'Not scheduled'}"
                }
                if (settings.eveningEnabled) {
                    def nextTime = getNextTransitionTime("evening")
                    paragraph "• Evening → ${settings.eveningMode}: ${nextTime ?: 'Not scheduled'}"
                }
                if (settings.nightEnabled) {
                    def nextTime = getNextTransitionTime("night")
                    paragraph "• Night → ${settings.nightMode}: ${nextTime ?: 'Not scheduled'}"
                }
            }
        }
        
        // Morning Transition
        section("<b>═══════════════════════════════════════</b>\n<b>MORNING TRANSITION</b>\n<b>═══════════════════════════════════════</b>") {
            input "morningEnabled", "bool", 
                  title: "Enable Morning Transition", 
                  defaultValue: false, 
                  submitOnChange: true
            
            if (settings.morningEnabled) {
                input "morningMode", "enum", 
                      title: "Morning Mode", 
                      options: location.modes.collect{it.name}, 
                      required: true
                
                input "morningTriggerType", "enum", 
                      title: "Trigger Type", 
                      options: ["Specific Time", "Sunrise", "Sunset"], 
                      defaultValue: "Sunrise", 
                      submitOnChange: true, 
                      required: true
                
                if (settings.morningTriggerType == "Specific Time") {
                    input "morningTime", "time", 
                          title: "Morning Time", 
                          required: true
                } else {
                    input "morningOffset", "number", 
                          title: "Offset (minutes)", 
                          description: "Negative for before, positive for after", 
                          defaultValue: 0, 
                          range: "-120..120", 
                          required: false
                }
                
                input "morningDays", "enum", 
                      title: "Active Days", 
                      options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], 
                      multiple: true, 
                      required: false, 
                      defaultValue: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            }
        }
        
        // Day Transition
        section("<b>═══════════════════════════════════════</b>\n<b>DAY TRANSITION</b>\n<b>═══════════════════════════════════════</b>") {
            input "dayEnabled", "bool", 
                  title: "Enable Day Transition", 
                  defaultValue: false, 
                  submitOnChange: true
            
            if (settings.dayEnabled) {
                input "dayMode", "enum", 
                      title: "Day Mode", 
                      options: location.modes.collect{it.name}, 
                      required: true
                
                input "dayTriggerType", "enum", 
                      title: "Trigger Type", 
                      options: ["Specific Time", "Sunrise", "Sunset"], 
                      defaultValue: "Specific Time", 
                      submitOnChange: true, 
                      required: true
                
                if (settings.dayTriggerType == "Specific Time") {
                    input "dayTime", "time", 
                          title: "Day Time", 
                          required: true
                } else {
                    input "dayOffset", "number", 
                          title: "Offset (minutes)", 
                          description: "Negative for before, positive for after", 
                          defaultValue: 0, 
                          range: "-120..120", 
                          required: false
                }
                
                input "dayDays", "enum", 
                      title: "Active Days", 
                      options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], 
                      multiple: true, 
                      required: false, 
                      defaultValue: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            }
        }
        
        // Evening Transition
        section("<b>═══════════════════════════════════════</b>\n<b>EVENING TRANSITION</b>\n<b>═══════════════════════════════════════</b>") {
            input "eveningEnabled", "bool", 
                  title: "Enable Evening Transition", 
                  defaultValue: false, 
                  submitOnChange: true
            
            if (settings.eveningEnabled) {
                input "eveningMode", "enum", 
                      title: "Evening Mode", 
                      options: location.modes.collect{it.name}, 
                      required: true
                
                input "eveningTriggerType", "enum", 
                      title: "Trigger Type", 
                      options: ["Specific Time", "Sunrise", "Sunset"], 
                      defaultValue: "Sunset", 
                      submitOnChange: true, 
                      required: true
                
                if (settings.eveningTriggerType == "Specific Time") {
                    input "eveningTime", "time", 
                          title: "Evening Time", 
                          required: true
                } else {
                    input "eveningOffset", "number", 
                          title: "Offset (minutes)", 
                          description: "Negative for before, positive for after", 
                          defaultValue: 0, 
                          range: "-120..120", 
                          required: false
                }
                
                input "eveningDays", "enum", 
                      title: "Active Days", 
                      options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], 
                      multiple: true, 
                      required: false, 
                      defaultValue: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            }
        }
        
        // Night Transition
        section("<b>═══════════════════════════════════════</b>\n<b>NIGHT TRANSITION</b>\n<b>═══════════════════════════════════════</b>") {
            input "nightEnabled", "bool", 
                  title: "Enable Night Transition", 
                  defaultValue: false, 
                  submitOnChange: true
            
            if (settings.nightEnabled) {
                input "nightMode", "enum", 
                      title: "Night Mode", 
                      options: location.modes.collect{it.name}, 
                      required: true
                
                input "nightTriggerType", "enum", 
                      title: "Trigger Type", 
                      options: ["Specific Time", "Sunrise", "Sunset"], 
                      defaultValue: "Specific Time", 
                      submitOnChange: true, 
                      required: true
                
                if (settings.nightTriggerType == "Specific Time") {
                    input "nightTime", "time", 
                          title: "Night Time", 
                          required: true
                } else {
                    input "nightOffset", "number", 
                          title: "Offset (minutes)", 
                          description: "Negative for before, positive for after", 
                          defaultValue: 0, 
                          range: "-120..120", 
                          required: false
                }
                
                input "nightDays", "enum", 
                      title: "Active Days", 
                      options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"], 
                      multiple: true, 
                      required: false, 
                      defaultValue: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
            }
        }
        
        section("<b>═══════════════════════════════════════</b>\n<b>LOGGING</b>\n<b>═══════════════════════════════════════</b>") {
            input "logLevel", "enum", title: "Log Level", options: ["None","Info","Debug","Trace"], defaultValue: "Info"
        }
    }
}

// ========================================
// LIFECYCLE METHODS
// ========================================

def installed() {
    logInfo "Mode Manager installed"
    initialize()
    updateLabel()
}

def updated() {
    logInfo "Mode Manager updated"
    unsubscribe()
    unschedule()
    
    // Validate schedules before initializing
    def warnings = validateSchedules()
    if (warnings) {
        warnings.each { warning ->
            logError(warning)
        }
        logError "Configuration has errors - schedules not initialized. Please fix the warnings and save again."
        return
    }
    
    initialize()
    updateLabel()
}

def uninstalled() {
    logInfo "Mode Manager uninstalled"
    unsubscribe()
    unschedule()
}

def initialize() {
    logInfo "Initializing Mode Manager"
    
    unsubscribe()
    unschedule()
    
    // Subscribe to mode changes to update the label
    subscribe(location, "mode", modeChangeHandler)
    
    // Subscribe to auto-control switch changes
    if (autoControlSwitch) {
        subscribe(autoControlSwitch, "switch", autoControlSwitchHandler)
        logInfo "Subscribed to auto-control switch: ${autoControlSwitch.displayName}"
    }
    
    // Set up schedules for each enabled transition
    // Morning, Day, and Evening always run automatically
    ["morning", "day", "evening"].each { setupScheduleForMode(it) }
    
    // Night schedule only runs if auto-control is disabled (switch is OFF)
    def autoControlState = autoControlSwitch?.currentValue("switch")
    if (autoControlState == "on") {
        logInfo "Auto-control is ON - Night mode must be set manually (other modes still automatic)"
    } else {
        setupScheduleForMode("night")
    }
    
    logInfo "Mode Manager initialization complete"
}

// ========================================
// SCHEDULE SETUP METHODS
// ========================================

def setupScheduleForMode(String mode) {
    String capMode = mode.capitalize()
    if (!settings."${mode}Enabled") {
        logDebug "${capMode} transition disabled"
        return
    }
    
    def triggerType = settings."${mode}TriggerType"
    String handlerName = "${mode}TransitionHandler"
    
    if (triggerType == "Specific Time") {
        if (settings."${mode}Time") {
            schedule(settings."${mode}Time", handlerName)
            logInfo "Scheduled ${capMode} transition at ${settings."${mode}Time"}"
        }
    } else if (triggerType in ["Sunrise", "Sunset"]) {
        String evtType = triggerType.toLowerCase()
        subscribe(location, evtType, handlerName)
        subscribe(location, "${evtType}Time", "reschedule${capMode}Solar")
        scheduleSolarForMode(mode)
        logInfo "Scheduled ${capMode} transition at ${evtType} with offset ${settings."${mode}Offset" ?: 0} minutes"
    }
}

def setupMorningSchedule() { setupScheduleForMode("morning") }
def setupDaySchedule()     { setupScheduleForMode("day") }
def setupEveningSchedule() { setupScheduleForMode("evening") }
def setupNightSchedule()   { setupScheduleForMode("night") }

// ========================================
// SOLAR SCHEDULING METHODS
// ========================================

def scheduleSolarForMode(String mode) {
    def offset = settings."${mode}Offset" ?: 0
    if (offset == 0) return // Let event subscription handle it
    
    def triggerType = settings."${mode}TriggerType"
    def solarTime = (triggerType == "Sunrise") ? location.sunrise : location.sunset
    if (solarTime) {
        def scheduledTime = new Date(solarTime.time + (offset * 60 * 1000))
        runOnce(scheduledTime, "${mode}TransitionHandler")
        logDebug "Scheduled ${mode.capitalize()} transition for ${scheduledTime.format('HH:mm', location.timeZone)}"
    }
}

def rescheduleMorningSolar(evt) { scheduleSolarForMode("morning") }
def rescheduleDaySolar(evt)     { scheduleSolarForMode("day") }
def rescheduleEveningSolar(evt) { scheduleSolarForMode("evening") }
def rescheduleNightSolar(evt)   { scheduleSolarForMode("night") }

// ========================================
// TRANSITION HANDLERS
// ========================================

def morningTransitionHandler(evt = null) {
    // Don't change from Away mode automatically
    if (location.currentMode == "Away") {
        logInfo "Skipping Morning transition - currently in Away mode"
        return
    }
    
    // Check day of week
    def today = new Date().format("EEEE")
    if (settings.morningDays && !settings.morningDays.contains(today)) {
        logDebug "Skipping Morning transition - not configured for ${today}"
        return
    }
    
    // Change mode
    def targetMode = settings.morningMode
    if (targetMode) {
        logInfo "Changing mode to ${targetMode} (Morning transition)"
        location.setMode(targetMode)
    }
}

def dayTransitionHandler(evt = null) {
    // Don't change from Away mode automatically
    if (location.currentMode == "Away") {
        logInfo "Skipping Day transition - currently in Away mode"
        return
    }
    
    // Check day of week
    def today = new Date().format("EEEE")
    if (settings.dayDays && !settings.dayDays.contains(today)) {
        logDebug "Skipping Day transition - not configured for ${today}"
        return
    }
    
    // Change mode
    def targetMode = settings.dayMode
    if (targetMode) {
        logInfo "Changing mode to ${targetMode} (Day transition)"
        location.setMode(targetMode)
    }
}

def eveningTransitionHandler(evt = null) {
    // Don't change from Away mode automatically
    if (location.currentMode == "Away") {
        logInfo "Skipping Evening transition - currently in Away mode"
        return
    }
    
    // Check day of week
    def today = new Date().format("EEEE")
    if (settings.eveningDays && !settings.eveningDays.contains(today)) {
        logDebug "Skipping Evening transition - not configured for ${today}"
        return
    }
    
    // Change mode
    def targetMode = settings.eveningMode
    if (targetMode) {
        logInfo "Changing mode to ${targetMode} (Evening transition)"
        location.setMode(targetMode)
    }
}

def nightTransitionHandler(evt = null) {
    // Don't change from Away mode automatically
    if (location.currentMode == "Away") {
        logInfo "Skipping Night transition - currently in Away mode"
        return
    }
    
    // Check day of week
    def today = new Date().format("EEEE")
    if (settings.nightDays && !settings.nightDays.contains(today)) {
        logDebug "Skipping Night transition - not configured for ${today}"
        return
    }
    
    // Check if auto-control is enabled (only affects Night mode)
    def autoControlState = autoControlSwitch?.currentValue("switch")
    if (autoControlState == "on") {
        logInfo "Skipping Night transition - auto-control is ON (must set Night mode manually)"
        return
    }
    
    // Change mode
    def targetMode = settings.nightMode
    if (targetMode) {
        logInfo "Changing mode to ${targetMode} (Night transition)"
        location.setMode(targetMode)
    }
}

// ========================================
// AUTO-CONTROL SWITCH HANDLER
// ========================================

def autoControlSwitchHandler(evt) {
    def switchState = evt.value
    logInfo "Auto-control switch changed to ${switchState?.toUpperCase()}"
    
    if (switchState == "off") {
        logInfo "Auto-control OFF - Night mode will change automatically"
        setupScheduleForMode("night")
    } else {
        logInfo "Auto-control ON - Night mode must be set manually (other modes still automatic)"
        // Clear only Night-related schedules
        unschedule("nightTransitionHandler")
    }
}

// ========================================
// VALIDATION METHODS
// ========================================

def validateSchedules() {
    def warnings = []
    def schedules = []
    
    // Collect all enabled schedules with their times
    if (settings.morningEnabled) {
        def time = getTransitionTime("morning")
        if (time) {
            schedules << [name: "Morning", time: time, days: settings.morningDays]
        }
    }
    
    if (settings.dayEnabled) {
        def time = getTransitionTime("day")
        if (time) {
            schedules << [name: "Day", time: time, days: settings.dayDays]
        }
    }
    
    if (settings.eveningEnabled) {
        def time = getTransitionTime("evening")
        if (time) {
            schedules << [name: "Evening", time: time, days: settings.eveningDays]
        }
    }
    
    if (settings.nightEnabled) {
        def time = getTransitionTime("night")
        if (time) {
            schedules << [name: "Night", time: time, days: settings.nightDays]
        }
    }
    
    // Check for overlapping times on same days
    for (int i = 0; i < schedules.size(); i++) {
        for (int j = i + 1; j < schedules.size(); j++) {
            def sched1 = schedules[i]
            def sched2 = schedules[j]
            
            // Check if schedules share any common days
            def commonDays = findCommonDays(sched1.days, sched2.days)
            if (commonDays) {
                // Check if times are too close (within 5 minutes)
                def timeDiff = Math.abs(sched1.time.time - sched2.time.time)
                if (timeDiff < 300000) { // 5 minutes in milliseconds
                    warnings << "${sched1.name} and ${sched2.name} transitions are scheduled within 5 minutes of each other on ${commonDays.join(', ')}"
                }
            }
        }
    }
    
    return warnings
}

def findCommonDays(days1, days2) {
    if (!days1 || !days2) return []
    def common = days1.findAll { day -> days2.contains(day) }
    return common
}

def getTransitionTime(transitionName) {
    def triggerType = settings."${transitionName}TriggerType"
    
    if (triggerType == "Specific Time") {
        def timeStr = settings."${transitionName}Time"
        if (timeStr) {
            return timeToday(timeStr, location.timeZone)
        }
    } else if (triggerType == "Sunrise") {
        def solarTime = location.sunrise
        def offset = settings."${transitionName}Offset" ?: 0
        if (solarTime) {
            return new Date(solarTime.time + (offset * 60 * 1000))
        }
    } else if (triggerType == "Sunset") {
        def solarTime = location.sunset
        def offset = settings."${transitionName}Offset" ?: 0
        if (solarTime) {
            return new Date(solarTime.time + (offset * 60 * 1000))
        }
    }
    
    return null
}

// ========================================
// HELPER METHODS
// ========================================

def getNextTransitionTime(transitionName) {
    def triggerType = settings."${transitionName}TriggerType"
    def days = settings."${transitionName}Days"
    
    if (triggerType == "Specific Time") {
        def timeStr = settings."${transitionName}Time"
        if (!timeStr) return null
        
        def scheduledTime = timeToday(timeStr, location.timeZone)
        def now = new Date()
        
        // If time has passed today, check if tomorrow is a valid day
        if (scheduledTime < now) {
            scheduledTime = new Date(scheduledTime.time + 86400000) // Add 24 hours
        }
        
        // Check if today or tomorrow is in the allowed days
        def dayName = scheduledTime.format("EEEE")
        if (days && !days.contains(dayName)) {
            return "Next on a configured day"
        }
        
        return scheduledTime.format("MMM dd HH:mm", location.timeZone)
        
    } else if (triggerType in ["Sunrise", "Sunset"]) {
        def solarTime = (triggerType == "Sunrise") ? location.sunrise : location.sunset
        def offset = settings."${transitionName}Offset" ?: 0
        
        if (!solarTime) return null
        
        def scheduledTime = new Date(solarTime.time + (offset * 60 * 1000))
        def now = new Date()
        
        if (scheduledTime < now) {
            return "Tomorrow ${scheduledTime.format('HH:mm', location.timeZone)}"
        }
        
        def dayName = scheduledTime.format("EEEE")
        if (days && !days.contains(dayName)) {
            return "Next on a configured day"
        }
        
        return "Today ${scheduledTime.format('HH:mm', location.timeZone)}"
    }
    
    return null
}

// ========================================
// MODE CHANGE HANDLER
// ========================================

def modeChangeHandler(evt) {
    logInfo "Mode changed to: ${evt.value}"
    updateLabel()
}

// ========================================
// LABEL UPDATE METHOD
// ========================================

def updateLabel() {
    def currentMode = location.currentMode
    def newLabel = "Mode Manager Custom - <span style='color:green'>${currentMode}</span>"
    app.updateLabel(newLabel)
    logDebug "Updated app label to show current mode: ${currentMode}"
}

// ========================================
// LOGGING METHODS
// ========================================

def logDebug(msg) {
    if (logLevel in ["Debug","Trace"]) log.debug "${app.label}: ${msg}"
}

def logInfo(msg) {
    if (logLevel in ["Info","Debug","Trace"]) log.info "${app.label}: ${msg}"
}

def logError(msg) {
    log.error "${app.label}: ${msg}"
}
void logTrace(String msg) { if (logLevel == "Trace") log.trace "${app.label}: ${msg}" }
