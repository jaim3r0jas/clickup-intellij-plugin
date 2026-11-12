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
import de.jaimerojas.clickup.model.GetTasks;
import de.jaimerojas.clickup.model.ClickUpCustomItem;
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

        @Test
        @DisplayName("Offset 0, limit 20: the first 20 tasks of page 0")
        void getTasks_shouldSliceFirstPage() throws IOException {
            when(apiClient.fetchTasks(TEST_WORKSPACE_ID, TEST_ASSIGNEE_ID, 0, true, false)).thenReturn(page(0, 100));

            List<ClickUpTask> result = service.getTasks(TEST_WORKSPACE_ID, TEST_ASSIGNEE_ID, 0, 20, true, false);

            assertEquals(ids(0, 20), idsOf(result));
        }

        @Test
        @DisplayName("Offset 20, limit 20: the next 20 tasks, not page 0 again (#66)")
        void getTasks_shouldReturnSecondSliceAtOffset20() throws IOException {
            when(apiClient.fetchTasks(TEST_WORKSPACE_ID, TEST_ASSIGNEE_ID, 0, false, false)).thenReturn(page(0, 100));

            List<ClickUpTask> result = service.getTasks(TEST_WORKSPACE_ID, TEST_ASSIGNEE_ID, 20, 20, false, false);

            assertEquals(ids(20, 40), idsOf(result));
        }

        @Test
        @DisplayName("A slice across two ClickUp pages fetches both")
        void getTasks_shouldSpanPages() throws IOException {
            when(apiClient.fetchTasks(TEST_WORKSPACE_ID, null, 0, false, false)).thenReturn(page(0, 100));
            when(apiClient.fetchTasks(TEST_WORKSPACE_ID, null, 1, false, false)).thenReturn(page(100, 100));

            List<ClickUpTask> result = service.getTasks(TEST_WORKSPACE_ID, null, 90, 20, false, false);

            assertEquals(ids(90, 110), idsOf(result));
        }

        @Test
        @DisplayName("Stops at the last page (fewer than 100 tasks)")
        void getTasks_shouldStopAtLastPage() throws IOException {
            when(apiClient.fetchTasks(TEST_WORKSPACE_ID, null, 0, false, false)).thenReturn(page(0, 30));

            List<ClickUpTask> result = service.getTasks(TEST_WORKSPACE_ID, null, 0, 50, false, false);

            assertEquals(30, result.size());
            verify(apiClient, never()).fetchTasks(TEST_WORKSPACE_ID, null, 1, false, false);
        }

        @Test
        @DisplayName("Never fetches more than 5 pages in one call (ClickUp's rate limit)")
        void getTasks_shouldCapPagesPerCall() throws IOException {
            when(apiClient.fetchTasks(eq(TEST_WORKSPACE_ID), isNull(), anyInt(), eq(false), eq(false)))
                    .thenAnswer(invocation -> page(invocation.<Integer>getArgument(2) * 100, 100));


            List<ClickUpTask> result = service.getTasks(TEST_WORKSPACE_ID, null, 0, 10_000, false, false);

            assertEquals(500, result.size());
            verify(apiClient, times(5)).fetchTasks(eq(TEST_WORKSPACE_ID), isNull(), anyInt(), eq(false), eq(false));
        }

        @Test
        @DisplayName("Passes include closed through")
        void getTasks_shouldPassIncludeClosed() throws IOException {
            when(apiClient.fetchTasks(TEST_WORKSPACE_ID, null, 0, false, true)).thenReturn(page(0, 1));

            assertEquals(1, service.getTasks(TEST_WORKSPACE_ID, null, 0, 20, false, true).size());
        }

        @Test
        @DisplayName("Should return empty list when no tasks found")
        void getTasks_shouldReturnEmptyListWhenNoTasks() throws IOException {
            when(apiClient.fetchTasks(anyString(), anyString(), anyInt(), anyBoolean(), anyBoolean()))
                    .thenReturn(new GetTasks(Collections.emptyList(), true));

            List<ClickUpTask> result = service.getTasks(TEST_WORKSPACE_ID, TEST_ASSIGNEE_ID, 0, 20, false, false);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        /**
         * A ClickUp page of tasks with IDs firstId..firstId+count-1, without the last_page flag (as older answers).
         */
        private GetTasks page(int firstId, int count) {
            List<ClickUpTask> tasks = new java.util.ArrayList<>();
            for (int i = firstId; i < firstId + count; i++) {
                ClickUpTask task = new ClickUpTask();
                task.setId(String.valueOf(i));
                tasks.add(task);
            }
            return new GetTasks(tasks, null);
        }

        @Test
        @DisplayName("Follows ClickUp's last_page flag over the page size")
        void getTasks_shouldFollowLastPageFlag() throws IOException {
            GetTasks shortButNotLast = page(0, 90);
            shortButNotLast.setLast_page(false);
            GetTasks fullButLast = page(90, 100);
            fullButLast.setLast_page(true);
            when(apiClient.fetchTasks(TEST_WORKSPACE_ID, null, 0, false, false)).thenReturn(shortButNotLast);
            when(apiClient.fetchTasks(TEST_WORKSPACE_ID, null, 1, false, false)).thenReturn(fullButLast);

            List<ClickUpTask> result = service.getTasks(TEST_WORKSPACE_ID, null, 0, 500, false, false);

            assertEquals(190, result.size());
            verify(apiClient, never()).fetchTasks(TEST_WORKSPACE_ID, null, 2, false, false);
        }

        @Test
        @DisplayName("Offsets count ClickUp's tasks as sent, so slices line up; a task without an ID is left out (#55)")
        void getTasks_shouldCountRawTasksAndDropTasksWithoutId() throws IOException {
            GetTasks withBlank = page(0, 30);
            withBlank.getTasks().set(1, new ClickUpTask());
            when(apiClient.fetchTasks(TEST_WORKSPACE_ID, null, 0, false, false)).thenReturn(withBlank);

            List<ClickUpTask> first = service.getTasks(TEST_WORKSPACE_ID, null, 0, 10, false, false);
            List<ClickUpTask> second = service.getTasks(TEST_WORKSPACE_ID, null, 10, 10, false, false);

            assertEquals(List.of("0", "2", "3", "4", "5", "6", "7", "8", "9"), idsOf(first));
            assertEquals(ids(10, 20), idsOf(second));
        }

        @Test
        @DisplayName("A slice starting inside a page still gets its full 5 pages' worth")
        void getTasks_shouldNotShortenSliceStartingInsidePage() throws IOException {
            when(apiClient.fetchTasks(eq(TEST_WORKSPACE_ID), isNull(), anyInt(), eq(false), eq(false)))
                    .thenAnswer(invocation -> page(invocation.<Integer>getArgument(2) * 100, 100));

            List<ClickUpTask> result = service.getTasks(TEST_WORKSPACE_ID, null, 50, 500, false, false);

            assertEquals(ids(50, 550), idsOf(result));
        }

        @Test
        @DisplayName("A status change made from the IDE drops the cached pages")
        void updateTaskStatus_shouldDropCachedPages() throws IOException {
            when(apiClient.fetchTasks(TEST_WORKSPACE_ID, null, 0, false, false)).thenReturn(page(0, 10));

            service.getTasks(TEST_WORKSPACE_ID, null, 0, 20, false, false);
            service.updateTaskStatus("1", "closed", TEST_WORKSPACE_ID, false);
            service.getTasks(TEST_WORKSPACE_ID, null, 0, 20, false, false);

            verify(apiClient, times(2)).fetchTasks(TEST_WORKSPACE_ID, null, 0, false, false);
        }

        @Test
        @DisplayName("The IDE's slices of one ClickUp page cost one request, until the page is 30 s old")
        void getTasks_shouldReuseRecentPage() throws IOException {
            long[] now = {1_000_000};
            ClickUpTaskService cachingService = new ClickUpTaskService(apiClient, () -> now[0]);
            when(apiClient.fetchTasks(TEST_WORKSPACE_ID, null, 0, false, false)).thenReturn(page(0, 100));

            cachingService.getTasks(TEST_WORKSPACE_ID, null, 0, 20, false, false);
            cachingService.getTasks(TEST_WORKSPACE_ID, null, 20, 20, false, false);
            cachingService.getTasks(TEST_WORKSPACE_ID, null, 40, 20, false, false);
            verify(apiClient, times(1)).fetchTasks(TEST_WORKSPACE_ID, null, 0, false, false);

            now[0] += ClickUpTaskService.PAGE_CACHE_MILLIS;
            cachingService.getTasks(TEST_WORKSPACE_ID, null, 0, 20, false, false);
            verify(apiClient, times(2)).fetchTasks(TEST_WORKSPACE_ID, null, 0, false, false);
        }

        private List<String> ids(int fromInclusive, int toExclusive) {
            return java.util.stream.IntStream.range(fromInclusive, toExclusive).mapToObj(String::valueOf).toList();
        }

        private List<String> idsOf(List<ClickUpTask> tasks) {
            return tasks.stream().map(ClickUpTask::getId).toList();
        }
    }

    @Nested
    @DisplayName("Custom task types")
    class CustomTaskTypes {

        @Test
        @DisplayName("One request gives every type by ID, the default type included")
        void getCustomItemsById_shouldMapTypesAndDefault() throws IOException {
            ClickUpCustomItem bug = new ClickUpCustomItem("1001", "Bug", "Bugs", null, null);
            when(apiClient.fetchCustomItems(TEST_WORKSPACE_ID)).thenReturn(List.of(bug));

            java.util.Map<String, ClickUpCustomItem> byId = service.getCustomItemsById(TEST_WORKSPACE_ID);

            assertSame(bug, byId.get("1001"));
            assertEquals("Task", byId.get(ClickUpCustomItem.DEFAULT_TASK_ID).getName());
        }

        @Test
        @DisplayName("The types are reused for 10 minutes")
        void getCustomItemsById_shouldReuseRecentTypes() throws IOException {
            long[] now = {1_000_000};
            ClickUpTaskService cachingService = new ClickUpTaskService(apiClient, () -> now[0]);
            when(apiClient.fetchCustomItems(TEST_WORKSPACE_ID)).thenReturn(List.of());

            cachingService.getCustomItemsById(TEST_WORKSPACE_ID);
            now[0] += ClickUpTaskService.CUSTOM_ITEMS_CACHE_MILLIS - 1;
            cachingService.getCustomItemsById(TEST_WORKSPACE_ID);
            verify(apiClient, times(1)).fetchCustomItems(TEST_WORKSPACE_ID);

            now[0] += 1;
            cachingService.getCustomItemsById(TEST_WORKSPACE_ID);
            verify(apiClient, times(2)).fetchCustomItems(TEST_WORKSPACE_ID);
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

