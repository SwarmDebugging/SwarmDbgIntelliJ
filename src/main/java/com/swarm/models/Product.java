package com.swarm.models;

import com.swarm.States;
import com.swarm.utils.HTTPRequest;
import org.json.JSONObject;

import java.util.ArrayList;

public class Product {
    private int id;
    private String name;
    private final ArrayList<Task> tasks = new ArrayList<>();

    public void create() {
        HTTPRequest createProductRequest = new HTTPRequest();
        createProductRequest.setUrl(States.URL);
        createProductRequest.setQuery("mutation productCreate($name: String!){productCreate(product:{name:$name}){id}}");
        createProductRequest.addVariable("name", name);
        JSONObject response = new JSONObject(createProductRequest.post().getString("body"));

        this.id = response.getJSONObject("data").getJSONObject("productCreate").getInt("id");
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
}
