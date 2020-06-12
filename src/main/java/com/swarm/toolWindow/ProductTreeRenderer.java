package com.swarm.toolWindow;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class ProductTreeRenderer extends DefaultTreeCellRenderer {

    Icon taskIcon = IconLoader.getIcon("/icons/task.svg");
    Icon productIcon = IconLoader.getIcon("/icons/product.svg");
    Icon productsIcon = IconLoader.getIcon("/icons/products.svg");

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
        setToolTipText(node.getToolTip() + " (Right click for more options)");

        setBorderSelectionColor((Color) null);
        setBackgroundNonSelectionColor((Color) null);
        setBackground((Color) null);
        //updates constantly, have to wait until parent is set
        if(node.getParent() == null) {
            setIcon(productsIcon);
            return this;
        }

        if (node.isLeaf() && node.getParent().getParent() != null){
            //if it's a task
            setIcon(taskIcon);
        } else if(!node.isRoot()) {
            //if it's a product
            setIcon(productIcon);
        }
        return this;
    }

    /*protected boolean isCurrentTask(Object value) {
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
    }*/

}
