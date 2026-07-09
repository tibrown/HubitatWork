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
 *  Main config page
 */
def mainPage() {
    dynamicPage(name: "mainPage", title: "AppLoader", install: true, uninstall: true) {

        section("Authentication") {
            paragraph "<b>Access Token:</b>"
            paragraph "<pre style='white-space:pre-wrap;word-break:break-word;margin:0;padding:8px;background:#f6f6f6;border:1px solid #ddd;border-radius:4px;'>${escapeHtml(state.accessToken ?: 'Install the app to generate a token.')}</pre>"
        }

        section("Endpoints") {
            paragraph "<b>Push URL (update existing code):</b>"
            paragraph "<pre style='white-space:pre-wrap;word-break:break-word;margin:0;padding:8px;background:#f6f6f6;border:1px solid #ddd;border-radius:4px;'>${escapeHtml(buildPushUrl())}</pre>"

            paragraph "<b>Create URL (brand new code entry):</b>"
            paragraph "<pre style='white-space:pre-wrap;word-break:break-word;margin:0;padding:8px;background:#f6f6f6;border:1px solid #ddd;border-radius:4px;'>${escapeHtml(buildCreateUrl())}</pre>"
        }

        // Hub Security — optional, only needed if hub requires login for /app/save
        section("Hub Security (optional)") {
            paragraph "Only required if your hub has Hub Security enabled. Leave blank to skip authentication."
            input "hubUsername", "text", title: "Hub Username", required: false
            input "hubPassword", "password", title: "Hub Password", required: false
            paragraph "${authStatusText()}"
        }
    }
}

/** Short badge showing whether credentials are available. */
private String authStatusText() {
    if (isHubAuthConfigured()) {
        return "✅ Credentials configured"
    } else {
        return "ℹ️  No credentials — AppLoader will skip /login (works on hubs without Hub Security)"
    }
}

private boolean isHubAuthConfigured() {
    return hubUsername && hubPassword && hubUsername.trim() != "" && hubPassword.trim() != ""
}

/**
 * Construct the full local endpoint URL that external callers POST to.
 */
String buildPushUrl() {
    if (!state.accessToken || !app?.id) {
        return "URL will appear after you click 'Done' to install the app."
    }
    return "http://127.0.0.1:8080/apps/${app.id}/pushCode?access_token=${state.accessToken}"
}

