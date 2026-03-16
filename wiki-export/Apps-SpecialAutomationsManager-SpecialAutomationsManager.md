# Mains Power Monitor

> **Note**: This app is stored in the `SpecialAutomationsManager` folder for historical reasons. The app definition name is **Mains Power Monitor**.

## Overview
Mains Power Monitor tracks the power source of the Hubitat hub (mains vs. battery/UPS), alerts on power outages, sends periodic battery reminders, and safely shuts down the hub after an extended outage to prevent data corruption.

## Purpose
- Detect when the hub switches from mains power to battery backup
- Alert via push notification when the outage persists
- Send periodic reminders while running on battery
- Automatically shut down the hub before the battery is exhausted
- Alert when mains power is restored
- Support a bypass switch to disable alerts/shutdown during maintenance

## Features
- **Power Loss Detection**: Reacts immediately to `powerSource` events (mains to battery)
- **Configurable Stay Duration**: Only alerts after the hub has been on battery for a configurable minimum time (avoids false alarms from brief flickers)
- **Hub Shutdown**: Safely shuts down the hub via the Hubitat Hub Controller device to prevent SD card/database corruption
- **Periodic Reminders**: Sends regular battery-level reminders with countdown to shutdown
- **Power Restore Notification**: Cancels all pending alerts/shutdown and notifies when mains power returns
- **Bypass Switch**: An ignore switch disables all alerts and scheduled shutdown during planned maintenance
- **Status Display**: Shows real-time power source and battery level in the app settings page

## Installation

### Prerequisites
1. A UPS or battery backup device reported to Hubitat as a `powerSource` sensor (e.g., via a compatible driver)
2. The Hubitat Hub Controller device must be enabled if you want hub shutdown functionality: **Settings > Hub Details > Hub Controller**
3. Optional: virtual switches for status tracking and ignore functionality

### Installation Steps
1. Navigate to **Apps Code** in Hubitat
2. Click **New App**
3. Paste the `SpecialAutomationsManager.groovy` code
4. Click **Save**
5. Navigate to **Apps**
6. Click **Add User App**
7. Select **Mains Power Monitor**
8. Configure the app (see Configuration below)

## Configuration

### Power Source Device
- **Mains Power Sensor** (required): The device that reports `powerSource` capability (e.g., a UPS device driver). The `powerSource` attribute should report `"mains"` when on utility power and `"battery"` when on battery backup.
- **Enable Mains Power Monitoring**: Toggle all mains monitoring on/off. When disabled, the app subscribes to nothing. Default: true.

### Status & Control Devices
- **On Mains Status Switch**: Optional virtual switch that mirrors the power source state. ON = on mains, OFF = on battery. Useful for dashboard indicators or triggering other rules.
- **Ignore Mains Check Switch**: Optional virtual switch. When this switch is **ON**, all battery alerts and hub shutdown are bypassed. Use during planned maintenance or testing.
- **Hub Controller Device**: The Hubitat Hub Controller device (actuator). Required for hub shutdown functionality. Receives a `.shutdown()` command when shutdown is triggered.

### Timing Settings
- **Battery Stay Duration Before Alert (minutes)**: How many minutes the hub must be on battery before an alert is sent (1-30, default: 5). Prevents false alarms from momentary power glitches.
- **Hub Shutdown Delay After Alert (minutes)**: How many minutes after the first alert before the hub shuts down (5-120, default: 30). This is the total time from the alert, not from the power loss.
- **Battery Reminder Interval (minutes)**: How often to send "still on battery" reminders while awaiting shutdown (1-30, default: 5).

### Notifications
- **Notification Devices**: Devices to receive push notifications for power events.

### Logging
- **Logging Level**: None, Info, Debug, or Trace. Info is recommended for production use.

## How It Works

### Power Loss Sequence
```
1. Mains power lost - powerSource event fires ("battery")
2. onMainsSwitch set to OFF
3. Wait configured Stay Duration (default: 5 minutes)
4. If still on battery and Ignore switch is OFF:
   - Send alert: "ALERT: Mains power is DOWN. Hub will shut down in X minutes..."
   - Schedule hub shutdown after configured delay
   - Begin sending periodic battery reminders every interval minutes
5. If power restores before any step: cancel all scheduled jobs, send restoration notification
```

