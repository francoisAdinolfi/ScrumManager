package com.example.francois.scrummanager;

import java.util.ArrayList;

public class Schedule {
    private ArrayList<TaskSchedule> tasks;
    private int duration;

    public Schedule(int duration){
        tasks = new ArrayList<>();
        this.duration = duration;
    }

    public int getDuration(){
        return duration;
    }

    public void setDuration(int duration){
        this.duration = duration;
    }

    public void addTask(TaskSchedule task){
        tasks.add(task);
    }

    public ArrayList<TaskSchedule> getTasks(){
        return tasks;
    }
}