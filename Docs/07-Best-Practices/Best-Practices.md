# Best Practices for Hubitat Development

## Overview

This document outlines best practices for developing efficient, user-friendly apps and drivers for Hubitat Elevation.

## 🎯 Core Principles

1. **Efficiency**: Minimize resource usage (memory, CPU)
2. **User Experience**: Provide clear interfaces and appropriate logging
3. **Reliability**: Handle errors gracefully
4. **Maintainability**: Write clean, documented code

---

## 💾 Storing Data

### Use state for Small Amounts of Data

`state` and `atomicState` serialize/deserialize to/from JSON. Large amounts of data should use:

**File Manager API**:
```groovy
// Write file
uploadHubFile("myData.txt", dataBytes)

// Read file
def data = downloadHubFile("myData.txt")
```

**Static @Field Variables** (not persistent):
```groovy
@groovy.transform.Field
static Map deviceCache = [:]

// For drivers, use device ID as key:
@groovy.transform.Field
static Map deviceData = new java.util.concurrent.ConcurrentHashMap()

// Store per-device:
deviceData[device.id] = myData
```

### state vs atomicState

```groovy
// PREFERRED: Use state (more efficient)
state.myValue = "data"

// ONLY IF NEEDED: Use atomicState for concurrent access
atomicState.myValue = "data"

// OR BETTER: Use singleThreaded in definition
definition(singleThreaded: true)
```

---

## 📊 Attributes vs State

### Use Attributes For:
- ✅ Real-world device state changes users/apps care about
- ✅ Values that trigger automations
- ✅ Information displayed on dashboards

```groovy
// Good: User wants to know and automate on switch state
sendEvent(name: "switch", value: "on")

// Good: Temperature changes matter for automation
sendEvent(name: "temperature", value: temp, unit: "°F")
```

### Use State For:
- ✅ Internal driver/app data
- ✅ Cached values
- ✅ Tracking between executions

```groovy
// Good: Internal tracking
state.lastPollTime = now()
state.retryCount = 0
```

### ⚠️ Avoid:
```groovy
// BAD: Don't use attributes for internal-only data
sendEvent(name: "internalCounter", value: counter)

// BETTER: Use state instead
state.counter = counter
```

---

## 📝 Logging

### Provide User Control

```groovy
preferences {
    input "logEnable", "bool", title: "Enable debug logging", defaultValue: false
    input "txtEnable", "bool", title: "Enable descriptionText logging", defaultValue: true
}
```

### Auto-Disable Debug Logging

```groovy
def updated() {
    log.info "Updated with settings: ${settings}"
    if (logEnable) runIn(1800, logsOff)  // 30 minutes
}

def logsOff() {
    log.warn "Debug logging disabled"
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}
```

### Use Appropriate Log Levels

```groovy
def myMethod() {
    if (logEnable) log.debug "Debug details: ${details}"
    if (txtEnable) log.info "${device.displayName} action completed"
    log.warn "Warning: unusual condition detected"
    log.error "Error: ${e.message}"
}
```

### Best Practice Pattern:
```groovy
void logDebug(String msg) {
    if (logEnable) log.debug msg
}

void logInfo(String msg) {
    if (txtEnable) log.info msg
}

// Usage:
logDebug("Processing data: ${data}")
logInfo("${device.displayName} turned on")
```

---

## ⚡ Performance: Explicit Types vs def

Using explicit types can improve performance:

### ✅ Preferred:
```groovy
void myMethod(String param) {
    Integer count = 0
    String result = "value"
}
```

### ⚠️ Less Efficient:
```groovy
def myMethod(param) {
    def count = 0
    def result = "value"
}
```

### Apply To:
- Method return types
- Method parameters
- Variable declarations

---

## 🔄 sendEvent Best Practices

### Always Include descriptionText

```groovy
// GOOD:
sendEvent(name: "switch", value: "on", 
          descriptionText: "${device.displayName} switch is on")

// ACCEPTABLE for internal attributes:
sendEvent(name: "switch", value: "on")
```

### Include Units Where Appropriate

```groovy
sendEvent(name: "temperature", value: temp, unit: "°F")
sendEvent(name: "level", value: level, unit: "%")
sendEvent(name: "power", value: watts, unit: "W")
```

### Use isStateChange When Needed

```groovy
// Force event even if value unchanged:
sendEvent(name: "pushed", value: buttonNumber, isStateChange: true)
```

### Don't Over-Generate Events

```groovy
// BAD: Generates event every second
runEvery1Second("updateStatus")
def updateStatus() {
    sendEvent(name: "status", value: getStatus())
}

// BETTER: Only generate when changed
def updateStatus() {
    def newStatus = getStatus()
    if (newStatus != device.currentValue("status")) {
        sendEvent(name: "status", value: newStatus)
    }
}
```

---

## 🛡️ Error Handling

### Always Wrap Risky Operations

```groovy
def httpRequest() {
    try {
        httpGet([uri: url]) { resp ->
            if (resp.success) {
                parseResponse(resp.data)
            }
        }
    } catch (e) {
        log.error "HTTP request failed: ${e.message}"
        if (logEnable) log.debug "Stack trace: ${e}"
    }
}
```

### Parse with Safety

