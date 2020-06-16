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

    public RegisterToolWindow(ToolWindow toolWindow, Project project) {

       back.setIcon(IconLoader.getIcon("/icons/back.svg"));
       back.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                LoginToolWindow loginToolWindow = new LoginToolWindow(toolWindow, project);
                ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
                Content content = contentFactory.createContent(loginToolWindow.getContent(), "", false);
                toolWindow.getContentManager().removeAllContents(true);
                toolWindow.getContentManager().addContent(content);
            }
        });
        registerButton.addActionListener(actionEvent -> {
            String username = usernameTextfield.getText();
            int developerId = HTTPRequests.createDeveloper(username);
            if(developerId == -1) {
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
        return registerWindowContent;
    }
}
