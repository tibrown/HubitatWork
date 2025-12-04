# Special Automations Manager

## Overview
The Special Automations Manager is a comprehensive Hubitat app that handles miscellaneous automations that don't fit into other specialized categories. It provides pet monitoring, work reminders, power monitoring, communication routing, and various convenience automations.

## Purpose
- Centralize diverse automation logic in one maintainable app
- Pet monitoring and care reminders
- Work-life balance features (meetings, PTO, wake-up)
- Critical infrastructure monitoring (power, water)
- Multi-channel communication routing
- Convenience automations (coffee, safe monitoring)

## Rules Consolidated
This app consolidates **20 rules**:

| Rule Name | Rule ID | Category | Function |
|-----------|---------|----------|----------|
| CarportZoneActiveSiren | 1656 | Pet | Dog in carport zone detection |
| DogIsOnTheFloor | 1698 | Pet | Dog floor sensor tracking |
| DogsOutside | 1780 | Pet | Dogs outside monitoring |
| DogOnFloorTest | 1669 | Pet | Dog floor sensor testing |
| DogsFedReset | 1645 | Pet | Daily feeding status reset |
| CheckDogsFed | 1646 | Pet | Feeding reminder |
| MeetingReminder | 866 | Work | Meeting notifications |
| MeetingTime | 1691 | Work | Meeting time indicator |
| PtoOn | 1690 | Work | PTO mode management |
| SetAwayDelay | 1644 | Mode | Delayed away mode |
| MainsDown | 1716 | Power | Mains power failure alert |
| MainsWatch | 1715 | Power | Mains power monitoring |
| SeeSlack | 1706 | Communication | Slack message reminder |
| CheckSafeLocked | 1672 | Security | Safe lock verification |
| WakeUpForWork | 1686 | Work | Morning wake-up alarm |
| NotifyPhone | 1718 | Communication | Phone notifications |
| TellAlexa | 694 | Communication | Alexa announcements |
| TellMe | 1347 | Communication | General notifications |
| PlaySO25 | 1658 | Audio | Audio playback |
| Heat Coffee | 1697 | Convenience | Coffee maker automation |

## Features

### Pet Monitoring
- **Dog Floor Detection**: Track when dog is on floor
- **Dogs Outside Tracking**: Monitor time dogs spend outside
- **Outside Timeout Alerts**: Alert if dogs outside too long
- **Feeding Reminders**: Daily feeding time reminders
- **Fed Status Reset**: Automatic daily reset
- **Carport Zone Detection**: Alert and siren for dog in restricted zone

### Work Reminders
- **Meeting Reminders**: Advance notice for upcoming meetings
- **Meeting Time Indicator**: Visual indicator during meetings
- **PTO Mode**: Toggle work vs. vacation mode
- **Wake-Up Alarm**: Weekday-specific wake-up alerts
- **Day-of-Week Filtering**: Different schedules for different days

### Power Monitoring
- **Mains Power Detection**: Alert when power fails
- **Battery Backup Tracking**: Know when running on backup
- **Delayed Alerts**: Prevent false alarms
- **Power Restoration Notifications**: Know when power returns
- **Water Monitoring**: Track water valve state

### Communication Routing
- **Multi-Channel Support**: Push, Alexa, Phone, Slack
- **Slack Integration**: Webhook-based Slack notifications
- **Slack Reminders**: Prompt to check Slack messages
- **Message Routing**: Context-aware delivery

### Miscellaneous Automations
- **Coffee Maker**: Scheduled morning coffee
- **Auto-Shutoff**: Safety timeout for appliances
- **Safe Monitoring**: Periodic lock status checks
- **Away Mode Delay**: Graceful transition to away
- **Audio Playback**: Scheduled or triggered sounds

## Configuration

### Pet Monitoring Settings
- **Dog Floor Sensor**: Contact sensor for dog presence
- **Dog On Floor Switch**: State indicator switch
- **Dogs Outside Switch**: Track when dogs go outside
- **Dogs Fed Switch**: Daily feeding status
- **Carport Zone Active**: Dog detection in carport
- **Carport Siren**: Alert device for carport zone
- **Feeding Reminder Time**: Daily reminder schedule
  - Hub Variable: `dogFeedingReminderTime` (HH:mm)
- **Fed Reset Time**: When to reset fed status
- **Dog Outside Timeout**: Minutes before alert (5-120)
  - Hub Variable: `dogOutsideTimeout`
- **Enable Feeding Reminder**: Toggle reminders
- **Enable Outside Alerts**: Toggle timeout alerts

### Work Reminders Settings
- **Calendar Device**: Calendar integration sensor
- **Meeting Reminder Advance**: Minutes before meeting (5-60)
  - Hub Variable: `meetingReminderAdvance`
