package com.swarm.toolWindow;

import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import com.swarm.States;
import com.swarm.models.Developer;
import com.swarm.models.Product;
import com.swarm.models.Session;
import com.swarm.models.Task;
import com.swarm.mouseAdapters.rightClickPopupMenuMouseAdapter;
import com.swarm.popupMenu.CreateProductDialog;
import com.swarm.popupMenu.CreateSessionDialog;
import com.swarm.popupMenu.CreateTaskDialog;
import com.swarm.utils.HTTPRequest;
import com.swarm.tree.ProductTree;
import com.swarm.tree.ProductTreeNode;
import com.swarm.tree.ProductTreeRenderer;
import icons.SwarmIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.util.ArrayList;

public class ProductToolWindow extends SimpleToolWindowPanel implements DumbAware {

    private final ToolWindow toolWindow;
    private final Project project;
    private final Developer developer;

    private final ArrayList<Product> productList = new ArrayList<>();
    private ProductTreeNode allProductsNode;
    private ProductTree allProductsTree;


    public ProductToolWindow(ToolWindow toolWindow, Project project, Developer developer) {
        super(true, true);

        this.toolWindow = toolWindow;
        this.project = project;
        this.developer = developer;

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

    private void buildProductTreeView() {
        productList.clear();
        addProductsToProductList();
        if (!productList.isEmpty()) {
            buildProductTree();
        } else {
            displayNoProductsMessage();
        }
    }

    private void addProductsToProductList() {
        addProductsLinkedToTasks();
        addRemainingProducts();
    }

    private void addProductsLinkedToTasks() {
        JSONObject data = fetchTasks();
        if (!data.isNull("tasks")) {
            JSONArray tasks = data.getJSONArray("tasks");
            buildProductsFromTasks(tasks);
        }
    }

    private JSONObject fetchTasks() {
        HTTPRequest fetchTasks = new HTTPRequest();
        fetchTasks.setUrl(States.URL);
        fetchTasks.setQuery("{tasks{product{id,name},id,title,done}}");
        JSONObject response = new JSONObject(fetchTasks.post().getString("body"));
        return response.getJSONObject("data");
    }

    private void buildProductsFromTasks(JSONArray tasks) {
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject jsonTask = tasks.getJSONObject(i);
            Task newTask = new Task();
            newTask.setId(jsonTask.getInt("id"));
            newTask.setTitle(jsonTask.getString("title"));
            newTask.setDone(jsonTask.getBoolean("done"));
            int index = productIsInArray(jsonTask.getJSONObject("product").getInt("id"));
            if (index != -1) {
                productList.get(index).addTask(newTask);
            } else {
                Product newProduct = new Product();
                newProduct.setId(jsonTask.getJSONObject("product").getInt("id"));
                newProduct.setName(jsonTask.getJSONObject("product").getString("name"));
                newProduct.addTask(newTask);
                productList.add(newProduct);
            }
        }
    }

    private int productIsInArray(int productId) {
        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).getId() == productId) {
                return i;
            }
        }
        return -1;
    }

    private void addRemainingProducts() {
        JSONObject data = fetchAllProducts();
        if (!data.isNull("allProducts")) {
            JSONArray products = data.getJSONArray("allProducts");
            buildRemainingProducts(products);
        }
    }

    private JSONObject fetchAllProducts() {
        HTTPRequest fetchAllProducts = new HTTPRequest();
        fetchAllProducts.setUrl(States.URL);
        fetchAllProducts.setQuery("{allProducts{id,name}}");
        JSONObject response = new JSONObject(fetchAllProducts.post().getString("body"));
        return response.getJSONObject("data");
    }

    private void buildRemainingProducts(JSONArray products) {
        for (int i = 0; i < products.length(); i++) {
            JSONObject jsonProduct = products.getJSONObject(i);
            if(productIsInArray(jsonProduct.getInt("id")) == -1) {
                Product product = new Product();
                product.setId(jsonProduct.getInt("id"));
                product.setName(jsonProduct.getString("name"));
                productList.add(product);
            }
        }
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
        allProductsTree.addMouseListener(new rightClickPopupMenuMouseAdapter(project, developer, toolWindow));
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

    private void displayNoProductsMessage() {
        Notification notification = new Notification("SwarmDebugging", SwarmIcons.Ant, NotificationType.INFORMATION);
        notification.setTitle("No products");
        notification.setContent("Create a new product to get started");
        Notifications.Bus.notify(notification);
    }

    private final class AddProductAction extends DumbAwareAction {
        AddProductAction() {
            super("Add a New Product", "Add a new product to the developer's products", AllIcons.General.Add);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            addNewProduct();
            buildToolWindowContent();
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
            buildToolWindowContent();
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(getSelectedProductFromTree() != null);
        }
    }

    private void addNewTaskToSelectedProduct() {
        ProductTreeNode productTreeNode = getSelectedProductFromTree();
        if (productTreeNode == null) {
            return;
        }
        Product product = new Product();
        product.setId(productTreeNode.getId());
        CreateTaskDialog createTaskDialog = new CreateTaskDialog(project, product, developer);
        createTaskDialog.showAndGet();
    }

    private ProductTreeNode getSelectedProductFromTree() {
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

    private final class RefreshAction extends DumbAwareAction {
        RefreshAction() {
            super("Refresh Products", "Refresh the developer's products", AllIcons.Actions.Refresh);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            buildToolWindowContent();
        }
    }

    private final class MarkTaskAsDoneAction extends DumbAwareAction {
        MarkTaskAsDoneAction() {
            super("Mark Task as Done", "Mark the selected task as done", AllIcons.Actions.Commit);
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
        ProductTreeNode taskNode = getSelectedTaskFromTree();
        if (taskNode == null) {
            return;
        }
        Task task = new Task();
        task.setId(taskNode.getId());
        task.markAsDone();
    }

    private ProductTreeNode getSelectedTaskFromTree() {
        ProductTreeNode node = (ProductTreeNode) allProductsTree.getLastSelectedPathComponent();
        if (node == null || node.isRoot()) {
            return null;
        }
        if (node.isTask()) {
            return node;
        }
        return null;
    }

    private class StartRecordingEventsAction extends DumbAwareAction {
        StartRecordingEventsAction() {
            super("Start Recording Events", "Start recording breakpoint and debugging events in the selected task", AllIcons.Debugger.Db_set_breakpoint);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            createSwarmSession();
            switchToolWindowContentToSessionToolWindow(new SessionToolWindow(States.currentSession, toolWindow, project, developer));
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(getSelectedTaskFromTree() != null);
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
        if(createSessionDialog.showAndGet()) {
            Session session = new Session();
            session.setTask(task);
            session.setDeveloper(developer);
            session.setDescription(createSessionDialog.getDescription());
            session.start();
        }
    }

    private void switchToolWindowContentToSessionToolWindow(SessionToolWindow sessionToolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(sessionToolWindow.getContent(), "", false);
        toolWindow.getContentManager().removeAllContents(true);
        toolWindow.getContentManager().addContent(content);
    }

    private class LogoutAction extends DumbAwareAction {
        LogoutAction() {
            super("Logout", "Logs out developer and takes him back to login screen", SwarmIcons.Logout);
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
