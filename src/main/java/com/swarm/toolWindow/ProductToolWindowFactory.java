package com.swarm.toolWindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class ProductToolWindowFactory implements ToolWindowFactory {
    private static ToolWindow productWindow;
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        productWindow = toolWindow;
        ProductToolWindow productToolWindow = new ProductToolWindow(toolWindow);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(productToolWindow.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public static void showWindow() {
        if(productWindow != null) {
            ApplicationManager.getApplication().invokeLater(() -> productWindow.show(null));
        }
    }
}
