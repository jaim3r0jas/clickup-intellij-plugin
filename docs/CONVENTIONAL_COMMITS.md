# Conventional Commits Integration

The ClickUp Integration plugin provides support for [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) through commit message placeholders.

## Overview

Conventional Commits is a specification for adding human and machine-readable meaning to commit messages. The specification provides a simple set of rules for creating an explicit commit history, which makes it easier to write automated tools on top of.

## Available Placeholders

The plugin provides the following placeholders that can be used in commit message templates:

### Standard Task Placeholders

- `{id}` - Task ID (presentable ID)
- `{number}` - Task number
- `{summary}` - Task summary
- `{project}` - Project name
- `{taskType}` - Task type (FEATURE, BUG, EXCEPTION, OTHER)

### Conventional Commits Placeholders

- `{type}` - Conventional commit type (automatically mapped from task type)
  - FEATURE → `feat`
  - BUG → `fix`
  - EXCEPTION → `fix`
  - OTHER → `chore`

- `{scope}` - Scope derived from project name (lowercase, hyphenated)

- `{description}` - Task summary formatted for conventional commits (starts with lowercase)

- `{body}` - Task description (optional body section)

- `{footer}` - Footer with task reference (e.g., `Refs: TASK-123`)

- `{breaking}` - Breaking change indicator (use `!` for breaking changes)

## Configuration

### Setting Up Commit Message Template

1. Go to **Settings** → **Tools** → **Tasks**
2. Under **Commit Message**, configure your template using the placeholders

### Example Templates

#### Basic Conventional Commit
```
{type}: {description}
```
Output: `feat: add user authentication`

#### With Scope
```
{type}({scope}): {description}
```
Output: `feat(auth): add user authentication`

#### With Breaking Change
```
{type}{breaking}: {description}
```
Output: `feat!: add user authentication`

#### Full Format with Body and Footer
```
{type}({scope}): {description}

{body}

{footer}
```
Output:
```
feat(auth): add user authentication

Implemented JWT-based authentication with refresh tokens.
Added login and logout endpoints.

Refs: CU-123abc
```

#### With Task ID Reference
```
{type}({scope}): {description}

{footer}
```
Output:
```
fix(api): resolve null pointer exception

Refs: CU-456def
```

## Conventional Commits Specification

According to the [Conventional Commits specification](https://www.conventionalcommits.org/en/v1.0.0/#specification), a commit message should be structured as follows:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Common Types

- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation only changes
- `style`: Changes that don't affect code meaning (white-space, formatting, etc.)
- `refactor`: Code change that neither fixes a bug nor adds a feature
- `perf`: Performance improvements
- `test`: Adding missing tests or correcting existing tests
- `build`: Changes that affect the build system or external dependencies
- `ci`: Changes to CI configuration files and scripts
- `chore`: Other changes that don't modify src or test files

### Breaking Changes

Breaking changes can be indicated by:
- Adding `!` after the type/scope: `feat!: breaking change`
- Adding `BREAKING CHANGE:` in the footer

## Best Practices

1. **Keep descriptions concise** - The description should be a short summary (ideally under 50 characters)

2. **Use imperative mood** - Write descriptions as if giving a command ("add" not "added" or "adds")

3. **Lowercase description** - The plugin automatically lowercases the first character of the task summary

4. **Add scope when relevant** - Use scope to indicate which part of the codebase is affected

5. **Include body for context** - Use the task description as the commit body for additional context

6. **Reference tasks in footer** - The plugin automatically adds task references in the footer

## Workflow Example

1. **Select a ClickUp task** in the Tasks tool window
2. **Open commit dialog** when ready to commit changes
3. **Review the pre-filled commit message** generated from your template and the task information
4. **Adjust if needed** - You can still manually edit the generated message
5. **Commit** - Your commit now follows the Conventional Commits specification!

## Integration with ClickUp Task Types

The plugin intelligently maps ClickUp custom task types to conventional commit types:

- Tasks with custom types like "Bug", "Issue", "Defect" → `fix`
- Tasks with custom types like "Feature", "Story", "User Story" → `feat`
- Tasks with custom types like "Exception", "Error", "Incident" → `fix`
- Other task types → `chore`

This mapping happens automatically based on the task's custom item name in ClickUp.

## Additional Resources

- [Conventional Commits Specification](https://www.conventionalcommits.org/en/v1.0.0/)
- [Semantic Versioning](https://semver.org/)
- [Keep a Changelog](https://keepachangelog.com/)
ow 