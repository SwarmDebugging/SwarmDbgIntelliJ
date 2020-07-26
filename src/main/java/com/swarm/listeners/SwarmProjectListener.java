package com.swarm.listeners;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.Messages;
import com.swarm.toolWindow.ProductToolWindow;
import icons.SwarmIcons;
import org.jetbrains.annotations.NotNull;

public class SwarmProjectListener implements ProjectManagerListener {

    @Override
    public void projectClosing(@NotNull Project project) {
        if(ProductToolWindow.getCurrentSessionId() != 0) {
            if(Messages.showYesNoDialog(project, "You have a swarm session in progress, do you want to stop it?",
                    "Session In Progress", SwarmIcons.Ant) == Messages.YES) {
                ProductToolWindow.getCurrentSession().stop();
            }
        }
    }
}
