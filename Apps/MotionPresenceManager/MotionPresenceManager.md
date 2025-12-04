# Motion Presence Manager

## Overview
The Motion Presence Manager provides comprehensive motion detection and presence management for your home automation system. It handles zone-based motion sensors, phone presence detection, arrival grace periods, and time-based motion responses.

**Key Features:**
- Zone-based motion detection (carport, front door, side yard, RV, office, rear carport)
- Phone presence tracking with arrival/departure detection
- Automatic arrival grace period to prevent false alarms
- Mode-based motion processing (Day/Night/Morning/Evening)
- Hub variable support for dynamic configuration
- Configurable notifications for motion and presence events

## Consolidated Rules
This app replaces the following 13 Rule Machine rules:

### Motion Detection Rules
1. **Motion-Carport** - Carport motion detection and notifications
2. **Motion-FrontDoorDay** - Front door motion detection during day mode
3. **MotionAMCSideYard** - Side yard motion monitoring
4. **MotionInRV** - RV motion detection
5. **MotionOffice** - Office motion detection
6. **RearCarportActive** - Rear carport motion with active switch control
7. **RearCarportMotion** - Rear carport motion handling
8. **CPFrontActive** - Carport front motion detection

### Presence Detection Rules
9. **PhoneArrivesDay** - Phone arrival during day mode
10. **PhoneArrivesLate** - Phone arrival during night/evening/morning
11. **MarjiPhoneHome** - Marji's phone arrival detection
12. **MarjisPhoneAway** - Marji's phone departure detection

### Grace Period Rule
13. **ArriveGraceTurnsOn** - Arrival grace period management (absorbed from separate app)

## Configuration

### Motion Sensors by Zone
Configure motion sensors for different zones around your property:
- **Carport Motion Sensor** - Main carport area
- **Front Door Motion Sensor** - Front entrance area
- **Side Yard Motion (AMC)** - Side yard monitoring
- **RV Motion Sensor** - RV parking area
- **Office Motion Sensor** - Office/workspace area
- **Rear Carport Motion Sensor** - Back carport area
- **Carport Front Motion Sensor** - Front carport entrance

### Presence Sensors
Configure phone presence detection:
- **Phone Presence Sensor** - Primary phone for arrival/departure tracking
- **Marji's Phone Presence** - Secondary phone presence monitoring

### Arrival Grace Period
Prevent false alarms when arriving home:
- **Arrive Grace Period Switch** - Virtual switch to activate grace period
- **Grace Period Duration** - How long to suppress alarms (default: 30 minutes)

### Control Switches
Switches used for automation control:
- **Alarms Enabled Switch** - Controls alarm system state
- **Silent Mode Switch** - Enables silent mode during grace period
- **Rear Carport Active Switch** - Indicates recent rear carport motion

### Notifications
- **Notification Devices** - Devices to receive motion and presence notifications

### Motion Configuration
Fine-tune motion detection behavior:
- **Motion Detection Timeout** - How long to wait before resetting motion state (default: 60 seconds)
- **Enable Day Mode Motion Detection** - Process motion events during day modes (default: true)
- **Enable Night Mode Motion Detection** - Process motion events during night mode (default: false)

### Presence Configuration
Control presence notifications:
- **Send Arrival Notifications** - Notify when phones arrive (default: true)
- **Send Departure Notifications** - Notify when phones depart (default: false)

## Hub Variable Support
The app supports hub variable overrides for flexible, centralized configuration:

- **gracePeriodDuration** (Number) - Override grace period duration in minutes
- **motionTimeout** (Number) - Override motion sensor timeout in seconds
- **enableDayMotion** (Boolean) - Enable/disable day motion detection
- **enableNightMotion** (Boolean) - Enable/disable night motion detection
- **arrivalNotifications** (Boolean) - Enable/disable arrival notifications

Hub variables take precedence over app settings when present.

## Behavior Details

### Motion Detection by Zone

