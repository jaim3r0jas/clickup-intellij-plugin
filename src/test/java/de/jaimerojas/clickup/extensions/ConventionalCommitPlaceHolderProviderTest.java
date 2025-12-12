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
package de.jaimerojas.clickup.extensions;

import com.intellij.tasks.LocalTask;
import com.intellij.tasks.TaskType;
import de.jaimerojas.clickup.ClickUpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConventionalCommitPlaceHolderProvider}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConventionalCommitPlaceHolderProvider Tests")
class ConventionalCommitPlaceHolderProviderTest {

    private ConventionalCommitPlaceHolderProvider provider;

    @Mock
    private LocalTask mockTask;

    @Mock
    private ClickUpRepository mockRepository;

    @BeforeEach
    void setUp() {
        provider = new ConventionalCommitPlaceHolderProvider();
    }

    @Nested
    @DisplayName("Placeholder Registration")
    class PlaceholderRegistration {

        @Test
        @DisplayName("Should provide all expected placeholders")
        void shouldProvideAllExpectedPlaceholders() {
            String[] placeholders = provider.getPlaceholders(null);

            assertNotNull(placeholders);
            assertEquals(11, placeholders.length);

            assertArrayEquals(
                new String[]{"id", "number", "summary", "project", "taskType",
                            "type", "scope", "description", "body", "footer", "breaking"},
                placeholders
            );
        }

        @Test
        @DisplayName("Should provide descriptions for all placeholders")
        void shouldProvideDescriptionsForAllPlaceholders() {
            String[] placeholders = provider.getPlaceholders(null);

            for (String placeholder : placeholders) {
                String description = provider.getPlaceholderDescription(placeholder);
                assertNotNull(description, "Description should not be null for placeholder: " + placeholder);
                assertFalse(description.isEmpty(), "Description should not be empty for placeholder: " + placeholder);
            }
        }
    }

    @Nested
    @DisplayName("Standard Task Placeholders")
    class StandardTaskPlaceholders {

        @Test
        @DisplayName("Should return presentable ID for 'id' placeholder")
        void shouldReturnPresentableIdForIdPlaceholder() {
            when(mockTask.getPresentableId()).thenReturn("CU-123");

            String result = provider.getPlaceholderValue(mockTask, "id");

            assertEquals("CU-123", result);
        }

        @Test
        @DisplayName("Should return number for 'number' placeholder")
        void shouldReturnNumberForNumberPlaceholder() {
            when(mockTask.getNumber()).thenReturn("456");

            String result = provider.getPlaceholderValue(mockTask, "number");

            assertEquals("456", result);
        }

        @Test
        @DisplayName("Should return summary for 'summary' placeholder")
        void shouldReturnSummaryForSummaryPlaceholder() {
            when(mockTask.getSummary()).thenReturn("Fix login bug");

            String result = provider.getPlaceholderValue(mockTask, "summary");

            assertEquals("Fix login bug", result);
        }

        @Test
        @DisplayName("Should return project for 'project' placeholder")
        void shouldReturnProjectForProjectPlaceholder() {
            when(mockTask.getProject()).thenReturn("Authentication");

            String result = provider.getPlaceholderValue(mockTask, "project");

            assertEquals("Authentication", result);
        }

        @Test
        @DisplayName("Should handle null project gracefully")
        void shouldHandleNullProjectGracefully() {
            when(mockTask.getProject()).thenReturn(null);

            String result = provider.getPlaceholderValue(mockTask, "project");

            assertEquals("", result);
        }

        @Test
        @DisplayName("Should return task type name for 'taskType' placeholder")
        void shouldReturnTaskTypeNameForTaskTypePlaceholder() {
            when(mockTask.getType()).thenReturn(TaskType.FEATURE);

            String result = provider.getPlaceholderValue(mockTask, "taskType");

            assertEquals("FEATURE", result);
        }
    }

    @Nested
    @DisplayName("Conventional Commit Type Mapping")
    class ConventionalCommitTypeMapping {

        @Test
        @DisplayName("Should map FEATURE to 'feat'")
        void shouldMapFeatureToFeat() {
            when(mockTask.getType()).thenReturn(TaskType.FEATURE);

            String result = provider.getPlaceholderValue(mockTask, "type");

            assertEquals("feat", result);
        }

        @Test
        @DisplayName("Should map BUG to 'fix'")
        void shouldMapBugToFix() {
            when(mockTask.getType()).thenReturn(TaskType.BUG);

            String result = provider.getPlaceholderValue(mockTask, "type");

            assertEquals("fix", result);
        }

