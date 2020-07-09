package com.swarm.models;

import com.swarm.utils.States;
import com.swarm.utils.HTTPRequest;
import org.json.JSONException;
import org.json.JSONObject;

public class Session {
    private int id;
    private Developer developer;
    private Task task;
    private String description;


    public void stop() {
        HTTPRequest stopSession = new HTTPRequest();
        stopSession.setUrl(States.URL);
        stopSession.setQuery("mutation sessionStop($sessionId:Long!)" +
                "{sessionStop(id:$sessionId){id}}");
        stopSession.addVariable("sessionId", id);
        JSONObject response = new JSONObject(stopSession.post().getString("body"));

        try {
            response.getJSONObject("data").getJSONObject("sessionStop").getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        clear();
    }

    private void clear() {
        id = 0;
        developer = null;
        task = null;
        description = "";
    }

    public void start() {
        HTTPRequest startSession = new HTTPRequest();
        startSession.setUrl(States.URL);
        startSession.setQuery("mutation sessionStart($description:String!,$developerId:Long!,$taskId:Long!)" +
        "{sessionStart(session:{description:$description,developer:{id:$developerId},task:{id:$taskId,done:false}}){id}}");
        startSession.addVariable("description", description);
        startSession.addVariable("developerId", developer.getId());
        startSession.addVariable("taskId", task.getId());
        JSONObject response = new JSONObject(startSession.post().getString("body"));

        this.id = response.getJSONObject("data").getJSONObject("sessionStart").getInt("id");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDeveloper(Developer developer) {
        this.developer = developer;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
