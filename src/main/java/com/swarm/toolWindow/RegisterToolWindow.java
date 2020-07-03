package com.swarm.toolWindow;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.swarm.models.Developer;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RegisterToolWindow implements DumbAware {
    private JPanel registerWindowContent;
    private JPanel registerContent;
    private JButton registerButton;
    private JTextField usernameTextField;
    private JLabel back;

    private final ToolWindow toolWindow;

    public RegisterToolWindow(ToolWindow toolWindow, Project project) {

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
            Developer developer = new Developer();
            developer.setUsername(usernameTextField.getText());
            developer.registerNewDeveloper();
            if(developer.getId() == -1) {
                //TODO: show wrong username notification
            } else {
                switchToolWindowContentToProductToolWindow(new ProductToolWindow(toolWindow, project, developer));
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
