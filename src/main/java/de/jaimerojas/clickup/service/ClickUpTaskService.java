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
package de.jaimerojas.clickup.service;

import de.jaimerojas.clickup.api.ClickUpApiClient;
import de.jaimerojas.clickup.model.ClickUpSpace;
import de.jaimerojas.clickup.model.ClickUpTask;
import de.jaimerojas.clickup.model.ClickUpWorkspace;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service layer for ClickUp task operations.
 * Separates business logic from repository implementation.
 */
public class ClickUpTaskService {
    private final ClickUpApiClient apiClient;

    public ClickUpTaskService(@NotNull ClickUpApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Fetches a task by ID.
     */
    public ClickUpTask getTask(@NotNull String taskId, boolean useCustomTaskIds, String workspaceId) throws IOException {
        return apiClient.fetchTask(taskId, useCustomTaskIds, workspaceId);
    }

    /**
     * Fetches tasks with pagination.
     */
    public @NotNull List<ClickUpTask> getTasks(
            @NotNull String workspaceId,
            String assigneeId,
            int offset,
            boolean useCustomTaskIds
    ) throws IOException {
        int clickUpLimit = 100; // ClickUp API always uses 100
        int page = offset / clickUpLimit;
        return apiClient.fetchTasks(workspaceId, assigneeId, page, useCustomTaskIds);
    }

    /**
     * Fetches all workspaces.
     */
    public @NotNull List<ClickUpWorkspace> getWorkspaces() throws IOException {
        return apiClient.fetchWorkspaces();
    }

    /**
     * Fetches space details by ID.
     */
    public @NotNull ClickUpSpace getSpace(@NotNull String spaceId) throws IOException {
        return apiClient.fetchSpace(spaceId);
    }

    /**
     * Updates time spent on a task.
     * Converts time format (e.g., "3h 15m") to milliseconds.
     *
     * @param taskId The task ID
     * @param timeSpent Time in format "Xh Ym"
     * @param workspaceId The workspace ID
     * @param useCustomTaskIds Whether to use custom task IDs
     * @throws IOException if the update fails
     * @throws IllegalArgumentException if timeSpent format is invalid
     */
    public void updateTimeSpent(
            @NotNull String taskId,
            @NotNull String timeSpent,
            @NotNull String workspaceId,
            boolean useCustomTaskIds
    ) throws IOException {
        long timeSpentMillis = parseTimeSpentToMillis(timeSpent);
        apiClient.trackTimeSpent(taskId, timeSpentMillis, workspaceId, useCustomTaskIds);
    }

    /**
     * Updates task status.
     */
    public void updateTaskStatus(
            @NotNull String taskId,
            @NotNull String statusName,
            @NotNull String workspaceId,
            boolean useCustomTaskIds
    ) throws IOException {
        apiClient.updateTaskStatus(taskId, statusName, workspaceId, useCustomTaskIds);
    }

    /**
     * Tests the API connection.
     */
    public void testConnection() throws IOException {
        apiClient.testConnection();
    }

    /**
     * Parses time spent string (e.g., "3h 15m") to milliseconds.
     * Package-private for testing.
     */
    long parseTimeSpentToMillis(@NotNull String timeSpent) {
        try {
            String[] parts = timeSpent.split("h");
            long hours = Long.parseLong(parts[0].trim());
            long minutes = 0;

            if (parts.length > 1) {
                String minutePart = parts[1].replace("m", "").trim();
                if (!minutePart.isEmpty()) {
                    minutes = Long.parseLong(minutePart);
                }
            }

            return TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid time format: " + timeSpent + ". Expected format: 'Xh Ym'", e);
        }
    }
}

