package com.swarm.utils;

import kong.unirest.Unirest;
import kong.unirest.HttpResponse;
import org.json.JSONObject;


public class HTTPRequest {

    private String url;
    private String query;
    private JSONObject variables; //TODO:this should have an add variable function so that higher call don't manipulate low-level json objexts

    public JSONObject post() {
        JSONObject body = new JSONObject();
        body.put("query",query);
        if(variables != null) {
            body.put("variables", variables);
        }
        String bodyString = body.toString(); //TODO: remove after debugging
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

    public void setVariables(JSONObject variables) {
        this.variables = variables;
    }
}
