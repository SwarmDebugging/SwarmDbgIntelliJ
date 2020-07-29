package com.swarm.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class TaskTreeNode extends DefaultMutableTreeNode {

    protected DefaultTreeModel model;

    private int id;
    private final String toolTip;

    public TaskTreeNode(String nodeTitle, int id) {
        super(nodeTitle);
        this.model = null;
        this.id = id;
        this.toolTip = nodeTitle;
    }

    public void setModel(DefaultTreeModel model) {
        this.model = model;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
