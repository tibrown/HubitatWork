# Emergency Help Manager

## Overview
Emergency Help Manager is a comprehensive Hubitat app that manages emergency help buttons and assistance requests throughout your home. It provides visual and audio alerts, repeating notifications, and location-specific help coordination to ensure emergency assistance is quickly summoned and clearly communicated.

## Purpose
- Centralize emergency help button management
- Provide multi-layered alert system (visual, audio, notifications)
- Support location-specific help requests
- Enable repeating alerts until acknowledged
- Coordinate with alarm systems for emergency sounds
- Manage silent mode automation

## Features
- **Shower Emergency System**: Multi-button shower help with repeating alerts and visual indicators
- **Location-Specific Help**: Separate buttons for different locations (shower, bird house, mobile key fob)
- **Visual Alerts**: Flash lights and change colors to indicate emergency
- **Repeating Notifications**: Configurable repeat count and interval for persistent alerts
- **Alert Cancellation**: Easy-to-access stop mechanisms
- **Silent Mode Automation**: Scheduled silent mode disable
- **Alarm Integration**: Trigger security alarm sounds during emergencies
- **Hub Variable Support**: Dynamic configuration via hub variables

## Installation

### Prerequisites
1. **Hubitat Hub** running firmware 2.3.0 or later
2. **Button Devices**: At least one help button device
3. **Virtual Switches**: Create the following switches in Hubitat:
   - `StopShowerHelp` (Virtual Switch or Connector Switch)
   - `Silent` (Connector Switch - recommended)
   - `SilentCarport` (Connector Switch - recommended, optional)

### Optional Devices
- Smart lights with flash capability
- Color-changing desk light
- Alarm trigger switch (for integration with SecurityAlarmManager)
- Notification devices (phone, Alexa, etc.)

### Installation Steps
1. Open Hubitat web interface
2. Navigate to **Apps Code**
3. Click **New App**
4. Copy and paste the contents of `EmergencyHelpManager.groovy`
5. Click **Save**
6. Navigate to **Apps**
7. Click **Add User App**
8. Select **Emergency Help Manager**
9. Configure the app (see Configuration section below)

## Configuration

### Shower Help Configuration
- **Shower Help Button**: Button device in the shower for emergency help
- **Stop Shower Help Switch**: Virtual switch to cancel shower help alerts
- **Desk Button**: Physical button to stop alerts (typically held)

### Other Help Buttons
- **Key Fob Help Button**: Mobile key fob for help requests anywhere
- **Bird House Help Button**: NanoMote or button in specific location

### Visual Alert Devices
- **Lights to Flash for Alerts**: Select multiple switches to flash during emergencies
- **Desk Light**: Color-changing light for emergency color indication (red)

### Silent Mode Switches
- **Silent Mode Switch**: Master silent mode switch
- **Silent Carport Switch**: Location-specific silent switch

### Alarm Integration
- **Alarm Trigger Switch**: Virtual switch to trigger SecurityAlarmManager alarms

### Alert Configuration
- **Help Alert Duration**: Duration of help alert in seconds (default: 300 = 5 minutes)
- **Alert Repeat Count**: Number of times to repeat alert (default: 5)
- **Alert Repeat Interval**: Seconds between repeats (default: 300 = 5 minutes)
- **Light Flash Rate**: Flashes per second (default: 2)
- **Time to Automatically Turn Off Silent Mode**: Scheduled time (e.g., 9:00 AM)

### Notifications
- **Notification Devices**: Devices to receive emergency notifications

## Hub Variables

The app supports the following hub variables for dynamic configuration. These override the standard settings when present:

### Configuration Variables (Read by App)
- `helpAlertDuration` (Number): Override help alert duration in seconds
- `flashRate` (Number): Override light flash rate (flashes per second)
- `emergencyVolume` (Number): Override emergency siren volume (0-100)
- `silentModeTimeout` (Number): Override silent mode timeout in minutes
- `notificationDelay` (Number): Override notification delay in seconds
- `visualOnlyMode` (String): Enable visual-only alerts ("true" or "false")
- `helpAlertRepeat` (Number): Override repeat count
- `helpAlertInterval` (Number): Override repeat interval in seconds

