# Lights Automation Manager

## Overview
Lights Automation Manager is a comprehensive Hubitat app that manages all automated lighting throughout your home. It consolidates mode-based schedules, motion-activated flood lights, desk lighting controls, color-changing light strips, and master light controls into a single, efficient application.

## Purpose
- Centralize all lighting automation
- Provide mode-based lighting schedules (Morning, Day, Evening, Night)
- Motion-activated flood lights for security
- Desk light control with motion and button triggers
- Color-changing light strip management
- Master all-on/all-off controls
- Emergency lighting integration

## Features
- **Mode-Based Automation**: Different lighting scenes for each mode
- **Desk Lighting**: Motion sensor and button control with brightness levels
- **Color Light Strips**: Automated color changes based on mode
- **Flood Lights**: Motion-activated security floods with auto-off timers
- **Master Controls**: Turn all lights on/off with single command
- **Emergency Integration**: Red alert lighting when triggered by other apps
- **Hub Variable Support**: Dynamic configuration overrides
- **Condition-Based**: Holiday and PTO modes affect automation

## Installation

### Prerequisites
1. **Hubitat Hub** running firmware 2.3.0 or later
2. **Light Devices**: Various switches, dimmers, color-changing bulbs, strips
3. **Motion Sensors**: For flood light activation

### Installation Steps
1. Open Hubitat web interface
2. Navigate to **Apps Code**
3. Click **New App**
4. Copy and paste the contents of `LightsAutomationManager.groovy`
5. Click **Save**
6. Navigate to **Apps**
7. Click **Add User App**
8. Select **Lights Automation Manager**
9. Configure the app (see Configuration section below)

## Configuration

### Desk Lighting
- **Desk Motion Sensor**: Motion sensor near desk for auto-dim
- **Desk Button**: Physical button for bright/dim control
- **Desk Light**: Smart bulb/strip with level and color temperature control

### Light Strips
- **Main Light Strip**: Primary color-changing strip
- **LAN Strip**: Secondary color-changing strip

### Night Mode Lights
- **Night Mode Lights**: Switches/lights that turn ON during Night mode
- **Turn OFF Night Mode Lights when entering**: Select which modes (Morning, Day, Evening) should turn these lights OFF
  - These lights are specifically for night-time illumination (e.g., night lights, hallway lights)
  - Turn ON automatically when entering Night mode
  - Turn OFF automatically when entering selected modes
  - If no modes are selected, lights remain ON across mode changes
  - **Important**: Do not include these devices in Generic Switches to avoid conflicts

### Generic Switches & Outlets
- **Generic Switches/Outlets**: Regular switches controlled by mode changes

### Flood Lights
Configure individual flood lights and their associated motion sensors:
- **Rear Flood Light** + **Rear Flood Motion Sensor**
- **Side Flood Light** + **Side Flood Motion Sensor**
- **Shower Flood Light**
- **Woodshed Flood Light**
- **Office Flood Light** + **Office Flood Motion Sensor**
- **Carport Flood Light**

### Master Light Groups
- **All Lights Group**: Collection of all lights for master control
- **All Lights Master Switch**: Virtual or physical switch for all lights

### Condition Switches
- **On PTO Switch**: When on, skips morning light automation. **Automatically turns ON at sunset on Fridays and OFF at sunrise on Sundays** for weekend PTO mode.
- **Holiday Switch**: When on, skips morning light automation
- **Traveling Switch**: Travel mode indicator

### Lighting Configuration
- **Desk Bright Level**: Brightness when button is pushed (0-100, default: 100)
- **Desk Dim Level**: Brightness for motion/double-tap (0-100, default: 5)
- **Desk Color Temperature**: Kelvin value for desk light (2000-6500K, default: 2700)
- **Strip Night Level**: Strip brightness at night (0-100, default: 30)
- **Strip Day Level**: Strip brightness during day modes (0-100, default: 50)

### Motion Timeout Configuration
- **Flood Light Motion Timeout**: Minutes before flood auto-off (default: 5)

### Day Mode Delay
- **Day Mode Delay**: Wait time in minutes after mode becomes Day before adjusting lights (0-60, default: 0)
  - When set to 0, lights adjust immediately
  - Useful to prevent lights from turning off prematurely during brief Day mode transitions

### Cross-App Communication
- **Emergency Light Trigger**: Virtual switch for emergency red alert from other apps

### Notifications
- **Notification Devices**: Devices to receive motion alerts

## Hub Variables

The app supports the following hub variables for dynamic configuration:

### Configuration Variables (Read by App)
- `deskBrightness` (Number): Override desk light brightness (0-100)
- `floodTimeout` (Number): Override motion-activated flood timeout in minutes
- `stripColorDay` (String): Override daytime strip color (e.g., "Soft White", "Blue")
- `stripColorNight` (String): Override nighttime strip color (e.g., "Blue", "Red")

