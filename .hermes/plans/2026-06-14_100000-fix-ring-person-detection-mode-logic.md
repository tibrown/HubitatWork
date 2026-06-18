# Plan: Fix RingPersonDetectionManager mode-based activation

## Goal
Modify `RingPersonDetectionManager` to ensure that it only activates and processes person detected events if the detected device is currently enabled for the active mode. Currently, the `handleRingPersonDetected` handler turns on any matching switch regardless of the current mode or configurations associated with those switches.

## Context
The manager currently has three categories of switches:
1. `rpdSwitches` (Night Mode)
2. `nightModeSoftSwitches` (Night Mode)
3. `notificationOnlySwitches` (Notification Only Modes)

The `handleRingPersonDetected` method turns on any switch matching the camera location without checking if that switch is configured for the *current* mode.

## Proposed Approach
Update `handleRingPersonDetected` to:
1. Identify the matching switch based on camera location.
2. Verify if the found switch belongs to one of the configured categories (RPD/Night, NightSoft, NotificationOnly).
3. Check if the current system mode is in the appropriate mode list for that switch's category:
    - If `rpdSwitches` or `nightModeSoftSwitches` match, confirm `location.currentMode` is in `nightModes`.
    - If `notificationOnlySwitches` match, confirm `location.currentMode` is in `notificationOnlyModes`.
4. Only call `matched.on()` if the mode check passes for the specific category.

## Implementation Steps
1. Refactor `handleRingPersonDetected` to categorize the match and then perform a conditional check against modes.
2. Implement validation methods or update existing ones (`isNightMode`, `isNotificationOnlyMode`) to take a switch or category context if needed, though they already check `location.mode`.
3. Ensure that the logic correctly identifies which group the matched switch belongs to before verifying the mode.

## Files to Change
- `/home/tim/gitrepos/HubitatWork/Apps/RingPersonDetectionManager/RingPersonDetectionManager.groovy`