### Message Variables (Written by App)
- `EchoMessage` (String): Message for Alexa TTS integration - Written by this app

### Creating Hub Variables
1. Navigate to **Settings → Hub Variables**
2. Click **Create New Variable**
3. Set variable name (e.g., `helpAlertDuration`)
4. Set variable type (String, Number, Boolean, etc.)
5. Set initial value
6. Click **Create Variable**

## Usage

### Shower Emergency System

#### Triggering Shower Help
1. Press, hold, or double-tap the **Shower Help Button**
2. The app will:
   - Trigger alarm sounds (via SecurityAlarmManager if configured)
   - Flash configured lights
   - Set desk light to red
   - Send notification: "Help Needed In The Shower"
   - Repeat alert 5 times (configurable) every 5 minutes (configurable)

#### Stopping Shower Help
**Method 1: Desk Button**
- Hold the **Desk Button** to activate the stop switch

**Method 2: Virtual Switch**
- Turn on the **Stop Shower Help Switch** manually

When stopped:
- All scheduled repeats are cancelled
- Desk light dims to level 1
- Stop switch auto-resets after 5 minutes

### Key Fob Emergency
1. Press any button on the **Key Fob Help Button**
2. The app will:
   - Flash configured lights
   - Send notification: "Help Needed, keyfob pressed"

### Bird House Emergency
1. Press or hold any button on the **Bird House Help Button**
2. The app will:
   - Flash configured lights
   - Send repeating notifications (3 times, every 30 seconds)
   - Message: "Help Needed in the Birdhouse, help needed in the birdhouse"

### Silent Mode Automation
- Silent mode automatically turns off at the configured time (e.g., 9:00 AM)
- Both `Silent` and `SilentCarport` switches are turned off
- Useful for ensuring alarms are re-enabled after overnight silent period

## Rules Replaced
This app replaces the following 8 Rule Machine rules:
1. ExecuteHelpShower (959)
2. ShowerAssist (1189)
3. ShowerAssistOff (1121)
4. StopShowerAlert (1217)
5. KeyFobHelp (1154)
6. BirdHouseHelp (1668)
7. TurnSilentOff (1779)
8. TurnSilentOff (1218) - duplicate reference

## Example Configurations

### Basic Shower Help
**Scenario**: Simple shower emergency button with notifications

**Configuration**:
- Shower Help Button: Bathroom button device
- Stop Shower Help Switch: Virtual switch
- Desk Button: Office button (held to stop)
- Notification Devices: Phone
- Alert Repeat Count: 5
- Alert Repeat Interval: 300 seconds (5 minutes)

### Advanced Multi-Location Help
**Scenario**: Multiple help buttons with visual alerts and alarm integration

**Configuration**:
- Shower Help Button: Bathroom button
- Key Fob Help Button: Mobile key fob
- Bird House Help Button: NanoMote in bird house
- Stop Shower Help Switch: Virtual switch
- Lights to Flash: Living room lights, bedroom lights, office lights
- Desk Light: RGB desk light (for red color indicator)
- Alarm Trigger Switch: Virtual switch connected to SecurityAlarmManager
- Silent Mode Switch: Master silent switch
- Silent Mode Auto-Off Time: 9:00 AM
- Notification Devices: Phone, Alexa devices

**Hub Variable Overrides**:
- `helpAlertRepeat` = 10 (more repetitions)
- `helpAlertInterval` = 180 (every 3 minutes)
- `flashRate` = 3 (faster flashing)

## Integration with Other Apps

### Security Alarm Manager Integration
The EmergencyHelpManager can trigger alarms via the SecurityAlarmManager:

