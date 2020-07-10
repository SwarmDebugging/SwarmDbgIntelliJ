package com.swarm.models;

import com.swarm.utils.HTTPRequest;
import org.json.JSONObject;

public class Type {
    private int id;
    private Session session;
    private String fullName;
    private String name;
    private String fullPath;
    private String sourceCode;

    public Type(){}

    public void create() {
        HTTPRequest createTypeRequest = new HTTPRequest();
        createTypeRequest.setQuery("mutation typeCreate($sessionId:Long!,$name:String!,$fullPath:String!,$fullName:String!,$source:String){" +
                "typeCreate(typeWrapper:{type:{session:{id:$sessionId},name:$name,fullPath:$fullPath,fullName:$fullName},source:$source}){id}}");
        createTypeRequest.addVariable("sessionId", session.getId());
        createTypeRequest.addVariable("name", name);
        createTypeRequest.addVariable("fullPath", fullPath);
        createTypeRequest.addVariable("fullName", fullName);
        createTypeRequest.addVariable("source", sourceCode);
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

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
}
