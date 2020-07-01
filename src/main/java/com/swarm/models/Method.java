package com.swarm.models;

import com.swarm.States;
import com.swarm.tools.HTTPRequest;
import org.json.JSONObject;

public class Method {
    private int id;
    private Type type;
    private String signature;
    private String name;

    public void create() {
        HTTPRequest createMethodRequest = new HTTPRequest();
        createMethodRequest.setUrl(States.URL);
        createMethodRequest.setQuery("mutation methodCreate($typeId:Long!,$signature:String!,$name:String!)" +
                "{methodCreate(method:{type:{id:$typeId},signature:$signature,name:$name}){id}}");
        JSONObject variables = new JSONObject();
        variables.put("typeId", type.getId());
        variables.put("name", name);
        variables.put("signature", signature);
        createMethodRequest.setVariables(variables);
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
}
