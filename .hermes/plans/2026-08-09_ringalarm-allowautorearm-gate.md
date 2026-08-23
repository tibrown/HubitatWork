# RingAlarmManager — AllowAutoRearm Switch Gate (Plan Amendment)

> **For Hermes:** Use subagent-driven-development skill to implement this plan task-by-task.

**Goal:** Add a new `AllowAutoRearm` switch that gates the delayed re-arm feature in RingAlarmManager. Delayed re-arm only runs when BOTH `enableReArmOnDelay` is ON **and** `AllowAutoRearm` is ON. Turning `AllowAutoRearm` OFF cancels any running re-arm delay.

**Architecture:** Small amendment to the already-shipped DELAYED RE-ARM implementation. No new methods beyond a gating check; the existing schedule/fire/cancel structure is reused:
- `handleRingModeOff` schedules re-arm only if `enableReArmOnDelay && isAllowAutoRearmOn() && !isPauseBDAlarmOn()`.
- `reArmRingMode` (fire-time) bails if `AllowAutoRearm` is OFF.
- A new subscription + handler on the `AllowAutoRearm` switch cancels a running re-arm delay the moment it turns OFF (mirrors the existing `handlePauseBDAlarm` pattern).
- `AllowAutoRearm` ON does nothing special — it merely permits scheduling on the next external OFF.

**Tech Stack:** Groovy (Hubitat app), `capability.switch`, `runIn`/`unschedule`, `state` flags, push-groovy MCP (validate only — **DO NOT PUSH until user says**).

**File to change:** `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy` (current re-arm code at lines 52-61, 94-114, 148-169, 228-269)

---

## Current context / assumptions

- Delayed re-arm was implemented and pushed to the hub (app 971, version 5), committed as `95dd2db`.
- Current gating in `handleRingModeOff` (lines 162-168):
  ```groovy
  if (enableReArmOnDelay && !isPauseBDAlarmOn()) {
      scheduleReArm()
  } else if (enableReArmOnDelay) {
      logDebug "Not scheduling re-arm — PauseBDAlarm is ON (guard active)"
  }
  ```
- `handlePauseBDAlarm` (lines 262-269) already demonstrates the "cancel on switch-ON" pattern by calling `unschedule(reArmRingMode)`. The new AllowAutoRearm handler mirrors this but cancels on switch-**OFF**.
- `reArmRingMode` (lines 244-260) already has a fire-time guard for pause; the AllowAutoRearm check is added alongside it.
- RingModeOnOff is written by DoorWindowMonitor (`:588-603`) and NightSecurityManager (`:303-312`) — untouched by this amendment.

---

## Proposed approach

1. **New preference** `allowAutoRearmSwitch` (`capability.switch`, optional) in the DELAYED RE-ARM section, shown when `enableReArmOnDelay` is ON. Label: "Allow Auto Re-arm Switch (must be ON for delayed re-arm)".
2. **New helper** `isAllowAutoRearmOn()`: `true` when the switch is configured and ON; `true` when not configured (backward-compatible, feature behaves as before).
3. **Gating in `handleRingModeOff`**: add `&& isAllowAutoRearmOn()` to the scheduling condition.
4. **Fire-time guard in `reArmRingMode`**: bail if `!isAllowAutoRearmOn()`.
5. **Subscription in `initialize()`**: subscribe `allowAutoRearmSwitch`, `"switch"` → `handleAllowAutoRearm`.
6. **New handler `handleAllowAutoRearm(evt)`**: on value `"off"`, `unschedule(reArmRingMode)` (cancel running delay) + log. On `"on"`, no action.
7. **No push.** Validate only via push-groovy MCP; wait for explicit user command.

Exact code in tasks below.

---

## Step-by-step plan

### Task 1: Add the `allowAutoRearmSwitch` preference

**Files:**
- Modify: `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy:55-60` (inside the `if (settings.enableReArmOnDelay)` block of the DELAYED RE-ARM section)

**Step 1: Insert the input**

Current (lines 55-60):
```groovy
            if (settings.enableReArmOnDelay) {
                input "reArmDelay", "number", title: "Re-arm Delay (seconds)", required: true, defaultValue: 60, range: "1..600",
                    description: "Seconds to wait after the switch is turned OFF before turning it back ON"
                input "pauseBDAlarmSwitch", "capability.switch", title: "Pause Backdoor Alarm Switch (suppress re-arm when ON)", required: false,
                    description: "The shared PauseBDAlarm switch. When it is ON, re-arm is skipped so an intentional disarm while paused is not overridden."
            }
```

