# Changelog

## [Unreleased]

### Added

- Support for the 2026.3 IDEs. From 2026.3 on, the IDE no longer bundles the Tasks API: the plugin needs the
  JetBrains "Issue Trackers" plugin, which the IDE installs along with it.

### Changed

- The plugin needs an IDE from 2024.3 on.
- The server settings load your workspaces in the background, so the dialog no longer freezes while ClickUp answers.

### Fixed

- New tasks missing from the task list until the server settings were applied again: a failed refresh no longer
  makes the IDE stop asking ClickUp or empties the list, the most recently updated tasks come first, paging reaches
  past the first 100 tasks (up to 500 per request), and the plugin sends far fewer requests (one for the custom task
  types instead of one per task, and ClickUp pages reused for 30 seconds), which kept running into ClickUp's rate
  limit ([#66](https://github.com/jaim3r0jas/clickup-intellij-plugin/issues/66)).
- "Include closed" in the task list now shows closed ClickUp tasks.
- Workspaces always empty, and the Test button passing with any API token: every ClickUp call now checks the
  answer, a rejected token shows as such in the settings, and a new token takes effect right away
  ([#56](https://github.com/jaim3r0jas/clickup-intellij-plugin/issues/56)).
- "ClickUpTask.getId must not return null" on commit or when switching tasks: a task ClickUp can't find (deleted, or
  opened before a switch to custom task IDs) no longer comes back as an empty task, and a task with missing fields no
  longer fails the IDE ([#55](https://github.com/jaim3r0jas/clickup-intellij-plugin/issues/55)).
