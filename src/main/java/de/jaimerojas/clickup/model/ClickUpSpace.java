package de.jaimerojas.clickup.model;

import java.util.List;
import java.util.Objects;

public class ClickUpSpace {
    private String id;
    private String name;
    private List<ClickUpTaskState> statuses;

    public ClickUpSpace(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
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
    public String toString() {
        return name;
    }

    public List<ClickUpTaskState> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<ClickUpTaskState> statuses) {
        this.statuses = statuses;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ClickUpSpace clickUpSpace)) return false;

        return Objects.equals(id, clickUpSpace.id) && Objects.equals(name, clickUpSpace.name);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        return result;
    }
}
