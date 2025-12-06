# Ring Person Detection Manager

## Overview
The Ring Person Detection Manager monitors RPD (Ring Person Detection) virtual switches, sets corresponding LRP (Last Ring Person) hub variable timestamps, and takes mode-based actions (notifications, lights, etc.) when person detection occurs.

## Purpose
- Monitor RPD* virtual switches for person detection events
- Set LRP* hub variable timestamps when detection occurs
- Take mode-specific actions (Night, Evening, Day)
- Reset RPD switches after detection (with configurable delays)
- Send notifications and control lights based on current mode
- Consolidate all RPD-related rules into one app

## Rules Consolidated
This app consolidates **11 rules**:

| Rule Name | Rule ID | Function |
|-----------|---------|----------|
| RPDBirdHouse | 1677 | Set LRPBirdHouse, reset switch |
| RPDGarden | 1703 | Set LRPGarden, reset switch |
| RPDCPen | 1632 | Set LRPCPen, reset switch |
| RPDBackDoor | 1687 | Reset switch with delay, conditional night notification |
| RPDFrontDoor | 1604 | Reset switch with 10s delay |
| Night-RPDBirdHouse | 1700 | Night mode: notify, set EchoMessage, lights, whisper |
| Night-RPDGarden | 1701 | Night mode: notify, turn on all lights |
| Night-RPDRearGate | 865 | Night mode: time calcs with LRPCPen, notify, RearGateActive |
| EveningRPDGarden | 1702 | Evening mode actions for garden detection |
| RingBackdoorMotionReset | 1725 | Auto-reset backdoor motion detection |
| TurnRingMotionOff | 1190 | Reset motion detection states |

## RPD Switches Monitored

| Switch Name | Device ID | Hub Variable Set | Reset Delay |
|-------------|-----------|------------------|-------------|
| RPDBackDoor | 1283 | LRPBackDoor | 3 seconds |
| RPDBirdHouse | 1319 | LRPBirdHouse | Immediate |
| RPDFrontDoor | 1257 | LRPFrontDoor | 10 seconds |
| RPDGarden | 1333 | LRPGarden | Immediate |
| RPDCPen | 1318 | LRPCPen | Immediate |
| RPDRearGate | - | LRPRearGate | Immediate |

## How It Works

```
1. Ring camera detects person
2. Ring integration turns on RPD* virtual switch
3. This app detects switch turning on
4. App sets LRP* hub variable to current timestamp
5. App resets RPD switch (immediately or after delay)
6. App takes mode-based actions:
   - Night Mode: Notifications, lights on, whisper announcements
   - Evening Mode: Evening-specific actions
   - Day Mode: Minimal actions
7. Other apps can also react to LRP* timestamps
```

## Mode-Based Actions

### Night Mode Actions
| Location | Actions |
|----------|---------|
| BackDoor | Notify (if not silent) |
| BirdHouse | Notify, set EchoMessage, all lights on, whisper to guest room |
| FrontDoor | Notify, all lights on |
| Garden | Notify "greenhouse", all lights on |
| CPen | Notify, turn on RearGateActive switch |
| RearGate | Notify, turn on RearGateActive switch |

### Evening Mode Actions
| Location | Actions |
|----------|--------|
| Garden | Announce "Person detected at the garden" |

## Hub Variables Set

| Hub Variable | Type | Description |
|--------------|------|-------------|
| LRPBackDoor | Decimal | Timestamp of last person at backdoor |
| LRPBirdHouse | Decimal | Timestamp of last person at birdhouse |
| LRPFrontDoor | Decimal | Timestamp of last person at front door |
| LRPGarden | Decimal | Timestamp of last person at garden/greenhouse |
| LRPCPen | Decimal | Timestamp of last person at chicken pen |
| LRPRearGate | Decimal | Timestamp of last person at rear gate |
| EchoMessage | String | Set to detection message for Alexa announcements |

## Configuration

### RPD Switches
Select the RPD virtual switches triggered by Ring person detection.

### Mode Configuration
- **Night Modes**: Modes for enhanced security actions
- **Evening Modes**: Modes for evening-specific actions

### Notification Devices
- **Notification Devices**: Push notification devices
- **Alexa Device**: For spoken announcements
- **Guest Room Echo**: For whisper notifications

