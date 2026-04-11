You should always analyze the .md files in this solution to understand the context and documentation of the project.

## Session Initialization

At the start of every new session, you MUST review the documentation in the `Docs/` and `wiki-export/` folders before taking any action. This includes:
- `Docs/` — project documentation, device tables, hub variables, and best practices
- `wiki-export/` — wiki pages for each app and feature area

This ensures you have full context about the project before making changes or answering questions.

## Coding Standards

### Hubitat Apps
All Hubitat app files (.groovy) must include the following copyright header at the top of the file:

```groovy
/**
 *  [App Name]
 *
 *  Copyright 2025 Tim Brown
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
```

This header should be placed before the `definition()` block.