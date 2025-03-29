package de.jaimerojas.clickup.model;

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

public class ClickUpTask extends Task {
    private String id;
    private String name;
    private String description;
    private String date_updated;
    private String date_created;
    private String date_closed;
    private ClickUpTaskState status;
    private String url;
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
        return new ImageIcon(new byte[]{});
    }

    @Override
    public @NotNull TaskType getType() {
        return TaskType.BUG;// clickup does not provide task type in the API (GET /task)
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

    public void setId(String id) {
        this.id = id;
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
}