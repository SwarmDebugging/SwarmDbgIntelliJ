package com.swarm.toolWindow;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.swarm.States;
import com.swarm.tools.HTTPRequests;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class LoginToolWindow {

    private JPanel loginWindowContent;
    private JLabel refresh;
    private JPanel loginContent;
    private JButton loginButton;
    private JButton signUpButton;
    private JTextField usernameTextfield;

    public LoginToolWindow(ToolWindow toolWindow) {

        signUpButton.addActionListener(actionEvent -> {
            RegisterToolWindow registerToolWindow = new RegisterToolWindow(toolWindow);
            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
            Content content = contentFactory.createContent(registerToolWindow.getContent(), "", false);
            toolWindow.getContentManager().removeAllContents(true);
            toolWindow.getContentManager().addContent(content);
        });
        loginButton.addActionListener(actionEvent -> {
            String username = usernameTextfield.getText();
            int developerId = HTTPRequests.login(username);
            if (developerId == -1) {
                //show wrong username notification
            } else {
                States.currentDeveloperId = developerId;
                ProductToolWindow productToolWindow = new ProductToolWindow(toolWindow);
                ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
                Content content = contentFactory.createContent(productToolWindow.getContent(), "", false);
                toolWindow.getContentManager().removeAllContents(true);
                toolWindow.getContentManager().addContent(content);
            }

        });
    }

    public JPanel getContent() {
        return loginWindowContent;
    }
}
