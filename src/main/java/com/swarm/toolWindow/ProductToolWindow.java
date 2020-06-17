package com.swarm.toolWindow;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.uiDesigner.core.GridConstraints;
import com.swarm.models.Product;
import com.swarm.models.Task;
import com.swarm.tools.HTTPRequests;
import org.jetbrains.annotations.NotNull;

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
    private JLabel addTask;
    private JLabel markTaskAsDone;
    private JLabel startRecordingEvents;

    private ToolWindow toolWindow;
    private Project project;
    private int developerId;

    private PopupMenuBuilder popupMenuBuilder;

    private ArrayList<Product> productArrayList;
    private ProductNode allProductsNode;
    private ProductTree allProductsTree;

    private final int RIGHT_CLICK = MouseEvent.BUTTON3;

    public ProductToolWindow(ToolWindow toolWindow, Project project, int developerId) {
        this.toolWindow = toolWindow;
        this.project = project;
        this.developerId = developerId;
        popupMenuBuilder = new PopupMenuBuilder(toolWindow, project, developerId);

        buildProductTreeView();
        buildToolBar();
    }

    private void buildToolBar() {
        createRefreshLabel();
        createAddProductLabel();
        createAddTaskLabel();
        createMarkAsDoneLabel();
        createStartRecordingEventsLabel();
        createLogoutLabel();
    }

    private void createStartRecordingEventsLabel() {
        startRecordingEvents.setIcon(IconLoader.getIcon("/icons/startRecordingEvents.svg"));
    }

    private void createMarkAsDoneLabel() {
        markTaskAsDone.setIcon(IconLoader.getIcon("/icons/markAsDone.svg"));
    }

    private void createAddTaskLabel() {
        addTask.setIcon(IconLoader.getIcon("/icons/task.svg"));
        addTask.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                ProductNode node = (ProductNode) allProductsTree.getLastSelectedPathComponent();
                if(node == null) {
                    return;
                }
                CreateTaskDialog createTaskDialog = new CreateTaskDialog(project, node.getId(), developerId);
                createTaskDialog.showAndGet();
                buildProductTreeView();
            }
        });
    }

    private final class addTaskAction extends AnAction {
        addTaskAction() {
            super("Add a New Task", "Add a new task to the selected product", IconLoader.getIcon("/icons/markAsDone.svg"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            addNewTaskToSelectedProduct();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(getSelectedProductFromTree() != null);
        }
    }

    private void addNewTaskToSelectedProduct() {
        ProductNode node = getSelectedProductFromTree();
        if(node == null) {
            return;
        }
        CreateTaskDialog createTaskDialog = new CreateTaskDialog(project, node.getId(), developerId);
        createTaskDialog.showAndGet();
        buildProductTreeView();
    }

    private ProductNode getSelectedProductFromTree() {
        ProductNode node = (ProductNode) allProductsTree.getLastSelectedPathComponent();
        if (node == null || node.isRoot()) {
            return null;
        }
        if (node.isTask()) {
            return null;
        } else if (node.isProduct()) {
            return node;
        }
        return null;
    }

    private void createAddProductLabel() {
        addProduct.setIcon(IconLoader.getIcon("/icons/add.svg"));
        addProduct.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                CreateProductDialog createProductDialog = new CreateProductDialog(project, developerId);
                createProductDialog.showAndGet();
                buildProductTreeView();
            }
        });
    }

    private void createRefreshLabel() {
        refresh.setIcon(IconLoader.getIcon("/icons/refresh.svg"));
        refresh.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                buildProductTreeView();
            }
        });
    }

    private void createLogoutLabel() {
        logout.setIcon(IconLoader.getIcon("/icons/logout.svg"));
        logout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                switchToolWindowContentToLoginToolWindow(new LoginToolWindow(toolWindow, project));
                //show logged out notification
            }
        });
    }

    public JPanel getContent() {
        return productWindowContent;
    }

    public void buildProductTreeView() {
        dataPanel.removeAll();
        productArrayList = HTTPRequests.productsByDeveloperId(developerId);
        if (productArrayList != null) {
            buildProductTree();
        } else {
            displayNoProductsMessage();
        }
        updateProductWindowContent();
    }

    //TODO
    private void displayNoProductsMessage() {
        String todo = "todo";
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

    private void createAllProductsNode() {
        allProductsNode = new ProductNode("Products", 0);
        DefaultTreeModel allProductsTreeModel = new DefaultTreeModel(allProductsNode);
        allProductsNode.setModel(allProductsTreeModel);
        dataPanel.add(buildAllProductsTree(allProductsTreeModel), createDataPanelGridConstraints());
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

    private void addProductToAllProductsNode(Product product) {
        ProductNode productNode = createProductNodeFromProduct(product);
        addTasksToProductNode(product.getTasks(), productNode);
        allProductsNode.add(productNode);
    }

    private void addTasksToProductNode(ArrayList<Task> tasks, ProductNode productNode) {
        for (Task task : tasks) {
            if (!(task.isDone())) {
                ProductNode taskNode = new ProductNode(task.getTitle(), task.getId());
                productNode.add(taskNode);
            }
        }
    }

    private void buildProductTree() {
        createAllProductsNode();
        for (Product product : productArrayList) {
            addProductToAllProductsNode(product);
        }
    }

    private void updateProductWindowContent() {
        productWindowContent.updateUI();
        productWindowContent.setVisible(true);
        productWindowContent.revalidate();
    }

    private GridConstraints createDataPanelGridConstraints() {
        GridConstraints constraints = new GridConstraints();
        constraints.setColumn(0);
        constraints.setRow(0);
        constraints.setAnchor(GridConstraints.ANCHOR_CENTER);
        constraints.setFill(GridConstraints.ALIGN_FILL);
        constraints.setUseParentLayout(false);
        return constraints;
    }

    private void switchToolWindowContentToLoginToolWindow(LoginToolWindow loginToolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(loginToolWindow.getContent(), "", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);
    }
}
