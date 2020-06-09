package com.swarm.toolWindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class SwarmToolWindowFactory implements ToolWindowFactory {
    private static ToolWindow productWindow;
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        productWindow = toolWindow;
        ProductToolWindow productToolWindow = new ProductToolWindow(toolWindow);
        LoginToolWindow loginToolWindow = new LoginToolWindow(toolWindow);
        RegisterToolWindow registerToolWindow = new RegisterToolWindow(toolWindow);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(loginToolWindow.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }


}
