/**
 *  Christmas Control
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
    name: "Christmas Control",
    namespace: "hubitat",
    author: "Hubitat User",
    description: "Unified control for Christmas trees and lights.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: ""
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Christmas Control Settings", install: true, uninstall: true) {
        section("<b>═══════════════════════════════════════</b>\n<b>TRIGGERS</b>\n<b>═══════════════════════════════════════</b>") {
            input "masterSwitch", "capability.switch", title: "Master Switch (Optional)", required: false, multiple: false, description: "A switch to manually control all Christmas devices."
            input "enableSchedule", "bool", title: "Enable Schedule?", submitOnChange: true
            if (enableSchedule) {
                input "startMonth", "number", title: "Start Month (e.g. 11)", defaultValue: 11
                input "startDay", "number", title: "Start Day (e.g. 23)", defaultValue: 23
                input "endMonth", "number", title: "End Month (e.g. 1)", defaultValue: 1
                input "endDay", "number", title: "End Day (e.g. 2)", defaultValue: 2
                input "onTimeType", "enum", title: "Turn On At", options: ["Sunset", "Time", "Mode"], defaultValue: "Sunset", submitOnChange: true
                if (onTimeType == "Time") {
                    input "onTimeVal", "time", title: "On Time"
                } else if (onTimeType == "Mode") {
                    input "onMode", "mode", title: "On Mode", multiple: true
                }
                input "offTimeType", "enum", title: "Turn Off At", options: ["Time", "Sunrise", "Sunset", "Mode"], defaultValue: "Time", submitOnChange: true
                if (offTimeType == "Time") {
                    input "offTimeVal", "time", title: "Off Time", defaultValue: "22:00"
                } else if (offTimeType == "Mode") {
                    input "offMode", "mode", title: "Off Mode", multiple: true
                }
            }
        }
        section("<b>═══════════════════════════════════════</b>\n<b>VIRTUAL CONTROL SWITCHES</b>\n<b>═══════════════════════════════════════</b>") {
            input "christmasTreesSwitch", "capability.switch", title: "ChristmasTrees Virtual Switch", required: false, multiple: false, description: "Virtual switch to control all trees"
            input "christmasLightsSwitch", "capability.switch", title: "ChristmasLights Virtual Switch", required: false, multiple: false, description: "Virtual switch to control all outdoor lights"
        }
        section("<b>═══════════════════════════════════════</b>\n<b>INDOOR DEVICES (TREES)</b>\n<b>═══════════════════════════════════════</b>") {
            input "treeSwitches", "capability.switch", title: "Tree Switches", required: false, multiple: true
        }
        section("<b>═══════════════════════════════════════</b>\n<b>OUTDOOR DEVICES (LIGHTS)</b>\n<b>═══════════════════════════════════════</b>") {
            input "mainLights", "capability.switch", title: "Main Christmas Lights", required: false, multiple: true
            input "porchLights", "capability.switch", title: "Porch Lights", required: false, multiple: true
            input "rainSensor", "capability.switch", title: "Rain Sensor", required: false, multiple: false
            input "notificationDevices", "capability.notification", title: "Notification Devices", required: false, multiple: true
        }
        section("<b>═══════════════════════════════════════</b>\n<b>ADVANCED SETTINGS</b>\n<b>═══════════════════════════════════════</b>") {
            paragraph "<i>Fine-tune device control timing to optimize reliability for your mesh network. Default values work for most setups.</i>"
            input "batchDelay", "number", title: "Batch Delay (milliseconds)", description: "Delay between each device command when controlling multiple devices (prevents mesh flooding)", defaultValue: 200, required: false
            input "verificationWait", "number", title: "Verification Wait (milliseconds)", description: "Time to wait after sending commands before verifying device states", defaultValue: 2000, required: false
            input "retryDelay", "number", title: "Retry Delay (milliseconds)", description: "Time to wait before retrying failed devices", defaultValue: 1000, required: false
            input "enableDiagnostics", "bool", title: "Enable Diagnostic Logging?", description: "Detailed logging to troubleshoot device control issues", defaultValue: true, required: false
        }
    }
}

def installed() {
    initialize()
}

def updated() {
    unsubscribe()
    unschedule()
    initialize()
}

def initialize() {
    log.debug "initialize called"
    
    // Subscribe to master switch
    if (masterSwitch) subscribe(masterSwitch, "switch", switchHandler)
    
    // Subscribe to virtual control switches for manual override
    if (christmasTreesSwitch) subscribe(christmasTreesSwitch, "switch", treeSwitchHandler)
    if (christmasLightsSwitch) subscribe(christmasLightsSwitch, "switch", lightsSwitchHandler)
    
    if (enableSchedule) {
        log.debug "Schedule enabled. On Type: $onTimeType"
        if (onTimeType == "Sunset") {
            subscribe(location, "sunset", sunsetHandler)
        } else if (onTimeType == "Mode") {
            subscribe(location, "mode", modeHandler)
        } else if (onTimeVal) {
            schedule(onTimeVal, scheduledTurnOn)
        }
        
        if (offTimeType == "Sunset") {
            subscribe(location, "sunset", scheduledTurnOff)
        } else if (offTimeType == "Sunrise") {
            subscribe(location, "sunrise", scheduledTurnOff)
        } else if (offTimeType == "Mode") {
            if (onTimeType != "Mode") subscribe(location, "mode", modeHandler)
        } else if (offTimeVal) {
            schedule(offTimeVal, scheduledTurnOff)
        }

        // Check if we should be on right now (Catch-up logic)
        checkIfShouldBeOn()
    } else {
        log.debug "Schedule disabled"
    }
}

def checkIfShouldBeOn() {
    log.debug "Checking if lights should be on now..."
    if (!checkDate()) {
        log.debug "Date not within range."
        return
    }

    // Check Mode Logic first
    if (onTimeType == "Mode") {
        if (onMode && location.mode in onMode) {
            log.debug "Current mode matches On Mode. Turning on."
            scheduledTurnOn()
        }
        return
    }
    
    // If Off is Mode, and we are in that mode, ensure off?
    if (offTimeType == "Mode") {
        if (offMode && location.mode in offMode) {
            log.debug "Current mode matches Off Mode. Ensuring off."
            scheduledTurnOff()
            return
        }
    }

    def now = new Date()
    def startTime
    def endTime
    
    if (offTimeType == "Sunset") {
        endTime = getSunriseAndSunset().sunset
    } else if (offTimeType == "Sunrise") {
        endTime = getSunriseAndSunset().sunrise
    } else if (offTimeType == "Mode") {
        // No specific end time.
        endTime = null
    } else {
        endTime = offTimeVal ? timeToday(offTimeVal) : timeToday("22:00")
    }
    
    if (onTimeType == "Sunset") {
        def sunTimes = getSunriseAndSunset()
        startTime = sunTimes.sunset
    } else if (onTimeType == "Mode") {
        startTime = null
    } else {
        startTime = onTimeVal ? timeToday(onTimeVal) : null
    }

    if (startTime && endTime) {
        // If end time is earlier than start time, assume it's the next day
        if (endTime < startTime) {
            endTime = endTime + 1
        }
        
        // Check if now is between start and end
        if (now >= startTime && now < endTime) {
            log.debug "Current time is within schedule window. Turning on."
            scheduledTurnOn()
        } else {
            log.debug "Current time is NOT within schedule window."
        }
    } else if (startTime && offTimeType == "Mode") {
        // Started by time, ends by mode. 
        // We already checked if we are in Off Mode above.
        // If we are here, we are NOT in Off Mode.
        // So if we are past start time, we should probably be on?
        if (now >= startTime) {
             log.debug "Past start time and not in Off Mode. Turning on."
             scheduledTurnOn()
        }
    }
}

def modeHandler(evt) {
    log.debug "Mode changed to ${evt.value}"
    
    if (onTimeType == "Mode" && onMode && evt.value in onMode) {
        log.debug "Mode matches On Mode. Turning on."
        scheduledTurnOn()
    }
    
    if (offTimeType == "Mode" && offMode && evt.value in offMode) {
        log.debug "Mode matches Off Mode. Turning off."
        scheduledTurnOff()
    }
}

def switchHandler(evt) {
    if (evt.value == "on") {
        activateChristmas()
    } else {
        deactivateChristmas()
    }
}

def treeSwitchHandler(evt) {
    // Manual control of trees via virtual switch
    if (atomicState.syncingTreesSwitch) {
        log.debug "Ignoring trees switch event caused by app sync"
        return
    }
    log.debug "ChristmasTrees switch manually turned ${evt.value}"
    if (evt.value == "on") {
        if (treeSwitches) treeSwitches.on()
    } else {
        if (treeSwitches) treeSwitches.off()
    }
}

def lightsSwitchHandler(evt) {
    // Manual control of lights via virtual switch
    if (atomicState.syncingLightsSwitch) {
        log.debug "Ignoring lights switch event caused by app sync"
        return
    }
    log.debug "ChristmasLights switch manually turned ${evt.value}"
    if (evt.value == "on") {
        // Check rain sensor before turning on outdoor lights
        if (rainSensor && rainSensor.currentValue("switch") == "on") {
            log.debug "Rain sensor is on, not turning on outdoor lights"
            if (notificationDevices) notificationDevices.deviceNotification("Cannot turn on Christmas lights - it is raining")
            // Turn the switch back off
            atomicState.syncingLightsSwitch = true
            christmasLightsSwitch.off()
            runIn(2, clearLightsSwitchSync)
            return
        }
        if (mainLights) mainLights.on()
        if (porchLights) {
            runIn(300, turnOffPorch)
        }
    } else {
        if (mainLights) mainLights.off()
        if (porchLights) porchLights.on()
    }
}

def clearTreesSwitchSync() {
    atomicState.syncingTreesSwitch = false
}

def clearLightsSwitchSync() {
    atomicState.syncingLightsSwitch = false
}

def sunsetHandler(evt) {
    log.debug "Sunset event received"
    scheduledTurnOn()
}

def scheduledTurnOn() {
    log.debug "scheduledTurnOn called"
    if (checkDate()) {
        log.debug "Date is within range, turning on"
        activateChristmas()
    } else {
        log.debug "Date is outside range, not turning on"
    }
}

def scheduledTurnOff() {
    deactivateChristmas()
}

def activateChristmas() {
    def diagnostics = settings.enableDiagnostics != null ? settings.enableDiagnostics : true
    
    if (diagnostics) {
        log.debug "========== activateChristmas called at ${new Date()} =========="
        // Log initial states
        log.debug "Initial device states:"
        logDeviceStates(treeSwitches, "Trees")
        logDeviceStates(mainLights, "MainLights")
        logDeviceStates(porchLights, "PorchLights")
    }
    
    // Turn on trees with verification and retry
    if (treeSwitches) {
        turnOnDevicesWithRetry(treeSwitches, "Trees")
    }
    
    // Sync the virtual switch
    if (christmasTreesSwitch && christmasTreesSwitch.currentValue("switch") != "on") {
        atomicState.syncingTreesSwitch = true
        christmasTreesSwitch.on()
        runIn(2, clearTreesSwitchSync)
    }
    
    // Check Rain for Outdoor Lights
    if (rainSensor && rainSensor.currentValue("switch") == "on") {
        log.debug "Rain sensor is on, skipping outdoor lights"
        if (notificationDevices) notificationDevices.deviceNotification("Christmas lights not coming on because it is raining")
        // Ensure outdoor lights and virtual switch are off if it's raining
        if (mainLights) {
            turnOffDevicesWithRetry(mainLights, "MainLights")
        }
        if (christmasLightsSwitch && christmasLightsSwitch.currentValue("switch") != "off") {
            atomicState.syncingLightsSwitch = true
            christmasLightsSwitch.off()
            runIn(2, clearLightsSwitchSync)
        }
        return
    }

    // Turn on Outdoor Lights (if not raining) with verification and retry
    if (mainLights) {
        turnOnDevicesWithRetry(mainLights, "MainLights")
    }
    
    // Sync the virtual switch
    if (christmasLightsSwitch && christmasLightsSwitch.currentValue("switch") != "on") {
        atomicState.syncingLightsSwitch = true
        christmasLightsSwitch.on()
        runIn(2, clearLightsSwitchSync)
    }
    
    // Handle Porch Lights
    runIn(300, turnOffPorch)
    
    if (diagnostics) log.debug "========== activateChristmas completed =========="
}

def deactivateChristmas() {
    def diagnostics = settings.enableDiagnostics != null ? settings.enableDiagnostics : true
    
    if (diagnostics) {
        log.debug "========== deactivateChristmas called at ${new Date()} =========="
        // Log initial states
        log.debug "Initial device states:"
        logDeviceStates(treeSwitches, "Trees")
        logDeviceStates(mainLights, "MainLights")
        logDeviceStates(porchLights, "PorchLights")
    }
    
    // Turn off trees with verification and retry
    if (treeSwitches) {
        turnOffDevicesWithRetry(treeSwitches, "Trees")
    }
    
    // Sync the virtual switch
    if (christmasTreesSwitch && christmasTreesSwitch.currentValue("switch") != "off") {
        atomicState.syncingTreesSwitch = true
        christmasTreesSwitch.off()
        runIn(2, clearTreesSwitchSync)
    }
    
    // Turn off Outdoor Lights with verification and retry
    if (mainLights) {
        turnOffDevicesWithRetry(mainLights, "MainLights")
    }
    
    // Sync the virtual switch
    if (christmasLightsSwitch && christmasLightsSwitch.currentValue("switch") != "off") {
        atomicState.syncingLightsSwitch = true
        christmasLightsSwitch.off()
        runIn(2, clearLightsSwitchSync)
    }

    // Restore Porch Lights with verification
    if (porchLights) {
        if (diagnostics) log.debug "Restoring porch lights"
        turnOnDevicesWithRetry(porchLights, "PorchLights")
    }
    
    if (diagnostics) log.debug "========== deactivateChristmas completed =========="
}

def turnOffPorch() {
    if (porchLights) porchLights.off()
}

// Helper method to log device states for diagnostics
def logDeviceStates(devices, deviceType) {
    if (!devices) return
    def deviceList = devices instanceof List ? devices : [devices]
    deviceList.each { device ->
        def state = device.currentValue("switch")
        log.debug "${deviceType} - ${device.displayName}: ${state}"
    }
}

// Helper method to turn off devices with verification and retry
def turnOffDevicesWithRetry(devices, deviceType, retryCount = 0) {
    if (!devices) return
    
    def batchDelayMs = settings.batchDelay != null ? settings.batchDelay : 200
    def verificationWaitMs = settings.verificationWait != null ? settings.verificationWait : 2000
    def retryDelayMs = settings.retryDelay != null ? settings.retryDelay : 1000
    def diagnostics = settings.enableDiagnostics != null ? settings.enableDiagnostics : true
    
    if (diagnostics) log.debug "turnOffDevicesWithRetry called for ${deviceType}, attempt ${retryCount + 1}"
    
    def deviceList = devices instanceof List ? devices : [devices]
    def failedDevices = []
    
    // Send off commands in batches to prevent mesh flooding
    deviceList.eachWithIndex { device, index ->
        if (diagnostics) log.debug "Turning off ${deviceType}: ${device.displayName}"
        device.off()
        // Small delay between each device to prevent mesh congestion
        if (index < deviceList.size() - 1 && deviceList.size() > 3) {
            pauseExecution(batchDelayMs)
        }
    }
    
    // Wait for commands to process
    pauseExecution(verificationWaitMs)
    
    // Verify all devices turned off
    deviceList.each { device ->
        def currentState = device.currentValue("switch")
        if (currentState != "off") {
            log.warn "${deviceType} ${device.displayName} failed to turn off (state: ${currentState})"
            failedDevices.add(device)
        } else {
            if (diagnostics) log.debug "${deviceType} ${device.displayName} confirmed OFF"
        }
    }
    
    // Retry failed devices once
    if (failedDevices.size() > 0 && retryCount < 1) {
        log.warn "Retrying ${failedDevices.size()} failed ${deviceType} devices"
        pauseExecution(retryDelayMs)
        turnOffDevicesWithRetry(failedDevices, deviceType, retryCount + 1)
    } else if (failedDevices.size() > 0) {
        log.error "${deviceType} devices still on after retry: ${failedDevices*.displayName.join(', ')}"
    }
}

// Helper method to turn on devices with verification and retry
def turnOnDevicesWithRetry(devices, deviceType, retryCount = 0) {
    if (!devices) return
    
    def batchDelayMs = settings.batchDelay != null ? settings.batchDelay : 200
    def verificationWaitMs = settings.verificationWait != null ? settings.verificationWait : 2000
    def retryDelayMs = settings.retryDelay != null ? settings.retryDelay : 1000
    def diagnostics = settings.enableDiagnostics != null ? settings.enableDiagnostics : true
    
    if (diagnostics) log.debug "turnOnDevicesWithRetry called for ${deviceType}, attempt ${retryCount + 1}"
    
    def deviceList = devices instanceof List ? devices : [devices]
    def failedDevices = []
    
    // Send on commands in batches to prevent mesh flooding
    deviceList.eachWithIndex { device, index ->
        if (diagnostics) log.debug "Turning on ${deviceType}: ${device.displayName}"
        device.on()
        // Small delay between each device to prevent mesh congestion
        if (index < deviceList.size() - 1 && deviceList.size() > 3) {
            pauseExecution(batchDelayMs)
        }
    }
    
    // Wait for commands to process
    pauseExecution(verificationWaitMs)
    
    // Verify all devices turned on
    deviceList.each { device ->
        def currentState = device.currentValue("switch")
        if (currentState != "on") {
            log.warn "${deviceType} ${device.displayName} failed to turn on (state: ${currentState})"
            failedDevices.add(device)
        } else {
            if (diagnostics) log.debug "${deviceType} ${device.displayName} confirmed ON"
        }
    }
    
    // Retry failed devices once
    if (failedDevices.size() > 0 && retryCount < 1) {
        log.warn "Retrying ${failedDevices.size()} failed ${deviceType} devices"
        pauseExecution(retryDelayMs)
        turnOnDevicesWithRetry(failedDevices, deviceType, retryCount + 1)
    } else if (failedDevices.size() > 0) {
        log.error "${deviceType} devices still off after retry: ${failedDevices*.displayName.join(', ')}"
    }
}

def checkDate() {
    def now = new Date()
    def tz = location.timeZone ?: TimeZone.getDefault()
    def currentMonth = now.format("M", tz).toInteger()
    def currentDay = now.format("d", tz).toInteger()
    def curr = currentMonth * 100 + currentDay
    
    // Use defaults if settings are null
    def sMonth = startMonth != null ? startMonth : 11
    def sDay = startDay != null ? startDay : 23
    def eMonth = endMonth != null ? endMonth : 1
    def eDay = endDay != null ? endDay : 2
    
    def s = sMonth * 100 + sDay
    def e = eMonth * 100 + eDay
    
    log.debug "checkDate: Current: $curr (Month: $currentMonth, Day: $currentDay), Start: $s, End: $e"
    
    if (s > e) { // Wraps year
        return (curr >= s || curr <= e)
    } else {
        return (curr >= s && curr <= e)
    }
}
