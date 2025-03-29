package de.jaimerojas.clickup.model;

import java.util.List;
import java.util.Objects;

public class GetAuthorizedWorkspaces {
    private List<ClickUpWorkspace> teams;

    public List<ClickUpWorkspace> getTeams() {
        return teams;
    }

    public void setTeams(List<ClickUpWorkspace> teams) {
        this.teams = teams;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof GetAuthorizedWorkspaces that)) return false;

        return Objects.equals(teams, that.teams);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(teams);
    }
}
