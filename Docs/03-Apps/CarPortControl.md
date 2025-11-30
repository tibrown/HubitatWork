# CarPort Control

## Overview
**CarPort Control** is a security app that manages the logic for the Carport Beam sensor across different modes (Away, Day, Evening, Morning). It handles notifications and alerts based on beam interruptions and motion detection.

## Features
- **Mode-Specific Logic**: Executes different actions based on the current location mode.
- **Motion Verification**: Uses a motion sensor to verify presence before sending alerts in certain modes.
- **Notifications**: Sends notifications to selected devices when the beam is broken or an intruder is detected.
- **Silent Mode Support**: Respects "Silent" switches to suppress alerts when needed.

## Configuration
### Sensors
- **Carport Beam**: The contact sensor on the carport beam.
- **Carport Front Motion**: (Optional) Motion sensor to verify activity.
- **Front Door Ring Motion**: (Optional) Switch representing Ring motion.

### Switches
- **Silent Switch**: Global silent mode.
- **Silent Carport Switch**: Specific silent mode for the carport.
- **Pause Carport Beam Switch**: Switch to temporarily pause beam logic.
- **Traveling Switch**: Switch indicating if the user is traveling.

### Notifications
- **Notification Devices**: Devices to receive alerts.

## Logic Flow
The app subscribes to `CarportBeam` events.

### Event Handling
- **Open (Beam Broken)**: Logic handled based on mode (currently placeholder in code).
- **Closed (Beam Restored)**: Logic handled based on mode (Note: Logic seems to be mapped to `closed` event in current code, possibly due to wiring).

### Mode Logic
- **Away Mode**:
    - Checks if `CarportMotion` is active.
    - If active, sends "Alert: Carport Beam Broken".
- **Day Mode**:
    - (Logic to be implemented/documented based on `handleDayMode`)
- **Evening Mode**:
    - (Logic to be implemented/documented based on `handleEveningMode`)
- **Morning Mode**:
    - (Logic to be implemented/documented based on `handleMorningMode`)
