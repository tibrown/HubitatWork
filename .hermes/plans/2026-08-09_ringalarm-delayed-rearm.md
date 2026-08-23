# RingAlarmManager — Delayed Re-Arm of RingModeOnOff Implementation Plan

> **For Hermes:** Use subagent-driven-development skill to implement this plan task-by-task.

**Goal:** Add a disableable option to RingAlarmManager that turns the `RingModeOnOff` switch back **ON** after a configurable number of seconds after it has been turned **OFF** (delayed re-arm).

**Architecture:** Extend the existing `RingAlarmManager` app (the sole subscriber to RingModeOnOff switch events — per the app's own docs, no other app subscribes). The new behavior hooks into the existing external-OFF branch of `handleRingModeOff()`: when the switch is turned OFF externally (not during the app's own repeat cycle), schedule a one-shot timer that re-issues `ringModeOnOff.on()` after a settable delay — **but only if a Backdoor-pause is not currently active** (the `pauseBDAlarm` guard). The re-arm intentionally does **not** set `state.intentionalOn`, so the re-arm flows through the app's normal `handleRingModeOn()` path and its existing "repeat ON" reliability logic. The timer is cancellable (external ON supersedes it), the whole feature is gated by a boolean so it can be fully disabled, and re-arm is suppressed while the Backdoor alarm is paused.

**Tech Stack:** Groovy (Hubitat app), `capability.switch`, `runIn`/`unschedule` scheduler, `state` flags, push-groovy MCP for validate + push.

**File to change:** `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy` (app code ID 1716 on the hub — verify with search before pushing).

---

## Current context / assumptions

- `RingAlarmManager` is GitHub-tracked at `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy`, currently version 18 on the hub.
- It subscribes to both `switch.on` and `switch.off` of the `RingModeOnOff` switch (lines 96-97).
- `handleRingModeOff` (lines 129-142) already cancels pending repeat sends on external OFF. It distinguishes the app's own repeat-cycle OFF via `state.intentionalOff`.
- RingModeOnOff is also written by two other apps (facts to be aware of, not modified):
  - `DoorWindowMonitor.groovy:588-603` — turns it ON on backdoor-unpause.
  - `NightSecurityManager.groovy:303-312` — turns it OFF on pause-backdoor, ON on unpause.
- The new re-arm feature interacts with those writers. The intentional-pause conflict (a pause/disarm being fought by re-arm) is handled by adding a **`pauseBDAlarmSwitch` guard** to RingAlarmManager: when `PauseBDAlarm` is ON, re-arm is suppressed (checked at both schedule and fire time). This requires selecting the shared `PauseBDAlarm` switch in the RingAlarmManager app config.

---

## Proposed approach

Add to RingAlarmManager:

1. **Three new preferences** in a new (or the existing Repeat) section:
   - `enableReArmOnDelay` (bool, default `false`) — master switch, the "disableable" requirement.
   - `reArmDelay` (number, default `60`, range `1..600`) — seconds to wait after an external OFF before re-arming.
   - `pauseBDAlarmSwitch` (capability.switch, optional) — the shared `PauseBDAlarm` switch (also used by NightSecurityManager/DoorWindowMonitor). When it is ON, re-arm is suppressed and any running re-arm delay is cancelled (the pauseBDAlarm guard).
2. **Hook in `handleRingModeOff`**: after cancelling repeat sends, if `enableReArmOnDelay` is on **and** the pause is **not** in effect, call `scheduleReArm()`. If pause IS in effect, do nothing (existing behavior unchanged).
3. **Subscribe to `pauseBDAlarmSwitch.switch`** so the app reacts to the pause switch changing:
   - When it turns **ON** → `unschedule(reArmRingMode)` (cancel any running re-arm delay) and log. Pause now owns the switch — existing pause behavior (via NightSecurityManager/DoorWindowMonitor) continues unchanged, and no re-arm timer is pending.
   - When it turns **OFF** → no action (re-arm scheduling resumes on the next external OFF event).
