# Freeze Alert Manager

## Overview
Freeze Alert Manager monitors temperature sensors and announces freeze warnings via Echo devices when the temperature drops below a configurable threshold. It also cycles a heat lamp to prevent pipes/plants from freezing, controls a chicken heater using hysteresis-based temperature control, and optionally uses NWS weather data as a backup temperature source for safer freeze detection.

## Purpose
- Monitor local temperature sensors for freeze conditions
- Announce freeze warnings via Echo/speech synthesis devices
- Cycle a heat lamp automatically when temperatures are dangerously low
- Control a chicken pen heater based on temperature thresholds
- Provide configurable cooldown periods to prevent alert spam
- Use NWS weather data as a backup when local sensors may read high (e.g., solar heating)

## Features
- **Multi-Sensor Support**: Configure multiple temperature sensors; uses the lowest reading when all sensors are active
- **Echo Announcements**: Send spoken freeze warning to selected Echo/Alexa devices
- **Volume Management**: Sets alert volume, then restores to normal volume after alert
- **Push Notifications**: Optionally send push notifications alongside voice alerts
- **Alert Cooldown**: Configurable minimum time between repeated alerts to prevent spam
- **Notification Control Switch**: Toggle notifications on/off via virtual switch
- **Heat Lamp Cycling**: Automatically cycle a heat lamp ON/OFF at configurable intervals when below threshold
- **Cycling Indicator Switch**: Visual indicator (Connector switch) showing active cycling status
- **Chicken Heater Control**: Hysteresis-based heater control for chicken pen, mode-specific operation
- **NWS Weather Backup**: Uses NWS weather device temperature as backup when local sensor is stale
- **Smart Sensor Selection**: Prefers the lowest temperature from all active sensors for freeze safety
- **Hub Restart Safe**: Initializes heat lamp cycling on hub restart if conditions warrant it

## Installation

### Prerequisites
1. At least one temperature sensor
2. At least one Echo/speech synthesis device for announcements
3. Optional: virtual switches for notification control and cycling indicator

### Installation Steps
1. Open Hubitat web interface
2. Navigate to **Apps Code**
3. Click **New App**
4. Paste the contents of `FreezeAlertManager.groovy`
5. Click **Save**
6. Navigate to **Apps**
7. Click **Add User App**
8. Select **Freeze Alert Manager**
9. Configure the app (see Configuration below)

## Configuration

### Temperature Monitoring
- **Temperature Sensors** (required, multiple): Local temperature sensors to monitor. When all sensors have active recent readings (within the Local Sensor Preference time), the app uses the **lowest** temperature for safer freeze detection. Falls back to the most recent reading if sensors have stale data.
- **Freeze Alert Temperature (°F)**: Temperature at or below which a freeze alert is sent (default: 32.0°F)

### Alert Configuration
- **Echo Devices for Announcements** (required, multiple): Echo or speech synthesis devices to receive spoken freeze warning
- **Alert Message**: Text of the spoken announcement (default: "Freeze Warning")
- **Echo Volume Level (1-100)**: Volume to use when speaking the alert (default: 30)
- **Reset Volume After Alert (1-100)**: Volume to restore after the alert completes (default: 35). Alert volume is reset approximately 10 seconds after the announcement.

### Notification Control
- **Push Notification Devices**: Optional push notification devices to receive alerts in addition to voice announcements
- **Notification Control Switch**: Optional virtual switch to enable/disable all notifications. When configured, notifications are only sent when this switch is **ON**.
- **Alert Cooldown Period (minutes)**: Minimum time between repeated freeze alerts (0-60, default: 5). Prevents repeated announcements while the temperature stays below threshold.

### Heat Lamp Control
Configure a heat lamp to cycle automatically during cold conditions:
- **Heat Lamp Switch**: Switch controlling the heat lamp
- **Heat Lamp Master Control Switch**: Master switch to enable/disable heat lamp cycling. Cycling only occurs when this switch is **ON** and temperature is at or below the trigger threshold.
- **Heat Lamp ON Duration (minutes)**: How long to keep the heat lamp on each cycle (1-60, default: 15)
- **Heat Lamp OFF Duration (minutes)**: How long to keep the heat lamp off between cycles (1-60, default: 15)
- **Heat Lamp Trigger Temperature (°F)**: Temperature at or below which cycling begins (default: 32.0°F)
- **Heat Lamp Cycling Indicator**: Optional Connector switch that is ON when cycling is active

