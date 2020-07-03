package com.swarm.toolWindow;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.swarm.models.Developer;

import javax.swing.*;

public class LoginToolWindow implements DumbAware {

    private JPanel loginWindowContent;
    private JPanel loginContent;
    private JButton loginButton;
    private JButton signUpButton;
    private JTextField usernameTextField;

    private final ToolWindow toolWindow;

    public LoginToolWindow(ToolWindow toolWindow, Project project){

        this.toolWindow = toolWindow;

        signUpButton.addActionListener(actionEvent -> {
            switchToolWindowContentToRegisterToolWindow(new RegisterToolWindow(toolWindow, project));
        });
        loginButton.addActionListener(actionEvent -> {
            Developer developer = new Developer();
            developer.setUsername(usernameTextField.getText());
            developer.login();
            switchToolWindowContentToProductToolWindow(new ProductToolWindow(toolWindow, project, developer));
        });
    }

    private void switchToolWindowContentToProductToolWindow(ProductToolWindow productToolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(productToolWindow.getContent(), "", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);
    }

    private void switchToolWindowContentToRegisterToolWindow(RegisterToolWindow registerToolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(registerToolWindow.getContent(), "", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);
    }

    public JPanel getContent() {
        return loginWindowContent;
    }
}