4. **New private helper `isPauseBDAlarmOn()`**: returns `true` if `pauseBDAlarmSwitch` is configured and its `switch` attr is `"on"`.
5. **New private method `scheduleReArm()`**: `unschedule(reArmRingMode)` then `runIn(delay, reArmRingMode)`.
6. **New method `reArmRingMode()`**: fire-time safety guard — if `isPauseBDAlarmOn()` return early (belt-and-suspenders; the subscription normally cancels the timer already). Otherwise, if the switch is currently OFF, call `ringModeOnOff.on()` (without setting `intentionalOn`, so the normal arming/repeat path runs); if already ON, skip.
7. **Cancellation in `handleRingModeOn`**: `unschedule(reArmRingMode)` so a genuine external/via-other-app ON cancels any pending re-arm.
8. **Cleanup in `updated()`**: `unsubscribe()` is already called; add `unschedule(reArmRingMode)` to the existing cleanup block.

**Exact semantics (per user):**
- PauseBDAlarm in effect → existing/system behavior, **no change**.
- New feature only ever acts when PauseBDAlarm is **OFF**.
- If PauseBDAlarm turns **ON** while the re-arm delay is running → the delay is **reset/cancelled immediately** (`unschedule`) and pause takes over. There is no re-arm once paused.

Exact code below.

---

## Step-by-step plan

### Task 1: Add preferences for the re-arm feature

**Files:**
- Modify: `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy` (inside `mainPage`, after the REPEAT CONFIGURATION section at line 50).

**Step 1: Insert the section**

