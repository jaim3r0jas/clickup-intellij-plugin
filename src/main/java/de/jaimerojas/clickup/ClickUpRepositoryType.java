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
package de.jaimerojas.clickup;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.impl.BaseRepositoryType;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class ClickUpRepositoryType extends BaseRepositoryType<ClickUpRepository> {
    private static final Logger LOG = Logger.getInstance(ClickUpRepositoryType.class);
    private static final Icon icon = new ImageIcon(Objects.requireNonNull(ClickUpRepositoryType.class.getClassLoader().getResource("icons/clickup.png")));

    @Override
    @NotNull
    public String getName() {
        return "ClickUp";
    }

    @Override
    @NotNull
    public Icon getIcon() {
        return icon;
    }

    @Override
    @NotNull
    public ClickUpRepository createRepository() {
        return new ClickUpRepository(this);
    }

    @Override
    public Class<ClickUpRepository> getRepositoryClass() {
        return ClickUpRepository.class;
    }

    @Override
    @NotNull
    public ClickUpRepositoryEditor createEditor(ClickUpRepository repository, Project project, Consumer<? super ClickUpRepository> changeListener) {
        return new ClickUpRepositoryEditor(project, repository, changeListener);
    }

}