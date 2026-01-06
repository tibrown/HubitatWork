# App Development Overview

## Introduction

Apps on Hubitat Elevation are the means by which users configure automations. Apps can be:
- **Built-in apps** (Room Lighting, Button Controller, Notifications, etc.)
- **User apps** (custom apps - focus of this documentation)

## Creating/Modifying Apps

1. Navigate to **Apps Code** in Hubitat web interface (Developer Tools section)
2. Select **New App** to create or select existing app to modify
3. Select **Save** to commit changes (only valid code will save)

## A Simple App Structure

```groovy
definition(
    name: "My First App",
    namespace: "MyNamespace",
    author: "My Name",
    description: "A simple example app",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: ""
)

preferences {
    page(name: "mainPage", title: "My Page", install: true, uninstall: true) {
        section {
            paragraph "Hello, Hubitat!"
        }
    }
}

// Called when app first installed
def installed() {
    log.trace "installed()"
}

// Called when user presses "Done" button in app
def updated() {
    log.trace "updated()"
}

// Called when app uninstalled
def uninstalled() {
   log.trace "uninstalled()"
}
```

## App Components

### 1. Definition

The `definition()` contains metadata about the app.

#### Required Parameters:
- `name`: App name displayed in Add User App dialog and App Status page
- `namespace`: Unique identifier for developer (username, typically GitHub username)
- `author`: Developer identification string (display purposes only)
- `description`: Short description of app's purpose
- `category`: Currently not used
- `iconUrl`: Currently not used; set to empty string
- `iconX2Url`: Currently not used; set to empty string

#### Optional Parameters:
- `iconX3Url`: Not currently used, empty string
- `installOnOpen`: `true`/`false`, defaults to `false`; installs app on first open
- `documentationLink`: Link to documentation (shows as "?" icon)
- `videoLink`: Link to video documentation
- `importUrl`: URL where raw Groovy code can be found
- `oauth`: `true`/`false`, defines if app uses OAuth
- `parent`: If child app, specifies parent app as `"namespace:app name"`
- `singleInstance`: `true`/`false`; if true, only one instance can be installed
- `singleThreaded`: `true`/`false` (default `false`); prevents simultaneous execution

### 2. Preferences

Defines the user interface for the app. Contains one or more `page` blocks.

#### Page Parameters (all optional):
- `name`: Displays at top of page
- `title`: Page title
- `nextPage`: For multi-page apps, name of next page
- `uninstall`: If `true`, shows Remove button (should be true on at least one page)
- `install`: If `true`, shows install button

#### Sections

Pages must contain one or more sections:

```groovy
section("My Section Title") {
   // inputs, paragraphs, etc. here
}
```

Section signatures:
- `section {}`
- `section("Title") {}`
- `section(Boolean hideable, "Title") {}`
- `section(Boolean hideable, Boolean hidden, "Title") {}`

#### Inputs

The `input()` method displays various types of inputs.

**Basic format**:
```groovy
input(name: "elementName", type: "elementType", title: "elementTitle", /* options */)

// Simplified (omit parentheses and labels):
input "myName", "myType", title: "My Input"
```

**Input Types**:
- `capability.capabilityName`: Device selector by capability
- `device.DriverName`: Device selector by driver name
- `text`: String
- `textarea`: Multi-line String (optional `rows` parameter)
- `number`: Integer number (`Long`)
- `decimal`: Floating point number (`Double`)
- `enum`: List; requires `options` parameter
- `bool`: Boolean (on/off slider)
- `time`: DateTime String with time picker
- `date`: Date String with date picker
- `color`: Color Map with color picker
- `button`: UI button (generates callback to `appButtonHandler(String buttonName)`)

**Input Options**:
- `title`: Display title
- `description`: Optional long description
- `required`: `true`/`false`
- `defaultValue`: Default value displayed
- `submitOnChange`: `true` refreshes page on completion
- `multiple`: `true` for device/enum inputs (stores as List)
- `width`: 1 to 12 (12 = full width desktop, 4 = mobile)
- `showFilter`: `true` shows search box for capability inputs

**Accessing Input Values**:
```groovy
settings.myName    // or settings["myName"]
myName             // direct access
```

#### Paragraph

