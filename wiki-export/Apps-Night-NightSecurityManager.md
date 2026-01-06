# Night Security Manager

## Overview
**Night Security Manager** is a comprehensive security application that consolidates multiple night-time security rules into a single, cohesive logic engine. It monitors perimeter sensors, detects intrusions, manages alarm responses, and controls lighting to protect your home during the night.

## Features
- **Consolidated Security Logic**: Replaces multiple individual Rule Machine rules with a unified app
- **Multi-sensor Monitoring**: Monitors doors, motion sensors, contact sensors, and Ring Person Detection (RPD) devices
- **Intelligent Alarm Response**: 
  - Configurable siren activation with automatic shut-off
  - Differentiated responses (full alarm vs. warning blast)
  - Silent mode support for discreet operation
- **Automated Lighting**: Turns on all lights to deter intruders
- **Smart Notifications**: 
  - Push notifications to mobile devices
  - Voice alerts via Echo devices
  - Customizable messages per event
- **Conditional Logic**:
  - Mode-based restrictions (e.g., only run in Night mode)
  - Time-based conditions (e.g., 10:30 PM to 6:00 AM for carport)
  - Travel mode support
  - Pause switches for temporary disablement

## Setup in Hubitat Hub

### Installation
1. Navigate to **Apps Code** in Hubitat
2. Click **New App**
3. Paste the `NightSecurityManager.groovy` code
4. Click **Save**
5. Navigate to **Apps**
6. Click **Add User App**
7. Select **Night Security Manager**

### Configuration Options

#### Sensors Section
- **BH Screen Door** (required): Contact sensor for birdhouse screen door
- **Carport Beam** (required): Contact sensor (beam break detector)
- **Carport Front Motion** (required): Motion sensor in carport area
- **Concrete Shed Door** (required): Contact sensor for shed door
- **Dining Room Door** (required): Contact sensor for dining room entry
- **Living Room French Doors** (required): Contact sensor for French doors
- **Front Door** (required): Main entry door contact sensor
- **Woodshed Door** (required): Contact sensor for woodshed
- **RPD Front Door** (required): Ring Person Detection switch for front door
- **RPD Bird House** (required): Ring Person Detection switch for side yard
- **RPD Garden** (required): Ring Person Detection switch for garden area
- **RPD Rear Gate** (required): Ring Person Detection switch for rear gate
- **Chicken Pen Outside Motion** (required): Motion sensor near chicken pen
- **She Shed Door (BirdHouse)** (required): Contact sensor for she shed
- **Outside Backdoor Motion** (required): Motion sensor at back door
- **Flood Side Motion** (required): Motion sensor on side of house
- **Lanai Door (Backdoor)** (required): Contact sensor for lanai/back door

#### Switches & Controls Section
- **Traveling Switch** (required): When ON, modifies behavior (e.g., BH Screen notifications)
- **Silent Switch** (required): When ON, suppresses audible alarms
- **High Alert Switch** (required): When ON, enables enhanced monitoring (carport beam)
- **Alarms Enabled Switch** (required): Master switch to enable/disable sirens
- **Pause DR Door Alarm** (required): Temporarily disables dining room door alarm
- **Pause Backdoor Alarm** (required): Temporarily disables backdoor alarm
- **Rear Gate Active Switch** (required): Indicates rear gate monitoring is active
- **All Lights ON Switch** (required): Master switch that tracks all lights state

#### Notification Devices Section
- **Notification Devices** (required, multiple): Devices that receive push notifications

#### Actions / Outputs Section
- **Sirens** (required, multiple): Alarm devices to activate during intrusions
- **All Lights** (required, multiple): All light switches to turn on during alarms
- **Guest Room Echo** (required): Echo device for voice notifications

#### Restrictions Section
- **Only run in these modes** (required, multiple): Select modes when app is active (typically "Night")

## How It Works

### Event Monitoring
The app subscribes to all configured sensors and switches. When any sensor triggers, the app:
1. Checks if current mode is in the allowed modes list
2. Routes to specific handler based on which device triggered
3. Evaluates conditions specific to that device
4. Executes appropriate response actions

### Device-Specific Logic

#### BH Screen Door
- **Trigger**: Door opens
- **Condition**: Traveling switch is OFF
- **Action**: Send notification "BH Screen Door Open"

#### Carport Beam
- **Trigger**: Beam broken (contact opens)
- **Time Condition**: Between 10:30 PM and 6:00 AM OR High Alert is ON
- **Additional Conditions**: Silent OFF AND Carport Front Motion active
- **Actions**: 
  - Send notification "Alert! Intruder in the carport!"
  - Wait 5 seconds, then activate sirens for 5 minutes

