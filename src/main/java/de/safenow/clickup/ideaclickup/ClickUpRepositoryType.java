package de.safenow.clickup.ideaclickup;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.CustomTaskState;
import com.intellij.tasks.TaskState;
import com.intellij.tasks.impl.BaseRepositoryType;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

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