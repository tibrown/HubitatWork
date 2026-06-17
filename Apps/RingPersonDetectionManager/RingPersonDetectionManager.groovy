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

    def eventHandler(evt) {
        log.debug "eventHandler: $evt"

        if (isNightMode()) {
            def nightModeDevices = getNightModeDevices()
            if (nightModeDevices.contains(deviceToCheck)) {
                takeAction(nightModeDevices, evt)
            }
        }

        if (isNotificationOnlyMode()) {
            def notificationOnlyDevices = getNotificationOnlyDevices() 
            if (notificationOnlyDevices.contains(deviceToCheck)) {
                sendNotification(notificationOnlyDevices, evt)
            }
        }

        def allDevices = getAllDevices()
        if (allDevices.contains(deviceToCheck)) {
            takeAction(allDevices, evt)
        } else {
            log.warn "Device ${deviceToCheck} not found in any lists"
        }
    }
}
