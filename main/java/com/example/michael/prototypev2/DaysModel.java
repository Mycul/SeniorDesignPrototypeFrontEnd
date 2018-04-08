package com.example.michael.prototypev2;

/**
 * Created by Michael on 3/18/2018.
 */

public class DaysModel {
    private String daysId;
    private String displayString;

    public DaysModel(){
    //required constructor

    }

    public DaysModel(String daysId, String displayString){
        this.daysId = daysId;
        this.displayString = displayString;
    }
    public String getDaysId(){
        return daysId;

    }
    public String getDisplayString(){
        return displayString;
    }

}
