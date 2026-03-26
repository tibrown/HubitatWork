# SilentCheck

## Overview
SilentCheck runs an hourly check between 12:00 noon and sunset. If either monitored switch is in the wrong state during this window, a push notification is sent. The check repeats every hour until both switches are in the correct state or the window closes at sunset.

## Purpose
- Remind you if the **Silent Mode switch** has been left ON during the day (when it typically shouldn't be)
- Remind you if **Ring is Disarmed** (RingModeOnOff switch is OFF) during the day

## How It Works

```
1. On install/update, an hourly scheduler is started
2. Each hour, the app checks if the current time is between noon and sunset
3. If inside the window:
   - Silent switch ON  → send "Warning: Silent is still engaged"
   - Ring mode switch OFF → send "Ring is Disarmed"
4. If outside the window, the check is skipped silently
5. On install/update, an immediate check runs only if already inside the window
```

## Alerts

| Condition | Notification Message |
|-----------|---------------------|
| Silent switch is ON between noon and sunset | `Warning: Silent is still engaged` |
| Ring mode switch is OFF between noon and sunset | `Ring is Disarmed` |

## Configuration

### Switches to Monitor
- **Silent Mode Switch**: The hub's global silent switch — alerts when ON during the window
- **Ring Mode Switch (RingModeOnOff)**: Ring arming switch — alerts when OFF (disarmed) during the window

### Notification Devices
Push notification devices to receive the hourly alerts.

### Logging
- **None**: No logging
- **Info**: Key events logged
- **Debug**: Detailed flow including window checks
- **Trace**: Full state output including time comparisons

## Installation

1. **Install App**:
   - Apps → Add User App → SilentCheck
   - Select Silent Mode switch
   - Select Ring Mode switch (RingModeOnOff)
   - Select notification device(s)
   - Set logging level
   - Click Done

2. **Verify**:
   - Turn on the silent switch between noon and sunset — a notification should arrive within the hour
   - Check logs to confirm the hourly scheduler is firing

## Assumptions
- **RingModeOnOff ON = Ring Armed**, OFF = Ring Disarmed. The alert fires when the switch is OFF.
- Sunset time is determined dynamically each check via `getSunriseAndSunset()` — no manual configuration needed.
- Both switches are optional; if not configured, that check is simply skipped.

## Code Structure

### Key Methods
- `initialize()` — starts `runEvery1Hour(performChecks)`, runs immediate check if inside window
- `performChecks()` — main check logic; only acts when `isInCheckWindow()` is true
- `isInCheckWindow()` — compares current time against noon and today's sunset
- `sendNotification(message)` — sends to all configured notification devices
