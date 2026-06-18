def personDetectedHandler(evt) {
    log.debug "person detected: ${evt.value}"

    if (nightModeSwitches && nightModeSwitches.any { it.currentValue("switch") == "on" }) {
        // Perform actions for night mode
        log.info "Night mode active, performing actions..."
        performNightModeActions()
    }

    if (notificationOnlySwitches && notificationOnlySwitches.any { it.currentValue("switch") == "on" }) {
        sendNotification("Person detected at ${location.name}")
        log.info "Notification sent"
    }
}
