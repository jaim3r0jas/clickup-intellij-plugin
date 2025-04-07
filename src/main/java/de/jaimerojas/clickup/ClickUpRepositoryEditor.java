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
package de.jaimerojas.clickup;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.TaskBundle;
import com.intellij.tasks.config.BaseRepositoryEditor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.util.Consumer;
import de.jaimerojas.clickup.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;

import static de.jaimerojas.clickup.ClickUpRepository.API_URL;

public class ClickUpRepositoryEditor extends BaseRepositoryEditor<ClickUpRepository> {
    private static final Logger LOG = Logger.getInstance(ClickUpRepositoryEditor.class);

    private final ComboBox<ClickUpWorkspace> workspaceDropdown;
    private final ComboBox<ClickUpSpace> spaceDropdown;
    private final ComboBox<ClickUpList> listDropdown;
    private final ComboBox<ClickUpUser> assigneeDropdown;

    public ClickUpRepositoryEditor(
            final Project project, final ClickUpRepository repository, final Consumer<? super ClickUpRepository> changeListener
    ) {
        super(project, repository, changeListener);

        myURLText.setText(API_URL); // fixed to ClickUp V2 API
        myPasswordLabel.setText(TaskBundle.message("label.api.token"));

        // hide unnecessary fields
        myUrlLabel.setVisible(false);
        myURLText.setVisible(false);
        myUsernameLabel.setVisible(false);
        myUserNameText.setVisible(false);

        // build custom fields
        workspaceDropdown = new ComboBox<>();
        spaceDropdown = new ComboBox<>();
        listDropdown = new ComboBox<>();
        assigneeDropdown = new ComboBox<>();

        if (StringUtil.isNotEmpty(myRepository.getPassword())) {
            loadWorkspaces();
            loadSpaces();
            loadLists();
            loadAssignees();
        }

        myPasswordText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (StringUtil.isNotEmpty(myRepository.getPassword())) {
                    myTestButton.setEnabled(true);
                    loadWorkspaces();
                    loadSpaces();
                    loadLists();
                    loadAssignees();
                }
            }
        });
        workspaceDropdown.addActionListener(e -> {
            myRepository.clearSelectedWorkspace();
            loadSpaces();
            loadAssignees();
        });
        spaceDropdown.addActionListener(e -> {
            myRepository.clearSelectedSpace();
            loadLists();
        });

        installListener(workspaceDropdown);
        installListener(spaceDropdown);
        installListener(listDropdown);
        installListener(assigneeDropdown);

        myTestButton.setEnabled(StringUtil.isNotEmpty(myRepository.getPassword()));


        JBPanel<?> inlinePanel = new JBPanel<>(new FlowLayout());
        // set no border and no padding and no margin to inlinePanel
        inlinePanel.setBorder(BorderFactory.createEmptyBorder());
        inlinePanel.add(workspaceDropdown);
        inlinePanel.add(new JBLabel("/"));
        inlinePanel.add(spaceDropdown);
        inlinePanel.add(new JBLabel("/"));
        inlinePanel.add(listDropdown);

        Container parent = myCustomPanel.getParent();

        final GridConstraints constraints = new GridConstraints();
        constraints.setRow(7);
        constraints.setColumn(0);
        constraints.setAnchor(GridConstraints.ANCHOR_EAST);
        parent.add(new JBLabel("Select:"), constraints.clone());

        constraints.setColumn(1);
        constraints.setAnchor(GridConstraints.ANCHOR_WEST);
        parent.add(inlinePanel, constraints.clone());

        constraints.setRow(8);
        constraints.setColumn(0);
        constraints.setAnchor(GridConstraints.ANCHOR_EAST);
        parent.add(new JBLabel("Assignee:"), constraints.clone());

        constraints.setColumn(1);
        constraints.setAnchor(GridConstraints.ANCHOR_WEST);
        parent.add(assigneeDropdown, constraints.clone());
    }

    @Override
    public void apply() {
        super.apply();
        // Save selected workspace, space, and list
        if (myRepository != null) {
            ClickUpWorkspace clickUpWorkspace = (ClickUpWorkspace) workspaceDropdown.getSelectedItem();
            if (clickUpWorkspace != null) {
                LOG.info("Selected workspace: " + clickUpWorkspace.getId());
                myRepository.setSelectedWorkspaceId(clickUpWorkspace.getId());
            }
            ClickUpSpace clickUpSpace = (ClickUpSpace) spaceDropdown.getSelectedItem();
            if (clickUpSpace != null) {
                LOG.info("Selected space: " + clickUpSpace.getId());
                myRepository.setSelectedSpaceId(clickUpSpace.getId());
            }
            ClickUpList list = (ClickUpList) listDropdown.getSelectedItem();
            if (list != null) {
                LOG.info("Selected list: " + list.getId());
                myRepository.setSelectedListId(list.getId());

            }
            ClickUpUser user = (ClickUpUser) assigneeDropdown.getSelectedItem();
            if (user != null) {
                LOG.info("Selected assignee: " + user.getId());
                myRepository.setSelectedAssigneeId(user.getId());
            }
        }
    }

    private void loadWorkspaces() {
        try {
            workspaceDropdown.removeAllItems();
            workspaceDropdown.addItem(new ClickUpWorkspace("-1", "<Workspace>"));
            for (ClickUpWorkspace clickUpWorkspace : myRepository.fetchWorkspaces()) {
                workspaceDropdown.addItem(clickUpWorkspace);
                if (clickUpWorkspace.getId().equals(myRepository.getSelectedWorkspaceId()))
                    workspaceDropdown.setSelectedItem(clickUpWorkspace);
            }
        } catch (IOException e) {
            LOG.error("Failed to fetch ClickUp data", e);
            //create an error label and add below myPasswordTest
            myPasswordText.add(new JLabel("Failed to fetch ClickUp data"));
            // remove automatically after 5 seconds
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            myPasswordText.remove(0);
                        }
                    },
                    5000
            );
        }
    }

    private void loadSpaces() {
        try {
            ClickUpWorkspace selectedClickUpWorkspace = (ClickUpWorkspace) workspaceDropdown.getSelectedItem();
            spaceDropdown.removeAllItems();
            spaceDropdown.addItem(new ClickUpSpace("-1", "<Space>"));
            if (selectedClickUpWorkspace == null || selectedClickUpWorkspace.getId().equals("-1")) return;
            for (ClickUpSpace clickUpSpace : myRepository.fetchSpaces(selectedClickUpWorkspace.getId())) {
                spaceDropdown.addItem(clickUpSpace);
                if (clickUpSpace.getId().equals(myRepository.getSelectedSpaceId()))
                    spaceDropdown.setSelectedItem(clickUpSpace);
            }
        } catch (IOException e) {
            LOG.error("Failed to fetch ClickUp data", e);
        }
    }

    private void loadLists() {
        try {
            ClickUpSpace selectedClickUpSpace = (ClickUpSpace) spaceDropdown.getSelectedItem();
            listDropdown.removeAllItems();
            listDropdown.addItem(new ClickUpList("-1", "<List>"));
            if (selectedClickUpSpace == null || selectedClickUpSpace.getId().equals("-1")) return;
            for (ClickUpList list : myRepository.fetchLists(selectedClickUpSpace.getId())) {
                listDropdown.addItem(list);
                if (list.getId().equals(myRepository.getSelectedListId()))
                    listDropdown.setSelectedItem(list);
            }
        } catch (IOException e) {
            LOG.error("Failed to fetch ClickUp data", e);
        }
    }

    private void loadAssignees() {
        ClickUpWorkspace selectedClickUpWorkspace = (ClickUpWorkspace) workspaceDropdown.getSelectedItem();
        assigneeDropdown.removeAllItems();
        assigneeDropdown.addItem(new ClickUpUser("-1", "<Assignee>", "-"));
        if (selectedClickUpWorkspace == null || selectedClickUpWorkspace.getId().equals("-1")) return;
        for (ClickUpTeamMember member : selectedClickUpWorkspace.getMembers()) {
            assigneeDropdown.addItem(member.getUser());
            if (member.getUser().getId().equals(myRepository.getSelectedAssigneeId()))
                listDropdown.setSelectedItem(member.getUser());
        }
    }
}