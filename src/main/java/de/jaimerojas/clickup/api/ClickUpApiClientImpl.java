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
import com.google.gson.reflect.TypeToken;
import de.jaimerojas.clickup.model.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Default implementation of ClickUpApiClient using Apache HttpClient.
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

        HttpGet httpGet = new HttpGet(uri.toString());
        httpGet.addHeader("Authorization", apiToken);

        return httpClient.execute(httpGet, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());
            return gson.fromJson(responseBody, ClickUpTask.class);
        });
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

        HttpGet httpGet = new HttpGet(url.toString());
        httpGet.addHeader("Authorization", apiToken);

        return httpClient.execute(httpGet, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());
            Type listType = new TypeToken<GetTasks>() {}.getType();
            GetTasks getTasks = gson.fromJson(responseBody, listType);
            return getTasks.getTasks();
        });
    }

    @Override
    public @NotNull List<ClickUpWorkspace> fetchWorkspaces() throws IOException {
        HttpGet httpGet = new HttpGet(BASE_URL + "/team");
        httpGet.addHeader("Authorization", apiToken);

        return httpClient.execute(httpGet, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());
            Type listType = new TypeToken<GetAuthorizedWorkspaces>() {}.getType();
            GetAuthorizedWorkspaces workspaces = gson.fromJson(responseBody, listType);
            return workspaces.getTeams();
        });
    }

    @Override
    public @NotNull ClickUpSpace fetchSpace(@NotNull String spaceId) throws IOException {
        HttpGet httpGet = new HttpGet(BASE_URL + "/space/" + spaceId);
        httpGet.addHeader("Authorization", apiToken);

        return httpClient.execute(httpGet, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());
            Type listType = new TypeToken<ClickUpSpace>() {}.getType();
            return gson.fromJson(responseBody, listType);
        });
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

        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Authorization", apiToken);
        httpPost.addHeader("Content-Type", "application/json");

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("time", String.valueOf(timeSpentMillis));

        httpPost.setEntity(new StringEntity(requestBody.toString()));

        httpClient.execute(httpPost, response -> {
            // Just consume the response
            EntityUtils.consume(response.getEntity());
            return null;
        });
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

        HttpPut httpPut = new HttpPut(urlBuilder.toString());
        httpPut.addHeader("Authorization", apiToken);
        httpPut.addHeader("Content-Type", "application/json");

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("status", statusName);

        httpPut.setEntity(new StringEntity(requestBody.toString()));

        httpClient.execute(httpPut, response -> {
            EntityUtils.consume(response.getEntity());
            return null;
        });
    }

    @Override
    public void testConnection() throws IOException {
        HttpGet httpGet = new HttpGet(BASE_URL + "/team");
        httpGet.addHeader("Authorization", apiToken);
        httpClient.execute(httpGet, response -> {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new IOException("Cannot connect to ClickUp API.\nStatus code: " + statusCode);
            }
            return null;
        });
    }
}

