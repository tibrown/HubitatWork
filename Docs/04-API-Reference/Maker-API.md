# Maker API

Maker API is a built-in Hubitat Elevation app that provides a simple HTTP API to retrieve device status and send commands. It is useful for integrating Hubitat into external systems or applications that don't offer direct Hubitat integration.

## Setup

1. From the hub sidebar, select **Apps** → **Add Built-In App**
2. Choose **Maker API** from the list
3. Optionally enable **debug logging** (visible in **Logs**)
4. Select the **devices and hub variables** to authorise for this instance
5. Select **Update** — you will see the generated endpoint URLs
6. Select **Done**

> **Security Note**: Your access token is like a username and password — anyone with it can access these endpoints. Reset it using **Create New Access Token** in the app, or by creating a new Maker API instance and removing the old one.

## URL Format

```
http://[hub_ip]/apps/api/[app_id]/[endpoint]?access_token=[access_token]
```

A Maker API instance can be configured for **LAN-only**, **cloud-only**, or **both** access.

---

## Endpoints

### Devices

| Endpoint | Description |
|---|---|
| `GET /devices` | List all authorised devices (id, name, label) |
| `GET /devices/all` | Full details for all authorised devices (capabilities, attributes, commands) |
| `GET /devices/[id]` | Full details for a specific device |
| `GET /devices/[id]/events` | Recent events for a specific device |
| `GET /devices/[id]/commands` | List of commands for a specific device |
| `GET /devices/[id]/[command]` | Send a command to a device |
| `GET /devices/[id]/[command]/[value]` | Send a command with a parameter (multiple params comma-separated) |
| `GET /devices/[id]/[attribute]` | Get the current value of a specific attribute |
| `GET /devices/[id]/setLabel?label=[name]` | Set the device label (display name) |
| `GET /devices/[id]/setDriver?namespace=[ns]&name=[name]` | Change the device driver |

#### Command Examples

```
# Turn on device 1
/devices/1/on

# Set dimmer level to 50%
/devices/1/setLevel/50

# Set lock code at position 3 with code 4321 and name "Guest"
/devices/1321/setCode/3,4321,Guest

# Get switch attribute
/devices/123/switch
# Returns: {"id":"123","attribute":"switch","value":"off"}
```

#### `GET /devices` — Example Response

```json
[
    { "id": "1", "name": "My First Device", "label": "Living Room Light" },
    { "id": "2", "name": "My Second Device", "label": "Living Room Switch" }
]
```

#### `GET /devices/all` — Example Response

```json
[
    {
        "name": "My First Device",
        "label": "Living Room Light",
        "type": "Virtual Switch",
        "id": "1",
        "date": "2018-10-16T00:08:18+0000",
        "model": null,
        "manufacturer": null,
        "capabilities": ["Switch", "Refresh"],
        "attributes": { "switch": "off" },
        "commands": [
            { "command": "off" },
            { "command": "on" },
            { "command": "refresh" }
        ]
    }
]
```

> **Note**: Not all commands listed in device details are supported via the API.

---

### Rooms

| Endpoint | Description |
|---|---|
| `GET /rooms` | List all rooms |
| `GET /room/select/[id]` | Details for a specific room |
| `GET /room/insert?name=[name]&deviceIds=[ids]` | Create a new room with devices |
| `GET /room/update/[id]?name=[name]&deviceIds=[ids]` | Update an existing room |
| `GET /room/delete/[id]` | Delete a room |

---

### Hub Variables

Requires variables to be authorised under **Allow endpoint to control these hub variables**.

| Endpoint | Description |
|---|---|
| `GET /hubvariables` | List all authorised hub variables |
| `GET /hubvariables/[name]` | Get a specific variable's value and type |
| `GET /hubvariables/[name]/[value]` | Set a variable's value |

#### Example

```
GET /hubvariables/myStringVariable
Returns: {"name":"myStringVariable","value":"Example value","type":"string"}

GET /hubvariables/myStringVariable/testing123
Returns: {"name":"myStringVariable","value":"testing123","type":"string"}
```

> **Note**: Variable names and values are case-sensitive. URL-encode spaces and special characters as needed.

---

### Modes

Requires **Allow control of modes** to be enabled.

| Endpoint | Description |
|---|---|
| `GET /modes` | Get current hub mode |
| `GET /modes/[id]` | Set hub mode by numeric ID |

---

### HSM (Hubitat Safety Monitor)

Requires **Allow control of HSM** to be enabled.

| Endpoint | Description |
|---|---|
| `GET /hsm` | Get current HSM status |
| `GET /hsm/[value]` | Set HSM status (e.g., `armAway`, `armHome`, `disarm`) |

See [HSM events reference](https://docs2.hubitat.com/en/developer/interfaces/hubitat-safety-monitor-interface) for valid `hsmStatus` and `hsmSetArm` values.

---

### Event Posting (HTTP POST / Webhooks)

Instead of polling, you can register a URL to receive device events via HTTP POST in real time.

**Set the POST URL:**
```
/postURL/[URL]          # Set (URL-encoded)
/postURL                # Clear (stops event stream)
```

**POST body format:**
```json
{
    "content": {
        "name":            "switch",
        "value":           "on",
        "displayName":     "Living Room Switch",
        "deviceId":        "2",
        "descriptionText": "Living Room Switch was turned on",
        "unit":            null,
        "data":            null
    }
}
```

You can also enable **location events** (mode changes, HSM status, hub variable changes) to be included in the event stream — these are sent alongside device events.

---

## Command Extensions

### `setColor()` — Extended Support

The standard `setColor()` command accepts HSB data:

```
/devices/[id]/setColor/{"hue":1,"saturation":100,"level":50}
```

Maker API also accepts an RGB hex shorthand — it converts to HSB automatically:

```
/devices/[id]/setColor/{"hex":"FF0400"}
```

> **Note**: JSON parameters may need to be URL-encoded depending on your client. E.g., `{"hex":"FF0400"}` becomes `%7B%22hex%22%3A%22FF0400%22%7D`.

---

## Further Reading

- [Official Maker API Docs](https://docs2.hubitat.com/en/apps/maker-api)
- [HSM Interface Reference](https://docs2.hubitat.com/en/developer/interfaces/hubitat-safety-monitor-interface)
