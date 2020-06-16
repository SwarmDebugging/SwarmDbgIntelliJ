package com.swarm.toolWindow;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class ProductNode extends DefaultMutableTreeNode {

    protected DefaultTreeModel model;

    private int id;
    private String title;
    private String toolTip;

    public ProductNode(String nodeTitle, int id) {
        super(nodeTitle);
        this.model = null;
        this.id = id;
        this.title = nodeTitle;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getToolTip() {
        return toolTip;
    }

    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    public void add(MutableTreeNode node) {
        super.add(node);
        nodeWasAdded(this, getChildCount() - 1);
    }

    protected void nodeWasAdded(TreeNode node, int index) {
        if(model == null) {
            ((ProductNode)node.getParent()).nodeWasAdded(node, index);
        }
        else {
            int[] childIndices = new int[1];
            childIndices[0] = index;
            model.nodesWereInserted(node, childIndices);
        }
    }

    public boolean isTask() {
        return this.isLeaf() && this.getParent().getParent() != null;
    }

    public boolean isProduct() {
        return !this.isRoot();
    }

}