**Heat Lamp Behavior:**
- When temperature drops to or below the trigger threshold AND master switch is ON: cycling starts
- During cycling: lamp turns ON for the configured duration, then OFF, then ON again repeatedly
- When temperature rises above threshold: cycling stops automatically and lamp turns off
- When master switch turns OFF: cycling stops with a notification

### Chicken Heater Control
Configure temperature-based hysteresis control for a chicken pen heater:
- **Chicken Heater Switch**: Switch controlling the heater
- **Chicken Heater Active Modes**: Hub modes when the chicken heater is allowed to operate. Leave empty to allow operation in all modes.
- **Heater ON Temperature (°F)**: Turn heater ON when temperature falls to or below this value (default: 46.0°F)
- **Heater OFF Temperature (°F)**: Turn heater OFF when temperature rises to or above this value (default: 48.0°F)

**Chicken Heater Behavior:**
- Uses hysteresis: turns ON below the minimum, turns OFF above the maximum
- Announcements are sent via Echo devices when heater state changes (subject to notification control switch)
- Mode-based: will turn OFF heater if mode changes to one not in the allowed modes list
- State is evaluated on hub restart/app update

### NWS Weather Backup
Optionally use a National Weather Service weather device as a backup temperature source to compensate for sensors that read high due to solar heating:
- **NWS Weather Device**: Select the NWS Weather Driver device (optional)
- **NWS Data Staleness Threshold (minutes)**: Ignore NWS data older than this value (15-120, default: 45). If NWS data is stale, the local sensor is used instead.
- **Local Sensor Preference (minutes)**: When a local sensor reading is this fresh (1-30, default: 5), it is always preferred over NWS data. If the local sensor is older than this, comparisons are made with NWS.

**Temperature Selection Logic:**
```
1. Collect readings from all configured local sensors
2. If ALL sensors have activity within the preference window:
   → Use the LOWEST temperature (coldest reading = most conservative for freeze safety)
3. If any sensor is stale:
   → Use the most recently updated temperature reading
4. If local reading is within the preference window:
   → Use local reading (ignore NWS)
5. If local reading is older than the preference window AND NWS is fresh:
   → Use NWS temperature
6. If NWS is stale or unavailable:
   → Fall back to local temperature
```

### Logging
- **Enable Debug Logging**: Enable detailed debug logs (recommended during setup/troubleshooting)

## Usage Examples

### Example 1: Simple Freeze Warning
**Scenario**: Alert when outdoor temperature drops to freezing

**Configuration**:
- Temperature Sensor: Outdoor sensor
- Freeze Alert Temperature: 32°F
- Echo Devices: Kitchen Echo, Bedroom Echo
- Alert Message: "Freeze Warning - protect your plants"
- Echo Volume: 40
- Reset Volume: 30
- Cooldown: 60 minutes

**Behavior**:
1. Temperature drops to 32°F → Echo devices announce "Freeze Warning - protect your plants" at volume 40
2. Volume resets to 30 after 10 seconds
3. No further alert for 60 minutes even if temperature stays below 32°F

### Example 2: Heat Lamp Cycling with Freeze Alert
**Scenario**: Protect a greenhouse or outdoor tap from freezing

**Configuration**:
- Freeze Alert Temperature: 32°F
- Heat Lamp Switch: Greenhouse heat lamp
- Heat Lamp Master Switch: Virtual switch "Heat Lamp Control" (turn ON each winter)
- Heat Lamp Trigger Temperature: 35°F (start cycling before freezing)
- ON Duration: 20 minutes
- OFF Duration: 10 minutes
- Cycling Indicator Switch: Virtual connector "Heat Lamp Active"

**Behavior**:
1. Temperature drops to 35°F and master switch is ON → Cycling starts
   - Lamp ON for 20 min → Lamp OFF for 10 min → Lamp ON for 20 min → ...
2. Temperature drops to 32°F → Freeze alert ALSO sent
3. Temperature rises above 35°F → Cycling stops, lamp turns off

