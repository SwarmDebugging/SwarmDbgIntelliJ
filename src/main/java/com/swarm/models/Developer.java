package com.swarm.models;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.util.IconLoader;
import com.swarm.utils.HTTPRequest;
import org.json.JSONException;
import org.json.JSONObject;

import static com.swarm.States.URL;

public class Developer {

    private int id;
    private String username;

    public void login() {
        HTTPRequest loginRequest = new HTTPRequest();
        loginRequest.setUrl(URL);
        loginRequest.setQuery("{developer(username:\"" + username + "\"){id}}");
        JSONObject response = new JSONObject(loginRequest.post().getString("body"));
        readIdFromLoginResponseBody(response);
    }

    private void readIdFromLoginResponseBody(JSONObject responseBody) {
        try {
            this.id = responseBody.getJSONObject("data").getJSONObject("developer").getInt("id");
        } catch (JSONException exception) {
            showWrongLoginNotification();
        }
    }

    private void showWrongLoginNotification() {
        Notification notification = new Notification("SwarmDebugging", IconLoader.getIcon("/icons/ant.svg"), NotificationType.INFORMATION);
        notification.setTitle("Wrong Username");
        notification.setContent("Try again or create a new account");
        Notifications.Bus.notify(notification);
    }

   public void registerNewDeveloper() {
        HTTPRequest registerRequest = new HTTPRequest();
        registerRequest.setUrl(URL);
        registerRequest.setQuery("mutation developerCreate($username: String!){developerCreate(developer:{username:$username}){id}}");
        registerRequest.addVariable("username", username);
        JSONObject response = new JSONObject(registerRequest.post().getString("body"));
        readIdFromDeveloperCreateResponseBody(response);
    }

    private void readIdFromDeveloperCreateResponseBody(JSONObject responseBody) {
        try {
            this.id = responseBody.getJSONObject("data").getJSONObject("developerCreate").getInt("id");
        } catch (JSONException exception) {
            showDeveloperAlreadyExistsNotification();
        }
    }

    private void showDeveloperAlreadyExistsNotification() {
        Notification notification = new Notification("SwarmDebugging", IconLoader.getIcon("/icons/ant.svg"), NotificationType.INFORMATION);
        notification.setTitle("Developer Already Exists");
        notification.setContent("Choose another username");
        Notifications.Bus.notify(notification);
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