- **Meeting Time Switch**: Indicator for active meetings
- **Enable Meeting Reminders**: Toggle meeting alerts
- **PTO Switch**: Work vs. vacation mode
- **Enable PTO Mode**: Toggle PTO features
- **Wake-Up Time**: Daily alarm time
  - Hub Variable: `wakeUpTime` (HH:mm)
- **Wake-Up Days**: Days to trigger alarm
- **Enable Wake-Up Alarm**: Toggle alarm

### Power Monitoring Settings
- **Mains Power Sensor**: Power source detection
- **On Mains Switch**: State indicator
- **Power Alert Delay**: Seconds before alert (10-300)
  - Hub Variable: `powerMonitorDelay`
- **Enable Mains Monitoring**: Toggle power alerts
- **Water Switch**: Water valve state indicator

### Communication Settings
- **Push Notification Devices**: Push notification targets
- **Alexa Device**: Voice announcement device
- **Phone Device**: Phone-specific notifications
- **Slack Webhook URL**: Slack integration endpoint
- **Enable Slack Notifications**: Toggle Slack messages
- **See Slack Switch**: Slack reminder trigger

### Miscellaneous Settings
- **Coffee Maker**: Smart plug for coffee maker
- **Coffee On Time**: Daily brew time
  - Hub Variable: `coffeeOnTime` (HH:mm)
- **Coffee On Days**: Days to brew
- **Enable Coffee Automation**: Toggle coffee feature
- **Safe Sensor**: Lock status sensor
- **Safe Check Interval**: Hours between checks (1-168)
  - Hub Variable: `safeCheckInterval`
- **Enable Safe Checks**: Toggle safe monitoring
- **Set Away Delay Switch**: Trigger delayed away mode
- **Away Mode Delay**: Minutes before mode change (1-60)
  - Hub Variable: `awayModeDelay`
- **Audio Device**: Audio playback device
- **Enable Audio Notifications**: Toggle audio features

## Hub Variable Support

The app supports the following hub variables for dynamic configuration:

| Hub Variable | Type | Description | Default |
|--------------|------|-------------|---------|
| `dogFeedingReminderTime` | String | Feeding reminder time (HH:mm) | Setting value |
| `dogOutsideTimeout` | Number | Dog outside timeout (minutes) | 30 |
| `meetingReminderAdvance` | Number | Meeting reminder advance (minutes) | 15 |
| `coffeeOnTime` | String | Coffee brew time (HH:mm) | Setting value |
| `wakeUpTime` | String | Wake-up alarm time (HH:mm) | Setting value |
| `safeCheckInterval` | Number | Safe check interval (hours) | 24 |
| `powerMonitorDelay` | Number | Power alert delay (seconds) | 30 |
| `awayModeDelay` | Number | Away mode delay (minutes) | 5 |

**Hub Variable Priority**: Hub variables take precedence over app settings. If a hub variable is not set, the app falls back to the configured setting value.

## Logic Flows

### Dog Feeding Reminder Flow
```
1. Scheduled at dogFeedingReminderTime
2. Check if dogsFedSwitch is ON
3. If not fed:
   - Alexa announcement
   - Push notification
4. If fed: Skip reminder
5. Daily reset at fedResetTime
```

### Dogs Outside Timeout Flow
```
1. DogsOutsideSwitch turns ON
2. Start timer for dogOutsideTimeout minutes
3. When timer expires:
   - Check if still outside (switch ON)
   - If yes: Send alert notification
   - If no: Cancel (already brought in)
```

### Mains Power Down Flow
```
1. Power source changes to "battery"
2. Set onMainsSwitch to OFF
3. Wait powerMonitorDelay seconds
4. If still on battery:
   - Send push notification
   - Alexa announcement
   - Slack message
5. When power restored:
   - Set onMainsSwitch to ON
   - Send restoration notification
```

### Coffee Automation Flow
```
1. Scheduled at coffeeOnTime
2. Check if today in coffeeOnDays
3. If yes:
   - Turn on coffee maker
   - Send notification
   - Schedule auto-shutoff (2 hours)
4. If no: Skip
```

### Meeting Reminder Flow
```
1. Calendar event detected
2. Calculate reminder time (event time - advance minutes)
3. At reminder time:
   - Turn on meetingTimeSwitch
   - Alexa announcement
   - Push notification
4. After 1 hour: Auto-reset switch
```

### Wake-Up Alarm Flow
```
1. Triggered at wakeUpTime
2. Check if today in wakeUpDays
3. If yes:
   - Alexa "Good morning"
   - Push notification
   - Play audio (if configured)
4. If no: Skip
```

## Integration Points

### Outbound (This App Calls)
None - standalone automations

### Inbound (Other Apps Can Monitor)
- **Meeting Time Switch**: Other apps can check meeting status
- **PTO Switch**: Other apps can adapt to vacation mode
- **On Mains Switch**: Other apps can check power status
- **Dogs Outside Switch**: Other apps can check dog location
- **Dogs Fed Switch**: Other apps can check feeding status

