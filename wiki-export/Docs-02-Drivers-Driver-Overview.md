# Driver Development Overview

## Introduction

Drivers on Hubitat Elevation are the means by which users and apps communicate with devices. Drivers can be:
- **Built-in drivers** (for various Zigbee, Z-Wave, LAN, and cloud devices)
- **User drivers** (custom drivers - focus of this documentation)

Most drivers correspond to real-world devices, but some are **virtual** (simulate device behavior without a real device).

## Creating/Modifying Drivers

1. Navigate to **Drivers Code** in Hubitat web interface (Developer Tools section)
2. Select **New Driver** to create or select existing driver to modify
3. Select **Save** to commit changes (only valid code will save)

## A Simple Driver Structure

```groovy
metadata {
   definition (name: "Custom Virtual Switch", namespace: "MyNamespace", author: "My Name") {
      capability "Actuator"
      capability "Switch"
   }

   preferences {
      // driver preferences here
   }
}

def installed() {
   log.debug "installed()"
}

def updated() {
   log.debug "updated()"
}

def on() {
    // With real device, send Z-Wave/Zigbee/etc. command here
    // For virtual device, just generate event:
    sendEvent(name: "switch", value: "on", descriptionText: "${device.displayName} switch is on")
}

def off() {
    sendEvent(name: "switch", value: "off", descriptionText: "${device.displayName} switch is off")
}
```

## Driver Code Components

### 1. Metadata Block

Unlike apps, driver `definition` and `preferences` are inside a `metadata` block.

#### Definition

Contains driver information and specifies capabilities, commands, attributes, and fingerprints.

**Required Parameters**:
- `name`: Driver name displayed in Type list on device detail page
- `namespace`: Unique identifier for developer (typically GitHub username)
- `author`: Developer identification string

**Optional Parameters**:
- `importUrl`: URL where Groovy code can be found (auto-populates Import button)
- `singleThreaded`: `true`/`false` (default `false`); prevents simultaneous execution

#### Capabilities

Specify capabilities the driver supports:

```groovy
capability "Actuator"
capability "Switch"
capability "SwitchLevel"
```

When specifying a capability, you **must implement all required commands and attributes**.

Example: `capability "Switch"` requires:
- Commands: `on()`, `off()`
- Attribute: `switch` with values `"on"` or `"off"`

See [Driver Capability List](../05-Capabilities/Capability-List.md) for complete list.

#### Custom Commands

Define commands not in existing capabilities:

```groovy
command "myCommand"
command "myCommand", [[name:"param1*", type:"STRING", description:"Required string"]]

// With parameters:
command "myCommand", [
   [name:"My first parameter*", type:"STRING", description:"Required string parameter"],
   [name:"Color", type: "ENUM", constraints: ["red","blue","green"]]
]
```

**Parameter types**: `STRING`, `NUMBER`, `DATE`, `ENUM`, `JSON_OBJECT`, `COLOR_MAP`

**Parameter options**:
- `name`: Parameter name (ending with `*` marks as required)
- `type`: Parameter type
- `description`: Tooltip description
- `constraints`: Valid options for `ENUM`

#### Custom Attributes

Define attributes not in existing capabilities:

```groovy
attribute "attributeName", type

// Examples:
attribute "myAttribute", "string"
attribute "enumAttribute", "enum", ["value 1", "value 2"]
```

**Common types**: `string`, `number`, `enum`

For `enum` type, must provide List of accepted values.

#### Fingerprints

Tell Hubitat how to identify Zigbee or Z-Wave device:

**Zigbee Example**:
```groovy
fingerprint profileId: "profileId", inClusters: "clusters", outClusters: "clusters",
            manufacturer: "manufacturerId", model: "modelId", 
            deviceJoinName: "name", controllerType: "ZGB"
```

**Z-Wave Example**:
```groovy
fingerprint deviceId: "id", inClusters: "clusters", outClusters: "clusters",
            mfr: "manufacturerId", prod: "productTypeId",
            deviceJoinName: "name", controllerType: "ZWV"
```

**Tip**: Switch to "Device" driver, run "Get Info" command, observe fingerprint output in Logs.

### 2. Preferences

Defines what appears under Preferences on device detail page.

