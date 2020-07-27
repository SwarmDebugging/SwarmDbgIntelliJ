package com.swarm.toolWindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Consumer;
import com.intellij.util.ui.JBUI;
import com.swarm.dialogs.CreateProductDialog;
import com.swarm.dialogs.CreateSessionDialog;
import com.swarm.dialogs.CreateTaskDialog;
import com.swarm.dialogs.LoginDialog;
import com.swarm.models.Developer;
import com.swarm.models.Product;
import com.swarm.models.Session;
import com.swarm.models.Task;
import com.swarm.mouseAdapters.RightClickPopupMenuMouseAdapter;
import com.swarm.services.ProductService;
import com.swarm.services.SessionService;
import com.swarm.tree.ProductTree;
import com.swarm.tree.ProductTreeNode;
import com.swarm.tree.ProductTreeRenderer;
import icons.SwarmIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;

public class ProductToolWindow extends SimpleToolWindowPanel implements DumbAware {

    private final Project project;
    private static final Developer developer = new Developer();
    private static final Session currentSession = new Session();

    private ArrayList<Product> productList = new ArrayList<>();
    private ProductTreeNode allProductsNode;
    private ProductTree allProductsTree;
    private final RightClickPopupMenuMouseAdapter rightClickPopupMenuMouseAdapter;

    public static Developer getDeveloper() {
        return developer;
    }

    public static int getCurrentSessionId() {
        return currentSession.getId();
    }

    public static Session getCurrentSession() {
        return currentSession;
    }

    public ProductToolWindow(Project project) {
        super(true, true);

        rightClickPopupMenuMouseAdapter = new RightClickPopupMenuMouseAdapter(project, developer);

        this.project = project;

        setContent(new JLabel("Login to view available products", SwingConstants.CENTER));
        createToolBarPanel();
    }

    private void createToolBarPanel() {
        final DefaultActionGroup group = new DefaultActionGroup();
        group.add(new LoginAction());
        group.add(new FetchAllProductsAction());
        group.add(new AddProductAction());
        group.add(new AddTaskAction());
        group.add(new FilterDeveloperProductsAction());
        group.add(new StartRecordingEventsAction());
        group.add(new MarkTaskAsDoneAction());
        group.add(new StopSessionAction());
        group.add(new LogoutAction());
        final ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("swarm", group, true);
        setToolbar(JBUI.Panels.simplePanel(actionToolbar.getComponent()));
    }

    private void buildAllProductTreeView() {
        setContent(new JLabel("Fetching products...", SwingConstants.CENTER));
        productList.clear();
        addProductsToProductList();
        if (!productList.isEmpty()) {
            buildProductTree();
            setContent(ScrollPaneFactory.createScrollPane(allProductsTree));
        } else {
            setContent(new JLabel("Create a new product to get started", SwingConstants.CENTER));
        }
    }

    private void addProductsToProductList() {
        ProductService productService = new ProductService();
        productList = productService.getAllProducts();
    }

    private void buildProductTree() {
        createAllProductsNode();
        for (Product product : productList) {
            addProductToAllProductsNode(product);
        }
    }

    private void createAllProductsNode() {
        allProductsNode = new ProductTreeNode("Products", 0);
        DefaultTreeModel allProductsTreeModel = new DefaultTreeModel(allProductsNode);
        allProductsNode.setModel(allProductsTreeModel);
        buildAllProductsTree(allProductsTreeModel);
    }

