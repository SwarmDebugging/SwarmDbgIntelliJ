package com.swarm.utils;

import kong.unirest.Unirest;
import kong.unirest.HttpResponse;
import org.json.JSONObject;


public class HTTPRequest {

    private final String URL = "http://localhost:8080/graphql";
    private String query;
    private final JSONObject variables = new JSONObject();

    public JSONObject post() {
        JSONObject body = new JSONObject();
        body.put("query",query);
        if(!variables.isEmpty()) {
            body.put("variables", variables);
        }
        HttpResponse<String> response = Unirest.post(URL)
                .header("content-type", "application/json")
                .body(body.toString())
                .asString();

        return new JSONObject(response);
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