Displays text output (may include HTML):
```groovy
paragraph "Hello, world!"
```

#### href

Links to other app pages:
```groovy
href name: "myHref", page: "MyOtherPage", title: "Go to other page!"
```

Parameters:
- `name`: Unique name
- `page`: Name of page to link to
- `params`: Optional Map of values to pass
- `title`: Link title
- `description`: Additional text
- `state`: `"complete"` (blue) or `null` (gray)

### 3. App Lifecycle

Apps wake in response to:
- Device/hub/location events (via `subscribe()`)
- Schedules created via `runIn()` or similar
- Rendering UI when user opens app
- Installation, update, or uninstallation
- HTTP endpoint hits (OAuth and `mappings`)

### 4. Storing Data (state)

Use built-in `state` object (behaves like a Map):

```groovy
state.foo = "bar"
```

**`state` vs `atomicState`**:
- `state`: Writes data before app sleeps (more efficient)
- `atomicState`: Commits changes immediately
- Both refer to same data, but choose one approach
- Use `singleThreaded: true` as efficient alternative to `atomicState`

**`atomicState` convenience method**:
```groovy
atomicState.updateMapValue(stateKey, key, value)
```

### 5. Required Methods

```groovy
def installed() {
    // Called once when app first installed
}

def updated() {
    // Called when user selects Done button
}

def uninstalled() {
    // Called when app uninstalled
    // Most apps won't need to do anything here
}
```

### 6. Logging

Available log methods:
- `log.info("message")`
- `log.debug("message")`
- `log.trace("message")`
- `log.warn("message")`
- `log.error("message")`

Example with string interpolation:
```groovy
log.debug "The value of state.foo is ${state.foo}"
```

## Dynamic Pages

For more control, use `dynamicPage` instead of `page`. Pages defined by returning page from Groovy method rather than inside `preferences`.

See: [Building a Multi-Page App](https://docs2.hubitat.com/en/developer/app/building-a-multipage-app)

## Single-Page Apps

For single-page apps, omit `page` block entirely and use `section` blocks directly.

Default features provided:
- Input for user to provide "label" (app name)
- Input for "only in certain mode(s)" restriction

## Subscribing to Events

```groovy
subscribe(device, "attributeName", handlerMethod)
subscribe(device, "attributeName.value", handlerMethod)  // specific value
subscribe(devices, "attributeName", handlerMethod)  // multiple devices
```

## Scheduling

```groovy
runIn(seconds, handlerMethod)
runOnce(dateTime, handlerMethod)
schedule(cronExpression, handlerMethod)
unschedule()  // cancel all scheduled jobs
```

## Device Commands

```groovy
mySwitch.on()
mySwitch.off()
myDimmer.setLevel(50)
```

## Best Practices

1. **Use specific types** instead of `def` for better performance:
   ```groovy
   void appButtonHandler(String buttonName) {
      // Better than: def appButtonHandler(buttonName)
   }
   ```

2. **Attributes vs State**:
   - Use attributes for values that trigger automations
   - Use state for internal data

3. **Logging conventions**:
   - Provide user control over logging
   - Use appropriate log levels
   - Auto-disable debug logging after timeout

4. **Efficient data storage**:
   - `state` is good for small amounts of data
   - For large data, consider:
     - File Manager files
     - `@Field` variables (not persistent across reboots)

## Hub-Provided Setting Names (Reserved)

Avoid using these input names:
- `hubitatQueryString`: JSON of URL parameters

## Further Reading

- [App Object](https://docs2.hubitat.com/developer/app/app-object)
- [InstalledApp Object](https://docs2.hubitat.com/developer/app/installedapp-object)
- [Common Methods](https://docs2.hubitat.com/developer/common-methods-object)
- [Building a Simple App](https://docs2.hubitat.com/en/developer/app/building-a-simple-app)
- [Parent/Child Apps](https://docs2.hubitat.com/en/developer/app/parent-child-apps)
- [OAuth Process](https://docs2.hubitat.com/en/developer/app/oauth)

---
*Sources:*
- *https://docs2.hubitat.com/en/developer/app/overview*
- *https://docs2.hubitat.com/en/developer/app/definition*
- *https://docs2.hubitat.com/en/developer/app/preferences*
