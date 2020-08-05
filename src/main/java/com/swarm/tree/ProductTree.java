package com.swarm.tree;

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

    public String getExpansionState() {
        StringBuffer stringState = new StringBuffer();
        for (int i = 0; i < this.getRowCount(); i++) {
            if(this.isExpanded(i)){
                stringState.append(i).append(",");
            }
        }
        return stringState.toString();
    }

    public void setExpansionState(String stringState) {
        if(stringState == null) {
            return;
        }
        String[] indexes = stringState.split(",");
        for (String index: indexes) {
            int row = Integer.parseInt(index);
            this.expandRow(row);
        }
    }
}