**Input types**:
- `text`: String
- `number`: Integer
- `decimal`: Double
- `enum`: List (requires `options` parameter)
- `bool`: Boolean

**Input options**:
- `name`: Uniquely identifies setting
- `type`: Data type
- `title`: Text shown in UI
- `description`: Long text description
- `required`: `true`/`false`
- `defaultValue`: Default value
- `options`: For `enum` type
- `range`: For `number`/`decimal` (e.g., `"0..10"`)

**Example**:
```groovy
preferences {
    input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    input name: "txtEnable", type: "bool", title: "Enable descriptionText logging", defaultValue: true
    input name: "parameter1", type: "enum", title: "Parameter 1", options: ["0":"Off", "1":"On"]
}
```

**Accessing preferences**:
```groovy
settings.logEnable    // or settings["logEnable"]
logEnable             // direct access
```

### 3. Required Methods

```groovy
def installed() {
    // Called when device first added
}

def updated() {
    // Called when user selects Save Preferences
}

def initialize() {
    // Called on hub startup if driver has capability "Initialize"
    // Use for re-establishing connections
}

def parse(String desc) {
    // Handles raw incoming data from Zigbee, Z-Wave, or LAN devices
    // Generally creates events as needed
    // Virtual devices typically don't use this
}
```

### 4. Command Methods

Implement all commands required by capabilities:

```groovy
def on() {
    // Send command to device (Z-Wave, Zigbee, HTTP, etc.)
    // Or for virtual device, just generate event:
    sendEvent(name: "switch", value: "on")
}

def off() {
    sendEvent(name: "switch", value: "off")
}

def setLevel(level, duration=0) {
    sendEvent(name: "level", value: level, unit: "%")
}
```

### 5. Generating Events

Use `sendEvent()` to update attributes:

```groovy
sendEvent(name: "attributeName", value: value)
sendEvent(name: "attributeName", value: value, unit: "unit")
sendEvent(name: "attributeName", value: value, descriptionText: "Description", isStateChange: true)
```

**Parameters**:
- `name`: Attribute name (required)
- `value`: Attribute value (required)
- `unit`: Unit of measurement (optional)
- `descriptionText`: Human-readable description (optional, shown in events log)
- `isStateChange`: Force event even if value unchanged (optional)
- `displayed`: Show in event log (optional, defaults to `true`)

### 6. Storing Data (state)

Use built-in `state` object (behaves like a Map):

```groovy
state.foo = "bar"
```

**`state` vs `atomicState`**:
- `state`: Writes data before driver sleeps (more efficient)
- `atomicState`: Commits changes immediately
- Use `singleThreaded: true` as efficient alternative to `atomicState`

**`atomicState` convenience method**:
```groovy
atomicState.updateMapValue(stateKey, key, value)
```

### 7. Logging

Available log methods:
- `log.info("message")`
- `log.debug("message")`
- `log.trace("message")`
- `log.warn("message")`
- `log.error("message")`

**Best practice**: Provide user control and auto-disable debug logging:

```groovy
def updated() {
    if (logEnable) runIn(1800, logsOff)  // Auto-disable after 30 min
}

def logsOff() {
    log.warn "Debug logging disabled"
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}

def myMethod() {
    if (logEnable) log.debug "Debug message"
    if (txtEnable) log.info "Description text"
}
```

## Protocol-Specific Development

### Z-Wave Drivers

