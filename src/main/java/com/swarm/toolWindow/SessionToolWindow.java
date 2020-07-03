package com.swarm.toolWindow;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.swarm.States;
import com.swarm.models.Developer;
import com.swarm.models.Session;

import javax.swing.*;

public class SessionToolWindow implements DumbAware {
    private JPanel sessionWindowContent;
    private JButton stopSessionButton;

    private final ToolWindow toolWindow;

    public SessionToolWindow(Session currentSession, ToolWindow toolWindow, Project project, Developer developer) {

        this.toolWindow = toolWindow;

        stopSessionButton.addActionListener(actionEvent -> {
            currentSession.stop();
            States.currentSession.setId(-1);
            switchToolWindowContentToProductToolWindow(new ProductToolWindow(toolWindow, project, developer));
        });
    }

    private void switchToolWindowContentToProductToolWindow(ProductToolWindow productToolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(productToolWindow.getContent(), "", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);
    }

    public JPanel getContent() {
        return sessionWindowContent;
    }
}
