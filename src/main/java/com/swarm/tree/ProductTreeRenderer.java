package com.swarm.tree;

import com.intellij.icons.AllIcons;
import icons.SwarmIcons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class ProductTreeRenderer extends DefaultTreeCellRenderer {

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
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        buildTreeNodesAppearance();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        if (node instanceof AllProductsTreeNode) {
            setIcon(SwarmIcons.Ant);
        } else if (node instanceof TaskTreeNode) {
            setIcon(AllIcons.Actions.Selectall);
        } else if (node instanceof ProductTreeNode) {
            setIcon(AllIcons.Nodes.Package);
        } else if (node instanceof SessionTreeNode) {
            setIcon(AllIcons.Nodes.Services);
        }
        return this;
    }

    private void buildTreeNodesAppearance() {
        setToolTipText("(Right click for more options)");
        setBorderSelectionColor(null);
        setBackgroundNonSelectionColor(null);
        setBackground(null);
    }
}
