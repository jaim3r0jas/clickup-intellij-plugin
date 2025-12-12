# Quick Start: Conventional Commits with ClickUp Plugin

## Setup (One-time)

1. Open **Settings** → **Tools** → **Tasks**
2. In the **Commit Message** section, enter your template using placeholders

## Recommended Templates

### Simple
```
{type}: {description}
```

### With Scope
```
{type}({scope}): {description}

{footer}
```

### Full Format
```
{type}({scope}): {description}

{body}

{footer}
```

## Usage Workflow

1. **Select a ClickUp task** in the Tasks tool window
2. **Make your code changes**
3. **Open the commit dialog** (Cmd+K on macOS, Ctrl+K on Windows/Linux)
4. **Review the auto-generated commit message** from your template
5. **Adjust if needed** and commit

## Examples

### Feature Task
- Task: "Add User Authentication"
- Type: FEATURE
- Project: "Auth Module"

**Generated:**
```
feat(auth-module): add user authentication

Refs: CU-123abc
```

### Bug Fix Task
- Task: "Fix Login Timeout"
- Type: BUG
- Project: "Authentication"

**Generated:**
```
fix(authentication): fix login timeout

Refs: CU-456def
```

## Tips

✅ Keep task summaries clear and concise
✅ Use meaningful project names in ClickUp
✅ Let the plugin handle type mapping automatically
✅ Review and adjust commit messages as needed

## All Available Placeholders

| Placeholder | Output Example |
|------------|----------------|
| `{id}` | `CU-123abc` |
| `{type}` | `feat`, `fix`, `chore` |
| `{scope}` | `auth`, `api` |
| `{description}` | `add user authentication` |
| `{summary}` | `Add User Authentication` |
| `{body}` | Task description text |
| `{footer}` | `Refs: CU-123abc` |
| `{project}` | `Authentication` |

For more details, see [docs/CONVENTIONAL_COMMITS.md](CONVENTIONAL_COMMITS.md)

