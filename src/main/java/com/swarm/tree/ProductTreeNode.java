package com.swarm.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class ProductTreeNode extends DefaultMutableTreeNode {

    protected DefaultTreeModel model;

    private int id;
    private final String toolTip;

    public ProductTreeNode(String nodeTitle, int id) {
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

    public String getToolTip() {
        return toolTip;
    }

    public void add(MutableTreeNode node) {
        super.add(node);
        nodeWasAdded(this, getChildCount() - 1);
    }

    protected void nodeWasAdded(TreeNode node, int index) {
        if(model == null) {
            ((ProductTreeNode)node.getParent()).nodeWasAdded(node, index);
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
