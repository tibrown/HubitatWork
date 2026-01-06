# Hubitat Capability Quick Reference

> **Fast lookup for capability commands and attributes**

## 📖 How to Use

1. **Find your capability** in the alphabetical list below
2. **See required commands and attributes**
3. **Implement all required items** in your driver

## Common Capabilities

### Switch
**Selector**: `capability.switch`  
**Definition**: `capability "Switch"`  
**Attributes**: `switch` - ENUM ["on", "off"]  
**Commands**:
- `on()`
- `off()`

### SwitchLevel
**Selector**: `capability.switchLevel`  
**Definition**: `capability "SwitchLevel"`  
**Attributes**: `level` - NUMBER (%, 0-100)  
**Commands**:
- `setLevel(level, duration)` - level (0-100), duration (seconds, optional)

### ColorControl
**Selector**: `capability.colorControl`  
**Definition**: `capability "ColorControl"`  
**Attributes**:
- `hue` - NUMBER (0-100)
- `saturation` - NUMBER (%, 0-100)
- `color` - STRING
- `colorName` - STRING
- `RGB` - STRING

**Commands**:
- `setColor(colormap)` - Map with `hue`, `saturation`, optional `level`
- `setHue(hue)` - NUMBER (0-100)
- `setSaturation(saturation)` - NUMBER (0-100)

### ColorTemperature
**Selector**: `capability.colorTemperature`  
**Definition**: `capability "ColorTemperature"`  
**Attributes**:
- `colorTemperature` - NUMBER (°K)
- `colorName` - STRING

**Commands**:
- `setColorTemperature(temp, level, transitionTime)` - temp (°K), level (0-100, optional), transitionTime (seconds, optional)

### MotionSensor
**Selector**: `capability.motionSensor`  
**Definition**: `capability "MotionSensor"`  
**Attributes**: `motion` - ENUM ["inactive", "active"]  
**Commands**: None

### ContactSensor
**Selector**: `capability.contactSensor`  
**Definition**: `capability "ContactSensor"`  
**Attributes**: `contact` - ENUM ["closed", "open"]  
**Commands**: None

### TemperatureMeasurement
**Selector**: `capability.temperatureMeasurement`  
**Definition**: `capability "TemperatureMeasurement"`  
**Attributes**: `temperature` - NUMBER (°F or °C)  
**Commands**: None

### RelativeHumidityMeasurement
**Selector**: `capability.relativeHumidityMeasurement`  
**Definition**: `capability "RelativeHumidityMeasurement"`  
**Attributes**: `humidity` - NUMBER (%rh)  
**Commands**: None

### Lock
**Selector**: `capability.lock`  
**Definition**: `capability "Lock"`  
**Attributes**: `lock` - ENUM ["locked", "unlocked with timeout", "unlocked", "unknown"]  
**Commands**:
- `lock()`
- `unlock()`

### Thermostat
**Selector**: `capability.thermostat`  
**Definition**: `capability "Thermostat"`  
**Attributes**:
- `temperature` - NUMBER (°F or °C)
- `heatingSetpoint` - NUMBER (°F or °C)
- `coolingSetpoint` - NUMBER (°F or °C)
- `thermostatSetpoint` - NUMBER (°F or °C)
- `thermostatMode` - ENUM ["auto", "off", "heat", "emergency heat", "cool"]
- `thermostatFanMode` - ENUM ["on", "circulate", "auto"]
- `thermostatOperatingState` - ENUM ["heating", "pending cool", "pending heat", "vent economizer", "idle", "cooling", "fan only"]
- `supportedThermostatModes` - JSON_OBJECT
- `supportedThermostatFanModes` - JSON_OBJECT

**Commands**:
- `auto()`, `cool()`, `emergencyHeat()`, `heat()`, `off()`
- `fanAuto()`, `fanCirculate()`, `fanOn()`
- `setCoolingSetpoint(temperature)`
- `setHeatingSetpoint(temperature)`
- `setThermostatMode(mode)`
- `setThermostatFanMode(fanmode)`

---

## Utility Capabilities

### Actuator
**Definition**: `capability "Actuator"`  
**Purpose**: Marker capability - device can be controlled  
**Attributes**: None  
**Commands**: None

### Sensor
**Definition**: `capability "Sensor"`  
**Purpose**: Marker capability - device reports sensor data  
**Attributes**: None  
**Commands**: None

### Refresh
**Definition**: `capability "Refresh"`  
**Attributes**: None  
**Commands**: `refresh()` - Request current state from device

