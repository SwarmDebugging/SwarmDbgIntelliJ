package com.swarm.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.swarm.States;
import com.swarm.tools.HTTPRequests;

import javax.swing.*;
import java.awt.event.*;

public class LoginToolWindow {

    private JPanel loginWindowContent;
    private JPanel loginContent;
    private JButton loginButton;
    private JButton signUpButton;
    private JTextField usernameTextfield;

    public LoginToolWindow(ToolWindow toolWindow, Project project) {

        signUpButton.addActionListener(actionEvent -> {
            RegisterToolWindow registerToolWindow = new RegisterToolWindow(toolWindow, project);
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
                ProductToolWindow productToolWindow = new ProductToolWindow(toolWindow, project);
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