**Note**: Morning, Evening, and Night lighting modes are triggered by mode changes, not time schedules.

### Creating Hub Variables
1. Navigate to **Settings → Hub Variables**
2. Click **Create New Variable**
3. Set variable name (e.g., `deskBrightness`)
4. Set variable type (String, Number, etc.)
5. Set initial value
6. Click **Create Variable**

## Usage

### Mode-Based Automation

The app automatically adjusts lighting when hub mode changes:

#### Night Mode
- Generic switches: OFF
- Night mode lights: ON (configurable lights for night-time illumination)
- Light strips: Blue at 30% (or hub variable override)
- Desk light: Dim to level 5
- Flood lights: OFF (will reactivate on motion)

#### Evening Mode
- Night mode lights: OFF (if "Evening" is selected in turn-off modes)
- Generic switches: ON
- Main strip: Soft White at 50%
- LAN strip: Yellow at 96%
- All configured lights activated

#### Morning Mode
- Night mode lights: OFF (if "Morning" is selected in turn-off modes)
- **IF** Holiday is OFF **AND** PTO is OFF:
  - Generic switches: ON
  - Main strip: Soft White at 50%
  - LAN strip: Yellow at 96%
- **ELSE**: Only updates main strip (skips switch activation)

#### Day Mode
- Night mode lights: OFF (if "Day" is selected in turn-off modes)
- Generic switches: OFF
- Light strips: OFF
- Desk light: Manual control only
- **Optional Delay**: If configured, waits specified minutes before turning off lights
  - Useful to prevent lights from turning off immediately during brief mode transitions
  - Default is 0 (no delay, immediate execution)

### Desk Light Control

#### Motion-Activated
1. Motion detected at desk
2. Desk light automatically dims to configured level (default: 5%)
3. Color temperature set to 2700K (warm white)

#### Button Control
- **Single Push (Button 1)**: Bright mode (100% or configured)
- **Double Tap (Button 1)**: Dim mode (5% or configured)

### Flood Light Automation

Motion sensors trigger associated flood lights:
1. Motion detected
2. Flood light turns ON
3. Stays on for configured timeout (default: 5 minutes)
4. Automatically turns OFF after timeout

**Supported Flood Zones**:
- Rear
- Side  
- Office

### Master Light Controls

#### All Lights ON
Activate by:
- Turning on "All Lights Master Switch"
- Calling `allLightsOn()` method
- Emergency light trigger

Actions:
- Turns on all lights in "All Lights Group"
- Turns on light strips
- Sets desk light to bright level

#### All Lights OFF
Activate by:
- Turning off "All Lights Master Switch"
- Calling `allLightsOff()` method

Actions:
- Turns off all lights in "All Lights Group"
- Turns off light strips
- Turns off desk light

### Emergency Lighting

When Emergency Light Trigger switch is activated (from NightSecurityManager or EmergencyHelpManager):
1. All lights turn ON
2. Light strips change to RED at 100%
3. Provides maximum visibility for emergency situations

### Light Strip Colors

Supported colors:
- **Blue**: Hue 66, Saturation 100
- **Soft White**: Color temp 2700K or Hue 23, Sat 56
- **Yellow**: Hue 18, Sat 19
- **Green**: Hue 33, Sat 100
- **Red**: Hue 0, Sat 100 (emergency)
- **White**: Color temp 5000K or Hue 0, Sat 0

## Rules Replaced

This app replaces the following 17 Rule Machine rules:
1. 1-FloodRearOff
2. 1-FloodRearOn
3. TurnAllLightsOff
4. TurnAllLightsOn
5. TurnFloodsOn
6. TurnLightsNight
7. TurnLightsOnEvening
8. TurnLightsOnMorning
9. DeskLightBright
10. DeskLightDimmest
11. LightstripGreen
12. LightstripRed
13. StripLightsWhite
14. Lightstrip
15. Motion-FloodOn
16. ShowerHelpDeskRed
17. WhisperToGuestroom

**Note**: CarportBeamDay, CarportBeamEvening, and CarportBeamMorning rules are now consolidated in PerimeterSecurityManager.

## Apps Replaced

This app consolidates and replaces:
1. **LightsApp.groovy** (214 LOC) - Mode-based lighting and desk control

**Note**: CarPortControl.groovy functionality has been moved to PerimeterSecurityManager.

## Example Configurations

### Basic Home Lighting
**Scenario**: Simple mode-based lighting with desk control

**Configuration**:
- Desk Light: Smart bulb in office
- Desk Motion: Office motion sensor
- Desk Button: Office button
- Main Light Strip: Living room strip
- Generic Switches: Lamp in living room, kitchen lights
- Modes: Morning, Day, Evening, Night

