# Ring Alarm Manager

## Overview
**Ring Alarm Manager** ensures the Ring alarm reliably arms when the `RingModeOnOff` switch is turned ON. Because the Ring cloud integration can be sluggish, a single ON command sometimes fails to arm the Ring alarm. This app solves that by automatically sending the ON command multiple additional times with a configurable delay between each send.

OFF is reliable and does not need repeating — this app only retries the ON path.

> **Future-ready**: The app is designed to eventually support direct Ring base station communication (status check + set). When that integration becomes available, a status verification step can be added before deciding whether to retry.

## Purpose
- Guarantee the Ring alarm arms when `RingModeOnOff` is turned ON
- Send configurable number of additional ON commands with a configurable delay
- Cancel pending repeat sends immediately if `RingModeOnOff` is turned OFF
- Optionally notify on each repeat send for visibility

## This App is the Sole Subscriber to RingModeOnOff Events
No other app in this system subscribes to `ringModeOnOff` switch events:
- `NightSecurityManager` **writes** to it (turns ON/OFF) but does not subscribe
- `SilentCheck` reads it on a schedule via `currentValue()` — no event subscription

`RingAlarmManager` is intentionally the **only** event subscriber. Do not add subscriptions to `ringModeOnOff` in other apps.

## Installation

### Prerequisites
1. A Hubitat hub with the `RingModeOnOff` Connector Switch already configured
2. The switch must be the same device used by `NightSecurityManager` and `SilentCheck`

### Installation Steps
1. Navigate to **Apps Code** in Hubitat
2. Click **New App**
3. Paste the contents of `RingAlarmManager.groovy`
4. Click **Save**
5. Navigate to **Apps**
6. Click **Add User App**
7. Select **Ring Alarm Manager**
8. Configure the app (see Configuration below)

## Configuration

### Ring Mode Switch
- **Ring Mode On/Off Switch** (required): Select the `RingModeOnOff` Connector Switch device. This is the virtual switch that represents the Ring alarm's armed/disarmed state.

### Repeat Configuration
- **Extra ON Sends** (required, default: 2, range: 1–5): How many additional OFF→ON cycles to perform after the initial arm. The total number of ON commands sent will be `Extra ON Sends + 1`.
- **Delay Before Each Repeat** (required, default: 15 seconds, range: 5–120): How long to wait after the previous ON before starting the next OFF→ON cycle.
- **OFF-to-ON Delay Within Each Repeat** (required, default: 5 seconds, range: 1–30): How long to hold the switch OFF before sending ON within each repeat cycle.

**Example with defaults**: If the switch turns ON at T=0, the sequence is:
- T=0s: original ON (from whatever turned the switch on)
- T=15s: repeat 1 — turn OFF
- T=20s: repeat 1 — turn ON (5s after the OFF)
- T=35s: repeat 2 — turn OFF
- T=40s: repeat 2 — turn ON (5s after the OFF)

### Notifications
- **Notify on Each Repeat Send** (default: false): When enabled, sends a push notification each time a repeat ON command is fired. Useful for debugging.
- **Notification Devices**: Push notification devices to receive the repeat alerts.

### Logging
- **Log Level**: None / Info / Debug / Trace. Info is recommended for production use.

## How It Works

### ON Flow
```
1. RingModeOnOff switch turns ON (from any source — rules, another app, dashboard)
2. RingAlarmManager receives the switch.on event
   - If repeatSendsPending is true, the event is self-generated (from a repeat send) and is ignored
3. Sets state: repeatSendsPending=true, repeatsRemaining=repeatCount
4. Schedules sendNextRingOn to run after repeatDelay seconds
5. sendNextRingOn fires:
   a. Sets intentionalOff=true, calls ringModeOnOff.off()
      - handleRingModeOff fires but sees intentionalOff=true and ignores it
   b. Schedules doRingOn after offDelay seconds
6. doRingOn fires:
   a. Clears intentionalOff
   b. Calls ringModeOnOff.on()
      - handleRingModeOn fires but sees repeatSendsPending=true and ignores it
   c. Optionally sends a notification
   d. Decrements repeatsRemaining; re-schedules sendNextRingOn if more cycles remain
7. Continues until repeatsRemaining reaches 0
```

### OFF Flow
```
1. RingModeOnOff switch turns OFF (from any source)
2. RingAlarmManager receives the switch.off event
3. All pending repeat ON sends are immediately cancelled
4. No further sends are made — OFF is reliable and does not need repeating
```

### Mid-Cycle Flip
If the switch turns ON → OFF before all repeats have fired, the OFF handler cancels all pending sends. If it turns ON again, the cycle restarts fresh.

## Hub Variables
This app does not use hub variables. Configuration is stored in app settings only.

## Known Limitations
- **No status verification**: There is currently no way to read the Ring alarm's actual armed status from Hubitat. Repeat sends are the reliability mechanism until direct base station communication is available.
- **Repeat sends always fire**: The app cannot tell whether the first ON was successful and will always send the configured number of repeats. This is intentional — sending ON to an already-armed system has no negative effect.
- **Ring cloud dependency**: This app improves reliability by sending multiple commands, but cannot guarantee arming if the Ring cloud service is fully unavailable.
