package com.swarm.models;

import com.swarm.States;
import com.swarm.tools.HTTPRequest;
import org.json.JSONObject;

public class Task {
    private int id;
    private String title;
    private boolean done;
    private Product product;

    public Task() {}

    public Task(int id, String title, boolean done) {
        this.id = id;
        this.done = done;
        this.title = title;
    }

    public void create() {
        HTTPRequest createTaskRequest = new HTTPRequest();
        createTaskRequest.setUrl(States.URL);
        createTaskRequest.setQuery("mutation taskCreate($title:String!,$done:Boolean!,$productId:Long!)" +
                "{taskCreate(task:{title:$title,done:$done,product:{id:$productId}}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("title", title);
        variables.put("done", done);
        variables.put("productId", product.getId());
        createTaskRequest.setVariables(variables);
        JSONObject response = new JSONObject(createTaskRequest.post().getString("body"));

        this.id = response.getJSONObject("data").getJSONObject("taskCreate").getInt("id");
    }


    public int getId() {
        return id;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isDone() {
        return done;
    }

}