    private void buildAllProductsTree(DefaultTreeModel allProductsTreeModel) {
        allProductsTree = new ProductTree(allProductsTreeModel);
        allProductsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        allProductsTree.setCellRenderer(new ProductTreeRenderer());
        allProductsTree.addMouseListener(rightClickPopupMenuMouseAdapter);
        allProductsTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                var tree = (ProductTree) treeSelectionEvent.getSource();
                ProductTreeNode node = (ProductTreeNode) tree.getLastSelectedPathComponent();
                Task task = new Task();
                if (node.isTask()) {
                    task.setId(node.getId());
                }
                CurrentTaskProvider.setTask(task);

            }
        });
    }

    private void addProductToAllProductsNode(Product product) {
        ProductTreeNode productTreeNode = createProductNodeFromProduct(product);
        addTasksToProductNode(product.getTasks(), productTreeNode);
        allProductsNode.add(productTreeNode);
    }

    private ProductTreeNode createProductNodeFromProduct(Product product) {
        ProductTreeNode productTreeNode = new ProductTreeNode(product.getName(), product.getId());
        DefaultTreeModel newProductTreeModel = new DefaultTreeModel(productTreeNode);
        productTreeNode.setModel(newProductTreeModel);
        ProductTree newProductTree = new ProductTree(newProductTreeModel);
        newProductTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        newProductTree.setCellRenderer(new ProductTreeRenderer());
        return productTreeNode;
    }

    private void addTasksToProductNode(ArrayList<Task> tasks, ProductTreeNode productTreeNode) {
        for (Task task : tasks) {
            if (!(task.isDone())) {
                ProductTreeNode taskNode = new ProductTreeNode(task.getTitle(), task.getId());
                productTreeNode.add(taskNode);
            }
        }
    }

    private final class AddProductAction extends DumbAwareAction {
        AddProductAction() {
            super("Add a New Product", "Add a new product to the developer's products", AllIcons.General.Add);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            addNewProduct();
            buildAllProductTreeView();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(developer.isLoggedIn() && !currentSession.isActive());
        }
    }

    private void addNewProduct() {
        CreateProductDialog createProductDialog = new CreateProductDialog(project);
        createProductDialog.showAndGet();
    }

    private final class AddTaskAction extends DumbAwareAction {
        AddTaskAction() {
            super("Add a New Task", "Add a new task to the selected product", AllIcons.Actions.Selectall);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            addNewTaskToSelectedProduct();
            buildAllProductTreeView();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(getSelectedProductFromTree() != null && !currentSession.isActive());
        }
    }

    private void addNewTaskToSelectedProduct() {
        ProductTreeNode productTreeNode = getSelectedProductFromTree();
        if (productTreeNode == null) {
            return;
        }
        Product product = new Product();
        product.setId(productTreeNode.getId());
        CreateTaskDialog createTaskDialog = new CreateTaskDialog(project, product);
        createTaskDialog.showAndGet();
    }

    private ProductTreeNode getSelectedProductFromTree() {
        if (allProductsTree == null) {
            return null;
        }
        ProductTreeNode node = (ProductTreeNode) allProductsTree.getLastSelectedPathComponent();
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

    private final class LoginAction extends DumbAwareAction {
        LoginAction() {
            super("Login", "Log into your swarm debugging account", AllIcons.Actions.TraceInto);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            /*Developer developer = new Developer();
            developer.setId(1);
            SessionService sessionService = new SessionService();
            ArrayList<Session> sessions = sessionService.sessionsByDeveloper(developer);
            var builder = JBPopupFactory.getInstance().createPopupChooserBuilder(sessions);
            builder.setItemChosenCallback(session -> {
                int i = 0;
            });
            builder.setRequestFocus(true).setTitle("Resume Session?").createPopup().showInBestPositionFor(e.getDataContext());*/
            LoginDialog loginDialog = new LoginDialog(project);
            boolean loggedIn = loginDialog.showAndGet();
            if (loggedIn) {
                buildAllProductTreeView();

                SessionService sessionService = new SessionService();
                ArrayList<Session> sessions = sessionService.sessionsByDeveloper(developer);
                ArrayList<Session> openedSessions = new ArrayList<>();
                for (Session session : sessions) {
                    if (!session.isFinished()) {
                        openedSessions.add(session);
                    }
                }
                var builder = JBPopupFactory.getInstance().createPopupChooserBuilder(openedSessions);
                builder.setItemChosenCallback(session -> {
                    int i = 0;
                });
                builder.setRequestFocus(true).createPopup().showInBestPositionFor(e.getDataContext());
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(!developer.isLoggedIn() && !currentSession.isActive());
        }
    }

    private final class FetchAllProductsAction extends DumbAwareAction {
        FetchAllProductsAction() {
            super("Fetch all products", "fetch all the products and tasks", AllIcons.Ide.IncomingChangesOn);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            buildAllProductTreeView();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(developer.isLoggedIn() && !currentSession.isActive());
        }
    }

    private final class MarkTaskAsDoneAction extends DumbAwareAction {
        MarkTaskAsDoneAction() {
            super("Mark Task as Done", "Mark the selected task as done", AllIcons.Actions.Commit);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            markSelectedTaskAsDone();
            buildAllProductTreeView();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(getSelectedTaskFromTree() != null && !currentSession.isActive());
        }
    }

    private void markSelectedTaskAsDone() {
        ProductTreeNode taskNode = getSelectedTaskFromTree();
        if (taskNode == null) {
            return;
        }
        Task task = new Task();
        task.setId(taskNode.getId());
        task.markAsDone();
    }

    private ProductTreeNode getSelectedTaskFromTree() {
        if (allProductsTree == null) {
            return null;
        }
        ProductTreeNode node = (ProductTreeNode) allProductsTree.getLastSelectedPathComponent();
        if (node == null || node.isRoot()) {
            return null;
        }
        if (node.isTask()) {
            return node;
        }
        return null;
    }

    private class FilterDeveloperProductsAction extends DumbAwareAction {
        FilterDeveloperProductsAction() {
            super("Filter developer's products", "Filter Products to get only the current developer's products", AllIcons.General.Filter);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            buildFilteredProductTreeView();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(developer.isLoggedIn() && !currentSession.isActive());
        }
    }

    private void buildFilteredProductTreeView() {
        setContent(new JLabel("Fetching developer's tasks...", SwingConstants.CENTER));
        productList.clear();
        addFilteredProductsToProductList();
        if (!productList.isEmpty()) {
            buildProductTree();
            setContent(ScrollPaneFactory.createScrollPane(allProductsTree));
        } else {
            setContent(new JLabel("start a session with a task to associate a product with your account", SwingConstants.CENTER));
        }
    }

    private void addFilteredProductsToProductList() {
        ProductService productService = new ProductService();
        productList = productService.getProductsByDeveloper();
    }

    private class StartRecordingEventsAction extends DumbAwareAction {
        StartRecordingEventsAction() {
            super("Start Recording Events", "Start recording breakpoint and debugging events in the selected task", AllIcons.Actions.Execute);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            createSwarmSession();
            allProductsTree.removeMouseListener(rightClickPopupMenuMouseAdapter);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(getSelectedTaskFromTree() != null && !currentSession.isActive());
        }
    }

    private void createSwarmSession() {
        ProductTreeNode taskNode = getSelectedTaskFromTree();
        if (taskNode == null) {
            return;
        }
        Task task = new Task();
        task.setId(taskNode.getId());
        CreateSessionDialog createSessionDialog = new CreateSessionDialog(project);
        if (createSessionDialog.showAndGet()) {
            currentSession.setTask(task);
            currentSession.setDeveloper(developer);
            currentSession.setDescription(createSessionDialog.getDescription());
            currentSession.start();
        }
    }

    private class StopSessionAction extends DumbAwareAction {
        StopSessionAction() {
            super("Stop Current Swarm Session", "Stop the currently active swarm session", AllIcons.Actions.Suspend);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            currentSession.stop();
            allProductsTree.addMouseListener(rightClickPopupMenuMouseAdapter);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(currentSession.isActive());
        }
    }

    private class LogoutAction extends DumbAwareAction {
        LogoutAction() {
            super("Logout", "Logs out developer and takes him back to login screen", SwarmIcons.Logout);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            getDeveloper().logout();
            setContent(new JLabel("Login to view available products", SwingConstants.CENTER));
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(developer.isLoggedIn() && !currentSession.isActive());
        }
    }

    @Nullable
    @Override
    public JComponent getContent() {
        return this.getComponent();
    }
}