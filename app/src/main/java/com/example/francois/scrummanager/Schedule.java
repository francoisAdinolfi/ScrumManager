package com.example.francois.scrummanager;

import java.util.ArrayList;

class Schedule {
    private ArrayList<TaskSchedule> tasks;
    private int duration;

    Schedule(int duration){
        tasks = new ArrayList<>();
        this.duration = duration;
    }

    int getDuration(){
        return duration;
    }

    void addTask(TaskSchedule task){
        tasks.add(task);
    }

    ArrayList<TaskSchedule> getTasks(){
        return tasks;
    }
}