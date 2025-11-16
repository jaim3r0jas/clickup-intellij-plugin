# Codecov Setup Guide

This guide will walk you through setting up Codecov for your ClickUp IntelliJ Plugin project.

## Prerequisites

- GitHub account with access to the repository `jaim3r0jas/clickup-intellij-plugin`
- Admin access to the repository to add secrets

## Step 1: Sign Up for Codecov

1. Go to [https://about.codecov.io/](https://about.codecov.io/)
2. Click **"Sign Up"** or **"Get Started"**
3. Choose **"Sign up with GitHub"**
4. Authorize Codecov to access your GitHub account

## Step 2: Add Your Repository

### Option A: Through Codecov Dashboard

1. Once logged in, you'll see the Codecov dashboard
2. Click **"Add new repository"** or the **"+"** button
3. Search for `clickup-intellij-plugin` in the list
4. Click **"Set up repo"** next to your repository

### Option B: Direct URL

Navigate directly to:
```
https://app.codecov.io/gh/jaim3r0jas/clickup-intellij-plugin
```

## Step 3: Get Your Codecov Token

1. In the Codecov dashboard, select your repository
2. Go to **Settings** (gear icon)
3. Navigate to **"General"** or **"Upload Token"** section
4. Copy the **Codecov upload token** (it looks like: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`)

**Important:** Keep this token secure! It's like a password.

## Step 4: Add Token to GitHub Secrets

1. Go to your GitHub repository: `https://github.com/jaim3r0jas/clickup-intellij-plugin`
2. Click **"Settings"** (at the top of the repository page)
3. In the left sidebar, click **"Secrets and variables"** → **"Actions"**
4. Click **"New repository secret"**
5. Add the following secret:
   - **Name:** `CODECOV_TOKEN`
   - **Value:** [Paste the token you copied from Codecov]
6. Click **"Add secret"**

## Step 5: Configure Codecov (Optional)

Create a `codecov.yml` file in the root of your repository to customize Codecov behavior:

```yaml
# codecov.yml
codecov:
  require_ci_to_pass: yes
  notify:
    wait_for_ci: yes

coverage:
  precision: 2
  round: down
  range: "70...100"
  status:
    project:
      default:
        target: 40%
        threshold: 5%
        if_ci_failed: error
    patch:
      default:
        target: 60%
        threshold: 10%
        if_ci_failed: error

comment:
  layout: "reach,diff,flags,tree,files"
  behavior: default
  require_changes: false
  require_base: false
  require_head: true

ignore:
  - "src/test/**/*"
  - "build/**/*"
  - "**/ClickUpBundle.java"
  - "**/ClickUpTaskIconHolder.java"
```

## Step 6: Test the Setup

### Method 1: Push to Main Branch

1. Commit and push your changes to the `main` branch:
   ```bash
   git add .
   git commit -m "Add JaCoCo and Codecov configuration"
   git push origin main
   ```

2. Go to the **Actions** tab in GitHub
3. Watch the "Code Coverage" workflow run
4. Once complete, check Codecov dashboard for the report

### Method 2: Create a Pull Request

1. Create a new branch and push changes:
   ```bash
   git checkout -b test-codecov
   git add .
   git commit -m "Test Codecov integration"
   git push origin test-codecov
   ```

2. Create a Pull Request on GitHub
3. The workflow will run and post coverage reports
4. Check both the PR comments and Codecov dashboard

## Step 7: Verify Coverage Reports

### On GitHub

1. Go to the **Actions** tab
2. Click on the latest "Code Coverage" workflow run
3. Check that all steps completed successfully
4. Look for the "Upload coverage reports to Codecov" step

### On Codecov

1. Go to `https://app.codecov.io/gh/jaim3r0jas/clickup-intellij-plugin`
2. You should see:
   - Coverage percentage
   - Coverage graph over time
   - File-by-file coverage breakdown
   - Commit history with coverage changes

### On Pull Requests

When you create a PR, Codecov will:
1. Post a comment with coverage details
2. Add a status check showing coverage changes
3. Show coverage diff for changed files

## Codecov Badge

The README.md already includes a Codecov badge:

```markdown
[![codecov](https://codecov.io/gh/jaim3r0jas/clickup-intellij-plugin/branch/main/graph/badge.svg)](https://codecov.io/gh/jaim3r0jas/clickup-intellij-plugin)
```

Once you push code and Codecov processes it, the badge will automatically display the current coverage percentage.

## Troubleshooting

### Token Issues

**Problem:** "Could not upload coverage reports to Codecov"

**Solutions:**
1. Verify the `CODECOV_TOKEN` secret is correctly set in GitHub
2. Check that the token hasn't expired
3. Make sure you copied the full token without spaces
4. Try regenerating the token in Codecov settings

### Report Upload Fails

**Problem:** Coverage report not showing in Codecov

**Solutions:**
1. Check the GitHub Actions logs for errors
2. Verify the file path in the workflow: `./build/reports/jacoco/test/jacocoTestReport.xml`
3. Ensure tests actually ran (check test results)
4. Confirm the XML report was generated locally:
   ```bash
   ./gradlew clean test jacocoTestReport
   ls -la build/reports/jacoco/test/
   ```

### Coverage Shows 0%

**Problem:** Codecov shows 0% coverage

**Solutions:**
1. Make sure tests are writing to the coverage report
2. Check JaCoCo configuration in `build.gradle.kts`
3. Verify test classes exist in `src/test/java/`
4. Run locally: `./gradlew test jacocoTestReport` and check the HTML report

### Badge Not Updating

**Problem:** README badge shows "unknown" or doesn't update

**Solutions:**
1. Wait a few minutes after the first upload (can take time to generate)
2. Clear browser cache and refresh
3. Check if Codecov has processed the report
4. Verify the badge URL matches your repository

## Advanced Configuration

### Multiple Upload Flags

You can tag different parts of your code with flags:

```yaml
- name: Upload coverage reports to Codecov
  uses: codecov/codecov-action@v4
  with:
    files: ./build/reports/jacoco/test/jacocoTestReport.xml
    flags: unittests,backend
    token: ${{ secrets.CODECOV_TOKEN }}
```

### Codecov GitHub App (Recommended)

For enhanced features, install the Codecov GitHub App:

1. Go to [https://github.com/apps/codecov](https://github.com/apps/codecov)
2. Click **"Install"**
3. Select your repository
4. This provides better integration and status checks

### Custom Coverage Thresholds

Edit `codecov.yml` to set custom thresholds per project:

```yaml
coverage:
  status:
    project:
      default:
        target: 80%  # Require 80% overall
    patch:
      default:
        target: 90%  # Require 90% on new code
```

## Useful Links

- **Codecov Dashboard:** `https://app.codecov.io/gh/jaim3r0jas/clickup-intellij-plugin`
- **Codecov Docs:** https://docs.codecov.com/docs
- **GitHub Action:** https://github.com/codecov/codecov-action
- **Badge Configuration:** https://docs.codecov.com/docs/status-badges

## Next Steps

After setup:

1. ✅ Write more tests to increase coverage
2. ✅ Monitor coverage trends in Codecov dashboard
3. ✅ Set up coverage goals for your team
4. ✅ Review coverage reports in every PR
5. ✅ Configure Codecov notifications (optional)

## Support

If you encounter issues:

1. Check the [Codecov Community](https://community.codecov.com/)
2. Review [GitHub Actions logs](https://github.com/jaim3r0jas/clickup-intellij-plugin/actions)
3. Consult the [Codecov documentation](https://docs.codecov.com/)
4. Check your repository's [Codecov settings](https://app.codecov.io/gh/jaim3r0jas/clickup-intellij-plugin/settings)

