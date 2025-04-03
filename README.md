### IntelliJ IDEA Tasks integration for ClickUp

#### Description

Enhance your productivity by integrating ClickUp with JetBrains IDEs. Manage your tasks seamlessly and track time spent
on each task within your development environment.

#### Features

- Easily update ClickUp tasks status.
- Automatically create a feature branch.
- Automatically create a change list.
- Set up a commit message template.
- Post time tracking entries directly to the active task or issue.

All of these from the comfort of your IDE.

For now, the plugin has limited functionality, but it is a work in progress. This plugin is an open source project and
is not officially supported by ClickUp, so contributions and feedback are equally welcome.

The logo is a registered trademark of ClickUp.

#### Setup Instructions

##### Installation

1. Open your JetBrains IDE (IntelliJ IDEA, PyCharm, WebStorm, etc.).
2. Navigate to `File` > `Settings` (or `Preferences` on macOS).
3. In the left-hand pane, select `Plugins`.
4. Click on the `Marketplace` tab.
5. In the search bar, type `ClickUp Integration`.
6. Locate the `ClickUp Integration` plugin and click `Install`.
7. Restart your IDE to activate the plugin.

##### Configuration

1. After restarting the IDE, go to `File` > `Settings` (or `Preferences` on macOS).
2. In the left-hand pane, select `Tools` > `Tasks`.
3. Click on the `Servers` tab.
4. Click the `+` button to add a new task server.
5. Select `ClickUp` from the list of available servers.
6. Enter your ClickUp API token and other required information.
7. Click `Test` to ensure the connection is successful.
8. Click `OK` to save the configuration.

##### Using the Plugin

1. Open the `Tasks` tool window from the bottom of the IDE.
2. You can now create, view, and manage your ClickUp tasks directly from the IDE.
3. Use the `Time Tracking` feature to log time spent on tasks (available in IntelliJ IDEA Ultimate).

#### Troubleshooting

##### Common Issues

1. **Plugin Not Showing Up After Installation**
    - Ensure that you have restarted your IDE after installing the plugin.
    - Verify that the plugin is enabled in `File` > `Settings` (or `Preferences` on macOS) > `Plugins`.

2. **Unable to Connect to ClickUp**
    - Double-check your ClickUp API token and ensure it is correctly entered.
    - Test the connection in `File` > `Settings` (or `Preferences` on macOS) > `Tools` > `Tasks` > `Servers`.

3. **Tasks Not Syncing**
    - Ensure that your internet connection is stable.
    - Verify that the ClickUp server is not experiencing downtime.

4. **Time Tracking Not Working**
    - Time tracking is only available in IntelliJ IDEA Ultimate. Ensure you are using the correct version of the IDE.
    - Check if the Time Tracking bundle is installed and enabled.

##### Getting Help

If you encounter any issues not listed above, please create an issue on
the [GitHub Issues](https://github.com/rojas-safenow/clickup-integration/issues) page. Provide detailed information
about the problem, including steps to reproduce, expected behavior, and any error messages.

#### Setting Up Local Environment

To set up the local development environment for this project, follow these steps:

1. **Clone the Repository**
   ```sh
   git clone https://github.com/rojas-safenow/clickup-integration.git
   cd clickup-integration