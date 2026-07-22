"""MCP server that pushes Groovy source code to a Hubitat hub — local-file only.

All code-sending tools read Groovy source from local disk files, never from
raw strings, to ensure the agent cannot fabricate or hallucinate code.
"""
import os
import re
from pathlib import Path

from dotenv import load_dotenv
from mcp.server.fastmcp import FastMCP
import requests

# ---------------------------------------------------------------------------
# Load .env from the same directory as this server.py file
# ---------------------------------------------------------------------------
script_dir = Path(__file__).parent
env_path = script_dir / ".env"
load_dotenv(dotenv_path=env_path)

# ---------------------------------------------------------------------------
# Configuration — override via environment variables or edit defaults below
# ---------------------------------------------------------------------------
CONFIG = {
    "HUB_IP": os.environ.get("HUB_IP", "192.168.0.142"),
    "OAUTH_ACCESS_TOKEN": os.environ.get("OAUTH_ACCESS_TOKEN", ""),
    "SANDBOX_CODE_ID": os.environ.get("SANDBOX_CODE_ID", ""),
}

REQUEST_TIMEOUT = 30  # seconds


def _get_current_version(code_id: str, access_token: str) -> int | None:
    """Fetch the current version integer of a Code ID via Hubitat's internal API.

    This is required for optimistic concurrency when posting back to
    /app/ajax/update (Monaco editor locks on version mismatch).

    Returns the version integer or None on failure so callers can fall
    back to the simpler /app/ajax/save endpoint without versioning.
    """
    hub = CONFIG["HUB_IP"].rstrip("/")
    url = f"http://{hub}/app/ajax/code?id={code_id}&access_token={access_token}"
    try:
        resp = requests.get(url, timeout=REQUEST_TIMEOUT)
        data = resp.json()
        if resp.status_code == 200 and isinstance(data, dict):
            version = data.get("version")
            if version is not None:
                return int(version)
    except Exception:
        pass
    return None


mcp = FastMCP("push-groovy")


@mcp.tool()
def push_hubitat_local_file(target_app_id: int, file_path: str) -> str:
    """Push Groovy code to Hubitat by reading it directly from a local file.
    Use this instead of passing raw code strings.
    """
    if not os.path.exists(file_path):
        return f"FAILED: The file {file_path} does not exist on the local disk."
        
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            groovy_source = f.read()
    except Exception as exc:
        return f"FAILED to read local file: {exc}"

    hub_ip = CONFIG['HUB_IP']
    
    # Step 1: Fetch the current version integer
    get_url = f"http://{hub_ip}/app/ajax/code?id={target_app_id}"
    try:
        get_resp = requests.get(get_url, timeout=REQUEST_TIMEOUT)
        get_resp.raise_for_status()
        data = get_resp.json()
        version = data.get('version')
        if version is None:
            return f"FAILED: Could not extract version for app {target_app_id}."
    except Exception as exc:
        return f"FAILED to fetch current app version: {exc}"
        
    # Step 2: Push the update directly
    post_url = f"http://{hub_ip}/app/ajax/update"
    payload = {
        "id": target_app_id,
        "version": version,
        "source": groovy_source
    }
    
    try:
        post_resp = requests.post(post_url, data=payload, timeout=REQUEST_TIMEOUT)
        post_resp.raise_for_status()
        result = post_resp.json()
        
        if result.get('status') == 'success':
            return f"✅ SUCCESS: App {target_app_id} updated to version {result.get('version')} from {file_path}!"
        else:
            return f"❌ COMPILE ERROR:\n{result.get('errorMessage', result)}"
    except Exception as exc:
        return f"FAILED to push code update: {exc}"


