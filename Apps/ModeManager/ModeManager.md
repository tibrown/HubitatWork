# Mode Manager Custom

## Overview
Mode Manager Custom provides automated hub mode management with time-based and solar-based scheduling. It supports day-of-week filtering, sunrise/sunset offsets, and a configurable auto-control switch to allow manual Night mode when needed. The app replaces Hubitat's built-in Mode Manager while adding schedule conflict validation and a manual override mechanism.

## Purpose
- Automatically change the hub mode at scheduled times or solar events
- Support four daily transitions: Morning, Day, Evening, Night
- Allow Night mode to be set manually when the auto-control switch is ON
- Prevent any automatic mode change while in Away mode
- Validate schedules at configuration time to warn of conflicts
- Display the current mode and next transition times in the app UI

## Features
- **Four Transition Periods**: Morning, Day, Evening, Night — each independently configurable
- **Trigger Types**: Specific time, Sunrise (with offset), or Sunset (with offset)
- **Day-of-Week Filtering**: Each transition can be restricted to specific days of the week
- **Auto-Control Switch**: Toggle Night mode automation on/off without reconfiguring
- **Away Mode Protection**: The hub will never automatically transition away from Away mode
- **Schedule Validation**: Warns when two transitions are scheduled within 5 minutes of each other on the same days
- **Status Display**: Shows current mode and next scheduled transition times in the app settings page
- **Dynamic App Label**: The app label updates in real-time to show the current mode

## Installation

### Prerequisites
1. Hub modes configured (Morning, Day, Evening, Night, Away or equivalent)
2. One virtual switch (or physical switch) for the auto-control function

### Installation Steps
1. Navigate to **Apps Code** in Hubitat
2. Click **New App**
3. Paste the `ModeManager.groovy` code
4. Click **Save**
5. Navigate to **Apps**
6. Click **Add User App**
7. Select **Mode Manager Custom**
8. Configure (see Configuration Guide below)

## Configuration Guide

### Auto-Control Switch (Required)
Select a switch to control automatic Night mode transitions:
- **Switch OFF**: Night mode changes automatically at the scheduled time/event
- **Switch ON**: Night mode transitions are skipped — you must change Night mode manually (e.g., via a button, routine, or Rule Machine rule)

Morning, Day, and Evening transitions always run automatically regardless of this switch state. Away mode is also never exited automatically.

### Morning Transition
- **Enable Morning Transition**: Toggle this transition on or off
- **Morning Mode**: The hub mode to set at this time (select from your configured modes)
- **Trigger Type**: How to trigger the transition:
  - **Specific Time** — enter a time of day
  - **Sunrise** — fire at sunrise (with optional offset)
  - **Sunset** — fire at sunset (with optional offset)
- **Offset (minutes)**: For solar triggers, add or subtract minutes (e.g., -30 for 30 minutes before sunrise). Negative = before the event.
- **Active Days**: Select which days of the week this transition fires. Defaults to all 7 days.

### Day Transition
Same configuration options as Morning Transition. Typically used mid-morning to switch from Morning to Day mode.

### Evening Transition
Same configuration options. Typically triggered at sunset or a fixed evening time.

### Night Transition
Same configuration options. Controlled by the Auto-Control Switch:
- When auto-control is **OFF**: fires at the configured time/event
- When auto-control is **ON**: skipped (manual Night mode required)

### Logging
- **Enable Debug Logging**: Logs detailed schedule setup and skip reasons
- **Enable Info Logging**: Logs mode transition events

## How It Works

### Mode Change Logic
When a scheduled transition fires:
1. The handler checks if the current mode is **Away** — if so, the transition is skipped
2. The handler checks the **day-of-week** — if today is not in the configured Active Days, the transition is skipped
3. For the **Night** transition only: if the Auto-Control Switch is **ON**, the transition is skipped
4. If all checks pass: `location.setMode(targetMode)` is called to change the hub mode

### Solar Offset Scheduling
When using Sunrise or Sunset triggers:
- If the offset is **0**: the app subscribes to the Hubitat location `sunrise`/`sunset` event
- If the offset is **non-zero**: the app calculates the adjusted time and schedules it with `runOnce()`
- Solar times are rescheduled each day when the `sunriseTime`/`sunsetTime` event fires (typically published the night before)

### Schedule Validation
When you save the app, it validates all enabled schedules and warns if any two transitions would fire within 5 minutes of each other on the same days of the week. Configuration warnings are shown in red at the top of the settings page and logged as errors.

