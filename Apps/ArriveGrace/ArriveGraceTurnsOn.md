# Arrive Grace Turns On

## Overview
**Arrive Grace Turns On** is a convenience automation app that creates a temporary "grace period" when you arrive home. During this period, security alarms are disabled and silent mode is activated, allowing you to enter your home and move about without triggering security alerts. After the configured duration, all security settings automatically restore.

## Features
- **One-Switch Activation**: Single switch triggers entire grace period sequence
- **Automatic Alarm Disabling**: Turns off security alarms upon arrival
- **Silent Mode Activation**: Suppresses notifications and alerts during grace period
- **Configurable Duration**: Set grace period length from 1-60+ minutes
- **Automatic Restoration**: All settings return to normal after grace period expires
- **Hands-Free Operation**: Once triggered, requires no further interaction

## Setup in Hubitat Hub

### Installation
1. Navigate to **Apps Code** in Hubitat
2. Click **New App**
3. Paste the `ArriveGraceTurnsOn.groovy` code
4. Click **Save**
5. Navigate to **Apps**
6. Click **Add User App**
7. Select **Arrive Grace Turns On**

### Configuration Options

#### Trigger Device Section
- **Arrive Grace Period Switch** (required): Virtual switch that activates the grace period
  - Typically triggered by presence sensors, geofencing, or automation
  - Can be manually triggered via dashboard or voice command

#### Devices to Control Section
- **Alarms Enabled Switch** (required): Master switch controlling security alarm system
- **Silent Mode Switch** (required): Switch that suppresses notifications and alerts

#### Settings Section
- **Grace Duration** (required): Length of grace period in minutes
  - Default: 30 minutes
  - Range: Any positive number (typically 5-60 minutes)
  - Recommended: 15-30 minutes for typical arrival scenarios

## How It Works

### Activation Sequence

When the Arrive Grace Period Switch turns ON:

1. **Disable Alarms** (Immediate)
   - Alarms Enabled switch turns OFF
   - Prevents security system from triggering sirens
   - Allows movement through monitored areas

2. **Enable Silent Mode** (Immediate)
   - Silent Mode switch turns ON
   - Suppresses push notifications
   - Mutes audio alerts
   - Other apps respect this switch to stay quiet

3. **Schedule Restoration** (Delayed)
   - Timer set for configured Grace Duration
   - Runs in background
   - No user interaction needed

### Restoration Sequence

When Grace Duration expires:

1. **Re-enable Alarms**
   - Alarms Enabled switch turns ON
   - Security system resumes normal monitoring
   - Intrusions will now trigger alarms

2. **Disable Silent Mode**
   - Silent Mode switch turns OFF
   - Notifications resume
   - Audio alerts resume

3. **Reset Trigger**
   - Arrive Grace Period switch turns OFF
   - System ready for next arrival
   - Indicates grace period has ended

### Integration Points

#### Triggering the Grace Period
Common methods to turn ON the Arrive Grace Period switch:
- **Presence Sensors**: Geofencing via mobile app
- **Mode Changes**: When hub changes to "Home" or "Day" mode
- **Time-based**: Scheduled arrival times (e.g., end of workday)
- **Voice Commands**: Alexa/Google "Turn on Arrive Grace Period"
- **Dashboard Button**: Manual tap when approaching home
- **Smart Lock**: When specific user code unlocks door

#### Compatible Apps
This app works alongside:
- **Night Security Manager**: Respects Silent Mode switch
- **CarPort Control**: Respects Silent Switch (often same as Silent Mode)
- **Other Security Apps**: Any app checking Alarms Enabled or Silent Mode

## Example Usage Scenarios

### Scenario 1: After Work Arrival
- **Setup**: Geofencing triggers switch when within 1 mile of home
- **Duration**: 20 minutes
- **Benefit**: Allows unloading car, bringing in packages, settling in
- **Result**: After 20 minutes, security resumes automatically

