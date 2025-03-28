package de.safenow.clickup.ideaclickup.model;

import java.util.List;
import java.util.Objects;

public class GetFolderlessLists {
    private List<ClickUpList> lists;

    public List<ClickUpList> getLists() {
        return lists;
    }

    public void setLists(List<ClickUpList> lists) {
        this.lists = lists;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof GetFolderlessLists that)) return false;

        return Objects.equals(lists, that.lists);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(lists);
    }
}
