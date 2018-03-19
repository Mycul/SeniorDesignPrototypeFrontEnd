package com.example.michael.prototypev2;

/**
 * Created by Michael on 3/18/2018.
 */

public class FoodModel {
    private String foodId;
    private String foodName;
    private String calorieCount;

    public FoodModel(){

    }
    public FoodModel(String foodId, String foodName, String calorieCount){
        this.foodId = foodId;
        this.foodName = foodName;
        this.calorieCount = calorieCount;
    }
    public String getFoodId(){
        return foodId;
    }
    public String getFoodName(){
        return foodName;

    }
    public String getCalorieCount(){
        return calorieCount;
    }
}
