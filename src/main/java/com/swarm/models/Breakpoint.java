package com.swarm.models;

import com.swarm.utils.HTTPRequest;
import org.json.JSONObject;

public class Breakpoint {
    private int id;
    private Type type;
    private int lineNumber;

    public void create() {
        HTTPRequest createBreakpointRequest = new HTTPRequest();
        createBreakpointRequest.setQuery("mutation breakpointCreate($typeId:Long!,$lineNumber:Int!)" +
                "{breakpointCreate(breakpoint:{type:{id:$typeId},lineNumber:$lineNumber}){id}}");
        createBreakpointRequest.addVariable("typeId", type.getId());
        createBreakpointRequest.addVariable("lineNumber", lineNumber);
        JSONObject response = new JSONObject(createBreakpointRequest.post().getString("body"));

        this.id = response.getJSONObject("data").getJSONObject("breakpointCreate").getInt("id");
    }

    public int getId() {
        return id;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public String toString() {
        return "line " + lineNumber + " in class " + type.getFullName();
    }
}
