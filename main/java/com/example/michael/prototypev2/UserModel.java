package com.example.michael.prototypev2;

/**
 * Created by Michael on 3/18/2018.
 */

public class UserModel {
    private String userID;
    private String userName;

    public UserModel(){

    }
    public UserModel(String userID, String userName){
        this.userID = userID;
        this.userName = userName;
    }

    public String getUserID(){
        return userID;

    }
    public String getUserName(){
        return userName;
    }
}
