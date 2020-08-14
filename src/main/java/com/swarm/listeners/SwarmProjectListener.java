package com.swarm.listeners;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.swarm.toolWindow.ProductToolWindow;
import icons.SwarmIcons;
import org.jetbrains.annotations.NotNull;

public class SwarmProjectListener implements ProjectManagerListener {

    @Override
    public void projectClosing(@NotNull Project project) {
        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Swarm Debugging Manager");
        ProductToolWindow productToolWindow = (ProductToolWindow) toolWindow.getContentManager().getContent(0).getComponent();
        if(productToolWindow.getCurrentSession().getId() != 0) {
            if(Messages.showYesNoDialog(project, "You have a swarm session in progress, do you want to stop it?",
                    "Session In Progress", SwarmIcons.Ant) == Messages.YES) {
                productToolWindow.getCurrentSession().stop();
            }
        }
    }
}
