# The JetBrains Marketplace page

The plugin's page: https://plugins.jetbrains.com/plugin/26944-clickup-integration

## Where each part comes from

| Part of the page | Source | Updated |
|---|---|---|
| Name, vendor | `src/main/resources/META-INF/plugin.xml` | with each release |
| Description (Overview tab) | `<description>` in `plugin.xml` | with each release |
| What's New (change notes) | `CHANGELOG.md`: `[Unreleased]` at release time, rendered by `patchPluginXml` | with each release |
| Compatibility (versions) | `sinceBuild` in `build.gradle.kts`, no `untilBuild` | with each release |
| Screenshots, video, tags, links | the Marketplace web UI: plugin page > Edit | by hand, any time |

## Releasing

1. Make sure `CHANGELOG.md`'s `[Unreleased]` says what users get, under Added, Changed, Fixed or Removed.
2. Tag `main` with the version (`0.4.0`, no `v`) and push the tag. The publish workflow verifies, signs, publishes to
   the Marketplace, creates the GitHub release with the `[Unreleased]` notes, and opens a pull request that moves
   them under the new version (made from the tagged commit). Merge that pull request; if `CHANGELOG.md` changed on
   `main` since the tag, resolve the conflict by keeping the newer lines under `[Unreleased]`.

That pull request needs "Allow GitHub Actions to create and approve pull requests" on (repository Settings > Actions
> General > Workflow permissions). A pull request opened by the workflow's token doesn't start the CI workflow by
itself: close and reopen it to run CI if branch protection asks for it.

## Screenshots

The Marketplace shows them in order, at their own size (1280×800 or larger, PNG). Take them in a light theme with a
real but harmless workspace (no customer names), at 100% scale:

1. **Open Task with ClickUp tasks**: `Tools | Tasks & Contexts | Open Task` showing a list of ClickUp tasks with
   their icons and IDs (custom IDs on, like `DEV-123`).
2. **The server settings**: `Settings | Tools | Tasks | Servers` with the ClickUp server: the token field with the
   Generate token link, a workspace and an assignee picked.
3. **Opening a task**: the Open Task dialog for a ClickUp task, with the status update to a ClickUp status and a
   new branch.
4. **A commit message from the task**: the commit dialog with a Conventional Commits message filled by the
   placeholders (for a Bug: `fix(scope): …` with the `Refs` footer), and the template in `Settings | Tools | Tasks`
   beside it or as a fifth shot.
5. Optional: **time tracking**, the Work Time dialog posting time to ClickUp.
