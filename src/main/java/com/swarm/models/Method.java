package com.swarm.models;

import com.swarm.utils.HTTPRequest;
import org.json.JSONObject;

import java.util.Objects;

public class Method {
    private int id;
    private Type type;
    private String signature;
    private String name;

    public void create() {
        HTTPRequest createMethodRequest = new HTTPRequest();
        createMethodRequest.setQuery("mutation methodCreate($typeId:Long!,$signature:String!,$name:String!)" +
                "{methodCreate(method:{type:{id:$typeId},signature:$signature,name:$name}){id}}");
        createMethodRequest.addVariable("typeId", type.getId());
        createMethodRequest.addVariable("name", name);
        createMethodRequest.addVariable("signature", signature);
        JSONObject response = new JSONObject(createMethodRequest.post().getString("body"));

        this.id = response.getJSONObject("data").getJSONObject("methodCreate").getInt("id");
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        final Method other = (Method) obj;
        return this.hashCode() == other.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.name);
        hash = 67 * hash + Objects.hashCode(this.type.getFullName());
        return hash;
    }
}
