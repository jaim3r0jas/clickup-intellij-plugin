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
import de.jaimerojas.clickup.model.ClickUpCustomItem;
import de.jaimerojas.clickup.model.ClickUpSpace;
import de.jaimerojas.clickup.model.ClickUpTask;
import de.jaimerojas.clickup.model.ClickUpWorkspace;
import de.jaimerojas.clickup.model.GetTasks;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/**
 * Service layer for ClickUp task operations.
 * Separates business logic from repository implementation.
 */
public class ClickUpTaskService {
    /**
     * At most this many ClickUp pages per call: ClickUp allows 100 requests a minute per token.
     */
    static final int MAX_PAGES_PER_CALL = 5;
    /**
     * A ClickUp page is reused this long: the IDE asks in slices of 20 or 50, which all fall in the same page.
     */
    static final long PAGE_CACHE_MILLIS = 30_000;
    /**
     * A workspace's custom task types change rarely: they are fetched again after this long.
     */
    static final long CUSTOM_ITEMS_CACHE_MILLIS = 10 * 60_000;
    private static final int MAX_CACHED_PAGES = 50;

    private final ClickUpApiClient apiClient;
    private final LongSupplier clock;
    private final Map<String, Timed<GetTasks>> pages = new ConcurrentHashMap<>();
    private final Map<String, Timed<Map<String, ClickUpCustomItem>>> customItems = new ConcurrentHashMap<>();

    private record Timed<T>(T value, long fetchedAt) {
    }

    public ClickUpTaskService(@NotNull ClickUpApiClient apiClient) {
        this(apiClient, System::currentTimeMillis);
    }

    ClickUpTaskService(@NotNull ClickUpApiClient apiClient, @NotNull LongSupplier clock) {
        this.apiClient = apiClient;
        this.clock = clock;
    }

    /**
     * Fetches a task by ID.
     */
    public ClickUpTask getTask(@NotNull String taskId, boolean useCustomTaskIds, String workspaceId) throws IOException {
        return apiClient.fetchTask(taskId, useCustomTaskIds, workspaceId);
    }

    /**
     * Fetches the tasks from {@code offset} to {@code offset + limit} (the IDE's paging) across ClickUp's pages of
     * {@value ClickUpApiClient#PAGE_SIZE}, stopping at the last page and after {@value #MAX_PAGES_PER_CALL} pages.
     * Offsets count ClickUp's tasks as it sends them, so slices of different calls line up; a task without an ID
     * (which ClickUp doesn't send in a list) is then left out of the slice.
     */
    public @NotNull List<ClickUpTask> getTasks(
            @NotNull String workspaceId,
            String assigneeId,
            int offset,
            int limit,
            boolean useCustomTaskIds,
            boolean includeClosed
    ) throws IOException {
        int pageSize = ClickUpApiClient.PAGE_SIZE;
        int wanted = limit > 0 ? limit : pageSize;
        int page = Math.max(offset, 0) / pageSize;
        int skip = Math.max(offset, 0) % pageSize;

        // a slice that starts inside a page may need one page more than MAX_PAGES_PER_CALL to be complete
        int maxPages = MAX_PAGES_PER_CALL + (skip > 0 ? 1 : 0);
        List<ClickUpTask> result = new ArrayList<>();
        int taken = 0;
        for (int fetched = 0; fetched < maxPages && taken < wanted; fetched++, page++, skip = 0) {
            GetTasks clickUpPage = fetchPage(workspaceId, assigneeId, page, useCustomTaskIds, includeClosed);
            List<ClickUpTask> tasks = clickUpPage.getTasks();
            for (int i = skip; i < tasks.size() && taken < wanted; i++, taken++) {
                if (tasks.get(i) != null && tasks.get(i).hasId()) {
                    result.add(tasks.get(i));
                }
            }
            boolean lastPage = clickUpPage.getLast_page() != null
                    ? clickUpPage.getLast_page()
                    : clickUpPage.getTasks().size() < pageSize;
            if (lastPage) {
                break;
            }
        }
        return result;
    }

    // synchronized: two refreshes at once (the IDE's background update and the Open Task popup) send one request
    private synchronized @NotNull GetTasks fetchPage(
            @NotNull String workspaceId,
            String assigneeId,
            int page,
            boolean useCustomTaskIds,
            boolean includeClosed
    ) throws IOException {
        String key = String.join("|", workspaceId, String.valueOf(assigneeId), String.valueOf(page),
                String.valueOf(useCustomTaskIds), String.valueOf(includeClosed));
        long now = clock.getAsLong();
        Timed<GetTasks> cached = pages.get(key);
        if (cached != null && now - cached.fetchedAt() < PAGE_CACHE_MILLIS) {
            return cached.value();
        }
        GetTasks fetched = apiClient.fetchTasks(workspaceId, assigneeId, page, useCustomTaskIds, includeClosed);
        if (pages.size() >= MAX_CACHED_PAGES) {
            pages.values().removeIf(entry -> now - entry.fetchedAt() >= PAGE_CACHE_MILLIS);
            if (pages.size() >= MAX_CACHED_PAGES) {
                pages.clear();
            }
        }
        pages.put(key, new Timed<>(fetched, now));
        return fetched;
    }

    /**
     * The workspace's custom task types by ID, ClickUp's default task type ("0") included. Fetched with one request
     * and reused for {@value #CUSTOM_ITEMS_CACHE_MILLIS} ms.
     */
    public synchronized @NotNull Map<String, ClickUpCustomItem> getCustomItemsById(@NotNull String workspaceId) throws IOException {
        long now = clock.getAsLong();
        Timed<Map<String, ClickUpCustomItem>> cached = customItems.get(workspaceId);
        if (cached != null && now - cached.fetchedAt() < CUSTOM_ITEMS_CACHE_MILLIS) {
            return cached.value();
        }
        Map<String, ClickUpCustomItem> byId = new HashMap<>();
        for (ClickUpCustomItem item : apiClient.fetchCustomItems(workspaceId)) {
            if (item != null && item.getId() != null) {
                byId.put(item.getId(), item);
            }
        }
        byId.put(ClickUpCustomItem.DEFAULT_TASK_ID, ClickUpCustomItem.defaultTask());
        Map<String, ClickUpCustomItem> result = Map.copyOf(byId);
        customItems.put(workspaceId, new Timed<>(result, now));
        return result;
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
        // the cached pages show the old status (and a closed task where closed tasks are left out)
        pages.clear();
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

