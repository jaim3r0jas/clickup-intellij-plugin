package de.jaimerojas.clickup.model;

import java.util.Objects;

public class ClickUpWorkspace {
    private String id;
    private String name;

    public ClickUpWorkspace(String id, String name) {
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
        return this.name;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ClickUpWorkspace clickUpWorkspace)) return false;

        return Objects.equals(id, clickUpWorkspace.id) && Objects.equals(name, clickUpWorkspace.name);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(name);
        return result;
    }
}