Add directly after the `offDelay` input (line 49's closing) and before the closing of the REPEAT CONFIGURATION section (line 50):

```groovy
        section("<b>═══════════════════════════════════════</b>\n<b>DELAYED RE-ARM</b>\n<b>═══════════════════════════════════════</b>") {
            input "enableReArmOnDelay", "bool", title: "Re-arm Ring Mode after it is turned OFF?", defaultValue: false, submitOnChange: true,
                description: "When ON, turns RingModeOnOff back ON automatically after the delay below (unless Backdoor alarm is paused)"
            if (settings.enableReArmOnDelay) {
                input "reArmDelay", "number", title: "Re-arm Delay (seconds)", required: true, defaultValue: 60, range: "1..600",
                    description: "Seconds to wait after the switch is turned OFF before turning it back ON"
                input "pauseBDAlarmSwitch", "capability.switch", title: "Pause Backdoor Alarm Switch (suppress re-arm when ON)", required: false,
                    description: "The shared PauseBDAlarm switch. When it is ON, re-arm is skipped so an intentional disarm while paused is not overridden."
            }
        }
```

**Step 2: Verify** `updated()` still compiles — it already resets repeat state; the new inputs are plain settings, no change needed here yet.

---

### Task 2: Schedule the re-arm in `handleRingModeOff`

**Files:**
- Modify: `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy:129-142`

**Step 1: Edit the external-OFF branch**

Current (lines 135-141):
```groovy
    logInfo "RingModeOnOff turned OFF externally — cancelling any pending repeat sends"
    unschedule(sendNextRingOn)
    unschedule(doRingOn)
    state.repeatSendsPending = false
    state.repeatsRemaining = 0
    state.intentionalOff = false
    state.intentionalOn = false
```

New (append re-arm scheduling):
```groovy
    logInfo "RingModeOnOff turned OFF externally — cancelling any pending repeat sends"
    unschedule(sendNextRingOn)
    unschedule(doRingOn)
    state.repeatSendsPending = false
    state.repeatsRemaining = 0
    state.intentionalOff = false
    state.intentionalOn = false

    // Delayed re-arm (optional): schedule turning the switch back ON,
    // skipped while the Backdoor alarm is paused
    if (enableReArmOnDelay && !isPauseBDAlarmOn()) {
        scheduleReArm()
    } else if (enableReArmOnDelay) {
        logDebug "Not scheduling re-arm — PauseBDAlarm is ON (guard active)"
    }
```

**Step 2: Verify** the `if (state.intentionalOff) { ... return }` guard (lines 131-134) remains ABOVE this block so the app's own repeat-cycle OFF never schedules a re-arm.

---

### Task 3: Add `isPauseBDAlarmOn()`, `scheduleReArm()` and `reArmRingMode()` methods

**Files:**
- Modify: `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy` (insert between the CHAINED REPEAT SEND HANDLERS section (ends line 199) and the NOTIFICATION section (line 201)).

**Step 1: Insert the three new methods**

```groovy
// ========================================
// DELAYED RE-ARM
// ========================================

// True when the Backdoor alarm is paused (PauseBDAlarm switch ON).
private Boolean isPauseBDAlarmOn() {
    return pauseBDAlarmSwitch?.currentValue("switch")?.toLowerCase() == "on"
}

private void scheduleReArm() {
    unschedule(reArmRingMode)
    Integer delay = (reArmDelay ?: 60) as Integer
    logInfo "Scheduling Ring Mode re-arm in ${delay}s"
    runIn(delay, reArmRingMode)
}

def reArmRingMode() {
    // Fire-time pause guard: if the Backdoor alarm is now paused, do not re-arm
    if (isPauseBDAlarmOn()) {
        logInfo "Re-arm skipped — PauseBDAlarm is ON (guard active)"
        return
    }
    // Do not re-arm if the switch has already been turned ON since the schedule
    if (ringModeOnOff.currentValue("switch")?.toLowerCase() != "on") {
        logInfo "Re-arming Ring Mode (delayed re-arm)"
        // Deliberately do NOT set state.intentionalOn — we want the normal
        // handleRingModeOn path (and its repeat-ON reliability) to run.
        ringModeOnOff.on()
    } else {
        logDebug "Re-arm fired but switch already ON — skipping"
    }
}
```

**Step 2: Verify** `reArmRingMode` and `isPauseBDAlarmOn` are referenced only in the intended spots and have no conflicting names elsewhere in the file.

---

### Task 4: Subscribe to PauseBDAlarm and add `handlePauseBDAlarm` (cancel running re-arm)

**Files:**
- Modify: `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy` (in `initialize()`, lines 94-98; and add a new handler near the DELAYED RE-ARM methods).

**Rationale:** Per the user's exact semantics, if `PauseBDAlarm` turns ON *while* the re-arm delay is running, the delay must be **reset/cancelled immediately** and pause must take over. This requires the app to subscribe to the pause switch and `unschedule(reArmRingMode)` on pause-ON — not just a fire-time guard.

**Step 1: Subscribe in `initialize()`**

Extend the existing `initialize()` (lines 94-97):
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

**Step 2: Add the `handlePauseBDAlarm` handler**

Insert near the DELAYED RE-ARM methods (below `reArmRingMode()`):
```groovy
def handlePauseBDAlarm(evt) {
    if (evt.value?.toLowerCase() == "on") {
        // Pause is now in effect — cancel any running re-arm delay immediately.
        // Pause own behavior (via NightSecurityManager/DoorWindowMonitor) is untouched.
        unschedule(reArmRingMode)
        logInfo "PauseBDAlarm turned ON — cancelled any pending re-arm (pause takes over)"
    }
    // When PauseBDAlarm turns OFF, no action here — re-arm scheduling resumes
    // naturally on the next external RingModeOnOff OFF event.
}
```

**Step 3: Verify** the subscription only happens when `pauseBDAlarmSwitch` is configured (so disabling/clearing the input removes the subscription on next `updated()`).

---

### Task 5: Cancel pending re-arm on genuine ON + cleanup in `updated()`

**Files:**
- Modify: `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy` (in `handleRingModeOn`, lines 104-127; and in `updated()`, lines 83-91).

**Step 1: Cancel in `handleRingModeOn`**

In the genuine (non-intentional, non-pending) branch, after the guards at lines 119-121, add:
```groovy
    // A genuine ON (manual, other app, or re-arm) supersedes any pending re-arm
    unschedule(reArmRingMode)
```

**Step 2: Cleanup in `updated()`**

Extend the existing cleanup (lines 87-90) to also cancel/clear the re-arm:
```groovy
    unschedule(reArmRingMode)
```

**Step 3: Verify** the added lines are in the correct branches (only the genuine-ON path cancels; a re-arm-originated ON still reaches there and is harmless because the timer already fired/scheduled danger is nil).

---

### Task 6: Validate, push, and re-configure the installed app

**Files:**
- Validate + push: `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy`

**Step 1: Validate Groovy syntax on the hub**
Run (push-groovy MCP): `validate_groovy_syntax` with `file_path=/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy`
Expected: `COMPILER PASS`

**Step 2: Find the app code ID**
Run (push-groovy MCP): `search_hubitat_apps` with `name_query="Ring Alarm Manager"`
Expected: an App Code ID (currently 1716 — confirm; the earlier installed-app id was 1931 from `list_apps`, but deployment targets the **App Code ID**, not the installed instance).

**Step 3: Push**
Run (push-groovy MCP): `push_hubitat_local_file` with `target_app_id=<code id>` and the file path.
Expected: `✅ SUCCESS: App <id> updated to version N`.

**Step 4: Re-configure installed instance (manual/user step)**
Open the installed **Ring Alarm Manager** app in Hubitat and, to enable:
- Set **"Re-arm Ring Mode after it is turned OFF?"** = ON
- Set **Re-arm Delay (seconds)** to the desired value (default 60)
- Select **Pause Backdoor Alarm Switch** = the shared `PauseBDAlarm` switch (so the guard can suppress re-arm while the backdoor alarm is paused)
- Save.
This is required because the new inputs default to disabled/false.

---

## Tests / validation

- Groovy compile check via `validate_groovy_syntax` → `COMPILER PASS`.
- Manual functional test on the hub:
  1. Enable the feature with a short delay (e.g. 10s) and select the PauseBDAlarm switch.
  2. Turn `RingModeOnOff` OFF (as if disarmed).
  3. Observe log: `Scheduling Ring Mode re-arm in 10s`.
  4. After ~10s, observe `Re-arming Ring Mode (delayed re-arm)` and the switch returning ON.
  5. Negative test: turn the switch OFF, then manually turn it ON before the delay elapses — expect `handleRingModeOn` cancels the pending re-arm (no surprise later ON); verify log shows no re-arm after the manual ON.
  6. **Pause-guard test (schedule-time):** with `PauseBDAlarm` switch ON, turn `RingModeOnOff` OFF — expect log `Not scheduling re-arm — PauseBDAlarm is ON`, and the switch stays OFF (no re-arm).
  7. **Pause mid-delay test (active cancellation):** with the feature enabled and delay ON, turn `RingModeOnOff` OFF to schedule a re-arm (log `Scheduling Ring Mode re-arm in Ns`), then turn `PauseBDAlarm` switch ON before the delay elapses — expect immediate log `PauseBDAlarm turned ON — cancelled any pending re-arm (pause takes over)` and **no** re-arm at the original fire time (pause takes over).
  8. Disable test: with `enableReArmOnDelay` OFF, turning the switch OFF produces **no** re-arm scheduling.
- Verify no regression in the existing repeat-sequence logic (an OFF→ON arming still fires the normal repeats).

---

## Risks, trade-offs, and open questions

- **Intentional-pause conflict — resolved with guard:** When `NightSecurityManager` (pause-backdoor) turns the switch OFF, the re-arm feature would normally re-arm it and fight the intentional disarm. This is handled three ways, matching the user's exact semantics:
  1. Schedule-time (`handleRingModeOff`): if `PauseBDAlarm` is ON, no re-arm is scheduled (existing behavior unchanged).
  2. Subscription (`handlePauseBDAlarm`): if `PauseBDAlarm` turns ON *while* a re-arm delay is running, the timer is `unschedule`d immediately and pause takes over.
  3. Fire-time safety (`reArmRingMode`): even if a timer somehow still fires while paused, it bails.
  Residual caveat: all of this depends on the user selecting the correct `PauseBDAlarm` switch in the app config; if it is left unselected, the guard is inert and re-arm behaves as a plain delayed re-arm.
- **Re-arm flows through repeat logic:** Because `reArmRingMode` does not set `intentionalOn`, the re-arm triggers a full repeat-ON cycle. This is desirable (reliable arming) but means re-arming also produces the app's repeat notifications when `notifyOnRepeat` is set.
- **Open question 1:** Should the re-arm apply in all modes, or only certain modes (e.g. only Night/Away)? Current plan: all modes. User can disable if too aggressive.
- **Open question 2:** Should there be a cap on how long the re-arm is allowed to delay a genuine disarm (e.g., a window during which OFF is respected before re-arming)? Not in scope unless requested.
- **Deployment target:** ensure the push uses the **App Code ID** from `search_hubitat_apps`, not the installed-instance id.

---

## Files likely to change

- `/home/tim/gitrepos/HubitatWork/Apps/RingAlarmManager/RingAlarmManager.groovy` (the only file changed)
- No DB/schema changes; no other apps touched.

---

## Remember

Bite-sized tasks (each above is 2-5 min), exact file paths, complete copy-pasteable code, exact commands with expected output, verification steps, DRY/YAGNI, and a `git commit` after implementation (the repo uses git).
