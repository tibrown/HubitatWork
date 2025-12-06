# Camera Privacy Manager

## Overview
Camera Privacy Manager automatically controls indoor and outdoor camera power based on phone presence, providing privacy when home and security when away.

## Purpose
- Automatically turn off indoor cameras when your phone arrives home
- Enable cameras when your phone leaves for security monitoring
- Support manual override for temporary control
- Configurable delays for smooth transitions
- Hub variable support for dynamic configuration
- Traveling switch prevents cameras when you're away but spouse is home

## Rules Consolidated
This app replaces the following Rule Machine rules:
1. **IndoorCamsOff** (901) - Turn off indoor cameras when home
2. **IndoorCamsOn** (900) - Turn on cameras when away

**Total Rules Replaced**: 2

## Features

### Core Functionality
- **Presence-Based Control**: Cameras respond to phone presence switch changes
- **Privacy Protection**: Indoor cameras off when phone arrives home
- **Security Enhancement**: All cameras on when phone leaves
- **Delayed Activation**: Configurable delays prevent false triggers
- **Manual Override**: Temporary manual control with auto-revert
- **Traveling Mode**: Prevents cameras when you travel but spouse is home
- **Separate Indoor/Outdoor**: Different control for indoor vs outdoor cameras

### Hub Variables Support
The app supports these hub variables for dynamic configuration:
- `PrivacyDelay` - Override camera off delay (minutes)
- `EnableDelay` - Override camera on delay (minutes)
- `ManualOverrideDuration` - Override manual override timeout (hours)

If hub variables are not set, the app uses the configured settings as defaults.

## Installation

### Prerequisites
1. Indoor camera power switches (required)
2. Phone presence switch (required) - typically driven by geofence/Life360
3. Outdoor camera power switches (optional)
4. Manual override switch (optional)
5. Traveling switch (optional)

### Hub Variables (Optional)
Create these hub variables in Settings → Hub Variables for dynamic control:

```
Name: PrivacyDelay
Type: number
Initial Value: 2

Name: EnableDelay
Type: number
Initial Value: 1

Name: ManualOverrideDuration
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
- These cameras will turn OFF when phone arrives

### Outdoor Cameras (Optional)
- **Outdoor Camera Power Outlets**: Select switches for outdoor cameras
- These cameras remain ON for perimeter security

### Presence Detection
- **Phone Presence Switch**: Select your phone's presence switch
  - Switch OFF = Phone has left (away from home)
  - Switch ON = Phone is home
- **Privacy Delay**: Minutes to wait before turning cameras off when arriving home (default: 2)
- **Enable Delay**: Minutes to wait before turning cameras on when leaving (default: 1)

### Override Switches
- **Traveling Switch**: Prevents cameras from turning on when you leave but your spouse is still home
  - ON = You're traveling, spouse is home alone - cameras stay OFF even when your phone leaves
  - OFF = Normal operation - cameras turn on when phone leaves
- **Manual Override Switch**: Optional switch for manual control
  - ON = Force cameras off regardless of presence
  - OFF = Return to automatic presence-based control
- **Manual Override Duration**: Hours before auto-reverting to automatic (default: 4)

### Logging
- **Enable Debug Logging**: Turn on for detailed logs during setup/troubleshooting

## Usage Examples

### Example 1: Basic Presence Control
**Scenario**: Turn off indoor cameras when phone arrives, on when phone leaves

**Configuration**:
- Phone Presence Switch: "Tim's iPhone"
- Privacy Delay: 2 minutes
- Enable Delay: 1 minute

**Behavior**:
1. Phone presence switch turns OFF (you left) → Wait 1 minute → Turn all cameras ON
2. Phone presence switch turns ON (you arrived) → Wait 2 minutes → Turn indoor cameras OFF (outdoor stay ON)

### Example 2: Manual Privacy Override
**Scenario**: Temporarily disable cameras for a guest visit

**Configuration**:
- Manual Override Switch: Virtual Switch "Camera Override"
- Override Duration: 4 hours

**Behavior**:
1. Turn on "Camera Override" switch
2. All indoor cameras immediately turn OFF
3. After 4 hours, switch auto-resets and cameras return to presence-based control

### Example 3: Traveling Mode
**Scenario**: You're traveling for work, spouse is still home

**Configuration**:
- Traveling Switch: Virtual Switch "Traveling"

**Behavior**:
1. You turn ON the "Traveling" switch before leaving
2. Your phone leaves the geofence (phone presence switch turns OFF)
3. Normally cameras would turn ON, but Traveling is ON
4. Cameras stay OFF because spouse is still home and doesn't need indoor surveillance
5. When you return, turn OFF the Traveling switch

**Why this matters**: Without this, when your phone leaves the geofence, the system turns on indoor cameras - but your spouse is still home and doesn't want cameras recording them.

### Example 4: Hub Variable Dynamic Control
**Scenario**: Adjust delays remotely without app reconfiguration

**Setup**:
1. Create hub variable `PrivacyDelay` = 5
2. Create hub variable `EnableDelay` = 3

**Behavior**:
- App reads hub variables first
- If not set, uses app settings
- Change hub variable value → Takes effect immediately
- No app reconfiguration needed

## Troubleshooting

### Cameras Not Turning Off
1. Check that phone presence switch is ON (phone is home)
2. Wait for privacy delay to expire
3. Check manual override switch is OFF
4. Enable debug logging and check logs

### Cameras Not Turning On
1. Check that phone presence switch is OFF (phone has left)
2. Wait for enable delay to expire
3. **Check Traveling switch** - if ON, cameras won't turn on
4. Verify camera power switches are working
5. Check hub events for switch commands

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
- **Event-Driven**: Responds to phone presence switch changes
- **Scheduled Jobs**: Uses delays for smooth transitions
- **Persistent State**: No state required, drives entirely from switch states

### Presence Change Logic
```
Phone Switch OFF (left) → Check Traveling Switch
  ↓
Traveling OFF → Schedule camerasOn() after enable delay
  ↓
Traveling ON → No action (spouse still home)

Phone Switch ON (arrived) → Cancel any pending camerasOn()
  ↓
Schedule camerasOff() after privacy delay
```

### Manual Override Logic
```
Override Switch ON → Immediate camerasOff()
  ↓
Schedule auto-revert after duration
  ↓
Duration Expires → Turn override switch OFF → Return to presence control
```

## Best Practices

1. **Set Privacy Delay**: Allow time to enter home before cameras turn off
2. **Set Enable Delay**: Ensure you've left before cameras activate
3. **Use Traveling Switch**: When spouse is home but you're traveling
4. **Test Manual Override**: Verify it works before relying on it
5. **Use Hub Variables**: For dynamic control without app updates
6. **Monitor Logs**: Enable debug logging during setup
7. **Separate Indoor/Outdoor**: Keep outdoor cameras for perimeter security

## Version History

### Version 1.1.0 (2025-01-XX)
- Changed from mode-based to phone presence switch-based control
- Works in any mode - presence switch is the trigger
- Simplified configuration

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
