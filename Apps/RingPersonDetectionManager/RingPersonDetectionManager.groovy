import groovy.json.JsonSlurper

class RingPersonDetectionManager {
    def config = [:]
    def nightModeDevices = []
    def notificationOnlyDevices = []

    RingPersonDetectionManager(context) {
        this.context = context
        
        // Load configuration
        loadConfig()
    }

    def loadConfig() {
        def jsonSlurper = new JsonSlurper()
        def configFile = new File("config/ring-person-detection-config.json")
        if (configFile.exists()) {
            config = jsonSlurper.parse(configFile)
            
            // Load night mode devices
            nightModeDevices = config.nightModeDevices
            
            // Load notification only devices
            notificationOnlyDevices = config.notificationOnlyDevices
        }
    }

    def eventHandler(event) {
        def deviceName = event.deviceName
        
        // Check if the device is in the night mode list
        if (nightModeDevices.contains(deviceName)) {
            handleNightModeEvent(event)
        }
        
        // Check if the device is in the notification only list
        if (notificationOnlyDevices.contains(deviceName)) {
            handleNotificationOnlyEvent(event)
        }
    }

    def handleNightModeEvent(event) {
        // Custom logic for handling night mode events
        log.debug("Handling night mode event for device: ${event.deviceName}")
        
        // Send notifications, etc.
    }

    def handleNotificationOnlyEvent(event) {
        // Custom logic for handling notification only events
        log.debug("Handling notification only event for device: ${event.deviceName}")
        
        // Send notifications, etc.
    }
}
