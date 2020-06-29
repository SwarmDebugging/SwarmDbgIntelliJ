package com.swarm.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.swarm.States;
import com.swarm.tools.HTTPUtils;

import javax.swing.*;

public class PopupMenuBuilder {

    JMenuItem createNewTask;
    JPopupMenu productPopupMenu;

    JMenuItem markAsDone;
    JMenuItem newSwarmSession;
    JPopupMenu taskPopupMenu;

    private Project project;
    private ToolWindow toolWindow;
    private int developerId;
    private int taskId;
    private int productId;

    public PopupMenuBuilder(ToolWindow toolWindow, Project project, int developerId) {
        this.project = project;
        this.toolWindow = toolWindow;
        this.developerId = developerId;
    }

    public JPopupMenu buildProductNodePopupMenu(int productId) {
        this.productId = productId;
        buildCreateNewTaskMenuItem();
        return buildProductPopupMenu();
    }

    private JPopupMenu buildProductPopupMenu() {
        productPopupMenu = new JPopupMenu();
        productPopupMenu.add(createNewTask);
        return productPopupMenu;
    }

    private void buildCreateNewTaskMenuItem() {
        createNewTask = new JMenuItem("Create a new Task");
        createNewTask.addActionListener(actionEvent -> {
            CreateTaskDialog createTaskDialog = new CreateTaskDialog(project, productId, developerId);
            createTaskDialog.showAndGet();
        });
    }

    public JPopupMenu buildTaskNodePopupMenu(int taskId) {
        this.taskId = taskId;
        buildMarkAsDoneMenuItem();
        buildNewSwarmSessionMenuItem();
        return buildTaskPopupMenu();
    }

    private JPopupMenu buildTaskPopupMenu() {
        taskPopupMenu = new JPopupMenu();
        taskPopupMenu.add(newSwarmSession);
        taskPopupMenu.add(markAsDone);
        return taskPopupMenu;
    }

    private void buildNewSwarmSessionMenuItem() {
        newSwarmSession = new JMenuItem("Start a New Swarm Debugging Session");
        newSwarmSession.addActionListener(actionEvent -> {
            int sessionId = HTTPUtils.sessionStart(developerId, taskId);
            States.currentSessionId = sessionId;
            switchToolWindowContentToSessionToolWindow(new SessionToolWindow(sessionId, toolWindow, project, developerId));
        });
    }

    private void buildMarkAsDoneMenuItem() {
        markAsDone = new JMenuItem("Mark As Done");
        markAsDone.addActionListener(actionEvent -> {
            HTTPUtils.taskDone(taskId);
        });
    }

    private void switchToolWindowContentToSessionToolWindow(SessionToolWindow sessionToolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(sessionToolWindow.getContent(), "", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);
    }
}
