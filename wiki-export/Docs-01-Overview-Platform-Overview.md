# Hubitat Platform Overview

## Platform Architecture

Hubitat is an event-driven system. Automations on Hubitat are performed using an **app** that acts on a **device** (the code behind a device is called a **driver**), where the device in Hubitat usually corresponds to some real-world device like a switch or thermostat.

### Key Concepts

- **Devices**: Offer commands that can be executed (e.g., turn a switch off or set a thermostat setpoint)
- **Events**: Usually changes in state (e.g., motion becoming active, switch turning off)
- **Attributes**: Have a value representing current state of a device (visible under Current States on device detail page)
- **Commands**: Actions that can be performed on a device

### Event-Attribute Relationship

While not explicitly required, commands and attributes often have a real-world relationship:
- `on()` and `off()` commands on a switch should result in events that change the value of the `switch` attribute
- However, exact behavior is driver-dependent
- Use a "fire-and-forget" approach: driver sends command asynchronously, then handles generating events when device sends information back

## Role of Apps

Apps might subscribe to one or more device events, then perform user-configured actions in response.

**Example**: An app could listen for (subscribe to) a specific motion sensor becoming active (an event), then tell a switch to turn on (run a command on a device).

### Types of Apps

1. **Automation Apps**: Create automations based on device events
2. **Integration Apps**: Help integrate devices, handle communication between service/device and Hubitat devices
   - Examples: Hue Bridge Integration, Sonos Integration

## Role of Drivers

Drivers "translate" information to/from Zigbee, Z-Wave, Matter, LAN/cloud API, or device protocol into "standard" Hubitat attributes (events) and commands, so both users and apps can interact with devices without knowing underlying details.

### Virtual Drivers

Virtual drivers simulate real device behavior without an underlying real device. Useful for:
- Testing apps without needing a real device
- Creative automation uses by users

## Capabilities

An important concept for driver (and app) development. Capabilities specify a set of commands and/or attributes that a driver implementing that capability should offer.

Most drivers implement multiple capabilities depending on device features.

**Example**: A driver implementing `capability "Switch"` must offer:
- `on()` and `off()` commands
- `switch` attribute that reports either `on` or `off` value

## Development Environment

### Restrictions

You cannot:
- Define your own classes or use custom JARs
- Access certain classes (subset of Java/Groovy classes is allowed)
- Use methods like `println()` or `sleep()`
- Create threads

### Solutions Provided

The sandbox provides ways to do everything needed:
- Use `log.debug()` and similar methods instead of `println()`
- Use Hubitat-provided methods for scheduling and async operations

### Groovy Version

Hubitat uses **Groovy version 2.4**

## Groovy Tips

### Syntax Features

1. **No semicolons required** (typically omitted by Groovy developers)

2. **Optional parentheses** when calling methods with at least one parameter:
   ```groovy
   log.debug "My log message"  // parentheses optional
   ```

3. **Optional `return` keyword**: Last statement in method used as return value (if method is not `void`)

4. **String interpolation** using `${}`:
   ```groovy
   String name = "John"
   log.debug "My name is ${name}"  // Prints: My name is John
   ```

5. **Closures**: Anonymous blocks of code similar to methods
   ```groovy
   def myList = [1,2,3]
   log.debug myList.any { it == 1 } // returns true
   
   myMotionSensors.any { it.currentValue("motion") == "active" }
   ```

6. **Named parameters**: Makes code readable like English
   ```groovy
   input name: "mySettingName", type: "text", title: "Write your text here"
   ```

7. **Naming conventions**: Use `camelCase` for:
   - Method and variable names
   - Setting (`input`) names
   - Custom commands or attributes

## Features Provided

### For Apps

- Methods from [App object](https://docs2.hubitat.com/en/developer/app/app-object)
- Methods from [InstalledApp object](https://docs2.hubitat.com/en/developer/app/installedapp-object)
- [Common methods](https://docs2.hubitat.com/en/developer/common-methods-object)

### For Drivers

- Methods from [Device object](https://docs2.hubitat.com/en/developer/device-object)
- [Common methods](https://docs2.hubitat.com/en/developer/common-methods-object)

### For Both

- Built-in `log` object:
  - `log.debug("My log entry")`
  - `log.info("My info entry")`
  - `log.warn("My warning")`
  - `log.error("My error")`
  - `log.trace("My trace entry")`

## Next Steps

- **For Apps**: Start with [App Development Overview](App-Overview.md)
- **For Drivers**: Start with [Driver Development Overview](../02-Drivers/Driver-Overview.md)

---
*Source: https://docs2.hubitat.com/en/developer/overview*