### Status Display
The settings page shows:
- Current Auto-Control state (ON/OFF)
- Current hub mode
- Next scheduled transition time for each enabled transition (when auto-control is OFF)

### App Label
The app label is dynamically updated to include the current mode, e.g.:
`Mode Manager Custom - <current mode>`

This makes it easy to see the current mode from the Apps list without opening the app.

## Usage Examples

### Example 1: Typical Daily Schedule
| Transition | Trigger | Time/Event | Days |
|------------|---------|------------|------|
| Morning | Specific Time | 6:30 AM | Mon–Fri |
| Day | Specific Time | 8:00 AM | All days |
| Evening | Sunset | Sunset +0 min | All days |
| Night | Specific Time | 10:00 PM | All days |

**Auto-Control Switch**: OFF (Night mode always automatic)

### Example 2: Weekend Variation
- Add a second Mode Manager instance for weekend schedules with different Morning/Day times
- Or disable Monday–Friday days on the Morning transition and add a different Saturday–Sunday transition time

### Example 3: Manual Night Mode for Security
**Scenario**: You want to control exactly when Night mode (with security monitoring) activates, rather than having it trigger automatically.

**Configuration**:
- Auto-Control Switch: Connected to a virtual switch "Bedtime"
- Night Transition: 11:00 PM (as a fallback)

**Behavior**:
- When you're ready for bed, turn ON the "Bedtime" switch → auto-control enables, Night mode runs automatically at 11 PM
- If you're out late, leave "Bedtime" OFF → Night mode won't change automatically
- When you come home, turn ON "Bedtime" and Night mode will activate at 11 PM

### Example 4: Solar-Based Evening
**Configuration**:
- Evening Trigger: Sunset with -15 minute offset

**Behavior**:
- Hubitat calculates today's sunset time
- Evening transition fires 15 minutes before sunset each day
- Automatically shifts with the seasons

## Troubleshooting

### Mode Not Changing at Expected Time
1. Verify the transition is **enabled** (toggle switch is ON)
2. Check that today is in the **Active Days** configured
3. Check if current mode is **Away** (Away mode prevents all automatic transitions)
4. For Night mode: verify the **Auto-Control Switch** is **OFF**
5. Review app logs for "Skipping" messages
6. Check for schedule validation warnings in the app settings

### Solar Trigger Not Working
1. Verify your hub has a time zone and location (sunrise/sunset require location)
2. Check hub settings: **Settings → Location and Modes**
3. Enable debug logging and look for solar scheduling messages on initialization

### Two Transitions Firing Too Close Together
1. Open the app settings — validation warnings appear in red at the top
2. Separate the scheduled times by at least 5 minutes on shared days

### Mode Changes When Away
- This is by design. Away mode is protected and no automatic transitions occur while in Away mode.
- To leave Away mode automatically, use a separate rule or presence-based automation.

## Technical Details

### App Properties
- **Name**: Mode Manager Custom  
- **Namespace**: hubitat  
- **Author**: Tim Brown  
- **Category**: Convenience  
- **Single Threaded**: Yes

### Event Subscriptions
- `location.sunrise` / `location.sunset` — for solar triggers with zero offset
- `location.sunriseTime` / `location.sunsetTime` — to reschedule solar-offset transitions daily
- `location.mode` — to update the app label when mode changes
- `autoControlSwitch.switch` — to setup/tear down the Night schedule when the switch changes

### Scheduled Jobs
Each enabled transition creates one scheduled job at initialization:
- Specific Time → `schedule(time, transitionHandler)`
- Solar with zero offset → event subscription handles it
- Solar with offset → `runOnce(adjustedTime, transitionHandler)` + daily rescheduling

### Key Methods
- `setupMorningSchedule()`, `setupDaySchedule()`, `setupEveningSchedule()`, `setupNightSchedule()` — schedule each transition
- `morningTransitionHandler()`, `dayTransitionHandler()`, `eveningTransitionHandler()`, `nightTransitionHandler()` — execute mode change after guard checks
- `validateSchedules()` — returns list of warnings for scheduled conflicts
- `updateLabel()` — updates app label to show current mode
- `getNextTransitionTime(transitionName)` — returns human-readable next firing time for the status display

## License

Copyright 2025 Tim Brown — Licensed under the Apache License, Version 2.0
