package com.swarm.mouseAdapters;

import com.intellij.openapi.project.Project;
import com.swarm.models.Developer;
import com.swarm.models.Product;
import com.swarm.models.Task;
import com.swarm.popupMenu.PopupMenuBuilder;
import com.swarm.tree.ProductTreeNode;
import com.swarm.tree.TaskTreeNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RightClickPopupMenuMouseAdapter extends MouseAdapter {
    PopupMenuBuilder popupMenuBuilder;
    DefaultMutableTreeNode node;
    MouseEvent mouseEvent;

    int RIGHT_CLICK = MouseEvent.BUTTON3;

    public RightClickPopupMenuMouseAdapter(Project project, Developer developer) {
        popupMenuBuilder = new PopupMenuBuilder(project, developer);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
        super.mouseClicked(mouseEvent);
        if (mouseEvent.getButton() == RIGHT_CLICK) {
            handleRightClick();
        }
    }

    private void handleRightClick() {
        selectClickedTreeRow();

        node = (DefaultMutableTreeNode) ((JTree) mouseEvent.getSource()).getLastSelectedPathComponent();
        if (node == null || node.isRoot()) {
            return;
        }
        if (node instanceof TaskTreeNode) {
            showTaskMenu();
        } else if (node instanceof ProductTreeNode) {
            showProductMenu();
        }
    }

    private void selectClickedTreeRow() {
        JTree tree = (JTree) mouseEvent.getSource();
        int selRow = tree.getRowForLocation(mouseEvent.getX(), mouseEvent.getY());
        tree.setSelectionRow(selRow);
        tree.requestFocusInWindow();
    }

    private void showTaskMenu() {
        Task task = new Task();
        TaskTreeNode taskNode = (TaskTreeNode) node;
        task.setId(taskNode.getId());
        JPopupMenu popupMenu = popupMenuBuilder.buildTaskNodePopupMenu(task);
        popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
    }

    private void showProductMenu() {
        Product product = new Product();
        ProductTreeNode productNode = (ProductTreeNode) node;
        product.setId(productNode.getId());
        JPopupMenu popupMenu = popupMenuBuilder.buildProductNodePopupMenu(product);
        popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
    }
}