/** URL for creating a brand-new code entry */
String buildCreateUrl() {
    if (!state.accessToken || !app?.id) {
        return "URL will appear after you click 'Done' to install the app."
    }
    return "http://127.0.0.1:8080/apps/${app.id}/createApp?access_token=${state.accessToken}"
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
    path("/pushCode")  { action: [POST: "handlePushCode"] }
    path("/createApp") { action: [POST: "handleCreateApp"] }
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
 *//**
 * Handle incoming pushCode request.
 */
/**
 * Handle incoming pushCode request.
 */
def handlePushCode() {
    if (!state.accessToken) {
        renderJson 503, [status: "error", message: "App not installed."]
        return
    }

    Map body = request?.JSON as Map
    if (!body?.appId || (!body?.source && body?.source != "")) {
        renderJson 400, [status: "error", message: "Missing appId or source."]
        return
    }

    String appId  = "${body.appId}"
    String source = "${body.source}"
    log.info "handlePushCode — processing save for app '${appId}' (${source.length()} chars)"

    // 1. Fetch the current version of the code (Required by Monaco)
    def currentVersion = null
    try {
        httpGet([
            uri: "http://127.0.0.1:8080",
            path: "/app/ajax/code",
            query: [id: appId]
        ]) { resp ->
            if (resp.status == 200 && resp.data) {
                // Handle both auto-parsed Maps and raw JSON strings
                if (resp.data instanceof Map) {
                    currentVersion = resp.data.version
                } else {
                    def parsed = new groovy.json.JsonSlurper().parseText(resp.data.toString())
                    currentVersion = parsed.version
                }
            }
        }
    } catch (Exception e) {
        log.warn "handlePushCode — Failed to fetch code version: ${e.message}"
        renderJson 500, [status: "error", message: "Failed to fetch current code version. Check if App ID ${appId} exists."]
        return
    }

    if (currentVersion == null) {
        log.warn "handlePushCode — Version returned null"
        renderJson 500, [status: "error", message: "Retrieved empty version block from hub."]
        return
    }

    // 2. Push the update using the required version token
    try {
        def httpResult = null
        httpPost([
            uri:      "http://127.0.0.1:8080",
            path:     "/app/ajax/update",
            headers: [
                "Content-Type":     "application/x-www-form-urlencoded",
                "X-Requested-With": "XMLHttpRequest",
                "Accept":           "*/*"
            ],
            body:   [
                id:      appId,
                version: currentVersion,
                source:  source
            ]
        ]) { resp ->
            httpResult = resp
        }

        if (httpResult?.status == 200) {
            renderJson 200, [status: "success", appId: appId, message: "Code saved successfully via /app/ajax/update"]
        } else {
            log.warn "handlePushCode — update returned ${httpResult?.status}: ${httpResult?.data}"
            renderJson 502, [status: "error", message: "Update rejected by hub.", hub_status: httpResult?.status]
        }

    } catch (Exception ex) {
        log.error "handlePushCode — exception during update call: ${ex.message}"
        renderJson 500, [status: "error", message: "Internal error calling update endpoint: ${ex.message}"]
    }
}

/**
 *  Attempt to authenticate against Hubitat's /login endpoint.
 *  Returns the session cookie string on success, or null if credentials
 *  were not provided (graceful — caller skips auth in that case).
 */
private String authenticateHub() {
    if (!isHubAuthConfigured()) {
        log.info "authenticateHub — no hub credentials configured, skipping /login"
        return null
    }

    try {
        def loginResult = false
        def cookie = null
        httpPost([
            uri:      "http://127.0.0.1:8080",
            path:     "/login",
            body: [
                username: hubUsername,
                password: hubPassword,
                submit:   "Login"
            ],
            requestContentType: "application/x-www-form-urlencoded",
            textParser: true,
            ignoreSSLIssues:  true
        ]) { resp ->
            // Bad credentials → login page contains an error banner
            if (resp.data?.text?.contains("The login information you supplied was incorrect.")) {
                log.warn "authenticateHub — bad credentials"
                return
            }
            def setCookie = resp.headers?.'Set-Cookie'
            if (setCookie) {
                cookie = setCookie.split(';')[0]
                loginResult = true
            } else if (setCookie instanceof List) {
                // Some Hubitat versions may return Set-Cookie as a list of headers
                cookie = setCookie[0].split(';')[0]
                loginResult = true
            }
        }

        if (!loginResult) {
            log.warn "authenticateHub — could not capture session cookie"
            return null
        }
        log.info "authenticateHub — authenticated successfully, cookie acquired"
        return cookie
    } catch (Exception ex) {
        log.error "authenticateHub — login exception: ${ex.message}"
        return null
    }
}

/**
 *  Handle incoming createApp request.
 *
 *  Expected JSON body:
 *      { "source": "<raw Groovy source string>" }
 *
 *  Creates a brand-new code entry on Hubitat by POSTing to /app/save with
 *  empty id/version (mimicking HPM installApp). Returns the new Code ID via
 *  the 302 redirect Location header, with fallback extraction if Hubitat
 *  auto-follows the redirect.
 */
def handleCreateApp() {
    Map body = request?.JSON as Map
    String source = "${body.source}"

    // Authenticate — gracefully skips when credentials are blank
    String cookie = authenticateHub()
    if (cookie == null && isHubAuthConfigured()) {
        // Credentials ARE configured but login failed → this IS an error
        renderJson 503, [status: "error", message: "Hub authentication failed. Check hubUsername/hubPassword preferences."]
        return
    }

    // Build the request — with or without Cookie header
    def headerMap = [
        "Content-Type":     "application/x-www-form-urlencoded",
        "X-Requested-With": "XMLHttpRequest",
        "Accept":           "*/*"
    ]
    if (cookie != null) {
        headerMap << ["Cookie": cookie]
    }

    try {
        def resp = null
        httpPost([
            uri:              "http://127.0.0.1:8080",
            path:             "/app/save",
            headers:          headerMap,
            body: [
                id:      "",
                version: "",
                create:  "",
                source:  source
            ],
            requestContentType: "application/x-www-form-urlencoded",
            followRedirects:    false,
            timeout:           300,
            ignoreSSLIssues:   true
        ]) { r -> resp = r }

        if (resp == null) {
            renderJson 502, [status: "error", message: "No response from /app/save"]
            return
        }

        // ---------- Primary path: 301/302 redirect with Location header ----------
        String codeId = null
        if (resp.status == 301 || resp.status == 302) {
            String location = resp.headers."Location" ?: ""
            codeId = extractCodeIdFromLocation(location)
        }

        // ---------- Fallback path: Hubitat followed the redirect anyway (status 200) ----------
        if (codeId == null && resp.status == 200) {
            log.warn "handleCreateApp — hub followed redirect (status ${resp.status}), attempting fallback extraction"

            // Attempt 1: Request_URI header tells us where we landed
            def requestUri = resp.headers."Request_URI"
            if (requestUri) {
                codeId = extractCodeIdFromLocation(requestUri)
            }

            // Attempt 2: Parse HTML body for hidden codeId input
            if (codeId == null && resp.data?.text) {
                def html = resp.data.text
                def m1 = html =~ /<input[^>]+id=["']app\.codeId["'][^>]+value=["'](\d+)["']/
                if (!m1) m1 = html =~ /name=["']id["'][^>]*value=["'](\d+)["']/

                if (m1) {
                    codeId = m1[0][1]
                    log.info "handleCreateApp — extracted Code ID ${codeId} from HTML body"
                } else {
                    // Attempt 3: Look for /app/editor/NNN anywhere in the response
                    def m2 = html =~ /\/app\/editor\/(\d+)/
                    if (m2) {
                        codeId = m2[0][1]
                        log.info "handleCreateApp — extracted Code ID ${codeId} from HTML body pattern"
                    }
                }
            }
        }

        if (codeId == null) {
            // Total failure — dump diagnostic info for debugging
            def diag = [status: "error", message: "Failed to extract Code ID from hub response"]
            return renderJson(502, diag)
        }

        // Success — verify it's actually numeric, just in case regex captured garbage
        if (!codeId.matches("\\d+")) {
            renderJson 500, [status: "error", message: "Extracted Code ID is not numeric: ${codeId}"]
            return
        }

        log.info "handleCreateApp — new Code ID ${codeId} created successfully"
        renderJson 200, [
            status:   "success",
            codeId:   codeId.toInteger(),
            message:  "New code entry created on hub with Code ID ${codeId}"
        ]

    } catch (Exception ex) {
        log.error "handleCreateApp — exception: ${ex.message}"
        renderJson 500, [status: "error", message: "Internal error during create: ${ex.message}"]
    }
}

/**
 *  Strip URL prefix from a Location/URI string to isolate the bare Code ID.
 *  Example input:  https://127.0.0.1:8443/app/editor/5678
 *  Returns:        "5678"
 */
private String extractCodeIdFromLocation(String location) {
    if (!location) return null
    try {
        String id = location.replaceAll("https?://.*?/?app/editor/", "")
        // Strip any trailing garbage (query params, fragments, slashes)
        id = id.takeWhile { Character.isDigit(it as char) || it == '-' }
        // Return only the digits
        def m = id =~ /(\d+)/
        return m ? m[0][1] : null
    } catch (Exception ex) {
        log.warn "extractCodeIdFromLocation — regex error: ${ex.message}"
        return null
    }
}

/**
 *  Convenience helper — serialises a Map to JSON and returns the render object.
 */
private def renderJson(int statusCode, Map data) {
    def json = new groovy.json.JsonOutput().toJson(data)
    return render(contentType: "application/json", data: json, status: statusCode)
}
