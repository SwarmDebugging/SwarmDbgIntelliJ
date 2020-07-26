package com.swarm.services;

import com.swarm.models.Developer;
import com.swarm.models.Session;
import com.swarm.utils.HTTPRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SessionService {

    private ArrayList<Session> sessionList;

    public ArrayList<Session> sessionsByDeveloper(Developer developer) {
        sessionList = new ArrayList<>();
        HTTPRequest getSessionsByDeveloperId = new HTTPRequest();
        getSessionsByDeveloperId.setQuery("query sessions($developerId:Long!){sessions(developerId:$developerId){id,finished}}");
        getSessionsByDeveloperId.addVariable("developerId", developer.getId());
        JSONObject response = new JSONObject(getSessionsByDeveloperId.post().getString("body"));
        if((!response.getJSONObject("data").isNull("sessions"))) {
            JSONArray sessions = response.getJSONObject("data").getJSONArray("sessions");
            for (int i = 0; i < sessions.length(); i++) {
                JSONObject jsonSession = sessions.getJSONObject(i);
                Session newSession = new Session();
                newSession.setFinished(jsonSession.isNull("finished"));
                newSession.setId(jsonSession.getInt("id"));
                sessionList.add(newSession);
            }
        }
        return sessionList;
    }
}
