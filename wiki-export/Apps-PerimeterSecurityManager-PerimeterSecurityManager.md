# Perimeter Security Manager

## Overview
Perimeter Security Manager monitors all gates, fences, shock sensors, carport beam, and perimeter security devices, providing comprehensive property boundary protection with Ring person detection integration.

## Purpose
- Monitor gate sensors for open/close status
- Detect shock events on fences and gates
- Monitor carport beam for intrusion detection
- Integrate Ring person detection cameras
- Provide mode-based security responses
- Alert on gun cabinet access
- Scheduled perimeter status checks

## Rules Consolidated
This app replaces the following Rule Machine rules:
1. **FrontGateActive** (1667) - Front gate monitoring
2. **RearGateActivity** (837) - Rear gate monitoring
3. **RearGateActiveAway** (1026) - Enhanced away mode alerts
4. **RearGateOutsidePenActive** (1192) - Outside pen motion
5. **RearGateShockActive** (1195) - Shock sensor detection
6. **SideYardGateActive** (1602) - Side yard gate monitoring
7. **GunCabinet** (1636) - Gun cabinet sensor
8. **RPDBackDoor** (1687) - Ring person detection at back door
9. **RPDBirdHouse** (1677) - Ring person detection at bird house
10. **RPDCPen** (1632) - Ring person detection at chicken pen
11. **RPDFrontDoor** (1604) - Ring person detection at front door
12. **RPDGarden** (1703) - Ring person detection at garden
13. **EveningRPDGarden** (1702) - Evening-specific garden detection
14. **CarportBeamDay** (686) - Carport beam daytime detection
15. **CarportBeamEvening** (1575) - Carport beam evening detection
16. **CarportBeamMorning** (1573) - Carport beam morning detection

**Total Rules Replaced**: 16

## Features

### Core Functionality
- **Gate Monitoring**: Track all property gates with configurable delays
- **Shock Detection**: Tamper and impact detection on fences
- **Carport Beam**: Infrared beam intrusion detection with mode-specific responses
- **Ring Integration**: Person detection from Ring cameras
- **Mode-Based Alerts**: Enhanced security in away modes
- **Gun Cabinet Monitoring**: Alerts on cabinet access
- **Perimeter Checks**: Scheduled status verification
- **Dual Notification**: Push notifications and Alexa announcements

### Hub Variables Support
The app supports these hub variables for dynamic configuration:
- `gateAlertDelay` - Override gate alert delay (seconds)
- `shockSensitivity` - Override shock sensitivity (1-10)
- `perimeterCheckInterval` - Override check interval (minutes)
- `awayModeAlertEnabled` - Enable/disable away alerts (true/false)
- `ringPersonTimeout` - Override Ring timeout (seconds)
- `gunCabinetAlertEnabled` - Enable/disable cabinet alerts (true/false)
- `CarportBeamPauseDuration` - Override carport beam pause duration (seconds)
- `SilentCarportTimeout` - Override silent carport timeout (seconds)

If hub variables are not set, the app uses the configured settings as defaults.

## Installation

### Prerequisites
1. Gate contact sensors (any combination of front/rear/side yard)
2. Shock sensors (optional)
3. Ring person detection devices (optional)
4. Notification devices
5. Alexa devices for voice alerts (optional)

### Hub Variables (Optional)
Create these hub variables in Settings → Hub Variables for dynamic control:

```
Name: gateAlertDelay
Type: number
Initial Value: 30

Name: shockSensitivity
Type: number
Initial Value: 5

Name: perimeterCheckInterval
Type: number
Initial Value: 15

Name: awayModeAlertEnabled
Type: boolean
Initial Value: true

Name: ringPersonTimeout
Type: number
Initial Value: 30

Name: gunCabinetAlertEnabled
Type: boolean
Initial Value: true
```

### Installation Steps
1. Add the app code to Apps Code in Hubitat
2. Click "Save"
3. Go to Apps → Add User App
4. Select "Perimeter Security Manager"
5. Configure settings (see Configuration Guide below)
6. Click "Done"

## Configuration Guide

