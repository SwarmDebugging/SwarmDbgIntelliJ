package com.swarm.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.swarm.States;
import com.swarm.tools.HTTPRequests;

import javax.swing.*;

public class SessionToolWindow {
    private JPanel sessionWindowContent;
    private JButton stopSessionButton;

    private ToolWindow toolWindow;
    private Project project;

    public SessionToolWindow(int currentSessionId, ToolWindow toolWindow, Project project, int developerId) {

        this.toolWindow = toolWindow;
        this.project = project;

        stopSessionButton.addActionListener(actionEvent -> {
            HTTPRequests.sessionFinish(currentSessionId);
            States.currentSessionId = -1;
            switchToolWindowContentToProductToolWindow(new ProductToolWindow(toolWindow, project, developerId));
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
