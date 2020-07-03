package com.swarm.models;

import com.swarm.utils.HTTPRequest;
import org.json.JSONObject;

import static com.swarm.States.URL;

public class Developer {

    private int id;
    private String username;

    public void login() {
        HTTPRequest loginRequest = new HTTPRequest();
        loginRequest.setUrl(URL);
        loginRequest.setQuery("{developer(username:\"" + username + "\"){id}}");
        //TODO: try catch errors
        JSONObject response = new JSONObject(loginRequest.post().getString("body"));

        this.id = response.getJSONObject("data").getJSONObject("developer").getInt("id");
    }

    //handle develpoer already exist exception
   public void registerNewDeveloper() {
        HTTPRequest registerRequest = new HTTPRequest();
        registerRequest.setUrl(URL);
        registerRequest.setQuery("mutation developerCreate($username: String!){developerCreate(developer:{username:$username}){id}}");
        registerRequest.addVariable("username", username);
        JSONObject response = new JSONObject(registerRequest.post().getString("body"));

        this.id = response.getJSONObject("data").getJSONObject("developerCreate").getInt("id");
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
