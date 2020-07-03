package com.swarm.utils;

import kong.unirest.Unirest;
import kong.unirest.HttpResponse;
import org.json.JSONObject;


public class HTTPRequest {

    private String url;
    private String query;
    private final JSONObject variables = new JSONObject(); //TODO:this should have an add variable function so that higher call don't manipulate low-level json objexts

    public JSONObject post() {
        JSONObject body = new JSONObject();
        body.put("query",query);
        if(!variables.isEmpty()) {
            body.put("variables", variables);
        }
        HttpResponse<String> response = Unirest.post(url)
                .header("content-type", "application/json")
                .body(body.toString())
                .asString();

        return new JSONObject(response);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void addVariable(String name, String value) {
        variables.put(name, value);
    }

    public void addVariable(String name, Boolean value) {
        variables.put(name, value);
    }

    public void addVariable(String name, int value) {
        variables.put(name, value);
    }
}