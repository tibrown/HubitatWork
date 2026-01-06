# Door Window Monitor

## Overview
Door Window Monitor is a comprehensive Hubitat app that monitors all doors and windows throughout your home. It provides immediate alerts when doors/windows open, detects left-open conditions, monitors special doors (freezer, safe), offers alarm pause functionality, and includes tamper detection.

## Purpose
- Monitor all door and window activity
- Alert on door/window openings based on mode
- Detect doors/windows left open beyond thresholds
- Special monitoring for freezer and safe
- Pause alarm functionality for controlled access
- Tamper detection for security
- Mode-based alert variations

## Features
- **Comprehensive Monitoring**: 11+ door/window sensors
- **Left-Open Detection**: Periodic scanning with configurable thresholds
- **Freezer Monitoring**: Quick alerts for freezer door left open
- **Safe Monitoring**: Immediate notification when safe is opened
- **Pause Functionality**: Temporarily disable alarms for specific doors
- **Mode-Based Alerts**: Different responses for Day/Evening/Night/Morning modes
- **Tamper Detection**: Security alerts for device tampering
- **Hub Variable Support**: Dynamic threshold configuration

## Installation

### Prerequisites
1. **Hubitat Hub** running firmware 2.3.0 or later
2. **Contact Sensors**: Door/window sensors throughout home
3. **Notification Device**: For receiving alerts

### Installation Steps
1. Open Hubitat web interface
2. Navigate to **Apps Code**
3. Click **New App**
4. Copy and paste the contents of `DoorWindowMonitor.groovy`
5. Click **Save**
6. Navigate to **Apps**
7. Click **Add User App**
8. Select **Door Window Monitor**
9. Configure the app (see Configuration section below)

## Configuration

### Exterior Doors
- **Front Door (Living Room)**: Main entrance door
- **Dining Room Front Door**: Secondary entrance
- **Living Room French Doors**: Patio/exterior access
- **Backdoor (Lanai)**: Rear entrance

### Shed & Storage Doors
- **Bird House Door (She Shed)**: She shed main door
- **Bird House Screen Door**: She shed screen door
- **Concrete Shed Door**: Concrete shed access
- **Woodshed Door**: Woodshed storage

### Special Doors
- **Freezer Door**: Freezer with critical temperature monitoring
- **Safe Door**: Security safe access monitoring

### Windows
- **Living Room Window**: Main living area window

### Pause Switches
- **Pause DR Door Alarm Switch**: Temporarily disable dining room door alerts
- **Pause Backdoor Alarm Switch**: Temporarily disable backdoor alerts

### Alert Thresholds
- **Door Left Open Alert**: Minutes before alerting (default: 5)
- **Window Left Open Alert**: Minutes before alerting (default: 10)
- **Freezer Door Left Open Alert**: Minutes before alerting (default: 2)
- **Check Interval**: How often to scan for left-open (default: 1 minute)

### Pause Configuration
- **Auto Pause Duration**: Minutes before auto-unpause (default: 5)

### Notifications
- **Notification Devices**: Devices to receive door/window alerts

## Hub Variables

The app supports the following hub variables for dynamic configuration:

### Configuration Variables (Read by App)
- `doorOpenThreshold` (Number): Override time before alerting on open door in minutes
- `windowOpenThreshold` (Number): Override time before alerting on open window in minutes
- `freezerDoorThreshold` (Number): Override freezer door open threshold in minutes
- `pauseDuration` (Number): Override alarm pause duration in minutes
- `checkInterval` (Number): Override periodic check interval in minutes
- `tamperAlertEnabled` (String): Enable/disable tamper detection ("true" or "false")

### Creating Hub Variables
1. Navigate to **Settings → Hub Variables**
2. Click **Create New Variable**
3. Set variable name (e.g., `doorOpenThreshold`)
4. Set variable type (Number for thresholds)
5. Set initial value (e.g., 5 for 5 minutes)
6. Click **Create Variable**

## Usage

### Door/Window Open Alerts

The app monitors all configured doors and windows and sends alerts based on mode:

#### Front Door
- **Day Mode**: "Front door is open, Front door is open"
- **Morning Mode**: "Alert, Intruder at the front door"