### Configuration
**Definition**: `capability "Configuration"`  
**Attributes**: None  
**Commands**: `configure()` - Configure device parameters

### Initialize
**Definition**: `capability "Initialize"`  
**Purpose**: `initialize()` runs on hub startup  
**Attributes**: None  
**Commands**: `initialize()` - Called on hub start (for re-establishing connections)

---

## Full Capability List

### A-C

#### AccelerationSensor
**Attributes**: `acceleration` - ENUM ["inactive", "active"]  
**Commands**: None

#### AirQuality
**Attributes**: `airQualityIndex` - NUMBER (0-500)  
**Commands**: None

#### Alarm
**Attributes**: `alarm` - ENUM ["strobe", "off", "both", "siren"]  
**Commands**: `both()`, `off()`, `siren()`, `strobe()`

#### AudioNotification
**Commands**:
- `playText(text, volumelevel)`
- `playTrack(trackuri, volumelevel)`
- (and restore/resume variants)

#### AudioVolume
**Attributes**: `mute` - ENUM ["unmuted", "muted"], `volume` - NUMBER (%)  
**Commands**: `mute()`, `unmute()`, `volumeUp()`, `volumeDown()`, `setVolume(level)`

#### Battery
**Attributes**: `battery` - NUMBER (%)  
**Commands**: None

#### Beacon
**Attributes**: `presence` - ENUM ["not present", "present"]  
**Commands**: None

#### Bulb
**Attributes**: `switch` - ENUM ["on", "off"]  
**Commands**: `on()`, `off()`

#### CarbonDioxideMeasurement
**Attributes**: `carbonDioxide` - NUMBER (ppm)  
**Commands**: None

#### CarbonMonoxideDetector
**Attributes**: `carbonMonoxide` - ENUM ["clear", "tested", "detected"]  
**Commands**: None

#### ChangeLevel
**Commands**: `startLevelChange(direction)`, `stopLevelChange()`

#### Chime
**Attributes**: `soundEffects` - JSON_OBJECT, `soundName` - STRING, `status` - ENUM  
**Commands**: `playSound(soundnumber)`, `stop()`

#### ColorMode
**Attributes**: `colorMode` - ENUM ["CT", "RGB", "EFFECTS"]  
**Commands**: None

#### Consumable
**Attributes**: `consumableStatus` - ENUM  
**Commands**: `setConsumableStatus(status)`

#### CurrentMeter
**Attributes**: `amperage` - NUMBER (A)  
**Commands**: None

---

### D-H

#### DoorControl
**Attributes**: `door` - ENUM ["unknown", "closed", "open", "closing", "opening"]  
**Commands**: `close()`, `open()`

#### DoubleTapableButton
**Attributes**: `doubleTapped` - NUMBER  
**Commands**: `doubleTap(buttonNumber)`

#### EnergyMeter
**Attributes**: `energy` - NUMBER (kWh)  
**Commands**: None

#### FanControl
**Attributes**: `speed` - ENUM, `supportedFanSpeeds` - JSON_OBJECT  
**Commands**: `setSpeed(fanspeed)`, `cycleSpeed()`

#### FilterStatus
**Attributes**: `filterStatus` - ENUM ["normal", "replace"]  
**Commands**: None

#### Flash
**Commands**: `flash(rateToFlash)` - optional rate in ms

#### GarageDoorControl
**Attributes**: `door` - ENUM ["unknown", "open", "closing", "closed", "opening"]  
**Commands**: `close()`, `open()`

#### GasDetector
**Attributes**: `naturalGas` - ENUM ["clear", "tested", "detected"]  
**Commands**: None

#### HealthCheck
**Attributes**: `checkInterval` - NUMBER  
**Commands**: `ping()`

#### HoldableButton
**Attributes**: `held` - NUMBER  
**Commands**: `hold(buttonNumber)`

---

### I-L

#### IlluminanceMeasurement
**Attributes**: `illuminance` - NUMBER (lx)  
**Commands**: None

#### ImageCapture
**Attributes**: `image` - STRING  
**Commands**: `take()`

#### Indicator
**Attributes**: `indicatorStatus` - ENUM ["never", "when on", "when off"]  
**Commands**: `indicatorNever()`, `indicatorWhenOff()`, `indicatorWhenOn()`

#### Light
**Attributes**: `switch` - ENUM ["on", "off"]  
**Commands**: `on()`, `off()`

