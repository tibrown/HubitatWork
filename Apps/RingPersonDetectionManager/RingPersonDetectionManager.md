# Ring Person Detection Manager

## Overview
The Ring Person Detection Manager is a comprehensive Hubitat app that centralizes all Ring doorbell and camera motion/person detection logic. It provides intelligent monitoring, notification management, and integration with the broader home security system.

## Purpose
- Centralize Ring device motion and person detection
- Provide location-based security responses
- Intelligent notification management with cooldown periods
- Integration with night security and alarm systems
- Eliminate notification spam through debouncing

## Rules Consolidated
This app consolidates **7 rules**:

| Rule Name | Rule ID | Function |
|-----------|---------|----------|
| RingBackdoorMotionReset | 1725 | Auto-reset backdoor motion detection |
| RingMotionBackdoor | 1693 | Handle backdoor motion events |
| TurnRingMotionOff | 1190 | Reset motion detection states |
| RingPersonBirdHouse | - | Person detection at birdhouse camera |
| RingPersonDetected | - | General person detection handling |
| RingPersonRearDetected | - | Person detection at rear locations |
| RingPersonPen | - | Person detection at pen camera |

## Features

### Motion Detection
- **Automatic Reset**: Configurable auto-reset for motion sensors
- **Debouncing**: Prevents multiple triggers from same event
- **Per-Location Tracking**: Independent state for each camera
- **Configurable Delays**: Customize reset timing per use case

### Person Detection
- **Location-Based Responses**: Different actions for different camera locations
- **Critical Location Identification**: Front door and backdoor treated as high-priority
- **Night Mode Enhancement**: Elevated security during night modes
- **Cooldown Periods**: Prevents notification spam

### Notification Management
- **Multi-Channel**: Push notifications and Alexa announcements
- **Silent Mode Support**: Respects silent mode switch
- **Delayed Notifications**: Optional delay before sending alerts
- **Smart Cooldown**: Per-location cooldown to prevent spam

### Security Integration
- **Night Security Alerts**: Triggers broader night security actions
- **Alarm Integration**: Direct alarm triggering for critical detections
- **Mode-Aware**: Different behaviors based on house mode
- **Auto-Reset Switches**: Automatic cleanup of trigger switches

## Configuration

### Ring Devices
Configure Ring cameras/doorbells for each monitored location:
- **Ring Backdoor Camera**: Backdoor monitoring
- **Ring Birdhouse Camera**: Birdhouse area monitoring
- **Ring Rear Gate Camera**: Rear gate monitoring
- **Ring Pen Camera**: Animal pen monitoring
- **Ring Garden Camera**: Garden area monitoring
- **Ring Front Door Camera**: Front entrance monitoring

### Motion Detection Settings
- **Motion Auto-Reset Delay**: Time before motion state is cleared (10-600 seconds)
  - Default: 60 seconds
  - Hub Variable: `motionResetDelay`
- **Enable Automatic Motion Reset**: Toggle auto-reset feature
  - Default: true

### Person Detection Settings
- **Person Detection Timeout**: Duration person detection stays active (30-600 seconds)
  - Default: 120 seconds
  - Hub Variable: `personDetectionTimeout`
- **Notification Delay**: Wait time before sending notifications (0-60 seconds)
  - Default: 5 seconds
  - Hub Variable: `notificationDelay`
- **Notification Cooldown Period**: Minimum time between notifications (1-60 minutes)
  - Default: 5 minutes
  - Hub Variable: `cooldownPeriod`
- **Detection Sensitivity Level**: Sensitivity for detection logic (1-10)
  - Default: 5
  - Hub Variable: `sensitivityLevel`

### Night Mode Settings
- **Enable Night Mode Detection**: Toggle enhanced night security
  - Default: true
  - Hub Variable: `nightModeEnabled` (true/false)
- **Night Modes**: Select which modes are considered "night"

### Notification Settings
- **Push Notification Devices**: Devices for push notifications
- **Alexa Device**: Device for voice announcements
- **Silent Mode Switch**: Disables audible alerts when on

### Security Integration
- **Night Security Alert Switch**: Triggers night security actions
- **Alarm Trigger Switch**: Triggers alarm system for critical events

## Hub Variable Support

The app supports the following hub variables for dynamic configuration:

| Hub Variable | Type | Description | Default |
|--------------|------|-------------|---------|
| `motionResetDelay` | Number | Motion auto-reset delay (seconds) | 60 |
| `personDetectionTimeout` | Number | Person detection timeout (seconds) | 120 |
| `notificationDelay` | Number | Delay before notifications (seconds) | 5 |
| `cooldownPeriod` | Number | Notification cooldown (minutes) | 5 |
| `sensitivityLevel` | Number | Detection sensitivity (1-10) | 5 |
| `nightModeEnabled` | Boolean | Enable night mode detection | true |