1. Create a virtual switch named `AlarmTrigger`
2. Configure SecurityAlarmManager to subscribe to this switch
3. Configure EmergencyHelpManager with this switch as **Alarm Trigger Switch**
4. When help is requested, the switch activates, triggering alarms

### TellAlexa Integration
The app sets the `EchoMessage` hub variable for Alexa TTS integration:
- Messages are automatically formatted for Alexa
- Alexa announces emergency help messages
- Requires TellAlexa app or similar integration

## Troubleshooting

### Shower Help Doesn't Activate
1. Check that **Shower Help Button** is configured and responsive
2. Verify **Stop Shower Help Switch** is OFF (not previously activated)
3. Check button battery level
4. Review logs for button press events

### Alerts Don't Repeat
1. Verify **Alert Repeat Count** is greater than 1
2. Check that **Stop Shower Help Switch** hasn't been activated
3. Review scheduled jobs in app state
4. Check logs for execution messages

### Lights Don't Flash
1. Verify lights are selected in **Lights to Flash**
2. Check that lights support flash command
3. Try turning lights on/off manually to verify connectivity
4. Some lights may not support native flash command

### Notifications Not Received
1. Verify **Notification Devices** are configured
2. Check notification device settings in Hubitat
3. Test notification device with manual message
4. Review logs for notification send attempts

### Silent Mode Doesn't Turn Off Automatically
1. Verify **Time to Automatically Turn Off Silent Mode** is configured
2. Check hub time zone settings
3. Review scheduled jobs
4. Manually test by running `turnOffSilentMode()` method

### Desk Light Doesn't Turn Red
1. Verify desk light supports color commands
2. Check if light has `setColor` capability
3. Try setting color manually in device page
4. Some lights may require specific color format

## Technical Details

### App Properties
- **Name**: Emergency Help Manager
- **Namespace**: hubitat
- **Author**: Tim Brown
- **Category**: Safety & Security
- **Single Threaded**: Yes (for simpler state management)

### Supported Capabilities
- `capability.pushableButton` - Help buttons, key fobs
- `capability.switch` - Control switches, flash lights, stop switches
- `capability.switchLevel` - Desk light dimming
- `capability.notification` - Notification devices

### State Variables
- `showerHelpActive` (Boolean): Whether shower help alert is currently active
- `repeatCount` (Integer): Current repetition number for shower help

### Event Subscriptions
- Button push events (pushed, held, doubleTapped)
- Switch on events (stop switch)
- Scheduled time events (silent mode auto-off)

### Methods Available for Manual Triggering
- `executeShowerHelp()` - Manually execute shower help alert
- `handleStopShowerHelp()` - Manually stop shower help
- `turnOffSilentMode()` - Manually disable silent mode
- `flashLightsForHelp()` - Flash lights for visual alert
- `setDeskLightRed()` - Set desk light to red emergency color

## Safety Considerations

### Emergency Response
- **This app is NOT a substitute for proper emergency services**
- Always dial emergency services (911, etc.) for life-threatening emergencies
- Help buttons should supplement, not replace, traditional emergency communication
- Ensure all household members know how to use emergency help buttons
- Test emergency buttons regularly to ensure functionality

### Reliability
- Use reliable button devices with good battery life
- Consider battery backup for hub and network equipment
- Test all alert paths (lights, sounds, notifications) regularly
- Have redundant help buttons in critical locations
- Ensure notification devices are accessible 24/7

### Privacy and Security
- Be mindful of who has access to stop emergency alerts
- Consider requiring authentication for stopping help alerts
- Review notification recipient lists periodically
- Secure virtual switches to prevent unauthorized access

## Version History
- **1.0.0** (2025-12-04): Initial release
  - Consolidates 8 Rule Machine rules
  - Full hub variable support
  - Shower help with repeating alerts
  - Key fob and location-specific help
  - Visual alert system
  - Alarm integration capability
  - Silent mode automation

## License
Licensed under the Apache License, Version 2.0. See LICENSE file for details.

## Support
For issues, questions, or feature requests, please check the Hubitat Community forums.
