package com.swarm.models;

import com.swarm.States;
import com.swarm.utils.HTTPRequest;
import org.json.JSONObject;

public class Event {
    private int id;
    private Session session;
    private Method method;
    private int lineNumber;
    private String kind;

    public void create() {
        HTTPRequest createEventRequest = new HTTPRequest();
        createEventRequest.setUrl(States.URL);
        createEventRequest.setQuery("mutation eventCreate($sessionId:Long!,$lineNumber:Int!,$eventKind:String!,$methodId:Long!)" +
                "{eventCreate(event:{session:{id:$sessionId},lineNumber:$lineNumber,kind:$eventKind,method:{id:$methodId}}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("sessionId", session.getId());
        variables.put("lineNumber", lineNumber);
        variables.put("eventKind", kind);
        variables.put("methodId", method.getId());
        createEventRequest.setVariables(variables);
        JSONObject response = new JSONObject(createEventRequest.post().getString("body"));

        this.id = response.getJSONObject("data").getJSONObject("eventCreate").getInt("id");
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}
