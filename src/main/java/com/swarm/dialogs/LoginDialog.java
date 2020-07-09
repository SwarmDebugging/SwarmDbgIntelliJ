package com.swarm.dialogs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.swarm.toolWindow.ProductToolWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginDialog extends DialogWrapper {

    private final JPanel panel = new JPanel(new GridBagLayout());
    private final JTextField usernameField = new JTextField();

    private final Action myRegisterAction = new RegisterAction();

    public LoginDialog(Project project) {
        super(project);
        init();
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
        ProductToolWindow.getDeveloper().setUsername(usernameField.getText());

        ProductToolWindow.getDeveloper().login();

        if(ProductToolWindow.getDeveloper().getId() != 0) {
            super.doOKAction();
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

            ProductToolWindow.getDeveloper().setUsername(usernameField.getText());

            ProductToolWindow.getDeveloper().registerNewDeveloper();

            if(ProductToolWindow.getDeveloper().getId() != 0) {
                close(OK_EXIT_CODE, true);
            }
        }
    }
}
