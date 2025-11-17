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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ClickUpTaskService}.
 * <p>
 * Tests business logic and API client integration without requiring actual HTTP calls.
 * This ensures the service layer correctly transforms data and delegates to the API client.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClickUpTaskService Tests")
class ClickUpTaskServiceTest {

    private static final String TEST_TASK_ID = "task123";
    private static final String TEST_WORKSPACE_ID = "workspace456";
    private static final String TEST_ASSIGNEE_ID = "user789";
    private static final String TEST_SPACE_ID = "space123";

    @Mock
    private ClickUpApiClient apiClient;

    private ClickUpTaskService service;

    @BeforeEach
    void setUp() {
        service = new ClickUpTaskService(apiClient);
    }

    @Nested
    @DisplayName("Task Retrieval")
    class TaskRetrieval {

        @Test
        @DisplayName("Should fetch task by ID from API")
        void getTask_shouldFetchTaskFromApi() throws IOException {
            // Arrange
            ClickUpTask expectedTask = new ClickUpTask();
            expectedTask.setId(TEST_TASK_ID);
            expectedTask.setName("Test Task");

            when(apiClient.fetchTask(TEST_TASK_ID, true, TEST_WORKSPACE_ID))
                    .thenReturn(expectedTask);

            // Act
            ClickUpTask result = service.getTask(TEST_TASK_ID, true, TEST_WORKSPACE_ID);

            // Assert
            assertNotNull(result, "Task should not be null");
            assertEquals(TEST_TASK_ID, result.getId());
            assertEquals("Test Task", result.getName());
            verify(apiClient).fetchTask(TEST_TASK_ID, true, TEST_WORKSPACE_ID);
        }

        @Test
        @DisplayName("Should fetch task without custom IDs")
        void getTask_shouldFetchWithoutCustomIds() throws IOException {
            // Arrange
            ClickUpTask task = new ClickUpTask();
            task.setId(TEST_TASK_ID);

            when(apiClient.fetchTask(TEST_TASK_ID, false, null))
                    .thenReturn(task);

            // Act
            ClickUpTask result = service.getTask(TEST_TASK_ID, false, null);

            // Assert
            assertNotNull(result);
            verify(apiClient).fetchTask(TEST_TASK_ID, false, null);
        }

        @ParameterizedTest(name = "Offset {0} should calculate page {1}")
        @CsvSource({
                "0, 0",      // First page
                "100, 1",    // Second page
                "200, 2",    // Third page
                "50, 0",     // Partial first page
                "150, 1",    // Partial second page
                "999, 9"     // High offset
        })
        @DisplayName("Should calculate pagination correctly")
        void getTasks_shouldCalculatePageCorrectly(int offset, int expectedPage) throws IOException {
            // Arrange
            List<ClickUpTask> tasks = Arrays.asList(new ClickUpTask(), new ClickUpTask());

            when(apiClient.fetchTasks(eq(TEST_WORKSPACE_ID), eq(TEST_ASSIGNEE_ID), eq(expectedPage), eq(true)))
                    .thenReturn(tasks);

            // Act
            List<ClickUpTask> result = service.getTasks(TEST_WORKSPACE_ID, TEST_ASSIGNEE_ID, offset, true);

            // Assert
            assertEquals(2, result.size());
            verify(apiClient).fetchTasks(TEST_WORKSPACE_ID, TEST_ASSIGNEE_ID, expectedPage, true);
        }

