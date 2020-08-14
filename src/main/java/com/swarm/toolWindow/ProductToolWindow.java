package com.swarm.toolWindow;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.ui.JBUI;
import com.swarm.dialogs.CreateProductDialog;
import com.swarm.dialogs.CreateSessionDialog;
import com.swarm.dialogs.CreateTaskDialog;
import com.swarm.dialogs.LoginDialog;
import com.swarm.models.Developer;
import com.swarm.models.Product;
import com.swarm.models.Session;
import com.swarm.models.Task;
import com.swarm.mouseListeners.RightClickPopupMenuMouseAdapter;
import com.swarm.services.ProductService;
import com.swarm.tree.*;
import icons.SwarmIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.util.ArrayList;

public class ProductToolWindow extends SimpleToolWindowPanel implements DumbAware {

    private final Project project;
    private final Developer developer = new Developer();
    private Session currentSession = new Session();

    private ArrayList<Product> productList = new ArrayList<>();
    private AllProductsTreeNode allProductsNode;
    private ProductTree allProductsTree;
    private final RightClickPopupMenuMouseAdapter rightClickPopupMenuMouseAdapter;
    private String treeExpansionState;

    public Developer getDeveloper() {
        return developer;
    }

    public void setCurrentSession(Session currentSession) {
        this.currentSession = currentSession;
        if (allProductsTree != null) {
            allProductsTree.removeMouseListener(rightClickPopupMenuMouseAdapter);
        }
        buildAllProductTreeView();
    }

    public Session getCurrentSession() {
        return currentSession;
    }

    public ProductToolWindow(Project project) {
        super(true, true);

        rightClickPopupMenuMouseAdapter = new RightClickPopupMenuMouseAdapter(project);

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

    public void buildAllProductTreeView() {
        setContent(new JLabel("Fetching products...", SwingConstants.CENTER));
        productList.clear();
        addProductsToProductList();
        if(allProductsTree != null) {
            treeExpansionState = allProductsTree.getExpansionState();
        }
        if (!productList.isEmpty()) {
            buildProductTree();
            allProductsTree.setExpansionState(treeExpansionState);
            setContent(ScrollPaneFactory.createScrollPane(allProductsTree));
        } else {
            setContent(new JLabel("Create a new product to get started", SwingConstants.CENTER));
        }
    }

    private void addProductsToProductList() {
        ProductService productService = new ProductService(project);
        productList = productService.getAllProducts();
    }

    private void buildProductTree() {
        createAllProductsNode();
        for (Product product : productList) {
            addProductToAllProductsNode(product);
        }
    }

    private void createAllProductsNode() {
        allProductsNode = new AllProductsTreeNode();
        DefaultTreeModel allProductsTreeModel = new DefaultTreeModel(allProductsNode);
        allProductsNode.setModel(allProductsTreeModel);
        buildAllProductsTree(allProductsTreeModel);
    }

    private void buildAllProductsTree(DefaultTreeModel allProductsTreeModel) {
        allProductsTree = new ProductTree(allProductsTreeModel);
        allProductsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        allProductsTree.setCellRenderer(new ProductTreeRenderer());
        allProductsTree.addMouseListener(rightClickPopupMenuMouseAdapter);
        allProductsTree.addTreeSelectionListener(treeSelectionEvent -> {
            var tree = (ProductTree) treeSelectionEvent.getSource();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node instanceof TaskTreeNode) {
                Task task = ((TaskTreeNode) node).getTask();
                TreeSelectionProvider.setTreeNode(task);
            } else if(node instanceof SessionTreeNode) {
                Session session = ((SessionTreeNode) node).getSession();
                TreeSelectionProvider.setTreeNode(session);
            } else if(node instanceof ProductTreeNode) {
                Product product = ((ProductTreeNode) node).getProduct();
                TreeSelectionProvider.setTreeNode(product);
            }
            else {
                TreeSelectionProvider.setTreeNode(null);
            }
        });
    }

    private void addProductToAllProductsNode(Product product) {
        ProductTreeNode productTreeNode = createProductNodeFromProduct(product);
        addTasksToProductNode(product.getTasks(), productTreeNode);
        allProductsNode.add(productTreeNode);
    }

