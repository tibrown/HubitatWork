definition(
    name: "MCP Pipeline Test App",
    namespace: "mcp.test",
    author: "Tim",
    description: "A minimal app to verify the local disk to Hubitat AJAX pipeline.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: ""
)

preferences {
    page(name: "mainPage", title: "Test Configuration", install: true, uninstall: true) {
        section("Test Settings") {
            input "logEnable", "bool", title: "Enable debug logging", defaultValue: true
        }
    }
}

def installed() {
    log.info "MCP Pipeline Test App Installed"
    initialize()
}

def updated() {
    log.info "MCP Pipeline Test App Updated"
    initialize()
}

def initialize() {
    if (logEnable) log.debug "Initialization complete."
}
