package com.swarm.models;

import com.swarm.States;
import com.swarm.tools.HTTPRequest;
import org.json.JSONObject;

public class Breakpoint {
    private int id;
    private Type type;
    private int lineNumber;

    public void create() {
        HTTPRequest createBreakpointRequest = new HTTPRequest();
        createBreakpointRequest.setUrl(States.URL);
        createBreakpointRequest.setQuery("mutation breakpointCreate($typeId:Long!,$lineNumber:Int!)" +
                "{breakpointCreate(breakpoint:{type:{id:$typeId},lineNumber:$lineNumber}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("typeId", type.getId());
        variables.put("lineNumber", lineNumber);
        createBreakpointRequest.setVariables(variables);
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
}