#### Dining Room Door
- **Day Mode**: "Dining room door is open, dining room door is open"

#### French Doors
- **Day Mode**: "French doors are open, French doors are open"

#### Shed Doors
- **All Modes**: Immediate notification when opened
- **Bird House Door**: "Bird House door is open"
- **Concrete Shed**: "Concrete shed door is open"
- **Woodshed**: "Woodshed door is open"

#### Living Room Window
- **Day Mode**: "Living room window is open, living room window is open"
- **Evening/Night/Morning**: "Living room window is open"

### Special Door Monitoring

#### Freezer Door
- Monitors open time
- Alerts if left open beyond threshold (default: 2 minutes)
- Critical for food safety
- Message: "ALERT: Freezer door has been left open!"

#### Safe Door
- Immediate notification when opened
- Message: "Safe door has been opened"
- Provides security audit trail

### Left-Open Detection

The app periodically scans all doors and windows every minute (configurable):

1. Tracks when each door/window opens
2. Calculates how long it's been open
3. Compares to threshold:
   - **Doors**: 5 minutes (configurable)
   - **Windows**: 10 minutes (configurable)
   - **Freezer**: 2 minutes (configurable)
4. Sends alert if threshold exceeded
5. Clears tracking to prevent spam (alerts once per opening)

**Example Alert**: "ALERT: Front door has been left open!"

### Pause Functionality

Temporarily disable alarms for specific doors:

#### Pause Dining Room Door Alarm
1. Turn on **Pause DR Door Alarm Switch**
2. Alarm paused for configured duration (default: 5 minutes)
3. Notification sent: "Dining room door alarm paused for 5 minutes"
4. Automatically unpauses after duration
5. Switch automatically turns off

**Use Case**: Moving furniture through door, loading groceries, etc.

#### Pause Backdoor Alarm
1. Turn on **Pause Backdoor Alarm Switch**
2. Same behavior as dining room pause
3. Notification sent: "Backdoor alarm paused for 5 minutes"

### Tamper Detection

If tamper detection is enabled (hub variable or setting):
- Monitors for device tampering attempts
- Sends immediate security alert
- Message: "SECURITY ALERT: Tamper detected on [device name]"

## Rules Replaced

This app replaces the following 16 Rule Machine rules:
1. BirdHouseDoorOpen (1663)
2. BirdHouseScreenDoorOpen (1797)
3. ConcreteShedDoorOpen (1631)
4. DiningFrontDoorOpen (801)
5. LivingRoomDoorOpen (912)
6. LRFrenchDoorOpenDay (913)
7. WoodshedDoorOpen (597)
8. FreezerDoorOpen (1742)
9. SafeDoorOpened (1616)
10. PauseDRDoorAlarm (803)
11. PauseBDAlarmRule (1694)
12. DoorBHScreen (1705)
13. LRWindowOpenDay (917)
14. LRWindowOpenENM (918)
15. Morning-IntruderLRFrontDoor (589)
16. Tamper (1633)

## Example Configurations

### Basic Home Monitoring
**Scenario**: Monitor main entry points with left-open detection

**Configuration**:
- Front Door: Main entrance
- Backdoor: Rear entrance
- Freezer Door: Kitchen freezer
- Door Left Open Alert: 5 minutes
- Freezer Door Alert: 2 minutes
- Check Interval: 1 minute
- Notification Device: Phone

### Complete Home Security
**Scenario**: Monitor all doors, windows, and special areas

**Configuration**:
- **Exterior**: Front door, dining room door, French doors, backdoor
- **Sheds**: Bird house door, bird house screen, concrete shed, woodshed
- **Special**: Freezer door, safe door
- **Windows**: Living room window
- **Pause Switches**: DR door pause, backdoor pause
- **Thresholds**:
  - Doors: 5 minutes
  - Windows: 10 minutes
  - Freezer: 2 minutes
- **Check Interval**: 1 minute
- **Pause Duration**: 5 minutes
- **Notifications**: Phone + Alexa devices

**Hub Variable Overrides**:
- `freezerDoorThreshold` = 1 (more sensitive, 1 minute)
- `doorOpenThreshold` = 3 (quicker alerts, 3 minutes)
- `tamperAlertEnabled` = "true"

## Troubleshooting

