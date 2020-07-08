package com.swarm.services;

import com.swarm.States;
import com.swarm.models.Product;
import com.swarm.models.Task;
import com.swarm.utils.HTTPRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProductService {
    
    private ArrayList<Product> productList;

    public ArrayList<Product> getAllProducts() {
        productList = new ArrayList<>();
        addProductsLinkedToTasks();
        addRemainingProducts();
        return productList;
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
}
