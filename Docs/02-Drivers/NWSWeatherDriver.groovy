/**
 *  NWS Weather Driver
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
 *  ==============================================================================
 *  SETUP INSTRUCTIONS
 *  ==============================================================================
 *  
 *  1. INSTALL THE DRIVER:
 *     - Go to Drivers Code in Hubitat
 *     - Click "New Driver"
 *     - Paste this code and click Save
 *
 *  2. CREATE A VIRTUAL DEVICE:
 *     - Go to Devices → Add Device → Virtual
 *     - Enter a name (e.g., "NWS Weather - Umatilla")
 *     - Select "NWS Weather Driver" as the Type
 *     - Click Save
 *
 *  3. CONFIGURE THE DEVICE:
 *     - Open the newly created device
 *     - Set "Primary Weather Station ID" to your nearest NWS station
 *       Default: KLEE (Leesburg International Airport)
 *     - Set "Fallback Weather Station ID" for redundancy
 *       Default: KSFB (Orlando/Sanford Airport)
 *     - Optionally adjust "Data Stale Threshold" (default 45 minutes)
 *     - Click Save Preferences
 *
 *  4. TEST THE DRIVER:
 *     - Click "Refresh" to manually poll the NWS API
 *     - Check "Current States" for temperature, humidity, etc.
 *     - Verify "activeStation" shows which station is providing data
 *
 *  5. USE IN APPS:
 *     - In FreezeAlertManager or EnvironmentalControlManager:
 *       - Find the "NWS Weather Device" input
 *       - Select this virtual device
 *     - The apps will automatically use the lower of local sensor vs NWS temperature
 *
 *  ==============================================================================
 *  NWS STATION IDs FOR CENTRAL FLORIDA (Umatilla area)
 *  ==============================================================================
 *  
 *  Primary (closest to Umatilla, FL 32784):
 *    KLEE - Leesburg International Airport (~11 miles)
 *  
 *  Fallback options:
 *    KSFB - Orlando/Sanford Airport (~28 miles)
 *    KDED - DeLand Municipal Airport (~26 miles)
 *    KORL - Orlando Executive Airport (~33 miles)
 *    KMCO - Orlando International Airport (~41 miles)
 *
 *  To find stations for other locations:
 *    1. Visit: https://api.weather.gov/points/{latitude},{longitude}
 *    2. Look for "observationStations" URL in response
 *    3. Stations are listed closest to farthest
 *
 *  ==============================================================================
 *  VERSION HISTORY
 *  ==============================================================================
 *  
 *  2025-02-01 - Initial version
 *    - NWS API polling with primary/fallback station support
 *    - Temperature (°F), humidity, wind speed/direction, barometric pressure
 *    - Automatic retry on failure with 5-minute backoff
 *    - Data staleness tracking for downstream apps
 *
 */

metadata {
    definition(
        name: "NWS Weather Driver",
        namespace: "tibrown",
        author: "Tim Brown",
        importUrl: ""
    ) {
        // Standard capabilities
        capability "TemperatureMeasurement"
        capability "RelativeHumidityMeasurement"
        capability "Sensor"
        capability "Refresh"
        
        // Custom attributes for extended weather data
        attribute "windSpeed", "number"           // mph
        attribute "windDirection", "number"       // degrees (0-360)
        attribute "windDirectionCardinal", "string" // N, NE, E, SE, S, SW, W, NW
        attribute "barometricPressure", "number"  // inHg
        attribute "weatherDescription", "string"  // textDescription from NWS
        attribute "activeStation", "string"       // Which station is providing data
        attribute "lastUpdate", "string"          // ISO timestamp of last successful update
        attribute "dataStale", "string"           // "true" or "false" - indicates if data may be outdated
        attribute "observationTime", "string"     // When NWS made the observation
        
        // Commands
        command "configure"
        command "poll"
    }
    
    preferences {
        input name: "primaryStation", type: "text",
              title: "Primary Weather Station ID",
              description: "NWS observation station (e.g., KLEE for Leesburg)",
              defaultValue: "KLEE",
              required: true
        
        input name: "fallbackStation", type: "text",
              title: "Fallback Weather Station ID",
              description: "Backup station if primary fails (e.g., KSFB for Orlando/Sanford)",
              defaultValue: "KSFB",
              required: false
        
        input name: "staleThresholdMinutes", type: "number",
              title: "Data Stale Threshold (minutes)",
              description: "Mark data as stale if not updated within this period",
              defaultValue: 45,
              range: "15..120",
              required: false
        
        input name: "userAgent", type: "text",
              title: "User-Agent Contact Info",
              description: "Email or website for NWS API identification (required by NWS)",
              defaultValue: "HubitatHub (hubitat@example.com)",
              required: true
        
        input name: "logEnable", type: "bool",
              title: "Enable Debug Logging",
              defaultValue: false
        
        input name: "txtEnable", type: "bool",
              title: "Enable Description Text Logging",
              defaultValue: true
    }
}

