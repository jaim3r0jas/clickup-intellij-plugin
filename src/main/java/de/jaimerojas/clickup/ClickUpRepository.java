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
import java.net.URI;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Tag("ClickUpRepository")
public class ClickUpRepository extends NewBaseRepositoryImpl {
    private static final Logger LOG = Logger.getInstance(ClickUpRepository.class);

    public static final String API_URL = "https://api.clickup.com/api/v2";

    private static final Gson gson = new Gson();

    private String selectedWorkspaceId;
    private String selectedSpaceId;
    private String selectedListId;

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
        setSelectedSpaceId(other.getSelectedSpaceId());
        setSelectedListId(other.getSelectedListId());
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ClickUpRepository that)) return false;
        if (!super.equals(o)) return false;

        return Objects.equals(selectedWorkspaceId, that.selectedWorkspaceId)
                && Objects.equals(selectedSpaceId, that.selectedSpaceId)
                && Objects.equals(selectedListId, that.selectedListId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(selectedWorkspaceId);
        result = 31 * result + Objects.hashCode(selectedSpaceId);
        result = 31 * result + Objects.hashCode(selectedListId);
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
        HttpGet httpGet = new HttpGet(API_URL + "/task/" + taskId);
        httpGet.addHeader("Authorization", myPassword);
        try {
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
    public Task[] getIssues(@Nullable String query, int offset, int limit, boolean withClosed) throws Exception {
        return getIssues(query, offset, limit, withClosed, new EmptyProgressIndicator());
    }

    @Override
    public Task[] getIssues(@Nullable String query, int offset, int limit, boolean withClosed, @NotNull ProgressIndicator cancelled) throws Exception {
        LOG.info("getIssues called with listId: " + selectedListId);

        // FIXME: move archived flag to a checkbox in the settings
        HttpGet httpGet = new HttpGet(API_URL + "/list/" + selectedListId + "/task?archived=false");

        if (query != null && !query.isEmpty()) {
            httpGet.setURI(new URI(httpGet.getURI().toString() + "&assignees[]=" + query));
        }
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
        for (ClickUpSpace space : fetchSpaces(selectedWorkspaceId)) {
            if (space.getId().equals(selectedSpaceId)) {
                space.getStatuses().forEach(state -> {
                    taskStatuses.add(new CustomTaskState(state.getId(), state.getStatus()) {
                        @Override
                        public boolean isPredefined() {
                            return false;
                        }
                    });
                });
            }
        }

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
                HttpGet httpGet = new HttpGet(API_URL + "/team");
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

    @Attribute("SelectedSpaceId")
    public String getSelectedSpaceId() {
        return selectedSpaceId;
    }

    public void setSelectedSpaceId(String selectedSpaceId) {
        this.selectedSpaceId = selectedSpaceId;
    }

    @Attribute("SelectedListId")
    public String getSelectedListId() {
        return selectedListId;
    }

    public void setSelectedListId(String selectedListId) {
        this.selectedListId = selectedListId;
    }

    private @NotNull HttpPost trackTimeSpend(@NotNull String timeSpent, /* not supported via ClickUp API v2 */ @NotNull String ignore, String taskId) throws UnsupportedEncodingException {
        String url = API_URL + "/task/" + taskId + "/time?custom_task_ids=true&team_id=" + selectedWorkspaceId;

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
        String url = API_URL + "/task/" + taskId + "?custom_task_ids=true&team_id=" + selectedWorkspaceId;

        HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader("Authorization", myPassword);
        httpPut.addHeader("Content-Type", "application/json");

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("status", state.getPresentableName());

        StringEntity entity = new StringEntity(requestBody.toString());
        httpPut.setEntity(entity);
        return httpPut;
    }

    public List<ClickUpWorkspace> fetchWorkspaces() throws IOException {
        HttpGet httpGet = new HttpGet(API_URL + "/team");
        httpGet.addHeader("Authorization", myPassword);
        return getHttpClient().execute(httpGet, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());
            Type listType = new TypeToken<GetAuthorizedWorkspaces>() {
            }.getType();
            return ((GetAuthorizedWorkspaces) gson.fromJson(responseBody, listType)).getTeams();
        });
    }

    public List<ClickUpSpace> fetchSpaces(String workspaceId) throws IOException {
        HttpGet httpGet = new HttpGet(API_URL + "/team/" + workspaceId + "/space?archived=false");
        httpGet.addHeader("Authorization", myPassword);
        return getHttpClient().execute(httpGet, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());
            Type listType = new TypeToken<GetSpaces>() {
            }.getType();
            return ((GetSpaces) gson.fromJson(responseBody, listType)).getSpaces();
        });
    }

    public List<ClickUpList> fetchLists(String spaceId) throws IOException {
        HttpGet httpGet = new HttpGet(API_URL + "/space/" + spaceId + "/list?archived=false");
        httpGet.addHeader("Authorization", myPassword);
        return getHttpClient().execute(httpGet, response -> {
            String responseBody = EntityUtils.toString(response.getEntity());
            Type listType = new TypeToken<GetFolderlessLists>() {
            }.getType();
            return ((GetFolderlessLists) gson.fromJson(responseBody, listType)).getLists();
        });
    }

    public void clearSelectedWorkspace() {
        setSelectedWorkspaceId(null);
        setSelectedSpaceId(null);
        setSelectedListId(null);
    }

    public void clearSelectedSpace() {
        setSelectedSpaceId(null);
        setSelectedListId(null);
    }

}