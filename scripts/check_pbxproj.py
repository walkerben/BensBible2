#!/usr/bin/env python3
"""
Post-edit hook: checks that any .swift file edited is referenced in the Xcode project.
Reads CLAUDE_TOOL_INPUT from env to get the file path that was just edited.
"""

import json
import os
import sys

tool_input_raw = os.environ.get("CLAUDE_TOOL_INPUT", "{}")
try:
    tool_input = json.loads(tool_input_raw)
except json.JSONDecodeError:
    sys.exit(0)

file_path = tool_input.get("file_path", "")

if not file_path.endswith(".swift"):
    sys.exit(0)

pbxproj_path = "BensBible2.xcodeproj/project.pbxproj"
if not os.path.exists(pbxproj_path):
    sys.exit(0)

filename = os.path.basename(file_path)

with open(pbxproj_path, "r") as f:
    contents = f.read()

if filename not in contents:
    print(
        f"WARNING: '{filename}' was edited but is NOT referenced in {pbxproj_path}. "
        f"Add it to the Xcode project to avoid build failures.",
        file=sys.stderr,
    )
    sys.exit(1)