### Gate Sensors
- **Front Gate Sensor**: Contact sensor on front gate
- **Rear Gate Sensor**: Contact sensor on rear gate
- **Side Yard Gate Sensor**: Contact sensor on side yard gate
- **Gate Alert Delay**: Seconds to wait before alerting on open gate (default: 30)
  - Prevents alerts for brief openings
  - Immediate alert in away modes

### Shock & Tamper Sensors
- **Rear Gate Shock Sensor**: Detects impacts/tampering
- **Shock Sensitivity**: 1-10 scale, higher = more sensitive (default: 5)
  - Alerts trigger when sensitivity ≥ 5

### Additional Perimeter Sensors
- **Outside Pen Motion Sensor**: Motion detection in pen area
- **Gun Cabinet Sensor**: Contact sensor on gun cabinet

### Carport Beam
- **Carport Beam Sensor**: Infrared beam contact sensor (closed = beam broken)
- **Carport Front Motion Sensor**: Motion sensor for beam break verification
- **Front Door Ring Motion (Switch)**: Ring doorbell motion as verification source

### Carport Beam Condition Switches
- **Silent Switch**: Global silent mode - suppresses all carport beam alerts
- **Silent Carport Switch**: Carport-specific silent mode
- **Pause Carport Beam Switch**: Temporarily disable beam alerts (auto-managed)

### Carport Beam Timing
- **Carport Beam Pause Duration**: Seconds to pause after beam break in Day mode (default: 300)
- **Silent Carport Auto-Off**: Seconds before silent carport resets in Morning mode (default: 120)

### Ring Person Detection
Ring Person Detection (RPD) uses virtual switches that are triggered through an integration chain:

**How it works:**
1. Ring camera detects a person
2. Ring notifies Alexa via the Ring-Alexa integration
3. Alexa routine triggers the corresponding virtual switch in Hubitat
4. This app subscribes to the switch turning on and sends alerts

**Configuration - Virtual Switches:**
- **RPD Front Door Switch**: Triggered when person detected at front door
- **RPD Back Door Switch**: Triggered when person detected at back door
- **RPD Bird House Switch**: Triggered when person detected at bird house area
- **RPD Chicken Pen Switch**: Triggered when person detected at chicken pen
- **RPD Garden Switch**: Triggered when person detected in garden

**Timing:**
- **Ring Person Detection Timeout**: Seconds to wait for person detection (default: 30)

### Mode-Based Behavior
- **Away Modes**: Modes triggering enhanced security (e.g., Away, Vacation)
  - Immediate gate alerts
  - Enhanced motion sensitivity
- **Evening Mode**: Mode for evening-specific Ring alerts

### Notification Settings
- **Notification Devices**: Push notification devices
- **Alexa Devices**: Voice announcement devices

### Monitoring
- **Perimeter Check Interval**: Minutes between status checks (0 = disabled)
  - Periodic verification of gate states
  - Only alerts in away modes

### Logging
- **Enable Debug Logging**: Detailed logs for troubleshooting

## Usage Examples

### Example 1: Basic Gate Monitoring
**Scenario**: Alert if any gate remains open

**Configuration**:
- Front/Rear/Side Yard Gate Sensors configured
- Gate Alert Delay: 30 seconds
- Notification device selected

**Behavior**:
1. Gate opens → Start 30-second timer
2. Gate still open after 30s → Send alert
3. Gate closes before timer → Cancel alert

### Example 2: Away Mode Enhanced Security
**Scenario**: Immediate alerts when away

**Configuration**:
- Away Modes: Away, Vacation
- All sensors configured
- Alexa announcements enabled

**Behavior**:
1. Mode = Away
2. Any gate opens → Immediate alert + announcement
3. Motion detected → Alert + announcement
4. Shock detected → Alert + announcement

### Example 3: Ring Person Detection
**Scenario**: Announce visitors at different locations

**Configuration**:
- Ring devices at front door, back door, garden
- Evening Mode configured
- Alexa devices selected

**Behavior**:
1. Person at front door → "Person detected at front door"
2. Person at garden in evening → Special evening alert
3. Person at other locations → Standard alert

### Example 4: Scheduled Perimeter Checks
**Scenario**: Verify gates every 15 minutes when away

