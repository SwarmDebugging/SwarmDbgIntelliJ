package com.swarm.utils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestsQueue {

    private static RequestsQueue instance = null;
    private final Queue<Runnable> requests;

    private RequestsQueue() {
        requests = new LinkedList<>();
    }

    public static RequestsQueue getInstance() {
        if (instance == null) {
            instance = new RequestsQueue();
        }
        return instance;
    }

    public void addRequest(Runnable request) {
        requests.add(request);
        if(requests.size() > 4) {
            sendRequests();
        }
    }

    public void sendRequests() {
        ExecutorService es = Executors.newSingleThreadExecutor();
        while (!requests.isEmpty()){
            es.submit(requests.poll());
        }
        es.shutdown();
    }
}
