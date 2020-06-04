package com.swarm.toolWindow;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class ProductTreeRenderer extends DefaultTreeCellRenderer {

    public ProductTreeRenderer(){}

    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
        if(sel) {
            tree.requestFocusInWindow();
        }

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, true);

        ProductNode node = (ProductNode)value;
        int id = node.getId();
        setToolTipText(node.getToolTip() + " (Right click for more options)");

        if(node.isLeaf() && node.getParent() != null) {
            //it's a child node
        } else if(!node.isLeaf() && node.getId() != 0 && node.getChildCount() > 0) {
            //it's a root node
        }

        return this;
    }

    protected boolean isCurrentTask(Object value) {
        ProductNode node = (ProductNode)value;

        int id = node.getId();
        ProductNode root = (ProductNode)node.getRoot();
        if(true){
            //some condition
        }
        return false;
    }

    protected boolean isCurrentProduct(Object value) {
        ProductNode node = (ProductNode)value;

        int id = node.getId();
        if(true){//something like currentProduct == id

        }
        return false;
    }

}
