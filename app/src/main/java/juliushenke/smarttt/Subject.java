package juliushenke.smarttt;

import java.io.Serializable;

class Subject implements Serializable {

    private static final long serialVersionUID = -29238982928391L;

    private String name;
    private String room;
    private String teacher;
    private int color;
    
    Subject(String name, String room, String teacher, int color){
        this.name = name;
        this.room = room;
        this.teacher = teacher;
        this.color = color;
    }

    //Getters
    String getName(){return name;}
    String getRoom(){return room;}
    String getTeacher(){return teacher;}
    int getColor(){
        return color;
    }

    //Setters
    void setTeacher(String teacher){
        this.teacher = teacher;
    }
}
