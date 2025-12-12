# ConventionalCommitPlaceholderProvider Implementation Summary

## Overview

This document summarizes the implementation of the `ConventionalCommitPlaceholderProvider` for the ClickUp IntelliJ plugin, which provides support for Conventional Commits specification in commit messages.

## Implementation Date

December 12, 2025

## Files Created/Modified

### 1. Main Implementation

**File:** `src/main/java/de/jaimerojas/clickup/extensions/ConventionalCommitPlaceHolderProvider.java`

- **Status:** Implemented
- **Purpose:** Provides commit message placeholders based on Conventional Commits specification
- **Key Features:**
  - Maps task types to conventional commit types (feat, fix, chore)
  - Generates scope from project name
  - Formats description with proper casing
  - Creates footer with task references
  - Supports all standard task placeholders

### 2. Plugin Registration

**File:** `src/main/resources/META-INF/plugin.xml`

- **Status:** Updated
- **Change:** Added extension point registration for `tasks.commitPlaceholderProvider`
- **Registration:**
  ```xml
  <tasks.commitPlaceholderProvider implementation="de.jaimerojas.clickup.extensions.ConventionalCommitPlaceHolderProvider"/>
  ```

### 3. Unit Tests

**File:** `src/test/java/de/jaimerojas/clickup/extensions/ConventionalCommitPlaceHolderProviderTest.java`

- **Status:** Created
- **Coverage:** Comprehensive test suite with 20+ test cases
- **Test Categories:**
  - Placeholder Registration
  - Standard Task Placeholders
  - Conventional Commit Type Mapping
  - Conventional Commit Scope
  - Conventional Commit Description
  - Conventional Commit Body
  - Conventional Commit Footer
  - Breaking Change Placeholder
  - Error Handling

### 4. Documentation

**File:** `docs/CONVENTIONAL_COMMITS.md`

- **Status:** Created
- **Content:** Complete user guide including:
  - Overview of Conventional Commits
  - Available placeholders
  - Configuration instructions
  - Example templates
  - Best practices
  - Integration with ClickUp task types

**File:** `README.md`

- **Status:** Updated
- **Change:** Added feature mention for Conventional Commits support

## Placeholders Provided

### Standard Placeholders

| Placeholder | Description | Example |
|------------|-------------|---------|
| `{id}` | Task ID (presentable ID) | `CU-123abc` |
| `{number}` | Task number | `456` |
| `{summary}` | Task summary | `Fix login bug` |
| `{project}` | Project name | `Authentication` |
| `{taskType}` | Task type | `FEATURE`, `BUG`, etc. |

### Conventional Commits Placeholders

| Placeholder | Description | Example |
|------------|-------------|---------|
| `{type}` | Conventional commit type | `feat`, `fix`, `chore` |
| `{scope}` | Scope from project name | `auth`, `api` |
| `{description}` | Lowercased task summary | `add user authentication` |
| `{body}` | Task description | Full task description |
| `{footer}` | Task reference | `Refs: CU-123abc` |
| `{breaking}` | Breaking change indicator | Empty (for manual use) |

## Type Mapping

The implementation automatically maps IntelliJ task types to conventional commit types:

- `TaskType.FEATURE` → `feat`
- `TaskType.BUG` → `fix`
- `TaskType.EXCEPTION` → `fix`
- `TaskType.OTHER` → `chore`

## Example Usage

### Template Configuration

Users can configure commit message templates in:
**Settings → Tools → Tasks → Commit Message**

### Example Templates

1. **Basic:**
   ```
   {type}: {description}
   ```
   Output: `feat: add user authentication`

2. **With Scope:**
   ```
   {type}({scope}): {description}
   ```
   Output: `feat(auth): add user authentication`

3. **Full Format:**
   ```
   {type}({scope}): {description}
   
   {body}
   
   {footer}
   ```
   Output:
   ```
   feat(auth): add user authentication
   
   Implemented JWT-based authentication with refresh tokens.
   
   Refs: CU-123abc
   ```

## Technical Details

### Key Methods

1. **`getPlaceholders(TaskRepository)`**
   - Returns array of all available placeholder names
   - Total: 11 placeholders

2. **`getPlaceholderValue(LocalTask, String)`**
   - Resolves placeholder value from task data
   - Handles all standard and conventional commit placeholders
   - Throws `IllegalArgumentException` for unknown placeholders

3. **`getPlaceholderDescription(String)`**
   - Provides human-readable description for each placeholder
   - Used in IDE UI for placeholder selection

### Implementation Highlights

- **Null Safety:** Proper handling of null values using `StringUtil.notNullize()`
- **Case Conversion:** Automatic lowercase conversion for conventional commit style
- **Scope Formatting:** Converts project names to lowercase and replaces spaces with hyphens
- **Clean Code:** Well-documented with JavaDoc comments
- **Error Handling:** Throws clear exceptions for invalid placeholders

## Testing

### Test Coverage

- ✅ All 20+ tests passing
- ✅ No compiler errors or warnings
- ✅ Complete coverage of all placeholders
- ✅ Edge cases covered (null values, empty strings, etc.)

### Running Tests

```bash
./gradlew test --tests ConventionalCommitPlaceHolderProviderTest
```

## Compliance

### Conventional Commits Specification

The implementation follows the [Conventional Commits v1.0.0](https://www.conventionalcommits.org/en/v1.0.0/) specification:

✅ Type prefix support
✅ Optional scope support
✅ Description formatting
✅ Optional body support
✅ Optional footer support
✅ Breaking change indicator support

### Code Quality

- ✅ Apache 2.0 license headers
- ✅ JavaDoc documentation
- ✅ IntelliJ code style compliance
- ✅ No warnings or errors
- ✅ Proper null handling
- ✅ Exception handling

## Integration

The provider integrates seamlessly with:

1. **IntelliJ Tasks Framework**
   - Registered as `CommitPlaceholderProvider`
   - Works with all IntelliJ task types

2. **ClickUp Plugin**
   - Uses ClickUp task data
   - Leverages custom task IDs when available
   - Respects repository configuration

3. **VCS Integration**
   - Commit dialog placeholder substitution
   - Template preview in settings
   - Changelist commit messages

## Future Enhancements

Potential improvements for future versions:

1. **Dynamic Breaking Change Detection**
   - Analyze task labels or custom fields
   - Auto-populate `{breaking}` placeholder

2. **Custom Type Mapping**
   - User-configurable type mappings
   - Support for additional types (docs, style, etc.)

3. **Multi-line Body Formatting**
   - Better formatting for long descriptions
   - Markdown to plain text conversion

4. **Footer Enhancements**
   - Support for multiple footer formats
   - Configurable reference style
   - Co-authored-by support

## References

- [Conventional Commits Specification](https://www.conventionalcommits.org/en/v1.0.0/)
- [IntelliJ Platform SDK - Tasks](https://plugins.jetbrains.com/docs/intellij/task-management.html)
- [DefaultCommitPlaceholderProvider](https://github.com/JetBrains/intellij-community/blob/master/plugins/tasks/tasks-core/src/com/intellij/tasks/impl/DefaultCommitPlaceholderProvider.java)

## Conclusion

The `ConventionalCommitPlaceholderProvider` successfully implements comprehensive support for Conventional Commits in the ClickUp IntelliJ plugin. The implementation is fully tested, well-documented, and ready for use.

