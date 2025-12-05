# Environmental Control Manager

## Overview
Environmental Control Manager automates temperature control, fans, heaters, and environmental monitoring for greenhouse, office, and outdoor areas, providing intelligent climate management and safety features.

## Purpose
- Automatic temperature-based fan and heater control
- Freeze protection alerts for greenhouse
- Office climate management
- Mode-based mosquito killer control
- Water valve auto-shutoff safety
- Alexa voice control integration
- Hub variable support for dynamic configuration

## Rules Consolidated
This app replaces the following Rule Machine rules:
1. **GreenHouseFanOff** (1029) - Auto fan control
2. **GreenHouseFanOn** (1028) - Auto fan control
3. **GreenhouseFreezeAlarm** (1630) - Freeze protection
4. **GHHeaterOff** (1679) - Auto heater control
5. **GHHeaterOn** (1678) - Auto heater control
6. **OfficeHeaterOff** (1681) - Office temperature control
7. **OfficeHeaterOn** (1680) - Office temperature control
8. **OfficeFansOff** (1643) - Office fan management
9. **SkeeterKillerOff** (1639) - Mosquito control
10. **SkeeterKillerOn** (1638) - Mosquito control
11. **TurnWaterOff** (1648) - Water safety
12. **WaterOffReset** (1649) - Water timeout reset
13. **GreenhouseAlexaToggle** (1787) - Voice control

**Total Rules Replaced**: 13

## Features

### Core Functionality
- **Greenhouse Climate Control**: Automated fan and heater based on temperature
- **Freeze Protection**: Alerts when temperature drops dangerously low
- **Office Heating**: Maintains comfortable office temperature
- **Mosquito Control**: Scheduled mosquito killer operation
- **Water Safety**: Auto-shutoff prevents flooding
- **Alexa Integration**: Voice control for greenhouse systems
- **Temperature Monitoring**: Real-time sensor tracking
- **Smart Hysteresis**: Prevents rapid on/off cycling

### Hub Variables Support
The app supports these hub variables for dynamic configuration:
- `greenhouseFanOnTemp` - Override fan activation temperature (°F)
- `greenhouseFanOffTemp` - Override fan deactivation temperature (°F)
- `greenhouseHeaterOnTemp` - Override heater activation temperature (°F)
- `greenhouseHeaterOffTemp` - Override heater deactivation temperature (°F)
- `freezeAlertThreshold` - Override freeze warning temperature (°F)
- `officeHeaterTemp` - Override office heater temperature (°F)
- `waterTimeout` - Override water shutoff timeout (minutes)

If hub variables are not set, the app uses the configured settings as defaults.

## Installation

### Prerequisites
1. Temperature sensors (greenhouse and/or office)
2. Fan and heater switches
3. Mosquito killer switch (optional)
4. Water valve switch (optional)
5. Notification devices
6. Alexa devices (optional)

### Hub Variables (Optional)
Create these hub variables in Settings → Hub Variables for dynamic control:

```
Name: greenhouseFanOnTemp
Type: decimal
Initial Value: 75.0

Name: greenhouseFanOffTemp
Type: decimal
Initial Value: 70.0

Name: greenhouseHeaterOnTemp
Type: decimal
Initial Value: 40.0

Name: greenhouseHeaterOffTemp
Type: decimal
Initial Value: 45.0

Name: freezeAlertThreshold
Type: decimal
Initial Value: 32.0

Name: officeHeaterTemp
Type: decimal
Initial Value: 68.0

Name: waterTimeout
Type: number
Initial Value: 30
```

### Installation Steps
1. Add the app code to Apps Code in Hubitat
2. Click "Save"
3. Go to Apps → Add User App
4. Select "Environmental Control Manager"
5. Configure settings (see Configuration Guide below)
6. Click "Done"

## Configuration Guide

