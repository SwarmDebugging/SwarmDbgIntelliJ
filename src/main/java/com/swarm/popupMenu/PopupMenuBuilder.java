package com.swarm.popupMenu;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.swarm.dialogs.CreateSessionDialog;
import com.swarm.dialogs.CreateTaskDialog;
import com.swarm.models.Product;
import com.swarm.models.Task;
import com.swarm.toolWindow.ProductToolWindow;
import com.swarm.toolWindow.RecommendationToolWindow;

import javax.swing.*;

public class PopupMenuBuilder {

    JMenuItem createNewTask;
    JPopupMenu productPopupMenu;

    JMenuItem markAsDone;
    JMenuItem newSwarmSession;
    JPopupMenu taskPopupMenu;

    private final Project project;
    private Task task;
    private Product product;

    public PopupMenuBuilder(Project project) {
        this.project = project;
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
                ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Swarm Debugging Manager");
                ProductToolWindow productToolWindow = (ProductToolWindow) toolWindow.getContentManager().getContent(0).getComponent();
                productToolWindow.getCurrentSession().setTask(task);
                productToolWindow.getCurrentSession().setDeveloper(productToolWindow.getDeveloper());
                productToolWindow.getCurrentSession().setDescription(createSessionDialog.getDescription());
                productToolWindow.getCurrentSession().start();
            }
        });
    }

    private void buildMarkAsDoneMenuItem() {
        markAsDone = new JMenuItem("Mark As Done");
        markAsDone.addActionListener(actionEvent -> task.markAsDone());
    }

    public JBPopupMenu buildRecommendationPopupMenu() {
        JBMenuItem jumpToSource = new JBMenuItem("Jump to source");
        JBMenuItem setBreakpoint = new JBMenuItem("Set a Breakpoint in the Method");

        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Swarm Debugging Recommendations");
        RecommendationToolWindow recommendationToolWindow = (RecommendationToolWindow) toolWindow.getContentManager().getContent(0).getComponent();
        if(recommendationToolWindow != null) {
            jumpToSource.addActionListener(actionEvent -> recommendationToolWindow.jumpToSource());
            setBreakpoint.addActionListener(actionEvent -> recommendationToolWindow.setBreakpoint());
        }

        JBPopupMenu popupMenu = new JBPopupMenu();
        popupMenu.add(jumpToSource);
        popupMenu.add(setBreakpoint);

        return popupMenu;
    }
}