```groovy
def parse(String description) {
    try {
        def descMap = zigbee.parseDescriptionAsMap(description)
        // Process descMap
    } catch (e) {
        log.error "Parse error: ${e.message}"
        if (logEnable) log.debug "Failed description: ${description}"
    }
}
```

---

## 🏗️ Code Organization

### Method Naming

```groovy
// GOOD: camelCase
def myMethod() { }
def parseTemperature() { }
def handleMotionEvent() { }

// BAD: Other styles
def my_method() { }  // snake_case
def MyMethod() { }   // PascalCase
```

### Setting Names

```groovy
// GOOD: Will become variables
input "motionSensor", "capability.motionSensor"
input "logEnable", "bool"

// Use directly:
motionSensor.currentValue("motion")
if (logEnable) log.debug "message"
```

### Custom Command/Attribute Names

```groovy
// GOOD:
command "myCommand"
attribute "myAttribute", "string"

// AVOID:
command "my_command"
attribute "my_attribute", "string"
```

---

## 📱 Driver-Specific Best Practices

### Implement Standard Lifecycle Methods

```groovy
def installed() {
    log.info "${device.displayName} installed"
    initialize()
}

def updated() {
    log.info "${device.displayName} updated with settings: ${settings}"
    if (logEnable) runIn(1800, logsOff)
    unschedule()
    initialize()
}

def initialize() {
    log.info "${device.displayName} initializing"
    // Re-establish connections
    // Set up schedules
}
```

### Configuration Pattern

```groovy
capability "Configuration"

def configure() {
    log.info "configure()"
    List<String> cmds = []
    
    // Add configuration commands
    cmds += zigbee.configureReporting(...)
    cmds += zigbee.writeAttribute(...)
    
    return cmds
}
```

### Refresh Pattern

```groovy
capability "Refresh"

def refresh() {
    if (logEnable) log.debug "refresh()"
    List<String> cmds = []
    
    cmds += zigbee.onOffRefresh()
    cmds += zigbee.levelRefresh()
    cmds += zigbee.colorTemperatureRefresh()
    
    return cmds
}
```

---

## 📲 App-Specific Best Practices

### Subscribe Pattern

```groovy
def updated() {
    unsubscribe()  // Always unsubscribe first
    initialize()
}

def initialize() {
    subscribe(motionSensor, "motion", motionHandler)
    subscribe(contactSensor, "contact.open", contactOpenHandler)
}
```

### Scheduling Pattern

```groovy
def updated() {
    unschedule()  // Always unschedule first
    
    if (runDaily) {
        schedule("0 0 8 * * ?", dailyHandler)
    }
    
    if (checkInterval) {
        runEvery5Minutes(checkHandler)
    }
}
```

### Dynamic Page Pattern

```groovy
def pageName(params) {
    dynamicPage(name: "pageName", title: "Page Title", install: true, uninstall: true) {
        section("Section Title") {
            input "setting", "type", title: "Title", submitOnChange: true
            
            if (setting) {
                // Show more options based on setting
                paragraph "Additional options:"
                input "moreSetting", "type", title: "More"
            }
        }
    }
}
```

---

## 🔐 Security Best Practices

### Don't Log Sensitive Data

```groovy
// BAD:
log.debug "Password: ${password}"
log.debug "API Key: ${apiKey}"

// GOOD:
log.debug "Authentication configured"
log.debug "API Key present: ${apiKey ? 'yes' : 'no'}"
```

### Validate User Input

```groovy
def setLevel(level) {
    // Validate input
    if (level < 0 || level > 100) {
        log.error "Invalid level: ${level}"
        return
    }
    
    // Process valid input
    sendEvent(name: "level", value: level)
}
```

---

## 📚 Documentation

### Document Complex Logic

```groovy
/**
 * Parse temperature reading from Zigbee cluster
 * Temperature is reported in hundredths of degrees Celsius
 * Convert to Fahrenheit or Celsius based on location setting
 */
def parseTemperature(value) {
    BigDecimal tempC = value / 100.0
    BigDecimal temp = location.temperatureScale == "F" ? 
                      (tempC * 1.8 + 32) : tempC
    return temp.setScale(1, BigDecimal.ROUND_HALF_UP)
}
```

### Comment Non-Obvious Code

```groovy
// Zigbee spec requires 2-second delay between commands
pauseExecution(2000)

// Workaround for firmware bug in v1.2.3
if (firmwareVersion == "1.2.3") {
    // Double the delay
    pauseExecution(4000)
}
```

---

## ✅ Pre-Release Checklist

- [ ] All capabilities properly implemented
- [ ] Logging preferences included
- [ ] Debug logging auto-disables
- [ ] Error handling implemented
- [ ] Input validation added
- [ ] descriptionText included in sendEvent calls
- [ ] No sensitive data in logs
- [ ] Code follows naming conventions
- [ ] Complex logic documented
- [ ] Tested with real devices/scenarios
- [ ] Memory usage acceptable (check Logs for warnings)

---

## 📖 Additional Resources

- [Platform Overview](../01-Overview/Platform-Overview.md)
- [Driver Overview](../02-Drivers/Driver-Overview.md)
- [App Overview](../03-Apps/App-Overview.md)
- [Capability Reference](../05-Capabilities/Capability-Quick-Reference.md)
- [Community Forum](https://community.hubitat.com/)

---

*Compiled from official Hubitat best practices documentation*  
*Last updated: October 12, 2025*