// ============================================================================
// LIFECYCLE METHODS
// ============================================================================

def installed() {
    log.info "NWS Weather Driver installed"
    initialize()
}

def updated() {
    log.info "NWS Weather Driver updated"
    unschedule()
    initialize()
}

def initialize() {
    log.info "Initializing NWS Weather Driver"
    
    // Initialize state
    state.consecutiveFailures = 0
    state.lastSuccessfulStation = null
    
    // Set initial stale state
    sendEvent(name: "dataStale", value: "true", descriptionText: "Awaiting first data fetch")
    
    // Schedule polling every 30 minutes
    runEvery30Minutes('poll')
    
    // Do initial poll
    runIn(5, 'poll')
    
    if (logEnable) runIn(1800, 'logsOff')
}

def configure() {
    log.info "Configuring NWS Weather Driver"
    initialize()
}

def logsOff() {
    log.warn "Debug logging disabled"
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}

// ============================================================================
// POLLING METHODS
// ============================================================================

def refresh() {
    logDebug "Manual refresh requested"
    poll()
}

def poll() {
    logDebug "Polling NWS weather data"
    
    def primary = settings.primaryStation ?: "KLEE"
    def fallback = settings.fallbackStation ?: "KSFB"
    
    // Try primary station first
    if (fetchWeatherData(primary)) {
        state.consecutiveFailures = 0
        state.lastSuccessfulStation = primary
        return
    }
    
    logWarn "Primary station ${primary} failed, trying fallback ${fallback}"
    
    // Try fallback station
    if (fallback && fetchWeatherData(fallback)) {
        state.consecutiveFailures = 0
        state.lastSuccessfulStation = fallback
        return
    }
    
    // Both failed
    state.consecutiveFailures = (state.consecutiveFailures ?: 0) + 1
    logError "Failed to fetch weather data from both stations (failures: ${state.consecutiveFailures})"
    
    // Mark data as stale
    sendEvent(name: "dataStale", value: "true", descriptionText: "Unable to fetch weather data")
    
    // Schedule retry in 5 minutes if we haven't exceeded retry limit
    if (state.consecutiveFailures < 6) {
        logInfo "Scheduling retry in 5 minutes"
        runIn(300, 'poll')
    } else {
        logError "Too many consecutive failures (${state.consecutiveFailures}), waiting for next scheduled poll"
    }
}

def fetchWeatherData(String stationId) {
    def url = "https://api.weather.gov/stations/${stationId}/observations/latest"
    def userAgentHeader = settings.userAgent ?: "HubitatHub (hubitat@example.com)"
    
    logDebug "Fetching weather from: ${url}"
    
    def params = [
        uri: url,
        headers: [
            "User-Agent": userAgentHeader,
            "Accept": "application/json"
        ],
        contentType: "application/json",
        timeout: 30
    ]
    
    try {
        httpGet(params) { resp ->
            if (resp.status == 200 && resp.data) {
                logDebug "Received response from ${stationId}"
                
                // Parse JSON from the response
                def jsonData = resp.data
                
                // If it's not already parsed (check by trying to access .properties)
                try {
                    if (jsonData.properties == null && jsonData.getText) {
                        def jsonText = jsonData.getText()
                        logDebug "JSON text (first 300 chars): ${jsonText?.take(300)}"
                        jsonData = new groovy.json.JsonSlurper().parseText(jsonText)
                    }
                } catch (Exception parseEx) {
                    // Might already be parsed or needs getText()
                    try {
                        def jsonText = jsonData.getText()
                        logDebug "Fallback parse - JSON text (first 300 chars): ${jsonText?.take(300)}"
                        jsonData = new groovy.json.JsonSlurper().parseText(jsonText)
                    } catch (Exception e2) {
                        logDebug "Data appears to already be parsed"
                    }
                }
                
                return parseWeatherResponse(jsonData, stationId)
            } else {
                logWarn "Unexpected response status: ${resp.status}"
                return false
            }
        }
    } catch (groovyx.net.http.HttpResponseException e) {
        logError "HTTP error fetching from ${stationId}: ${e.statusCode} - ${e.message}"
        return false
    } catch (java.net.SocketTimeoutException e) {
        logError "Timeout fetching from ${stationId}: ${e.message}"
        return false
    } catch (Exception e) {
        logError "Error fetching weather from ${stationId}: ${e.message}"
        if (logEnable) log.debug "Stack trace: ${e}"
        return false
    }
}

