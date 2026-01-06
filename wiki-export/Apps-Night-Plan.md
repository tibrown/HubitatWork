# Plan for Night Security App

This plan outlines the creation of a single Hubitat App to replace the individual rules in the `Apps/Night` folder.

## App Structure

### Definition
- **Name**: Night Security Manager
- **Namespace**: hubitat
- **Author**: GitHub Copilot
- **Description**: Manages all night-time security rules and notifications.
- **Category**: Security

### Preferences
The app will need inputs for all the devices and variables used in the rules.

#### Devices
- **Contact Sensors**:
    - `DoorBHScreen`
    - `CarportBeam`
    - `ConcreteShedZooz`
    - `DoorDiningRoom`
    - `DoorLivingRoomFrench`
    - `DoorFront`
    - `WoodshedDoor`
    - `DoorBirdHouse` (SheShed)
    - `DoorLanai` (Backdoor)
- **Motion Sensors**:
    - `CarPortFrontMotion`
    - `ChickenPenOutside`
    - `OutsideBackdoor`
    - `Flood-Side` (Sengled-Flood-2)
- **Switches/Virtual Devices**:
    - `traveling`
    - `Silent`
    - `HighAlert`
    - `AlarmsEnabled`
    - `Siren1`
    - `PauseDRDoorAlarm`
    - `MasterOff`
    - `RPDFrontDoor`
    - `All Lights ON`
    - `RPDBirdHouse`
    - `marjis light`
    - `BirdHouseOutside`
    - `RPDGarden`
    - `RPDCPen`
    - `RearGateActive`
    - `PauseBDAlarm`
- **Notification Devices**:
    - `PhoneProxy` (ID 442) - Used for phone notifications.
    - `NotificationProxy` (ID 1347) - Used for general/audio notifications.

#### Variables (Global/Local)
- `TimeDiffCPFront`
- `TimeDiffRing`
- `TimeDiff`
- `TimePenDiff`
- `LastRearGateShock`
- `LastRingMotion`
- `LastCPFrontMotion`
- `EchoMessage`
- `AlertMessage`

### Logic Flow

1.  **Initialization**: Subscribe to all relevant events (contact opens, motion active, switch turns on).
2.  **Event Handler**:
    - Check if **Mode is Night**. If not, exit (unless specific rules apply otherwise, but analysis shows all are Night rules).
    - Switch based on `device.displayName` or `deviceId` to execute specific logic.

#### Specific Logic Blocks

- **BH Screen**: If `DoorBHScreen` open AND `traveling` off -> Notify `PhoneProxy`.
- **Carport Beam**:
    - If `CarportBeam` changed:
        - Update Time Vars.
        - If `Silent` off AND `CarPortFrontMotion` active AND (Time logic OR `HighAlert` on) -> Notify `PhoneProxy`, Run `ExecuteAlarms` (5s delay).
    - If `CarportBeam` closed -> Notify `PhoneProxy` "Beam Broken".
- **Concrete Shed**: If `ConcreteShedZooz` open AND `AlarmsEnabled` on AND `Siren1` off -> Notify `NotificationProxy`, Run `ExecuteShedSiren`, `TurnAllLightsOn`.
- **Dining Room Door**: If `DoorDiningRoom` open AND `AlarmsEnabled` on AND `Silent` off AND `PauseDRDoorAlarm` off -> `TurnAllLightsOn`, Notify `NotificationProxy` AND `PhoneProxy`, Run `ExecuteAlarms` (5s delay).
- **LR French Doors**: If `DoorLivingRoomFrench` open AND `AlarmsEnabled` on -> `TurnAllLightsOn`, Notify `NotificationProxy` AND `PhoneProxy`, Run `ExecuteAlarms` (5s delay).
- **Front Door**: If `DoorFront` open AND `AlarmsEnabled` on AND `Silent` off -> `TurnAllLightsOn`, Notify `NotificationProxy`, Run `ExecuteAlarms` (5s delay).
- **Woodshed**: If `WoodshedDoor` open AND `Silent` off AND `MasterOff` off AND `AlarmsEnabled` on -> Notify `NotificationProxy`, Run `ExecuteShedSiren`, `TurnAllLightsOn`.
- **Person at Front Door**: If `RPDFrontDoor` on -> Notify `NotificationProxy`, Turn on `All Lights ON`.
- **Bird House**: If `RPDBirdHouse` on -> Notify `NotificationProxy` AND `PhoneProxy`, Turn on lights, Set `EchoMessage`, Run `WhisperToGuestroom`.
- **Garden**: If `RPDGarden` on -> Notify `NotificationProxy` AND `PhoneProxy`, Turn on `All Lights ON`.
- **Rear Gate**: If `RPDCPen` on AND `ChickenPenOutside` active AND Time logic -> Set Vars, Turn on `RearGateActive`, Notify `PhoneProxy`.
- **She Shed**: If `DoorBirdHouse` open AND `Silent` off -> Turn on `All Lights ON`, Notify `NotificationProxy`, Run `ExecuteShedSiren`.
- **Backdoor Motion**: If `OutsideBackdoor` active AND `Flood-Side` active AND `HighAlert` on -> `TurnAllLightsOn`, Notify `PhoneProxy`.
- **Intruder Backdoor**: If `DoorLanai` open AND `PauseBDAlarm` off AND `Silent` off AND `AlarmsEnabled` on -> Set `AlertMessage`, Run `ExecuteAlarms`.

### Implementation Steps
1.  Create `NightSecurityManager.groovy` in `Apps/Night`.
2.  Define `definition` and `preferences`.
3.  Implement `installed()` and `updated()` to handle subscriptions.
4.  Implement `evtHandler(evt)` to process events.
5.  Implement helper methods for notifications and running other rules (if possible via `location.helloHome?.executeAction` or similar, otherwise just logging/notification for now as direct rule execution might need specific setup). *Note: Rule Machine rules can be run via `Rule Machine` API or by setting a global variable/switch that triggers them.*

## Notes
- The "Run Actions" for other rules (e.g., `ExecuteAlarms`) implies these are Rule Machine rules. In a custom app, we might need to trigger them via a virtual switch or see if we can invoke them directly. For this plan, we will assume we can trigger them or replicate their logic if simple.
- Time difference logic requires tracking `now()` and comparing with stored timestamps.
