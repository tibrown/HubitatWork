# Security Alarm Manager

## Overview
Security Alarm Manager is a comprehensive Hubitat app that centralizes all security alarm, siren, and alert functionality in your smart home. It replaces 19 individual Rule Machine rules with a single, efficient, and maintainable application.

## Purpose
- Centralize alarm arming/disarming
- Manage multiple siren sound patterns
- Provide button-based alarm control
- Enable cross-app alarm triggering
- Integrate with hub modes for automatic enable/disable
- Support hub variables for flexible configuration

## Features
- **Multiple Siren Support**: Control up to 3 sirens simultaneously
- **34+ Sound Patterns**: Doorbell, dog bark, bear alert, train horn, panic, and more
- **Mode-Based Automation**: Automatically arm/disarm based on hub modes
- **Button Controls**: Physical button support for alarm on/off
- **Cross-App Communication**: Virtual switches for triggering from other apps
- **Hub Variable Support**: Override settings with hub variables for dynamic configuration
- **Auto-Stop Timer**: Configurable alarm duration with automatic shutoff
- **Silent Mode**: Support for visual-only alerts
- **Camera Integration**: Automatically activate indoor cameras during alarms

## Installation

### Prerequisites
1. **Hubitat Hub** running firmware 2.3.0 or later
2. **Siren Devices**: At least one compatible siren (Zooz S2 Multisiren recommended)
3. **Virtual Switches**: Create the following switches in Hubitat:
   - `AlarmsEnabled` (Connector Switch - recommended)
   - `AudibleAlarmsOn` (Connector Switch - recommended)
   - `Silent` (Connector Switch - recommended, optional)

### Optional Virtual Switches for Cross-App Communication
These are only needed if you want other apps to trigger alarms:
- `AlarmTrigger` (Virtual Switch or Connector Switch)
- `AlarmStop` (Virtual Switch or Connector Switch)
- `PanicButton` (Virtual Switch or Connector Switch)

### Installation Steps
1. Open Hubitat web interface
2. Navigate to **Apps Code**
3. Click **New App**
4. Copy and paste the contents of `SecurityAlarmManager.groovy`
5. Click **Save**
6. Navigate to **Apps**
7. Click **Add User App**
8. Select **Security Alarm Manager**
9. Configure the app (see Configuration section below)

## Configuration

### Required Configuration

#### Siren Devices
- **Siren 1 (Office)**: Primary siren device
- **Siren 2**: Secondary siren device
- **Siren 3 (Chime)**: Tertiary siren/chime device
- **Alarm Plug Switch**: Optional switch to control additional alarm devices

#### Control Switches
- **Alarms Enabled Switch**: Master switch to enable/disable alarm system
- **Audible Alarms On Switch**: Controls whether alarms make sound or are silent
- **Silent Mode Switch**: Optional switch for silent mode

### Optional Configuration

#### Control Buttons
- **Alarm Off Button**: Physical button to disable alarms
- **Alarm Off Backup Button**: Backup button to disable alarms
- **Alarm On Button**: Physical button to enable alarms

#### Cross-App Communication
- **Alarm Trigger Switch**: Virtual switch that other apps can turn on to trigger alarms
- **Alarm Stop Switch**: Virtual switch that other apps can turn on to stop alarms
- **Panic Button Switch**: Virtual switch for panic alert activation

#### Camera Integration
- **Indoor Cameras Switch**: Switch to activate indoor cameras during alarm events

#### Alarm Configuration
- **Default Alarm Volume**: Volume level for siren sounds (0-100, default: 80)
- **Alarm Auto-Stop Duration**: Seconds before alarm auto-stops (default: 300 = 5 minutes)
- **Arm Delay**: Delay in seconds before arming alarms (default: 0)
- **Disarm Delay**: Delay in seconds before disarming alarms (default: 0)

#### Mode Configuration
- **Modes that enable alarms**: Select which hub modes should automatically arm the alarm system

#### Notifications
- **Notification Devices**: Devices to receive alarm status notifications

## Hub Variables

The app supports the following hub variables for dynamic configuration. These override the standard settings when present:

### Configuration Variables (Read by App)
- `alarmVolume` (Number): Override default siren volume (0-100)
- `alarmDuration` (Number): Override alarm sound duration in seconds
- `armDelay` (Number): Override arm delay timer in seconds
- `disarmDelay` (Number): Override disarm delay in seconds

### Status Variables (Written by App)
- `AlarmActive` (String): Current alarm state ("true" or "false") - Read by other apps
- `AlarmsEnabled` (String): Alarm system enabled state ("true" or "false") - Read by other apps

### Creating Hub Variables
1. Navigate to **Settings → Hub Variables**
2. Click **Create New Variable**
3. Set variable name (e.g., `alarmVolume`)
4. Set variable type (String, Number, Boolean, etc.)
5. Set initial value
6. Click **Create Variable**

## Usage

### Arming/Disarming Alarms

#### Manual Control
- Press the configured **Alarm On Button** to arm
- Press the configured **Alarm Off Button** to disarm
- Toggle the **Alarms Enabled Switch** on/off

#### Automatic Control (Mode-Based)
When configured with mode-based automation, the app will automatically:
- **Arm** when hub mode changes to a selected "alarm-enabled" mode
- **Disarm** when hub mode changes to any other mode

### Triggering Alarms

#### From Other Apps
Other apps can trigger alarms by turning on the `AlarmTrigger` virtual switch:
```groovy
// In another app
def alarmSwitch = getDeviceByName("AlarmTrigger")
alarmSwitch?.on()
```