## Code Structure

### Key Methods
- **Pet Monitoring**:
  - `dogFloorHandler(evt)`: Dog floor sensor changes
  - `dogsOutsideHandler(evt)`: Dogs went outside
  - `dogsOutsideTimeout()`: Alert for extended outside time
  - `checkDogsFed()`: Check and remind feeding
  - `resetDogsFed()`: Daily reset
  - `carportZoneActiveHandler(evt)`: Dog in restricted zone

- **Work Reminders**:
  - `meetingEventHandler(evt)`: Calendar event changes
  - `meetingReminder()`: Send meeting notification
  - `ptoHandler(evt)`: PTO mode changes
  - `wakeUpAlarm()`: Morning wake-up

- **Power Monitoring**:
  - `powerSourceHandler(evt)`: Power source changes
  - `handleMainsDown()`: Power failure response
  - `handleMainsRestored()`: Power restoration
  - `sendMainsDownAlert()`: Delayed alert

- **Communication**:
  - `notify(destination, message)`: Route notifications
  - `sendSlackMessage(message)`: Slack webhook
  - `seeSlackHandler(evt)`: Slack reminder

- **Miscellaneous**:
  - `heatCoffee()`: Coffee maker automation
  - `checkSafeLocked()`: Safe status verification
  - `setAwayDelayHandler(evt)`: Delayed away mode
  - `setAwayMode()`: Execute mode change

- **Helpers**:
  - `getConfigValue(settingName, hubVarName)`: Get value from hub variable or setting
  - `convertValue(value, settingName)`: Type conversion

### State Variables
Minimal state usage - primarily event-driven

## Installation

1. **Create Hub Variables** (optional, for dynamic configuration):
   ```
   Settings → Hub Variables → Add Variable
   - dogFeedingReminderTime (String): "18:00"
   - dogOutsideTimeout (Number): 30
   - meetingReminderAdvance (Number): 15
   - coffeeOnTime (String): "06:30"
   - wakeUpTime (String): "06:00"
   - safeCheckInterval (Number): 24
   - powerMonitorDelay (Number): 30
   - awayModeDelay (Number): 5
   ```

2. **Configure Slack** (optional):
   - Create Slack webhook in your workspace
   - Copy webhook URL
   - Paste into app settings

3. **Install App**:
   - Apps → Add User App → Special Automations Manager
   - Navigate through configuration pages
   - Configure only features you need
   - Set logging level (recommend "Info" initially)

4. **Test Each Feature**:
   - Trigger dog sensors
   - Test feeding reminder
   - Verify power monitoring
   - Check coffee automation
   - Test wake-up alarm
   - Verify Slack integration

## Testing Checklist

### Pet Monitoring
- [ ] Dog floor sensor triggers switch
- [ ] Dogs outside timeout alerts
- [ ] Feeding reminder fires at correct time
- [ ] Fed status resets daily
- [ ] Carport zone triggers siren

### Work Reminders
- [ ] Meeting reminders work
- [ ] PTO mode toggles correctly
- [ ] Wake-up alarm fires on correct days
- [ ] Wake-up skips non-work days

### Power Monitoring
- [ ] Mains down alert triggers
- [ ] Power restoration notification sent
- [ ] Delay prevents false alarms

### Communication
- [ ] Push notifications delivered
- [ ] Alexa announcements work
- [ ] Slack messages received
- [ ] See Slack reminder triggers

### Miscellaneous
- [ ] Coffee maker turns on/off
- [ ] Safe check alerts work
- [ ] Away delay triggers correctly
- [ ] Hub variables override settings

## Maintenance

### Adjusting Schedules
- Modify time settings for feeding, coffee, wake-up
- Update day-of-week selections
- Adjust timeout and delay values

### Monitoring
- Review logs for automation triggers
- Check notification delivery
- Verify schedules execute correctly
- Monitor power events

### Troubleshooting
- **No notifications**: Check notification devices configured
- **Reminders not firing**: Verify time format (HH:mm)
- **Slack not working**: Verify webhook URL
- **Coffee not turning on**: Check day-of-week settings
- **Safe alerts too frequent**: Increase check interval

## Performance Notes
- **Lines of Code**: 660
- **State Variables**: Minimal (mostly event-driven)
- **Scheduled Jobs**: ~8 (depending on enabled features)
- **Subscriptions**: ~15 (depending on configured devices)
- **Performance**: Lightweight, efficient
- **Memory**: Low state usage

## Future Enhancements
- Google Calendar integration
- Advanced pet activity tracking
- Energy usage monitoring
- Weather-based automation
- Voice command integration
- Mobile app integration
- Machine learning for pattern detection
