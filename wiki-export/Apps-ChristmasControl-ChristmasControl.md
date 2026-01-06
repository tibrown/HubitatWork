# Christmas Control

## Overview
**Christmas Control** is a comprehensive automation app for managing Christmas decorations throughout the holiday season. It provides unified control of indoor Christmas trees and outdoor lights with intelligent scheduling, weather protection, and manual override capabilities.

## Features
- **Dual Control System**: Separate control for indoor trees and outdoor lights
- **Virtual Switch Integration**: Uses virtual switches for manual control and status monitoring
- **Flexible Scheduling**:
  - Date-based seasonal control (start/end dates)
  - Multiple turn-on options (Sunset, Time, or Mode-based)
  - Multiple turn-off options (Time, Sunrise, Sunset, or Mode-based)
- **Weather Protection**: Rain sensor integration prevents outdoor lights from operating in wet conditions
- **Smart Porch Light Management**: Automatically manages porch lights with outdoor decorations
- **Manual Override**: All devices can be controlled independently via virtual switches
- **Notification System**: Alerts when rain prevents lights from turning on

## Setup in Hubitat Hub

### Installation
1. Navigate to **Apps Code** in Hubitat
2. Click **New App**
3. Paste the `ChristmasTreesControl.groovy` code
4. Click **Save**
5. Navigate to **Apps**
6. Click **Add User App**
7. Select **Christmas Control**

### Configuration Options

#### Triggers Section
- **Master Switch** (optional): Single switch to control all Christmas devices at once
- **Enable Schedule** (optional): Toggle for automated scheduling
  - When enabled, additional scheduling options appear:
  - **Start Month** (default: 11): Month to begin Christmas automation (1-12)
  - **Start Day** (default: 23): Day to begin automation
  - **End Month** (default: 1): Month to end automation
  - **End Day** (default: 2): Day to end automation
  - **Turn On At**: Choose trigger method
    - **Sunset**: Automatically turns on at sunset
    - **Time**: Specific time each day
    - **Mode**: Turns on when hub enters specified mode(s)
  - **Turn Off At**: Choose turn-off method
    - **Time**: Specific time each day (default: 22:00/10:00 PM)
    - **Sunrise**: At sunrise
    - **Sunset**: At sunset
    - **Mode**: When hub enters specified mode(s)

#### Virtual Control Switches Section
- **ChristmasTrees Virtual Switch** (optional): Virtual switch representing all tree states
- **ChristmasLights Virtual Switch** (optional): Virtual switch representing all outdoor light states

#### Indoor Devices (Trees) Section
- **Tree Switches** (optional, multiple): All switches controlling Christmas trees

#### Outdoor Devices (Lights) Section
- **Main Christmas Lights** (optional, multiple): Primary outdoor decoration lights
- **Porch Lights** (optional, multiple): Porch lights that temporarily turn off when decorations are on
- **Rain Sensor** (optional): Sensor that prevents outdoor lights during rain (switch type)
- **Notification Devices** (optional, multiple): Devices to receive rain/status notifications

## How It Works

### Scheduling System

#### Date Range Check
The app only operates within the configured date range:
- Example: November 23 to January 2
- Handles year wrap (when end date is in following year)
- Automatically disables outside this range

#### Daily On/Off Schedule

**Turn On Methods:**
1. **Sunset**: Subscribes to sunset events, turns on at sunset each day
2. **Time**: Uses specified time (e.g., 17:00)
3. **Mode**: Activates when hub enters specified mode(s)

**Turn Off Methods:**
1. **Time**: Turns off at specified time (default 22:00)
2. **Sunrise**: Turns off at sunrise
3. **Sunset**: Turns off at sunset
4. **Mode**: Deactivates when hub enters specified mode(s)

#### Startup Catchup Logic
When the app initializes (hub restart, app update):
- Checks if current date/time is within active period
- Automatically turns on if should currently be on
- Ensures decorations are in correct state

### Device Control Logic

#### Indoor Trees (activateChristmas)
1. All tree switches turn ON
2. ChristmasTrees virtual switch syncs to ON
3. No weather conditions affect trees

