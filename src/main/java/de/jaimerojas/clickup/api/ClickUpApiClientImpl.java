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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.jaimerojas.clickup.model.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * Default implementation of ClickUpApiClient using Apache HttpClient.
 * <p>
 * Every call checks the HTTP status: anything but 2xx throws a {@link ClickUpApiException} with ClickUp's error
 * message, instead of parsing an error body into an empty object.
 */
public class ClickUpApiClientImpl implements ClickUpApiClient {
    private static final String BASE_URL = "https://api.clickup.com/api/v2";

    private final HttpClient httpClient;
    private final String apiToken;
    private final Gson gson;

    public ClickUpApiClientImpl(@NotNull HttpClient httpClient, @NotNull String apiToken) {
        this(httpClient, apiToken, new Gson());
    }

    public ClickUpApiClientImpl(@NotNull HttpClient httpClient, @NotNull String apiToken, @NotNull Gson gson) {
        this.httpClient = httpClient;
        this.apiToken = apiToken;
        this.gson = gson;
    }

    @Override
    public ClickUpTask fetchTask(@NotNull String taskId, boolean useCustomTaskIds, String workspaceId) throws IOException {
        StringBuilder uri = new StringBuilder(BASE_URL).append("/task/").append(taskId);
        if (useCustomTaskIds && workspaceId != null) {
            uri.append("?custom_task_ids=true&team_id=").append(workspaceId);
        }
        ClickUpTask task = execute(new HttpGet(uri.toString()), ClickUpTask.class);
        if (task == null) {
            throw new IOException("ClickUp returned no task for ID: " + taskId);
        }
        return task;
    }

    @Override
    public @NotNull List<ClickUpTask> fetchTasks(
            @NotNull String workspaceId,
            String assigneeId,
            int page,
            boolean useCustomTaskIds
    ) throws IOException {
        StringBuilder url = new StringBuilder(BASE_URL)
                .append("/team/").append(workspaceId)
                .append("/task?subtasks=true&archived=false");

        if (assigneeId != null && !assigneeId.isEmpty()) {
            url.append("&page=").append(page).append("&assignees[]=").append(assigneeId);
        }

        if (useCustomTaskIds) {
            url.append("&custom_task_ids=true");
        }

        GetTasks getTasks = execute(new HttpGet(url.toString()), GetTasks.class);
        return getTasks == null || getTasks.getTasks() == null ? List.of() : getTasks.getTasks();
    }

    @Override
    public @NotNull List<ClickUpWorkspace> fetchWorkspaces() throws IOException {
        GetAuthorizedWorkspaces workspaces = execute(new HttpGet(BASE_URL + "/team"), GetAuthorizedWorkspaces.class);
        return workspaces == null || workspaces.getTeams() == null ? List.of() : workspaces.getTeams();
    }

    @Override
    public @NotNull ClickUpSpace fetchSpace(@NotNull String spaceId) throws IOException {
        ClickUpSpace space = execute(new HttpGet(BASE_URL + "/space/" + spaceId), ClickUpSpace.class);
        if (space == null) {
            throw new IOException("ClickUp returned no space for ID: " + spaceId);
        }
        return space;
    }

    @Override
    public @NotNull List<ClickUpCustomItem> fetchCustomItems(@NotNull String workspaceId) throws IOException {
        GetCustomItems getCustomItems = execute(
                new HttpGet(BASE_URL + "/team/" + workspaceId + "/custom_item"), GetCustomItems.class);
        return getCustomItems == null || getCustomItems.getCustom_items() == null
                ? List.of()
                : getCustomItems.getCustom_items();
    }

    @Override
    public @NotNull ClickUpCustomItem fetchCustomItem(@NotNull String customItemId, @NotNull String workspaceId) throws IOException {
        if (customItemId.equals("0")) {
            return new ClickUpCustomItem("0", "Task", "Tasks", "Default ClickUp task type", null);
        }

        return fetchCustomItems(workspaceId).stream()
                .filter(item -> customItemId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new IOException("Custom item not found: " + customItemId));
    }

    @Override
    public void trackTimeSpent(
            @NotNull String taskId,
            long timeSpentMillis,
            @NotNull String workspaceId,
            boolean useCustomTaskIds
    ) throws IOException {
        String url = BASE_URL + "/task/" + taskId + "/time";
        if (useCustomTaskIds) {
            url += "?custom_task_ids=true&team_id=" + workspaceId;
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("time", String.valueOf(timeSpentMillis));

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));
        execute(httpPost, null);
    }

    @Override
    public void updateTaskStatus(
            @NotNull String taskId,
            @NotNull String statusName,
            @NotNull String workspaceId,
            boolean useCustomTaskIds
    ) throws IOException {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL)
                .append("/task/").append(taskId)
                .append("?team_id=").append(workspaceId);

        if (useCustomTaskIds) {
            urlBuilder.append("&custom_task_ids=true");
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("status", statusName);

        HttpPut httpPut = new HttpPut(urlBuilder.toString());
        httpPut.setEntity(new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON));
        execute(httpPut, null);
    }

    @Override
    public void testConnection() throws IOException {
        execute(new HttpGet(BASE_URL + "/team"), null);
    }

    /**
     * Sends the request with the API token and returns the body parsed as {@code type}, or null when {@code type} is
     * null or the body is empty.
     *
     * @throws ClickUpApiException when ClickUp answers with a status other than 2xx
     * @throws IOException         when the request fails or the body is not valid JSON
     */
    private <T> @Nullable T execute(@NotNull HttpRequestBase request, @Nullable Class<T> type) throws IOException {
        request.addHeader("Authorization", apiToken);
        return httpClient.execute(request, response -> {
            String body = response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw toApiException(statusCode, body);
            }
            if (type == null || body.isBlank()) {
                return null;
            }
            try {
                return gson.fromJson(body, type);
            } catch (JsonParseException e) {
                throw new IOException("Unexpected response from ClickUp for " + request.getURI().getPath(), e);
            }
        });
    }

    private @NotNull ClickUpApiException toApiException(int statusCode, @NotNull String body) {
        String error = null;
        String errorCode = null;
        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);
            if (json != null) {
                error = json.has("err") && !json.get("err").isJsonNull() ? json.get("err").getAsString() : null;
                errorCode = json.has("ECODE") && !json.get("ECODE").isJsonNull() ? json.get("ECODE").getAsString() : null;
            }
        } catch (RuntimeException e) {
            // not ClickUp's JSON error body (a proxy's HTML page, for one): the status alone has to do
        }
        return new ClickUpApiException(statusCode, errorCode, error);
    }
}