#### Concrete Shed Door
- **Trigger**: Door opens
- **Conditions**: Alarms Enabled ON AND Siren1 is OFF
- **Actions**:
  - Send notification "Intruder in the Concrete Shed"
  - Activate siren warning blast (4 seconds)
  - Turn on all lights

#### Dining Room Door
- **Trigger**: Door opens
- **Conditions**: Alarms Enabled ON AND Silent OFF AND Pause DR Door Alarm OFF
- **Actions**:
  - Turn on all lights
  - Send notification "Intruder at the Dining Room Door"
  - Wait 5 seconds, then activate sirens for 5 minutes

#### Living Room French Doors
- **Trigger**: Doors open
- **Condition**: Alarms Enabled ON
- **Actions**:
  - Turn on all lights
  - Send notification "Intruder at the Living Room French Doors"
  - Wait 5 seconds, then activate sirens for 5 minutes

#### Front Door
- **Trigger**: Door opens
- **Conditions**: Alarms Enabled ON AND Silent OFF
- **Actions**:
  - Turn on all lights
  - Send notification "Intruder at the Front Door"
  - Wait 5 seconds, then activate sirens for 5 minutes

#### Woodshed Door
- **Trigger**: Door opens
- **Conditions**: Silent OFF AND Alarms Enabled ON
- **Actions**:
  - Send notification "Intruder in the Woodshed"
  - Activate siren warning blast (4 seconds)
  - Turn on all lights

#### RPD Front Door
- **Trigger**: Person detected (switch turns ON)
- **Actions**:
  - Send notification "Person at the Front Door"
  - Turn on All Lights ON switch

#### RPD Bird House
- **Trigger**: Person detected (switch turns ON)
- **Actions**:
  - Send notification "Intruder at the Bird House"
  - Turn on All Lights ON switch
  - Set global variable "EchoMessage" to alert text
  - Send voice alert to Guest Room Echo

#### RPD Garden
- **Trigger**: Person detected (switch turns ON)
- **Actions**:
  - Send notification "Intruder in the Garden"
  - Turn on All Lights ON switch

#### RPD Rear Gate
- **Trigger**: Person detected (switch turns ON)
- **Additional Condition**: Chicken Pen Outside Motion is active
- **Time Condition**: Between 8:00 PM and 6:00 AM
- **Actions**:
  - Set Rear Gate Active switch to ON
  - Send notification "Intruder at the Rear Gate"

#### She Shed Door
- **Trigger**: Door opens
- **Condition**: Silent OFF
- **Actions**:
  - Turn on All Lights ON switch
  - Send notification "Intruder in the She Shed"
  - Activate siren warning blast (4 seconds)

#### Outside Backdoor Motion
- **Trigger**: Motion detected
- **Conditions**: Flood Side Motion also active AND High Alert ON
- **Actions**:
  - Turn on all lights
  - Send notification "Intruder at the Backdoor"

#### Lanai Door (Backdoor)
- **Trigger**: Door opens
- **Conditions**: Pause BD Alarm OFF AND Silent OFF AND Alarms Enabled ON
- **Actions**:
  - Set global variable "AlertMessage" to alert text
  - Activate sirens for 5 minutes

### Response Actions

#### executeAlarmsOn()
- Checks if Alarms Enabled switch is ON
- Activates all configured sirens
- Automatically stops sirens after 5 minutes (300 seconds)

#### executeShedSirenOn()
- Activates all configured sirens immediately
- Automatically stops sirens after 4 seconds (warning blast)

#### turnAllLightsOnNow()
- Turns on all lights in the All Lights group
- Turns on the All Lights ON master switch

#### whisperToGuestroomNow()
- Reads the "EchoMessage" global variable
- Sends the message to Guest Room Echo for voice notification

## Global Variables Used
- **EchoMessage**: Stores message for Echo voice alerts
- **AlertMessage**: Stores general alert messages

## Troubleshooting

### Alarms not activating
- Check that Alarms Enabled switch is ON
- Verify Silent switch is OFF (if required for that rule)
- Confirm current mode is in the allowed modes list
- Check that siren devices are online

### False alarms from carport beam
- Ensure Carport Front Motion sensor is positioned correctly
- Check time conditions (10:30 PM - 6:00 AM or High Alert)
- Verify Silent switch is OFF

### Notifications not received
- Confirm notification devices are properly configured
- Check device connectivity
- Review notification device settings in Hubitat

### Lights not turning on
- Verify All Lights devices are selected and online
- Check All Lights ON switch functionality
- Review logs for errors

## Tips
- Use pause switches (Pause DR Door Alarm, Pause Backdoor Alarm) when expecting legitimate entry
- Set Silent switch ON when you need monitoring without audible alarms
- High Alert mode enhances carport beam monitoring outside normal hours
- Test each sensor individually during daytime to verify proper operation
- Review logs regularly to understand event patterns and adjust sensitivity
