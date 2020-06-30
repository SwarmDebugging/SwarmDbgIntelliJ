package com.swarm.models;

import com.swarm.States;
import com.swarm.tools.HTTPRequest;
import org.json.JSONObject;

import java.util.ArrayList;

public class Product {
    private int id;
    private String name;
    private Developer developer;
    private final ArrayList<Task> tasks = new ArrayList<>();

    public Product() {}

    public Product(int id, String name) {
        this.id = id;
        this.name = name;
    }

    //TODO: this needs to create a task too for association with developer
    public void create() {
        HTTPRequest createProductRequest = new HTTPRequest();
        createProductRequest.setUrl(States.URL);
        createProductRequest.setQuery("mutation productCreate($name: String!){productCreate(product:{name:$name}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("name", name);
        createProductRequest.setVariables(variables);
        JSONObject response = new JSONObject(createProductRequest.post().getString("body"));

        this.id = response.getJSONObject("data").getJSONObject("productCreate").getInt("id");

        createTaskLinkingWithDeveloper();
    }

    private void createTaskLinkingWithDeveloper() {
        Task task = new Task();
        task.setProduct(this);
        task.setDone(true);
        task.setTitle("productCreation");
        task.create();
        createSessionLinkingWithTask(task);
    }

    private void createSessionLinkingWithTask(Task task) {
        Session session = new Session();
        session.setDeveloper(developer);
        session.setTask(task);
        session.createForNewProductLink();
    }


    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
    }

    public void setDeveloper(Developer developer) {
        this.developer = developer;
    }
}
