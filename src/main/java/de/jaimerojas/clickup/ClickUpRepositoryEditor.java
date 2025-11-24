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
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.config.BaseRepositoryEditor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.Consumer;
import com.intellij.util.ui.FormBuilder;
import de.jaimerojas.clickup.model.ClickUpTeamMember;
import de.jaimerojas.clickup.model.ClickUpUser;
import de.jaimerojas.clickup.model.ClickUpWorkspace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;

public class ClickUpRepositoryEditor extends BaseRepositoryEditor<ClickUpRepository> {
    private static final Logger LOG = Logger.getInstance(ClickUpRepositoryEditor.class);

    private JBPasswordField myApiTokenField;
    private ComboBox<ClickUpWorkspace> myWorkspaceDropdown;
    private ComboBox<ClickUpUser> myAssigneeDropdown;
    private JCheckBox myUseCustomTaskIdsCheckBox;

    public ClickUpRepositoryEditor(
            Project project,
            ClickUpRepository repository,
            Consumer<? super ClickUpRepository> changeListener
    ) {
        super(project, repository, changeListener);

        myUrlLabel.setVisible(false);
        myURLText.setVisible(false);
        myUsernameLabel.setVisible(false);
        myUserNameText.setVisible(false);
        myPasswordLabel.setVisible(false);
        myPasswordText.setVisible(false);
        myShareUrlCheckBox.setVisible(false);

        myTestButton.setEnabled(StringUtil.isNotEmpty(myRepository.getPassword()));
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return myApiTokenField;
    }

