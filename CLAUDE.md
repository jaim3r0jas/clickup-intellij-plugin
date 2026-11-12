# ClickUp Integration for JetBrains IDEs

A JetBrains plugin that adds ClickUp as a task server to the IDE's Tasks framework (`com.intellij.tasks`): fetch
tasks, update their status, log time, and fill commit messages through placeholders. Java only: the Kotlin Gradle
plugin is applied only because Kover needs it to measure coverage (without it the report is empty). Published on the
JetBrains Marketplace as plugin 26944.

What users see: [README.md](README.md). How to contribute: [CONTRIBUTING.md](CONTRIBUTING.md). Feature and pipeline
write-ups: [docs/](docs/) (CI in detail: [docs/WORKFLOWS.md](docs/WORKFLOWS.md); commit placeholders:
[docs/CONVENTIONAL_COMMITS.md](docs/CONVENTIONAL_COMMITS.md); the Marketplace page and releasing:
[docs/MARKETPLACE.md](docs/MARKETPLACE.md)).

## Layout

- `src/main/java/de/jaimerojas/clickup/`
  - `ClickUpRepository` (the task server, extends `NewBaseRepositoryImpl`), `ClickUpRepositoryType` and
    `ClickUpRepositoryEditor` (its settings form): the glue to the Tasks API.
  - `api/`: `ClickUpApiClient` (interface) and `ClickUpApiClientImpl`, the HTTP calls to the ClickUp API (Gson).
  - `service/ClickUpTaskService`: the logic between the repository and the client.
  - `model/`: Gson DTOs for the ClickUp API, plus `ClickUpTaskIconHolder`.
  - `extensions/ConventionalCommitPlaceHolderProvider`: the commit message placeholders.
- `src/main/resources/META-INF/plugin.xml`: the plugin's id, dependencies and extensions. UI strings live in
  `messages/ClickUpBundle.properties` (read through `ClickUpBundle`).
- `src/test/java/`: mirrors the main packages.
- Versions of Gradle plugins and libraries: `gradle/libs.versions.toml`. Build logic: `build.gradle.kts`.

## Toolchain

- **JDK 21** to build. The bytecode targets Java 21 (`options.release = 21` in `build.gradle.kts`), which every IDE
  from 2024.2 on runs; `sinceBuild` is 243 (see IDE targets). Set `JAVA_HOME` to a JDK 21 if your default JDK is a
  different one.
- Gradle through the wrapper (`./gradlew`, 9.9.1). Configuration cache and build cache are on (`gradle.properties`).
- The plugin version comes from the `PLUGIN_VERSION` environment variable (the release tag in CI) and defaults to
  `0.0.1-SNAPSHOT`. There is no version in any file.

## Commands

| What | Command | Output |
|---|---|---|
| Compile, test, package | `./gradlew build` | |
| Tests | `./gradlew test` (one class: `--tests 'de.jaimerojas.clickup.service.ClickUpTaskServiceTest'`) | `build/reports/tests/test/index.html` |
| Coverage | `./gradlew koverHtmlReport koverVerify` (verify fails below 20%) | `build/reports/kover/html/index.html` |
| Plugin zip | `./gradlew buildPlugin` | `build/distributions/clickup-intellij-plugin-<version>.zip` |
| Run in a sandbox IDE | `./gradlew runIde` (or the `Run Plugin` config in `.run/`) | log: `.intellijPlatform/sandbox/clickup-intellij-plugin/IU-2026.3/log_runIde/idea.log` |

- `runIde` starts IntelliJ IDEA 2026.3 with the plugin. It needs a desktop session: started from a terminal with no
  display (ssh, a Claude session), the IDE loads the plugin and then dies with "Unable to detect graphics
  environment". A Claude session
  checks the log for `Loaded custom plugins: ClickUp Integration` and leaves the UI smoke test to the user.
- `./gradlew verifyPlugin` runs the IntelliJ Plugin Verifier against IntelliJ IDEA and PhpStorm, each at the oldest
  (2024.3) and the newest (`platformVersion`) supported release (`intellijPlatform.pluginVerification` in
  `build.gradle.kts`). It downloads each IDE once (about 1.5 GB each) and is not part of CI. Report:
  `build/reports/pluginVerifier/`.
