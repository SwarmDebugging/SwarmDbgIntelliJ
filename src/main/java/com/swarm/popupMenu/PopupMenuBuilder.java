package com.swarm.popupMenu;

import com.intellij.openapi.project.Project;
import com.swarm.dialogs.CreateSessionDialog;
import com.swarm.dialogs.CreateTaskDialog;
import com.swarm.models.Developer;
import com.swarm.models.Product;
import com.swarm.models.Task;
import com.swarm.toolWindow.ProductToolWindow;

import javax.swing.*;

public class PopupMenuBuilder {

    JMenuItem createNewTask;
    JPopupMenu productPopupMenu;

    JMenuItem markAsDone;
    JMenuItem newSwarmSession;
    JPopupMenu taskPopupMenu;

    private final Project project;
    private final Developer developer;
    private Task task;
    private Product product;

    public PopupMenuBuilder(Project project, Developer developer) {
        this.project = project;
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
            CreateTaskDialog createTaskDialog = new CreateTaskDialog(project, product);
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
                ProductToolWindow.getCurrentSession().setTask(task);
                ProductToolWindow.getCurrentSession().setDeveloper(developer);
                ProductToolWindow.getCurrentSession().setDescription(createSessionDialog.getDescription());
                ProductToolWindow.getCurrentSession().start();
            }
        });
    }

    private void buildMarkAsDoneMenuItem() {
        markAsDone = new JMenuItem("Mark As Done");
        markAsDone.addActionListener(actionEvent -> task.markAsDone());
    }
}