**Hub Variable Priority**: Hub variables take precedence over app settings. If a hub variable is not set, the app falls back to the configured setting value.

## Logic Flow

### Motion Detection Flow
```
1. Ring camera detects motion
2. Check if motion is "active"
3. Log detection and store timestamp
4. If auto-reset enabled:
   - Schedule reset based on motionResetDelay
   - Reset clears state and logs completion
```

### Person Detection Flow
```
1. Ring camera detects person
2. Check if detection is "detected" state
3. Check cooldown period for this location
4. If within cooldown → Skip notification
5. If past cooldown:
   - Store detection timestamp
   - Determine if night mode active
   - Build notification message
   - If notification delay > 0 → Schedule notification
   - Else → Send immediately
6. Send push notifications
7. If not silent → Alexa announcement
8. If critical location + night mode:
   - Trigger night security alert
   - If front/back door → Trigger alarm
```

### Cooldown Logic
```
Per-location cooldown prevents spam:
- Each location tracks last detection time
- Compare current time to last detection
- If elapsed < cooldown period → Skip
- If elapsed >= cooldown period → Allow
```

## Integration Points

### Outbound (This App Calls)
- **Night Security Alert Switch**: Activates when person detected at critical location during night
- **Alarm Trigger Switch**: Activates for front/back door detection at night
- **Push Notification Devices**: Sends alerts
- **Alexa Device**: Voice announcements

### Inbound (Other Apps Can Monitor)
None - this app responds to Ring device events only

## Code Structure

### Key Methods
- `handleMotion(evt, location, device)`: Process motion events
- `resetMotion(data)`: Auto-reset motion state
- `handlePerson(evt, location, device, critical)`: Process person detection
- `sendPersonNotification(data)`: Send notifications with context
- `shouldNotify(location)`: Check cooldown period
- `isNightMode()`: Determine if in night mode
- `triggerNightSecurity(location)`: Activate night security
- `triggerAlarm(location)`: Activate alarm system
- `getConfigValue(settingName, hubVarName)`: Get value from hub variable or setting

### State Variables
- `lastMotion_[Location]`: Timestamp of last motion at each location
- `lastPerson_[Location]`: Timestamp of last person detection at each location

## Installation

1. **Create Hub Variables** (optional, for dynamic configuration):
   ```
   Settings → Hub Variables → Add Variable
   - motionResetDelay (Number): 60
   - personDetectionTimeout (Number): 120
   - notificationDelay (Number): 5
   - cooldownPeriod (Number): 5
   - sensitivityLevel (Number): 5
   - nightModeEnabled (Boolean): true
   ```

2. **Create Connector Switches** (for security integration):
   ```
   Devices → Add Virtual Device → Connector Switch
   - NightSecurityAlert
   - AlarmTrigger
   ```

3. **Install App**:
   - Apps → Add User App → Ring Person Detection Manager
   - Configure all Ring devices
   - Set notification preferences
   - Configure security integration switches
   - Set logging level (recommend "Info" initially)

4. **Test**:
   - Trigger motion on each Ring device
   - Verify auto-reset works
   - Test person detection
   - Verify cooldown prevents spam
   - Check night mode responses
   - Verify silent mode works

## Testing Checklist

- [ ] Motion detection triggers for each camera
- [ ] Motion auto-reset completes successfully
- [ ] Person detection notifications sent
- [ ] Cooldown period prevents spam
- [ ] Night mode enhances messages
- [ ] Critical locations trigger security alerts
- [ ] Front/back door triggers alarms at night
- [ ] Silent mode suppresses Alexa announcements
- [ ] Hub variables override app settings
- [ ] Push notifications delivered
- [ ] Alexa announcements work (when not silent)
- [ ] Night security switch activates correctly
- [ ] Alarm trigger switch activates correctly

## Maintenance

### Adjusting Sensitivity
- Increase `cooldownPeriod` if too many notifications
- Decrease `notificationDelay` for faster alerts
- Adjust `motionResetDelay` based on camera behavior

### Monitoring
- Check logs for detection patterns
- Review cooldown effectiveness
- Monitor false positive rate
- Verify security integrations trigger correctly

### Troubleshooting
- **No notifications**: Check notification devices configured
- **Too many notifications**: Increase cooldown period
- **Missed detections**: Check Ring device connectivity
- **Silent mode not working**: Verify silent switch configured
- **Alarms not triggering**: Check alarm trigger switch and night mode settings

## Performance Notes
- **Lines of Code**: 437
- **State Variables**: ~12 (2 per location: motion + person)
- **Subscriptions**: 12 (2 per Ring device)
- **Performance**: Lightweight, event-driven
- **Memory**: Minimal state usage

## Future Enhancements
- Multiple notification profiles based on time of day
- Integration with video recording triggers
- Advanced pattern detection (repeated visits)
- Geofencing integration for away vs. home
- Machine learning for false positive reduction
