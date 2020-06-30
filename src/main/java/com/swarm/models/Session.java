package com.swarm.models;

import com.swarm.States;
import com.swarm.tools.HTTPRequest;
import org.json.JSONObject;

public class Session {
    private int id;
    private Developer developer;
    private Task task;


    public void start() {
        HTTPRequest startSession = new HTTPRequest();
        startSession.setUrl(States.URL);
        startSession.setQuery("mutation sessionStart($developerId:Long!,$taskId:Long!)" +
        "{sessionStart(session:{developer:{id:$developerId},task:{id:$taskId,done:false}}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("developerId", developer.getId());
        variables.put("taskId", task.getId());
        startSession.setVariables(variables);
        JSONObject response = new JSONObject(startSession.post().getString("body"));

        this.id = response.getJSONObject("data").getJSONObject("sessionStart").getInt("id");
    }

    public void createSessionForDeveloperLinking() {
        HTTPRequest createSessionForProductLink = new HTTPRequest();
        createSessionForProductLink.setUrl(States.URL);
        createSessionForProductLink.setQuery("mutation sessionCreate($developerId:Long!,$taskId:Long!,$done:Boolean!)" +
                "{sessionCreate(session:{developer:{id:$developerId},task:{id:$taskId,done:$done}}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("developerId", developer.getId());
        variables.put("taskId", task.getId());
        variables.put("done", task.isDone());
        createSessionForProductLink.setVariables(variables);
        JSONObject response = new JSONObject(createSessionForProductLink.post().getString("body"));

        this.id = response.getJSONObject("data").getJSONObject("sessionCreate").getInt("id");
    }

    public int getId() {
        return id;
    }

    public void setDeveloper(Developer developer) {
        this.developer = developer;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
