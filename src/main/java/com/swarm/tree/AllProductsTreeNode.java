package com.swarm.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class AllProductsTreeNode extends DefaultMutableTreeNode {

    protected DefaultTreeModel model;

    private final String toolTip;

    public AllProductsTreeNode() {
        super("Products");
        this.model = null;
        this.toolTip = "Products";
    }

    public void setModel(DefaultTreeModel model) {
        this.model = model;
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
}
