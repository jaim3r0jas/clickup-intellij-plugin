/*
   Copyright 2025 Jaime Enrique Rojas Almonte

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
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