#### Outdoor Lights (activateChristmas)
1. **Rain Check**: First checks rain sensor
   - If raining: Outdoor lights stay OFF, notification sent
   - Virtual switch syncs to OFF
   - Trees still turn on (unaffected by rain)
2. **If Clear**: 
   - Main Christmas Lights turn ON
   - ChristmasLights virtual switch syncs to ON
   - Porch lights turn OFF (after 5-minute delay)

#### Deactivation (deactivateChristmas)
1. All tree switches turn OFF
2. ChristmasTrees virtual switch syncs to OFF
3. Main Christmas Lights turn OFF
4. ChristmasLights virtual switch syncs to OFF
5. Porch lights turn back ON (restored)

### Manual Control

#### Via Virtual Switches
- **ChristmasTrees Switch**: 
  - Turn ON: All tree switches turn on
  - Turn OFF: All tree switches turn off
- **ChristmasLights Switch**:
  - Turn ON: Checks rain sensor first, then controls outdoor lights
  - Turn OFF: Turns off outdoor lights, restores porch lights

#### Via Master Switch
- Overrides all automation
- Controls both trees and lights simultaneously
- Respects rain sensor for outdoor lights

### Rain Protection
When rain is detected:
1. Outdoor lights will not turn on
2. Notification sent: "Christmas lights not coming on because it is raining" (automatic) or "Cannot turn on Christmas lights - it is raining" (manual)
3. Indoor trees operate normally
4. ChristmasLights virtual switch set to OFF
5. Main lights remain off until rain clears

### Porch Light Management
- When outdoor decorations turn ON: Porch lights turn OFF after 5 minutes (300 seconds)
- When outdoor decorations turn OFF: Porch lights immediately turn ON
- Reduces redundant lighting and saves energy

### Sync Prevention
The app uses atomic state flags to prevent infinite loops:
- When app changes virtual switch state, it sets a sync flag
- Events from virtual switches are ignored during sync
- Sync flags clear after 2 seconds
- Prevents cascading events between app and virtual switches

## Example Configurations

### Traditional Setup
- **Season**: November 25 - January 6
- **Turn On**: Sunset
- **Turn Off**: 22:00 (10 PM)
- **Rain Protection**: Enabled

### Extended Holiday Season
- **Season**: November 1 - February 14 (Thanksgiving through Valentine's)
- **Turn On**: 17:00
- **Turn Off**: 23:00 (11 PM)

### Mode-Based Control
- **Turn On**: When mode changes to "Evening"
- **Turn Off**: When mode changes to "Night"
- **Ignores**: Time and sunset (mode-driven only)

## Troubleshooting

### Lights don't turn on at sunset
- Verify Enable Schedule is checked
- Check current date is within season range
- Confirm devices are selected in configuration
- Review logs for "Date is outside range" messages

### Outdoor lights won't turn on
- Check rain sensor status (should be OFF for clear weather)
- Verify rain sensor switch is configured correctly
- Check notification devices for rain alerts
- Ensure Main Christmas Lights are selected

### Porch lights stay off
- Verify porch lights are selected in configuration
- Check if outdoor decorations are still on
- Porch lights should restore when decorations turn off

### Virtual switches out of sync
- App automatically syncs virtual switches to actual device states
- If out of sync, check logs for "Ignoring switch event" messages
- Try manually toggling virtual switch off and on

### Trees turn on but lights don't
- This is normal during rain - check rain sensor
- Trees and lights operate independently
- Outdoor lights have additional rain protection

### Schedule not working after hub restart
- App includes catchup logic on initialization
- Check logs for "Checking if lights should be on now" message
- Verify mode/time conditions are currently met

## Tips
- Create virtual switches before configuring the app for best results
- Test rain sensor by manually turning it ON to verify protection works
- Use Mode-based control for integration with other home automation
- Enable notifications to stay informed of rain-related events
- Porch lights automatically manage themselves - no manual intervention needed
- Schedule both Sunset turn-on and 22:00 turn-off for traditional operation
- Set up Master Switch for quick manual override of all decorations
