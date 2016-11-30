package juliushenke.smarttt;

import android.graphics.Color;

import java.io.Serializable;

public class Subject implements Serializable {

    private static final long serialVersionUID = -29238982928391L;

    private String name;
    private String room;
    private String teacher;
    private String notes;
    private int color;
    
    public Subject(String name, String room, String teacher, String notes, int color){
        this.name = name;
        this.room = room;
        this.teacher = teacher;
        this.notes = notes;
        this.color = color;
    }

    //Getters
    public String getName(){return name;}
    public String getRoom(){
        return room;
    }
    public String getTeacher(){
        return teacher;
    }
    public String getNotes() { return notes;}
    public int getColor(){
        return color;
    }

    //Setters
    public void setName(String name){
        this.name = name;
    }
    public void setRoom(String room){
        this.room = room;
    }
    public void setTeacher(String teacher){
        this.teacher = teacher;
    }
}
