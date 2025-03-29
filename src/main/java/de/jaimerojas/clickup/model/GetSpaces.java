package de.jaimerojas.clickup.model;

import java.util.List;
import java.util.Objects;

public class GetSpaces {
    private List<ClickUpSpace> spaces;

    public List<ClickUpSpace> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<ClickUpSpace> spaces) {
        this.spaces = spaces;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof GetSpaces getSpaces)) return false;

        return Objects.equals(spaces, getSpaces.spaces);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(spaces);
    }
}
