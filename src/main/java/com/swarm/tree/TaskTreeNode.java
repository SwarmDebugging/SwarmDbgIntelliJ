package com.swarm.tree;

import com.swarm.models.Task;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class TaskTreeNode extends DefaultMutableTreeNode {

    protected DefaultTreeModel model;

    private Task task;
    private final String toolTip;

    public TaskTreeNode(Task task) {
        super(task.getTitle());
        this.model = null;
        this.task = task;
        this.toolTip = task.getTitle();
    }

    public void setModel(DefaultTreeModel model) {
        this.model = model;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
