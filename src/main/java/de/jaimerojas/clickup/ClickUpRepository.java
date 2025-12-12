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
package de.jaimerojas.clickup;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.tasks.CustomTaskState;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepositoryType;
import com.intellij.tasks.impl.BaseRepository;
import com.intellij.tasks.impl.httpclient.NewBaseRepositoryImpl;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import de.jaimerojas.clickup.api.ClickUpApiClient;
import de.jaimerojas.clickup.api.ClickUpApiClientImpl;
import de.jaimerojas.clickup.model.ClickUpCustomItem;
import de.jaimerojas.clickup.model.ClickUpSpace;
import de.jaimerojas.clickup.model.ClickUpTask;
import de.jaimerojas.clickup.model.ClickUpWorkspace;
import de.jaimerojas.clickup.service.ClickUpTaskService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Tag("ClickUp")
public class ClickUpRepository extends NewBaseRepositoryImpl {
    private static final Logger LOG = Logger.getInstance(ClickUpRepository.class);

    private String selectedWorkspaceId;
    private String selectedAssigneeId;
    private boolean useCustomTaskIds = false;

    // Service layer for business logic - can be injected for testing
    private ClickUpTaskService taskService;

    /**
     * Serialization constructor
     */
    @SuppressWarnings("UnusedDeclaration")
    public ClickUpRepository() {
        super();
    }

    public ClickUpRepository(TaskRepositoryType type) {
        super(type);
    }

    public ClickUpRepository(ClickUpRepository other) {
        super(other);
        setPassword(other.getPassword());
        setSelectedWorkspaceId(other.getSelectedWorkspaceId());
        setSelectedAssigneeId(other.getSelectedAssigneeId());
        setUseCustomTaskIds(other.isUseCustomTaskIds());
        this.taskService = other.taskService;
    }

    /**
     * Constructor for testing - allows injection of custom service
     */
    public ClickUpRepository(TaskRepositoryType type, ClickUpTaskService taskService) {
        super(type);
        this.taskService = taskService;
    }

    /**
     * Gets or creates the task service.
     * Lazily initializes the service with the API client.
     */
    @NotNull
    protected ClickUpTaskService getTaskService() {
        if (taskService == null) {
            ClickUpApiClient apiClient = new ClickUpApiClientImpl(getHttpClient(), myPassword);
            taskService = new ClickUpTaskService(apiClient);
        }
        return taskService;
    }

    /**
     * For testing - allows setting a custom task service
     */
    void setTaskService(ClickUpTaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public String getUrl() {
        return "https://api.clickup.com/api/v2";
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ClickUpRepository that)) return false;
        if (!super.equals(o)) return false;

