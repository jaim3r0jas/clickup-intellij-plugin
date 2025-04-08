### IntelliJ IDEA Tasks Integration for ClickUp

#### Description

Integrate ClickUp with JetBrains IDEs to manage tasks and track time directly within your development environment.

[Plugin on JetBrains Marketplace](https://plugins.jetbrains.com/plugin/26944-clickup-integration)

#### Features

- Update ClickUp task status.
- Automatically create feature branches and change lists.
- Set commit message templates.
- Log time entries to tasks.

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

#### Troubleshooting

- **Plugin Not Showing**: Restart the IDE and ensure the plugin is enabled.
- **Connection Issues**: Verify your API token and test the connection.
- **Tasks Not Syncing**: Check your internet connection and ClickUp server status.
- **Time Tracking Issues**: Ensure you are using IntelliJ IDEA Ultimate.

For unresolved issues, create a ticket on [GitHub Issues](https://github.com/rojas-safenow/clickup-integration/issues).

#### Development Setup

1. Clone the repository:
   ```sh
   git clone https://github.com/rojas-safenow/clickup-integration.git
   cd clickup-integration