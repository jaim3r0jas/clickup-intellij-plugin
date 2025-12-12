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
package de.jaimerojas.clickup.api;

import de.jaimerojas.clickup.model.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

/**
 * Interface for ClickUp API operations.
 * This abstraction allows for easier testing by enabling mock implementations.
 */
public interface ClickUpApiClient {

    /**
     * Fetches a single task by ID.
     *
     * @param taskId The ID of the task
     * @param useCustomTaskIds Whether to use custom task IDs
     * @param workspaceId The workspace ID (required when using custom task IDs)
     * @return The task, or null if not found
     * @throws IOException if the request fails
     */
    ClickUpTask fetchTask(@NotNull String taskId, boolean useCustomTaskIds, String workspaceId) throws IOException;

    /**
     * Fetches tasks from a workspace.
     *
     * @param workspaceId The workspace ID
     * @param assigneeId Optional assignee ID to filter by
     * @param page The page number for pagination
     * @param useCustomTaskIds Whether to use custom task IDs
     * @return List of tasks
     * @throws IOException if the request fails
     */
    @NotNull
    List<ClickUpTask> fetchTasks(
            @NotNull String workspaceId,
            String assigneeId,
            int page,
            boolean useCustomTaskIds
    ) throws IOException;

    /**
     * Fetches all authorized workspaces.
     *
     * @return List of workspaces
     * @throws IOException if the request fails
     */
    @NotNull
    List<ClickUpWorkspace> fetchWorkspaces() throws IOException;

    /**
     * Fetches a single space by ID.
     *
     * @param spaceId The space ID
     * @return The space
     * @throws IOException if the request fails
     */
    @NotNull
    ClickUpSpace fetchSpace(@NotNull String spaceId) throws IOException;

    /**
     * Fetches custom items by custom item ID.
     *
     * @param workspaceId The workspace ID
     * @return List of custom items
     * @throws IOException if the request fails
     */
    @NotNull
    List<ClickUpCustomItem> fetchCustomItems(@NotNull String workspaceId) throws IOException;

    /**
     * Fetches a custom item by ID.
     *
     * @param customItemId The custom item ID
     * @param workspaceId The workspace ID
     * @return The custom item
     * @throws IOException if the request fails
     */
    @NotNull
    ClickUpCustomItem fetchCustomItem(@NotNull String customItemId, @NotNull String workspaceId) throws IOException;

    /**
     * Tracks time spent on a task.
     *
     * @param taskId The task ID
     * @param timeSpentMillis Time spent in milliseconds
     * @param workspaceId The workspace ID
     * @param useCustomTaskIds Whether to use custom task IDs
     * @throws IOException if the request fails
     */
    void trackTimeSpent(
            @NotNull String taskId,
            long timeSpentMillis,
            @NotNull String workspaceId,
            boolean useCustomTaskIds
    ) throws IOException;

    /**
     * Updates the status of a task.
     *
     * @param taskId The task ID
     * @param statusName The new status name
     * @param workspaceId The workspace ID
     * @param useCustomTaskIds Whether to use custom task IDs
     * @throws IOException if the request fails
     */
    void updateTaskStatus(
            @NotNull String taskId,
            @NotNull String statusName,
            @NotNull String workspaceId,
            boolean useCustomTaskIds
    ) throws IOException;

    /**
     * Tests the connection to ClickUp API.
     *
     * @throws IOException if the connection test fails
     */
    void testConnection() throws IOException;
}

