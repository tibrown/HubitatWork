---
name: hubitat-deploy
description: 'Deploy or update a Hubitat Groovy app on the live hub. Use when the user asks to "push", "deploy", "publish", or "update" a Hubitat app/driver to the hub, or asks to sync a local .groovy file in Apps/ with the hub.'
---

# Hubitat Deployment

Fast, direct-to-compiler deployment pipeline for Hubitat Groovy apps in this repo. When a user asks to deploy/push/update a Hubitat app, follow these steps in order without deviation.

**Working directory:** All deployable Groovy files live under `Apps/<AppName>/<AppName>.groovy` in this workspace. Resolve the user's requested app name to its full absolute path under `Apps/` before doing anything else (use `file_search` if the exact path isn't already known).

## Step 1 — Read the app name

Use `read_file` to inspect only the first ~30 lines of the target file. Locate the `name:` property inside the `definition(...)` block to confirm the app name. Do not read the rest of the file unless you need to make edits.

## Step 2 — Validate compiler safety

Call `mcp_push-groovy_validate_groovy_syntax` with the file's full local path (`file_path`). Do not read the file into your context and pass raw source — always pass the path so the tool reads it directly from disk.

- If it returns `COMPILER FAIL`, stop and report the errors. Do not proceed to Step 3.

## Step 3 — Direct deployment

If Step 2 returns `COMPILER PASS`:

1. If the target hub Code ID (`target_app_id`) isn't already known, look it up with `mcp_push-groovy_search_hubitat_apps` using the app name.
2. Call `mcp_push-groovy_push_hubitat_local_file` with the explicit `target_app_id` and the full local `file_path`.
3. Report back: the app name, the target Code ID, and the compilation/push result.

## Strict constraints

- Never paste raw Groovy source across tool calls — always pass the local `file_path` and let the MCP tools read from disk.
- Never skip the Step 2 validation before pushing.
- Don't guess a `target_app_id`; look it up via `mcp_push-groovy_search_hubitat_apps` if uncertain.
