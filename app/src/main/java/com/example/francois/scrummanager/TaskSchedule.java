package com.example.francois.scrummanager;

class TaskSchedule {
    private int id_task;
    private String name;
    private int start;
    private int end;
    private String developer;

    TaskSchedule(int id_task, String name, int start, int end, String developer){
        this.id_task = id_task;
        this.name = name;
        this.start = start;
        this.end = end;
        this.developer = developer;
    }

    public int getIdTask(){
        return id_task;
    }

    public String getName() {
        return name;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String toString(){
        return name + " Début : " + start + " Fin : " + end + " Dev : " + developer;
    }
}