- Parse Z-Wave command classes
- Use `zwave` helper object
- See: [Building a Z-Wave Driver](https://docs2.hubitat.com/en/developer/driver/building-a-zwave-driver)
- Reference: [Z-Wave Classes](https://docs2.hubitat.com/en/developer/driver/zwave-classes)

### Zigbee Drivers

- Parse Zigbee clusters
- Use `zigbee` helper object
- See: [Building a Zigbee Driver](https://docs2.hubitat.com/en/developer/driver/building-a-zigbee-driver)

### Matter Drivers

- Use Matter clusters
- See: [Building a Matter Driver](https://docs2.hubitat.com/en/developer/driver/building-a-matter-driver)

### LAN/Cloud Drivers

- Use HTTP requests
- Implement interfaces (EventStream, WebSocket, Telnet)
- See: [Building a LAN/Cloud Driver](https://docs2.hubitat.com/en/developer/driver/building-a-lan-driver)

## Parent/Child Drivers

Create child devices for multi-endpoint devices:

```groovy
def addChildDevice(String namespace, String typeName, String deviceNetworkId, Map properties)
def getChildDevice(String deviceNetworkId)
def getChildDevices()
def deleteChildDevice(String deviceNetworkId)
```

See: [Parent/Child Drivers](https://docs2.hubitat.com/en/developer/driver/parent-child-drivers)

## Common Driver Patterns

### 1. Refresh/Poll Pattern

```groovy
capability "Refresh"

def refresh() {
    if (logEnable) log.debug "refresh()"
    // Send command to device to request current state
    return zigbee.onOffRefresh() + zigbee.levelRefresh()
}
```

### 2. Configuration Pattern

```groovy
capability "Configuration"

def configure() {
    if (logEnable) log.debug "configure()"
    // Configure device parameters
    return zigbee.configureReporting(0x0006, 0x0000, 0x10, 0, 600, null) +
           zigbee.configureReporting(0x0008, 0x0000, 0x20, 1, 3600, 0x01)
}
```

### 3. Initialize Pattern

```groovy
capability "Initialize"

def initialize() {
    if (logEnable) log.debug "initialize()"
    // Re-establish connections, subscriptions
    unschedule()
    if (autoRefresh) runEvery5Minutes("refresh")
}
```

### 4. Parse Pattern

```groovy
def parse(String description) {
    if (logEnable) log.debug "parse: ${description}"
    
    def descMap = zigbee.parseDescriptionAsMap(description)
    
    if (descMap.cluster == "0006" && descMap.attrId == "0000") {
        def value = descMap.value == "01" ? "on" : "off"
        sendEvent(name: "switch", value: value, descriptionText: "${device.displayName} switch is ${value}")
    }
}
```

## Best Practices

1. **Use specific types** instead of `def`:
   ```groovy
   void on() {
      // Better than: def on()
   }
   ```

2. **Attributes vs State**:
   - Use attributes for real-world device state changes
   - Use state for internal driver data

3. **Logging conventions**:
   - Provide `logEnable` and `txtEnable` preferences
   - Auto-disable debug logging after 30 minutes
   - Use appropriate log levels

4. **Efficient data storage**:
   - Use `state` for small amounts of data
   - For large data, consider File Manager or `@Field` variables

5. **sendEvent best practices**:
   - Always include `descriptionText` for user-facing events
   - Use `unit` parameter for numeric values
   - Don't generate events excessively

6. **Error handling**:
   ```groovy
   try {
       // risky operation
   } catch (e) {
       log.error "Error: ${e.message}"
   }
   ```

## Device Object Methods

Available via `device` reference in driver code:

```groovy
device.displayName          // Device name
device.label                // Custom label
device.name                 // Device type name
device.id                   // Device ID
device.deviceNetworkId      // Network ID
device.zigbeeId             // Zigbee ID (if applicable)
device.currentValue("attr") // Get current attribute value
device.updateSetting(name, [value: value, type: type])
device.updateDataValue(key, value)
device.getDataValue(key)
device.removeDataValue(key)
```

## Data Values vs State vs Attributes

- **Data Values**: Device-specific data (firmware version, manufacturer, etc.)
  - Persistent across driver changes
  - Use for device-specific configuration

- **State**: Driver runtime data
  - Lost when driver code changes
  - Use for temporary driver state

- **Attributes**: Current device state
  - Visible to users and apps
  - Generate events when changed
  - Use for automation-relevant values

## Further Reading

- [Driver Object](https://docs2.hubitat.com/en/developer/driver/driver-object)
- [Device Object](https://docs2.hubitat.com/en/developer/device-object)
- [Common Methods](https://docs2.hubitat.com/en/developer/common-methods-object)
- [Driver Capability List](../05-Capabilities/Capability-List.md)
- [Example Drivers](https://github.com/hubitat/HubitatPublic/tree/master/examples/drivers)

---
*Sources:*
- *https://docs2.hubitat.com/en/developer/driver/overview*
- *https://docs2.hubitat.com/en/developer/driver/definition*
- *https://docs2.hubitat.com/en/developer/driver/preferences*
