# Analysis of Hubitat Apps for Rule Logic

This document analyzes two Hubitat Apps from the `hubitat-bearmay` repository to understand how to implement automation logic ("Rules") as custom Apps. The goal is to assist in converting Rule Machine rules into standalone Groovy Apps.

## Case Study 1: Power Outage Manager (`powOutMgr.groovy`)

**Location:** `hubitat-bearmay/apps/powOutMgr.groovy`

### Overview
The **Power Outage Manager** is a sophisticated automation App that monitors power sources (like a UPS or mains power sensor) and performs a sequence of actions when a power outage is detected, and another sequence when power is restored. It effectively implements a complex "Rule" with triggers, conditions, delays, and actions.

### Key Components & Structure

1.  **Definition (`definition`)**:
    *   Defines metadata: Name, namespace, author, description, category.
    *   `singleThreaded: false`: Allows concurrent execution (important for complex logic).

2.  **User Interface (`preferences`)**:
    *   **Triggers**: Uses `input "triggerDevs", "capability.powerSource..."` to let the user select devices.
    *   **Configuration**: Inputs for delays (`triggerDelay`), agreement count (`agreement`), and notifications.
    *   **Dynamic Pages**: Uses `href` to navigate to sub-pages (`outAction`, `upAction`) for configuring specific actions, keeping the main interface clean.

3.  **Event Subscription (`initialize` / `updated`)**:
    *   The App subscribes to device events based on the user's selection.
    *   Example: `subscribe(it, "powerSource", "triggerOccurrence")`
    *   This is equivalent to the **Trigger** section of a Rule.

4.  **Event Handler (`triggerOccurrence`)**:
    *   This method is called when a subscribed event occurs.
    *   **Logic**: It checks the state of all trigger devices to see if they meet the "agreement" criteria (e.g., "at least 2 devices must report outage").
    *   **State Management**: Uses `state.onMains` and `state.onBattery` to track the current status of devices.

5.  **Actions & Delays**:
    *   **Delays**: Uses `runIn(delay, method)` to schedule actions. This is equivalent to "Wait" or "Delay" in Rule Machine.
    *   **Cancellation**: Uses `unschedule(method)` to cancel pending actions if power is restored before the delay expires.
    *   **Complex Actions**:
        *   **Notifications**: `sendNotificationEvent()`.
        *   **Hub Management**: Can reboot or shutdown the hub (`httpPost` to hub endpoints).
        *   **Device Control**: Can turn off radios (Zigbee/Z-Wave) or specific devices.
        *   **App Control**: Can disable other Apps/Rules.

### Lessons for Converting Rules to Apps

*   **State is Key**: Use `state` (or `atomicState`) to persist variables between event executions. In Rules, these are Local/Global Variables.
*   **Debouncing**: The App uses `runIn` to wait before taking action, effectively debouncing the power outage event to avoid false alarms.
*   **User Choice**: Unlike a hardcoded Rule, an App allows the user to select *which* devices to use and *what* thresholds to set via `preferences`.

## Case Study 2: Motion Timing (`motionTiming.groovy`)

**Location:** `hubitat-bearmay/apps/motionTiming.groovy`

### Overview
**Motion Timing** is a utility App that compares motion events from multiple devices. Unlike a typical automation rule, its primary purpose is **data analysis and reporting**.

### Key Components

1.  **Data Querying**:
    *   Uses `device.statesSince("motion", date, [max:100])` to retrieve historical event data.
    *   Rule Machine generally reacts to *current* events; Apps can easily query *past* history.

2.  **Visualization**:
    *   Constructs an HTML table (`dispTable`) to display the data in the Hubitat UI.
    *   This demonstrates how Apps can provide custom dashboards or reports that are impossible in Rule Machine.

### Why use an App?
This example highlights a use case where Rule Machine is insufficient. If your "Rule" needs to analyze trends, compare historical timestamps, or generate a report, a Groovy App is the correct tool.

## Guide: From Rule to App

To convert a Rule Machine rule into a Groovy App, map the concepts as follows:

| Rule Machine Concept | Groovy App Implementation |
| :--- | :--- |
| **Trigger** | `subscribe(device, attribute, handlerMethod)` in `initialize()` |
| **Condition (IF)** | `if (condition) { ... }` inside the `handlerMethod` |
| **Action (Then)** | Direct method calls (e.g., `switch.on()`) or helper methods |
| **Wait / Delay** | `runIn(seconds, methodToRunLater)` |
| **Cancel Delayed Action** | `unschedule(methodToRunLater)` |
| **Local Variable** | `state.variableName` (persists) or local `def var` (transient) |
| **Global Variable** | Access via `getGlobalVar()` / `setGlobalVar()` |
| **Required Expression** | Check condition at start of `handlerMethod`; return if false |

### Basic App Template

```groovy
definition(
    name: "My Custom Rule App",
    namespace: "myNamespace",
    author: "Me",
    description: "Converted from Rule Machine",
    category: "My Apps",
    iconUrl: "",
    iconX2Url: ""
)

preferences {
    section("Triggers") {
        input "motionSensors", "capability.motionSensor", title: "Motion Sensors", multiple: true
    }
    section("Actions") {
        input "lights", "capability.switch", title: "Lights to Turn On", multiple: true
    }
}

def installed() { initialize() }
def updated() { unsubscribe(); initialize() }

def initialize() {
    // Subscribe to Triggers
    subscribe(motionSensors, "motion.active", motionHandler)
}

def motionHandler(evt) {
    // Conditions & Actions
    log.info "Motion detected by ${evt.displayName}"
    lights.on()
}
```
