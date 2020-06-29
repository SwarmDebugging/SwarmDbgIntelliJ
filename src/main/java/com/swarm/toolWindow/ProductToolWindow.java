package com.swarm.toolWindow;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import com.swarm.States;
import com.swarm.models.Product;
import com.swarm.models.Task;
import com.swarm.tools.HTTPUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ProductToolWindow extends SimpleToolWindowPanel implements DumbAware {

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

        setToolbar(createToolBarPanel());
        buildToolWindowContent();
    }

    private void buildToolWindowContent() {
        buildProductTreeView();
        setContent(ScrollPaneFactory.createScrollPane(allProductsTree));
    }

    private JPanel createToolBarPanel() {
        final DefaultActionGroup group = new DefaultActionGroup();
        group.add(new RefreshAction());
        group.add(new AddProductAction());
        group.add(new AddTaskAction());
        group.add(new StartRecordingEventsAction());
        group.add(new MarkTaskAsDoneAction());
        group.add(new LogoutAction());
        final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("swarm", group, true);
        return JBUI.Panels.simplePanel(actionToolbar.getComponent());
    }

            //TODO: rename
    private void buildProductTreeView() {
        productArrayList = HTTPUtils.productsByDeveloperId(developerId);
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

    private final class AddProductAction extends DumbAwareAction {
        AddProductAction() {
            super("Add a New Product", "Add a new product to the developer's products", IconLoader.getIcon("/icons/add.svg"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            addNewProduct();
            buildToolWindowContent();
        }
    }

    private void addNewProduct() {
        CreateProductDialog createProductDialog = new CreateProductDialog(project, developerId);
        createProductDialog.showAndGet();
    }

    private final class AddTaskAction extends DumbAwareAction {
        AddTaskAction() {
            super("Add a New Task", "Add a new task to the selected product", IconLoader.getIcon("/icons/task.svg"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            addNewTaskToSelectedProduct();
            buildToolWindowContent();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(getSelectedProductFromTree() != null);
        }
    }

    private void addNewTaskToSelectedProduct() {
        ProductNode product = getSelectedProductFromTree();
        if(product == null) {
            return;
        }
        CreateTaskDialog createTaskDialog = new CreateTaskDialog(project, product.getId(), developerId);
        createTaskDialog.showAndGet();
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

    private final class RefreshAction extends DumbAwareAction {
        RefreshAction() {
            super("Refresh Products", "Refresh the developer's products", IconLoader.getIcon("/icons/refresh.svg"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            buildToolWindowContent();
        }
    }

    private final class MarkTaskAsDoneAction extends DumbAwareAction {
        MarkTaskAsDoneAction() {
            super("Mark Task as Done", "Mark the selected task as done", IconLoader.getIcon("/icons/markAsDone.svg"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            markSelectedTaskAsDone();
            buildToolWindowContent();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(getSelectedTaskFromTree() != null);
        }
    }

    private void markSelectedTaskAsDone() {
        ProductNode task = getSelectedTaskFromTree();
        if (task == null) {
            return;
        }
        markTaskAsDone(task.getId());
    }

    private void markTaskAsDone(int taskId) {
        HTTPUtils.taskDone(taskId);
    }

    private ProductNode getSelectedTaskFromTree() {
        ProductNode node = (ProductNode) allProductsTree.getLastSelectedPathComponent();
        if (node == null || node.isRoot()) {
            return null;
        }
        if (node.isTask()) {
            return node;
        }
        return null;
    }

    private class StartRecordingEventsAction extends DumbAwareAction{
        StartRecordingEventsAction(){
            super("Start Recording Events",
                    "Start recording breakpoint and debugging events in the selected task",
                    IconLoader.getIcon("/icons/startRecordingEvents.svg"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            States.currentSessionId = createSwarmSession();
            switchToolWindowContentToSessionToolWindow(new SessionToolWindow(States.currentSessionId, toolWindow, project, developerId));
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(getSelectedTaskFromTree() != null);
        }
    }

    private int createSwarmSession() {
        ProductNode task = getSelectedTaskFromTree();
        if (task == null) {
            return -1;
        }
        return HTTPUtils.sessionStart(developerId, task.getId());
    }

    private void switchToolWindowContentToSessionToolWindow(SessionToolWindow sessionToolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(sessionToolWindow.getContent(), "", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);
    }

    private class LogoutAction extends DumbAwareAction {
        LogoutAction() {
            super("Logout", "Logs out developer and takes him back to login screen", IconLoader.getIcon("/icons/logout.svg"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            switchToolWindowContentToLoginToolWindow(new LoginToolWindow(toolWindow, project));
        }
    }

    private void switchToolWindowContentToLoginToolWindow(LoginToolWindow loginToolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(loginToolWindow.getContent(), "", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);
    }

    //May not be best practice, we may want to create a new function called getComponent instead
    @Nullable
    @Override
    public JComponent getContent() {
        return this.getComponent();
    }

}
