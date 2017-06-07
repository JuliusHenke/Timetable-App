package juliushenke.smarttt;

import java.io.Serializable;

class Settings implements Serializable {

    private static final long serialVersionUID = -29238982928391L;

    private int startDay;
    private int endDay;
    private boolean evenOddWeekSystem;
    private boolean weekDisplay;
    private boolean darkDesign;

    Settings() {
        this.startDay = 1;
        this.endDay = 5;
        this.evenOddWeekSystem = false;
        this.weekDisplay = false;
        this.darkDesign = false;
    }

    //Getters
    int getStartDay(){return startDay;}
    int getEndDay(){return endDay;}
    boolean isEvenOddWeekSystem(){return evenOddWeekSystem;}
    boolean isWeekDisplay(){return weekDisplay;}
    boolean isDarkDesign(){return darkDesign;}

    //Setters
    void setStartDay(int input_day){
        this.startDay = input_day;
        if(endDay < startDay) endDay = startDay;
    }
    void setEndDay(int input_day){
        this.endDay = input_day;
        if(startDay > endDay) startDay = endDay;
    }
    void setEvenOddWeekSystem(boolean evenOddWeekSystem){this.evenOddWeekSystem = evenOddWeekSystem;}
    void setWeekDisplay(boolean weekDisplay){this.weekDisplay = weekDisplay;}
    void setDarkDesign(boolean darkDesign){this.darkDesign = darkDesign;}
}
