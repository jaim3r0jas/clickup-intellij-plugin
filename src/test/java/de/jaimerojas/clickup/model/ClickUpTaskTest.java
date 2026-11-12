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
package de.jaimerojas.clickup.model;

import com.google.gson.Gson;
import com.intellij.tasks.TaskType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ClickUpTask}.
 */
@DisplayName("ClickUpTask Tests")
class ClickUpTaskTest {

    @Test
    @DisplayName("Should map the default task custom item to FEATURE")
    void getType_shouldMapDefaultCustomItemToFeature() {
        // Arrange
        ClickUpTask task = new ClickUpTask();
        task.setCustomItem(new ClickUpCustomItem("0", "Task", "Tasks", "Default ClickUp task type", null));

        // Act & Assert
        assertEquals(TaskType.FEATURE, task.getType());
    }

    @Test
    @DisplayName("Should map a Bug custom item to BUG")
    void getType_shouldMapBugCustomItemToBug() {
        // Arrange
        ClickUpTask task = new ClickUpTask();
        task.setCustomItem(new ClickUpCustomItem("1001", "Bug", "Bugs", null, null));

        // Act & Assert
        assertEquals(TaskType.BUG, task.getType());
    }

    @Nested
    @DisplayName("A task with missing fields (#55)")
    class MissingFields {

        @Test
        @DisplayName("An empty task (ClickUp's error body parsed as a task) has an empty ID instead of failing @NotNull")
        void getId_shouldNeverReturnNull() {
            // what the plugin built before #56's fix from {"err":"Task not found, deleted","ECODE":"ITEM_013"}
            ClickUpTask task = new Gson().fromJson("{\"err\":\"Task not found, deleted\",\"ECODE\":\"ITEM_013\"}",
                    ClickUpTask.class);

            assertFalse(task.hasId());
            assertEquals("", task.getId());
            assertEquals("", task.getSummary());
            assertEquals("", task.getPresentableId());
        }

        @Test
        @DisplayName("Missing or malformed dates are no date, not an exception")
        void dates_shouldBeNullWhenMissingOrMalformed() {
            ClickUpTask task = new ClickUpTask();
            assertNull(task.getUpdated());
            assertNull(task.getCreated());

            task.setDate_updated("not-a-number");
            task.setDate_created(" ");
            assertNull(task.getUpdated());
            assertNull(task.getCreated());
        }

        @Test
        @DisplayName("Dates in epoch milliseconds are parsed")
        void dates_shouldParseEpochMillis() {
            ClickUpTask task = new ClickUpTask();
            task.setDate_updated("1700000000000");

            assertEquals(new Date(1700000000000L), task.getUpdated());
        }

        @Test
        @DisplayName("Custom item names map to a type whatever the JVM's locale (Turkish dotless i)")
        void getType_shouldIgnoreDefaultLocale() {
            Locale previous = Locale.getDefault();
            try {
                Locale.setDefault(Locale.forLanguageTag("tr-TR"));
                ClickUpTask task = new ClickUpTask();
                task.setCustomItem(new ClickUpCustomItem("1002", "ISSUE", "ISSUES", null, null));

                assertEquals(TaskType.BUG, task.getType());
            } finally {
                Locale.setDefault(previous);
            }
        }

        @Test
        @DisplayName("A custom item without a name gives the default type instead of an NPE")
        void getType_shouldHandleCustomItemWithoutName() {
            ClickUpTask task = new ClickUpTask();
            task.setCustomItem(new ClickUpCustomItem("1001", null, null, null, null));

            assertEquals(TaskType.FEATURE, task.getType());
        }
    }
}
