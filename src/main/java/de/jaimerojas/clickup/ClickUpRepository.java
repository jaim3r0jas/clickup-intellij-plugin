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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.tasks.CustomTaskState;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepositoryType;
import com.intellij.tasks.impl.BaseRepository;
import com.intellij.tasks.impl.httpclient.NewBaseRepositoryImpl;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import de.jaimerojas.clickup.model.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Tag("ClickUp")
public class ClickUpRepository extends NewBaseRepositoryImpl {
    private static final Logger LOG = Logger.getInstance(ClickUpRepository.class);
    private static final Gson gson = new Gson();

    private String selectedWorkspaceId;
    private String selectedAssigneeId;
    private boolean useCustomTaskIds = false;

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
        String uri = getUrl() + "/task/" + taskId;
        if (useCustomTaskIds) {
            uri += "?custom_task_ids=true&team_id=" + selectedWorkspaceId;
        }
        HttpGet httpGet = new HttpGet(uri);
        httpGet.addHeader("Authorization", myPassword);
        try {
            // fixme: NewBaseRepositoryImplgetHttpClient() hast protected access
            return getHttpClient().execute(httpGet, response -> {
                String responseBody = EntityUtils.toString(response.getEntity());
                return gson.fromJson(responseBody, Task.class);
            });
        } catch (IOException e) {
            LOG.error("Error fetching task with ID: " + taskId, e);
        }
        return null;
    }

    @Override
    public Task[] getIssues(@Nullable String query, int offset, int limit, boolean withClosed) {
        return getIssues(query, offset, limit, withClosed, new EmptyProgressIndicator());
    }

    @Override
    public Task[] getIssues(
            @Nullable String query,
            int offset,
            int limit,
            boolean withClosed,
            @NotNull ProgressIndicator cancelled) {
        if (myPassword == null || myPassword.trim().isEmpty())
            return new Task[0];

        LOG.debug("getIssues called with offset: " + offset);
        LOG.debug("getIssues called with limit: " + limit);

        HttpGet httpGet = new HttpGet(buildGetIssuesUrl(offset));
        httpGet.addHeader("Authorization", myPassword);
        try {
            return getHttpClient().execute(httpGet, response -> {
                String responseBody = EntityUtils.toString(response.getEntity());
                Type listType = new TypeToken<GetTasks>() {
                }.getType();
                final ClickUpTask[] tasks = ((GetTasks) gson.fromJson(responseBody, listType)).getTasks().toArray(new ClickUpTask[0]);
                // set repo to each task - necessary to enable status update on open task dialog
                Arrays.stream(tasks).forEach(task -> task.setRepository(this));
                return tasks;
            });
        } catch (IOException e) {
            LOG.error("Error fetching tasks with query: " + query, e);
        }
        return new Task[0];
    }

    private @NotNull String buildGetIssuesUrl(int offset) {
        String getIssuesUrl = getUrl() + "/team/" + selectedWorkspaceId + "/task?subtasks=true&archived=false";

        int clickUpLimit = 100;// Fixed because ClickUp API always uses 100
        int page = offset / clickUpLimit;
        if (selectedAssigneeId != null && !selectedAssigneeId.isEmpty()) {
            getIssuesUrl += "&page=" + page + "&assignees[]=" + selectedAssigneeId;
        }
        if (useCustomTaskIds) {
            getIssuesUrl += "&custom_task_ids=true";
        }
        return getIssuesUrl;
    }

    @Override
    public void updateTimeSpent(@NotNull LocalTask task, @NotNull String timeSpent, @NotNull String comment) throws Exception {
        String taskId = task.getId();
        LOG.warn("Updating time spent for task ID: " + taskId);
        try {
            // convert time tracking format (3h 15m) to duration in millis 11700000
            String timeSpentInMillis = String.valueOf(
                    TimeUnit.HOURS.toMillis(Long.parseLong(timeSpent.split("h")[0])) +
                            TimeUnit.MINUTES.toMillis(Long.parseLong(timeSpent.split("h")[1].split("m")[0].trim()))
            );

            getHttpClient().execute(trackTimeSpend(timeSpentInMillis, comment, taskId), response -> {
                LOG.warn("Time spent updated for task ID: " + taskId + " with " + response.getStatusLine());
                return null;
            });
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
            getHttpClient().execute(updateTaskState(state, taskId));
        } catch (IOException e) {
            LOG.debug("Error updating task state for task ID: " + taskId, e);
            throw new Exception("Failed to update task state", e);
        }
    }

    @Override
    public @NotNull Set<CustomTaskState> getAvailableTaskStates(@NotNull Task task) throws Exception {
        Set<CustomTaskState> taskStatuses = new HashSet<>();
        LOG.warn("Fetching available task states for task ID: " + task.getId());

        ClickUpTask clickUpTask = fetchTask(task.getId());
        String spaceId = clickUpTask.getSpace().getId();
        ClickUpSpace space = fetchSpace(spaceId);

        space.getStatuses().forEach(state ->
                taskStatuses.add(new CustomTaskState(state.getId(), state.getStatus())));

        LOG.warn("Available task states: " + taskStatuses.stream()
                .map(CustomTaskState::getPresentableName)
                .reduce((a, b) -> a + ", " + b)
                .orElse(""));

        return taskStatuses;
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
                HttpGet httpGet = new HttpGet(getUrl() + "/team");
                httpGet.addHeader("Authorization", myPassword);
                getHttpClient().execute(httpGet);
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

    private @NotNull HttpPost trackTimeSpend(@NotNull String timeSpent, /* not supported via ClickUp API v2 */ @NotNull String ignore, String taskId) throws UnsupportedEncodingException {
        String url = getUrl() + "/task/" + taskId + "/time?custom_task_ids=true&team_id=" + selectedWorkspaceId;

        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Authorization", myPassword);
        httpPost.addHeader("Content-Type", "application/json");

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("time", timeSpent);

        StringEntity entity = new StringEntity(requestBody.toString());
        httpPost.setEntity(entity);

        return httpPost;
    }

    private @NotNull HttpPut updateTaskState(@NotNull CustomTaskState state, String taskId) throws UnsupportedEncodingException {
        StringBuilder urlBuilder = new StringBuilder(getUrl()).append("/task/").append(taskId).append("?team_id=").append(selectedWorkspaceId);
        if (useCustomTaskIds) {
            urlBuilder.append("&custom_task_ids=true");
        }

        HttpPut httpPut = new HttpPut(urlBuilder.toString());
        httpPut.addHeader("Authorization", myPassword);
        httpPut.addHeader("Content-Type", "application/json");

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("status", state.getPresentableName());

        StringEntity entity = new StringEntity(requestBody.toString());
        httpPut.setEntity(entity);
        return httpPut;
    }

    public List<ClickUpWorkspace> fetchWorkspaces() throws IOException {
        HttpGet httpGet = new HttpGet(getUrl() + "/team");
        httpGet.addHeader("Authorization", myPassword);
        return getHttpClient().execute(httpGet, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());
            Type listType = new TypeToken<GetAuthorizedWorkspaces>() {
            }.getType();
            return ((GetAuthorizedWorkspaces) gson.fromJson(responseBody, listType)).getTeams();
        });
    }

    private ClickUpTask fetchTask(String taskId) throws IOException {
        HttpGet httpGet = new HttpGet(getUrl() + "/task/" + taskId);
        httpGet.addHeader("Authorization", myPassword);
        return getHttpClient().execute(httpGet, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());
            Type listType = new TypeToken<ClickUpTask>() {
            }.getType();
            return gson.fromJson(responseBody, listType);
        });
    }

    private ClickUpSpace fetchSpace(String spaceId) throws IOException {
        HttpGet httpGet = new HttpGet(getUrl() + "/space/" + spaceId);
        httpGet.addHeader("Authorization", myPassword);
        return getHttpClient().execute(httpGet, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());
            Type listType = new TypeToken<ClickUpSpace>() {
            }.getType();
            return gson.fromJson(responseBody, listType);
        });
    }
}