        @Test
        @DisplayName("Should fetch tasks with null assignee")
        void getTasks_shouldHandleNullAssignee() throws IOException {
            // Arrange
            List<ClickUpTask> tasks = Collections.singletonList(new ClickUpTask());

            when(apiClient.fetchTasks(TEST_WORKSPACE_ID, null, 0, false))
                    .thenReturn(tasks);

            // Act
            List<ClickUpTask> result = service.getTasks(TEST_WORKSPACE_ID, null, 0, false);

            // Assert
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return empty list when no tasks found")
        void getTasks_shouldReturnEmptyListWhenNoTasks() throws IOException {
            // Arrange
            when(apiClient.fetchTasks(anyString(), anyString(), anyInt(), anyBoolean()))
                    .thenReturn(Collections.emptyList());

            // Act
            List<ClickUpTask> result = service.getTasks(TEST_WORKSPACE_ID, TEST_ASSIGNEE_ID, 0, false);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Workspace Management")
    class WorkspaceManagement {

        @Test
        @DisplayName("Should fetch all workspaces from API")
        void getWorkspaces_shouldFetchWorkspacesFromApi() throws IOException {
            // Arrange
            List<ClickUpWorkspace> expectedWorkspaces = Arrays.asList(
                    new ClickUpWorkspace("w1", "Workspace 1"),
                    new ClickUpWorkspace("w2", "Workspace 2"),
                    new ClickUpWorkspace("w3", "Workspace 3")
            );

            when(apiClient.fetchWorkspaces()).thenReturn(expectedWorkspaces);

            // Act
            List<ClickUpWorkspace> result = service.getWorkspaces();

            // Assert
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals("Workspace 1", result.get(0).getName());
            assertEquals("Workspace 2", result.get(1).getName());
            assertEquals("Workspace 3", result.get(2).getName());
            verify(apiClient).fetchWorkspaces();
        }

        @Test
        @DisplayName("Should return empty list when no workspaces available")
        void getWorkspaces_shouldReturnEmptyListWhenNoneAvailable() throws IOException {
            // Arrange
            when(apiClient.fetchWorkspaces()).thenReturn(Collections.emptyList());

            // Act
            List<ClickUpWorkspace> result = service.getWorkspaces();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Space Management")
    class SpaceManagement {

        @Test
        @DisplayName("Should fetch space by ID from API")
        void getSpace_shouldFetchSpaceFromApi() throws IOException {
            // Arrange
            ClickUpSpace expectedSpace = new ClickUpSpace(TEST_SPACE_ID, "Test Space");

            when(apiClient.fetchSpace(TEST_SPACE_ID)).thenReturn(expectedSpace);

            // Act
            ClickUpSpace result = service.getSpace(TEST_SPACE_ID);

            // Assert
            assertNotNull(result);
            assertEquals(TEST_SPACE_ID, result.getId());
            assertEquals("Test Space", result.getName());
            verify(apiClient).fetchSpace(TEST_SPACE_ID);
        }
    }

    @Nested
    @DisplayName("Time Tracking")
    class TimeTracking {

        @ParameterizedTest(name = "{0} should convert to {1} milliseconds")
        @CsvSource({
                "'3h 15m', 11700000",    // 3 hours 15 minutes
                "'2h 30m', 9000000",     // 2 hours 30 minutes
                "'1h 0m', 3600000",      // 1 hour exactly
                "'0h 45m', 2700000",     // 45 minutes
                "'5h 0m', 18000000",     // 5 hours
                "'10h 30m', 37800000"    // 10 hours 30 minutes
        })
        @DisplayName("Should parse time format correctly")
        void parseTimeSpentToMillis_shouldParseCorrectly(String timeSpent, long expectedMillis) {
            // Act
            long result = service.parseTimeSpentToMillis(timeSpent);

            // Assert
            assertEquals(expectedMillis, result);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid",
                "3hours",
                "h 30m",
                "",
                "abc 123"
        })
        @DisplayName("Should throw exception for invalid time format")
        void parseTimeSpentToMillis_shouldThrowExceptionForInvalidFormat(String invalidFormat) {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.parseTimeSpentToMillis(invalidFormat)
            );
            assertTrue(exception.getMessage().contains("Invalid time format"));
        }

        @Test
        @DisplayName("Should update time spent on task")
        void updateTimeSpent_shouldParseTimeAndCallApi() throws IOException {
            // Arrange
            String timeSpent = "3h 15m";
            long expectedMillis = TimeUnit.HOURS.toMillis(3) + TimeUnit.MINUTES.toMillis(15);

            // Act
            service.updateTimeSpent(TEST_TASK_ID, timeSpent, TEST_WORKSPACE_ID, true);

            // Assert
            verify(apiClient).trackTimeSpent(TEST_TASK_ID, expectedMillis, TEST_WORKSPACE_ID, true);
        }

        @Test
        @DisplayName("Should handle time tracking without custom IDs")
        void updateTimeSpent_shouldWorkWithoutCustomIds() throws IOException {
            // Arrange
            long expectedMillis = TimeUnit.HOURS.toMillis(1);

            // Act
            service.updateTimeSpent(TEST_TASK_ID, "1h 0m", TEST_WORKSPACE_ID, false);

            // Assert
            verify(apiClient).trackTimeSpent(TEST_TASK_ID, expectedMillis, TEST_WORKSPACE_ID, false);
        }
    }

    @Nested
    @DisplayName("Task Status Updates")
    class TaskStatusUpdates {

        @Test
        @DisplayName("Should update task status")
        void updateTaskStatus_shouldCallApi() throws IOException {
            // Arrange
            String statusName = "In Progress";

            // Act
            service.updateTaskStatus(TEST_TASK_ID, statusName, TEST_WORKSPACE_ID, false);

            // Assert
            verify(apiClient).updateTaskStatus(TEST_TASK_ID, statusName, TEST_WORKSPACE_ID, false);
        }

        @ParameterizedTest
        @ValueSource(strings = {"To Do", "In Progress", "Review", "Done", "Closed"})
        @DisplayName("Should handle different status names")
        void updateTaskStatus_shouldHandleDifferentStatuses(String statusName) throws IOException {
            // Act
            service.updateTaskStatus(TEST_TASK_ID, statusName, TEST_WORKSPACE_ID, true);

            // Assert
            verify(apiClient).updateTaskStatus(TEST_TASK_ID, statusName, TEST_WORKSPACE_ID, true);
        }
    }

    @Nested
    @DisplayName("Connection Testing")
    class ConnectionTesting {

        @Test
        @DisplayName("Should test API connection")
        void testConnection_shouldCallApi() throws IOException {
            // Act
            service.testConnection();

            // Assert
            verify(apiClient).testConnection();
        }

        @Test
        @DisplayName("Should propagate connection errors")
        void testConnection_shouldPropagateErrors() throws IOException {
            // Arrange
            doThrow(new IOException("Connection failed")).when(apiClient).testConnection();

            // Act & Assert
            assertThrows(IOException.class, () -> service.testConnection());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should propagate IOException from task fetch")
        void getTask_shouldPropagateIOException() throws IOException {
            // Arrange
            when(apiClient.fetchTask(anyString(), anyBoolean(), anyString()))
                    .thenThrow(new IOException("API Error"));

            // Act & Assert
            assertThrows(IOException.class,
                    () -> service.getTask(TEST_TASK_ID, false, TEST_WORKSPACE_ID));
        }

        @Test
        @DisplayName("Should propagate IOException from workspace fetch")
        void getWorkspaces_shouldPropagateIOException() throws IOException {
            // Arrange
            when(apiClient.fetchWorkspaces()).thenThrow(new IOException("Network Error"));

            // Act & Assert
            assertThrows(IOException.class, () -> service.getWorkspaces());
        }

        @Test
        @DisplayName("Should propagate IOException from time tracking")
        void updateTimeSpent_shouldPropagateIOException() throws IOException {
            // Arrange
            doThrow(new IOException("Update failed"))
                    .when(apiClient).trackTimeSpent(anyString(), anyLong(), anyString(), anyBoolean());

            // Act & Assert
            assertThrows(IOException.class,
                    () -> service.updateTimeSpent(TEST_TASK_ID, "1h 0m", TEST_WORKSPACE_ID, false));
        }
    }
}

