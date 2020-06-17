package com.swarm.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.ScrollPaneFactory;
import com.swarm.models.Product;
import com.swarm.models.Task;
import com.swarm.tools.HTTPRequests;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ProductToolWindow extends SimpleToolWindowPanel {

    private final int RIGHT_CLICK = MouseEvent.BUTTON3;


    private ToolWindow toolWindow;
    private Project project;
    private int developerId;

    private PopupMenuBuilder popupMenuBuilder;

    private ArrayList<Product> productArrayList;
    private ProductNode allProductsNode;
    private ProductTree allProductsTree;


    public ProductToolWindow(ToolWindow toolWindow, Project project, int developerId) {
        super(true, true);

        this.toolWindow = toolWindow;
        this.project = project;
        this.developerId = developerId;
        popupMenuBuilder = new PopupMenuBuilder(toolWindow, project, developerId);

        buildProductTreeView();
        setContent(ScrollPaneFactory.createScrollPane(allProductsTree));
    }

    public void buildProductTreeView() {
        productArrayList = HTTPRequests.productsByDeveloperId(developerId);
        if (productArrayList != null) {
            buildProductTree();
        } else {
            displayNoProductsMessage();
        }
    }

    private void buildProductTree() {
        createAllProductsNode();
        for (Product product : productArrayList) {
            addProductToAllProductsNode(product);
        }
    }

    private void createAllProductsNode() {
        allProductsNode = new ProductNode("Products", 0);
        DefaultTreeModel allProductsTreeModel = new DefaultTreeModel(allProductsNode);
        allProductsNode.setModel(allProductsTreeModel);
        buildAllProductsTree(allProductsTreeModel);
    }

    private ProductTree buildAllProductsTree(DefaultTreeModel allProductsTreeModel) {
        allProductsTree = new ProductTree(allProductsTreeModel);
        allProductsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        allProductsTree.setCellRenderer(new ProductTreeRenderer());
        //TODO create new mouseAdapter and mousemotionadpater classes
        allProductsTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (e.getButton() == RIGHT_CLICK) {
                    JTree tree = (JTree) e.getSource();
                    int selRow = tree.getRowForLocation(e.getX(), e.getY());
                    tree.setSelectionRow(selRow);
                    tree.requestFocusInWindow();

                    ProductNode node = (ProductNode) allProductsTree.getLastSelectedPathComponent();
                    if (node == null || node.isRoot()) {
                        return;
                    }
                    //TODO: refresh after
                    if (node.isTask()) {
                        JPopupMenu popupMenu = popupMenuBuilder.buildTaskNodePopupMenu(node.getId());
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    } else if (node.isProduct()) {
                        //TODO: how to get developerID?
                        JPopupMenu popupMenu = popupMenuBuilder.buildProductNodePopupMenu(node.getId());
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        return allProductsTree;
    }

    private void addProductToAllProductsNode(Product product) {
        ProductNode productNode = createProductNodeFromProduct(product);
        addTasksToProductNode(product.getTasks(), productNode);
        allProductsNode.add(productNode);
    }

    private ProductNode createProductNodeFromProduct(Product product) {
        ProductNode productNode = new ProductNode(product.getTitle(), product.getId());
        DefaultTreeModel newProductTreeModel = new DefaultTreeModel(productNode);
        productNode.setModel(newProductTreeModel);
        ProductTree newProductTree = new ProductTree(newProductTreeModel);
        newProductTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        newProductTree.setCellRenderer(new ProductTreeRenderer());
        return productNode;
    }

    private void addTasksToProductNode(ArrayList<Task> tasks, ProductNode productNode) {
        for (Task task : tasks) {
            if (!(task.isDone())) {
                ProductNode taskNode = new ProductNode(task.getTitle(), task.getId());
                productNode.add(taskNode);
            }
        }
    }

    //TODO
    private void displayNoProductsMessage() {
        String todo = "todo";
    }

}
