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

import com.intellij.tasks.CustomTaskState;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.Task;
import de.jaimerojas.clickup.model.*;
import de.jaimerojas.clickup.service.ClickUpTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ClickUpRepository}.
 * <p>
 * Note: Some tests are intentionally omitted as they require IntelliJ Application context:
 * - getIssues() tests - require EmptyProgressIndicator initialization
 * - clone() and equals() tests - require PasswordSafe service
 * <p>
 * For full integration testing, use IntelliJ's LightPlatformTestCase.
 * Service layer logic is thoroughly tested in {@link de.jaimerojas.clickup.service.ClickUpTaskServiceTest}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClickUpRepository Tests")
class ClickUpRepositoryTest {

    private static final String TEST_WORKSPACE_ID = "workspace123";
    private static final String TEST_ASSIGNEE_ID = "user456";
    private static final String TEST_API_TOKEN = "test-api-token";
    private static final String TEST_TASK_ID = "task123";
    private static final String TEST_SPACE_ID = "space456";

    @Mock
    private ClickUpTaskService taskService;

    @Mock
    private LocalTask localTask;

    private ClickUpRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ClickUpRepository(new ClickUpRepositoryType());
        repository.setTaskService(taskService);
        repository.setSelectedWorkspaceId(TEST_WORKSPACE_ID);
        repository.setSelectedAssigneeId(TEST_ASSIGNEE_ID);
        repository.setPassword(TEST_API_TOKEN);
    }

    @Nested
    @DisplayName("Task Operations")
    class TaskOperations {

        @Test
        @DisplayName("Should fetch task by ID and set repository reference")
        void findTask_shouldReturnTaskFromService() throws IOException {
            // Arrange
            ClickUpTask expectedTask = new ClickUpTask();
            expectedTask.setId(TEST_TASK_ID);
            expectedTask.setName("Test Task");

            when(taskService.getTask(TEST_TASK_ID, false, TEST_WORKSPACE_ID))
                    .thenReturn(expectedTask);

            // Act
            Task result = repository.findTask(TEST_TASK_ID);

            // Assert
            assertNotNull(result, "Task should not be null");
            assertEquals(TEST_TASK_ID, result.getId());
            assertEquals("Test Task", result.getSummary());
            assertSame(repository, ((ClickUpTask) result).getRepository(),
                    "Task should have repository reference set");
            verify(taskService).getTask(TEST_TASK_ID, false, TEST_WORKSPACE_ID);
        }

        @Test
        @DisplayName("Should return null when task not found")
        void findTask_shouldReturnNullWhenTaskNotFound() throws IOException {
            // Arrange
            when(taskService.getTask(anyString(), anyBoolean(), anyString()))
                    .thenReturn(null);

            // Act
            Task result = repository.findTask("nonexistent");

            // Assert
            assertNull(result, "Should return null for non-existent task");
        }
    }

    @Nested
    @DisplayName("Time Tracking")
    class TimeTracking {

        @Test
        @DisplayName("Should update time spent with correct parameters")
        void updateTimeSpent_shouldDelegateToService() throws Exception {
            // Arrange
            when(localTask.getId()).thenReturn(TEST_TASK_ID);

            // Act
            repository.updateTimeSpent(localTask, "2h 30m", "Time tracking comment");

            // Assert
            verify(taskService).updateTimeSpent(TEST_TASK_ID, "2h 30m", TEST_WORKSPACE_ID, false);
        }

        @Test
        @DisplayName("Should handle custom task IDs when enabled")
        void updateTimeSpent_shouldUseCustomTaskIds() throws Exception {
            // Arrange
            repository.setUseCustomTaskIds(true);
            when(localTask.getId()).thenReturn(TEST_TASK_ID);

            // Act
            repository.updateTimeSpent(localTask, "1h 0m", "Quick update");

            // Assert
            verify(taskService).updateTimeSpent(TEST_TASK_ID, "1h 0m", TEST_WORKSPACE_ID, true);
        }
    }

    @Nested
    @DisplayName("Task State Management")
    class TaskStateManagement {

        @Test
        @DisplayName("Should update task state to new status")
        void setTaskState_shouldUpdateTaskStatus() throws Exception {
            // Arrange
            ClickUpTask task = new ClickUpTask();
            task.setId(TEST_TASK_ID);
            task.setName("Test Task");

            CustomTaskState state = new CustomTaskState("state1", "In Progress");

            // Act
            repository.setTaskState(task, state);

            // Assert
            verify(taskService).updateTaskStatus(TEST_TASK_ID, "In Progress", TEST_WORKSPACE_ID, false);
        }

        @Test
        @DisplayName("Should fetch available states from space")
        void getAvailableTaskStates_shouldReturnAllSpaceStatuses() throws Exception {
            // Arrange
            ClickUpTask task = createTaskWithSpace();
            ClickUpSpace space = createSpaceWithStatuses();

            when(taskService.getTask(TEST_TASK_ID, false, TEST_WORKSPACE_ID))
                    .thenReturn(task);
            when(taskService.getSpace(TEST_SPACE_ID))
                    .thenReturn(space);

            // Act
            Set<CustomTaskState> result = repository.getAvailableTaskStates(task);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(s -> s.getPresentableName().equals("To Do")));
            assertTrue(result.stream().anyMatch(s -> s.getPresentableName().equals("In Progress")));
        }

        @Test
        @DisplayName("Should return empty set when space has no statuses")
        void getAvailableTaskStates_shouldReturnEmptySetWhenNoStatuses() throws Exception {
            // Arrange
            ClickUpTask task = createTaskWithSpace();
            ClickUpSpace space = new ClickUpSpace(TEST_SPACE_ID, "Empty Space");
            space.setStatuses(Collections.emptyList());

            when(taskService.getTask(TEST_TASK_ID, false, TEST_WORKSPACE_ID))
                    .thenReturn(task);
            when(taskService.getSpace(TEST_SPACE_ID))
                    .thenReturn(space);

            // Act
            Set<CustomTaskState> result = repository.getAvailableTaskStates(task);

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty(), "Should return empty set when space has no statuses");
        }

        private ClickUpTask createTaskWithSpace() {
            ClickUpTask task = new ClickUpTask();
            task.setId(TEST_TASK_ID);
            ClickUpSpaceIdOnly spaceIdOnly = new ClickUpSpaceIdOnly(TEST_SPACE_ID);
            task.setSpace(spaceIdOnly);
            return task;
        }

        private ClickUpSpace createSpaceWithStatuses() {
            ClickUpSpace space = new ClickUpSpace(TEST_SPACE_ID, "Test Space");

            ClickUpTaskState state1 = new ClickUpTaskState();
            state1.setId("state1");
            state1.setStatus("To Do");

            ClickUpTaskState state2 = new ClickUpTaskState();
            state2.setId("state2");
            state2.setStatus("In Progress");

            space.setStatuses(Arrays.asList(state1, state2));
            return space;
        }
    }

    @Nested
    @DisplayName("Workspace Management")
    class WorkspaceManagement {

        @Test
        @DisplayName("Should fetch all workspaces")
        void fetchWorkspaces_shouldReturnAllWorkspaces() throws IOException {
            // Arrange
            List<ClickUpWorkspace> expectedWorkspaces = Arrays.asList(
                    new ClickUpWorkspace("w1", "Workspace 1"),
                    new ClickUpWorkspace("w2", "Workspace 2")
            );

            when(taskService.getWorkspaces()).thenReturn(expectedWorkspaces);

            // Act
            List<ClickUpWorkspace> result = repository.fetchWorkspaces();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Workspace 1", result.get(0).getName());
            assertEquals("Workspace 2", result.get(1).getName());
            verify(taskService).getWorkspaces();
        }

        @Test
        @DisplayName("Should return empty list when no workspaces available")
        void fetchWorkspaces_shouldReturnEmptyListWhenNoneAvailable() throws IOException {
            // Arrange
            when(taskService.getWorkspaces()).thenReturn(Collections.emptyList());

            // Act
            List<ClickUpWorkspace> result = repository.fetchWorkspaces();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Configuration")
    class Configuration {

        @Test
        @DisplayName("Should support state updating feature")
        void getFeatures_shouldIncludeStateUpdating() {
            // Act
            int features = repository.getFeatures();

            // Assert
            assertTrue((features & ClickUpRepository.STATE_UPDATING) != 0,
                    "Should support STATE_UPDATING feature");
        }

        @Test
        @DisplayName("Should support time management feature")
        void getFeatures_shouldIncludeTimeManagement() {
            // Act
            int features = repository.getFeatures();

            // Assert
            assertTrue((features & ClickUpRepository.TIME_MANAGEMENT) != 0,
                    "Should support TIME_MANAGEMENT feature");
        }

        @Test
        @DisplayName("Should create cancellable connection")
        void createCancellableConnection_shouldReturnValidConnection() {
            // Act
            ClickUpRepository.CancellableConnection connection = repository.createCancellableConnection();

            // Assert
            assertNotNull(connection, "Connection should not be null");
        }

        @Test
        @DisplayName("Should get repository URL")
        void getUrl_shouldReturnClickUpApiUrl() {
            // Act
            String url = repository.getUrl();

            // Assert
            assertEquals("https://api.clickup.com/api/v2", url);
        }
    }
}