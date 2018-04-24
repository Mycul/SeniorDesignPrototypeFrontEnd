package com.example.michael.prototypev2;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Camera extends AppCompatActivity  implements AsyncResponse{

    static final int REQUEST_TAKE_PHOTO = 1;

    //demo add food stuff
    //Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference databaseFood;
    private DatabaseReference databaseDays;




    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
    SimpleDateFormat sdf2 = new SimpleDateFormat("MMddyyyy");

    //EditText mEdit;
    //Button buttonAddFood;

    String mUsername;
    String mUserID;
    String currentDate = sdf.format(new Date());
    String dateID = sdf2.format(new Date());
    String foodItem;




    //end of demo add food stuff
    messageSender MS;

    //String mUsername;
    //String mUserID;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private String pictureImagePath = "";
    private ProgressBar mProgressBar;
    private Button mCameraButton;

    public AsyncResponse delegateBackup = null;

    String response = "";
    File photoFile;

    String mCurrentPhotoPath;

    private TextView downloadUrlTextView;
    private TextView result1;
    private TextView result2;
    private TextView result3;



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

	File image = File.createTempFile(
           
		imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        
	return image;
    }
    public void takePhoto(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File...

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,"com.example.android.michael.prototypev2.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);



		if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP) {
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clip=
                            ClipData.newUri(getContentResolver(), "A photo", photoURI);

                    takePictureIntent.setClipData(clip);
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                else {
                    List<ResolveInfo> resInfoList=
                            getPackageManager()
                                    .queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);

                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        grantUriPermission(packageName, photoURI,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    }
                }
                
                
                
                
      		startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        Bundle extras = getIntent().getExtras();
        if(extras != null){
            mUsername = extras.getString("EXTRA_USERNAME");
            mUserID = extras.getString("EXTRA_ID");
        }

        //async result listener
        MS = new messageSender();
        MS.delegate = this;
        delegateBackup = this;

        //demo add food stuff


        //instantiation for the firebase vars
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        //Food:
        //List of days that people have entered food:
        //List of userIDs that entered food on that day:
        //List of food that User entered on that day
        databaseFood = mFirebaseDatabase.getReference("Food").child(mUserID).child(currentDate); //need to .child
        databaseDays = mFirebaseDatabase.getReference("Days").child(mUserID);

        //end


        mProgressBar = (ProgressBar) findViewById(R.id.uploadPictureSpinner);
        mCameraButton = (Button) findViewById(R.id.camera_button);
        downloadUrlTextView = (TextView) findViewById(R.id.donwloadUrlTextView);
        result1 = (TextView) findViewById(R.id.resultTextView1);
        result2 = (TextView) findViewById(R.id.resultTextView2);
        result3 = (TextView) findViewById(R.id.resultTextView3);

    }

    //Demo add food functions

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
    public void addToJournal(String foodName){
        //foodItem = mEdit.getText().toString();
        foodItem = foodName; //TODO: Replace with value from the parsed JSON
        String calories = "temporary#";
        if(!TextUtils.isEmpty(foodItem)){
            String id = databaseFood.push().getKey();
            FoodModel foodModel = new FoodModel(id, foodItem, calories);
            databaseFood.child(id).setValue(foodModel);
            Toast.makeText(this, "Food Saved", Toast.LENGTH_LONG).show();
            //mEdit.setText("");
        }else{
            Toast.makeText(this, "Please Enter a Food Item", Toast.LENGTH_LONG).show();
        }
    }

//end of demo add food functions



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            Toast.makeText(this, "Entered on Activity result", Toast.LENGTH_LONG).show();
        
	    File imgFile = new File(mCurrentPhotoPath);

            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
	
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	
	
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] cameraData = baos.toByteArray();

            String path = "SnapNSnack/" + UUID.randomUUID() + ".jpg";
            StorageReference firebaseStorageRef = storage.getReference(path);


            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setCustomMetadata("userID", mUserID)
                    .build();
            mProgressBar.setVisibility(View.VISIBLE);
            mCameraButton.setEnabled(false);
            UploadTask uploadTask = firebaseStorageRef.putBytes(cameraData, metadata);
            uploadTask.addOnSuccessListener(Camera.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //mProgressBar.setVisibility(View.GONE);
                    //mCameraButton.setEnabled(true);

                 
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    downloadUrlTextView.setText(downloadUrl.toString());
                    downloadUrlTextView.setVisibility(View.VISIBLE);

                    response = "";
                    MS = new messageSender();
                    MS.delegate = delegateBackup;
                    MS.execute(downloadUrl.toString());



                    /*
                    try {
                        response = MS.get();

                        result1.setText(response);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    */
                    //while(response == "") {
                       // result1.setText(response);
                        //result1.setVisibility(View.VISIBLE);

                   // }
                    //addToDays();
                    //addToJournal(); //TODO: set up the receiving of data from computer and parse into parts that can be






                }
            });
            


        }
    }

    @Override	
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCurrentPhotoPath != null) {
            outState.putString("mCurrentPhotoPath", mCurrentPhotoPath);
        }
    }
	
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("mCurrentPhotoPath")) {
            mCurrentPhotoPath = savedInstanceState.getString("mCurrentPhotoPath");
        }
    }

    @Override
    public void processFinish(String output){
        mProgressBar.setVisibility(View.GONE);
        mCameraButton.setEnabled(true);
        response = output;
        result1.setText(response);

        try{
            List<String> items = new ArrayList<>();
            JSONObject root = new JSONObject(response);
            JSONArray array = root.getJSONArray("Items");
            //System.out.println(root.getString("FoodItem"));

            for(int i =0; i < array.length(); i++){
                JSONObject object = array.getJSONObject(i);
                items.add(object.getString("FoodItem"));
                System.out.println(items.get(i));
                //TODO:change this so the user can choose to add the item or not for now it will add the first value found to food journal
                if(i == 0){
                    addToDays();
                    addToJournal(items.get(i));
                }
            }

            //JSONObject nested = root.getJSONObject("nested");
            //Log.d("TAG","flag value "+nested.getBoolean("flag"));

        }catch (JSONException e) {
            e.printStackTrace();
        }


    }




}