- Qodana runs in CI on Qodana Cloud (`qodana.yaml`: image `jetbrains/qodana-jvm-community:2026.2`, profile
  `qodana.recommended`), with the secret `QODANA_TOKEN`. The Cloud organisation has the free Community license: no
  code coverage or license audit in Qodana, and a paid-only linter or feature fails the license check. A local
  `./gradlew qodanaScan` (Docker) does not work yet: the container can't resolve the project JDK `graalvm-21` from
  `.idea/misc.xml`. Read the Qodana result of the PR's CI run instead.

## IDE targets

- Compiles against the newest stable IntelliJ IDEA (`platformVersion` in `build.gradle.kts`, now 2026.3). A new IDE
  release: bump `platformVersion` and run `verifyPlugin`.
- The Tasks API changed home in 2026.3. Up to 2025.3 it is the bundled plugin `com.intellij.tasks`. From 2026.3 on,
  its core (`Task`, `TaskRepositoryType`) is the bundled `com.intellij.platform.tasks`, and the rest (the repository
  base classes, the editor, the commit placeholders) is "Issue Trackers", still `com.intellij.tasks`, a Marketplace
  plugin the IDE installs with ours. The build takes both (`bundledPlugin` + `compatiblePlugin`). `plugin.xml` keeps
  `<depends>com.intellij.tasks</depends>`, which works on every supported IDE.
- `sinceBuild = 243` (`pluginSinceBuild`), no `untilBuild`: every IDE from 2024.3 on, PhpStorm, WebStorm and the rest
  included (`plugin.xml` depends only on `com.intellij.modules.platform` and `com.intellij.tasks`). An API newer than
  2024.3 breaks the older IDEs: check when it was introduced before using it (`verifyPlugin` catches it).
- Time tracking needs the Tasks time-tracking feature: bundled in IntelliJ IDEA Ultimate up to 2025.3, the JetBrains
  "Time Tracking" Marketplace plugin (`com.intellij.tasks.timeTracking`) from 2026.3 on.

## Conventions

- Work for a GitHub issue: branch `feat/clickup-intellij-plugin-<issue>/<short-desc>`, commit and PR title
  `#clickup-intellij-plugin-<issue>: <issue title>`. Work without an issue: branch `chore/<short-desc>`.
- PRs go to `main` and are squash-merged (the subject gains ` (#<PR>)`). The PR checklist is in CONTRIBUTING.md.
- Every Java file starts with the Apache 2.0 license header (copy it from any file in `src/`).
- Tests use JUnit Jupiter (JUnit 6) and Mockito (`@ExtendWith(MockitoExtension.class)`), grouped with `@Nested`
  and named with `@DisplayName`. Mock `ClickUpApiClient` or `ClickUpTaskService`. Tests never call the real ClickUp
  API.
- New UI text goes into `ClickUpBundle.properties`, not into string literals.

## CI

- `.github/workflows/build.yaml` runs on every PR to `main` and every push to it:
  1. `./gradlew build test koverXmlReport koverHtmlReport`
  2. `koverVerify`, which doesn't fail the job (`continue-on-error`)
  3. a coverage comment on the PR and a Codecov upload (targets in `codecov.yml`)
  4. then the Qodana scan
- `.github/workflows/publish.yaml` runs on a tag `X.Y.Z` that is on `main`: tests with a blocking `koverVerify`,
  Qodana, then signs the plugin, publishes it to the Marketplace, creates the GitHub release with `CHANGELOG.md`'s
  `[Unreleased]` notes and opens a pull request that moves them under the version. Pushing a tag releases: only
  when the user asks.
- A user-visible change adds a line to `CHANGELOG.md` under `[Unreleased]` (Added, Changed, Fixed, Removed): it
  becomes the Marketplace's change notes.

## Secrets

Signing and publishing use `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD` and `PUBLISH_TOKEN`. They exist
only as GitHub Actions secrets. A checkout may have a git-ignored `keys/` folder with the signing keys: never read,
print, copy or commit anything from it, and never run `signPlugin` or `publishPlugin` locally.

## Before a change lands

1. `./gradlew build koverVerify` passes on JDK 21.
2. New behaviour has tests.
3. A change to the UI or Tasks behaviour was tried in `./gradlew runIde` (by the user, from a desktop session).
4. The PR's CI is green, Qodana included.