### Example 3: Chicken Heater
**Scenario**: Keep chicken pen warm overnight without constant power draw

**Configuration**:
- Chicken Heater Switch: Pen heater outlet
- Active Modes: Night, Evening (daytime not needed)
- Heater ON Temperature: 46°F
- Heater OFF Temperature: 48°F

**Behavior**:
1. Mode changes to Night or Evening → Heater monitoring activates
2. Temperature drops below 46°F → Heater turns ON + announcement
3. Temperature rises above 48°F → Heater turns OFF + announcement
4. Mode changes to Day → Heater turns OFF

### Example 4: NWS Backup for Solar-Heated Sensors
**Scenario**: Outdoor sensor reads high in afternoon sun, causing missed freeze alerts

**Configuration**:
- Local Sensor: Outdoor thermometer (may read high during sunny days)
- NWS Weather Device: NWS Weather Driver configured for your area
- NWS Staleness Threshold: 45 minutes
- Local Sensor Preference: 5 minutes

**Behavior**:
- Sensor reading < 5 minutes old → Use local sensor (trusted as current)
- Sensor reading > 5 minutes old + NWS is fresh → Use NWS temperature
- NWS data not available or stale → Fall back to local sensor

## Troubleshooting

### No Alert When Temperature is Below Threshold
1. Check that **Notification Control Switch** is configured and turned ON (if configured)
2. Check the **Cooldown Period** - may still be in cooldown from a prior alert
3. Verify Echo devices are configured and responsive
4. Enable debug logging and check logs for "skipping alert" messages
5. Verify the temperature sensor is reporting correctly

### Heat Lamp Not Cycling
1. Check that **Heat Lamp Switch** is configured
2. Ensure **Heat Lamp Master Control Switch** is turned ON
3. Verify current temperature is at or below the **Heat Lamp Trigger Temperature**
4. Enable debug logging to see threshold comparison messages
5. Check the **Cycling Indicator Switch** status (if configured)

### Heat Lamp Cycling Not Stopping
1. Verify temperature has actually risen above the trigger threshold
2. Turn OFF the **Heat Lamp Master Control Switch** to manually stop cycling
3. Check logs for cycling guard checks

### Chicken Heater Not Activating
1. Verify current hub mode is in the **Chicken Heater Active Modes** list (or leave that setting empty)
2. Check temperature is actually below the **Heater ON Temperature**
3. Verify the heater switch is configured and working
4. Enable debug logging for `checkChickenHeater` messages

### NWS Temperature Not Being Used
1. Verify the NWS Weather Device is configured and has a recent temperature reading
2. Check that local sensor reading is older than the **Local Sensor Preference** time
3. Verify NWS data is not stale (check **NWS Data Staleness Threshold**)
4. Enable debug logging for temperature selection messages

## Technical Details

### App Properties
- **Name**: Freeze Alert Manager
- **Namespace**: tibrown
- **Author**: Tim Brown
- **Category**: Safety & Security
- **Single Threaded**: Yes

### Supported Capabilities
- `capability.temperatureMeasurement` - Temperature sensors and NWS device
- `capability.speechSynthesis` - Echo/Alexa devices for announcements
- `capability.notification` - Push notification devices
- `capability.switch` - Heat lamp, chicken heater, notification control, cycling indicator, master control

### State Variables
- `heatLampCycling` (Boolean): Whether heat lamp cycling is currently active
- `heatLampCurrentlyOn` (Boolean): Whether heat lamp is presently turned on
- `chickenHeaterAnnouncedOn` (Boolean): Tracks whether ON announcement was sent
- `chickenHeaterAnnouncedOff` (Boolean): Tracks whether OFF announcement was sent
- `lastAlertTime` (Long): Timestamp of last freeze alert for cooldown calculation
- `mainsAlertStartTime` (Long): Not used in this app (legacy)

### Volume Reset
After sending a freeze, heat lamp, or chicken heater announcement, a 10-second timer is set to restore the Echo volume to the configured **Reset Volume**. If you trigger manual tests, wait at least 10 seconds before checking volume.

## License

Copyright 2025 Tim Brown — Licensed under the Apache License, Version 2.0
