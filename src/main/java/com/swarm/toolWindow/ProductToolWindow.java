package com.swarm.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.uiDesigner.core.GridConstraints;
import com.swarm.States;
import com.swarm.models.Product;
import com.swarm.models.Task;
import com.swarm.tools.HTTPRequests;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

public class ProductToolWindow {
    private JPanel dataPanel;
    private JPanel productWindowContent;
    private JLabel refresh;
    private JLabel addProduct;
    private JLabel logout;
    ToolWindow toolWindow;

    public ProductToolWindow(ToolWindow toolWindow, Project project) {
        this.toolWindow = toolWindow;
        this.buildProductTreeView(project);

        refresh.setIcon(IconLoader.getIcon("/icons/refresh.svg"));
        refresh.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                buildProductTreeView(project);
            }
        });
        logout.setIcon(IconLoader.getIcon("/icons/logout.svg"));
        logout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                States.currentDeveloperId = -1;
                LoginToolWindow loginToolWindow = new LoginToolWindow(toolWindow, project);
                ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
                Content content = contentFactory.createContent(loginToolWindow.getContent(), "", false);
                toolWindow.getContentManager().removeAllContents(true);
                toolWindow.getContentManager().addContent(content);
                //show logged out notification
            }
        });
        addProduct.setIcon(IconLoader.getIcon("/icons/add.svg"));
        addProduct.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                CreateProductDialog createProductDialog = new CreateProductDialog(project, 1);//States.developerId);???
                createProductDialog.showAndGet();
                buildProductTreeView(project);
            }
        });
    }

    public JPanel getContent() {return productWindowContent;}

    public void buildProductTreeView(Project project) {

        dataPanel.removeAll();
        ArrayList<Product> productArrayList = HTTPRequests.productsByDeveloperId(1);//States.currentDeveloperId);???
        if(productArrayList == null) {
            return; //TODO: display no products message
        }

        ProductNode productNode = new ProductNode("Products", 0);
        DefaultTreeModel productTreeModel = new DefaultTreeModel(productNode);
        productNode.setModel(productTreeModel);

        for (Product product : productArrayList) {

            ProductNode newProductNode = new ProductNode(product.getTitle(), product.getId());
            DefaultTreeModel newProductTreeModel = new DefaultTreeModel(newProductNode);
            newProductNode.setModel(newProductTreeModel);

            ArrayList<Task> tasks = product.getTasks();
            if (tasks != null) {
                for (Task task : tasks) {
                    if(task.isDone()){
                        continue;
                    }
                    ProductNode taskNode = new ProductNode(task.getTitle(), task.getId());
                    newProductNode.add(taskNode);
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
        productTree.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);

                    if(e.getButton() == MouseEvent.BUTTON3) { //right click
                        ProductNode node = (ProductNode) productTree.getLastSelectedPathComponent();

                        if(node == null) {
                            return;
                        }

                        //TODO: refresh after
                        if (node.isLeaf() && node.getParent().getParent() != null){
                            //if it's a task
                            JPopupMenu popupMenu = PopupMenuBuilder.buildTaskPopupMenu(project, node.getId(), 1, toolWindow); //TODO:developerID
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        } else if(!node.isRoot()){
                            //if it's a product
                            //TODO: how to get developerID?
                            JPopupMenu popupMenu = PopupMenuBuilder.buildProductPopupMenu(node.getId() ,project, 1);
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
        });
        productTree.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                JTree tree = (JTree) e.getSource();
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                tree.setSelectionRow(selRow);
                tree.requestFocusInWindow();
            }
        });

        GridConstraints constraints1 = new GridConstraints();
        constraints1.setColumn(0);
        constraints1.setRow(0);
        constraints1.setAnchor(GridConstraints.ANCHOR_CENTER);
        constraints1.setFill(GridConstraints.ALIGN_FILL);
        constraints1.setUseParentLayout(false);
        dataPanel.add(productTree, constraints1);

        productWindowContent.updateUI();
        productWindowContent.setVisible(true);
        productWindowContent.revalidate();
    }
}
