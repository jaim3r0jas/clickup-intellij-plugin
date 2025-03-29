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
