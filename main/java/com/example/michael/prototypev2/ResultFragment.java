package com.example.michael.prototypev2;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;


public class ResultFragment extends Fragment {

    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
    SimpleDateFormat sdf2 = new SimpleDateFormat("MMddyyyy");

    //EditText mEdit;
    //Button buttonAddFood;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    //demo add food stuff
    //Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference databaseFood;
    private DatabaseReference databaseDays;

    String mUsername;
    String mUserID;
    String currentDate = sdf.format(new Date());
    String dateID = sdf2.format(new Date());
    String foodItem = "";
    String foodFound = "";
    double foodScore = 0;
    TextView foodTextView;
    TextView scoreTextView;

    int calorieVal = 0;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.result_frag, container, false);
        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
            ResultFragment details = new ResultFragment();
            Bundle bundle = this.getArguments();
            if (bundle != null) {
                mUsername = bundle.getString("EXTRA_USERNAME");
                mUserID = bundle.getString("EXTRA_ID");
                foodFound = bundle.getString("FOOD_FOUND");
                foodScore = bundle.getDouble("FOODS_SCORE");
            }
            getFragmentManager().beginTransaction().add(
                    android.R.id.content, details).commit();
            container.findViewById(R.id.foodNameTextView);
            container.findViewById(R.id.foodScoreTextView);
            foodTextView = (TextView) v.findViewById(R.id.foodNameTextView);
            scoreTextView = (TextView) v.findViewById(R.id.foodScoreTextView);





   /* potentially useless here
   Bundle extras = getIntent().getExtras();
        if(extras !=null)

    {
        mUsername = extras.getString("EXTRA_USERNAME");
        mUserID = extras.getString("EXTRA_ID");
        foodFound = extras.getString("FOOD_FOUND");
        foodScore = extras.getDouble("FOODS_SCORE");
    }
    */
            //foodTextView =(TextView)

            //findViewById(R.id.foodNameTextView);

            //scoreTextView =(TextView)

            //findViewById(R.id.foodScoreTextView);

            System.out.println(foodFound);
            switch (foodFound) {
                case "babycarrots":
                    calorieVal = 4;
                    foodItem = "Baby Carrots";
                    break;
                case "greenbean":
                    calorieVal = 31;
                    foodItem = "Green Beans";
                    break;
                case "chickenleg":
                    calorieVal = 170;
                    foodItem = "Chicken Legs";
                    break;
            }
            foodTextView.setText(foodItem);
            scoreTextView.setText(Double.toString(foodScore));


            //instantiation for the firebase vars
            mFirebaseDatabase = FirebaseDatabase.getInstance();

            //Food:
            //List of days that people have entered food:
            //List of userIDs that entered food on that day:
            //List of food that User entered on that day
            databaseFood = mFirebaseDatabase.getReference("Food").child(mUserID).child(currentDate); //need to .child

            databaseDays = mFirebaseDatabase.getReference("Days").child(mUserID);

        }
        //end
        return v;

    }

    public void addEntry(View view) {
        addToDays();
        addToJournal();
        getActivity().getFragmentManager().beginTransaction().remove(this).commit();
    }

    //public void goBack(View view){
    //     finish();
    // }
    //Demo add food functions

    public void addToDays() {
        final String id = dateID;
        final DaysModel daysModel = new DaysModel(id, currentDate);
        databaseDays.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(id)) {
                    //do nothing, user already in database
                } else {
                    //saving the user to the database
                    databaseDays.child(id).setValue(daysModel);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addToJournal() {
        //foodItem = mEdit.getText().toString();


        //foodItem = foodFound;
        String calories = "Calories per serving are roughly: " + calorieVal;
        if (!TextUtils.isEmpty(foodItem)) {
            String id = databaseFood.push().getKey();
            FoodModel foodModel = new FoodModel(id, foodItem, calories);
            databaseFood.child(id).setValue(foodModel);
            Toast.makeText(ResultFragment.this.getActivity().getApplicationContext(),"Food Saved", Toast.LENGTH_LONG).show();
            //Toast.makeText(this, "Food Saved", Toast.LENGTH_LONG).show();
            //mEdit.setText("");
        } else {
            Toast.makeText(ResultFragment.this.getActivity().getApplicationContext(), "Please Enter a Food Item", Toast.LENGTH_LONG).show();
        }
    }
}