The Security Alarm Manager will:
1. Detect the switch activation
2. Execute the alarm sequence
3. Automatically reset the trigger switch after 2 seconds

#### Direct Execution
The app executes alarms when:
- The `AlarmTrigger` switch is turned on
- The `executeAlarms()` method is called internally

### Alarm Sequence
When alarms execute:
1. Checks if alarms are enabled
2. Activates indoor cameras (if configured)
3. Plays alarm sound (if audible mode is on)
4. Activates siren strobe lights
5. Sets `AlarmActive` hub variable to "true"
6. Schedules auto-stop after configured duration

### Stopping Alarms
Alarms can be stopped by:
- Turning off the `AlarmsEnabled` switch
- Pressing the **Alarm Off Button**
- Turning on the `AlarmStop` virtual switch
- Waiting for auto-stop timeout

### Sound Patterns
The app includes methods for various sound patterns:
- **Doorbell** - Standard doorbell chime
- **Dog Bark** - Single dog bark (repeats 4 times)
- **Dogs Barking** - Multiple dogs barking
- **Bear Alert** - Bear deterrent sound
- **Panic Alert** - Emergency panic sound
- **Siren** - Standard siren sound
- **Train Horn** - Train horn sound
- **Tuning Bell** - Tuning bell chime

## Rules Replaced
This app replaces the following 19 Rule Machine rules:
1. A-Alarm02
2. A-AlarmIsArmed
3. A-AlarmIsDisarmed
4. A-Bear
5. A-DogBark6
6. A-DogsBarking
7. A-DoorBell
8. A-Doorbell-SO-25
9. A-PanicAlert
10. A-Siren03
11. A-SirenSound
12. A-TrainHorn
13. A-TuningBell
14. AlarmDisableOnMode
15. AlarmEnableOnMode
16. AlarmOffFromButton
17. AlarmsOnFromButton
18. ExecuteAlarms
19. StopAlarms

## Example Configurations

### Basic Security System
**Scenario**: Simple home security with one siren and mode-based arming

**Configuration**:
- Siren 1: Main siren device
- Siren 2: (same as Siren 1 if only one device)
- Siren 3: (same as Siren 1 if only one device)
- Modes that enable alarms: "Away", "Night"
- Default Alarm Volume: 80
- Alarm Auto-Stop Duration: 300 (5 minutes)

### Advanced Multi-Zone System
**Scenario**: Multiple sirens with button controls and cross-app integration

**Configuration**:
- Siren 1: Office siren
- Siren 2: Bedroom siren
- Siren 3: Outdoor chime
- Alarm Off Button: Bedroom button
- Alarm Off Backup Button: Office button
- Alarm Trigger Switch: Virtual switch for NightSecurityManager
- Modes that enable alarms: "Away", "Night", "Vacation"
- Hub Variable Overrides:
  - `alarmVolume` = 90 (louder during away mode)
  - `alarmDuration` = 600 (10 minutes)

## Troubleshooting

### Alarms Don't Sound
1. Check that **AlarmsEnabled** switch is ON
2. Check that **AudibleAlarmsOn** switch is ON
3. Verify siren devices are responsive
4. Check alarm volume setting (should be > 0)
5. Review logs for error messages

### Alarms Won't Stop
1. Press **Alarm Off Button**
2. Turn off **AlarmsEnabled** switch
3. Check hub variables - ensure `AlarmActive` isn't stuck
4. Restart the app (pause/resume)

### Mode-Based Arming Not Working
1. Verify **Modes that enable alarms** is configured
2. Check current hub mode
3. Review mode change logs
4. Ensure no delays are blocking the action

### Cross-App Communication Not Working
1. Verify virtual switches are created and selected
2. Check that other apps are turning switches ON (not just toggling)
3. Review logs to see if switch events are being received
4. Ensure switches auto-reset after 2 seconds

### Hub Variable Override Not Working
1. Verify hub variable name matches exactly (case-sensitive)
2. Check hub variable type (should be appropriate for value)
3. Ensure hub variable has a value set
4. Review debug logs to see which value is being used

## Technical Details

### App Properties
- **Name**: Security Alarm Manager
- **Namespace**: hubitat
- **Author**: Tim Brown
- **Category**: Security
- **Single Threaded**: Yes (for simpler state management)

### Supported Capabilities
- `capability.alarm` - Siren devices
- `capability.switch` - Control switches and alarm plugs
- `capability.pushableButton` - Physical button controls
- `capability.notification` - Notification devices

### Event Subscriptions
- Location mode changes
- Alarm control switch changes
- Button push events
- Virtual switch activations

### Methods Available for Cross-App Use
While this app is designed to be triggered via virtual switches, these internal methods are available:
- `executeAlarms()` - Execute full alarm sequence
- `stopAllAlarms()` - Stop all alarms immediately
- `armAlarms()` - Arm the alarm system
- `disarmAlarms()` - Disarm the alarm system
- Various `play*()` methods for specific sounds

## Version History
- **1.0.0** (2025-12-04): Initial release
  - Consolidates 19 Rule Machine rules
  - Full hub variable support
  - Cross-app communication via virtual switches
  - Multiple siren support
  - Mode-based automation

## License
Licensed under the Apache License, Version 2.0. See LICENSE file for details.

## Support
For issues, questions, or feature requests, please check the Hubitat Community forums.
