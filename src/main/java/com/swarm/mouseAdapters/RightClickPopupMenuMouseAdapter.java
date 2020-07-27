package com.swarm.mouseAdapters;

import com.intellij.openapi.project.Project;
import com.swarm.models.Developer;
import com.swarm.models.Product;
import com.swarm.models.Task;
import com.swarm.popupMenu.PopupMenuBuilder;
import com.swarm.toolWindow.CurrentTaskProvider;
import com.swarm.tree.ProductTreeNode;
import com.swarm.tree.TaskTreeNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RightClickPopupMenuMouseAdapter extends MouseAdapter {
    PopupMenuBuilder popupMenuBuilder;

    public RightClickPopupMenuMouseAdapter(Project project, Developer developer) {
        popupMenuBuilder = new PopupMenuBuilder(project, developer);
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

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) ((JTree) e.getSource()).getLastSelectedPathComponent();
            if (node == null || node.isRoot()) {
                return;
            }
            if (node instanceof TaskTreeNode) {
                Task task = new Task();
                TaskTreeNode taskNode = (TaskTreeNode) node;
                task.setId(taskNode.getId());
                JPopupMenu popupMenu = popupMenuBuilder.buildTaskNodePopupMenu(task);
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            } else if (node instanceof ProductTreeNode) {
                Product product = new Product();
                ProductTreeNode productNode = (ProductTreeNode) node;
                product.setId(productNode.getId());
                JPopupMenu popupMenu = popupMenuBuilder.buildProductNodePopupMenu(product);
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
