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

import com.intellij.openapi.util.NlsSafe;
import com.intellij.tasks.Comment;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.TaskType;
import de.jaimerojas.clickup.ClickUpRepository;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Date;

import static de.jaimerojas.clickup.model.ClickUpTaskIconHolder.CLICKUP_ICON;

public class ClickUpTask extends Task {
    private String id;
    private String custom_id;
    private String custom_item_id;
    private String name;
    private String description;
    private String date_updated;
    private String date_created;
    private String date_closed;
    private ClickUpTaskState status;
    private String url;
    private ClickUpSpaceIdOnly space;
    private ClickUpCustomItem customItem;
    private ClickUpRepository taskRepository;

    @Override
    public @Nls @NotNull String getSummary() {
        return name;
    }

    @Override
    public Comment @NotNull [] getComments() {
        return new Comment[0];
    }

    @Override
    public @NotNull Icon getIcon() {
        return CLICKUP_ICON;
    }

    @Override
    public @NotNull TaskType getType() {
        if (customItem != null) {
            var customTaskType = customItem.getName().toLowerCase();
            if (customTaskType.matches("(bug|bugs|issue|issues|defect|defects)")) {
                return TaskType.BUG;
            } else if (customTaskType.matches("(task|tasks|story|stories|user story|user stories|feature|features)")) {
                return TaskType.FEATURE;
            } else if (customTaskType.matches("(ex|exception|exceptions|error|errors|incident|incidents)")) {
                return TaskType.EXCEPTION;
            } else {
                return TaskType.OTHER;
            }
        } else {
            return TaskType.FEATURE;
        }
    }

    @Override
    public @Nullable Date getUpdated() {
        return new Date(Long.parseLong(date_updated));
    }

    @Override
    public @Nullable Date getCreated() {
        return new Date(Long.parseLong(date_created));
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NlsSafe @NotNull String getPresentableId() {
        // If the task is associated with a ClickUpRepository and that repository is configured
        // to use custom task ids, prefer custom_id when present. Otherwise fall back to id.
        if (taskRepository != null && taskRepository.isUseCustomTaskIds()) {
            if (custom_id != null && !custom_id.isEmpty()) {
                return custom_id;
            }
        }
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustom_id() {
        return custom_id;
    }

    public void setCustom_id(String custom_id) {
        this.custom_id = custom_id;
    }

    public String getCustom_item_id() {
        return custom_item_id;
    }

    public void setCustom_item_id(String custom_item_id) {
        this.custom_item_id = custom_item_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public @Nls @Nullable String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate_updated() {
        return date_updated;
    }

    public void setDate_updated(String date_updated) {
        this.date_updated = date_updated;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getDate_closed() {
        return date_closed;
    }

    public void setDate_closed(String date_closed) {
        this.date_closed = date_closed;
    }

    @Override
    public boolean isClosed() {
        return date_closed != null;
    }

    @Override
    public boolean isIssue() {
        return true;
    }

    public ClickUpTaskState getStatus() {
        return status;
    }

    public void setStatus(ClickUpTaskState status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ClickUpSpaceIdOnly getSpace() {
        return space;
    }

    public void setSpace(ClickUpSpaceIdOnly space) {
        this.space = space;
    }

    public ClickUpCustomItem getCustomItem() {
        return customItem;
    }

    public void setCustomItem(ClickUpCustomItem customItem) {
        this.customItem = customItem;
    }

    @Override
    public @Nullable TaskRepository getRepository() {
        return taskRepository;
    }

    public void setRepository(ClickUpRepository clickUpRepository) {
        this.taskRepository = clickUpRepository;
    }

    @Override
    public @Nullable String getIssueUrl() {
        return this.url;
    }

    @Override
    public @Nullable String getProject() {
        return extractProjectFromId(this.getPresentableId());
    }
}