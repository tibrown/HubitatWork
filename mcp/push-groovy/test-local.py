import requests
import json
import os

HUB_IP = "192.168.0.142"
TARGET_APP_ID = 975
FILE_PATH = "test_app.groovy"

# 1. Read the local file
if not os.path.exists(FILE_PATH):
    print(f"❌ FAILED: {FILE_PATH} not found.")
    exit(1)

print(f"Reading {FILE_PATH}...")
with open(FILE_PATH, 'r', encoding='utf-8') as f:
    groovy_source = f.read()

# 2. Fetch the current version token from Hubitat
get_url = f"http://{HUB_IP}/app/ajax/code?id={TARGET_APP_ID}"
print(f"Fetching current lock version from {get_url}...")

try:
    get_resp = requests.get(get_url, timeout=5)
    get_resp.raise_for_status()
    data = get_resp.json()
    version = data.get('version')
    
    if version is None:
        print(f"❌ FAILED: Response did not contain a version token. Payload: {data}")
        exit(1)
    print(f"Current Hubitat App Version Token: {version}")

except Exception as exc:
    print(f"❌ FAILED to contact hub for version: {exc}")
    exit(1)

# 3. Post the update directly to the native compiler
post_url = f"http://{HUB_IP}/app/ajax/update"
payload = {
    "id": TARGET_APP_ID,
    "version": version,
    "source": groovy_source
}

print(f"Pushing payload to compiler endpoint {post_url}...")
try:
    post_resp = requests.post(post_url, data=payload, timeout=5)
    post_resp.raise_for_status()
    result = post_resp.json()
    
    if result.get('status') == 'success':
        print(f"\n✅ SUCCESS! App {TARGET_APP_ID} has been updated to version {result.get('version')}.")
    else:
        print(f"\n❌ HUBITAT COMPILER ERROR:\n{json.dumps(result, indent=2)}")

except Exception as exc:
    print(f"\n❌ FAILED during push network request: {exc}")