### Control Switches
- **Silent Mode Switch**: Disables all audible alerts
- **Silent Backdoor Switch**: Disables backdoor alerts specifically
- **All Lights ON Switch**: Turned on during night detections
- **Rear Gate Active Switch**: Turned on for rear gate/pen detections

### Timing Configuration
- **Backdoor Reset Delay**: Seconds before resetting backdoor switch (default: 3)
- **Front Door Reset Delay**: Seconds before resetting front door switch (default: 10)

## Integration Points

### Inbound (Triggers This App)
- **RPD* Switches**: When any RPD switch turns on

### Outbound (This App Controls)
- **LRP* Hub Variables**: Timestamps for other apps to monitor
- **EchoMessage Hub Variable**: For Alexa announcements
- **All Lights ON Switch**: Activated during night detections
- **Rear Gate Active Switch**: Activated for rear gate/pen detections
- **Notification Devices**: Receives alerts
- **Alexa/Echo Devices**: Voice announcements and whispers

## Code Structure

### Key Methods
- `handleRPD[Location](evt)`: Handler for each RPD switch
- `setLastPersonTime(location, hubVarName)`: Sets hub variable timestamp
- `sendNotification(message)`: Sends to all notification devices
- `isNightMode()`: Checks if current mode is night
- `isEveningMode()`: Checks if current mode is evening
- `isSilent()`: Checks silent switch state
- `resetRPD[Location]()`: Delayed switch reset methods

## Installation

1. **Ensure Hub Variables Exist**:
   ```
   Settings → Hub Variables → Add Variable
   - LRPBackDoor (Decimal): 0
   - LRPBirdHouse (Decimal): 0
   - LRPFrontDoor (Decimal): 0
   - LRPGarden (Decimal): 0
   - LRPCPen (Decimal): 0
   - LRPRearGate (Decimal): 0
   - EchoMessage (String): ""
   ```

2. **Install App**:
   - Apps → Add User App → Ring Person Detection Manager
   - Configure RPD switches
   - Set Night and Evening modes
   - Configure notification devices
   - Configure control switches
   - Set timing delays
   - Set logging level

3. **Disable Old Rules**:
   - Disable RPDBirdHouse (1677)
   - Disable RPDGarden (1703)
   - Disable RPDCPen (1632)
   - Disable RPDBackDoor (1687)
   - Disable RPDFrontDoor (1604)
   - Disable Night-RPDBirdHouse (1700)
   - Disable Night-RPDGarden (1701)
   - Disable Night-RPDRearGate (865)
   - Disable EveningRPDGarden (1702)

4. **Test**:
   - Turn on each RPD switch manually
   - Verify LRP* hub variables update
   - Test in Night mode - verify notifications and lights
   - Test in Evening mode
   - Verify silent switches work
   - Check that switch resets happen

## Testing Checklist

- [ ] RPDBackDoor: LRPBackDoor updated, switch resets after 3s
- [ ] RPDBirdHouse: LRPBirdHouse updated, switch resets immediately
- [ ] RPDFrontDoor: LRPFrontDoor updated, switch resets after 10s
- [ ] RPDGarden: LRPGarden updated, switch resets immediately
- [ ] RPDCPen: LRPCPen updated, switch resets immediately
- [ ] RPDRearGate: LRPRearGate updated, switch resets immediately
- [ ] Night mode: Notifications sent
- [ ] Night mode: All lights switch turns on
- [ ] Night mode: EchoMessage set correctly
- [ ] Night mode: Guest room whisper works
- [ ] Night mode: RearGateActive turns on for pen/gate detections
- [ ] Silent switch: Suppresses notifications
- [ ] Silent backdoor switch: Suppresses backdoor notifications only
- [ ] Evening mode: Evening actions work

## Troubleshooting
- **Hub variable not updating**: Check RPD switch is configured in app
- **No notifications**: Check notification devices configured, check silent switches
- **Lights not turning on**: Check All Lights ON switch configured
- **Switch not resetting**: Check timing configuration
- **Wrong mode actions**: Verify modes configured correctly

## Performance Notes
- **Lines of Code**: ~300
- **State Variables**: 0 (stateless)
- **Subscriptions**: Up to 6 (one per RPD switch)
- **Performance**: Lightweight, event-driven
- **Memory**: Minimal - no persistent state
