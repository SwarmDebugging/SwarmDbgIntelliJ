package com.swarm.services;

import com.swarm.models.Developer;
import com.swarm.models.Session;
import com.swarm.models.Task;
import com.swarm.utils.HTTPRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SessionService {

    private ArrayList<Session> sessionList;

    public ArrayList<Session> sessionsByDeveloper(Developer developer) {
        sessionList = new ArrayList<>();
        HTTPRequest getSessionsByDeveloperId = new HTTPRequest();
        getSessionsByDeveloperId.setQuery("query sessions($developerId:Long!){sessions(developerId:$developerId){id,finished,description,task{title,done}}}");
        getSessionsByDeveloperId.addVariable("developerId", developer.getId());
        JSONObject response = new JSONObject(getSessionsByDeveloperId.post().getString("body"));

        if((!response.getJSONObject("data").isNull("sessions"))) {
            JSONArray sessions = response.getJSONObject("data").getJSONArray("sessions");
            addJSONSessionsToSessionList(sessions);
        }
        return sessionList;
    }

    private void addJSONSessionsToSessionList(JSONArray sessions) {
        for (int i = 0; i < sessions.length(); i++) {
            JSONObject jsonSession = sessions.getJSONObject(i);
            if(!jsonSession.getJSONObject("task").getBoolean("done")) {
                Session newSession = new Session();
                newSession.setFinished(!jsonSession.isNull("finished"));
                newSession.setId(jsonSession.getInt("id"));
                newSession.setDescription(jsonSession.getString("description"));

                Task task = new Task();
                task.setTitle(jsonSession.getJSONObject("task").getString("title"));

                newSession.setTask(task);
                sessionList.add(newSession);
            }
        }
    }
}
