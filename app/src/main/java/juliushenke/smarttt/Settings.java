package juliushenke.smarttt;

import java.io.Serializable;

public class Settings implements Serializable {

    private static final long serialVersionUID = -29238982928391L;

    private int start_day;
    private int end_day;
    private boolean two_week_system;
    private int max_hours;

    public Settings(int start_day, int end_day, int max_hours, boolean two_week_system) {
        this.start_day = start_day;
        this.end_day = end_day;
        this.max_hours = max_hours;
        this.two_week_system = two_week_system;
    }

    //Getters
    public int getStart_day(){return start_day;}
    public int getEnd_day(){return end_day;}
    public boolean getTwo_week_system(){return two_week_system;}
    public int getMax_hours(){return max_hours;}

    //Setters
    public void setStart_day(int input_day){
        this.start_day = input_day;
        if(end_day < start_day) end_day = start_day;
    }
    public void setEnd_day(int input_day){
        this.end_day = input_day;
        if(start_day > end_day) start_day = end_day;
    }
    public void setTwo_week_system(boolean two_week_system){this.two_week_system = two_week_system;}
    public void setMax_hours(int max_hours){this.max_hours = max_hours;}
}
