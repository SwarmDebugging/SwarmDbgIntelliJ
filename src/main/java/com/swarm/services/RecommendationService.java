package com.swarm.services;

import com.swarm.models.Breakpoint;
import com.swarm.models.Method;
import com.swarm.models.Type;
import com.swarm.utils.HTTPRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RecommendationService {

    private ArrayList<Method> recommendedMethods;
    private ArrayList<Breakpoint> breakpointArrayList;

    public ArrayList<Breakpoint> getBreakpointsBySessionId(int sessionId) {
        breakpointArrayList = new ArrayList<>();
        HTTPRequest getBreakpointsBySessionId = new HTTPRequest();
        getBreakpointsBySessionId.setQuery("{breakpoints(sessionId:" + sessionId + "){id,lineNumber,type{id,fullName,fullPath}}}");
        JSONObject response = new JSONObject(getBreakpointsBySessionId.post().getString("body"));
        JSONObject data = response.getJSONObject("data");

        if(!data.isNull("breakpoints")) {
            JSONArray breakpoints = data.getJSONArray("breakpoints");
            addJSONBreakpointsToBreakpointList(breakpoints);
        }
        return breakpointArrayList;
    }

    private void addJSONBreakpointsToBreakpointList(JSONArray breakpoints){
        for (int i = 0; i < breakpoints.length(); i++) {
            JSONObject jsonBreakpoint = breakpoints.getJSONObject(i);
            Breakpoint breakpoint = new Breakpoint();
            breakpoint.setId(jsonBreakpoint.getInt("id"));
            breakpoint.setLineNumber(jsonBreakpoint.getInt("lineNumber"));
            Type type = new Type();
            type.setId(jsonBreakpoint.getJSONObject("type").getInt("id"));
            type.setFullName(jsonBreakpoint.getJSONObject("type").getString("fullName"));
            type.setFullPath(jsonBreakpoint.getJSONObject("type").getString("fullPath"));
            breakpoint.setType(type);
            breakpointArrayList.add(breakpoint);
        }
    }

    public ArrayList<Method> getRecommendedMethods(int taskId) {
        recommendedMethods = new ArrayList<>();
        HTTPRequest getRecommendedMethodsQuery = new HTTPRequest();
        getRecommendedMethodsQuery.setQuery("{methodsUsedInTask(taskId:" + taskId + "){id,name,type{id,name,fullName,fullPath}}}");
        JSONObject response = new JSONObject(getRecommendedMethodsQuery.post().getString("body"));
        JSONObject data = response.getJSONObject("data");

        if(!data.isNull("methodsUsedInTask")) {
            JSONArray methods = data.getJSONArray("methodsUsedInTask");
            addJSONMethodsToMethodList(methods);
        }
        return recommendedMethods;
    }

    private void addJSONMethodsToMethodList(JSONArray methods) {
        for (int i = 0; i < methods.length(); i++) {
            JSONObject jsonMethod = methods.getJSONObject(i);
            Method method = new Method();
            method.setId(jsonMethod.getInt("id"));
            method.setName(jsonMethod.getString("name"));
            Type type = new Type();
            type.setId(jsonMethod.getJSONObject("type").getInt("id"));
            type.setName(jsonMethod.getJSONObject("type").getString("name"));
            type.setFullName(jsonMethod.getJSONObject("type").getString("fullName"));
            type.setFullPath(jsonMethod.getJSONObject("type").getString("fullPath"));
            method.setType(type);
            recommendedMethods.add(method);
        }
    }

}
