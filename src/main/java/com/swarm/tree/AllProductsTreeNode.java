package com.swarm.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

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
}
