/*
 * Copyright 2026 Jaime Enrique Rojas Almonte
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

import de.jaimerojas.clickup.model.ClickUpWorkspace;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClickUpApiClientImpl Tests")
class ClickUpApiClientImplTest {

    private static final String API_TOKEN = "pk_test_token";
    private static final String TOKEN_INVALID = "{\"err\":\"Token invalid\",\"ECODE\":\"OAUTH_025\"}";

    @Mock
    private HttpClient httpClient;

    private ClickUpApiClientImpl client;

    @BeforeEach
    void setUp() {
        client = new ClickUpApiClientImpl(httpClient, API_TOKEN);
    }

    /**
     * Makes the mocked HttpClient answer every request with this status and body, run through the client's own
     * response handler as the real HttpClient does.
     */
    private void respond(int status, String body) throws IOException {
        when(httpClient.execute(any(HttpUriRequest.class), ArgumentMatchers.<ResponseHandler<Object>>any()))
                .thenAnswer(invocation -> {
                    BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, status, "");
                    response.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
                    ResponseHandler<?> handler = invocation.getArgument(1);
                    return handler.handleResponse(response);
                });
    }

    @Nested
    @DisplayName("Error responses (#56)")
    class ErrorResponses {

        @Test
        @DisplayName("A rejected token fails the connection test instead of passing it")
        void testConnection_shouldFailOnRejectedToken() throws IOException {
            respond(401, TOKEN_INVALID);

            ClickUpApiException error = assertThrows(ClickUpApiException.class, () -> client.testConnection());

            assertEquals(401, error.getStatusCode());
            assertEquals("OAUTH_025", error.getErrorCode());
            assertTrue(error.isAuthenticationError());
            assertTrue(error.getMessage().contains("Token invalid"), error.getMessage());
        }

        @Test
        @DisplayName("Fetching workspaces with a rejected token throws instead of returning null")
        void fetchWorkspaces_shouldThrowOnRejectedToken() throws IOException {
            respond(401, TOKEN_INVALID);

            ClickUpApiException error = assertThrows(ClickUpApiException.class, () -> client.fetchWorkspaces());

            assertTrue(error.isAuthenticationError());
        }

        @Test
        @DisplayName("A task ClickUp can't find throws instead of returning an empty task")
        void fetchTask_shouldThrowOnNotFound() throws IOException {
            respond(404, "{\"err\":\"Task not found, deleted\",\"ECODE\":\"ITEM_013\"}");

            ClickUpApiException error = assertThrows(ClickUpApiException.class,
                    () -> client.fetchTask("abc123", false, null));

            assertTrue(error.isNotFound());
            assertEquals("ITEM_013", error.getErrorCode());
        }

        @Test
        @DisplayName("An error body that isn't ClickUp's JSON still reports the status")
        void shouldReportStatusForNonJsonErrorBody() throws IOException {
            respond(502, "<html>Bad Gateway</html>");

            ClickUpApiException error = assertThrows(ClickUpApiException.class, () -> client.fetchWorkspaces());

            assertEquals(502, error.getStatusCode());
            assertNull(error.getErrorCode());
            assertEquals("ClickUp API error (HTTP 502)", error.getMessage());
        }

        @Test
        @DisplayName("A failed status update throws instead of passing silently")
        void updateTaskStatus_shouldThrowOnError() throws IOException {
            respond(400, "{\"err\":\"Status does not exist\",\"ECODE\":\"ITEM_115\"}");

            ClickUpApiException error = assertThrows(ClickUpApiException.class,
                    () -> client.updateTaskStatus("abc123", "nope", "ws1", false));

            assertEquals(400, error.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Successful responses")
    class SuccessfulResponses {

        @Test
        @DisplayName("Parses the workspaces and sends the API token")
        void fetchWorkspaces_shouldParseTeams() throws IOException {
            respond(200, "{\"teams\":[{\"id\":\"1\",\"name\":\"Acme\",\"members\":[]}]}");

            List<ClickUpWorkspace> workspaces = client.fetchWorkspaces();

            assertEquals(1, workspaces.size());
            assertEquals("1", workspaces.get(0).getId());
            ArgumentCaptor<HttpUriRequest> request = ArgumentCaptor.forClass(HttpUriRequest.class);
            verify(httpClient).execute(request.capture(), ArgumentMatchers.<ResponseHandler<Object>>any());
            assertEquals(API_TOKEN, request.getValue().getFirstHeader("Authorization").getValue());
        }

        @Test
        @DisplayName("A response without workspaces gives an empty list, never null")
        void fetchWorkspaces_shouldReturnEmptyListWhenTeamsMissing() throws IOException {
            respond(200, "{}");

            assertEquals(List.of(), client.fetchWorkspaces());
        }

        @Test
        @DisplayName("A response without tasks gives an empty list, never null")
        void fetchTasks_shouldReturnEmptyListWhenTasksMissing() throws IOException {
            respond(200, "{}");

            assertEquals(List.of(), client.fetchTasks("ws1", null, 0, false, false).getTasks());
        }

        @Test
        @DisplayName("An empty task body throws instead of returning null")
        void fetchTask_shouldThrowOnEmptyBody() throws IOException {
            respond(200, "");

            assertThrows(IOException.class, () -> client.fetchTask("abc123", false, null));
        }

        @Test
        @DisplayName("Asks for the page even without an assignee, newest updates first, closed tasks on request (#66)")
        void fetchTasks_shouldSendPageOrderAndIncludeClosed() throws IOException {
            respond(200, "{\"tasks\":[]}");

            client.fetchTasks("ws1", null, 2, false, true);

            ArgumentCaptor<HttpUriRequest> request = ArgumentCaptor.forClass(HttpUriRequest.class);
            verify(httpClient).execute(request.capture(), ArgumentMatchers.<ResponseHandler<Object>>any());
            String query = request.getValue().getURI().getQuery();
            assertTrue(query.contains("page=2"), query);
            assertTrue(query.contains("order_by=updated"), query);
            assertTrue(query.contains("include_closed=true"), query);
            assertFalse(query.contains("assignees"), query);
        }

        @Test
        @DisplayName("Reads ClickUp's last_page flag")
        void fetchTasks_shouldReadLastPage() throws IOException {
            respond(200, "{\"tasks\":[{\"id\":\"t1\"}],\"last_page\":true}");

            de.jaimerojas.clickup.model.GetTasks page = client.fetchTasks("ws1", null, 0, false, false);

            assertEquals(1, page.getTasks().size());
            assertEquals(Boolean.TRUE, page.getLast_page());
        }

        @Test
        @DisplayName("Leaves closed tasks out unless asked")
        void fetchTasks_shouldLeaveClosedTasksOutByDefault() throws IOException {
            respond(200, "{\"tasks\":[]}");

            client.fetchTasks("ws1", "user1", 0, false, false);

            ArgumentCaptor<HttpUriRequest> request = ArgumentCaptor.forClass(HttpUriRequest.class);
            verify(httpClient).execute(request.capture(), ArgumentMatchers.<ResponseHandler<Object>>any());
            String query = request.getValue().getURI().getQuery();
            assertFalse(query.contains("include_closed"), query);
            assertTrue(query.contains("page=0"), query);
        }

        @Test
        @DisplayName("A valid token passes the connection test")
        void testConnection_shouldPassOnSuccess() throws IOException {
            respond(200, "{\"teams\":[]}");

            assertDoesNotThrow(() -> client.testConnection());
        }
    }
}