**Configuration**:
- Perimeter Check Interval: 15 minutes
- Away Modes: Away, Vacation

**Behavior**:
1. Every 15 minutes → Check all gate states
2. If away mode + gates open → Alert with list
3. If home mode → No alert, just log

### Example 5: Carport Beam Detection
**Scenario**: Mode-aware intrusion detection at carport

**Configuration**:
- Carport Beam Sensor: Infrared beam
- Carport Front Motion Sensor: For verification
- Silent switches configured
- Notification device selected

**Behavior by Mode**:

**Away Mode**:
1. Beam broken + motion detected → "Alert: Carport Beam Broken (Away Mode)"
2. Beam broken without motion → No alert (reduces false positives)

**Day Mode**:
1. Beam broken + motion detected → "Carport Beam Broken"
2. Pause switch activates for 5 minutes (prevents repeated alerts)
3. Respects silent/pause switches

**Evening Mode**:
1. Beam broken → "Carport Beam Broken" + Alexa announcement
2. Respects silent switch

**Morning Mode**:
1. Beam broken → "Intruder in the carport!" (urgent message)
2. Silent carport activates for 2 minutes
3. Alexa announcement: "Alert! Intruder in the carport!"

## Troubleshooting

### Gates Not Being Monitored
1. Verify contact sensors are paired
2. Check sensor battery levels
3. Test sensor by opening/closing manually
4. Enable debug logging
5. Check subscriptions initialized correctly

### Shock Sensor Too Sensitive
1. Reduce shock sensitivity setting (lower number)
2. Check sensor mounting (ensure stable)
3. Monitor debug logs for false triggers
4. Adjust hub variable `shockSensitivity`

### Ring Alerts Not Working
1. Verify Ring device type matches configuration
2. Check Ring device is online
3. Test Ring motion detection manually
4. Verify person detection is enabled on Ring

### No Alerts in Away Mode
1. Check current mode matches "Away Modes" setting
2. Verify notification devices are working
3. Check hub variable `awayModeAlertEnabled` = true
4. Enable debug logging

### Perimeter Checks Not Running
1. Verify interval > 0
2. Check scheduled jobs in app
3. Hub reboot may have cleared schedule
4. Update app to reinitialize schedule

## Technical Details

### App Behavior
- **Single Threaded**: Uses `singleThreaded: true` for simpler state management
- **Event-Driven**: Responds to sensor events immediately
- **Scheduled Checks**: Optional periodic perimeter verification
- **Mode-Aware**: Different behavior based on location mode

### Alert Priority
```
High Priority (Immediate):
- Away mode gate opening
- Shock detection (sensitivity ≥ 5)
- Gun cabinet opening
- Motion in away mode

Medium Priority (Delayed):
- Home mode gate opening (after delay)
- Ring person detection

Low Priority (Informational):
- Gate closing events
- Perimeter check results
```

### Ring Integration
The app integrates with Ring Virtual Motion Sensor devices:
- Listens for motion.active events
- Person detection triggers alerts
- Location-specific messaging
- Mode-based response variations

## Best Practices

1. **Set Appropriate Delays**: Balance between nuisance and security
2. **Test Shock Sensitivity**: Adjust to eliminate false positives
3. **Use Away Modes**: Enable enhanced security when away
4. **Schedule Checks**: Periodic verification catches stuck sensors
5. **Monitor Logs**: Check regularly during initial setup
6. **Battery Maintenance**: Replace sensor batteries proactively
7. **Hub Variables**: Use for seasonal adjustments (sensitivity, delays)

## Version History

### Version 1.0.0 (2025-12-04)
- Initial release
- Gate monitoring with configurable delays
- Shock sensor integration
- Ring person detection support
- Mode-based security responses
- Gun cabinet monitoring
- Scheduled perimeter checks
- Hub variable integration
- Dual notification system (push + Alexa)

## Support

For issues or questions:
1. Enable debug logging
2. Check Hubitat logs for sensor events
3. Verify sensor battery levels
4. Test sensors manually
5. Review mode configuration
6. Check hub variable settings

## License

Copyright 2025 Tim Brown

Licensed under the Apache License, Version 2.0
