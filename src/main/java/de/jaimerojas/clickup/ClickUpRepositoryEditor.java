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

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.config.BaseRepositoryEditor;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.ActionLink;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.Consumer;
import com.intellij.util.ui.FormBuilder;
import de.jaimerojas.clickup.api.ClickUpApiException;
import de.jaimerojas.clickup.model.ClickUpTeamMember;
import de.jaimerojas.clickup.model.ClickUpUser;
import de.jaimerojas.clickup.model.ClickUpWorkspace;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.Objects;

public class ClickUpRepositoryEditor extends BaseRepositoryEditor<ClickUpRepository> {
    private static final Logger LOG = Logger.getInstance(ClickUpRepositoryEditor.class);
    private static final String NO_SELECTION_ID = "-1";

    private JBPasswordField myApiTokenField;
    private JBLabel myErrorLabel;
    private ComboBox<ClickUpWorkspace> myWorkspaceDropdown;
    private ComboBox<ClickUpUser> myAssigneeDropdown;
    private JCheckBox myUseCustomTaskIdsCheckBox;

    // No field initializers below: BaseRepositoryEditor's constructor calls createCustomPanel() before they would run.
    // Each load from ClickUp gets a number; a slower, older load that finishes later is dropped (EDT only).
    private int myLoadGeneration;
    // the token the dropdowns were last loaded for, and whether that load succeeded
    private String myLoadedToken;
    private boolean myWorkspacesLoaded;

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
        // #42: ClickUp's page for personal API tokens, one click away
        ActionLink generateTokenLink = new ActionLink(ClickUpBundle.message("link.clickup.generate.token"),
                (ActionListener) e -> BrowserUtil.browse(ClickUpRepositoryType.API_TOKEN_SETTINGS_URL));
        generateTokenLink.setExternalLinkIcon();
        generateTokenLink.setToolTipText(ClickUpRepositoryType.API_TOKEN_SETTINGS_URL);
        myApiTokenPanel.add(generateTokenLink, BorderLayout.EAST);

        myErrorLabel = new JBLabel();
        myErrorLabel.setForeground(JBColor.RED);
        myErrorLabel.setVisible(false);

        myWorkspaceDropdown = new ComboBox<>();
        JPanel myWorkspacePanel = new JPanel(new BorderLayout(5, 0));
        myWorkspacePanel.add(myWorkspaceDropdown, BorderLayout.CENTER);

        myAssigneeDropdown = new ComboBox<>();
        JPanel myAssigneePanel = new JPanel(new BorderLayout(5, 5));
        myAssigneePanel.add(myAssigneeDropdown, BorderLayout.CENTER);

        myUseCustomTaskIdsCheckBox = new JCheckBox(ClickUpBundle.message("label.use.custom.task.ids"));
        myUseCustomTaskIdsCheckBox.setSelected(myRepository.isUseCustomTaskIds());
        myAssigneePanel.add(myUseCustomTaskIdsCheckBox, BorderLayout.EAST);

        showWorkspaces(List.of());
        loadWorkspaces();

