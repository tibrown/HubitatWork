/**
 *  Arrive Grace Turns On
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
    name: "Arrive Grace Turns On",
    namespace: "hubitat",
    author: "GitHub Copilot",
    description: "Manages alarm grace period upon arrival.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: ""
)

preferences {
    section("Trigger Device") {
        input "arriveGracePeriod", "capability.switch", title: "Arrive Grace Period Switch", required: true
    }
    section("Devices to Control") {
        input "alarmsEnabled", "capability.switch", title: "Alarms Enabled Switch", required: true
        input "silentMode", "capability.switch", title: "Silent Mode Switch", required: true
    }
    section("Settings") {
        input "graceDuration", "number", title: "Grace Duration (minutes)", defaultValue: 30, required: true
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
    subscribe(arriveGracePeriod, "switch.on", handler)
}

def handler(evt) {
    log.info "ArriveGracePeriod turned on. Starting grace period of ${graceDuration} minutes."
    
    // 1. Off: AlarmsEnabled
    alarmsEnabled.off()
    
    // 2. On: Silent
    silentMode.on()
    
    // 3. Delay ArriveGraceDuration
    // Convert minutes to seconds for runIn
    def delaySeconds = graceDuration * 60
    runIn(delaySeconds, endGracePeriod)
}

def endGracePeriod() {
    log.info "Grace period ended. Restoring settings."
    
    // 4. On: AlarmsEnabled
    alarmsEnabled.on()
    
    // 5. Off: Silent
    silentMode.off()
    
    // 6. Off: ArriveGracePeriod
    arriveGracePeriod.off()
}
