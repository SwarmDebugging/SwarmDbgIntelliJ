package com.swarm.models;

import com.swarm.utils.States;
import com.swarm.utils.HTTPRequest;
import org.json.JSONObject;

public class Invocation {
    private int id;
    private Session session;
    private Method invoking;
    private Method invoked;

    public void create() {
        HTTPRequest createInvocationRequest = new HTTPRequest();
        createInvocationRequest.setUrl(States.URL);
        createInvocationRequest.setQuery("mutation invocationCreate($sessionId:Long!,$invokingId:Long!,$invokedId:Long!)" +
                "{invocationCreate(invocation:{session:{id:$sessionId},invoking:{id:$invokingId},invoked:{id:$invokedId},virtual:false}){id}}");
        createInvocationRequest.addVariable("sessionId", session.getId());
        createInvocationRequest.addVariable("invokingId", invoking.getId());
        createInvocationRequest.addVariable("invokedId", invoked.getId());
        JSONObject response = new JSONObject(createInvocationRequest.post().getString("body"));

        this.id = response.getJSONObject("data").getJSONObject("invocationCreate").getInt("id");
    }

    public int getId() {
        return id;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setInvoking(Method invoking) {
        this.invoking = invoking;
    }

    public void setInvoked(Method invoked) {
        this.invoked = invoked;
    }
}
