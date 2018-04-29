package com.example.michael.prototypev2;

import android.content.Intent;
import android.renderscript.Sampler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FoodJournal extends AppCompatActivity {



    String mUsername;
    String mUserID;
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
    String currentDate = sdf.format(new Date());

    ListView listView;

    List<DaysModel> days;


    //database reference object
    public DatabaseReference databaseDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_journal);


        Bundle extras = getIntent().getExtras();
        if(extras != null){
            mUsername = extras.getString("EXTRA_USERNAME");
            mUserID = extras.getString("EXTRA_ID");
        }


        databaseDay = FirebaseDatabase.getInstance().getReference("Days").child(mUserID);


        listView = (ListView) findViewById(R.id.listViewDays);


        days = new ArrayList<>();


        //attach listener to listview
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //get the day selected
                DaysModel day = days.get(i);

                //create an intent
                Intent intent = new Intent(getApplicationContext(), FoodPerDay.class);

                intent.putExtra("EXTRA_DAYS_ID", day.getDaysId());
                intent.putExtra("EXTRA_DAYS_DISPLAY", day.getDisplayString());
                intent.putExtra("EXTRA_USERNAME", mUsername);
                intent.putExtra("EXTRA_ID", mUserID);

                startActivity(intent);
            }
        });


    }

    @Override
    protected void onStart(){
        super.onStart();

        databaseDay.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                days.clear();

                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    DaysModel day = postSnapshot.getValue(DaysModel.class);
                    days.add(day);
                }
                Collections.reverse(days);
                //creating the adapter
                DaysList daysAdapter = new DaysList(FoodJournal.this, days);

                listView.setAdapter(daysAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }





}
