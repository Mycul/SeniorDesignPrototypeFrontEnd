package com.example.michael.prototypev2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final int RC_SIGN_IN = 1;

    String mUsername;
    String mUserID;




    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    DatabaseReference databaseUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        //instantiation for the firebase vars
        mFirebaseAuth = FirebaseAuth.getInstance();

        //get a reference of the users node
        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");




        //Authentication onCreate
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){
                    //user is signed in
                    //mDatabaseReference = mFirebaseDatabase.getReference().child(user.getUid());
                    onSignedInInitialize(user.getDisplayName());
                    mUserID = user.getUid();
                    addUser();
                }else{
                    //user is signed out


                    onSignedOutCleanup();


                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new  AuthUI.IdpConfig.GoogleBuilder().build()
                                    ))
                                    .build(),
                            RC_SIGN_IN);
                }

            }
        };

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {
            if(resultCode == RESULT_OK){

                Toast.makeText(MainActivity.this, "You're now signed in!", Toast.LENGTH_SHORT).show();

            }

            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_menu:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(mAuthStateListener != null){
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        }
        //detatchDatabaseReadListener();
        //mJournalAdapter.clear();
    }

    private void onSignedInInitialize(String username){
        mUsername = username;
        //getActionBar().setTitle(mUsername);
        //attatchDatabaseReadListener();
    }

    private void onSignedOutCleanup(){
        mUsername = "NULL";
        //mJournalAdapter.clear();
    }

    public void toCamera(View view) {
        Intent intent = new Intent(MainActivity.this, Camera.class);
        intent.putExtra("EXTRA_USERNAME", mUsername);
        startActivity(intent);

    }
    public void toDemoAddFood(View view) {
        Intent intent = new Intent(MainActivity.this, DemoAddFood.class);
        intent.putExtra("EXTRA_USERNAME", mUsername);
        intent.putExtra("EXTRA_ID", mUserID);
        startActivity(intent);

    }
    public void toFoodJournal(View view) {
        Intent intent = new Intent(MainActivity.this, FoodJournal.class);
        intent.putExtra("EXTRA_USERNAME", mUsername);
        intent.putExtra("EXTRA_ID", mUserID);
        startActivity(intent);

    }
    private void addUser(){

        //creating the user object
        final UserModel mUser = new UserModel(mUserID, mUsername);

        databaseUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(mUserID)){
                    //do nothing, user already in database
                }else{
                    //saving the user to the database
                    databaseUsers.child(mUserID).setValue(mUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
