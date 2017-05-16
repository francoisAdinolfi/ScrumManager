package com.example.francois.scrummanager;

class TaskSchedule {
    private String name;
    private int start;
    private int end;
    private String developer;

    public TaskSchedule(String name, int start, int end, String developer){
        this.name = name;
        this.start = start;
        this.end = end;
        this.developer = developer;
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

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String toString(){
        return name + " Début : " + start + " Fin : " + end + " Dev : " + developer;
    }
}
