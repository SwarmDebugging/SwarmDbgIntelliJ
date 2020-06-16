package com.swarm;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.uiDesigner.core.AbstractLayout;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.swarm.tools.HTTPRequests;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class MyCustomDialog extends DialogWrapper {

    private final JPanel panel = new JPanel(new GridBagLayout());

    public MyCustomDialog(@Nullable Project project) {
        super(project);
        init();
        setTitle("Log into Your Swarm Debugging Account");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        GridBag gb = new GridBag();
        gb.setDefaultInsets(JBUI.insets(0, 0, AbstractLayout.DEFAULT_VGAP, AbstractLayout.DEFAULT_HGAP));

        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        JTextField usernameField = new JTextField();

        JBLabel tryAgain = new JBLabel("Username/Password did not match");
        tryAgain.setVisible(false);

        JButton login = new JButton("login");
        login.addActionListener(actionEvent -> {
            int developerId = HTTPRequests.login(usernameField.getText());
            if(developerId != -1) {
                States.currentDeveloperId = developerId;
                //loggedin popup
                JOptionPane.showMessageDialog(panel, usernameField.getText() + " is now logged in!");
                doOKAction();
            } else {
                //username not valid try again
                tryAgain.setVisible(true);
            }
        });

        gridBagConstraints.fill =GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        panel.add(label("username: "), gridBagConstraints);
        gridBagConstraints.gridx = 1;
        panel.add(usernameField, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        panel.add(login, gridBagConstraints);
        gridBagConstraints.gridx = 1;
        panel.add(new JButton("register"), gridBagConstraints);
        gridBagConstraints.fill = GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        panel.add(tryAgain, gridBagConstraints);

        panel.setPreferredSize(new Dimension(400,200));

        return panel;
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        super.createActions();
        return new Action[]{};
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    private JComponent label(String text) {
        JBLabel label = new JBLabel(text);
        label.setComponentStyle(UIUtil.ComponentStyle.SMALL);
        label.setFontColor(UIUtil.FontColor.BRIGHTER);
        label.setBorder(JBUI.Borders.empty(0,5,2,0));

        return label;
    }
}
