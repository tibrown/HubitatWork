/**
 *  Mocreo MQTT Bridge (Manager)
 *
 *  A single Hubitat app that bridges up to two MOCREO H5-Pro sensor telemetry
 *  topics into per-sensor Hub Variables.
 *
 *  Data flow:
 *     MOCREO sensor -> H5-Pro gateway -> local MQTT broker (see brokerIP pref)
 *     -> Simple MQTT Client driver (parent) -> Simple MQTT Topic child devices
 *     -> this app subscribes to each child's `variable` attribute
 *     -> parses the JSON payload via JsonSlurper
 *     -> writes each value into its Hub Variable via setGlobalVar()
 *
 *  Live payload shape (captured from the broker, LS1 temp-only sensors):
 *     topic:  mocreo/<GATEWAY>/node/<NODE-EUI>/data
 *     payload: [{"measureId": 295, "temperature": -1800, "model": "LS1",
 *                "timestamp": 1788345501}]
 *  The root is a JSON ARRAY. `temperature` is scaled (divide by `tempDivisor`,
 *  default 100 -> -18.0 degC). Humidity/battery are NOT reported by this model.
 *
 *  Requires:
 *    - sethkinast/hubitat-simple-mqtt driver (Simple MQTT Client + child
 *      Simple MQTT Topic). The child stores every non-on/off/true/false payload
 *      as its `variable` attribute (always a string) and fires `button`
 *      (pushed) on EVERY message, even repeated ones.
 *    - The destination Hub Variables created first in Settings > Hub Variables
 *      (an app can set them but cannot create them).
 *
 *  Copyright 2026 Tim Brown
 */

definition(
    name: "Mocreo MQTT Bridge",
    namespace: "timbrown",
    author: "Tim Brown",
    description: "Bridge up to two MOCREO H5-Pro MQTT sensor telemetry topics into Hub Variables.",
    category: "Convenience",
    singleThreaded: true
)

preferences {
    page(name: "mainPage")
}

def mainPage() {
    dynamicPage(name: "mainPage", title: "Mocreo MQTT Bridge", install: true, uninstall: true) {
        section("<b>MQTT sensor sources</b>") {
            input "topicDevice1", "capability.pushableButton",
                title: "Sensor 1 - Simple MQTT Topic child device", required: true
            input "topicDevice2", "capability.pushableButton",
                title: "Sensor 2 - Simple MQTT Topic child device", required: true
            input "variableAttribute", "string",
                title: "Payload attribute (usually 'variable')",
                defaultValue: "variable", required: true
        }
        section("<b>Temperature parsing</b>") {
            input "tempKey", "string",
                title: "Temperature JSON key", defaultValue: "temperature", required: true
            input "tempDivisor", "decimal",
                title: "Divide raw temperature by this (MOCREO LS1 uses 100)", defaultValue: 100, required: true
        }
        section("<b>Destination Hub Variables</b> (pick the two you created in Settings > Hub Variables; must be Decimal/BigDecimal type)") {
            input "tempVar1", "enum",
                title: "Sensor 1 -> Hub Variable", options: decimalVars(), required: true
            input "tempVar2", "enum",
                title: "Sensor 2 -> Hub Variable", options: decimalVars(), required: true
        }
    }
}

// List existing Decimal/BigDecimal hub variables so the user can pick from them.
// (Label == value == the variable's name, which setGlobalVar() expects.)
private String[] decimalVars() {
    def names = getGlobalVarsByType("bigdecimal")?.keySet() ?: []
    names.sort()
    return names.toArray(new String[0])
}

def installed()  { initialize() }
def updated()    { initialize() }

def initialize() {
    unsubscribe()
    // PushableButton: the client fires `pushed` on EVERY inbound message (even
    // repeats) and sets `variable` to the raw payload. Subscribe to BOTH so a
    // re-reported (unchanged) temperature still bridges — defeating Hubitat's
    // same-value event dedup.
    subscribe(topicDevice1, "pushed", payloadTrigger1)
    subscribe(topicDevice1, "variable", payloadTrigger1)
    subscribe(topicDevice2, "pushed", payloadTrigger2)
    subscribe(topicDevice2, "variable", payloadTrigger2)

    // Register interest (shows "in use by" and warns before deletion while in use).
    [tempVar1, tempVar2].each { v -> addInUseGlobalVar(v) }

    log.info "MocreoMqttBridge initialized: ${topicDevice1.displayName}->${tempVar1}, ${topicDevice2.displayName}->${tempVar2} ($variableAttribute, /$tempDivisor, deg F)"
}

def payloadTrigger1(evt) { handlePayload(topicDevice1, tempVar1) }
def payloadTrigger2(evt) { handlePayload(topicDevice2, tempVar2) }

private void handlePayload(device, varName) {
    def raw = device.currentValue(variableAttribute)
    if (raw == null || raw.toString().trim() == "") {
        log.trace "MocreoMqttBridge: empty payload on ${device.displayName}; skipping"
        return
    }
    def text = raw.toString()
    log.trace "MocreoMqttBridge ${device.displayName}: payload=$text"

    def parsed
    try {
        parsed = new groovy.json.JsonSlurper().parseText(text)
    } catch (e) {
        log.warn "MocreoMqttBridge ${device.displayName}: payload not valid JSON: ${text} (${e.message})"
        return
    }

    // MOCREO wraps readings in a JSON array; take the first object element.
    def obj = parsed
    if (parsed instanceof List) {
        obj = parsed.find { it instanceof Map }
    }
    if (!(obj instanceof Map)) {
        log.warn "MocreoMqttBridge ${device.displayName}: payload is not a JSON object/array: ${text}"
        return
    }

    setVar(varName, scaledTemp(obj[tempKey]))
}

private Object scaledTemp(rawTemp) {
    if (rawTemp == null || rawTemp.toString().trim() == "") { return null }
    // Always store in Fahrenheit. Use BigDecimal throughout so .setScale() always
    // exists (never convert to Double — Double has no setScale()).
    // Raw is scaled by tempDivisor (MOCREO LS1 uses 100) → °C, then C → F.
    BigDecimal raw   = new BigDecimal(rawTemp.toString())
    BigDecimal div   = new BigDecimal(tempDivisor.toString())
    BigDecimal c     = raw.divide(div, 2, BigDecimal.ROUND_HALF_UP)
    BigDecimal value = c.multiply(9).divide(5, 2, BigDecimal.ROUND_HALF_UP).add(BigDecimal.valueOf(32))
    return value.setScale(1, BigDecimal.ROUND_HALF_UP).toPlainString()
}

private void setVar(String varName, rawValue) {
    if (varName == null || varName.trim() == "") { return }
    if (rawValue == null) {
        log.trace "MocreoMqttBridge: no value -> ${varName}; skipping"
        return
    }
    def val = rawValue.toString().trim()
    if (val == "") { return }
    def ok = false
    try {
        ok = setGlobalVar(varName, val)
    } catch (e) {
        log.warn "MocreoMqttBridge: setGlobalVar(${varName}) failed: ${e.message}"
        return
    }
    log.info "MocreoMqttBridge: ${varName} = ${val} (ok=${ok})"
}