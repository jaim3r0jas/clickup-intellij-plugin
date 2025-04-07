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

import org.jetbrains.annotations.NotNull;

public class ClickUpUser {

    @NotNull
    private String id;
    private String username;
    private String email;

    public ClickUpUser(String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public @NotNull String getId() {
        return id;
    }

    public void setId(@NotNull String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return username;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof ClickUpUser that)) return false;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}