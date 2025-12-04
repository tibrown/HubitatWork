# Hub Variables & Connector Switches Reference

This document provides a complete reference of all hub variables and connector switches used across the HubitatWork system. Hub variables should be created in **Settings → Hub Variables**, and connector switches should be created as **Connector** type devices.

---

## 📋 Table of Contents

1. [Connector Switches (Required)](#connector-switches-required)
2. [Hub Variables - Configuration](#hub-variables---configuration)
3. [Hub Variables - Status/State](#hub-variables---statusstate)
4. [Hub Variables - Messaging](#hub-variables---messaging)
5. [Quick Setup Checklist](#quick-setup-checklist)

---

## Connector Switches (Required)

These must be created as **Connector Switch** devices (namespace: "hubitat") in Devices. Connector switches are backed by hub variables and provide event-based triggering.

### Alarm Control
| Switch Name | Type | Purpose | Used By |
|------------|------|---------|---------|
| `AlarmsEnabled` | Connector | Master alarm enable/disable | SecurityAlarmManager, NightSecurityManager |
| `AudibleAlarmsOn` | Connector | Control audible alarm state | SecurityAlarmManager |
| `Silent` | Connector | Silent mode switch | SecurityAlarmManager, NightSecurityManager, EmergencyHelpManager |
| `AlarmTrigger` | Connector | Triggers alarm execution (auto-reset) | NightSecurityManager → SecurityAlarmManager |
| `AlarmStop` | Connector | Stops all alarms (auto-reset) | NightSecurityManager → SecurityAlarmManager |
| `PanicButton` | Connector | Panic alert trigger | SecurityAlarmManager |

### Lighting Control
| Switch Name | Type | Purpose | Used By |
|------------|------|---------|---------|
| `EmergencyLightsOn` | Connector | Emergency lighting activation | NightSecurityManager → LightsAutomationManager |
| `AllLightsControl` | Connector | Master light control | LightsAutomationManager |
| `NightLights` | Connector | Night mode lighting | LightsAutomationManager |

### Security & Presence
| Switch Name | Type | Purpose | Used By |
|------------|------|---------|---------|------|
| `Traveling` | Connector | Travel mode indicator | NightSecurityManager, DoorWindowMonitor |
| `SilentCarport` | Connector | Silent carport mode | LightsAutomationManager |
| `PauseCarportBeam` | Connector | Pause carport beam alerts | LightsAutomationManager |
| `PauseDRDoorAlarm` | Connector | Pause dining room door alarm | DoorWindowMonitor, NightSecurityManager |
| `PauseBDAlarm` | Connector | Pause backdoor alarm | DoorWindowMonitor, NightSecurityManager |
| `RearGateActive` | Connector | Rear gate active indicator | NightSecurityManager |
| `ArriveGracePeriodSwitch` | Connector | Arrival grace period active | MotionPresenceManager |

### Status Indicators
| Switch Name | Type | Purpose | Used By |
|------------|------|---------|---------|------|
| `Holiday` | Connector | Holiday mode indicator | LightsAutomationManager |
| `OnPTO` | Connector | PTO/vacation mode | LightsAutomationManager |
| `HighAlert` | Connector | High alert security mode | NightSecurityManager |
| `ChristmasTrees` | Connector | Christmas decorations status | ChristmasTreesControl |
| `SummerTime` | Connector | Summer mode indicator | System-wide |

### Emergency & Help
| Switch Name | Type | Purpose | Used By |
|------------|------|---------|---------|------|
| `StopShowerHelpSwitch` | Connector | Stop shower help alert | EmergencyHelpManager |
| `IndoorCamsSwitch` | Connector | Indoor cameras control | SecurityAlarmManager, CameraPrivacyManager |

---

## Hub Variables - Configuration

These hub variables override default app settings and provide flexible configuration without redeploying apps.

### SecurityAlarmManager
| Variable Name | Type | Default | Purpose |
|--------------|------|---------|---------|
| `AlarmVolume` | Number | 80 | Siren volume (0-100) |
| `AlarmDuration` | Number | 300 | Alarm sound duration (seconds) |
| `ArmDelay` | Number | 0 | Arm delay timer (seconds) |
| `DisarmDelay` | Number | 0 | Disarm delay timer (seconds) |

### NightSecurityManager
| Variable Name | Type | Default | Purpose |
|--------------|------|---------|---------|
| `NightStartTime` | String | "22:30" | Night mode start time (HH:mm) |
| `NightEndTime` | String | "06:00" | Night mode end time (HH:mm) |
| `AlertDelay` | Number | 5 | Delay before alerting (seconds) |
| `MotionTimeout` | Number | 60 | Motion sensor timeout (seconds) |
| `BeamLogEnabled` | Boolean | true | Enable/disable detailed beam logging |

### LightsAutomationManager
| Variable Name | Type | Default | Purpose |
|--------------|------|---------|---------|
| `MorningTime` | String | - | Morning light activation time (HH:mm) |
| `EveningTime` | String | - | Evening light activation time (HH:mm) |
| `NightTime` | String | - | Night mode light time (HH:mm) |
| `DeskBrightness` | Number | 100 | Desk light brightness (0-100) |
| `FloodTimeout` | Number | 5 | Motion flood timeout (minutes) |
| `StripColorDay` | String | "Soft White" | Daytime strip color |
| `StripColorNight` | String | "Blue" | Nighttime strip color |
| `BeamLightDelay` | Number | 300 | Carport beam light delay (seconds) |

### EnvironmentalControlManager
| Variable Name | Type | Default | Purpose |
|--------------|------|---------|---------|
| `GreenhouseFanOnTemp` | Decimal | 75.0 | Fan on temperature (°F) |
| `GreenhouseFanOffTemp` | Decimal | 70.0 | Fan off temperature (°F) |
| `GreenhouseHeaterOnTemp` | Decimal | 40.0 | Heater on temperature (°F) |
| `GreenhouseHeaterOffTemp` | Decimal | 45.0 | Heater off temperature (°F) |
| `FreezeAlertThreshold` | Decimal | 32.0 | Freeze warning temperature (°F) |
| `OfficeHeaterTemp` | Decimal | 68.0 | Office heater temperature (°F) |
| `SkeeterOnTime` | String | - | Skeeter on time (HH:mm) |
| `SkeeterOffTime` | String | - | Skeeter off time (HH:mm) |
| `WaterTimeout` | Number | 30 | Water shutoff timeout (minutes) |

### DoorWindowMonitor
| Variable Name | Type | Default | Purpose |
|--------------|------|---------|---------|
| `DoorOpenThreshold` | Number | 5 | Door left open alert (minutes) |
| `WindowOpenThreshold` | Number | 10 | Window left open alert (minutes) |
| `FreezerDoorThreshold` | Number | 2 | Freezer door open alert (minutes) |
| `PauseDuration` | Number | 5 | Alarm pause duration (minutes) |
| `CheckInterval` | Number | 1 | Periodic check interval (minutes) |
| `TamperAlertEnabled` | Boolean | true | Enable/disable tamper detection |

### PerimeterSecurityManager
| Variable Name | Type | Default | Purpose |
|--------------|------|---------|---------|
| `GateAlertDelay` | Number | 30 | Gate alert delay (seconds) |
| `ShockSensitivity` | Number | 5 | Shock sensitivity (1-10) |
| `PerimeterCheckInterval` | Number | 0 | Check interval (minutes, 0=disabled) |
| `AwayModeAlertEnabled` | Boolean | true | Enable/disable away alerts |
| `RingPersonTimeout` | Number | 30 | Ring timeout (seconds) |
| `GunCabinetAlertEnabled` | Boolean | true | Enable/disable cabinet alerts |

### EmergencyHelpManager
| Variable Name | Type | Default | Purpose |
|--------------|------|---------|---------|
| `HelpAlertDuration` | Number | 300 | Help alert duration (seconds) |
| `FlashRate` | Number | 2 | Light flash rate (flashes/second) |
| `EmergencyVolume` | Number | 100 | Emergency siren volume (0-100) |
| `SilentModeTimeout` | Number | - | Silent mode timeout (minutes) |
| `NotificationDelay` | Number | 0 | Notification delay (seconds) |
| `VisualOnlyMode` | Boolean | false | Enable visual-only alerts |

### MotionPresenceManager
| Variable Name | Type | Default | Purpose |
|--------------|------|---------|---------|
| `GracePeriodDuration` | Number | 30 | Grace period duration (minutes) |
| `MotionTimeout` | Number | 60 | Motion sensor timeout (seconds) |
| `EnableDayMotion` | Boolean | true | Enable/disable day motion detection |
| `EnableNightMotion` | Boolean | false | Enable/disable night motion detection |
| `ArrivalNotifications` | Boolean | true | Enable/disable arrival notifications |

### CameraPrivacyManager
| Variable Name | Type | Default | Purpose |
|--------------|------|---------|---------|
| `PrivacyModeDelay` | Number | 2 | Camera off delay (minutes) |
| `EnableDelay` | Number | 1 | Camera on delay (minutes) |
| `ManualOverrideDuration` | Number | 4 | Manual override timeout (hours) |

---

## Hub Variables - Status/State

These hub variables are written by apps to track system state and read by other apps.

| Variable Name | Type | Written By | Read By | Purpose |
|--------------|------|------------|---------|---------|
| `AlarmActive` | Boolean | SecurityAlarmManager | NightSecurityManager | Tracks if alarms are currently active |
| `AlarmsEnabled` | Boolean | SecurityAlarmManager | NightSecurityManager | Master alarm status |

---

## Hub Variables - Messaging

These hub variables are used for cross-app communication and message passing.

| Variable Name | Type | Purpose | Used By |
|--------------|------|---------|---------|
| `EchoMessage` | String | Message for Alexa TTS | NightSecurityManager, EmergencyHelpManager |
| `AlertMessage` | String | General alert message | NightSecurityManager |

---

## Quick Setup Checklist

### Step 1: Create Hub Variables
Go to **Settings → Hub Variables** and create these variables:

#### Configuration Variables (most commonly used)
- [ ] `AlarmVolume` (Number) - Default: 80
- [ ] `AlarmDuration` (Number) - Default: 300
- [ ] `AlertDelay` (Number) - Default: 5
- [ ] `DeskBrightness` (Number) - Default: 100
- [ ] `FloodTimeout` (Number) - Default: 5
- [ ] `DoorOpenThreshold` (Number) - Default: 5
- [ ] `WindowOpenThreshold` (Number) - Default: 10
- [ ] `FreezerDoorThreshold` (Number) - Default: 2

#### Status Variables
- [ ] `AlarmActive` (String) - Default: "false"
- [ ] `AlarmsEnabled` (String) - Default: "false"

#### Message Variables
- [ ] `EchoMessage` (String) - Default: ""
- [ ] `AlertMessage` (String) - Default: ""

### Step 2: Create Connector Switches
Go to **Devices → Add Device → Virtual** and select **Connector** type:

#### Critical Switches (Must Have)
- [ ] `AlarmsEnabled` (Connector)
- [ ] `AudibleAlarmsOn` (Connector)
- [ ] `Silent` (Connector)
- [ ] `AlarmTrigger` (Connector)
- [ ] `AlarmStop` (Connector)

#### Lighting Switches
- [ ] `EmergencyLightsOn` (Connector)
- [ ] `AllLightsControl` (Connector)
- [ ] `NightLights` (Connector)

#### Security Switches
- [ ] `Traveling` (Connector)
- [ ] `PauseDRDoorAlarm` (Connector)
- [ ] `PauseBDAlarm` (Connector)
- [ ] `HighAlert` (Connector)

#### Status Switches
- [ ] `Holiday` (Connector)
- [ ] `OnPTO` (Connector)

### Step 3: Verify Setup
1. Check that all connector switches appear in **Settings → Hub Variables**
2. Verify each connector switch can be controlled from the device page
3. Confirm hub variables have appropriate default values

---

## Alphabetical Reference

### All Connector Switches (Alphabetical)

| Switch Name | Type | Purpose | Used By |
|------------|------|---------|---------|
| `AlarmStop` | Connector | Stops all alarms (auto-reset) | NightSecurityManager → SecurityAlarmManager |
| `AlarmTrigger` | Connector | Triggers alarm execution (auto-reset) | NightSecurityManager → SecurityAlarmManager |
| `AlarmsEnabled` | Connector | Master alarm enable/disable | SecurityAlarmManager, NightSecurityManager |
| `AllLightsControl` | Connector | Master light control | LightsAutomationManager |
| `ArriveGracePeriodSwitch` | Connector | Arrival grace period active | MotionPresenceManager |
| `AudibleAlarmsOn` | Connector | Control audible alarm state | SecurityAlarmManager |
| `ChristmasTrees` | Connector | Christmas decorations status | ChristmasTreesControl |
| `EmergencyLightsOn` | Connector | Emergency lighting activation | NightSecurityManager → LightsAutomationManager |
| `HighAlert` | Connector | High alert security mode | NightSecurityManager |
| `Holiday` | Connector | Holiday mode indicator | LightsAutomationManager |
| `IndoorCamsSwitch` | Connector | Indoor cameras control | SecurityAlarmManager, CameraPrivacyManager |
| `NightLights` | Connector | Night mode lighting | LightsAutomationManager |
| `OnPTO` | Connector | PTO/vacation mode | LightsAutomationManager |
| `PanicButton` | Connector | Panic alert trigger | SecurityAlarmManager |
| `PauseBDAlarm` | Connector | Pause backdoor alarm | DoorWindowMonitor, NightSecurityManager |
| `PauseCarportBeam` | Connector | Pause carport beam alerts | LightsAutomationManager |
| `PauseDRDoorAlarm` | Connector | Pause dining room door alarm | DoorWindowMonitor, NightSecurityManager |
| `RearGateActive` | Connector | Rear gate active indicator | NightSecurityManager |
| `Silent` | Connector | Silent mode switch | SecurityAlarmManager, NightSecurityManager, EmergencyHelpManager |
| `SilentCarport` | Connector | Silent carport mode | LightsAutomationManager |
| `StopShowerHelpSwitch` | Connector | Stop shower help alert | EmergencyHelpManager |
| `SummerTime` | Connector | Summer mode indicator | System-wide |
| `Traveling` | Connector | Travel mode indicator | NightSecurityManager, DoorWindowMonitor |

### All Hub Variables (Alphabetical)

| Variable Name | Type | Default | Purpose | Used By |
|--------------|------|---------|---------|---------|
| `AlertDelay` | Number | 5 | Delay before alerting (seconds) | NightSecurityManager |
| `AlertMessage` | String | "" | General alert message | NightSecurityManager |
| `AlarmActive` | Boolean | false | Tracks if alarms are currently active | SecurityAlarmManager → NightSecurityManager |
| `AlarmDuration` | Number | 300 | Alarm sound duration (seconds) | SecurityAlarmManager |
| `AlarmsEnabled` | Boolean | false | Master alarm status | SecurityAlarmManager → NightSecurityManager |
| `AlarmVolume` | Number | 80 | Siren volume (0-100) | SecurityAlarmManager |
| `ArmDelay` | Number | 0 | Arm delay timer (seconds) | SecurityAlarmManager |
| `ArrivalNotifications` | Boolean | true | Enable/disable arrival notifications | MotionPresenceManager |
| `AwayModeAlertEnabled` | Boolean | true | Enable/disable away alerts | PerimeterSecurityManager |
| `BeamLightDelay` | Number | 300 | Carport beam light delay (seconds) | LightsAutomationManager |
| `BeamLogEnabled` | Boolean | true | Enable/disable detailed beam logging | NightSecurityManager |
| `CheckInterval` | Number | 1 | Periodic check interval (minutes) | DoorWindowMonitor |
| `DeskBrightness` | Number | 100 | Desk light brightness (0-100) | LightsAutomationManager |
| `DisarmDelay` | Number | 0 | Disarm delay timer (seconds) | SecurityAlarmManager |
| `DoorOpenThreshold` | Number | 5 | Door left open alert (minutes) | DoorWindowMonitor |
| `EchoMessage` | String | "" | Message for Alexa TTS | NightSecurityManager, EmergencyHelpManager |
| `EmergencyVolume` | Number | 100 | Emergency siren volume (0-100) | EmergencyHelpManager |
| `EnableDayMotion` | Boolean | true | Enable/disable day motion detection | MotionPresenceManager |
| `EnableDelay` | Number | 1 | Camera on delay (minutes) | CameraPrivacyManager |
| `EnableNightMotion` | Boolean | false | Enable/disable night motion detection | MotionPresenceManager |
| `EveningTime` | String | - | Evening light activation time (HH:mm) | LightsAutomationManager |
| `FlashRate` | Number | 2 | Light flash rate (flashes/second) | EmergencyHelpManager |
| `FloodTimeout` | Number | 5 | Motion flood timeout (minutes) | LightsAutomationManager |
| `FreezeAlertThreshold` | Decimal | 32.0 | Freeze warning temperature (°F) | EnvironmentalControlManager |
| `FreezerDoorThreshold` | Number | 2 | Freezer door open alert (minutes) | DoorWindowMonitor |
| `GateAlertDelay` | Number | 30 | Gate alert delay (seconds) | PerimeterSecurityManager |
| `GracePeriodDuration` | Number | 30 | Grace period duration (minutes) | MotionPresenceManager |
| `GreenhouseFanOffTemp` | Decimal | 70.0 | Fan off temperature (°F) | EnvironmentalControlManager |
| `GreenhouseFanOnTemp` | Decimal | 75.0 | Fan on temperature (°F) | EnvironmentalControlManager |
| `GreenhouseHeaterOffTemp` | Decimal | 45.0 | Heater off temperature (°F) | EnvironmentalControlManager |
| `GreenhouseHeaterOnTemp` | Decimal | 40.0 | Heater on temperature (°F) | EnvironmentalControlManager |
| `GunCabinetAlertEnabled` | Boolean | true | Enable/disable cabinet alerts | PerimeterSecurityManager |
| `HelpAlertDuration` | Number | 300 | Help alert duration (seconds) | EmergencyHelpManager |
| `ManualOverrideDuration` | Number | 4 | Manual override timeout (hours) | CameraPrivacyManager |
| `MorningTime` | String | - | Morning light activation time (HH:mm) | LightsAutomationManager |
| `MotionTimeout` | Number | 60 | Motion sensor timeout (seconds) | NightSecurityManager, MotionPresenceManager |
| `NightEndTime` | String | "06:00" | Night mode end time (HH:mm) | NightSecurityManager |
| `NightStartTime` | String | "22:30" | Night mode start time (HH:mm) | NightSecurityManager |
| `NightTime` | String | - | Night mode light time (HH:mm) | LightsAutomationManager |
| `NotificationDelay` | Number | 0 | Notification delay (seconds) | EmergencyHelpManager |
| `OfficeHeaterTemp` | Decimal | 68.0 | Office heater temperature (°F) | EnvironmentalControlManager |
| `PauseDuration` | Number | 5 | Alarm pause duration (minutes) | DoorWindowMonitor |
| `PerimeterCheckInterval` | Number | 0 | Check interval (minutes, 0=disabled) | PerimeterSecurityManager |
| `PrivacyModeDelay` | Number | 2 | Camera off delay (minutes) | CameraPrivacyManager |
| `RingPersonTimeout` | Number | 30 | Ring timeout (seconds) | PerimeterSecurityManager |
| `ShockSensitivity` | Number | 5 | Shock sensitivity (1-10) | PerimeterSecurityManager |
| `SilentModeTimeout` | Number | - | Silent mode timeout (minutes) | EmergencyHelpManager |
| `SkeeterOffTime` | String | - | Skeeter off time (HH:mm) | EnvironmentalControlManager |
| `SkeeterOnTime` | String | - | Skeeter on time (HH:mm) | EnvironmentalControlManager |
| `StripColorDay` | String | "Soft White" | Daytime strip color | LightsAutomationManager |
| `StripColorNight` | String | "Blue" | Nighttime strip color | LightsAutomationManager |
| `TamperAlertEnabled` | Boolean | true | Enable/disable tamper detection | DoorWindowMonitor |
| `VisualOnlyMode` | Boolean | false | Enable visual-only alerts | EmergencyHelpManager |
| `WaterTimeout` | Number | 30 | Water shutoff timeout (minutes) | EnvironmentalControlManager |
| `WindowOpenThreshold` | Number | 10 | Window left open alert (minutes) | DoorWindowMonitor |

---

## Notes

### Why Connector Switches?

**Connector Switches** are preferred over standard Virtual Switches because:

1. **Centralized Management** - All state is visible in Settings → Hub Variables
2. **Event Generation** - Connector switches can trigger subscriptions (hub variables alone cannot)
3. **Persistence** - State persists across hub reboots
4. **Dashboard Integration** - Easy to add to dashboards
5. **Debugging** - Easier to monitor and troubleshoot system state

### Hub Variable Types

- **String** - Text values, JSON, boolean as "true"/"false"
- **Number** - Integer values (whole numbers)
- **Decimal** - Floating point values (e.g., temperatures)
- **Boolean** - Not used directly; use String "true"/"false" instead

### Best Practices

1. **Always use Connector Switches** for cross-app communication that needs to trigger events
2. **Use Hub Variables directly** for configuration values and state tracking
3. **Document defaults** - Each app should have sensible defaults if hub variables aren't set
4. **Test fallbacks** - Ensure apps work even if hub variables don't exist
5. **Use consistent naming** - Follow the naming convention in this document

---

## Maintenance

This document should be updated whenever:
- New hub variables are added to apps
- New connector switches are required
- Default values change
- New apps are added to the system

**Last Updated:** December 4, 2025
