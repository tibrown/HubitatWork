# Christmas Control

## Overview
**Christmas Control** is a convenience app for unified control of Christmas trees and lights. It allows for manual control via a master switch and automated scheduling based on dates and time/sunset.

## Features
- **Master Control**: A single switch to toggle all Christmas devices on or off.
- **Seasonal Schedule**: Configurable start and end dates (Month/Day) for the holiday season.
- **Daily Schedule**:
    - **Turn On**: Can be set to a specific time or Sunset.
    - **Turn Off**: Set to a specific time (default 22:00).
- **Device Grouping**: Separate selection for Indoor Trees and Outdoor Lights.

## Configuration
### Triggers
- **Master Switch**: (Optional) Manual control switch.
- **Enable Schedule**: Toggle to enable/disable automated scheduling.
- **Season Dates**: Start Month/Day and End Month/Day.
- **On Time**: "Sunset" or specific Time.
- **Off Time**: Specific Time.

### Indoor Devices
- **Tree Switches**: Switches controlling indoor Christmas trees.

### Outdoor Devices
- **Main Christmas Lights**: Primary outdoor lights.
- **Secondary Christmas Lights**: Secondary outdoor lights.
- **Porch Lights**: Porch lights to include.
- **Rain Sensor**: (Optional) To prevent operation during rain (logic implied).
- **Notification Devices**: For status updates.

## Logic Flow
1.  **Initialization**: Subscribes to Master Switch and schedules (if enabled).
2.  **Master Switch Event**:
    - **On**: Calls `activateChristmas`.
    - **Off**: Calls `deactivateChristmas`.
3.  **Scheduled On**:
    - Checks if current date is within the configured season (`checkDate`).
    - If yes, turns on Master Switch (or directly activates devices).
4.  **Scheduled Off**:
    - Turns off Master Switch (or directly deactivates devices).