### Greenhouse Controls
- **Greenhouse Temperature Sensor**: Main temperature sensor
- **Greenhouse Fan**: Fan switch for cooling
- **Greenhouse Heater**: Heater switch for warming
- **Fan On Temperature**: Activate fan when temp rises above (default: 75°F)
- **Fan Off Temperature**: Deactivate fan when temp falls below (default: 70°F)
- **Heater On Temperature**: Activate heater when temp falls below (default: 40°F)
- **Heater Off Temperature**: Deactivate heater when temp rises above (default: 45°F)
- **Freeze Alert Temperature**: Send alert when temp at or below (default: 32°F)
- **Alexa Toggle Switch**: Optional virtual switch for voice control

### Office Controls
- **Office Temperature Sensor**: Office temperature sensor
- **Office Heater**: Office heater switch
- **Office Fans**: Office fan switches (multiple supported)
- **Office Heater Temperature**: Target temperature (default: 68°F)

### Mosquito Control
- **Mosquito Killer Device**: Mosquito killer switch
- **Modes to Turn Skeeter ON**: Hub modes that activate mosquito killer
- **Modes to Turn Skeeter OFF**: Hub modes that deactivate mosquito killer

### Water Control
- **Water Control Valve**: Water valve switch
- **Water Auto-Off Timeout**: Minutes before auto-shutoff (default: 30)
- **Water Reset Switch**: Optional switch to reset timeout

### Notification Settings
- **Notification Devices**: Push notification devices
- **Alexa Devices**: Voice announcement devices

### Logging
- **Enable Debug Logging**: Detailed logs for troubleshooting

## Usage Examples

### Example 1: Greenhouse Climate Control
**Scenario**: Keep greenhouse cool in summer, warm in winter

**Configuration**:
- Greenhouse sensor, fan, heater configured
- Fan On: 75°F, Fan Off: 70°F
- Heater On: 40°F, Heater Off: 45°F
- Freeze Alert: 32°F

**Behavior**:
1. Temperature rises to 75°F → Fan turns ON
2. Temperature cools to 70°F → Fan turns OFF
3. Temperature drops to 40°F → Heater turns ON + alert
4. Temperature warms to 45°F → Heater turns OFF
5. Temperature at/below 32°F → FREEZE ALERT + announcement

### Example 2: Office Comfort Control
**Scenario**: Maintain comfortable office temperature

**Configuration**:
- Office sensor and heater configured
- Target Temperature: 68°F
- Hysteresis: ±2°F (built-in)

**Behavior**:
1. Temperature drops below 66°F → Heater turns ON
2. Temperature rises above 70°F → Heater turns OFF
3. Prevents rapid cycling with hysteresis band

### Example 3: Mode-Based Mosquito Killer
**Scenario**: Run mosquito killer during evening modes

**Configuration**:
- Mosquito killer device configured
- Modes to Turn ON: Evening, Night
- Modes to Turn OFF: Day, Away

**Behavior**:
1. Mode changes to Evening or Night → Mosquito killer turns ON
2. Mode changes to Day or Away → Mosquito killer turns OFF
3. Current mode checked on app startup

### Example 4: Water Safety Auto-Shutoff
**Scenario**: Prevent water valve from being left on

**Configuration**:
- Water valve configured
- Timeout: 30 minutes
- Reset switch configured

**Behavior**:
1. Water valve turned ON → Start 30-minute timer
2. After 30 minutes → Auto-shutoff + alert
3. Press reset switch → Restart timer (if valve still on)

### Example 5: Alexa Greenhouse Control
**Scenario**: Voice control for greenhouse systems

**Configuration**:
- Alexa toggle switch configured
- Alexa devices set up

**Behavior**:
1. Say "Alexa, turn on greenhouse" → Evaluates temperature and controls
2. Say "Alexa, turn off greenhouse" → Turns off fan and heater
3. Temperature changes announce via Alexa

### Example 6: Hub Variable Seasonal Adjustment
**Scenario**: Adjust temperatures for seasons without app changes

