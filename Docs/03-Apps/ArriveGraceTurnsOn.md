# Arrive Grace Turns On

## Overview
**Arrive Grace Turns On** is a convenience app designed to manage alarm settings upon arrival. It provides a "grace period" where alarms are disabled and silent mode is enabled, allowing for a peaceful entry without triggering security alerts.

## Features
- **Automatic Trigger**: Activates when the "Arrive Grace Period" switch is turned on.
- **Alarm Management**: Automatically disables the "Alarms Enabled" switch.
- **Silent Mode**: Automatically enables the "Silent Mode" switch.
- **Configurable Duration**: Users can specify the duration of the grace period in minutes.
- **Automatic Restoration**: After the grace period expires, the app automatically:
    - Re-enables alarms.
    - Disables silent mode.
    - Turns off the "Arrive Grace Period" switch.

## Configuration
### Trigger Device
- **Arrive Grace Period Switch**: The switch that triggers the grace period when turned on.

### Devices to Control
- **Alarms Enabled Switch**: The master switch for your alarm system.
- **Silent Mode Switch**: The switch that controls silent mode.

### Settings
- **Grace Duration**: The length of the grace period in minutes (default: 30).

## Logic Flow
1.  **Trigger**: `ArriveGracePeriod` turns `on`.
2.  **Action**:
    - `AlarmsEnabled` -> `off`
    - `SilentMode` -> `on`
    - Schedule `endGracePeriod` after `GraceDuration`.
3.  **End of Grace Period**:
    - `AlarmsEnabled` -> `on`
    - `SilentMode` -> `off`
    - `ArriveGracePeriod` -> `off`
