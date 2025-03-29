package de.jaimerojas.clickup;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.TaskBundle;
import com.intellij.tasks.config.BaseRepositoryEditor;
import com.intellij.util.Consumer;
import de.jaimerojas.clickup.model.ClickUpList;
import de.jaimerojas.clickup.model.ClickUpSpace;
import de.jaimerojas.clickup.model.ClickUpWorkspace;

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

    public ClickUpRepositoryEditor(
            final Project project, final ClickUpRepository repository, final Consumer<? super ClickUpRepository> changeListener
    ) {
        super(project, repository, changeListener);

        // Set clickup API url by default
        myURLText.setText(API_URL);
        myPasswordLabel.setText(TaskBundle.message("label.api.token"));

        // Hide unused fields
        myUrlLabel.setVisible(false);
        myURLText.setVisible(false);
        myUsernameLabel.setVisible(false);
        myUserNameText.setVisible(false);

        // Add dropdowns for workspaces, spaces, and lists
        workspaceDropdown = new ComboBox<>();
        spaceDropdown = new ComboBox<>();
        listDropdown = new ComboBox<>();

        JPanel dropdownPanel = new JPanel(new GridLayout(3, 2));
        dropdownPanel.add(new JLabel("Workspace:"));
        dropdownPanel.add(workspaceDropdown);
        dropdownPanel.add(new JLabel("Space:"));
        dropdownPanel.add(spaceDropdown);
        dropdownPanel.add(new JLabel("List:"));
        dropdownPanel.add(listDropdown);

        myCustomPanel.add(dropdownPanel, BorderLayout.CENTER);

        // add if only to load when api token is set
        if (StringUtil.isNotEmpty(myRepository.getPassword())) {
            loadWorkspaces();
            loadSpaces();
            loadLists();
        }

        // add on blur listener to myPasswordText
        myPasswordText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (StringUtil.isNotEmpty(myRepository.getPassword())) {
                    loadWorkspaces();
                    loadSpaces();
                    loadLists();
                }
            }
        });

        // Add action listeners
        workspaceDropdown.addActionListener(e -> {
            myRepository.clearSelectedWorkspace();
            loadSpaces();
        });
        spaceDropdown.addActionListener(e -> {
            myRepository.clearSelectedSpace();
            loadLists();
        });

        installListener(workspaceDropdown);
        installListener(spaceDropdown);
        installListener(listDropdown);

        myTestButton.setEnabled(myRepository.isConfigured());
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
        }
    }

    private void loadWorkspaces() {
        try {
            workspaceDropdown.removeAllItems();
            workspaceDropdown.addItem(new ClickUpWorkspace("-1", "Select workspace"));
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
            spaceDropdown.addItem(new ClickUpSpace("-1", "Select space"));
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
            listDropdown.addItem(new ClickUpList("-1", "Select list"));
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
}