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
import com.swarm.models.Developer;
import com.swarm.models.Product;
import com.swarm.models.Session;
import com.swarm.models.Task;
import com.swarm.popupMenu.CreateProductDialog;
import com.swarm.popupMenu.CreateTaskDialog;
import com.swarm.popupMenu.PopupMenuBuilder;
import com.swarm.utils.HTTPRequest;
import com.swarm.tree.ProductTree;
import com.swarm.tree.ProductTreeNode;
import com.swarm.tree.ProductTreeRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ProductToolWindow extends SimpleToolWindowPanel implements DumbAware {

    private final int RIGHT_CLICK = MouseEvent.BUTTON3;

    private final ToolWindow toolWindow;
    private final Project project;
    private final Developer developer;

    private final PopupMenuBuilder popupMenuBuilder;

    private final ArrayList<Product> productList = new ArrayList<>();
    private ProductTreeNode allProductsNode;
    private ProductTree allProductsTree;


    public ProductToolWindow(ToolWindow toolWindow, Project project, Developer developer) {
        super(true, true);

        this.toolWindow = toolWindow;
        this.project = project;
        this.developer = developer;
        popupMenuBuilder = new PopupMenuBuilder(toolWindow, project, developer);

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
        fetchProducts();
        if (!productList.isEmpty()) {
            buildProductTree();
        } else {
            displayNoProductsMessage();
        }
    }

    //not very efficient todo:refractor so it's more readable
    private void fetchProducts() {
        HTTPRequest fetchTasks = new HTTPRequest();
        fetchTasks.setUrl(States.URL);
        fetchTasks.setQuery("{tasks{product{id,name},id,title,done}}");
        JSONObject response = new JSONObject(fetchTasks.post().getString("body"));
        JSONObject data = response.getJSONObject("data");

        if (!data.isNull("tasks")) {
            JSONArray tasks = data.getJSONArray("tasks");
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

        HTTPRequest fetchAllProducts = new HTTPRequest();
        fetchAllProducts.setUrl(States.URL);
        fetchAllProducts.setQuery("{allProducts{id,name}}");
        response = new JSONObject(fetchAllProducts.post().getString("body"));
        data = response.getJSONObject("data");
        if (!data.isNull("allProducts")) {
            JSONArray products = data.getJSONArray("allProducts");
            for (int i = 0; i < products.length(); i++) {
                JSONObject jsonProduct = products.getJSONObject(i);
                boolean found = false;
                for (Product value : productList) {
                    if (value.getId() == jsonProduct.getInt("id")) {
                        found = true;
                    }
                }
                if(!found) {
                    Product product = new Product();
                    product.setId(jsonProduct.getInt("id"));
                    product.setName(jsonProduct.getString("name"));
                    productList.add(product);
                }
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

                    ProductTreeNode node = (ProductTreeNode) allProductsTree.getLastSelectedPathComponent();
                    if (node == null || node.isRoot()) {
                        return;
                    }
                    //TODO: refresh after
                    if (node.isTask()) {
                        Task task = new Task();
                        task.setId(node.getId());
                        JPopupMenu popupMenu = popupMenuBuilder.buildTaskNodePopupMenu(task);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    } else if (node.isProduct()) {
                        Product product = new Product();
                        product.setId(node.getId());
                        JPopupMenu popupMenu = popupMenuBuilder.buildProductNodePopupMenu(product);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
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

    //TODO: make this function
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
        CreateProductDialog createProductDialog = new CreateProductDialog(project);
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
            super("Start Recording Events",
                    "Start recording breakpoint and debugging events in the selected task",
                    IconLoader.getIcon("/icons/startRecordingEvents.svg"));
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            States.currentSession.setId(createSwarmSession());
            switchToolWindowContentToSessionToolWindow(new SessionToolWindow(States.currentSession, toolWindow, project, developer));
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            super.update(e);
            e.getPresentation().setEnabled(getSelectedTaskFromTree() != null);
        }
    }

    private int createSwarmSession() {
        ProductTreeNode taskNode = getSelectedTaskFromTree();
        if (taskNode == null) {
            return -1;
        }
        Task task = new Task();
        task.setId(taskNode.getId());
        Session session = new Session();
        session.setDeveloper(developer);
        session.setTask(task);
        session.start();
        return session.getId();
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