    @Override
    protected JComponent createCustomPanel() {
        myApiTokenField = new JBPasswordField();
        myApiTokenField.setText(myRepository.getPassword());
        myApiTokenField.getEmptyText().setText(ClickUpBundle.message("label.api.token.hint"));
        JPanel myApiTokenPanel = new JPanel(new BorderLayout(5, 0));
        myApiTokenPanel.add(myApiTokenField, BorderLayout.CENTER);

        myWorkspaceDropdown = new ComboBox<>();
        JPanel myWorkspacePanel = new JPanel(new BorderLayout(5, 0));
        myWorkspacePanel.add(myWorkspaceDropdown, BorderLayout.CENTER);

        myAssigneeDropdown = new ComboBox<>();
        JPanel myAssigneePanel = new JPanel(new BorderLayout(5, 5));
        myAssigneePanel.add(myAssigneeDropdown, BorderLayout.CENTER);

        myUseCustomTaskIdsCheckBox = new JCheckBox(ClickUpBundle.message("label.use.custom.task.ids"));
        myAssigneePanel.add(myUseCustomTaskIdsCheckBox, BorderLayout.EAST);

        if (myRepository.isConfigured() && canConnectToClickUp()) {
            loadWorkspaces();
            loadAssignees();
            loadUseCustomTaskIds();
        }

        myApiTokenField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // update password in repository with new myApiTokenField value
                char[] apiToken = myApiTokenField.getPassword();
                myRepository.setPassword(apiToken != null && apiToken.length > 0 ? String.valueOf(apiToken) : null);
                myRepository.setTaskService(null);
                if (myRepository.isConfigured() && canConnectToClickUp()) {
                    myWorkspaceDropdown.setEnabled(true);
                    myAssigneeDropdown.setEnabled(true);
                    loadWorkspaces();
                    loadAssignees();
                    loadUseCustomTaskIds();
                } else {
                    myWorkspaceDropdown.setEnabled(false);
                    myAssigneeDropdown.setEnabled(false);
                }
                if (StringUtil.isNotEmpty(myRepository.getPassword())) {
                    myTestButton.setEnabled(true);
                }
            }
        });
        myWorkspaceDropdown.addActionListener(e -> loadAssignees());

        installListener(myApiTokenField);
        installListener(myWorkspaceDropdown);
        installListener(myAssigneeDropdown);
        installListener(myUseCustomTaskIdsCheckBox);

        // Use FormBuilder to create the panel
        return FormBuilder.createFormBuilder()
                .setAlignLabelOnRight(true)
                .addLabeledComponent(new JBLabel(ClickUpBundle.message("label.clickup.api.token"), SwingConstants.RIGHT), myApiTokenPanel)
                .addLabeledComponent(new JBLabel(ClickUpBundle.message("label.clickup.workspace"), SwingConstants.RIGHT), myWorkspacePanel)
                .addLabeledComponent(new JBLabel(ClickUpBundle.message("label.clickup.assignedTo"), SwingConstants.RIGHT), myAssigneePanel)
                .getPanel();
    }

    private boolean canConnectToClickUp() {
        boolean canConnect;
        try {
            myRepository.getTaskService().testConnection();
            canConnect = true;
        } catch (IOException e) {
            canConnect = false;
            LOG.warn("Connection test failed", e);
        }
        return canConnect;
    }

    @Override
    public void apply() {
        super.apply();
        // Save selected workspace, space, and list
        if (myRepository != null) {
            char[] apiToken = myApiTokenField.getPassword();
            myRepository.setPassword(apiToken != null && apiToken.length > 0 ? String.valueOf(apiToken) : null);

            ClickUpWorkspace clickUpWorkspace = (ClickUpWorkspace) myWorkspaceDropdown.getSelectedItem();
            if (clickUpWorkspace != null) {
                LOG.info("Selected workspace: " + clickUpWorkspace.getId());
                myRepository.setSelectedWorkspaceId(clickUpWorkspace.getId());
            }
            ClickUpUser user = (ClickUpUser) myAssigneeDropdown.getSelectedItem();
            if (user != null) {
                LOG.info("Selected assignee: " + user.getId());
                myRepository.setSelectedAssigneeId(user.getId());
            }
            myRepository.setUseCustomTaskIds(myUseCustomTaskIdsCheckBox.isSelected());
        }
    }

    private void loadWorkspaces() {
        try {
            myWorkspaceDropdown.removeAllItems();
            myWorkspaceDropdown.addItem(new ClickUpWorkspace("-1", "<Workspace>"));
            if (StringUtil.isEmpty(myRepository.getPassword())) return;
            for (ClickUpWorkspace workspace : myRepository.fetchWorkspaces()) {
                myWorkspaceDropdown.addItem(workspace);
                if (workspace.getId().equals(myRepository.getSelectedWorkspaceId()))
                    myWorkspaceDropdown.setSelectedItem(workspace);
            }
        } catch (IOException e) {
            LOG.error("Failed to fetch ClickUp data", e);
            //create an error label and add below myPasswordTest
            myApiTokenField.add(new JLabel("Failed to fetch ClickUp data"));
            // remove automatically after 5 seconds
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            myApiTokenField.remove(0);
                        }
                    },
                    5000
            );
        }
    }

    private void loadAssignees() {
        ClickUpWorkspace selectedClickUpWorkspace = (ClickUpWorkspace) myWorkspaceDropdown.getSelectedItem();
        myAssigneeDropdown.removeAllItems();
        myAssigneeDropdown.addItem(new ClickUpUser("-1", "<Assignee>", "-"));
        if (selectedClickUpWorkspace == null || selectedClickUpWorkspace.getId().equals("-1")) {
            myAssigneeDropdown.setEnabled(false);
            return;
        }
        myAssigneeDropdown.setEnabled(true);
        for (ClickUpTeamMember member : selectedClickUpWorkspace.getMembers()) {
            myAssigneeDropdown.addItem(member.getUser());
            if (member.getUser().getId().equals(myRepository.getSelectedAssigneeId()))
                myAssigneeDropdown.setSelectedItem(member.getUser());
        }
    }

    private void loadUseCustomTaskIds() {
        myUseCustomTaskIdsCheckBox.setSelected(myRepository.isUseCustomTaskIds());
    }
}
