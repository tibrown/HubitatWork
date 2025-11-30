# Night Security Manager

## Overview
**Night Security Manager** is a comprehensive security application designed to consolidate various night-time security rules into a single, cohesive logic engine. It monitors perimeter sensors, manages alarms, and controls lighting based on intrusion events during the night.

## Features
- **Consolidated Logic**: Replaces multiple individual Rule Machine rules.
- **Perimeter Monitoring**: Monitors doors, gates, beams, and motion sensors.
- **Intrusion Response**:
    - **Notifications**: Sends alerts to phone and audio proxies.
    - **Alarms**: Activates sirens (Siren 1, 2, 3).
    - **Lighting**: Turns on all lights to deter intruders.
    - **Voice Alerts**: Whispers alerts to the Guest Room Echo.
- **Conditional Execution**:
    - **Mode Restrictions**: Only runs in specified modes (e.g., Night).
    - **Silent Mode**: Respects "Silent" switches to suppress audible alarms.
    - **Alarm State**: Checks "Alarms Enabled" before triggering sirens.
    - **Travel Mode**: Specific logic for when the user is traveling.

## Configuration
### Sensors
- **Contacts**: BH Screen Door, Carport Beam, Concrete Shed, Dining Room, Living Room French, Front Door, Woodshed, She Shed, Lanai Door.
- **Switches (as Sensors)**: RPD Front Door, RPD Bird House, RPD Garden, RPD Rear Gate.
- **Motion**: Carport Front, Chicken Pen Outside, Outside Backdoor, Flood Side.

### Switches & Controls
- **State Switches**: Traveling, Silent, High Alert, Alarms Enabled, Master Off, Rear Gate Active.
- **Pause Switches**: Pause DR Door Alarm, Pause Backdoor Alarm.
- **Control Switches**: All Lights ON.

### Notification Devices
- **Notification Devices**: List of devices to receive text/audio alerts.

### Actions / Outputs
- **Sirens**: List of alarm devices to activate.
- **All Lights**: List of switches to turn on during an intrusion.
- **Guest Room Echo**: Notification device for voice alerts.

## Logic Flow
The app subscribes to all configured sensors. When an event occurs:
1.  **Mode Check**: Verifies if the current mode is allowed.
2.  **Event Handler**: Specific handler for each device (e.g., `handleFrontDoor`).
3.  **Conditions**: Checks relevant state switches (Alarms Enabled, Silent, etc.).
4.  **Actions**:
    - Sends notifications.
    - Turns on lights (`turnAllLightsOnNow`).
    - Activates sirens (`executeAlarmsOn` or `executeShedSirenOn`).
    - Sets Global Variables (e.g., "EchoMessage", "AlertMessage").

### Key Actions
- **executeAlarmsOn**: Turns on all sirens for 5 minutes.
- **executeShedSirenOn**: Turns on sirens for 4 seconds (warning blast).
- **turnAllLightsOnNow**: Turns on the "All Lights" group and the master "All Lights ON" switch.
- **whisperToGuestroomNow**: Reads "EchoMessage" global variable and speaks it on the Guest Room Echo.
