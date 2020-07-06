package com.swarm.mouseAdapters;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.swarm.models.Developer;
import com.swarm.models.Product;
import com.swarm.models.Task;
import com.swarm.popupMenu.PopupMenuBuilder;
import com.swarm.tree.ProductTreeNode;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class rightClickPopupMenuMouseAdapter extends MouseAdapter {
    PopupMenuBuilder popupMenuBuilder;

    public rightClickPopupMenuMouseAdapter(Project project, Developer developer, ToolWindow toolWindow) {
        popupMenuBuilder = new PopupMenuBuilder(toolWindow, project, developer);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        int RIGHT_CLICK = MouseEvent.BUTTON3;
        if (e.getButton() == RIGHT_CLICK) {
            JTree tree = (JTree) e.getSource();
            int selRow = tree.getRowForLocation(e.getX(), e.getY());
            tree.setSelectionRow(selRow);
            tree.requestFocusInWindow();

            ProductTreeNode node = (ProductTreeNode) ((JTree) e.getSource()).getLastSelectedPathComponent();
            if (node == null || node.isRoot()) {
                return;
            }
            if (node.isTask()) {
                Task task = new Task();
                task.setId(node.getId());
                JPopupMenu popupMenu = popupMenuBuilder.buildTaskNodePopupMenu(task);
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            } else if (node.isProduct()) {
                Product product = new Product();
                product.setId(node.getId());
                JPopupMenu popupMenu = popupMenuBuilder.buildProductNodePopupMenu(product);
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
