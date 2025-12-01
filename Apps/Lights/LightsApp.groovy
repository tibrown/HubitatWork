/**
 *  Lights App
 *
 *  Copyright 2025 Hubitat
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
    name: "LightsApp",
    namespace: "hubitat",
    author: "Gemini",
    description: "Controls Lights based on Modes and Sensors, combining logic from multiple rules.",
    category: "Lights",
    iconUrl: "",
    iconX2Url: ""
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Lights App", install: true, uninstall: true) {
        section("Desk Control") {
            input "deskMotion", "capability.motionSensor", title: "Desk Motion", required: false
            input "deskButton", "capability.pushableButton", title: "Desk Button", required: false
            input "deskCT", "capability.colorTemperature", title: "Desk Light (CT)", required: false
        }
        section("Strips") {
            input "lightStrip", "capability.colorControl", title: "Light Strip", required: false
            input "lanStrip", "capability.colorControl", title: "Lan Strip", required: false
        }
        section("Switches & Outlets") {
            input "switches", "capability.switch", title: "Switches & Outlets", multiple: true, required: false
        }
        section("Conditions") {
            input "onPTO", "capability.switch", title: "On PTO Switch", required: false
            input "holiday", "capability.switch", title: "Holiday Switch", required: false
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
    subscribe(location, "mode", modeHandler)
    if (deskMotion) subscribe(deskMotion, "motion.active", deskHandler)
    if (deskButton) subscribe(deskButton, "pushed", deskHandler)
    if (deskButton) subscribe(deskButton, "doubleTapped", deskHandler)
}

def deskHandler(evt) {
    log.info "Desk Handler Event: ${evt.name} ${evt.value}"
    
    // Check for Button 1 if it's a button event
    if (evt.name == "pushed" || evt.name == "doubleTapped") {
        if (evt.value != "1") return // Only react to Button 1
        
        if (evt.name == "pushed") {
            setDeskLight(100)
        } else {
            setDeskLight(5)
        }
    } else {
        // Motion event
        setDeskLight(5)
    }
}

def setDeskLight(level = 5) {
    // Method: Soft White, Level 5 (default)
    if (deskCT) {
        deskCT.setLevel(level)
        if (deskCT.hasCommand("setColorTemperature")) {
            deskCT.setColorTemperature(2700)
        }
    }
}

def modeHandler(evt) {
    def mode = evt.value
    log.info "Mode changed to ${mode}"

    // Lightstrip Method Logic
    // Night: Blue, 30
    // Evening: Soft White, 50
    // Morning: Soft White, 50
    // Day: Off
    
    if (mode == "Night") {
        // TurnLightsNight Logic
        if (switches) switches.off()
        
        // Lightstrip method for Night is Blue 30.
        setStrip(lightStrip, "Blue", 30)
        // LanStrip should be off at night
        if (lanStrip) lanStrip.off()

        // Special Note: DeskLightDimmest method at Night
        setDeskLight()

    } else if (mode == "Evening") {
        // TurnLightsOnEvening Logic
        if (switches) switches.on()

        // LanStrip ON -> Use Lightstrip method
        setStrip(lightStrip, "Soft White", 50)
        setStrip(lanStrip, "Soft White", 50)

    } else if (mode == "Morning") {
        // TurnLightsOnMorning Logic
        // Conditions: Holiday off, OnPTO off
        def holidayOn = holiday && holiday.currentValue("switch") == "on"
        def ptoOn = onPTO && onPTO.currentValue("switch") == "on"

        if (!holidayOn && !ptoOn) {
            if (switches) switches.on()
            
            // LanStrip ON -> Use Lightstrip method
            setStrip(lightStrip, "Soft White", 50)
            setStrip(lanStrip, "Soft White", 50)
        } else {
            // If conditions not met, LightStrip still updates per its own rule logic (Mode Change)
            setStrip(lightStrip, "Soft White", 50)
            // LanStrip is not turned on, so we don't touch it? 
            // Or should we ensure it's off? The rule doesn't say.
        }

    } else if (mode == "Day") {
        // Lightstrip Rule: Day -> Off
        if (switches) switches.off()
        if (lightStrip) lightStrip.off()
        if (lanStrip) lanStrip.off()
    }
}

def setStrip(device, colorName, level) {
    if (!device) return

    if (level == 0) {
        device.off()
        return
    }

    // Ensure device is on
    if (device.currentValue("switch") != "on") {
        device.on()
    }
    
    device.setLevel(level)

    if (colorName == "Blue") {
        // Blue: Hue ~66, Sat 100
        def colorMap = [hue: 66, saturation: 100, level: level]
        device.setColor(colorMap)
    } else if (colorName == "Soft White") {
        // Soft White: CT 2700
        if (device.hasCommand("setColorTemperature")) {
            device.setColorTemperature(2700)
        } else {
            // Fallback for RGBW if no CT command
            // Hue 23, Sat 56 is often used for Warm White/Soft White
            def colorMap = [hue: 23, saturation: 56, level: level]
            device.setColor(colorMap)
        }
    }
}