        @Test
        @DisplayName("Should map EXCEPTION to 'fix'")
        void shouldMapExceptionToFix() {
            when(mockTask.getType()).thenReturn(TaskType.EXCEPTION);

            String result = provider.getPlaceholderValue(mockTask, "type");

            assertEquals("fix", result);
        }

        @Test
        @DisplayName("Should map OTHER to 'chore'")
        void shouldMapOtherToChore() {
            when(mockTask.getType()).thenReturn(TaskType.OTHER);

            String result = provider.getPlaceholderValue(mockTask, "type");

            assertEquals("chore", result);
        }
    }

    @Nested
    @DisplayName("Conventional Commit Scope")
    class ConventionalCommitScope {

        @Test
        @DisplayName("Should derive scope from project name")
        void shouldDeriveScopeFromProjectName() {
            when(mockTask.getProject()).thenReturn("MyProject");

            String result = provider.getPlaceholderValue(mockTask, "scope");

            assertEquals("myproject", result);
        }

        @Test
        @DisplayName("Should replace spaces with hyphens in scope")
        void shouldReplaceSpacesWithHyphensInScope() {
            when(mockTask.getProject()).thenReturn("My Great Project");

            String result = provider.getPlaceholderValue(mockTask, "scope");

            assertEquals("my-great-project", result);
        }

        @Test
        @DisplayName("Should return empty string when project is null")
        void shouldReturnEmptyStringWhenProjectIsNull() {
            when(mockTask.getProject()).thenReturn(null);

            String result = provider.getPlaceholderValue(mockTask, "scope");

            assertEquals("", result);
        }

        @Test
        @DisplayName("Should return empty string when project is empty")
        void shouldReturnEmptyStringWhenProjectIsEmpty() {
            when(mockTask.getProject()).thenReturn("");

            String result = provider.getPlaceholderValue(mockTask, "scope");

            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Conventional Commit Description")
    class ConventionalCommitDescription {

        @Test
        @DisplayName("Should lowercase first character of summary")
        void shouldLowercaseFirstCharacterOfSummary() {
            when(mockTask.getSummary()).thenReturn("Add user authentication");

            String result = provider.getPlaceholderValue(mockTask, "description");

            assertEquals("add user authentication", result);
        }

        @Test
        @DisplayName("Should preserve already lowercase summary")
        void shouldPreserveAlreadyLowercaseSummary() {
            when(mockTask.getSummary()).thenReturn("fix login bug");

            String result = provider.getPlaceholderValue(mockTask, "description");

            assertEquals("fix login bug", result);
        }

        @Test
        @DisplayName("Should handle empty summary")
        void shouldHandleEmptySummary() {
            when(mockTask.getSummary()).thenReturn("");

            String result = provider.getPlaceholderValue(mockTask, "description");

            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Conventional Commit Body")
    class ConventionalCommitBody {

        @Test
        @DisplayName("Should return task description as body")
        void shouldReturnTaskDescriptionAsBody() {
            when(mockTask.getDescription()).thenReturn("This is a detailed description of the task.");

            String result = provider.getPlaceholderValue(mockTask, "body");

            assertEquals("This is a detailed description of the task.", result);
        }

        @Test
        @DisplayName("Should return empty string when description is null")
        void shouldReturnEmptyStringWhenDescriptionIsNull() {
            when(mockTask.getDescription()).thenReturn(null);

            String result = provider.getPlaceholderValue(mockTask, "body");

            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Conventional Commit Footer")
    class ConventionalCommitFooter {

        @Test
        @DisplayName("Should create footer with task reference")
        void shouldCreateFooterWithTaskReference() {
            when(mockTask.getPresentableId()).thenReturn("CU-123abc");
            when(mockTask.getRepository()).thenReturn(mockRepository);

            String result = provider.getPlaceholderValue(mockTask, "footer");

            assertEquals("Refs: CU-123abc", result);
        }

        @Test
        @DisplayName("Should return empty string when task ID is empty")
        void shouldReturnEmptyStringWhenTaskIdIsEmpty() {
            when(mockTask.getPresentableId()).thenReturn("");

            String result = provider.getPlaceholderValue(mockTask, "footer");

            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Breaking Change Placeholder")
    class BreakingChangePlaceholder {

        @Test
        @DisplayName("Should return empty string for 'breaking' placeholder")
        void shouldReturnEmptyStringForBreakingPlaceholder() {
            String result = provider.getPlaceholderValue(mockTask, "breaking");

            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should throw exception for unknown placeholder")
        void shouldThrowExceptionForUnknownPlaceholder() {
            assertThrows(IllegalArgumentException.class,
                () -> provider.getPlaceholderValue(mockTask, "unknown"));
        }
    }
}

