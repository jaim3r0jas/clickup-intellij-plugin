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
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.text.StringUtil;
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
import de.jaimerojas.clickup.api.ClickUpApiException;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Tag("ClickUp")
public class ClickUpRepository extends NewBaseRepositoryImpl {
    private static final Logger LOG = Logger.getInstance(ClickUpRepository.class);

    private String selectedWorkspaceId;
    private String selectedAssigneeId;
    private boolean useCustomTaskIds = false;

    // Service layer for business logic. Built from the current API token, and rebuilt when the token changes; a
    // service injected for testing is kept as it is.
    private ClickUpTaskService taskService;
    private String taskServiceToken;
    private boolean taskServiceInjected;

    // The last tasks each task list request got, returned when ClickUp fails (see getIssues). Never persisted.
    private static final int MAX_REMEMBERED_REQUESTS = 20;
    private final transient Map<String, ClickUpTask[]> lastIssues = new ConcurrentHashMap<>();

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
        // a copy builds its own service from its own token: sharing it would keep using the token of the original
        if (other.taskServiceInjected) {
            setTaskService(other.taskService);
        }
    }

    /**
     * Constructor for testing - allows injection of custom service
     */
    public ClickUpRepository(TaskRepositoryType type, ClickUpTaskService taskService) {
        super(type);
        setTaskService(taskService);
    }

    /**
     * Gets the task service, built with the current API token. A changed token (a new one typed in the settings)
     * gets a new service, so the old token is never used again.
     */
    @NotNull
    protected synchronized ClickUpTaskService getTaskService() {
        if (taskServiceInjected) {
            return taskService;
        }
        String token = StringUtil.notNullize(myPassword);
        if (taskService == null || !token.equals(taskServiceToken)) {
            taskService = createTaskService(token);
            taskServiceToken = token;
            lastIssues.clear(); // never fall back to tasks another token got
        }
        return taskService;
    }

    /**
     * Builds the service for an API token. Overridden in tests.
     */
    @NotNull
    protected ClickUpTaskService createTaskService(@NotNull String apiToken) {
        ClickUpApiClient apiClient = new ClickUpApiClientImpl(getHttpClient(), apiToken);
        return new ClickUpTaskService(apiClient);
    }

    /**
     * For testing - sets a service that is used whatever the token; null goes back to building it from the token.
     */
    synchronized void setTaskService(@Nullable ClickUpTaskService taskService) {
        this.taskService = taskService;
        this.taskServiceInjected = taskService != null;
        this.taskServiceToken = null;
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
            // no task, or one without an ID (an error body): the IDE would copy the empty ID into its local task (#55)
            if (task == null || !task.hasId()) {
                return null;
            }

            task.setRepository(this);
            attachCustomItem(task, customItemsFor(List.of(task)));
            return task;
        } catch (IOException e) {
            LOG.warn("Error fetching task with ID: " + taskId, e);
        }
        return null;
    }

    /**
     * The IDE's task list. It never throws: the Tasks framework marks a repository whose getIssues throws as bad and
     * skips it on every refresh until the settings are applied again (#66). It never answers a failure with an empty
     * list either: the IDE's refresh would replace its cached tasks with nothing. A failure is logged and gives the
     * last tasks this request got, so the next refresh simply tries again. The IDE filters by {@code query} itself
     * (no native search).
     */
    @Override
    public Task[] getIssues(@Nullable String query, int offset, int limit, boolean withClosed) {
        if (StringUtil.isEmptyOrSpaces(myPassword) || StringUtil.isEmptyOrSpaces(selectedWorkspaceId)) {
            return new Task[0];
        }

        LOG.debug("getIssues called with offset " + offset + ", limit " + limit + ", withClosed " + withClosed);
        String request = String.join("|", selectedWorkspaceId, String.valueOf(selectedAssigneeId),
                String.valueOf(useCustomTaskIds), String.valueOf(offset), String.valueOf(limit), String.valueOf(withClosed));

        try {
            List<ClickUpTask> tasks = getTaskService().getTasks(
                    selectedWorkspaceId,
                    selectedAssigneeId,
                    offset,
                    limit,
                    useCustomTaskIds,
                    withClosed
            );

            Map<String, ClickUpCustomItem> customItems = customItemsFor(tasks);
            // set repo to each task - necessary to enable status update on open task dialog
            tasks.forEach(task -> {
                task.setRepository(this);
                attachCustomItem(task, customItems);
            });
            ClickUpTask[] issues = tasks.toArray(new ClickUpTask[0]);
            if (lastIssues.size() >= MAX_REMEMBERED_REQUESTS) {
                lastIssues.clear();
            }
            lastIssues.put(request, issues);
            return issues;
        } catch (ProcessCanceledException e) {
            throw e; // the IDE cancelled the request: not a failure
        } catch (ClickUpApiException e) {
            LOG.warn("Error fetching tasks with query: " + query, e);
            if (e.isAuthenticationError()) {
                // a revoked or rejected token: showing old tasks would hide it
                lastIssues.clear();
                return new Task[0];
            }
        } catch (IOException | RuntimeException e) {
            LOG.warn("Error fetching tasks with query: " + query, e);
        }
        return lastIssues.getOrDefault(request, new ClickUpTask[0]);
    }

    /**
     * The custom task types these tasks need, by ID: none requested when every task has the default type, else the
     * workspace's types (one request, cached by the service). Types that can't be fetched are left out: those tasks
     * keep the default type.
     */
    private @NotNull Map<String, ClickUpCustomItem> customItemsFor(@NotNull List<ClickUpTask> tasks) {
        boolean needsCustomTypes = tasks.stream()
                .map(ClickUpTask::getCustom_item_id)
                .anyMatch(id -> id != null && !ClickUpCustomItem.DEFAULT_TASK_ID.equals(id));
        if (!needsCustomTypes) {
            return Map.of(ClickUpCustomItem.DEFAULT_TASK_ID, ClickUpCustomItem.defaultTask());
        }
        try {
            return getTaskService().getCustomItemsById(selectedWorkspaceId);
        } catch (ProcessCanceledException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            LOG.warn("Error fetching custom task types of workspace " + selectedWorkspaceId, e);
            return Map.of(ClickUpCustomItem.DEFAULT_TASK_ID, ClickUpCustomItem.defaultTask());
        }
    }

    // a task without a custom item ID, or whose custom item can't be fetched, keeps the default task type
    private static void attachCustomItem(@NotNull ClickUpTask task, @NotNull Map<String, ClickUpCustomItem> customItems) {
        // checked first: immutable maps don't take null keys
        String customItemId = task.getCustom_item_id();
        ClickUpCustomItem customItem = customItemId == null ? null : customItems.get(customItemId);
        if (customItem != null) {
            task.setCustomItem(customItem);
        }
    }


    @Override
    public void updateTimeSpent(@NotNull LocalTask task, @NotNull String timeSpent, @NotNull String comment) throws Exception {
        String taskId = task.getId();
        LOG.debug("Updating time spent for task ID: " + taskId);
        try {
            getTaskService().updateTimeSpent(taskId, timeSpent, selectedWorkspaceId, useCustomTaskIds);
            LOG.debug("Time spent updated for task ID: " + taskId);
        } catch (IOException e) {
            LOG.warn("Error updating time spent for task ID: " + taskId, e);
            throw new Exception(ClickUpBundle.message("error.clickup.time.spent.failed", e.getMessage()), e);
        }
    }

    @Override
    public void setTaskState(@NotNull Task task, @NotNull CustomTaskState state) throws Exception {
        String taskId = task.getId();
        LOG.debug("Updating task state for task ID: " + taskId);
        try {
            getTaskService().updateTaskStatus(taskId, state.getPresentableName(), selectedWorkspaceId, useCustomTaskIds);
        } catch (IOException e) {
            LOG.warn("Error updating task state for task ID: " + taskId, e);
            throw new Exception(ClickUpBundle.message("error.clickup.task.state.failed", e.getMessage()), e);
        }
    }

    @Override
    public @NotNull Set<CustomTaskState> getAvailableTaskStates(@NotNull Task task) throws Exception {
        Set<CustomTaskState> taskStatuses = new HashSet<>();
        LOG.debug("Fetching available task states for task ID: " + task.getId());

        try {
            ClickUpTask clickUpTask = getTaskService().getTask(task.getId(), useCustomTaskIds, selectedWorkspaceId);
            if (clickUpTask == null || clickUpTask.getSpace() == null || clickUpTask.getSpace().getId() == null) {
                LOG.warn("No space for task ID " + task.getId() + ": no task states to offer");
                return taskStatuses;
            }
            ClickUpSpace space = getTaskService().getSpace(clickUpTask.getSpace().getId());
            if (space.getStatuses() == null) {
                return taskStatuses;
            }

            space.getStatuses().forEach(state ->
                    taskStatuses.add(new CustomTaskState(state.getId(), state.getStatus())));

            LOG.debug("Available task states: " + taskStatuses.stream()
                    .map(CustomTaskState::getPresentableName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));

            return taskStatuses;
        } catch (IOException e) {
            LOG.warn("Error fetching task states for task ID: " + task.getId(), e);
            throw new Exception(ClickUpBundle.message("error.clickup.task.states.failed", e.getMessage()), e);
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

    public List<ClickUpWorkspace> fetchWorkspaces() throws IOException {
        return getTaskService().getWorkspaces();
    }
}