# ClickUp Integration for JetBrains IDEs

A JetBrains plugin that adds ClickUp as a task server to the IDE's Tasks framework (`com.intellij.tasks`): fetch
tasks, update their status, log time, and fill commit messages through placeholders. Java only; the Kotlin Gradle
plugin is applied but there are no Kotlin sources. Published on the JetBrains Marketplace as plugin 26944.

What users see: [README.md](README.md). How to contribute: [CONTRIBUTING.md](CONTRIBUTING.md). Feature and pipeline
write-ups: [docs/](docs/) (CI in detail: [docs/WORKFLOWS.md](docs/WORKFLOWS.md); commit placeholders:
[docs/CONVENTIONAL_COMMITS.md](docs/CONVENTIONAL_COMMITS.md)).

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

- **JDK 21** to build: the IntelliJ Platform 2025.3 jars are Java 21 class files. Bytecode targets 17
  (`sourceCompatibility`/`targetCompatibility` in `build.gradle.kts`), so the plugin still runs on 2024.1. Set
  `JAVA_HOME` to a JDK 21 if your default JDK is a different one.
- Gradle through the wrapper (`./gradlew`, 8.11.1). Configuration cache and build cache are on (`gradle.properties`).
- The plugin version comes from the `PLUGIN_VERSION` environment variable (the release tag in CI) and defaults to
  `0.0.1-SNAPSHOT`. There is no version in any file.

## Commands

| What | Command | Output |
|---|---|---|
| Compile, test, package | `./gradlew build` | |
| Tests | `./gradlew test` (one class: `--tests 'de.jaimerojas.clickup.service.ClickUpTaskServiceTest'`) | `build/reports/tests/test/index.html` |
| Coverage | `./gradlew koverHtmlReport koverVerify` (verify fails below 20%) | `build/reports/kover/html/index.html` |
| Plugin zip | `./gradlew buildPlugin` | `build/distributions/clickup-intellij-plugin-<version>.zip` |
| Run in a sandbox IDE | `./gradlew runIde` (or the `Run Plugin` config in `.run/`) | log: `build/idea-sandbox/IU-2025.3/log/idea.log` |

- `runIde` starts IntelliJ IDEA 2025.3 with the plugin. It needs a desktop session: started from a terminal with no
  display (ssh, a Claude session), the IDE loads the plugin and then dies with `HeadlessException`. A Claude session
  checks the log for `Loaded custom plugins: ClickUp Integration` and leaves the UI smoke test to the user.
- `verifyPlugin` is not configured (no `pluginVerification.ides`): it fails with "No IDE resolved for verification".
- Qodana runs in CI on Qodana Cloud (`qodana.yaml`: image `jetbrains/qodana-jvm-community:2025.2`, profile
  `qodana.recommended`), with the secret `QODANA_TOKEN`. The Cloud organisation has the free Community license: no
  code coverage or license audit in Qodana, and a paid-only linter or feature fails the license check. A local
  `./gradlew qodanaScan` (Docker) does not work yet: the container can't resolve the project JDK `graalvm-21` from
  `.idea/misc.xml`. Read the Qodana result of the PR's CI run instead.

## IDE targets

- Compiles against IntelliJ IDEA 2025.3 (`intellijIdea("2025.3")`) with the bundled `com.intellij.tasks` plugin.
- `sinceBuild = 241`, no `untilBuild`: every IDE from 2024.1 on, PhpStorm, WebStorm and the rest included
  (`plugin.xml` depends only on `com.intellij.modules.platform` and `com.intellij.tasks`). An API newer than 2024.1
  breaks the older IDEs: check when it was introduced before using it.
- Time tracking needs an IDE that ships the Tasks time-tracking feature (IntelliJ IDEA Ultimate).

## Conventions

- Work for a GitHub issue: branch `feat/clickup-intellij-plugin-<issue>/<short-desc>`, commit and PR title
  `#clickup-intellij-plugin-<issue>: <issue title>`. Work without an issue: branch `chore/<short-desc>`.
- PRs go to `main` and are squash-merged (the subject gains ` (#<PR>)`). The PR checklist is in CONTRIBUTING.md.
- Every Java file starts with the Apache 2.0 license header (copy it from any file in `src/`).
- Tests use JUnit 5 and Mockito (`@ExtendWith(MockitoExtension.class)`), grouped with `@Nested` and named with
  `@DisplayName`. Mock `ClickUpApiClient` or `ClickUpTaskService`. Tests never call the real ClickUp API.
- New UI text goes into `ClickUpBundle.properties`, not into string literals.

## CI

- `.github/workflows/build.yaml` runs on every PR to `main` and every push to it:
  1. `./gradlew build test koverXmlReport koverHtmlReport`
  2. `koverVerify`, which doesn't fail the job (`continue-on-error`)
  3. a coverage comment on the PR and a Codecov upload (targets in `codecov.yml`)
  4. then the Qodana scan
- `.github/workflows/publish.yaml` runs on a tag `X.Y.Z` that is on `main`: tests with a blocking `koverVerify`,
  Qodana, then signs the plugin, publishes it to the Marketplace and creates the GitHub release. Pushing a tag
  releases: only when the user asks.

## Secrets

Signing and publishing use `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD` and `PUBLISH_TOKEN`. They exist
only as GitHub Actions secrets. A checkout may have a git-ignored `keys/` folder with the signing keys: never read,
print, copy or commit anything from it, and never run `signPlugin` or `publishPlugin` locally.

## Before a change lands

1. `./gradlew build koverVerify` passes on JDK 21.
2. New behaviour has tests.
3. A change to the UI or Tasks behaviour was tried in `./gradlew runIde` (by the user, from a desktop session).
4. The PR's CI is green, Qodana included.
