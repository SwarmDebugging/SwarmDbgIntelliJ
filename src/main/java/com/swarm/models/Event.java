package com.swarm.models;

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
        createEventRequest.setQuery("mutation eventCreate($sessionId:Long!,$lineNumber:Int!,$eventKind:String!,$methodId:Long!)" +
                "{eventCreate(event:{session:{id:$sessionId},lineNumber:$lineNumber,kind:$eventKind,method:{id:$methodId}}){id}}");
        createEventRequest.addVariable("sessionId", session.getId());
        createEventRequest.addVariable("lineNumber", lineNumber);
        createEventRequest.addVariable("eventKind", kind);
        createEventRequest.addVariable("methodId", method.getId());
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
