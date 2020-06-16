package com.swarm.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.swarm.States;
import com.swarm.tools.HTTPRequests;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RegisterToolWindow {
    private JPanel registerWindowContent;
    private JPanel registerContent;
    private JButton registerButton;
    private JTextField usernameTextfield;
    private JLabel back;

    private ToolWindow toolWindow;
    private Project project;

    public RegisterToolWindow(ToolWindow toolWindow, Project project) {

        this.project = project;
        this.toolWindow = toolWindow;

       back.setIcon(IconLoader.getIcon("/icons/back.svg"));
       back.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                switchToolWindowContentToLoginToolWindow(new LoginToolWindow(toolWindow, project));
            }
        });
        registerButton.addActionListener(actionEvent -> {
            int developerId = HTTPRequests.createDeveloper(usernameTextfield.getText());
            if(developerId == -1) {
                //show wrong username notification
            } else {
                switchToolWindowContentToProductToolWindow(new ProductToolWindow(toolWindow, project, developerId));
            }
        });
    }

    private void switchToolWindowContentToProductToolWindow(ProductToolWindow productToolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(productToolWindow.getContent(), "", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);
    }

    private void switchToolWindowContentToLoginToolWindow(LoginToolWindow loginToolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(loginToolWindow.getContent(), "", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);
    }

    public JPanel getContent() {
        return registerWindowContent;
    }
}
