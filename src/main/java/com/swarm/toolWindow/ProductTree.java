package com.swarm.toolWindow;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;

public class ProductTree extends JTree {
    public int id;
    public boolean expandState = false;

    public ProductTree(TreeModel newModel) {
        super(newModel);
    }

    @Override
    protected void setExpandedState(TreePath path, boolean state) {
        this.expandState = state;
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

    public Component add(String title, int id) {
        this.id = id;
        Component comp = new Component() {
            @Override
            public void setName(String name) {
                super.setName(name);
            }
        };
        comp.setName(title);
        return super.add(comp);
    }
}
