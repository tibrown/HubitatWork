/**
 *  AppLoader v1.0.0
 *
 *  REST API bridge that forwards a POST to /pushCode into a loopback call
 *  to Hubitat's internal developer code-save endpoint (/app/code/saveText),
 *  enabling programmatic updates of Developer App Code from external tools
 *  (CI pipelines, local scripts, IDEs, etc.).
 *
 *  Copyright 2026 Tim Brown
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 */

definition(
    name: "AppLoader",
    namespace: "timbrown",
    author: "Tim Brown",
    description: "Local REST API bridge for programmatically pushing Developer App Code to the hub.",
    category: "Tools",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    oauth: true,           // Hubitat auto-generates state.accessToken on install
    singleInstance: false
)

preferences {
    page(name: "mainPage")
}

/**
 *  Main config page — displays access token and push URL for easy copying.
 *  No install/uninstall logic needed; the app is ready as soon as it's saved.
 */
def mainPage() {
    dynamicPage(name: "mainPage", title: "AppLoader", install: true, uninstall: true) {

        // Access token — generated automatically when oauth:true + app is installed
        section("Authentication") {
            paragraph "<b>Access Token:</b>"
            paragraph "<pre style='white-space:pre-wrap;word-break:break-word;margin:0;padding:8px;background:#f6f6f6;border:1px solid #ddd;border-radius:4px;'>${escapeHtml(state.accessToken ?: 'Install the app to generate a token.')}</pre>"
        }

        // Full push URL ready to copy-paste
        section("Endpoint") {
            def url = buildPushUrl()
            paragraph "<b>Push URL:</b>"
            paragraph "<pre style='white-space:pre-wrap;word-break:break-word;margin:0;padding:8px;background:#f6f6f6;border:1px solid #ddd;border-radius:4px;'>${escapeHtml(url)}</pre>"
        }
    }
}

/**
 *  Construct the full local endpoint URL that external callers POST to.
 */
String buildPushUrl() {
    def token = state.accessToken
    if (!token) {
        return "Token not yet generated — please save/install this app first."
    }
    "http://127.0.0.1:8080/apps/${app.id}/pushCode?access_token=${token}"
}

/**
 *  Make sure OAuth has produced a token for this app instance.
 */
private void ensureAccessToken() {
    if (!state.accessToken) {
        try {
            createAccessToken()
        } catch (Exception ex) {
            log.warn "AppLoader could not create an OAuth access token: ${ex.message}"
        }
    }
}

/**
 *  Basic HTML escaping for text rendered inside paragraph markup.
 */
private String escapeHtml(String value) {
    return (value ?: "")
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")
}

def installed() {
    ensureAccessToken()
}

def updated() {
    ensureAccessToken()
}

/**
 *  REST endpoint mappings.
 *  POST /pushCode  →  handlePushCode()
 */
mappings {
    path("/pushCode") { action: [POST: "handlePushCode"] }
}

/**
 *  Handle incoming pushCode request.
 *
 *  Expected JSON body:
 *      { "appId": "<Hubitat app code ID>", "source": "<raw Groovy source string>" }
 *
 *  The handler forwards this to Hubitat's internal /app/code/saveText endpoint via
 *  a loopback HTTP POST with url-encoded form data (matching the developer UI's own
 *  save mechanism).
 */
def handlePushCode() {
    // --- Guard: oauth token must exist (not yet installed) ---
    if (!state.accessToken) {
        log.warn "handlePushCode rejected — app not installed. Save/install it first."
        renderJson 503, [status: "error", message: "App not installed. Please save/install AppLoader before using the push endpoint."]
        return
    }

    // --- Parse and validate JSON body ---
    Map body = request?.JSON as Map

    if (!body?.appId) {
        renderJson 400, [status: "error", message: "Missing required field 'appId'."]
        return
    }

    if (!body?.source && body?.source != "") {
        // source can be empty string (legitimate), but null/missing is wrong
        renderJson 400, [status: "error", message: "Missing required field 'source'."]
        return
    }

    String appId  = "${body.appId}"
    String source = "${body.source}"

    log.info "handlePushCode — saving app '${appId}' (${source.length()} chars)"

    // --- Loopback POST to Hubitat's internal developer save endpoint ---
    // This lives on the hub itself, reachable at 127.0.0.1:8080
    // Form fields: id = target app code ID, txt = raw source code
    try {
        def httpResult = null

        httpPost([
            uri:      "http://127.0.0.1:8080/app/code/saveText",
            headers: [
                "Content-Type":   "application/x-www-form-urlencoded",
                "Accept":         "*/*"
            ],
            body:   [
                id:  appId,
                txt: source
            ]
        ]) { resp ->
            httpResult = resp
        }

        // --- Respond based on what came back from the save endpoint ---
        if (httpResult?.status == 200) {
            renderJson 200, [
                status: "success",
                appId:  appId,
                message: "Code saved successfully"
            ]
        } else {
            log.warn "handlePushCode — saveText returned ${httpResult?.status}: ${httpResult?.data}"
            renderJson httpResult?.status ?: 502, [
                status:  "error",
                code:    httpResult?.status,
                message: "saveText rejected the request",
                detail:  "${httpResult?.data}"
            ]
        }

    } catch (Exception ex) {
        log.error "handlePushCode — exception during saveText call: ${ex.message}"
        renderJson 500, [
            status:  "error",
            message: "Internal error calling saveText endpoint",
            detail:  "${ex.message}"
        ]
    }
}

/**
 *  Convenience helper — serialises a Map to JSON and renders it.
 */
private void renderJson(int statusCode, Map data) {
    def json = new groovy.json.JsonOutput().toJson(data)
    render contentType: "application/json", data: json, status: statusCode
}
