package com.example.michael.prototypev2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DemoAddFood extends AppCompatActivity {



    //Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference databaseFood;
    private DatabaseReference databaseDays;




    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
    SimpleDateFormat sdf2 = new SimpleDateFormat("MMddyyyy");

    EditText mEdit;
    Button buttonAddFood;

    String mUsername;
    String mUserID;
    String currentDate = sdf.format(new Date());
    String dateID = sdf2.format(new Date());
    String foodItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_add_food);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            mUsername = extras.getString("EXTRA_USERNAME");
            mUserID = extras.getString("EXTRA_ID");
        }
        //display the date and the username
        updateTextViewDate(currentDate);
        updateTextViewUsername(mUsername);

        mEdit = (EditText) findViewById(R.id.food_input);


        buttonAddFood = (Button) findViewById(R.id.Food_input_button);

        //instantiation for the firebase vars
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        //Food:
            //List of days that people have entered food:
                //List of userIDs that entered food on that day:
                    //List of food that User entered on that day
        databaseFood = mFirebaseDatabase.getReference("Food").child(mUserID).child(currentDate); //need to .child
        databaseDays = mFirebaseDatabase.getReference("Days").child(mUserID);
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.Food_input_button:
                addToDays();
                addToJournal();
        }

    }


    public void updateTextViewDate(String toThis) {
        TextView currentDateTextView = (TextView) findViewById(R.id.current_date);
        currentDateTextView.setText(toThis);

    }
    public void updateTextViewUsername(String toThis) {
        TextView currentDateTextView = (TextView) findViewById(R.id.username_demo_page);
        currentDateTextView.setText(toThis);

    }
    public void addToDays(){
        final String id = dateID;
        final DaysModel daysModel = new DaysModel(id, currentDate);
        databaseDays.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(id)){
                    //do nothing, user already in database
                }else{
                    //saving the user to the database
                    databaseDays.child(id).setValue(daysModel);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void addToJournal(){
        foodItem = mEdit.getText().toString();
        String calories = "temporary#";
        if(!TextUtils.isEmpty(foodItem)){
            String id = databaseFood.push().getKey();
            FoodModel foodModel = new FoodModel(id, foodItem, calories);
            databaseFood.child(id).setValue(foodModel);
            Toast.makeText(this, "Food Saved", Toast.LENGTH_LONG).show();
            mEdit.setText("");
        }else{
            Toast.makeText(this, "Please Enter a Food Item", Toast.LENGTH_LONG).show();
        }
    }
}
