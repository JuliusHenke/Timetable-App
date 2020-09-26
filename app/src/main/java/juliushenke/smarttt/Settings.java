package juliushenke.smarttt;

import java.io.Serializable;

class Settings implements Serializable {

    private static final long serialVersionUID = -29238982928391L;

    private int start_day;
    private int end_day;
    private boolean weekSystem;
    private boolean showWeek;
    private boolean showHourTimes;
    private String[] hourTimes;

    Settings() {
        this.start_day = 1;
        this.end_day = 5;
        this.weekSystem = false;
        this.showWeek = false;
        this.showHourTimes = false;
        this.hourTimes = new String[0];
    }

    //Getters
    int getStart_day(){return start_day;}
    int getEnd_day(){return end_day;}
    boolean getWeekSystem(){return weekSystem;}
    boolean getShowWeek(){return showWeek;}
    boolean getShowHourTimes(){return showHourTimes;}
    String[] getHourTimes(){return hourTimes;}

    //Setters
    void setStart_day(int input_day){
        this.start_day = input_day;
        if(end_day < start_day) end_day = start_day;
    }
    void setEnd_day(int input_day){
        this.end_day = input_day;
        if(start_day > end_day) start_day = end_day;
    }
    void setWeekSystem(boolean weekSystem){this.weekSystem = weekSystem;}
    void setShowWeek(boolean showWeek){this.showWeek = showWeek;}
    void setShowHourTimes(boolean showHourTimes){this.showHourTimes = showHourTimes;}
    void setHourTimes(String[] hourTimes){this.hourTimes = hourTimes;}
}