    private ProductTreeNode createProductNodeFromProduct(Product product) {
        ProductTreeNode productTreeNode = new ProductTreeNode(product);
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
                TaskTreeNode taskNode = new TaskTreeNode(task);
                addSessionsToTaskNode(task.getSessions(), taskNode);
                productTreeNode.add(taskNode);
            }
        }
    }

    private void addSessionsToTaskNode(ArrayList<Session> sessions, TaskTreeNode taskTreeNode) {
        for (Session session: sessions) {
            SessionTreeNode sessionTreeNode = new SessionTreeNode(session);
            taskTreeNode.add(sessionTreeNode);
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
            e.getPresentation().setEnabled(TreeSelectionProvider.getTreeNode() instanceof Product && !currentSession.isActive() && developer.isLoggedIn());
        }
    }

    private void addNewTaskToSelectedProduct() {
        Product product = (Product) TreeSelectionProvider.getTreeNode();
        CreateTaskDialog createTaskDialog = new CreateTaskDialog(project, product);
        createTaskDialog.showAndGet();
    }

    private final class LoginAction extends DumbAwareAction {
        LoginAction() {
            super("Login", "Log into your swarm debugging account", AllIcons.Actions.TraceInto);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            LoginDialog loginDialog = new LoginDialog(project);
            boolean loggedIn = loginDialog.showAndGet();
            if (loggedIn) {
                buildAllProductTreeView();
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
            super("Fetch All Products", "Fetch all the products and tasks", AllIcons.Ide.IncomingChangesOn);
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
            e.getPresentation().setEnabled(TreeSelectionProvider.getTreeNode() instanceof Task && !currentSession.isActive() && developer.isLoggedIn());
        }
    }

    private void markSelectedTaskAsDone() {
        Task task = (Task) TreeSelectionProvider.getTreeNode();
        task.markAsDone();
    }

    private class FilterDeveloperProductsAction extends DumbAwareAction {
        FilterDeveloperProductsAction() {
            super("Filter Developer's Products", "Filter Products to get only the current developer's products", AllIcons.General.Filter);
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
        if(allProductsTree != null) {
            treeExpansionState = allProductsTree.getExpansionState();
        }
        productList.clear();
        addFilteredProductsToProductList();
        if (!productList.isEmpty()) {
            buildProductTree();
            allProductsTree.setExpansionState(treeExpansionState);
            setContent(ScrollPaneFactory.createScrollPane(allProductsTree));
        } else {
            setContent(new JLabel("start a session with a task to associate a product with your account", SwingConstants.CENTER));
        }
    }

    private void addFilteredProductsToProductList() {
        ProductService productService = new ProductService(project);
        productList = productService.getProductsByDeveloper();
    }

    private class StartRecordingEventsAction extends DumbAwareAction {
        StartRecordingEventsAction() {
            super("Start/Resume Swarm Debugging Session", "Start recording breakpoint and debugging events in the selected task", AllIcons.Actions.Execute);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if(TreeSelectionProvider.getTreeNode() instanceof Task) {
                createSwarmSession();
            } else {
                currentSession = (Session) TreeSelectionProvider.getTreeNode();
            }
            allProductsTree.removeMouseListener(rightClickPopupMenuMouseAdapter);
            buildAllProductTreeView();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(false);
            if(developer.isLoggedIn()) {
                if (!currentSession.isActive()) {
                    if (TreeSelectionProvider.getTreeNode() instanceof Task) {
                        e.getPresentation().setEnabled(true);
                    } else if (TreeSelectionProvider.getTreeNode() instanceof Session) {
                        Session session = (Session) TreeSelectionProvider.getTreeNode();
                        if (!session.isFinished()) {
                            e.getPresentation().setEnabled(true);
                        }
                    }
                }
            }
        }
    }

    private void createSwarmSession() {
        Task task = (Task) TreeSelectionProvider.getTreeNode();
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
            buildAllProductTreeView();
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
            allProductsTree = null;
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