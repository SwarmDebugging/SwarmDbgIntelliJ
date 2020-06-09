package com.swarm.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.swarm.models.Product;
import com.swarm.models.Task;
import com.swarm.tools.HTTPRequests;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ProductToolWindow {
    private JPanel dataPanel;
    private JPanel productWindowContent;
    private JLabel refresh;

    //TODO: finish this
    public ProductToolWindow(ToolWindow toolWindow) {
        this.buildProductTreeView();
        refresh.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
            }
        });
    }

    public JPanel getContent() {return productWindowContent;}

    public void buildProductTreeView() {

        dataPanel.removeAll();
        int developerId = 1; //for testing
        ArrayList<Product> productArrayList = HTTPRequests.productsByDeveloperId(developerId);
        if(productArrayList == null) {
            return; //TODO: display no products message
        }

        ProductNode productNode = new ProductNode("Developers name Products", 0); //root element, make this fixed in left corner
        DefaultTreeModel productTreeModel = new DefaultTreeModel(productNode);
        productNode.setModel(productTreeModel);

        for (Product product : productArrayList) {

            ProductNode newProductNode = new ProductNode(product.getTitle(), product.getId());
            DefaultTreeModel newProductTreeModel = new DefaultTreeModel(newProductNode);
            newProductNode.setModel(newProductTreeModel);

            ArrayList<Task> tasks = product.getTasks();
            if (tasks != null) { //may need more conditions
                for (int j = 0; j < tasks.size(); j++) {
                    ProductNode node = new ProductNode(tasks.get(j).getTitle(), tasks.get(j).getId());
                    newProductNode.add(node);
                }
            }

            ProductTree newProductTree = new ProductTree(newProductTreeModel);
            newProductTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            newProductTree.setCellRenderer(new ProductTreeRenderer());

            productNode.add(newProductNode);
        }
        ProductTree productTree = new ProductTree(productTreeModel);
        productTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        productTree.setCellRenderer(new ProductTreeRenderer());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        dataPanel.add(productTree, constraints); //make it fixed in left corner

        productWindowContent.updateUI();
        productWindowContent.setVisible(true);
        productWindowContent.revalidate();
    }
}