#### LightEffects
**Attributes**: `effectName` - STRING, `lightEffects` - JSON_OBJECT  
**Commands**: `setEffect(num)`, `setNextEffect()`, `setPreviousEffect()`

#### LiquidFlowRate
**Attributes**: `rate` - NUMBER (LPM or GPM)  
**Commands**: None

#### LocationMode
**Attributes**: `mode` - DYNAMIC_ENUM  
**Commands**: None

#### LockCodes
**Attributes**: `codeChanged` - ENUM, `codeLength` - NUMBER, `lockCodes` - JSON_OBJECT, `maxCodes` - NUMBER  
**Commands**: `deleteCode(position)`, `getCodes()`, `setCode(position, pin, name)`, `setCodeLength(length)`

---

### M-P

#### MediaController
**Attributes**: `activities` - JSON_OBJECT, `currentActivity` - STRING  
**Commands**: `getAllActivities()`, `getCurrentActivity()`, `startActivity(name)`

#### MediaInputSource
**Attributes**: `supportedInputs` - JSON_OBJECT, `mediaInputSource` - STRING  
**Commands**: `setInputSource(inputName)`

#### MediaTransport
**Attributes**: `transportStatus` - ENUM ["playing", "paused", "stopped"]  
**Commands**: `play()`, `pause()`, `stop()`

#### Momentary
**Commands**: `push()`

#### MusicPlayer
**Attributes**: `level`, `mute`, `status`, `trackData`, `trackDescription`  
**Commands**: `mute()`, `unmute()`, `play()`, `pause()`, `stop()`, `nextTrack()`, `previousTrack()`, `setLevel(level)`, etc.

#### Notification
**Commands**: `deviceNotification(text)`

#### Outlet
**Attributes**: `switch` - ENUM ["on", "off"]  
**Commands**: `on()`, `off()`

#### Polling
**Commands**: `poll()`

#### PowerMeter
**Attributes**: `power` - NUMBER (W)  
**Commands**: None

#### PowerSource
**Attributes**: `powerSource` - ENUM ["battery", "dc", "mains", "unknown"]  
**Commands**: None

#### PresenceSensor
**Attributes**: `presence` - ENUM ["present", "not present"]  
**Commands**: None

#### PressureMeasurement
**Attributes**: `pressure` - NUMBER (Pa or psi)  
**Commands**: None

#### PushableButton
**Attributes**: `numberOfButtons` - NUMBER, `pushed` - NUMBER  
**Commands**: `push(buttonNumber)`

---

### R-S

#### RelaySwitch
**Attributes**: `switch` - ENUM ["on", "off"]  
**Commands**: `on()`, `off()`

#### ReleasableButton
**Attributes**: `released` - NUMBER  
**Commands**: `release(buttonNumber)`

#### SecurityKeypad
**Attributes**: `securityKeypad` - ENUM, `codeChanged`, `codeLength`, `lockCodes`, `maxCodes`  
**Commands**: `armAway()`, `armHome()`, `disarm()`, `deleteCode()`, `setCode()`, `setCodeLength()`, `setEntryDelay()`, `setExitDelay()`

#### ShockSensor
**Attributes**: `shock` - ENUM ["clear", "detected"]  
**Commands**: None

#### SignalStrength
**Attributes**: `lqi` - NUMBER, `rssi` - NUMBER  
**Commands**: None

#### SleepSensor
**Attributes**: `sleeping` - ENUM ["not sleeping", "sleeping"]  
**Commands**: None

#### SmokeDetector
**Attributes**: `smoke` - ENUM ["clear", "tested", "detected"]  
**Commands**: None

#### SoundPressureLevel
**Attributes**: `soundPressureLevel` - NUMBER (dB)  
**Commands**: None

#### SoundSensor
**Attributes**: `sound` - ENUM ["detected", "not detected"]  
**Commands**: None

#### SpeechRecognition
**Attributes**: `phraseSpoken` - STRING  
**Commands**: None

#### SpeechSynthesis
**Commands**: `speak(text, volume, voice)` - voice is AWS Polly voice name

#### StepSensor
**Attributes**: `goal` - NUMBER, `steps` - NUMBER  
**Commands**: None

---

### T-Z

#### TamperAlert
**Attributes**: `tamper` - ENUM ["clear", "detected"]  
**Commands**: None

#### Telnet
**Attributes**: `networkStatus` - ENUM ["online", "offline"]  
**Commands**: `sendMsg(message)`

