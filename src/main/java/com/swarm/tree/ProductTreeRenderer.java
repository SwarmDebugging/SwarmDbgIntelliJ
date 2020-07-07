package com.swarm.tree;

import com.intellij.icons.AllIcons;
import icons.SwarmIcons;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class ProductTreeRenderer extends DefaultTreeCellRenderer {

    ProductTreeNode node;

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
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, true);
        this.node = (ProductTreeNode) value;
        buildTreeNodesAppearance();

        if (node.getParent() == null) {
            setIcon(SwarmIcons.Ant);
            return this;
        }
        if (node.isTask()) {
            setIcon(AllIcons.Actions.Selectall);
        } else if (node.isProduct()) {
            setIcon(AllIcons.Nodes.Package);
        }
        return this;
    }

    private void buildTreeNodesAppearance() {
        setToolTipText(node.getToolTip() + " (Right click for more options)");
        setBorderSelectionColor(null);
        setBackgroundNonSelectionColor(null);
        setBackground(null);
    }
}
