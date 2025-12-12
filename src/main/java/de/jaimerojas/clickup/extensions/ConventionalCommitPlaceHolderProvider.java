/*
 * Copyright 2025 Jaime Enrique Rojas Almonte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.jaimerojas.clickup.extensions;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.CommitPlaceholderProvider;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.TaskType;
import de.jaimerojas.clickup.ClickUpRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides commit message placeholders based on Conventional Commits specification.
 *
 * @see <a href="https://www.conventionalcommits.org/en/v1.0.0/">Conventional Commits</a>
 */
public class ConventionalCommitPlaceHolderProvider implements CommitPlaceholderProvider {

    @Override
    public String @NotNull [] getPlaceholders(@Nullable TaskRepository taskRepository) {
        return new String[]{
                // Standard task placeholders
                "id",
                "number",
                "summary",
                "project",
                "taskType",
                // Conventional Commits placeholders
                "type",
                "scope",
                "description",
                "body",
                "footer",
                "breaking"
        };
    }

    @Override
    public @Nullable String getPlaceholderValue(LocalTask task, String placeholder) {
        if ("id".equals(placeholder)) {
            return task.getPresentableId();
        }
        if ("number".equals(placeholder)) {
            return task.getNumber();
        }
        if ("summary".equals(placeholder)) {
            return task.getSummary();
        }
        if ("project".equals(placeholder)) {
            return StringUtil.notNullize(task.getProject());
        }
        if ("taskType".equals(placeholder)) {
            return task.getType().name();
        }

        // Conventional Commits placeholders
        if ("type".equals(placeholder)) {
            return getConventionalCommitType(task);
        }
        if ("scope".equals(placeholder)) {
            return getConventionalCommitScope(task);
        }
        if ("description".equals(placeholder)) {
            return getConventionalCommitDescription(task);
        }
        if ("body".equals(placeholder)) {
            return getConventionalCommitBody(task);
        }
        if ("footer".equals(placeholder)) {
            return getConventionalCommitFooter(task);
        }
        if ("breaking".equals(placeholder)) {
            return ""; // Can be used for breaking change indicator
        }

        throw new IllegalArgumentException("Unknown placeholder: " + placeholder);
    }

    @Override
    public String getPlaceholderDescription(String placeholder) {
        return switch (placeholder) {
            case "id" -> "Task ID";
            case "number" -> "Task number";
            case "summary" -> "Task summary";
            case "project" -> "Project name";
            case "taskType" -> "Task type";
            case "type" -> "Conventional commit type (feat, fix, etc.)";
            case "scope" -> "Conventional commit scope (project or custom)";
            case "description" -> "Conventional commit description (task summary)";
            case "body" -> "Conventional commit body (task description)";
            case "footer" -> "Conventional commit footer (task reference)";
            case "breaking" -> "Breaking change indicator (!)";
            default -> null;
        };
    }

    /**
     * Maps task type to conventional commit type.
     *
     * @param task the task
     * @return conventional commit type string
     */
    private String getConventionalCommitType(LocalTask task) {
        TaskType taskType = task.getType();
        return switch (taskType) {
            case FEATURE -> "feat";
            case BUG -> "fix";
            case EXCEPTION -> "fix";
            case OTHER -> "chore";
        };
    }

    /**
     * Gets the scope for conventional commits.
     * Uses the project name if available.
     *
     * @param task the task
     * @return scope string or empty string
     */
    private String getConventionalCommitScope(LocalTask task) {
        String project = task.getProject();
        if (project != null && !project.isEmpty()) {
            // Clean up project name for scope (lowercase, no spaces)
            return project.toLowerCase().replaceAll("\\s+", "-");
        }
        return "";
    }

    /**
     * Gets the description for conventional commits.
     * Uses the task summary.
     *
     * @param task the task
     * @return description string
     */
    private String getConventionalCommitDescription(LocalTask task) {
        String summary = task.getSummary();
        if (!summary.isEmpty()) {
            // Ensure description starts with lowercase (conventional commits style)
            return Character.toLowerCase(summary.charAt(0)) + summary.substring(1);
        }
        return "";
    }

    /**
     * Gets the body for conventional commits.
     * Uses the task description if available.
     *
     * @param task the task
     * @return body string or empty string
     */
    private String getConventionalCommitBody(LocalTask task) {
        String description = task.getDescription();
        return StringUtil.notNullize(description);
    }

    /**
     * Gets the footer for conventional commits.
     * References the task ID in the footer.
     *
     * @param task the task
     * @return footer string with task reference
     */
    private String getConventionalCommitFooter(LocalTask task) {
        String taskId = task.getPresentableId();
        if (!taskId.isEmpty()) {
            TaskRepository repository = task.getRepository();
            if (repository instanceof ClickUpRepository) {
                return "Refs: " + taskId;
            }
            return "Refs: " + taskId;
        }
        return "";
    }
}
