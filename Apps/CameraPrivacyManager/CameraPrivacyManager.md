# Camera Privacy Manager

## Overview
Camera Privacy Manager automatically controls indoor and outdoor camera power based on presence modes, providing privacy when home and security when away.

## Purpose
- Automatically turn off indoor cameras when home for privacy
- Enable cameras when away for security monitoring
- Support manual override for temporary control
- Configurable delays for smooth transitions
- Hub variable support for dynamic configuration

## Rules Consolidated
This app replaces the following Rule Machine rules:
1. **IndoorCamsOff** (901) - Turn off indoor cameras in home modes
2. **IndoorCamsOn** (900) - Turn on cameras in away modes

**Total Rules Replaced**: 2

## Features

### Core Functionality
- **Mode-Based Control**: Cameras respond to mode changes
- **Privacy Protection**: Indoor cameras off when home
- **Security Enhancement**: All cameras on when away
- **Delayed Activation**: Configurable delays prevent false triggers
- **Manual Override**: Temporary manual control with auto-revert
- **Separate Indoor/Outdoor**: Different control for indoor vs outdoor cameras

### Hub Variables Support
The app supports these hub variables for dynamic configuration:
- `privacyModeDelay` - Override camera off delay (minutes)
- `enableDelay` - Override camera on delay (minutes)
- `manualOverrideDuration` - Override manual override timeout (hours)

If hub variables are not set, the app uses the configured settings as defaults.

## Installation

### Prerequisites
1. Indoor camera power switches (required)
2. Outdoor camera power switches (optional)
3. Mode configuration for home/away states
4. Manual override switch (optional)

### Hub Variables (Optional)
Create these hub variables in Settings → Hub Variables for dynamic control:

```
Name: privacyModeDelay
Type: number
Initial Value: 2

Name: enableDelay
Type: number
Initial Value: 1

Name: manualOverrideDuration
Type: number
Initial Value: 4
```

### Installation Steps
1. Add the app code to Apps Code in Hubitat
2. Click "Save"
3. Go to Apps → Add User App
4. Select "Camera Privacy Manager"
5. Configure settings (see Configuration Guide below)
6. Click "Done"

## Configuration Guide

### Indoor Cameras
- **Indoor Camera Power Outlets**: Select all switches that control indoor camera power
- These cameras will turn OFF in privacy modes

### Outdoor Cameras (Optional)
- **Outdoor Camera Power Outlets**: Select switches for outdoor cameras
- These cameras remain ON for perimeter security

### Privacy Mode Settings
- **Privacy Modes (Cameras Off)**: Select modes when indoor cameras should be OFF (e.g., Home, Day, Evening, Night)
- **Security Modes (Cameras On)**: Select modes when all cameras should be ON (e.g., Away, Vacation)
- **Privacy Mode Delay**: Minutes to wait before turning cameras off when arriving home (default: 2)
- **Enable Delay**: Minutes to wait before turning cameras on when leaving (default: 1)

### Manual Override
- **Manual Override Switch**: Optional switch for manual control
  - ON = Force cameras off regardless of mode
  - OFF = Return to automatic mode-based control
- **Manual Override Duration**: Hours before auto-reverting to automatic (default: 4)

### Logging
- **Enable Debug Logging**: Turn on for detailed logs during setup/troubleshooting

## Usage Examples

### Example 1: Basic Home/Away Control
**Scenario**: Turn off indoor cameras when home, on when away

**Configuration**:
- Privacy Modes: Home, Day, Evening, Night
- Security Modes: Away, Vacation
- Privacy Delay: 2 minutes
- Enable Delay: 1 minute

**Behavior**:
1. Mode changes to "Away" → Wait 1 minute → Turn all cameras ON
2. Mode changes to "Home" → Wait 2 minutes → Turn indoor cameras OFF (outdoor stay ON)

### Example 2: Manual Privacy Override
**Scenario**: Temporarily disable cameras for a guest visit

**Configuration**:
- Manual Override Switch: Virtual Switch "Camera Override"
- Override Duration: 4 hours

**Behavior**:
1. Turn on "Camera Override" switch
2. All indoor cameras immediately turn OFF
3. After 4 hours, switch auto-resets and cameras return to mode-based control

### Example 3: Hub Variable Dynamic Control
**Scenario**: Adjust delays remotely without app reconfiguration

**Setup**:
1. Create hub variable `privacyModeDelay` = 5
2. Create hub variable `enableDelay` = 3

**Behavior**:
- App reads hub variables first
- If not set, uses app settings
- Change hub variable value → Takes effect immediately
- No app reconfiguration needed

## Troubleshooting

### Cameras Not Turning Off
1. Check that current mode is in "Privacy Modes" list
2. Wait for privacy delay to expire
3. Check manual override switch is OFF
4. Enable debug logging and check logs

### Cameras Not Turning On
1. Check that current mode is in "Security Modes" list
2. Wait for enable delay to expire
3. Verify camera power switches are working
4. Check hub events for switch commands

### Manual Override Not Working
1. Verify manual override switch is configured
2. Check switch events in device page
3. Enable debug logging
4. Verify override duration setting

### Hub Variables Not Working
1. Verify hub variable names match exactly (case-sensitive)
2. Check hub variable type (should be "number" for delays)
3. Enable debug logging to see which values are being used
4. Hub variables override app settings when present

## Technical Details

### App Behavior
- **Single Threaded**: Uses `singleThreaded: true` for simpler state management
- **Event-Driven**: Responds to mode changes and switch events
- **Scheduled Jobs**: Uses delays for smooth transitions
- **Persistent State**: No state required, drives entirely from mode and settings

### Mode Change Logic
```
Mode Change → Check if Home or Away Mode
  ↓
Home Mode → Schedule camerasOff() after privacy delay
  ↓
Away Mode → Schedule camerasOn() after enable delay
  ↓
Other Modes → No action (cameras maintain current state)
```

### Manual Override Logic
```
Override Switch ON → Immediate camerasOff()
  ↓
Schedule auto-revert after duration
  ↓
Duration Expires → Turn override switch OFF → Return to mode control
```

## Best Practices

1. **Set Privacy Delay**: Allow time to enter home before cameras turn off
2. **Set Enable Delay**: Ensure you've left before cameras activate
3. **Test Manual Override**: Verify it works before relying on it
4. **Use Hub Variables**: For dynamic control without app updates
5. **Monitor Logs**: Enable debug logging during setup
6. **Separate Indoor/Outdoor**: Keep outdoor cameras for perimeter security

## Version History

### Version 1.0.0 (2025-12-04)
- Initial release
- Mode-based camera control
- Manual override support
- Hub variable integration
- Configurable delays
- Separate indoor/outdoor camera control

## Support

For issues or questions:
1. Enable debug logging
2. Check Hubitat logs for error messages
3. Verify mode configuration
4. Check device capabilities
5. Review hub variable settings

## License

Copyright 2025 Tim Brown

Licensed under the Apache License, Version 2.0
