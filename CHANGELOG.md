# Changelog

## [Unreleased]

### Added

- Support for the 2026.3 IDEs. From 2026.3 on, the IDE no longer bundles the Tasks API: the plugin needs the
  JetBrains "Issue Trackers" plugin, which the IDE installs along with it.

### Changed

- The plugin needs an IDE from 2024.3 on.
- The server settings load your workspaces in the background, so the dialog no longer freezes while ClickUp answers.

### Fixed

- Workspaces always empty, and the Test button passing with any API token: every ClickUp call now checks the
  answer, a rejected token shows as such in the settings, and a new token takes effect right away
  ([#56](https://github.com/jaim3r0jas/clickup-intellij-plugin/issues/56)).
- A task ClickUp can't find (deleted, or opened before a switch to custom task IDs) no longer comes back as an
  empty task ([#55](https://github.com/jaim3r0jas/clickup-intellij-plugin/issues/55)).
