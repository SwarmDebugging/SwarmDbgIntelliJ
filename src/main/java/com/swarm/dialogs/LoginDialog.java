package com.swarm.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.swarm.models.Session;
import com.swarm.services.SessionService;
import com.swarm.toolWindow.ProductToolWindow;
import com.swarm.toolWindow.RecommendationToolWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class LoginDialog extends DialogWrapper {
    private Project project;
    private ProductToolWindow productToolWindow;

    private final JPanel panel = new JPanel(new GridBagLayout());
    private final JTextField usernameField = new JTextField();

    private final Action myRegisterAction = new RegisterAction();

    public LoginDialog(Project project) {
        super(project);
        this.project = project;
        init();

        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Swarm Debugging Manager");
        productToolWindow = (ProductToolWindow) toolWindow.getContentManager().getContent(0).getComponent();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        createLoginPanel();
        return panel;
    }

    private void createLoginPanel() {
        panel.removeAll();
        setTitle("Login");
        setOKButtonText("Login");
        GridBagConstraints constraints = createGridBagConstraints();
        panel.add(userNameLabel(), constraints);
        constraints.gridx = 1;
        panel.add(usernameField, constraints);
        panel.setPreferredSize(new Dimension(400,200));
    }

    private GridBagConstraints createGridBagConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill =GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.2;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridx = 0;
        constraints.gridy = 0;

        return constraints;
    }

    private JComponent userNameLabel() {
        JBLabel label = new JBLabel("Username: ");
        label.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        label.setFontColor(UIUtil.FontColor.BRIGHTER);
        label.setBorder(JBUI.Borders.empty(0,5,2,0));

        return label;
    }

    @Override
    protected void doOKAction() {

        productToolWindow.getDeveloper().setUsername(usernameField.getText());

        productToolWindow.getDeveloper().login();

        if(productToolWindow.getDeveloper().getId() != 0) {
            SessionService sessionService = new SessionService();
            ArrayList<Session> sessions = sessionService.sessionsByDeveloper(productToolWindow.getDeveloper());
            ArrayList<Session> openedSessions = new ArrayList<>();
            for (Session session : sessions) {
                if (!session.isFinished()) {
                    openedSessions.add(session);
                }
            }
            var builder = JBPopupFactory.getInstance().createPopupChooserBuilder(openedSessions);
            builder.setItemChosenCallback(session -> {
                productToolWindow.setCurrentSession(session);
                super.doOKAction();
            });
            builder.setCancelCallback(() -> {
                super.doOKAction();
                return true;
            });
            builder.setTitle("Continue an Unfinished Session?");
            JBPopup popup = builder.setRequestFocus(true).createPopup();
            popup.showInCenterOf(this.panel);
        }
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        super.createActions();
        return new Action[] {myOKAction, myRegisterAction, getCancelAction()};
    }

    private class RegisterAction extends AbstractAction {
        RegisterAction() {
            super();
            putValue(Action.NAME, "Register");
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {

            productToolWindow.getDeveloper().setUsername(usernameField.getText());

            productToolWindow.getDeveloper().registerNewDeveloper();

            if(productToolWindow.getDeveloper().getId() != 0) {
                close(OK_EXIT_CODE, true);
            }
        }
    }
}