@mcp.tool()
def validate_groovy_syntax(file_path: str, sandbox_code_id: int | None = None) -> str:
    """Compile-check a local Groovy file on the Hubitat hub before deployment.

    Reads the file directly from the local disk and posts the source to a
    'Compiler Sandbox' app's code endpoint so Hubitat runs its built-in Groovy
    compiler.  Returns a clear PASS/FAIL with line numbers and messages.

    Args:
        file_path: The exact path to the local Groovy file on disk.
        sandbox_code_id: Optional override for the sandbox app Code ID.  When
            omitted, uses SANDBOX_CODE_ID from the environment / .env file.
            Pass this when the default sandbox is broken or nonexistent.

    Returns:
        A human-readable result showing COMPILER PASS or COMPILER FAIL.
    """
    # --- Guard: file must exist ---
    if not os.path.exists(file_path):
        return f"FAILED: The file {file_path} does not exist on the local disk."

    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            groovy_source = f.read()
    except Exception as exc:
        return f"FAILED to read local file: {exc}"

    # --- Determine sandbox Code ID ---
    code_id = str(sandbox_code_id) if sandbox_code_id is not None else CONFIG.get("SANDBOX_CODE_ID", "")
    if not code_id:
        return (
            "CONFIGURATION ERROR — no sandbox Code ID available.\n"
            "Either set SANDBOX_CODE_ID in .env, or pass sandbox_code_id=NNN "
            "to this tool."
        )

    token = CONFIG.get("OAUTH_ACCESS_TOKEN", "")
    if not token:
        return (
            "CONFIGURATION ERROR — OAUTH_ACCESS_TOKEN is empty.\n"
            "Set it in .env."
        )

    version = _get_current_version(code_id, token)

    # If the sandbox app returns null fields it's broken/nonexistent —
    # surface a clear hint so the caller can try a different ID.
    if version is None:
        return (
            f"SANDBOX UNAVAILABLE — App Code ID {code_id} is broken or "
            f"doesn't exist on the hub (version is null).\n"
            f"Try passing sandbox_code_id=<valid_id> to this tool, "
            f"or update SANDBOX_CODE_ID in .env."
        )

    payload = {
        "id": code_id,
        "version": version,
        "code": groovy_source,
    }
    url = f"http://{CONFIG['HUB_IP'].rstrip('/')}/app/ajax/update"
    headers = {"access_token": token}

    try:
        resp = requests.post(
            url, data=payload, headers=headers, timeout=REQUEST_TIMEOUT
        )
    except requests.ConnectionError:
        return (
            f"CONNECTION FAILED — could not reach Hubitat hub at "
            f"{CONFIG['HUB_IP']}\n"
            f"Check that HUB_IP is correct and the hub is online."
        )
    except requests.Timeout:
        return (
            f"REQUEST TIMEOUT — hub did not respond within {REQUEST_TIMEOUT}s.\n"
            f"URL: {url}"
        )
    except Exception as exc:
        return f"UNEXPECTED ERROR during HTTP POST: {exc}"

    # --- Parse compiler response ---
    try:
        body = resp.json()
    except ValueError:
        body = None

    if resp.status_code != 200 or (body is not None and isinstance(body, dict) and body.get("status") != "success"):
        # General failure — return whatever the hub gave us
        return (
            f"COMPILER FAIL\n"
            f"HTTP {resp.status_code} from hub\n"
            f"Response: {body if body is not None else repr(resp.text[:500])}"
        )

    # Check for compiler errors inside the response
    if isinstance(body, dict) and body.get("errors"):
        errors = body["errors"]
        lines = []
        if isinstance(errors, list):
            for err in errors:
                if isinstance(err, dict):
                    line = err.get("line", "?")
                    msg = err.get("message", str(err))
                    lines.append(f"  Line {line}: {msg}")
                else:
                    lines.append(f"  {err}")
        elif isinstance(errors, str):
            for raw_line in errors.strip().splitlines():
                lines.append(f"  {raw_line.strip()}")

        return (
            f"COMPILER FAIL — {len(lines)} error(s) found:\n"
            + "\n".join(lines)
        )

    # No errors — clean compilation
    return "COMPILER PASS — Groovy source compiled cleanly on the hub. Ready to deploy."

@mcp.tool()
def search_hubitat_apps(name_query: str) -> str:
    """Search the Hubitat hub for an App Code ID by its name using the internal JSON API. 
    Use this if you forgot the target_app_id for a deployment.
    """
    url = f"http://{CONFIG['HUB_IP']}/hub2/userAppTypes"
    
    try:
        resp = requests.get(url, timeout=REQUEST_TIMEOUT)
        resp.raise_for_status()
        data = resp.json()
    except Exception as exc:
        return f"FAILED: Could not fetch App Code JSON from hub: {exc}"

    results = []
    for app in data:
        app_id = app.get('id')
        app_name = app.get('name', '')
        
        if name_query.lower() in app_name.lower():
            results.append(f"ID: {app_id} | Name: {app_name}")
            
    if not results:
        return f"No apps found matching '{name_query}'."
        
    return "✅ Found matching apps:\n" + "\n".join(results)


def main():
    mcp.run()  # stdio transport — default for Hermes subprocess spawn


if __name__ == "__main__":
    main()