New (add the AllowAutoRearm input inside the same block, after `reArmDelay`):
```groovy
            if (settings.enableReArmOnDelay) {
                input "reArmDelay", "number", title: "Re-arm Delay (seconds)", required: true, defaultValue: 60, range: "1..600",
                    description: "Seconds to wait after the switch is turned OFF before turning it back ON"
                input "allowAutoRearmSwitch", "capability.switch", title: "Allow Auto Re-arm Switch (must be ON for delayed re-arm)", required: false,
                    description: "Delayed re-arm only happens while this switch is ON. Turning it OFF cancels any pending re-arm immediately. Leave unset for always-allowed (feature behaves as before)."
                input "pauseBDAlarmSwitch", "capability.switch", title: "Pause Backdoor Alarm Switch (suppress re-arm when ON)", required: false,
                    description: "The shared PauseBDAlarm switch. When it is ON, re-arm is skipped so an intentional disarm while paused is not overridden."
            }
```

**Step 2: Verify** the preference renders in the DELAYED RE-ARM section and does not require `submitOnChange` (no dependent inputs).

---

### Task 2: Add `isAllowAutoRearmOn()` helper and gate scheduling

**Files:**
- Modify: `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy:162-168` (gating) and `:232-235` (helper area, next to `isPauseBDAlarmOn`)

**Step 1: Add the helper**

Insert directly after `isPauseBDAlarmOn()` (line 235):
```groovy
// True when auto re-arm is permitted. Unconfigured = always allowed (backward compatible).
private Boolean isAllowAutoRearmOn() {
    if (!allowAutoRearmSwitch) return true
    return allowAutoRearmSwitch.currentValue("switch")?.toLowerCase() == "on"
}
```

**Step 2: Gate scheduling in `handleRingModeOff`**

Current (lines 162-168):
```groovy
    // Delayed re-arm (optional): schedule turning the switch back ON,
    // skipped while the Backdoor alarm is paused (existing behavior unchanged)
    if (enableReArmOnDelay && !isPauseBDAlarmOn()) {
        scheduleReArm()
    } else if (enableReArmOnDelay) {
        logDebug "Not scheduling re-arm — PauseBDAlarm is ON (guard active)"
    }
```

New:
```groovy
    // Delayed re-arm (optional): schedule turning the switch back ON.
    // Requires: feature enabled, AllowAutoRearm switch ON (or unset), and no backdoor pause.
    if (enableReArmOnDelay && isAllowAutoRearmOn() && !isPauseBDAlarmOn()) {
        scheduleReArm()
    } else if (enableReArmOnDelay) {
        logDebug "Not scheduling re-arm — AllowAutoRearm OFF or PauseBDAlarm ON (guard active)"
    }
```

**Step 3: Verify** the condition order reads naturally and logs distinguish the guard reason.

---

### Task 3: Fire-time guard in `reArmRingMode`

**Files:**
- Modify: `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy:244-250`

**Step 1: Add the AllowAutoRearm check**

Current (lines 244-250):
```groovy
def reArmRingMode() {
    // Fire-time safety guard: if the Backdoor alarm is now paused, do not re-arm
    // (the handlePauseBDAlarm subscription normally cancels the timer already).
    if (isPauseBDAlarmOn()) {
        logInfo "Re-arm skipped — PauseBDAlarm is ON (guard active)"
        return
    }
```

New:
```groovy
def reArmRingMode() {
    // Fire-time safety guards: if auto re-arm was disallowed or the Backdoor alarm
    // is now paused, do not re-arm (the subscriptions normally cancel the timer already).
    if (!isAllowAutoRearmOn()) {
        logInfo "Re-arm skipped — AllowAutoRearm is OFF (guard active)"
        return
    }
    if (isPauseBDAlarmOn()) {
        logInfo "Re-arm skipped — PauseBDAlarm is ON (guard active)"
        return
    }
```

**Step 2: Verify** both guards precede the `outer switch check` and no behavior change when AllowAutoRearm is unset (helper returns `true`).

---

### Task 4: Subscribe to AllowAutoRearm + add `handleAllowAutoRearm` (cancel on OFF)

**Files:**
- Modify: `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy:106-114` (`initialize()`) and near `handlePauseBDAlarm` (lines 262-269)

**Step 1: Subscribe in `initialize()`**

Current (lines 106-114):
```groovy
def initialize() {
    logInfo "Initializing Ring Alarm Manager (repeatCount=${repeatCount}, quickRetryDelay=${quickRetryDelay}s, repeatDelay=${repeatDelay}s)"
    subscribe(ringModeOnOff, "switch.on", handleRingModeOn)
    subscribe(ringModeOnOff, "switch.off", handleRingModeOff)
    if (pauseBDAlarmSwitch) {
        subscribe(pauseBDAlarmSwitch, "switch", handlePauseBDAlarm)
        logDebug "Subscribed to PauseBDAlarm switch for re-arm cancellation"
    }
}
```