#### ThermostatCoolingSetpoint
**Attributes**: `coolingSetpoint` - NUMBER (°F or °C)  
**Commands**: `setCoolingSetpoint(temperature)`

#### ThermostatHeatingSetpoint
**Attributes**: `heatingSetpoint` - NUMBER (°F or °C)  
**Commands**: `setHeatingSetpoint(temperature)`

#### ThermostatFanMode
**Attributes**: `thermostatFanMode` - ENUM ["auto", "circulate", "on"]  
**Commands**: `fanAuto()`, `fanCirculate()`, `fanOn()`, `setThermostatFanMode(mode)`

#### ThermostatMode
**Attributes**: `thermostatMode` - ENUM ["heat", "cool", "emergency heat", "auto", "off"]  
**Commands**: `auto()`, `cool()`, `emergencyHeat()`, `heat()`, `off()`, `setThermostatMode(mode)`

#### ThermostatOperatingState
**Attributes**: `thermostatOperatingState` - ENUM  
**Commands**: None

#### ThreeAxis
**Attributes**: `threeAxis` - VECTOR3  
**Commands**: None

#### TimedSession
**Attributes**: `sessionStatus` - ENUM, `timeRemaining` - NUMBER  
**Commands**: `cancel()`, `pause()`, `start()`, `stop()`, `setTimeRemaining(number)`

#### Tone
**Commands**: `beep()`

#### TouchSensor
**Attributes**: `touch` - ENUM ["touched"]  
**Commands**: None

#### UltravioletIndex
**Attributes**: `ultravioletIndex` - NUMBER  
**Commands**: None

#### Valve
**Attributes**: `valve` - ENUM ["open", "closed"]  
**Commands**: `close()`, `open()`

#### Variable
**Attributes**: `variable` - STRING  
**Commands**: `setVariable(valueToSet)`

#### VideoCamera
**Attributes**: `camera` - ENUM, `mute` - ENUM, `settings` - JSON_OBJECT, `statusMessage` - STRING  
**Commands**: `flip()`, `mute()`, `unmute()`, `on()`, `off()`

#### VideoCapture
**Attributes**: `clip` - JSON_OBJECT  
**Commands**: `capture(date, date, date)`

#### VoltageMeasurement
**Attributes**: `voltage` - NUMBER (V), `frequency` - NUMBER (Hz)  
**Commands**: None

#### WaterSensor
**Attributes**: `water` - ENUM ["wet", "dry"]  
**Commands**: None

#### WindowBlind
**Attributes**: `position` - NUMBER (%), `windowBlind` - ENUM, `tilt` - NUMBER (%)  
**Commands**: `close()`, `open()`, `setPosition(position)`, `setTiltLevel(tilt)`, `startPositionChange(direction)`, `stopPositionChange()`

#### WindowShade
**Attributes**: `position` - NUMBER (%), `windowShade` - ENUM  
**Commands**: `close()`, `open()`, `setPosition(position)`, `startPositionChange(direction)`, `stopPositionChange()`

#### ZwMultichannel
**Attributes**: `epEvent` - STRING, `epInfo` - STRING  
**Commands**: `enableEpEvents(string)`, `epCmd(number, string)`

#### pHMeasurement
**Attributes**: `pH` - NUMBER  
**Commands**: None

---

## 💡 Usage Tips

### In Device Selector (Apps)
```groovy
input "myDevice", "capability.switch", title: "Select Switch"
input "sensors", "capability.temperatureMeasurement", title: "Temp Sensors", multiple: true
```

### In Driver Definition
```groovy
metadata {
    definition(...) {
        capability "Switch"
        capability "SwitchLevel"
        capability "ColorControl"
    }
}
```

### Implementing Commands
```groovy
// Must implement ALL commands from capabilities
def on() {
    // Your implementation
    sendEvent(name: "switch", value: "on")
}

def off() {
    // Your implementation
    sendEvent(name: "switch", value: "off")
}

def setLevel(level, duration=0) {
    // Your implementation
    sendEvent(name: "level", value: level, unit: "%")
}
```

---

## 📚 Related Documentation

- [Driver Overview](../02-Drivers/Driver-Overview.md)
- [App Overview](../03-Apps/App-Overview.md)
- [Best Practices](../07-Best-Practices/Best-Practices.md)
- [Official Capability List](https://docs2.hubitat.com/en/developer/driver/capability-list)

---

*Source: https://docs2.hubitat.com/en/developer/driver/capability-list*  
*Last updated: October 12, 2025*
