# Changelog

## [Unreleased]

### Added

### Changed

### Fixed

### Removed

## [0.4.0] - 2026-11-12

### Added

- A "Generate token" link next to the API token field, which opens the page in ClickUp where you create one
  ([#42](https://github.com/jaim3r0jas/clickup-intellij-plugin/issues/42)).
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

## [0.3.6] - 2025-12-11

### Fixed

- The plugin works again on the latest PhpStorm
  ([#63](https://github.com/jaim3r0jas/clickup-intellij-plugin/issues/63)).

## [0.3.5] - 2025-11-24

### Changed

- Build and test tooling only, nothing changes for users.

## [0.3.4] - 2025-11-24

### Changed

- More reliable checks of the ClickUp connection and of the server settings.

## [0.3.3] - 2025-11-16

### Fixed

- The ClickUp icon of the task server.

## [0.3.2] - 2025-11-16

### Changed

- Maintenance release.

## [0.3.1] - 2025-11-15

### Changed

- A task without a custom task type counts as a feature.

## [0.3.0] - 2025-11-15

### Added

- Custom task IDs: the "Use Custom Task IDs" option shows and finds tasks by your workspace's IDs (like `DEV-123`).

## [0.2.3] - 2025-05-17

### Changed

- Maintenance release.

## [0.2.2] - 2025-05-04

### Added

- Support for the 2025.2 IDEs ([#44](https://github.com/jaim3r0jas/clickup-intellij-plugin/issues/44)).

## [0.2.1] - 2025-04-09

### Fixed

- The server settings didn't show the server URL, and the dropdowns didn't refresh
  ([#40](https://github.com/jaim3r0jas/clickup-intellij-plugin/issues/40)).

## [0.2.0] - 2025-04-09

### Changed

- Simpler server settings: the tasks of the whole workspace, without picking a space and a list.

## [0.1.4] - 2025-04-08

### Fixed

- The statuses offered for a task are read from the task itself.

## [0.1.3] - 2025-04-08

### Fixed

- The statuses offered for a task are those of its own space, not of the space picked in the settings.

## [0.1.2] - 2025-04-08

### Changed

- ClickUp requests need only the workspace.

## [0.1.1] - 2025-04-08

### Changed

- Documentation.

## [0.1.0] - 2025-04-07

### Added

- An assignee filter in the server settings.
- More than 100 tasks, fetched page by page.

## [0.0.9] - 2025-04-07

### Added

- The ClickUp icon.
- The Test button is enabled as soon as a token is set.
- The server settings are kept across IDE restarts.

### Fixed

- Compatibility with IntelliJ IDEA 2025.1.

[Unreleased]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.4.0...HEAD
[0.4.0]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.3.6...0.4.0
[0.3.6]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.3.5...0.3.6
[0.3.5]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.3.4...0.3.5
[0.3.4]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.3.3...0.3.4
[0.3.3]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.3.2...0.3.3
[0.3.2]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.3.1...0.3.2
[0.3.1]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.3.0...0.3.1
[0.3.0]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.2.3...0.3.0
[0.2.3]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.2.2...0.2.3
[0.2.2]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.2.1...0.2.2
[0.2.1]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.2.0...0.2.1
[0.2.0]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.1.4...0.2.0
[0.1.4]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.1.3...0.1.4
[0.1.3]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.1.2...0.1.3
[0.1.2]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.1.1...0.1.2
[0.1.1]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/jaim3r0jas/clickup-intellij-plugin/compare/0.0.9...0.1.0
[0.0.9]: https://github.com/jaim3r0jas/clickup-intellij-plugin/commits/0.0.9