**Carport Motion:**
- Sends notification when motion detected in Day/Morning/Evening modes
- Respects mode-based motion processing settings
- Uses configurable timeout for motion reset

**Front Door Motion:**
- Day mode only by default
- Sends notification for daytime motion detection
- Useful for monitoring front entrance activity

**Side Yard Motion (AMC):**
- All modes when enabled
- Sends immediate notification
- Monitors area near air conditioning units

**RV Motion:**
- All modes when enabled
- Sends notification when motion detected
- Monitors RV parking area

**Office Motion:**
- Primarily for desk light control (handled by LightsAutomationManager)
- Logged but no direct action in this app
- Motion state available to other apps

**Rear Carport Motion:**
- Activates "Rear Carport Active" switch
- Sends notification
- Auto-resets switch after configured timeout
- Useful for triggering other automations

**Carport Front Motion:**
- Logged for tracking purposes
- Available for integration with other apps

### Presence Detection

**Phone Arrival:**
- Day Mode: "Phone has arrived (Day)" notification
- Night/Evening/Morning: "Phone has arrived (Late)" notification
- Automatically triggers arrival grace period if configured
- Prevents false alarms when arriving home

**Phone Departure:**
- Optional notification (disabled by default)
- "Phone has left" message when enabled
- Can trigger away mode automations

**Marji's Phone:**
- Arrival: "Marji is home" notification
- Departure: "Marji has left" notification
- Independent tracking for multi-person households

### Arrival Grace Period

When the Arrive Grace Period Switch is turned on (automatically by phone arrival or manually):

1. **Disables Alarms** - Turns off "Alarms Enabled" switch
2. **Enables Silent Mode** - Turns on "Silent Mode" switch
3. **Starts Timer** - Begins countdown based on configured duration (default: 30 minutes)
4. **Sends Notification** - "Arrival grace period started (X minutes)"

After the grace period expires:

1. **Re-enables Alarms** - Turns on "Alarms Enabled" switch
2. **Disables Silent Mode** - Turns off "Silent Mode" switch
3. **Resets Switch** - Turns off "Arrive Grace Period" switch
4. **Sends Notification** - "Arrival grace period ended - alarms restored"

This prevents false alarms from security sensors when you first arrive home, giving you time to disarm systems or settle in before full monitoring resumes.

## Integration with Other Apps

### LightsAutomationManager
- Shares office motion sensor for desk light control
- Coordinates carport lighting with motion detection
- Emergency lighting triggered by presence changes

### NightSecurityManager
- Motion detection disabled during night mode by default
- Can be enabled via hub variable for enhanced security
- Prevents duplicate notifications

### SecurityAlarmManager
- Coordinated through Alarms Enabled switch
- Grace period prevents false alarms on arrival
- Silent mode integration for quiet periods

### DoorWindowMonitor
- Complementary monitoring systems
- Both apps can share notification devices
- Coordinated security coverage

## Usage Examples

### Example 1: Basic Motion Monitoring
```
Configuration:
- Carport Motion: Enabled
- Front Door Motion: Enabled
- Enable Day Mode Motion: true
- Enable Night Mode Motion: false

Behavior:
- Daytime motion in carport → Notification sent
- Nighttime motion in carport → No action (night mode disabled)
- Motion at front door during day → Notification sent
```

### Example 2: Arrival Grace Period
```
Configuration:
- Phone Presence: Your phone
- Arrive Grace Period Switch: Virtual switch "ArriveGracePeriod"
- Grace Duration: 30 minutes
- Alarms Enabled: Virtual switch "AlarmsEnabled"
- Silent Mode: Virtual switch "Silent"

Behavior:
1. You arrive home → Phone presence changes to "present"
2. App activates grace period:
   - ArriveGracePeriod switch turns ON
   - AlarmsEnabled switch turns OFF
   - Silent switch turns ON
   - Notification: "Arrival grace period started (30 minutes)"
3. After 30 minutes:
   - AlarmsEnabled switch turns ON
   - Silent switch turns OFF
   - ArriveGracePeriod switch turns OFF
   - Notification: "Arrival grace period ended - alarms restored"
```

