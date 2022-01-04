package juliushenke.smarttt;

import java.io.Serializable;

class Subject implements Serializable {

    private static final long serialVersionUID = -29238982928391L;

    private String name;
    private String room;
    private String teacher;
    private String notes;
    private int color;

    Subject(String name, int color, String room, String teacher, String notes){
        this.name = name;
        this.color = color;
        this.room = room;
        this.teacher = teacher;
        this.notes = notes;
    }

    Subject(String name, int color){
        this.name = name;
        this.color = color;
    }

    //Getters
    String getName(){return name;}
    String getRoom(){return room;}
    String getTeacher(){return teacher;}
    String getNotes(){return notes;}
    int getColor(){return color;}
    boolean isNew(){return teacher == null;}

    //Setters
    void markNotNew(){
        this.teacher = "";
    }
}
