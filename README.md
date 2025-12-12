[![Build](https://github.com/jaim3r0jas/clickup-intellij-plugin/actions/workflows/build.yaml/badge.svg)](https://github.com/jaim3r0jas/clickup-intellij-plugin/actions/workflows/build.yaml)
[![codecov](https://codecov.io/gh/jaim3r0jas/clickup-intellij-plugin/graph/badge.svg?token=43VL7NXZTL)](https://codecov.io/gh/jaim3r0jas/clickup-intellij-plugin)

### IntelliJ IDEA Tasks Integration for ClickUp

#### Description

Integrate ClickUp with JetBrains IDEs to manage tasks and track time directly within your development environment.

[Plugin on JetBrains Marketplace](https://plugins.jetbrains.com/plugin/26944-clickup-integration)

#### Features

- Update ClickUp task status.
- Automatically create feature branches and change lists.
- Set commit message templates.
- Log time entries to tasks.
- **Conventional Commits support** - Use placeholders to compose commit messages following the [Conventional Commits](https://www.conventionalcommits.org/) specification.

#### Installation

1. Open your JetBrains IDE.
2. Go to `File` > `Settings` (or `Preferences` on macOS) > `Plugins`.
3. Search for `ClickUp Integration` in the `Marketplace` tab and install.
4. Restart the IDE.

#### Configuration

1. Go to `File` > `Settings` > `Tools` > `Tasks` > `Servers`.
2. Add a new server, select `ClickUp`, and enter your API token.
3. Test the connection and save.

#### Usage

1. Open the `Tasks` tool window.
2. Manage ClickUp tasks and log time directly from the IDE.

#### Development Setup

1. Clone the repository:
   ```sh
   git clone https://github.com/rojas-safenow/clickup-integration.git
   cd clickup-integration
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
- **Connection Issues**: Verify your API token and test the connection.
- **Tasks Not Syncing**: Check your internet connection and ClickUp server status.
- **Time Tracking Issues**: Ensure you are using IntelliJ IDEA Ultimate.

For unresolved issues, create a ticket on [GitHub Issues](https://github.com/rojas-safenow/clickup-integration/issues).

#### License

This project is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).