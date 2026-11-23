#!/usr/bin/env python3
"""
Validates that:
1. Every screen id from prototypes/wakatta/parts/*.screens.html exists in screens.json.
2. Every routeKey is declared in :core:navigation.
3. Every feature module contains entry registration / placeholder for its assigned screen ids.
Exits 0 on success, non-zero on failure.
"""
import sys
import os
import re
import json
import glob

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    android_dir = os.path.abspath(os.path.join(script_dir, ".."))
    repo_root = os.path.abspath(os.path.join(android_dir, ".."))

    # 1. Parse prototype screens
    proto_screens = set()
    proto_pattern = os.path.join(repo_root, "prototypes", "wakatta", "parts", "*.screens.html")
    for file_path in glob.glob(proto_pattern):
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()
            for m in re.finditer(r'data-screen=["\']([^"\']+)["\']', content):
                proto_screens.add(m.group(1))

    if not proto_screens:
        print(f"ERROR: No prototype screens found matching {proto_pattern}", file=sys.stderr)
        return 1

    # 2. Parse screens.json
    screens_json_path = os.path.join(android_dir, "screens.json")
    if not os.path.exists(screens_json_path):
        print(f"ERROR: Missing {screens_json_path}", file=sys.stderr)
        return 1

    with open(screens_json_path, "r", encoding="utf-8") as f:
        screens_data = json.load(f)

    json_screens = {s["id"]: s for s in screens_data}

    # Verify all prototype screens are present
    missing_in_json = proto_screens - set(json_screens.keys())
    if missing_in_json:
        print(f"ERROR: Screens missing from screens.json: {sorted(missing_in_json)}", file=sys.stderr)
        return 1

    # 3. Read all navigation route keys declared in :core:navigation
    nav_dir = os.path.join(android_dir, "core", "navigation", "src", "main", "kotlin")
    nav_content = ""
    for root, _, files in os.walk(nav_dir):
        for file in files:
            if file.endswith(".kt"):
                with open(os.path.join(root, file), "r", encoding="utf-8") as f:
                    nav_content += f.read() + "\n"

    # 4. Check each screen's routeKey and module entry
    errors = []
    for screen_id, info in json_screens.items():
        route_key = info.get("routeKey")
        module = info.get("module")
        if not route_key or not module:
            errors.append(f"Screen {screen_id} missing routeKey or module in screens.json")
            continue

        # Check routeKey in navigation code
        # Can match class/object/data class <route_key>
        if not re.search(r'\b(class|object|interface)\s+' + re.escape(route_key) + r'\b', nav_content):
            errors.append(f"RouteKey '{route_key}' for screen '{screen_id}' not found in :core:navigation")

        # Check module entry
        # module is like :feature:chat -> path android/feature/chat/src/main/kotlin
        parts = module.strip(":").split(":")
        module_path = os.path.join(android_dir, *parts, "src", "main", "kotlin")
        if not os.path.exists(module_path):
            errors.append(f"Module directory '{module_path}' for screen '{screen_id}' does not exist")
            continue

        module_content = ""
        for root, _, files in os.walk(module_path):
            for file in files:
                if file.endswith(".kt"):
                    with open(os.path.join(root, file), "r", encoding="utf-8") as f:
                        module_content += f.read() + "\n"

        # Module entry must reference the screen id or route key
        if screen_id not in module_content and route_key not in module_content:
            errors.append(f"Module '{module}' does not register or reference screen '{screen_id}' (routeKey: '{route_key}')")

    if errors:
        for err in errors:
            print(f"ERROR: {err}", file=sys.stderr)
        return 1

    print(f"SUCCESS: All {len(proto_screens)} prototype screens validated against screens.json, :core:navigation, and feature modules.")
    return 0

if __name__ == "__main__":
    sys.exit(main())