**Setup**:
- Summer: Set `greenhouseFanOnTemp` = 80, `greenhouseFanOffTemp` = 75
- Winter: Set `greenhouseFanOnTemp` = 70, `greenhouseFanOffTemp` = 65

**Behavior**:
- App reads hub variables first
- Changes take effect immediately
- No app reconfiguration needed

## Troubleshooting

### Fan/Heater Not Responding
1. Check temperature sensor reading
2. Verify temperature thresholds in settings
3. Check device switch status manually
4. Enable debug logging
5. Verify hub variables (if used)

### Freeze Alert Not Working
1. Check greenhouse temperature sensor
2. Verify freeze alert threshold setting
3. Check notification devices configured
4. Test alert manually

### Mosquito Killer Not Responding to Mode Changes
1. Verify skeeter on/off modes are configured
2. Check current hub mode
3. Verify mosquito killer device switch
4. Enable debug logging to see mode changes
5. Test mode change manually

### Water Not Auto-Shutting Off
1. Verify timeout setting is not 0
2. Check water valve switch subscription
3. Enable debug logging
4. Check scheduled jobs
5. Verify hub variable `waterTimeout` value

### Temperature Cycling Too Frequently
1. Check hysteresis settings (office has built-in ±2°F)
2. Increase gap between on/off temperatures
3. Verify sensor placement (avoid drafts)
4. Check sensor accuracy

## Technical Details

### App Behavior
- **Single Threaded**: Uses `singleThreaded: true` for state management
- **Event-Driven**: Responds to temperature and mode changes immediately
- **Mode-Based Control**: Mosquito killer responds to hub mode changes
- **Hysteresis Control**: Office heater uses ±2°F band to prevent cycling
- **Smart Timers**: Water timeout tracks valve state

### Temperature Control Logic
```
Greenhouse Fan:
  Temperature >= Fan On Temp → Turn Fan ON
  Temperature <= Fan Off Temp → Turn Fan OFF
  
Greenhouse Heater:
  Temperature <= Heater On Temp → Turn Heater ON + Alert
  Temperature >= Heater Off Temp → Turn Heater OFF
  
Freeze Protection:
  Temperature <= Freeze Alert → CRITICAL ALERT + Announcement
  
Office Heater:
  Temperature < (Target - 2°F) → Turn Heater ON
  Temperature > (Target + 2°F) → Turn Heater OFF
```

### Water Safety Logic
```
Valve Turned ON → Start timeout timer
  ↓
Timer Expires → Check valve state
  ↓
If still ON → Turn OFF + Alert
  ↓
Reset Switch → Cancel timer, restart if valve ON
```

## Best Practices

1. **Set Proper Thresholds**: Leave gap between on/off temps to prevent cycling
2. **Use Hub Variables**: Adjust seasonally without app changes
3. **Monitor Initially**: Enable debug logging during setup
4. **Test Freeze Alerts**: Verify notification system works
5. **Sensor Placement**: Avoid direct sun, drafts, heat sources
6. **Water Safety**: Always configure timeout for water valves
7. **Alexa Names**: Use clear, distinct names for voice control
8. **Battery Maintenance**: Replace sensor batteries proactively

## Version History

### Version 1.1.0 (2025-12-05)
- Changed mosquito killer from time-based to mode-based control
- Removed SkeeterOnTime and SkeeterOffTime hub variables
- Added mode change event subscription
- Improved mosquito killer responsiveness

### Version 1.0.0 (2025-12-04)
- Initial release
- Greenhouse fan and heater control
- Freeze protection alerts
- Office climate management
- Mosquito killer time-based scheduling
- Water valve auto-shutoff
- Alexa voice control integration
- Hub variable support for all settings
- Smart hysteresis to prevent cycling

## Support

For issues or questions:
1. Enable debug logging
2. Check temperature sensor readings
3. Verify switch device status
4. Review threshold settings
5. Check hub variable values
6. Test devices manually
7. Review Hubitat logs

## License

Copyright 2025 Tim Brown

Licensed under the Apache License, Version 2.0
