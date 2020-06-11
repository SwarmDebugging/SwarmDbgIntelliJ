package com.swarm.toolWindow;

import com.intellij.openapi.project.Project;
import com.swarm.tools.HTTPRequests;

import javax.swing.*;

public class PopupMenuBuilder {

    public static JPopupMenu buildProductPopupMenu(int productId, Project project, int developerId) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem createNewTask = new JMenuItem("Create a new Task");
        createNewTask.addActionListener(actionEvent -> {
            CreateTaskDialog createTaskDialog = new CreateTaskDialog(project, productId, developerId);//States.developerId);
            createTaskDialog.showAndGet();
        });
        popupMenu.add(createNewTask);
        return popupMenu;
    }

    public static JPopupMenu buildTaskPopupMenu(Project project, int taskId) {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem markAsDone = new JMenuItem("Mark As Done");
        markAsDone.addActionListener(actionEvent -> {
            HTTPRequests.taskDone(taskId);
        });
        popupMenu.add(markAsDone);
        return popupMenu;
    }
}
