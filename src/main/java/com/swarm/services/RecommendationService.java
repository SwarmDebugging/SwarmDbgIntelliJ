package com.swarm.services;

import com.swarm.models.Method;
import com.swarm.models.Type;
import com.swarm.utils.HTTPRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class RecommendationService {

    private ArrayList<Method> recommendedMethods;

    public ArrayList<Method> getRecommendedMethods(int taskId) {
        recommendedMethods = new ArrayList<>();
        HTTPRequest getRecommendedMethodsQuery = new HTTPRequest();
        getRecommendedMethodsQuery.setQuery("{breakpointRecommendation(taskId:" + taskId + "){id,name,type{id,name,fullName,fullPath}}}");
        JSONObject response = new JSONObject(getRecommendedMethodsQuery.post().getString("body"));
        JSONObject data = response.getJSONObject("data");

        if(!data.isNull("breakpointRecommendation")) {
            JSONArray methods = data.getJSONArray("breakpointRecommendation");
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