New:
```groovy
def initialize() {
    logInfo "Initializing Ring Alarm Manager (repeatCount=${repeatCount}, quickRetryDelay=${quickRetryDelay}s, repeatDelay=${repeatDelay}s)"
    subscribe(ringModeOnOff, "switch.on", handleRingModeOn)
    subscribe(ringModeOnOff, "switch.off", handleRingModeOff)
    if (allowAutoRearmSwitch) {
        subscribe(allowAutoRearmSwitch, "switch", handleAllowAutoRearm)
        logDebug "Subscribed to AllowAutoRearm switch for re-arm gating/cancellation"
    }
    if (pauseBDAlarmSwitch) {
        subscribe(pauseBDAlarmSwitch, "switch", handlePauseBDAlarm)
        logDebug "Subscribed to PauseBDAlarm switch for re-arm cancellation"
    }
}
```

**Step 2: Add the `handleAllowAutoRearm` handler**

Insert directly after `handlePauseBDAlarm` (line 269):
```groovy
def handleAllowAutoRearm(evt) {
    if (evt.value?.toLowerCase() == "off") {
        // Auto re-arm no longer permitted — cancel any running re-arm delay immediately.
        unschedule(reArmRingMode)
        logInfo "AllowAutoRearm turned OFF — cancelled any pending re-arm"
    }
    // When AllowAutoRearm turns ON, no action here — re-arm scheduling resumes
    // naturally on the next external RingModeOnOff OFF event.
}
```

**Step 3: Verify** the handler mirrors `handlePauseBDAlarm` but triggers on `"off"`, and the subscription is conditional on the switch being configured (removed automatically on next `updated()` if unset).

---

### Task 5: Validate (NO PUSH) — Groovy compile check only

**Files:**
- Validate: `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy`

**Step 1: Validate syntax on the hub (read-only)**
Run (push-groovy MCP): `validate_groovy_syntax` with `file_path=/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy`
Expected: `COMPILER PASS`

**Step 2: STOP — do NOT push**
Per user instruction: do not call `push_hubitat_local_file`. Report validation result and wait for explicit approval.

**Step 3: Reconfigure (manual, after user approves push)**
- Open the installed **Ring Alarm Manager** app.
- Select **Allow Auto Re-arm Switch** = the `AllowAutoRearm` virtual switch (creates it first in Hubitat if it doesn't exist — Connector Switch, capability.switch).
- Save. Feature only runs when `enableReArmOnDelay` is ON **and** `AllowAutoRearm` is ON.

---

## Tests / validation

- Groovy compile check via `validate_groovy_syntax` → `COMPILER PASS` (no push).
- Manual functional tests on the hub (after push approval):
  1. **Enabled + switch ON:** `enableReArmOnDelay` ON, `AllowAutoRearm` ON → external OFF schedules re-arm (log `Scheduling Ring Mode re-arm in Ns`).
  2. **Enabled + switch OFF:** `enableReArmOnDelay` ON, `AllowAutoRearm` OFF → external OFF does NOT schedule (log `Not scheduling re-arm — AllowAutoRearm OFF or PauseBDAlarm ON`).
  3. **Mid-delay cancel:** schedule a re-arm, then turn `AllowAutoRearm` OFF before the delay elapses → immediate log `AllowAutoRearm turned OFF — cancelled any pending re-arm`, no re-arm at fire time.
  4. **Fire-time guard:** with `AllowAutoRearm` OFF at fire time, log `Re-arm skipped — AllowAutoRearm is OFF (guard active)`.
  5. **Unset switch (backward compat):** `allowAutoRearmSwitch` unconfigured → behaves exactly like the previous version (re-arm permitted when `enableReArmOnDelay` on and not paused).
  6. **Pause interaction unchanged:** with `AllowAutoRearm` ON but `PauseBDAlarm` ON → no re-arm scheduled (existing behavior).
- Verify no regression: normal repeat-sequence arming still works.

---

## Risks, trade-offs, and open questions

- **Backward compatibility decision:** unset `allowAutoRearmSwitch` = always allowed. If the user would rather require it to be configured (no fallback), change `isAllowAutoRearmOn()` to return `false` when unconfigured. Open question — default chosen for least surprise.
- **Implementation overlap:** this amendment replaces nothing from the previous commit `95dd2db`; it layers an additional gate. The previous behavior is preserved when the new switch is unset.
- **Deployment:** app code ID 971 (confirmed via `search_hubitat_apps`). NO push until explicit user command.
- **Open question:** should `AllowAutoRearm` OFF also cancel a re-arm that has ALREADY fired (i.e., mid repeat-cycle)? Current plan: no — the gate only affects scheduling and the pending delay, matching the user's description ("turning it off cancels the delayed rearm").

---

## Files likely to change

- `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy` (only file; no other apps touched)
- Commit after implementation, only this file.

---