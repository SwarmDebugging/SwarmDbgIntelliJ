package com.swarm.models;

import com.swarm.States;
import com.swarm.tools.HTTPRequest;
import org.json.JSONObject;

public class Type {
    private int id;
    private Session session;
    private String fullName;
    private String name;
    private String fullPath;
    String sourceCode;

    public Type(){}

    public void create() {
        HTTPRequest createTypeRequest = new HTTPRequest();
        createTypeRequest.setUrl(States.URL);
        createTypeRequest.setQuery("mutation typeCreate($sessionId:Long!,$name:String!,$fullPath:String!,$fullName:String!,$source:String){" +
                "typeCreate(typeWrapper:{type:{session:{id:$sessionId},name:$name,fullPath:$fullPath,fullName:$fullName},source:$source}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("sessionId", session.getId());
        variables.put("name", name);
        variables.put("fullPath", fullPath);
        variables.put("fullName", fullName);
        variables.put("source", sourceCode);
        createTypeRequest.setVariables(variables);
        JSONObject response = new JSONObject(createTypeRequest.post().getString("body"));

        this.id = response.getJSONObject("data").getJSONObject("typeCreate").getInt("id");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
}