        myApiTokenField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String token = currentToken();
                myTestButton.setEnabled(StringUtil.isNotEmpty(token));
                // compared with the token of the last load, not the repository's: apply() runs on every keystroke
                // and has stored the new token already
                if (Objects.equals(token, myLoadedToken)) {
                    return;
                }
                // a new token: the repository builds a new ClickUp client for it
                myRepository.setPassword(token);
                loadWorkspaces();
            }
        });
        myWorkspaceDropdown.addActionListener(e -> showAssignees());

        installListener(myApiTokenField);
        installListener(myWorkspaceDropdown);
        installListener(myAssigneeDropdown);
        installListener(myUseCustomTaskIdsCheckBox);

        return FormBuilder.createFormBuilder()
                .setAlignLabelOnRight(true)
                .addLabeledComponent(new JBLabel(ClickUpBundle.message("label.clickup.api.token"), SwingConstants.RIGHT), myApiTokenPanel)
                .addComponentToRightColumn(myErrorLabel)
                .addLabeledComponent(new JBLabel(ClickUpBundle.message("label.clickup.workspace"), SwingConstants.RIGHT), myWorkspacePanel)
                .addLabeledComponent(new JBLabel(ClickUpBundle.message("label.clickup.assignedTo"), SwingConstants.RIGHT), myAssigneePanel)
                .getPanel();
    }

    @Override
    public void apply() {
        super.apply();
        if (myRepository == null) {
            return;
        }
        myRepository.setPassword(currentToken());

        // Until the dropdowns hold what ClickUp sent (still loading, or the load failed), keep the saved selection.
        // Once loaded, the <Workspace> and <Assignee> placeholders clear it.
        if (myWorkspacesLoaded) {
            myRepository.setSelectedWorkspaceId(selectedId((ClickUpWorkspace) myWorkspaceDropdown.getSelectedItem()));
            ClickUpUser user = (ClickUpUser) myAssigneeDropdown.getSelectedItem();
            myRepository.setSelectedAssigneeId(user == null || NO_SELECTION_ID.equals(user.getId()) ? null : user.getId());
        }
        myRepository.setUseCustomTaskIds(myUseCustomTaskIdsCheckBox.isSelected());
    }

    private static String selectedId(ClickUpWorkspace workspace) {
        return workspace == null || NO_SELECTION_ID.equals(workspace.getId()) ? null : workspace.getId();
    }

    private String currentToken() {
        char[] apiToken = myApiTokenField.getPassword();
        return apiToken != null && apiToken.length > 0 ? String.valueOf(apiToken) : null;
    }

    /**
     * Fetches the workspaces for the current token in the background (never on the UI thread: the settings dialog
     * would freeze while ClickUp answers), then fills the dropdowns, or shows why it couldn't.
     */
    private void loadWorkspaces() {
        int generation = ++myLoadGeneration;
        myLoadedToken = myRepository.getPassword();
        myWorkspacesLoaded = false;
        hideError();
        if (StringUtil.isEmpty(myLoadedToken)) {
            showWorkspaces(List.of());
            return;
        }
        myWorkspaceDropdown.setEnabled(false);
        myAssigneeDropdown.setEnabled(false);
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            List<ClickUpWorkspace> workspaces;
            Exception failure = null;
            try {
                workspaces = myRepository.fetchWorkspaces();
            } catch (Exception e) {
                workspaces = List.of();
                failure = e;
                LOG.warn("Failed to fetch ClickUp workspaces", e);
            }
            List<ClickUpWorkspace> result = workspaces;
            Exception error = failure;
            // ModalityState.any(): the settings dialog is modal. A result that arrives after the dialog was closed
            // (disposed: no longer displayable), or after a newer load started, is dropped, so it never changes a
            // repository the user is done with. A hidden card of the dialog is still displayable and gets its result.
            ApplicationManager.getApplication().invokeLater(() -> {
                if (generation != myLoadGeneration || !myApiTokenField.isDisplayable()) {
                    return;
                }
                myWorkspacesLoaded = error == null;
                showWorkspaces(result);
                if (error != null) {
                    showError(error);
                }
            }, ModalityState.any());
        });
    }

    private void showWorkspaces(@NotNull List<ClickUpWorkspace> workspaces) {
        myWorkspaceDropdown.removeAllItems();
        myWorkspaceDropdown.addItem(new ClickUpWorkspace(NO_SELECTION_ID, ClickUpBundle.message("label.clickup.workspace.none")));
        for (ClickUpWorkspace workspace : workspaces) {
            myWorkspaceDropdown.addItem(workspace);
            if (Objects.equals(workspace.getId(), myRepository.getSelectedWorkspaceId())) {
                myWorkspaceDropdown.setSelectedItem(workspace);
            }
        }
        myWorkspaceDropdown.setEnabled(!workspaces.isEmpty());
        showAssignees();
    }

    private void showAssignees() {
        ClickUpWorkspace selectedClickUpWorkspace = (ClickUpWorkspace) myWorkspaceDropdown.getSelectedItem();
        myAssigneeDropdown.removeAllItems();
        myAssigneeDropdown.addItem(new ClickUpUser(NO_SELECTION_ID, ClickUpBundle.message("label.clickup.assignee.none"), "-"));
        if (selectedClickUpWorkspace == null || NO_SELECTION_ID.equals(selectedClickUpWorkspace.getId())
                || selectedClickUpWorkspace.getMembers() == null) {
            myAssigneeDropdown.setEnabled(false);
            return;
        }
        myAssigneeDropdown.setEnabled(true);
        for (ClickUpTeamMember member : selectedClickUpWorkspace.getMembers()) {
            ClickUpUser user = member.getUser();
            if (user == null) {
                continue;
            }
            myAssigneeDropdown.addItem(user);
            if (Objects.equals(user.getId(), myRepository.getSelectedAssigneeId())) {
                myAssigneeDropdown.setSelectedItem(user);
            }
        }
    }

    private void showError(@NotNull Exception error) {
        boolean tokenRejected = error instanceof ClickUpApiException apiError && apiError.isAuthenticationError();
        myErrorLabel.setText(tokenRejected
                ? ClickUpBundle.message("error.clickup.token.rejected")
                : ClickUpBundle.message("error.clickup.workspaces.failed", StringUtil.notNullize(error.getMessage())));
        myErrorLabel.setVisible(true);
    }

    private void hideError() {
        myErrorLabel.setText("");
        myErrorLabel.setVisible(false);
    }
}
