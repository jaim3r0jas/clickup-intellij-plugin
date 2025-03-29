package de.jaimerojas.clickup.model;

import java.util.List;
import java.util.Objects;

// FIXME: API call get tasks is limited to 100 records, add filtering mechanisms to narrow
//  down results and add support to multipage results
public class GetTasks {
    private List<ClickUpTask> tasks;

    public List<ClickUpTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<ClickUpTask> tasks) {
        this.tasks = tasks;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof GetTasks getTasks)) return false;

        return Objects.equals(tasks, getTasks.tasks);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tasks);
    }
}
