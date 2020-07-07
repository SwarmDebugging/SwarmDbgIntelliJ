package com.swarm.popupMenu;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.swarm.States;
import com.swarm.models.Developer;
import com.swarm.models.Product;
import com.swarm.models.Session;
import com.swarm.models.Task;
import com.swarm.toolWindow.SessionToolWindow;

import javax.swing.*;

public class PopupMenuBuilder {

    JMenuItem createNewTask;
    JPopupMenu productPopupMenu;

    JMenuItem markAsDone;
    JMenuItem newSwarmSession;
    JPopupMenu taskPopupMenu;

    private final Project project;
    private final ToolWindow toolWindow;
    private final Developer developer;
    private Task task;
    private Product product;

    public PopupMenuBuilder(ToolWindow toolWindow, Project project, Developer developer) {
        this.project = project;
        this.toolWindow = toolWindow;
        this.developer = developer;
    }

    public JPopupMenu buildProductNodePopupMenu(Product product) {
        this.product = product;
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
            CreateTaskDialog createTaskDialog = new CreateTaskDialog(project, product, developer);
            createTaskDialog.showAndGet();
        });
    }

    public JPopupMenu buildTaskNodePopupMenu(Task task) {
        this.task = task;
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
            CreateSessionDialog createSessionDialog = new CreateSessionDialog(project);
            if(createSessionDialog.showAndGet()) {
                Session session = new Session();
                session.setTask(task);
                session.setDeveloper(developer);
                session.setDescription(createSessionDialog.getDescription());
                session.start();
                switchToolWindowContentToSessionToolWindow(new SessionToolWindow(session, toolWindow, project, developer));
            }
        });
    }

    private void buildMarkAsDoneMenuItem() {
        markAsDone = new JMenuItem("Mark As Done");
        markAsDone.addActionListener(actionEvent -> task.markAsDone());
    }

    private void switchToolWindowContentToSessionToolWindow(SessionToolWindow sessionToolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(sessionToolWindow.getContent(), "", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);
    }
}
