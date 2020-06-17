package com.swarm.toolWindow;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class ProductTreeRenderer extends DefaultTreeCellRenderer {

    Icon taskIcon = IconLoader.getIcon("/icons/task.svg");
    Icon productIcon = IconLoader.getIcon("/icons/product.svg");
    Icon productsIcon = IconLoader.getIcon("/icons/ant.svg");

    ProductNode node;

    public ProductTreeRenderer() {
    }

    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
        if (sel) {
            tree.requestFocusInWindow();
        }
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, true);
        this.node = (ProductNode) value;
        buildTreeNodesAppearance();

        if (node.getParent() == null) {
            setIcon(productsIcon);
            return this;
        }
        if (node.isTask()) {
            setIcon(taskIcon);
        } else if (node.isProduct()) {
            setIcon(productIcon);
        }
        return this;
    }

    private void buildTreeNodesAppearance() {
        setToolTipText(node.getToolTip() + " (Right click for more options)");
        setBorderSelectionColor((Color) null);
        setBackgroundNonSelectionColor((Color) null);
        setBackground((Color) null);
    }
}
