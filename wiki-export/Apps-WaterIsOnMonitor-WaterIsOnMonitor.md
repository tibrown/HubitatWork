# Water Is On Monitor

## Overview
Water Is On Monitor is a Hubitat app that watches the `WaterIsOn` connector switch and sends reminder notifications when water has been running longer than a configured threshold. After the initial timer fires, reminders repeat at a configurable interval until the water turns off.

## Purpose
- Alert when water has been left running too long
- Send repeated reminders until water is turned off
- Fully configurable delay, repeat interval, notification devices, and message text

## Features
- **Initial Delay**: Configurable minutes before the first notification fires
- **Repeat Reminders**: Configurable interval for follow-up notifications while water stays on
- **Auto-Cancel**: All timers are silently cancelled when water turns off
- **Custom Message**: Notification text is a free-form string input
- **Multiple Notification Devices**: Supports any number of notification-capable devices
- **Status Display**: Shows current switch state on the app settings page

## Installation

### Prerequisites
1. The `WaterIsOn` connector switch device (device ID 1297) or any equivalent virtual switch
2. One or more notification devices (e.g., Pushover, phone notification)

### Installation Steps
1. Navigate to **Apps Code** in Hubitat
2. Click **New App**
3. Paste the contents of `WaterIsOnMonitor.groovy`
4. Click **Save**
5. Navigate to **Apps**
6. Click **Add User App**
7. Select **Water Is On Monitor**
8. Configure the app (see Configuration below)

## Configuration

### Water Switch
- **Water Is On Switch** (required): The switch device that goes ON when water is running. Typically the `WaterIsOn` connector switch.

### Timing Settings
- **Initial Delay Before First Notification (minutes)**: How many minutes the water must be on before the first notification is sent. Range: 1–240. Default: 30.
- **Repeat Notification Interval (minutes)**: How often repeat reminders are sent after the first notification, while water is still on. Range: 1–120. Default: 15.

### Notifications
- **Notification Devices** (required): One or more devices to receive push notifications.
- **Notification Message** (required): The text sent with every notification. Default: `"Reminder: Water is still on!"`

### Logging
- **Logging Level**: None, Info, Debug, or Trace. Info is recommended for production use.

## How It Works

### Water Turns On
```
1. WaterIsOn switch turns ON
2. App schedules handleWaterTimerExpired after initialDelay minutes
3. Any previously scheduled timers are cancelled first
```

### Timer Fires (Water Still On)
```
1. handleWaterTimerExpired checks switch is still ON
2. Sends notification message to all configured devices
3. Schedules sendWaterRepeatReminder after repeatInterval minutes
```

### Repeat Reminders
```
1. sendWaterRepeatReminder checks switch is still ON
2. Sends notification message
3. Schedules next repeat after repeatInterval minutes
4. Continues until water turns off
```

### Water Turns Off
```
1. WaterIsOn switch turns OFF
2. Both handleWaterTimerExpired and sendWaterRepeatReminder are unscheduled
3. No notification is sent
```

## Usage Examples

### Example 1: Basic 30-Minute Reminder
**Scenario**: Notify if water has been on for 30 minutes, repeat every 15 minutes.

**Configuration**:
- Water Is On Switch: WaterIsOn
- Initial Delay: 30 minutes
- Repeat Interval: 15 minutes
- Notification Device: Phone
- Notification Message: "Reminder: Water is still on!"

**Behavior**:
1. Water turns on
2. After 30 minutes: "Reminder: Water is still on!"
3. Every 15 minutes after that: "Reminder: Water is still on!"
4. Water turns off → reminders stop

### Example 2: Quick Garden Hose Check
**Scenario**: Short reminder in case the hose was left running.

**Configuration**:
- Initial Delay: 10 minutes
- Repeat Interval: 5 minutes
- Message: "⚠️ Garden hose is still running!"

## Technical Details

### App Properties
- **Definition Name**: Water Is On Monitor
- **Namespace**: timbrown
- **Author**: Tim Brown
- **Category**: Convenience
- **Single Threaded**: Yes

### Supported Capabilities
- `capability.switch` – Water Is On switch input
- `capability.notification` – Notification devices

### Event Subscriptions
- `waterIsOnSwitch.switch` – fires on any ON/OFF change

### Scheduled Jobs
- `handleWaterTimerExpired` – fires once after the initial delay when water turns on
- `sendWaterRepeatReminder` – fires repeatedly at the repeat interval while water is on

## License

Copyright 2025 Tim Brown – Licensed under the Apache License, Version 2.0
