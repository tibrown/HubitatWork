# Rule Analysis for Night Apps

This document analyzes the existing rules in the `Apps/Night` folder to consolidate them into a single Hubitat App.

## Common Conditions
- **Mode**: All rules require the mode to be "Night".
- **AlarmsEnabled**: Many rules check if `AlarmsEnabled` is on.
- **Silent**: Many rules check if `Silent` is off.

## Rule Details

| Rule Name | Trigger | Conditions | Actions |
| :--- | :--- | :--- | :--- |
| **Night-BHScreenOpen** | `DoorBHScreen` open | Mode is Night AND `traveling` is off | Notify: "Birdhouse screen door is open" |
| **Night-CarPortBeam** | `CarportBeam` changed | Mode is Night AND `Silent` off AND `CarPortFrontMotion` active AND (`TimeDiffCPFront` <= 120 OR `TimeDiffRing` <= 120 OR `HighAlert` on) | Set Vars, Notify: "Intruder in carport", Run `ExecuteAlarms` (delayed) |
| **Night-CarPortBeamLog** | `CarportBeam` closed | Mode is Night | Notify: "Car Port Beam Broken" |
| **Night-IntruderConcreteShed** | `ConcreteShedZooz` open | Mode is Night AND `AlarmsEnabled` on AND `Siren1` off | Notify: "Concrete Shed Door Open", Run `ExecuteShedSiren`, `TurnAllLightsOn` |
| **Night-IntruderDRFrontDoor** | `DoorDiningRoom` open | Mode is Night AND `AlarmsEnabled` on AND `Silent` off AND `PauseDRDoorAlarm` off | `TurnAllLightsOn`, Notify: "Intrusion at Dining Room Door", Run `ExecuteAlarms` (delayed) |
| **Night-IntruderLRFrenchDoors** | `DoorLivingRoomFrench` open | Mode is Night AND `AlarmsEnabled` on | `TurnAllLightsOn`, Notify: "Intrusion at Living Room French Doors", Run `ExecuteAlarms` (delayed) |
| **Night-IntruderLRFrontDoor** | `DoorFront` open | Mode is Night AND `AlarmsEnabled` on AND `Silent` off | `TurnAllLightsOn`, Notify: "Intruder at Living Room Front Door", Run `ExecuteAlarms` (delayed) |
| **Night-IntruderWoodshed** | `WoodshedDoor` open | Mode is Night AND `Silent` off AND `MasterOff` off AND `AlarmsEnabled` on | Notify: "Woodshed Door open", Run `ExecuteShedSiren`, `TurnAllLightsOn` |
| **Night-PersonAtFrontDoor** | `RPDFrontDoor` on | Mode is Night | Notify: "Person at front door", Turn on `All Lights ON` |
| **Night-RPDBirdHouse** | `RPDBirdHouse` on | Mode is Night | Notify: "Person at Bird House", Turn on lights, Set `EchoMessage`, Run `WhisperToGuestroom` |
| **Night-RPDGarden** | `RPDGarden` on | Mode is Night | Notify: "Person at greenhouse", Turn on `All Lights ON` |
| **Night-RPDRearGate** | `RPDCPen` on | Mode is Night AND `ChickenPenOutside` motion active AND TimeDiffs | Set Vars, Turn on `RearGateActive`, Notify: "Someone at rear gate" |
| **Night-SheShedDoorOpen** | `DoorBirdHouse` open | Mode is Night AND `Silent` off | Turn on `All Lights ON`, Notify: "Intruder in bird house", Run `ExecuteShedSiren` |
| **Night-BackdoorMotion** | `OutsideBackdoor` motion active | Mode is Night AND `Flood-Side` active AND `HighAlert` on | `TurnAllLightsOn`, Notify: "Motion outside backdoor" |
| **NightIntruderAtBackdoor** | `DoorLanai` open | Mode is Night AND `PauseBDAlarm` off AND `Silent` off AND `AlarmsEnabled` on | Set `AlertMessage`, Run `ExecuteAlarms` |

## Variable Management
- **TimeNow**: Used in CarPortBeam and RPDRearGate.
- **TimeDiffCPFront**, **TimeDiffRing**: Used in CarPortBeam.
- **TimeDiff**, **TimePenDiff**: Used in RPDRearGate.
- **EchoMessage**: Used in RPDBirdHouse.
- **AlertMessage**: Used in NightIntruderAtBackdoor.

## Helper Apps/Rules Called
- `ExecuteAlarms`
- `ExecuteShedSiren`
- `TurnAllLightsOn`
- `WhisperToGuestroom`
