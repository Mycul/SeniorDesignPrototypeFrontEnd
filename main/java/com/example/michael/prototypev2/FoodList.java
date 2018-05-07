package com.example.michael.prototypev2;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Michael on 3/18/2018.
 */

public class FoodList extends ArrayAdapter<FoodModel>{
    private Activity context;
    List<FoodModel> foods;

    public FoodList(Activity context, List<FoodModel> foods){
        super(context, R.layout.layout_food_list, foods);
        this.context = context;
        this.foods = foods;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.layout_food_list, null, true);

        TextView textViewFood = (TextView) listViewItem.findViewById(R.id.textViewFoodName);
        TextView textViewCalories = (TextView) listViewItem.findViewById(R.id.textViewCalories);

        FoodModel food = foods.get(position);
        textViewFood.setText((position + 1) + ": " + food.getFoodName());
        textViewCalories.setText(food.getCalorieCount());

        return listViewItem;
    }
}
