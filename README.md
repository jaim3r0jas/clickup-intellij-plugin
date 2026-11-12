[![Build](https://github.com/jaim3r0jas/clickup-intellij-plugin/actions/workflows/build.yaml/badge.svg)](https://github.com/jaim3r0jas/clickup-intellij-plugin/actions/workflows/build.yaml)
[![codecov](https://codecov.io/gh/jaim3r0jas/clickup-intellij-plugin/graph/badge.svg?token=43VL7NXZTL)](https://codecov.io/gh/jaim3r0jas/clickup-intellij-plugin)

### IntelliJ IDEA Tasks Integration for ClickUp

#### Description

Work on your [ClickUp](https://clickup.com) tasks without leaving the IDE. ClickUp Integration adds ClickUp as a
task server to the IDE's tasks support: open a ClickUp task, and the IDE switches your branch, changelist and editor
context to it, keeps its status in ClickUp up to date and fills your commit message.

[Plugin on JetBrains Marketplace](https://plugins.jetbrains.com/plugin/26944-clickup-integration) ·
[Changelog](CHANGELOG.md)

#### Features

- **Your ClickUp tasks in the IDE.** `Tools` > `Tasks & Contexts` > `Open Task` lists the tasks of your workspace,
  assigned to you or to anyone, most recently updated first. Closed tasks on request.
- **Status updates.** Move a task to any status of its ClickUp space when you open or finish it.
- **Branches and context.** A branch and a changelist per task, and your editor tabs and breakpoints restored when
  you switch back.
- **Commit messages from the task.** Placeholders for the task's ID, number, summary, project and type, plus
  [Conventional Commits](https://www.conventionalcommits.org/) ones (`${type}`, `${scope}`, `${description}`,
  `${body}`, `${footer}`, `${breaking}`): a ClickUp Bug becomes `fix`, a Task or Feature `feat`. See
  [docs/CONVENTIONAL_COMMITS.md](docs/CONVENTIONAL_COMMITS.md).
- **Custom task IDs.** Use your workspace's custom IDs (like `DEV-123`) instead of ClickUp's internal ones.
- **Time tracking.** Log the time you spent on a task to ClickUp.

#### Compatibility

- Every IntelliJ-based IDE from 2024.3 on: IntelliJ IDEA, PhpStorm, WebStorm, PyCharm, GoLand, Rider and the others.
- From 2026.3 on, the IDE's tasks support comes from the JetBrains **Issue Trackers** plugin, which is installed
  along with this one when your IDE doesn't bundle it.
- Time tracking needs the IDE's time tracking feature: IntelliJ IDEA Ultimate up to 2025.3, the JetBrains
  **Time Tracking** plugin from 2026.3 on.

#### Installation

1. Open your JetBrains IDE.
2. Go to `Settings` (`Preferences` on macOS) > `Plugins`.
3. Search for `ClickUp Integration` in the `Marketplace` tab and install it.

#### Configuration

1. Go to `Settings` > `Tools` > `Tasks` > `Servers`, click `+` and choose `ClickUp`.
2. Paste your personal ClickUp API token. `Generate token` opens the page in ClickUp where you create it
   (`Settings` > `Apps`).
3. Pick your workspace and, optionally, an assignee, then click `Test`.

#### Usage

1. Open a task with `Tools` > `Tasks & Contexts` > `Open Task`.
2. Commit with the placeholders in your commit message template (`Settings` > `Tools` > `Tasks`), log time, and close
   the task when you're done.

#### Development Setup

1. Clone the repository:
   ```sh
   git clone https://github.com/jaim3r0jas/clickup-intellij-plugin.git
   cd clickup-intellij-plugin
    ```
2. Build the plugin:
   ```sh
   ./gradlew buildPlugin
   ```
3. Run the plugin in a sandbox IDE:
   ```sh
    ./gradlew runIde
    ```
   
#### Submit changes

To contribute to the project, please follow these steps:

1. Fork the repository.
2. Create a new branch for your feature or bug fix.
3. Make your changes and commit them with a clear message.
4. Push your changes to your forked repository.
5. Create a pull request to the main repository with a description of your changes.
6. Wait for review and feedback from the maintainers.

#### Troubleshooting

- **Plugin Not Showing**: Restart the IDE and ensure the plugin is enabled.
- **Connection Issues**: The server settings say when ClickUp rejects your API token. Generate a new one and test
  the connection.
- **New tasks missing**: The IDE refreshes its task list in the background (`Settings` > `Tools` > `Tasks`,
  "Update cache every N minutes"); the `Open Task` list asks ClickUp again.
- **Time Tracking Issues**: Time tracking needs the IDE's time tracking feature: IntelliJ IDEA Ultimate up to
  2025.3, the JetBrains **Time Tracking** plugin from 2026.3 on.
- **Errors in the IDE log**: Search `idea.log` for `de.jaimerojas.clickup` and attach the lines to an issue.

For unresolved issues, create a ticket on [GitHub Issues](https://github.com/jaim3r0jas/clickup-intellij-plugin/issues).

#### License

This project is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).