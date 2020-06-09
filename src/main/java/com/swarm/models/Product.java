package com.swarm.models;

import java.util.ArrayList;

public class Product {
    private int id;
    private String title;
    private ArrayList<Task> tasks;

    public Product(int id, String title) {
        this.id = id;
        this.title = title;
        tasks = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
    }
}
