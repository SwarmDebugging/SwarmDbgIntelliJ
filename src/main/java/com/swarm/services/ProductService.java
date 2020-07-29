package com.swarm.services;

import com.swarm.models.Product;
import com.swarm.models.Session;
import com.swarm.models.Task;
import com.swarm.toolWindow.ProductToolWindow;
import com.swarm.utils.HTTPRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProductService {
    
    private ArrayList<Product> productList;

    public ArrayList<Product> getAllProducts() {
        productList = new ArrayList<>();
        addProductsLinkedToSessions();
        addRemainingProducts();
        addRemainingTasks();
        return productList;
    }

    private void addProductsLinkedToSessions() {
        JSONObject data = fetchSessions();
        if (!data.isNull("sessions")) {
            JSONArray sessions = data.getJSONArray("sessions");
            buildProductsFromSessions(sessions);
        }
    }

    private JSONObject fetchSessions() {
        HTTPRequest fetchSessions = new HTTPRequest();
        fetchSessions.setQuery("{sessions{id,description,task{id,title,done,product{id,name}}}}");
        JSONObject response = new JSONObject(fetchSessions.post().getString("body"));
        return response.getJSONObject("data");
    }

    //TODO: This method is very long (40 lines)
    private void buildProductsFromSessions(JSONArray sessions) {
        for (int i = 0; i < sessions.length(); i++) {
            JSONObject jsonSession = sessions.getJSONObject(i);

            int index = productIsInArray(jsonSession.getJSONObject("task").getJSONObject("product").getInt("id"));
            if (index != -1) {
                Product product = productList.get(index);

                boolean isInProduct = false;
                for (Task task: product.getTasks()){
                    if(task.getId() == jsonSession.getJSONObject("task").getInt("id")){
                        isInProduct = true;
                        Session session = new Session();
                        session.setId(jsonSession.getInt("id"));
                        session.setDescription(jsonSession.getString("description"));
                        task.addSession(session);
                        break;
                    }
                }

                 if(!isInProduct) {
                    Task task = new Task();
                    task.setId(jsonSession.getJSONObject("task").getInt("id"));
                    task.setTitle(jsonSession.getJSONObject("task").getString("title"));
                    task.setDone(jsonSession.getJSONObject("task").getBoolean("done"));
                    productList.get(index).addTask(task);

                     Session session = new Session();
                     session.setId(jsonSession.getInt("id"));
                     session.setDescription(jsonSession.getString("description"));
                     task.addSession(session);
                }
            } else {
                Product product = new Product();
                product.setId(jsonSession.getJSONObject("task").getJSONObject("product").getInt("id"));
                product.setName(jsonSession.getJSONObject("task").getJSONObject("product").getString("name"));

                Task task = new Task();
                task.setId(jsonSession.getJSONObject("task").getInt("id"));
                task.setDone(jsonSession.getJSONObject("task").getBoolean("done"));
                task.setTitle(jsonSession.getJSONObject("task").getString("title"));

                Session session = new Session();
                session.setId(jsonSession.getInt("id"));
                session.setDescription(jsonSession.getString("description"));
                task.addSession(session);

                product.addTask(task);

                productList.add(product);
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

    private void addRemainingTasks() {
        JSONObject data = fetchAllTasks();
        if(!data.isNull("tasks")) {
            JSONArray tasks = data.getJSONArray("tasks");
            buildRemainingTasks(tasks);
        }
    }

    private JSONObject fetchAllTasks() {
        HTTPRequest fetchAllTasks = new HTTPRequest();
        fetchAllTasks.setQuery("{tasks{id,title,done,product{id}}}");
        JSONObject response = new JSONObject(fetchAllTasks.post().getString("body"));
        return response.getJSONObject("data");
    }

    private void buildRemainingTasks(JSONArray tasks) {
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject jsonProduct = tasks.getJSONObject(i).getJSONObject("product");
            int productIndex = productIsInArray(jsonProduct.getInt("id"));
            Product product = productList.get(productIndex);

            boolean isInProduct = false;
            for (Task task: product.getTasks()){
                if(task.getId() == tasks.getJSONObject(i).getInt("id")){
                    isInProduct = true;
                    break;
                }
            }

            if(isInProduct) continue;

            JSONObject jsonTask = tasks.getJSONObject(i);
            Task task = new Task();
            task.setId(jsonTask.getInt("id"));
            task.setDone(jsonTask.getBoolean("done"));
            task.setTitle(jsonTask.getString("title"));
            product.addTask(task);
        }
    }

    public ArrayList<Product> getProductsByDeveloper() {
        productList = new ArrayList<>();
        addProductsLinkedToDevelopersTasks();
        return productList;
    }

    private void addProductsLinkedToDevelopersTasks() {
        JSONObject data = fetchDevelopersTasks();
        if(!data.isNull("tasks")) {
            JSONArray tasks = data.getJSONArray("tasks");
            buildProductsFromSessions(tasks);
        }
    }

    private JSONObject fetchDevelopersTasks() {
        HTTPRequest fetchTasks = new HTTPRequest();
        fetchTasks.setQuery("{tasks(developerId:" + ProductToolWindow.getDeveloper().getId() + "){product{id,name},id,title,done}}");
        JSONObject response = new JSONObject(fetchTasks.post().getString("body"));
        return response.getJSONObject("data");
    }
}
