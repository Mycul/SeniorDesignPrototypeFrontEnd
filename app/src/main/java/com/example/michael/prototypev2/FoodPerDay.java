package com.example.michael.prototypev2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FoodPerDay extends AppCompatActivity {

    String mUsername;
    String mUserID;
    String daysDisplay;
    String daysID;



    TextView textViewDay;
    ListView listViewFood;

    DatabaseReference databaseFoods;

    List<FoodModel> foods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_per_day);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            mUsername = extras.getString("EXTRA_USERNAME");
            mUserID = extras.getString("EXTRA_ID");
            daysDisplay = extras.getString("EXTRA_DAYS_DISPLAY");
            daysID = extras.getString("EXTRA_DAYS_ID");
        }


        databaseFoods = FirebaseDatabase.getInstance().getReference("Food").child(mUserID).child(daysDisplay);


        textViewDay = (TextView) findViewById(R.id.textViewSelectedDay);
        listViewFood = (ListView) findViewById(R.id.listViewFood);

        foods = new ArrayList<>();

        textViewDay.setText(daysDisplay);




    }

    @Override
    protected void onStart(){
        super.onStart();

        databaseFoods.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                foods.clear();
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    FoodModel food = postSnapshot.getValue(FoodModel.class);
                    foods.add(food);
                }
                Collections.reverse(foods);
                FoodList foodlistAdapater = new FoodList(FoodPerDay.this, foods);
                listViewFood.setAdapter(foodlistAdapater);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
