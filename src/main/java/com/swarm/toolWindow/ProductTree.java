package com.swarm.toolWindow;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class ProductTree extends JTree {
    public int id;

    public ProductTree(TreeModel newModel) {
        super(newModel);
    }

    @Override
    protected void setExpandedState(TreePath path, boolean state) {
        super.setExpandedState(path, state);
    }

    @Override
    public void setModel(TreeModel newModel) {
        super.setModel(newModel);
    }

    @Override
    public TreeCellRenderer getCellRenderer() {
        return super.getCellRenderer();
    }
}