### Not Receiving Door Open Alerts
1. Verify contact sensor is configured
2. Check sensor battery level
3. Test sensor manually (open/close and check device page)
4. Verify notification device is configured
5. Review logs for contact events

### Left-Open Alerts Not Working
1. Check that door/window is actually open
2. Verify check interval is running (review logs)
3. Check threshold settings (may need to wait longer)
4. Ensure notification device is working
5. Check app state for open time tracking

### Freezer Alert Too Sensitive/Not Sensitive Enough
1. Adjust **Freezer Door Left Open Alert** setting
2. Or use hub variable `freezerDoorThreshold`
3. Default is 2 minutes - increase if false positives
4. Decrease to 1 minute for more critical monitoring

### Pause Not Working
1. Verify pause switch is configured
2. Check that switch is actually turning ON
3. Review logs for pause activation
4. Test manual pause switch activation
5. Verify pause duration setting

### Pause Not Auto-Unpausing
1. Check scheduled jobs in app state
2. Verify pause duration is reasonable
3. Review logs for unpause execution
4. Manually turn off pause switch if stuck

### Too Many Duplicate Alerts
1. Check if multiple apps monitoring same sensor
2. Verify alert threshold tracking (should clear after first alert)
3. Review state variables for open time tracking
4. May need to adjust check interval (increase)

### Safe Door Alert Not Received
1. Verify safe door sensor is configured
2. Check sensor battery and connectivity
3. Test sensor manually
4. Review logs for contact events
5. Ensure notification device is working

## Integration with Other Apps

### Night Security Manager
- Coordinates nighttime door/window monitoring
- Night mode alerts handled by NightSecurityManager
- Pause switches shared between apps
- Complementary rather than overlapping coverage

### Security Alarm Manager
- Door/window breaches can trigger alarms
- Pause switches prevent false alarms
- Coordinated security response

## Technical Details

### App Properties
- **Name**: Door Window Monitor
- **Namespace**: hubitat
- **Author**: Tim Brown
- **Category**: Safety & Security
- **Single Threaded**: Yes
- **Estimated LOC**: ~400 lines

### Supported Capabilities
- `capability.contactSensor` - Door/window sensors
- `capability.switch` - Pause switches
- `capability.notification` - Notification devices

### Event Subscriptions
- Contact sensor state changes (open/closed)
- Pause switch activations

### Scheduled Jobs
- Periodic left-open check (cron schedule)
- Auto-unpause timers (runIn)

### State Variables
- `[deviceId]_openTime` - Timestamp when device opened (for left-open detection)

### Methods Available
- `handleDoorOpen(device, mode)` - Process door open event
- `handleDoorClosed(device)` - Process door close event
- `checkLeftOpen()` - Periodic scan for left-open conditions
- `handlePauseDRDoor(evt)` - Pause dining room door alarm
- `handlePauseBD(evt)` - Pause backdoor alarm
- `handleTamper(device)` - Process tamper event

## Performance Considerations

- **Efficient Scanning**: Periodic check only processes tracked devices
- **State Management**: Minimal state storage (only open timestamps)
- **Smart Alerting**: Clears tracking after first alert to prevent spam
- **Scheduled Efficiency**: Uses cron for periodic checks vs polling
- **Single Threaded**: Avoids concurrent state modification

## Safety Considerations

### Critical Monitoring
- **Freezer Door**: Food safety critical - keep threshold low (1-2 minutes)
- **Exterior Doors**: Security critical - consider mode-based variations
- **Safe Access**: Audit trail for security valuables

### Reliability
- Monitor sensor battery levels regularly
- Test sensors periodically
- Ensure notification devices are accessible 24/7
- Consider redundant notification methods
- Review logs for missed events

## Version History
- **1.0.0** (2025-12-04): Initial release
  - Consolidates 16 Rule Machine rules
  - Comprehensive door/window monitoring
  - Left-open detection with periodic scanning
  - Freezer and safe special monitoring
  - Pause functionality with auto-reset
  - Mode-based alert variations
  - Tamper detection
  - Hub variable support

## License
Licensed under the Apache License, Version 2.0. See LICENSE file for details.

## Support
For issues, questions, or feature requests, please check the Hubitat Community forums.