def parseWeatherResponse(data, String stationId) {
    try {
        // Debug: log the entire data structure
        logDebug "Data type: ${data?.toString()?.take(200)}"
        
        def props = data.properties
        
        // Debug: log what we got
        logDebug "Props: ${props?.toString()?.take(500)}"
        
        if (!props) {
            logWarn "No properties in response from ${stationId}"
            // Try to access data differently - maybe it's already the properties
            props = data
            logDebug "Trying data directly as props"
        }
        
        // Debug: log keys if it's a map
        if (props instanceof Map) {
            logDebug "Props keys: ${props.keySet()?.take(10)}"
        }
        
        // Debug: log available keys in props.temperature
        logDebug "Temperature object: ${props.temperature}"
        
        // Parse temperature (Celsius to Fahrenheit)
        // Try multiple access methods due to Hubitat JSON parsing quirks
        def tempRaw = null
        if (props.temperature != null) {
            if (props.temperature instanceof Map) {
                tempRaw = props.temperature.value
                logDebug "Accessed as Map: ${tempRaw}"
            } else {
                tempRaw = props.temperature?.value
                logDebug "Accessed with safe navigation: ${tempRaw}"
            }
            // If still null, try direct string key access
            if (tempRaw == null) {
                tempRaw = props.temperature["value"]
                logDebug "Accessed with bracket notation: ${tempRaw}"
            }
        }
        
        logDebug "Final raw temperature: ${tempRaw}"
        
        if (tempRaw != null) {
            // Explicit type conversion to handle various input types
            def tempC = tempRaw as BigDecimal
            def tempF = (tempC * 9.0 / 5.0) + 32.0
            def tempFRounded = (Math.round(tempF * 10) / 10) as BigDecimal
            
            logDebug "Temperature conversion: ${tempC}°C -> ${tempFRounded}°F"
            
            sendEvent(name: "temperature", value: tempFRounded, unit: "°F",
                      descriptionText: "Temperature is ${tempFRounded}°F")
            if (txtEnable) log.info "Temperature: ${tempFRounded}°F (${tempC}°C from ${stationId})"
        } else {
            logWarn "No temperature data from ${stationId}"
        }
        
        // Parse relative humidity
        def humidity = props.relativeHumidity?.value
        if (humidity != null) {
            def humidityRounded = Math.round(humidity as BigDecimal) as Integer
            sendEvent(name: "humidity", value: humidityRounded, unit: "%",
                      descriptionText: "Humidity is ${humidityRounded}%")
            logDebug "Humidity: ${humidityRounded}%"
        }
        
        // Parse wind speed (m/s to mph)
        def windSpeedMs = props.windSpeed?.value
        if (windSpeedMs != null) {
            def windSpeedMph = metersPerSecondToMph(windSpeedMs as BigDecimal)
            def windSpeedRounded = (Math.round(windSpeedMph * 10) / 10) as BigDecimal
            sendEvent(name: "windSpeed", value: windSpeedRounded, unit: "mph",
                      descriptionText: "Wind speed is ${windSpeedRounded} mph")
            logDebug "Wind Speed: ${windSpeedRounded} mph"
        }
        
        // Parse wind direction
        def windDir = props.windDirection?.value
        if (windDir != null) {
            def windDirRounded = Math.round(windDir as BigDecimal) as Integer
            def windDirBD = windDir as BigDecimal
            sendEvent(name: "windDirection", value: windDirRounded, unit: "°",
                      descriptionText: "Wind direction is ${windDirRounded}°")
            sendEvent(name: "windDirectionCardinal", value: degreesToCardinal(windDirBD),
                      descriptionText: "Wind from ${degreesToCardinal(windDirBD)}")
            logDebug "Wind Direction: ${windDirRounded}° (${degreesToCardinal(windDirBD)})"
        }
        
        // Parse barometric pressure (Pa to inHg)
        def pressurePa = props.barometricPressure?.value
        if (pressurePa != null) {
            def pressureInHg = pascalsToInHg(pressurePa as BigDecimal)
            def pressureRounded = (Math.round(pressureInHg * 100) / 100) as BigDecimal
            sendEvent(name: "barometricPressure", value: pressureRounded, unit: "inHg",
                      descriptionText: "Barometric pressure is ${pressureRounded} inHg")
            logDebug "Pressure: ${pressureRounded} inHg"
        }
        
        // Parse weather description
        def description = props.textDescription
        if (description) {
            sendEvent(name: "weatherDescription", value: description,
                      descriptionText: "Weather: ${description}")
            logDebug "Weather: ${description}"
        }
        
        // Parse observation time
        def obsTime = props.timestamp
        if (obsTime) {
            sendEvent(name: "observationTime", value: obsTime,
                      descriptionText: "Observation time: ${obsTime}")
            logDebug "Observation time: ${obsTime}"
        }
        
        // Update metadata
        def now = new Date().format("yyyy-MM-dd'T'HH:mm:ssXXX")
        sendEvent(name: "lastUpdate", value: now,
                  descriptionText: "Last update: ${now}")
        sendEvent(name: "activeStation", value: stationId,
                  descriptionText: "Active station: ${stationId}")
        sendEvent(name: "dataStale", value: "false",
                  descriptionText: "Data is fresh")
        
        logInfo "Weather data updated from ${stationId}"
        return true
        
    } catch (Exception e) {
        logError "Error parsing weather response: ${e.message}"
        if (logEnable) log.debug "Stack trace: ${e}"
        return false
    }
}

