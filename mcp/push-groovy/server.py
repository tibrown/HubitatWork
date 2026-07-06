"""MCP server that pushes Groovy source code to a Hubitat hub.

Tool: push_hubitat_code  -- sends compiled Groovy source to the AppLoader
receiver endpoint on the hub and returns compilation status.
"""
import os

import requests
from mcp.server.fastmcp import FastMCP

# ---------------------------------------------------------------------------
# Configuration — override via environment variables or edit defaults below
# ---------------------------------------------------------------------------
CONFIG = {
    "HUB_IP": os.environ.get("HUB_IP", "192.168.0.142"),
    "APPLOADER_APP_ID": os.environ.get("APPLOADER_APP_ID", ""),
    "OAUTH_ACCESS_TOKEN": os.environ.get("OAUTH_ACCESS_TOKEN", ""),
}

REQUEST_TIMEOUT = 30  # seconds


def _build_push_url() -> str:
    """Construct the full AppLoader pushCode endpoint URL."""
    hub = CONFIG["HUB_IP"].rstrip("/")
    app_id = CONFIG["APPLOADER_APP_ID"]
    token = CONFIG["OAUTH_ACCESS_TOKEN"]
    return f"http://{hub}/apps/api/{app_id}/pushCode?access_token={token}"


mcp = FastMCP("push-groovy")


@mcp.tool()
def push_hubitat_code(target_app_id: int, groovy_source: str) -> str:
    """Push compiled Groovy source code to a Hubitat hub via the AppLoader REST endpoint.

    This tool sends Groovy source to an already-installed AppLoader receiver app
    on the hub, which forwards it to Hubitat's internal save endpoint for the
    target app code ID. Use this to deploy or update Developer Apps from your
    local machine without opening the Hubitat UI.

    Args:
        target_app_id: The Hubitat installed-app ID of the Developer App whose
                       source code you want to replace/update (integer).
        groovy_source: The complete Groovy source code string to push to the hub.

    Returns:
        A plain-text summary containing the HTTP status code and response body
        from the hub. On success you will see HTTP 200 with a 'success' status
        message. On failure the response includes the error detail so you can
        diagnose compilation errors or connection problems.
    """
    # --- Guard: config must be set ---
    missing = [k for k in ("APPLOADER_APP_ID", "OAUTH_ACCESS_TOKEN") if not CONFIG[k]]
    if missing:
        return (
            f"CONFIGURATION ERROR — the following are required but empty: "
            f"{', '.join(missing)}\n"
            f"Set them as environment variables or edit CONFIG in server.py:\n"
            f"  APPLOADER_APP_ID  = installed ID of your AppLoader receiver app\n"
            f"  OAUTH_ACCESS_TOKEN = access token shown on that app's config page"
        )

    url = _build_push_url()
    payload = {"appId": target_app_id, "source": groovy_source}

    try:
        resp = requests.post(url, json=payload, timeout=REQUEST_TIMEOUT)
    except requests.ConnectionError:
        return (
            f"CONNECTION FAILED — could not reach Hubitat hub at {url}\n"
            f"Check that HUB_IP ({CONFIG['HUB_IP']}) is correct and the hub is online."
        )
    except requests.Timeout:
        return (
            f"REQUEST TIMEOUT — hub did not respond within {REQUEST_TIMEOUT}s.\n"
            f"URL: {url}"
        )
    except Exception as exc:
        return f"UNEXPECTED ERROR during HTTP POST: {exc}"

    # --- Parse response body for a readable summary ---
    body = None
    try:
        body = resp.json()
        body_str = str(body)
    except ValueError:
        body_str = repr(resp.text[:500])

    # Most AppLoader endpoints return {"status": "success|error", ...}
    if resp.status_code == 200 and (body is not None and isinstance(body, dict) and body.get("status") == "success"):
        return (
            f"SUCCESS — HTTP {resp.status_code}\n"
            f"App ID {target_app_id}: source code saved to hub.\n"
            f"Response: {body_str}"
        )

    # General result summary for any non-200-success case
    return (
        f"HTTP {resp.status_code} from hub at {url}\n"
        f"Response body: {body_str}"
    )


def main():
    mcp.run()  # stdio transport — default for Hermes subprocess spawn


if __name__ == "__main__":
    main()
