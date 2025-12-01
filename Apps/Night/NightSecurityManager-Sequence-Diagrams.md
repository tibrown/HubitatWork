# Night Security Manager Sequence Diagrams

## Flow 1: Intruder at Front Door (Triggers Execute Alarms)

This flow demonstrates the sequence of events when the Front Door is opened, triggering the main alarm system.

```mermaid
sequenceDiagram
    participant Sensor as Front Door
    participant App as Night Security Manager
    participant Switch1 as Alarms Enabled
    participant Switch2 as Silent
    participant Lights as All Lights
    participant Switch3 as All Lights On
    participant Notify as Notification Devices
    participant Siren as Sirens

    Sensor->>App: open
    App->>Switch1: check status (on)
    App->>Switch2: check status (off)
    par Actions
        App->>Lights: on()
        App->>Switch3: on()
        App->>Notify: deviceNotification("Intruder at the Front Door")
        App->>App: runIn(5, executeAlarmsOn)
    end
    
    Note over App: 5 seconds later
    
    App->>App: executeAlarmsOn()
    App->>Switch1: check status (on)
    App->>Siren: siren()
    App->>App: runIn(300, stopAlarms)
    
    Note over App: 300 seconds later
    
    App->>App: stopAlarms()
    App->>Siren: off()
```

## Flow 2: Intruder in Woodshed (Triggers Execute Shed Siren)

This flow demonstrates the sequence of events when the Woodshed Door is opened, triggering the shed siren (short duration).

```mermaid
sequenceDiagram
    participant Sensor as Woodshed Door
    participant App as Night Security Manager
    participant Switch1 as Silent
    participant Switch2 as Alarms Enabled
    participant Notify as Notification Devices
    participant Siren as Sirens
    participant Lights as All Lights
    participant Switch3 as All Lights On

    Sensor->>App: open
    App->>Switch1: check status (off)
    App->>Switch2: check status (on)
    
    par Actions
        App->>Notify: deviceNotification("Intruder in the Woodshed")
        App->>App: executeShedSirenOn()
        App->>Lights: on()
        App->>Switch3: on()
    end
    
    App->>Siren: siren()
    App->>App: runIn(4, stopShedSiren)
    
    Note over App: 4 seconds later
    
    App->>App: stopShedSiren()
    App->>Siren: off()
```
