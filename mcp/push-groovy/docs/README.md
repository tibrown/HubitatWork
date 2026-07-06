# `push_hubitat_code` — Push Groovy Source to Hubitat

A FastMCP stdio-server that lets your local AI agent push compiled Groovy source
directly to a Hubitat hub via the [AppLoader](../Apps/AppLoader/) receiver app.

---

## Quick start

1. Install the AppLoader SmartApp on your Hubitat hub and note its **installed ID**
   and **access token** (shown on the AppLoader config page).

2. Set the three required environment variables (or edit defaults inside `server.py`):

```bash
export HUB_IP=192.168.0.142
export APPLOADER_APP_ID=<installed_id_of_your_AppLoader_instance>
export OAUTH_ACCESS_TOKEN=<access_token_from_AppLoader_config_page>
```

3. Register with Hermes:

```bash
hermes config set mcp_servers.push-groovy.command "python3"
hermes config set "mcp_servers.push-groovy.args" '[\"/home/tim/gitrepos/HubitatWork/mcp/push-groovy/server.py\"]'
hermes mcp test push-groovy
```

The tool appears to your agent as **`mcp_push-groovy_push_hubitat_code`**.

---

## Tool reference

### `push_hubitat_code(target_app_id: int, groovy_source: str) → str`

Pushes raw Groovy source code to a running Developer App on the Hubitat hub.

| Parameter | Type   | Description                                                    |
|-----------|--------|----------------------------------------------------------------|
| `target_app_id`  | `int`  | Installed-app ID of the Developer App to update (not the code-library ID — the **installed** app row in *Device & Apps → My Apps*). |
| `groovy_source` | `str`  | Complete Groovy source text. Replace the entire file; there is no patch mode. |

### Return value

A plain-text string summarizing the result:

- **On success** — `SUCCESS — HTTP 200 … App ID <N>: source code saved to hub.`
- **On hub error (e.g., compile failure)** — `HTTP <code> from hub at …` plus the response body, which includes Hubitat's error detail such as a compilation message.
- **On network error** — `CONNECTION FAILED …`, `REQUEST TIMEOUT …`, or `UNEXPECTED ERROR …`

---

## How it works internally

```
Your agent (local)        MCP server          Hubitat AppLoader          Hubitat saveText endpoint
┌──────────┐      @tool()      ┌───────┐   JSON POST   ┌────────────────│──────────────────┐
│ push_    │ ───────────────▶  │       │ ─────────────▶ │ POST /apps/api/│                   │
│ code…    │ (groovy string)   │ server│               │ {loader_id}/  │ POST /app/code/  │
│          │                  │ .py   │               │ pushCode       │ saveText         │
└──────────┘                  └───────┘                └────────────────│──────────────────┘
                                                   access_token=<oauth>
```

1. The MCP tool receives `target_app_id` and the Groovy source string from the agent.
2. It POSTs JSON `{"appId": <id>, "source": "<code>"}` to the **AppLoader** receiver on the hub at:

   ```
   http://{HUB_IP}/apps/api/{APPLOADER_APP_ID}/pushCode?access_token={OAUTH_ACCESS_TOKEN}
   ```

3. The AppLoader forwards it (loopback) to Hubitat's internal `/app/code/saveText` endpoint,
   which compiles and activates the new code on the target app.
4. The response bubbles back through the MCP server as a summary string for the agent.

---

## Configuration refresher

Three values can be set either as **environment variables** or by editing the
`CONFIG` dict directly at the top of `server.py`:

| Variable               | Example        | Source                                    |
|------------------------|----------------|-------------------------------------------|
| `HUB_IP`               | `192.168.0.142`| Your Hubitat hub's LAN IP (or hostname)   |
| `APPLOADER_APP_ID`     | `42`          | Installed app row ID from Hubitat UI      |
| `OAUTH_ACCESS_TOKEN`   | `a1b2c3…`    | Shown on AppLoader config page after install |

Defaults are `192.168.0.142`, empty string, and empty string respectively — so
you *must* set at least the last two before the tool will make any requests.

---

## Troubleshooting

| Symptom                              | Likely cause                                       | Fix                                                                                                    |
|--------------------------------------|----------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| `CONFIGURATION ERROR`                | Env vars or CONFIG defaults are still empty         | Set `APPLOADER_APP_ID` and `OAUTH_ACCESS_TOKEN`                                                        |
| `CONNECTION FAILED`                  | Hub offline or wrong IP                             | Ping the hub; verify `HUB_IP`                                                                          |
| `REQUEST TIMEOUT (>30s)`            | Hub overloaded; large Groovy source taking long     | Retry; check hub logs for compile timeouts                                                              |
| HTTP 403 / unauthorized              | Wrong access token                                  | Copy the token again from AppLoader config page                                                         |
| HTTP 404                           | `APPLOADER_APP_ID` is wrong                         | Double-check the installed app ID (not its code-library number)                                         |
| Compilation error in response body   | Groovy syntax/runtime issue in the source           | Read Hubitat's compile message from the response body and fix the Groovy before resubmitting             |

---

## Notes

- **This replaces entire source** — there is no incremental patch mode. Send the full file contents each time.
- The tool uses a **30-second request timeout**. For very large SmartApps (200K+ chars) the hub's compile step can approach this limit; if you hit timeouts increase `REQUEST_TIMEOUT` in `server.py`.
- Because the AppLoader forwards via loopback (`127.0.0.1:8080`) on the hub itself, network firewalls between your machine and the hub do *not* affect the internal save — only the initial POST to the AppLoader matters.