### Battery Reminder Messages
Each reminder includes a countdown: "Still on battery - hub shutdown in X minutes"
Reminders stop automatically once the remaining time is shorter than the reminder interval.

### Hub Shutdown Sequence
```
1. Shutdown delay expires
2. Verify still on battery (cancel if power restored)
3. Check Ignore switch (cancel if bypassed)
4. Send notification: "Hub shutting down due to extended power outage"
5. Call hubController.shutdown()
```

### Power Restored Sequence
```
1. Mains power restored - powerSource event fires ("mains")
2. onMainsSwitch set to ON
3. Cancel all pending scheduled jobs (alert, shutdown, reminders)
4. Send notification: "Mains power restored"
```

## Usage Examples

### Example 1: Basic Power Outage Alerting
**Scenario**: Receive a notification if the hub loses power and may shut down

**Configuration**:
- Mains Power Sensor: UPS device
- Enable Monitoring: ON
- Battery Stay Duration: 3 minutes
- Hub Shutdown Delay: 45 minutes
- Reminder Interval: 10 minutes
- Notification Device: Phone

**Behavior**:
1. Power outage - after 3 minutes, alert sent: "Hub shuts down in 45 minutes"
2. Every 10 minutes: "Still on battery - shutdown in X minutes"
3. After 45 minutes on battery: hub shuts down safely
4. When power returns: "Mains power restored"

### Example 2: With Dashboard Indicator
**Scenario**: Show power status on a Hubitat dashboard

**Configuration**:
- On Mains Status Switch: Virtual switch "Hub On Mains"
- Display this switch on your dashboard

**Behavior**:
- Switch turns OFF when power is lost (visible on dashboard)
- Switch turns ON when power is restored

### Example 3: Maintenance Bypass
**Scenario**: Testing UPS or doing electrical work - don't want shutdown triggered

**Configuration**:
- Ignore Mains Check Switch: Virtual switch "Ignore Mains"

**Behavior**:
1. Turn ON "Ignore Mains" switch before maintenance
2. Power can be cycled without triggering alerts or shutdown
3. Turn OFF "Ignore Mains" when done

## Troubleshooting

### App Not Detecting Power Events
1. Verify the power sensor device reports the `powerSource` attribute
2. Check **Device Page > Current States** for `powerSource` value
3. Enable debug logging and check for subscription messages on initialize
4. Verify **Enable Mains Power Monitoring** is ON

### Alert Not Sent After Power Loss
1. Check the **Battery Stay Duration** - alert does not fire until after this delay
2. Verify **Ignore Mains Check Switch** is OFF
3. Check notification devices are configured and working
4. Review logs for "cancelling alert" messages (power may have been briefly restored)

### Hub Not Shutting Down
1. Verify **Hub Controller Device** is configured in the app
2. Verify the Hub Controller device is enabled in Hubitat settings
3. Check logs for the shutdown call
4. Note: if `ignoreMainsCheckSwitch` is ON, shutdown is bypassed

### Shutdown Triggered When It Shouldn't Be
1. Turn ON the **Ignore Mains Check Switch** immediately to cancel pending shutdown
2. Fix the underlying power sensor issue (may be reporting incorrect values)

## Technical Details

### App Properties
- **Definition Name**: Mains Power Monitor (in SpecialAutomationsManager.groovy)
- **Namespace**: timbrown
- **Author**: Tim Brown
- **Category**: Safety & Security
- **Single Threaded**: Yes

### Supported Capabilities
- `capability.powerSource` - Mains power sensor
- `capability.switch` - Status switch, ignore switch
- `capability.actuator` - Hub Controller for shutdown
- `capability.notification` - Push notification devices

### State Variables
- `mainsAlertStartTime` (Long): Timestamp when the battery alert started; used to calculate remaining shutdown time for reminders

### Event Subscriptions
- `mainsPowerSensor.powerSource` - fires on any change between "mains" and "battery"

### Scheduled Jobs
- `handleMainsStayedOnBattery` - fires once after the stay duration when battery is detected
- `shutdownHub` - fires after the shutdown delay to execute hub shutdown
- `sendBatteryReminder` - fires repeatedly at the reminder interval while on battery

## License

Copyright 2025 Tim Brown - Licensed under the Apache License, Version 2.0
