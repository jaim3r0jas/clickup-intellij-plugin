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

import java.util.List;
import java.util.Objects;

public class GetTasks {
    private List<ClickUpTask> tasks;
    // ClickUp's own "no more pages" flag; null when the answer doesn't carry it
    private Boolean last_page;

    public GetTasks() {
    }

    public GetTasks(List<ClickUpTask> tasks, Boolean lastPage) {
        this.tasks = tasks;
        this.last_page = lastPage;
    }

    public List<ClickUpTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<ClickUpTask> tasks) {
        this.tasks = tasks;
    }

    public Boolean getLast_page() {
        return last_page;
    }

    public void setLast_page(Boolean last_page) {
        this.last_page = last_page;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof GetTasks getTasks)) return false;

        return Objects.equals(tasks, getTasks.tasks) && Objects.equals(last_page, getTasks.last_page);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tasks, last_page);
    }
}
