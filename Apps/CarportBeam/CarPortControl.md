# CarPort Control

## Overview
**CarPort Control** is a mode-aware security app that monitors the carport beam sensor and responds differently based on the current hub mode (Away, Day, Evening, or Morning). It provides intelligent intrusion detection with motion verification and temporary pause capabilities.

## Features
- **Mode-Specific Behavior**: Different responses for Away, Day, Evening, and Morning modes
- **Motion Verification**: Requires motion sensor confirmation in certain modes to reduce false alarms
- **Flexible Silent Modes**: Both global and carport-specific silent switches
- **Temporary Pause**: Ability to temporarily disable carport monitoring
- **Auto-Reset**: Automatic re-enabling after timeout periods
- **Smart Notifications**: Context-aware messages based on mode and conditions

## Setup in Hubitat Hub

### Installation
1. Navigate to **Apps Code** in Hubitat
2. Click **New App**
3. Paste the `CarPortControl.groovy` code
4. Click **Save**
5. Navigate to **Apps**
6. Click **Add User App**
7. Select **CarPortControl**

### Configuration Options

#### Sensors Section
- **Carport Beam** (required): Contact sensor mounted on carport beam
  - **Note**: This sensor may be wired in reverse (closed = beam broken)
- **Carport Front Motion** (optional): Motion sensor in carport area for verification
- **Front Door Ring Motion (Switch)** (optional): Ring doorbell motion detection as switch

#### Switches Section
- **Silent Switch** (optional): Global silent mode suppresses alerts
- **Silent Carport Switch** (optional): Carport-specific silent mode
- **Pause Carport Beam Switch** (optional): Temporarily disables beam monitoring
- **Traveling Switch** (optional): Indicates traveling status (used by other apps)

#### Notifications Section
- **Notification Devices** (optional, multiple): Devices to receive push notifications

## How It Works

### Sensor Logic

**Important**: The carport beam sensor logic responds to the `closed` event (beam broken) rather than `open` due to reversed wiring on the physical sensor.

When the beam is broken (contact closes), the app:
1. Checks the current hub mode
2. Routes to the appropriate mode handler
3. Evaluates conditions specific to that mode
4. Executes notifications and switch controls

### Mode-Specific Behavior

#### Away Mode
- **Purpose**: Detect any activity while you're away
- **Trigger**: Carport beam broken (contact closes)
- **Conditions**: Carport Front Motion must be active
- **Action**: Send notification "Alert:Carport Beam Broken"
- **Logic**: Motion verification prevents false alarms from objects, animals, or weather

#### Day Mode
- **Purpose**: Monitor during daytime with multiple verification methods
- **Trigger**: Carport beam broken (contact closes)
- **Conditions Checked**:
  - Silent Switch must be OFF
  - Silent Carport Switch must be OFF
  - Pause Carport Beam Switch must be OFF
  - Motion detected (Carport Front Motion active OR Front Door Ring Motion on)
- **Actions**:
  - Send notification "Carport Beam Broken"
  - Turn ON Pause Carport Beam Switch
  - Automatically turn OFF Pause switch after 5 minutes (300 seconds)
- **Logic**: Requires motion verification and respects multiple silent/pause conditions. Auto-pause prevents repeated notifications for the same event.

#### Evening Mode
- **Purpose**: Simple notification during evening hours
- **Trigger**: Carport beam broken (contact closes)
- **Condition**: Silent Switch must be OFF
- **Action**: Send notification "Carport Beam Broken, Carport Beam Broken" (double message for emphasis)
- **Logic**: Minimal verification, assumes higher likelihood of valid activity in evening

#### Morning Mode
- **Purpose**: Detect unexpected activity during morning hours
- **Trigger**: Carport beam broken (contact closes)
- **Conditions Checked**:
  - Silent Switch must be OFF
  - Silent Carport Switch must be OFF
- **Actions**:
  - Send notification "Intruder in the carport"
  - Turn ON Silent Carport Switch
  - Automatically turn OFF Silent Carport switch after 2 minutes (120 seconds)
- **Logic**: More serious notification ("Intruder") and auto-silences for 2 minutes to prevent alert fatigue

### Auto-Reset Timers

#### Day Mode (5-minute pause)
- Pause Carport Beam Switch turns ON
- After 300 seconds, automatically turns OFF
- Prevents repeated alerts for same person/vehicle
- Allows resumption of monitoring after reasonable delay

#### Morning Mode (2-minute silence)
- Silent Carport Switch turns ON
- After 120 seconds, automatically turns OFF
- Shorter timeout for morning activity
- Balances alert reduction with security

## Notification Messages

| Mode | Message | Meaning |
|------|---------|---------|
| Away | "Alert:Carport Beam Broken" | Activity detected while away |
| Day | "Carport Beam Broken" | Normal beam break notification |
| Evening | "Carport Beam Broken, Carport Beam Broken" | Emphasized evening notification |
| Morning | "Intruder in the carport" | Serious morning alert |

## Troubleshooting

### No notifications in any mode
- Verify notification devices are configured
- Check that devices are online
- Review logs for event detection
- Confirm beam sensor is triggering events

### Notifications in Day mode even when paused
- Check Pause Carport Beam Switch status
- Verify switch is selected in configuration
- Review logs for auto-reset timing
- May need to manually turn on pause switch

### False alarms in Away mode
- Ensure Carport Front Motion sensor is positioned correctly
- Check for animals, wind-blown objects, or sensor sensitivity
- Motion must be active for notification to send
- Consider adjusting motion sensor settings

### Morning mode alerts too frequent
- Silent Carport Switch should auto-reset after 2 minutes
- If not resetting, check switch device functionality
- Review logs for runIn() execution
- Manually toggle Silent Carport switch to test

### Evening double notifications annoying
- This is intentional for emphasis in Evening mode
- Set Silent Switch to ON to suppress
- Or modify code to send single message

### Beam sensor seems reversed
- This is expected - sensor wiring is reversed
- App responds to `closed` event (beam broken)
- `open` event indicates beam is clear
- Do not attempt to "fix" this without rewiring physical sensor

## Tips

### Effective Use of Silent Modes
- **Silent Switch**: Suppresses alerts across multiple apps (global)
- **Silent Carport Switch**: Only affects carport in Morning mode
- Use Silent Switch when expecting activity (guests, deliveries)
- Use Silent Carport for morning routines when you're in the area

### Pause Switch Usage
- Manually turn ON Pause Carport Beam to disable monitoring temporarily
- Useful when working in carport or expecting frequent traffic
- Day mode automatically manages pause to prevent alert fatigue
- Remember to manually turn OFF if you set it manually

### Motion Sensor Placement
- Position Carport Front Motion to cover approach area
- Avoid aiming at trees, flags, or other moving objects
- Test by walking through carport and checking logs
- Adjust sensitivity if getting too many or too few activations

### Integration with Other Apps
- Works alongside Night Security Manager for comprehensive coverage
- Traveling Switch can be used by multiple apps
- Silent Switch provides house-wide notification control
- Consider mode-based automation to set Silent switch automatically
