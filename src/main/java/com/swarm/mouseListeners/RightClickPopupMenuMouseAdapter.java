package com.swarm.mouseListeners;

import com.intellij.openapi.project.Project;
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

    public RightClickPopupMenuMouseAdapter(Project project) {
        popupMenuBuilder = new PopupMenuBuilder(project);
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
        TaskTreeNode taskNode = (TaskTreeNode) node;
        JPopupMenu popupMenu = popupMenuBuilder.buildTaskNodePopupMenu(taskNode.getTask());
        popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
    }

    private void showProductMenu() {
        ProductTreeNode productNode = (ProductTreeNode) node;
        JPopupMenu popupMenu = popupMenuBuilder.buildProductNodePopupMenu(productNode.getProduct());
        popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
    }
}
