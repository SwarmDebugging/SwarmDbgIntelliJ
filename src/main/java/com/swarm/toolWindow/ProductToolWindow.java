package com.swarm.toolWindow;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.uiDesigner.core.GridConstraints;
import com.swarm.models.Task;
import com.swarm.tools.HTTPRequests;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;

public class ProductToolWindow {
    private JPanel dataPanel;

    public ProductToolWindow(ToolWindow toolWindow) {
        this.buildProductTreeView();
    }

    public JPanel getContent() {return dataPanel;}

    public void buildProductTreeView() {
        dataPanel.removeAll();
        int productId = 666; //for testing

        ProductNode productNode = new ProductNode("Product Title", productId);
        DefaultTreeModel productTreeModel = new DefaultTreeModel(productNode);
        productNode.setModel(productTreeModel);

        ArrayList<Task> tasks = HTTPRequests.tasksByProductId(productId);
        if(tasks != null) { //may need more conditions
            for (int i = 0; i < tasks.size(); i++) {
                ProductNode node = new ProductNode(tasks.get(i).getTitle(), tasks.get(i).getId());
                productNode.add(node);
            }
        }

        ProductTree productTree = new ProductTree(productTreeModel);
        productTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        productTree.setCellRenderer(new ProductTreeRenderer());

        productTree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent treeExpansionEvent) {
                //update tasks
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent treeExpansionEvent) {

            }
        });

        ProductTreeRenderer productRenderer = (ProductTreeRenderer) productTree.getCellRenderer();
        productRenderer.setBackground((Color)null);


        dataPanel.add(productTree, new GridConstraints());
        int i = 0;
    }
}