### Scenario 2: Expected Guest
- **Setup**: Manually activate via dashboard 5 minutes before guest arrives
- **Duration**: 15 minutes
- **Benefit**: Guest can enter through monitored doors without triggering alarms
- **Result**: Security resumes after guest is settled

### Scenario 3: Morning Routine
- **Setup**: Mode change from Night to Morning activates switch
- **Duration**: 30 minutes
- **Benefit**: Morning activities (taking out trash, getting mail) don't trigger alerts
- **Result**: Daytime security settings resume automatically

### Scenario 4: Package Delivery
- **Setup**: Voice command "Alexa, turn on arrive grace"
- **Duration**: 10 minutes
- **Benefit**: Open doors for delivery without alarms
- **Result**: Quick grace period for brief interaction

## State Diagram

```
NORMAL SECURITY STATE
  ↓
[Arrive Grace Period Switch] turns ON
  ↓
GRACE PERIOD ACTIVE
├─ Alarms Enabled: OFF
├─ Silent Mode: ON
└─ Timer running: {Grace Duration} minutes
  ↓
{Grace Duration} expires
  ↓
AUTOMATIC RESTORATION
├─ Alarms Enabled: ON
├─ Silent Mode: OFF
└─ Arrive Grace Period: OFF
  ↓
NORMAL SECURITY STATE (restored)
```

## Troubleshooting

### Grace period doesn't activate
- Verify Arrive Grace Period switch is actually turning ON
- Check app is installed and active
- Review logs for errors
- Confirm all required switches are configured

### Alarms don't turn back on
- Check that grace duration has fully elapsed
- Verify Alarms Enabled switch device is online
- Review logs for `endGracePeriod` execution
- Check if runIn() timer was cancelled (hub restart)

### Grace period ends too soon/late
- Verify Grace Duration setting (minutes, not seconds)
- Check hub time zone is correct
- Review logs for actual timer execution
- Confirm no other automation is conflicting

### Silent mode stays on after grace period
- Verify Silent Mode switch is configured correctly
- Check device is responding to OFF commands
- Review logs for restoration sequence
- Manually turn off Silent Mode and test again

### Grace period switch won't turn off
- App automatically turns it OFF at end of grace period
- If stuck ON, manually turn OFF and review logs
- Check for conflicting automation keeping it ON
- Verify switch device is functioning properly

## Tips

### Optimal Duration Settings
- **Quick errands (5-10 min)**: Running to car, checking mailbox
- **Normal arrival (15-20 min)**: Coming home from work, unloading car
- **Extended arrival (30-45 min)**: Shopping trip, bringing in lots of items
- **Guest visits (30-60 min)**: Expected visitors settling in

### Best Practices
- Use virtual switch for Arrive Grace Period (not physical)
- Create dashboard tile for manual activation
- Set up geofencing with 0.5-1 mile radius for timing
- Test the full sequence before relying on it
- Consider separate grace periods for different scenarios
- Monitor logs during first few uses to verify timing

### Integration Recommendations
- Link to presence detection for automatic triggering
- Create voice shortcuts for quick activation
- Add to bedtime routine if coming home late
- Combine with mode changes for seamless operation
- Use with smart lock codes for family members

### Safety Considerations
- Don't set duration too long (security gap)
- Ensure automatic restoration works reliably
- Test alarm restoration before depending on it
- Consider notification when grace period ends
- Have manual override method available
- Monitor logs to detect any failures

## Advanced Usage

### Multiple Grace Periods
Create separate instances for different scenarios:
- **Quick Grace**: 10 minutes for brief entries
- **Standard Grace**: 30 minutes for normal arrivals
- **Extended Grace**: 60 minutes for special situations

### Notification Enhancement
Add to `endGracePeriod()` function:
```groovy
notificationDevices.deviceNotification("Grace period ended - Security restored")
```

### Mode-Based Activation
Configure mode change automation:
- Night → Morning: Activate 15-minute grace
- Away → Home: Activate 20-minute grace
- Evening → Night: Skip grace (already home)
