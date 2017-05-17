package com.example.francois.scrummanager;

class TaskSchedule {
    private String name;
    private int start;
    private int end;
    private String developer;

    TaskSchedule(String name, int start, int end, String developer){
        this.name = name;
        this.start = start;
        this.end = end;
        this.developer = developer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String toString(){
        return name + " Début : " + start + " Fin : " + end + " Dev : " + developer;
    }
}
