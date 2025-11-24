# GitHub Workflows Documentation

## Overview

The CI/CD pipeline has been streamlined into two lean, efficient workflows that eliminate redundancy while providing comprehensive quality checks.

## Workflows

### 1. CI Pipeline (`build.yaml`)

**Triggers:**
- Push to `main` branch
- Pull requests to `main` branch

**Jobs:**

#### Job 1: Build, Test & Coverage
- ✅ Builds the plugin
- ✅ Runs all tests
- ✅ Generates Kover coverage reports (XML + HTML)
- ✅ Verifies coverage thresholds (40% overall, 60% changed files)
- ✅ Uploads test results as artifacts
- ✅ Uploads coverage reports as artifacts
- ✅ Uploads plugin distribution as artifact
- ✅ Posts coverage report to PR (for pull requests)
- ✅ Uploads coverage to Codecov

#### Job 2: Code Quality Analysis (Qodana)
- ✅ Runs after successful build and test
- ✅ Generates Kover coverage for Qodana integration
- ✅ Performs comprehensive code quality analysis
- ✅ Integrates coverage data with quality metrics
- ✅ Posts results to Qodana Cloud

**Artifacts Generated:**
- `test-results` - JUnit test reports (30 days retention)
- `kover-reports` - Coverage reports in HTML and XML (30 days retention)
- `plugin-artifact` - Built plugin distribution (7 days retention)

---

### 2. Publish Plugin (`publish.yaml`)

**Triggers:**
- Push of version tags matching pattern `*.*.*` (e.g., `1.0.0`, `2.1.3`)

**Jobs:**

#### Job 1: Verify, Build & Test
- ✅ Verifies tag is based on `main` branch
- ✅ Runs full test suite with coverage
- ✅ Verifies coverage thresholds
- ✅ Uploads test and coverage artifacts

#### Job 2: Quality Gate (Qodana)
- ✅ Runs after successful tests
- ✅ Performs code quality analysis
- ✅ Must pass before publishing

#### Job 3: Publish to JetBrains Marketplace
- ✅ Runs only after tests and quality checks pass
- ✅ Patches plugin.xml with version from tag
- ✅ Signs the plugin with private key
- ✅ Publishes to JetBrains Marketplace
- ✅ Creates GitHub Release with auto-generated notes
- ✅ Attaches plugin distribution to release

**Tag Verification:**
The publish workflow ensures that only tags based on the `main` branch can trigger a release, preventing accidental releases from feature branches.

---

## Quality Gates

### Coverage Requirements
- **Overall Coverage:** Minimum 40%
- **Changed Files:** Minimum 60%
- Violations are reported but don't fail the build (continue-on-error)

### Code Quality (Qodana)
- Runs on every push and PR
- Integrated with Kover coverage data
- Results available on Qodana Cloud

---

## Secrets Required

### CI Pipeline
- `CODECOV_TOKEN` - For uploading coverage to Codecov
- `QODANA_TOKEN_576555964` - For Qodana Cloud integration

### Publish Pipeline
- `CERTIFICATE_CHAIN` - Plugin signing certificate chain
- `PRIVATE_KEY` - Plugin signing private key
- `PRIVATE_KEY_PASSWORD` - Password for private key
- `PUBLISH_TOKEN` - JetBrains Marketplace API token
- `GITHUB_TOKEN` - Automatically provided by GitHub Actions

---

## Workflow Optimization

### Eliminated Redundancies
Previously, there were 4 separate workflows:
1. ❌ `build.yaml` - Build and test
2. ❌ `coverage.yaml` - Coverage reporting (duplicate)
3. ❌ `qodana_code_quality.yml` - Code quality
4. ✅ `publish.yaml` - Publishing

**Now consolidated to 2 workflows:**
1. ✅ `build.yaml` - CI Pipeline (build, test, coverage, quality)
2. ✅ `publish.yaml` - Publish Pipeline (with quality gates)

### Benefits
- **Faster CI runs** - No duplicate test executions
- **Better resource usage** - Shared artifacts between jobs
- **Clear dependencies** - Quality checks depend on successful tests
- **Fail-fast behavior** - Publishing only happens after all checks pass
- **Single source of truth** - One workflow for CI, one for publishing

---

## Testing Locally

Before pushing, you can test the pipeline steps locally:

```bash
# Run full build with tests and coverage
./gradlew build test koverXmlReport koverHtmlReport

# Verify coverage thresholds
./gradlew koverVerify

# Build plugin distribution
./gradlew buildPlugin

# Patch plugin.xml with version
PLUGIN_VERSION=1.0.0 ./gradlew patchPluginXml
```

---

## Monitoring

### CI Pipeline Status
- Check the **Actions** tab in GitHub
- View coverage trends in Codecov dashboard
- Review quality metrics in Qodana Cloud

### Publishing Status
- Verify GitHub Release was created
- Check JetBrains Marketplace for plugin availability
- Review plugin download statistics

---

## Troubleshooting

### Tests Fail
- Check `test-results` artifact for detailed failure reports
- Review logs in GitHub Actions

### Coverage Below Threshold
- Check `kover-reports` artifact for coverage details
- Review PR comment for specific file coverage
- Visit Codecov dashboard for trends

### Qodana Issues
- Review Qodana report in the Actions summary
- Visit Qodana Cloud for detailed analysis
- Check for new code quality issues

### Publishing Fails
- Ensure all secrets are configured
- Verify tag is on `main` branch
- Check that tests and quality gates passed
- Review JetBrains Marketplace API errors in logs

---

## Future Improvements

Potential enhancements:
- Add performance benchmarking
- Implement automatic version bumping
- Add security scanning (Snyk, Dependabot)
- Create preview releases for testing
- Add notification integrations (Slack, Discord)

