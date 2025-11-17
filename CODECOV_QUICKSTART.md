# Quick Setup Checklist for Codecov

## 🚀 5-Minute Setup

### 1️⃣ Sign Up for Codecov
- Go to: https://about.codecov.io/
- Click "Sign up with GitHub"
- Authorize Codecov

### 2️⃣ Add Your Repository
- Navigate to: https://app.codecov.io/gh/jaim3r0jas/clickup-intellij-plugin
- OR click "Add new repository" in Codecov dashboard
- Click "Set up repo"

### 3️⃣ Get Your Token
- In Codecov, go to your repo → Settings
- Copy the "Upload Token" (looks like: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`)

### 4️⃣ Add Token to GitHub
- Go to: https://github.com/jaim3r0jas/clickup-intellij-plugin/settings/secrets/actions
- Click "New repository secret"
- Name: `CODECOV_TOKEN`
- Value: [Paste your token]
- Click "Add secret"

### 5️⃣ Test It
```bash
# Push your changes to trigger the workflow
git add .
git commit -m "Add JaCoCo and Codecov configuration"
git push origin main
```

### 6️⃣ Verify
- Check GitHub Actions: https://github.com/jaim3r0jas/clickup-intellij-plugin/actions
- Check Codecov: https://app.codecov.io/gh/jaim3r0jas/clickup-intellij-plugin
- Badge in README will update automatically

## ✅ What's Already Done

Your project is already configured with:
- ✅ JaCoCo plugin in `build.gradle.kts`
- ✅ Coverage reports (XML, CSV, HTML)
- ✅ GitHub Actions workflows (`build.yaml`, `coverage.yaml`)
- ✅ Codecov badge in README.md
- ✅ Coverage thresholds (40% overall, 60% for changes)
- ✅ PR comments for coverage reports

## 📊 What You'll Get

Once setup is complete:
- 📈 Coverage percentage badge in README
- 💬 Automatic PR comments with coverage details
- 📉 Coverage trends over time
- 🎯 Coverage diff for each commit
- 🔍 File-by-file coverage breakdown
- ⚠️ Alerts when coverage drops

## 🔗 Important Links

- **Repository:** https://github.com/jaim3r0jas/clickup-intellij-plugin
- **GitHub Secrets:** https://github.com/jaim3r0jas/clickup-intellij-plugin/settings/secrets/actions
- **GitHub Actions:** https://github.com/jaim3r0jas/clickup-intellij-plugin/actions
- **Codecov Dashboard:** https://app.codecov.io/gh/jaim3r0jas/clickup-intellij-plugin

## 💡 Pro Tips

1. **First Upload Takes Time:** Badge may show "unknown" for a few minutes
2. **Token is Optional:** For public repos, Codecov works without a token, but having one is recommended
3. **Install GitHub App:** For better integration, install the [Codecov GitHub App](https://github.com/apps/codecov)
4. **Check Logs:** If something fails, check the GitHub Actions logs first

## 🆘 Common Issues

| Issue | Solution |
|-------|----------|
| Badge shows "unknown" | Wait a few minutes, then refresh browser |
| Upload fails | Check token in GitHub secrets |
| 0% coverage shown | Verify tests are running: `./gradlew test` |
| No PR comments | Ensure PR is from a branch in the same repo |

## 📖 Full Documentation

For detailed instructions, see: `CODECOV_SETUP.md`

---

**Total Setup Time:** ~5 minutes ⏱️
**Difficulty:** Easy 🟢