// ============================================================================
// STALENESS CHECK (called by apps)
// ============================================================================

/**
 * Check if the weather data is stale (older than threshold)
 * Apps should call this before using the temperature data
 * @return true if data is stale or unavailable, false if fresh
 */
def isDataStale() {
    def lastUpdate = device.currentValue("lastUpdate")
    if (!lastUpdate) {
        return true
    }
    
    try {
        def lastUpdateDate = Date.parse("yyyy-MM-dd'T'HH:mm:ssXXX", lastUpdate)
        def staleMinutes = settings.staleThresholdMinutes ?: 45
        def staleMs = staleMinutes * 60 * 1000
        def age = now() - lastUpdateDate.time
        
        if (age > staleMs) {
            logDebug "Data is stale (age: ${(age / 60000).round(1)} minutes, threshold: ${staleMinutes} minutes)"
            // Update the stale attribute if not already set
            if (device.currentValue("dataStale") != "true") {
                sendEvent(name: "dataStale", value: "true", descriptionText: "Data exceeded staleness threshold")
            }
            return true
        }
        return false
    } catch (Exception e) {
        logError "Error checking staleness: ${e.message}"
        return true
    }
}

// ============================================================================
// UNIT CONVERSION HELPERS
// ============================================================================

def celsiusToFahrenheit(BigDecimal celsius) {
    return (celsius * 9 / 5) + 32
}

def metersPerSecondToMph(BigDecimal ms) {
    return ms * 2.23694
}

def pascalsToInHg(BigDecimal pascals) {
    return pascals / 3386.39
}

def degreesToCardinal(BigDecimal degrees) {
    def directions = ["N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                      "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"]
    def index = ((degrees + 11.25) / 22.5).toInteger() % 16
    return directions[index]
}

// ============================================================================
// LOGGING
// ============================================================================

def logInfo(String msg) {
    log.info "[NWS Weather] ${msg}"
}

def logDebug(String msg) {
    if (logEnable) {
        log.debug "[NWS Weather] ${msg}"
    }
}

def logWarn(String msg) {
    log.warn "[NWS Weather] ${msg}"
}

def logError(String msg) {
    log.error "[NWS Weather] ${msg}"
}