        return Objects.equals(selectedWorkspaceId, that.selectedWorkspaceId)
                && Objects.equals(selectedAssigneeId, that.selectedAssigneeId)
                && useCustomTaskIds == that.useCustomTaskIds;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(selectedWorkspaceId);
        result = 31 * result + Objects.hashCode(selectedAssigneeId);
        result = 31 * result + Boolean.hashCode(useCustomTaskIds);
        return result;
    }

    @NotNull
    @Override
    public BaseRepository clone() {
        return new ClickUpRepository(this);
    }

    @Nullable
    @Override
    public Task findTask(@NotNull String taskId) {
        try {
            ClickUpTask task = getTaskService().getTask(taskId, useCustomTaskIds, selectedWorkspaceId);
            ClickUpCustomItem customItem = getTaskService().getCustomItem(selectedWorkspaceId, task.getCustom_item_id());
            task.setCustomItem(customItem);

            if (task != null) {
                task.setRepository(this);
            }
            return task;
        } catch (IOException e) {
            LOG.error("Error fetching task with ID: " + taskId, e);
        }
        return null;
    }

    @Override
    public Task[] getIssues(@Nullable String query, int offset, int limit, boolean withClosed) {
        if (myPassword == null || myPassword.trim().isEmpty())
            return new Task[0];

        LOG.debug("getIssues called with offset: " + offset);
        LOG.debug("getIssues called with limit: " + limit);

        try {
            List<ClickUpTask> tasks = getTaskService().getTasks(
                    selectedWorkspaceId,
                    selectedAssigneeId,
                    offset,
                    useCustomTaskIds
            );

            // set repo to each task - necessary to enable status update on open task dialog
            tasks.forEach(task -> {
                task.setRepository(this);
                ClickUpCustomItem customItem = null;
                try {
                    customItem = getTaskService().getCustomItem(selectedWorkspaceId, task.getCustom_item_id());
                } catch (IOException e) {
                    LOG.error("Error fetching custom item for task ID: " + task.getId(), e);
                }
                task.setCustomItem(customItem);
            });
            return tasks.toArray(new ClickUpTask[0]);
        } catch (IOException e) {
            LOG.error("Error fetching tasks with query: " + query, e);
        }
        return new Task[0];
    }


    @Override
    public void updateTimeSpent(@NotNull LocalTask task, @NotNull String timeSpent, @NotNull String comment) throws Exception {
        String taskId = task.getId();
        LOG.warn("Updating time spent for task ID: " + taskId);
        try {
            getTaskService().updateTimeSpent(taskId, timeSpent, selectedWorkspaceId, useCustomTaskIds);
            LOG.warn("Time spent updated for task ID: " + taskId);
        } catch (IOException e) {
            LOG.error("Error updating time spent for task ID: " + taskId, e);
            throw new Exception("Failed to update time spent", e);
        }
    }

    @Override
    public void setTaskState(@NotNull Task task, @NotNull CustomTaskState state) throws Exception {
        String taskId = task.getId();
        LOG.warn("Updating task state for task ID: " + taskId);
        try {
            getTaskService().updateTaskStatus(taskId, state.getPresentableName(), selectedWorkspaceId, useCustomTaskIds);
        } catch (IOException e) {
            LOG.debug("Error updating task state for task ID: " + taskId, e);
            throw new Exception("Failed to update task state", e);
        }
    }

    @Override
    public @NotNull Set<CustomTaskState> getAvailableTaskStates(@NotNull Task task) throws Exception {
        Set<CustomTaskState> taskStatuses = new HashSet<>();
        LOG.warn("Fetching available task states for task ID: " + task.getId());

        try {
            ClickUpTask clickUpTask = getTaskService().getTask(task.getId(), useCustomTaskIds, selectedWorkspaceId);
            String spaceId = clickUpTask.getSpace().getId();
            ClickUpSpace space = getTaskService().getSpace(spaceId);

            space.getStatuses().forEach(state ->
                    taskStatuses.add(new CustomTaskState(state.getId(), state.getStatus())));

            LOG.warn("Available task states: " + taskStatuses.stream()
                    .map(CustomTaskState::getPresentableName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));

            return taskStatuses;
        } catch (IOException e) {
            LOG.error("Error fetching task states for task ID: " + task.getId(), e);
            throw new Exception("Failed to fetch task states", e);
        }
    }

    @Override
    protected int getFeatures() {
        return /*super.getFeatures() |*/ STATE_UPDATING | TIME_MANAGEMENT;
    }

    @Override
    public @Nullable CancellableConnection createCancellableConnection() {
        return new CancellableConnection() {
            @Override
            public void cancel() {
                // do nothing
            }

            @Override
            protected void doTest() throws Exception {
                getTaskService().testConnection();
            }
        };
    }

    @Attribute("SelectedWorkspaceId")
    public String getSelectedWorkspaceId() {
        return selectedWorkspaceId;
    }

    public void setSelectedWorkspaceId(String selectedWorkspaceId) {
        this.selectedWorkspaceId = selectedWorkspaceId;
    }

    @Attribute("SelectedAssigneeId")
    public String getSelectedAssigneeId() {
        return selectedAssigneeId;
    }

    public void setSelectedAssigneeId(String selectedAssigneeId) {
        this.selectedAssigneeId = selectedAssigneeId;
    }

    @Attribute("UseCustomTaskIds")
    public boolean isUseCustomTaskIds() {
        return this.useCustomTaskIds;
    }

    public void setUseCustomTaskIds(boolean selected) {
        this.useCustomTaskIds = selected;
    }

    public void getHttpClientForTest() {
        getHttpClient();
    }

    public List<ClickUpWorkspace> fetchWorkspaces() throws IOException {
        return getTaskService().getWorkspaces();
    }
}