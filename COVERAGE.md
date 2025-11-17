# Code Coverage Setup

This project uses JaCoCo for code coverage reporting integrated with GitHub Actions.

## Overview

The project has three GitHub Actions workflows that handle code coverage:

1. **build.yaml** - Runs on all pushes and PRs, includes coverage reporting
2. **coverage.yaml** - Dedicated coverage workflow with enhanced reporting
3. **publish.yaml** - Handles plugin publishing (no coverage)

## JaCoCo Configuration

JaCoCo is configured in `build.gradle.kts` with the following features:

- **XML Report**: Required for GitHub Actions and Codecov integration
- **CSV Report**: Required for badge generation
- **HTML Report**: Human-readable coverage report
- **Coverage Verification**: Enforces minimum coverage thresholds

### Exclusions

The following classes are excluded from coverage reporting:
- `ClickUpBundle` - Resource bundle class
- `ClickUpTaskIconHolder` - Icon holder class

These are excluded because they are typically auto-generated or contain only static resources.

## Coverage Thresholds

Current minimum coverage requirements:

- **Overall Project**: 40%
- **Individual Classes**: 30%
- **Changed Files (PR)**: 60%

These thresholds can be adjusted in:
- `build.gradle.kts` - For local verification
- `.github/workflows/*.yaml` - For CI/CD checks

## Running Coverage Locally

### Generate Coverage Report

```bash
./gradlew test jacocoTestReport
```

The reports will be generated in:
- **XML**: `build/reports/jacoco/test/jacocoTestReport.xml`
- **CSV**: `build/reports/jacoco/test/jacocoTestReport.csv`
- **HTML**: `build/reports/jacoco/test/html/index.html`

### Verify Coverage Thresholds

```bash
./gradlew jacocoTestCoverageVerification
```

This will fail if the coverage is below the configured thresholds.

### View HTML Report

Open the HTML report in your browser:

```bash
open build/reports/jacoco/test/html/index.html
```

Or on Linux:

```bash
xdg-open build/reports/jacoco/test/html/index.html
```

## GitHub Actions Integration

### Automatic Coverage Comments on PRs

When you create a Pull Request, the workflow will automatically:
1. Run all tests
2. Generate coverage reports
3. Post a comment on the PR with coverage details
4. Show coverage for overall project and changed files
5. Highlight if coverage drops below thresholds

### Coverage Artifacts

All workflow runs upload coverage reports as artifacts that can be downloaded for 30 days:
- **test-results**: JUnit test results
- **jacoco-reports**: HTML and XML coverage reports

### Codecov Integration (Optional)

The workflows include Codecov integration. To enable it:

1. Sign up at [codecov.io](https://codecov.io)
2. Add your repository
3. Get your Codecov token
4. Add it as a GitHub secret: `CODECOV_TOKEN`

The badge in README.md will automatically update with coverage percentage.

## Viewing Coverage Reports

### On GitHub

1. Go to **Actions** tab in your repository
2. Click on a workflow run
3. Scroll down to **Artifacts**
4. Download `jacoco-reports` to view HTML reports locally

### In Pull Requests

Coverage details are automatically posted as a comment on PRs, showing:
- Overall project coverage
- Coverage for changed files
- Line-by-line coverage changes

### On Codecov (if configured)

Visit: `https://codecov.io/gh/YOUR_USERNAME/clickup-intellij-plugin`

## Troubleshooting

### Coverage report is empty

Make sure tests are actually running:
```bash
./gradlew clean test --info
```

### Coverage verification fails locally

Check which classes are below threshold:
```bash
./gradlew jacocoTestCoverageVerification --info
```

### GitHub Actions workflow fails

1. Check the workflow logs in the Actions tab
2. Ensure all secrets are properly configured
3. Verify the paths in workflow files match your project structure

## Customization

### Adjust Coverage Thresholds

Edit `build.gradle.kts`:

```kotlin
jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal() // Change to 80%
            }
        }
    }
}
```

### Add More Exclusions

Edit the `jacocoTestReport` task in `build.gradle.kts`:

```kotlin
classDirectories.setFrom(
    files(classDirectories.files.map {
        fileTree(it) {
            exclude(
                "**/ClickUpBundle.class",
                "**/YourClassToExclude.class"
            )
        }
    })
)
```

### Modify PR Comment Settings

Edit `.github/workflows/build.yaml` or `.github/workflows/coverage.yaml`:

```yaml
- name: Add coverage to PR
  uses: madrapps/jacoco-report@v1.6.1
  with:
    min-coverage-overall: 60  # Change thresholds
    min-coverage-changed-files: 80
```

## Best Practices

1. **Write tests first**: Aim for good test coverage before merging
2. **Review coverage in PRs**: Check the automated comment
3. **Don't obsess over 100%**: Focus on testing critical paths
4. **Exclude generated code**: Keep exclusion list updated
5. **Local testing**: Run coverage locally before pushing

## Resources

- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [Codecov Documentation](https://docs.codecov.com/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)