### Example 3: Rear Carport Motion with Auto-Reset
```
Configuration:
- Rear Carport Motion: Enabled
- Rear Carport Active Switch: Virtual switch
- Motion Timeout: 60 seconds

Behavior:
1. Motion detected in rear carport
2. "Rear Carport Active" switch turns ON
3. Notification sent: "Motion detected in rear carport"
4. After 60 seconds → "Rear Carport Active" switch turns OFF
```

### Example 4: Multi-Person Presence Tracking
```
Configuration:
- Phone Presence: Your phone
- Marji's Phone Presence: Marji's phone
- Arrival Notifications: Enabled
- Departure Notifications: Enabled

Behavior:
- Your phone arrives → "Phone has arrived (Day)" + grace period activated
- Marji's phone arrives → "Marji is home"
- Your phone leaves → "Phone has left"
- Marji's phone leaves → "Marji has left"
```

### Example 5: Hub Variable Overrides
```
Hub Variables:
- gracePeriodDuration = 45 (Number)
- enableNightMotion = true (Boolean)
- arrivalNotifications = true (Boolean)

App Settings:
- Grace Duration: 30 minutes (ignored - hub var used)
- Enable Night Motion: false (ignored - hub var used)
- Arrival Notifications: true (matches hub var)

Behavior:
- Grace period runs for 45 minutes (hub variable)
- Night motion detection enabled (hub variable)
- Arrival notifications enabled (consistent)
```

## Troubleshooting

### Motion Not Detected
1. Verify motion sensor is configured correctly
2. Check if mode-based motion processing is enabled
3. Confirm motion sensor is working (check events log)
4. Verify notification devices are configured

### Grace Period Not Activating
1. Confirm "Arrive Grace Period Switch" is configured
2. Verify phone presence sensor is working
3. Check that arrival actually triggers presence change
4. Review logs for grace period activation messages

### Notifications Not Sending
1. Verify notification devices are configured
2. Check device compatibility (must support deviceNotification)
3. Confirm notifications are enabled for specific events
4. Review app logs for notification attempts

### Grace Period Not Ending
1. Check scheduled jobs in hub
2. Verify switches are responsive
3. Review logs for "endGracePeriod" execution
4. Consider manual reset if needed

### Hub Variables Not Working
1. Verify hub variable name matches exactly (case-sensitive)
2. Confirm hub variable type matches expected type
3. Check hub variable value is set correctly
4. Review debug logs showing which values are used

## Advanced Customization

### Adding New Motion Zones
To add a new motion zone:
1. Add new motion sensor input in preferences
2. Create new handler method (e.g., `handleNewZoneMotion`)
3. Subscribe to sensor in `initialize()`
4. Implement desired behavior in handler
5. Update documentation

### Customizing Grace Period Behavior
The grace period can be customized:
- Adjust duration via hub variable or setting
- Modify switches affected in `handleGracePeriodActivation()`
- Add additional actions in `endGracePeriod()`
- Create different grace periods for different scenarios

### Mode-Based Motion Processing
Customize which modes allow motion detection:
- Use hub variables for dynamic control
- Modify `shouldProcessMotion()` logic
- Add time-based conditions
- Integrate with other automation systems

## Logging
- **Debug Logging** - Detailed operation information, sensor events, switch operations
- **Info Logging** - High-level events, arrivals/departures, grace period state changes

Enable debug logging for troubleshooting, disable for normal operation.

## Performance Notes
- All motion events processed immediately
- Grace period uses single scheduled job
- Rear carport auto-reset uses individual scheduled job
- Minimal hub resource usage
- No polling - event-driven architecture

## Version History
- **1.0** (2025-01-XX) - Initial release
  - Consolidated 13 motion/presence rules
  - Absorbed ArriveGraceTurnsOn app functionality
  - Added hub variable support
  - Implemented zone-based motion detection
  - Added multi-person presence tracking
