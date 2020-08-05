package com.swarm.services;

import com.swarm.models.Invocation;
import com.swarm.models.Method;
import com.swarm.models.Task;
import com.swarm.models.Type;
import com.swarm.utils.HTTPRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class VisualizationService {
    private ArrayList<Invocation> invocationList;

    public ArrayList<Invocation> invocationsByTask(Task task) {
        invocationList = new ArrayList<>();
        HTTPRequest getInvocationsByTaskRequest = new HTTPRequest();
        getInvocationsByTaskRequest.setQuery("query invocations($taskId:Long!){invocations(taskId:$taskId){invoked{type{fullName}}invoking{type{fullName}}}}");
        getInvocationsByTaskRequest.addVariable("taskId", task.getId());
        JSONObject response = new JSONObject(getInvocationsByTaskRequest.post().getString("body"));

        if((!response.getJSONObject("data").isNull("invocations"))) {
            JSONArray sessions = response.getJSONObject("data").getJSONArray("invocations");
            addJSONInvocationsToInvocationList(sessions);
        }

        return invocationList;
    }

    private void addJSONInvocationsToInvocationList(JSONArray invocations) {
        for (int i = 0; i < invocations.length(); i++) {
            JSONObject jsonInvocation = invocations.getJSONObject(i);

            Type invokedType = new Type();
            Type invokingType = new Type();
            invokedType.setFullName(jsonInvocation.getJSONObject("invoked").getJSONObject("type").getString("fullName"));
            invokingType.setFullName(jsonInvocation.getJSONObject("invoking").getJSONObject("type").getString("fullName"));

            Method invoked = new Method();
            Method invoking = new Method();
            invoked.setType(invokedType);
            invoking.setType(invokingType);

            Invocation invocation = new Invocation();
            invocation.setInvoked(invoked);
            invocation.setInvoking(invoking);
            invocation.setId(jsonInvocation.getInt("id"));
            invocationList.add(invocation);
        }
    }
}