### Advanced Multi-Zone System
**Scenario**: Full automation with floods and emergency

**Configuration**:
- **Desk**: Office smart bulb + motion sensor + button
- **Strips**: Living room strip + bedroom strip  
- **Generic Switches**: 5 lamps throughout home
- **Floods**: Rear, Side, Office (each with motion sensor)
- **Master**: Virtual "All Lights" switch + group of all lights
- **Conditions**: PTO switch, Holiday switch
- **Emergency Trigger**: Virtual switch from SecurityAlarmManager
- **Notifications**: Phone notification device

**Hub Variable Overrides**:
- `deskBrightness` = 80 (slightly dimmer than default 100)
- `floodTimeout` = 10 (longer timeout, 10 minutes)
- `stripColorNight` = "Purple" (custom night color)

## Troubleshooting

### Lights Don't Change with Mode
1. Verify mode has changed in hub
2. Check that lights are configured in correct sections
3. Review logs for mode change events
4. Ensure lights are responsive (test manual control)

### Desk Light Not Responding to Motion
1. Check motion sensor battery
2. Verify motion sensor is configured
3. Check desk light is online
4. Review logs for motion events
5. Ensure motion events are being received

### Desk Button Doesn't Work
1. Verify button device is selected
2. Check battery level
3. Ensure you're pressing Button 1
4. Review logs for button press events
5. Test with both push and double-tap

### Flood Lights Don't Turn Off
1. Check scheduled jobs in app state
2. Verify timeout setting is reasonable
3. Manually turn off to clear state
4. Review logs for auto-off execution

### Morning Lights Don't Turn On
1. Check Holiday switch state (should be OFF)
2. Check PTO switch state (should be OFF)
3. Verify generic switches are configured
4. Review logs for condition checks

### Emergency Lights Don't Activate
1. Verify Emergency Light Trigger switch is configured
2. Check cross-app communication setup
3. Test trigger switch manually
4. Review logs for trigger events

## Technical Details

### App Properties
- **Name**: Lights Automation Manager
- **Namespace**: hubitat
- **Author**: Tim Brown
- **Category**: Lighting
- **Single Threaded**: Yes
- **Estimated LOC**: ~700 lines

### Supported Capabilities
- `capability.switch` - Standard switches
- `capability.switchLevel` - Dimmers
- `capability.colorControl` - RGB/RGBW devices
- `capability.colorTemperature` - Tunable white devices
- `capability.motionSensor` - Motion detection
- `capability.contactSensor` - Contact/beam sensors
- `capability.pushableButton` - Button devices
- `capability.notification` - Notification devices

### Event Subscriptions
- Location mode changes
- Motion sensor activations
- Button push/double-tap events
- Contact sensor state changes
- Switch state changes (master controls, emergency triggers)

### Scheduled Jobs
- Flood light auto-off (motion timeout)
- Emergency trigger reset

### Methods Available
- `allLightsOn()` - Turn on all lights
- `allLightsOff()` - Turn off all lights
- `setDeskLight(level)` - Set desk light brightness
- `setDeskLightRed()` - Emergency red indicator
- `setStrip(device, color, level)` - Color strip control
- `setStripColor(color)` - Quick strip color change
- `handleFloodMotion(light, location)` - Motion-activated flood

## Integration with Other Apps

### Security Alarm Manager
- Emergency light trigger integration

### Night Security Manager
- Emergency light trigger activates all lights + red strips

### Perimeter Security Manager
- Carport beam detection is handled by PerimeterSecurityManager
- Uses shared notification devices for alerts

### Emergency Help Manager
- Desk light turns red for shower emergency indicator
- Emergency light trigger activates all lights + red strips

## Performance Considerations

- **Efficient Mode Handling**: Single mode change triggers coordinated lighting
- **Smart Scheduling**: Flood auto-off uses runIn for efficiency
- **Minimal Subscriptions**: Only subscribes to configured devices
- **Single Threaded**: Avoids concurrent state modification issues
- **Pauseexecution**: Uses small delays only when necessary for device settling

## Version History
- **1.0.0** (2025-12-04): Initial release
  - Consolidates LightsApp functionality
  - 17 rules consolidated
  - Mode-based automation
  - Flood light motion activation
  - Desk light control
  - Master light controls
  - Emergency lighting integration
  - Hub variable support
- **1.1.0** (2025-12-05): Refactored
  - Moved carport beam functionality to PerimeterSecurityManager
  - Removed silent switches (used by carport beam only)

## License
Licensed under the Apache License, Version 2.0. See LICENSE file for details.

## Support
For issues, questions, or feature requests, please check the Hubitat Community forums.
