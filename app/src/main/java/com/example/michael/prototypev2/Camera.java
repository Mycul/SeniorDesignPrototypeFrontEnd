package com.example.michael.prototypev2;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

    Bitmap bitmap;

    int k = 0;


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
    private TextView instructionTextView1;
    private TextView instructionTextView2;
    private TextView instructionTextView3;
    private TextView instructionTextView4;
    private TextView instructionTextView5;
    private TextView instructionTextView6;




    private ImageView foodImageView;


    String foodFound = "";
    double foodScore = 0;
    double foodXmult = 0;
    double foodYmult = 0;

    List<String> foodNames = new ArrayList<>();
    List<Double> foodScores = new ArrayList<>();


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
        mProgressBar = (ProgressBar) findViewById(R.id.uploadPictureSpinner);
        mCameraButton = (Button) findViewById(R.id.camera_button);
        downloadUrlTextView = (TextView) findViewById(R.id.donwloadUrlTextView);
        instructionTextView1 = (TextView) findViewById(R.id.instructionsTextView1);
        instructionTextView2 = (TextView) findViewById(R.id.instructionsTextView2);
        instructionTextView3 = (TextView) findViewById(R.id.instructionsTextView3);
        instructionTextView4 = (TextView) findViewById(R.id.instructionsTextView4);
        instructionTextView5 = (TextView) findViewById(R.id.instructionsTextView5);
        instructionTextView6 = (TextView) findViewById(R.id.instructionsTextView6);


        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto(view);
            }
        });


        foodImageView = (ImageView) findViewById(R.id.foodImage);

        //keeps track of the current photo if the user changes orientation
        if(savedInstanceState != null){
           mCurrentPhotoPath = savedInstanceState.getString("mCurrentPhotoPath");
        }

        if(mCurrentPhotoPath != null) {
            File imgFile = new File(mCurrentPhotoPath);
            //System.out.println(mCurrentPhotoPath);
            bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            //System.out.println(imgFile.getAbsolutePath());
            instructionTextView1.setVisibility(View.GONE);
            instructionTextView2.setVisibility(View.GONE);
            instructionTextView3.setVisibility(View.GONE);
            instructionTextView4.setVisibility(View.GONE);
            instructionTextView5.setVisibility(View.GONE);
            instructionTextView6.setVisibility(View.GONE);

            foodImageView.setImageBitmap(bitmap);

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


        /*if(findViewById(R.id.fragment_container) != null)
        {
            if(savedInstanceState != null)
            {
                return;
            }
            ResultFragment firstfrag = new ResultFragment();


        }

        if(findViewById(R.id.fragment_container) != null)
        {
            ResultFragment firstfrag = new ResultFragment();
            getFragmentManager().beginTransaction().add(R.id.fragment_container,firstfrag).commit();
        }
        */

    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            instructionTextView1.setVisibility(View.GONE);
            instructionTextView2.setVisibility(View.GONE);
            instructionTextView3.setVisibility(View.GONE);
            instructionTextView4.setVisibility(View.GONE);
            instructionTextView5.setVisibility(View.GONE);
            instructionTextView6.setVisibility(View.GONE);

            mCameraButton.setText(R.string.CameraBackToMenu);
            mCameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });


	        File imgFile = new File(mCurrentPhotoPath);

            bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
	        //System.out.println(imgFile.getAbsolutePath());
            foodImageView.setImageBitmap(bitmap);

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
                    //downloadUrlTextView.setText(downloadUrl.toString());
                    //downloadUrlTextView.setVisibility(View.VISIBLE);

                    response = "";
                    MS = new messageSender();
                    MS.delegate = delegateBackup;
                    MS.execute(downloadUrl.toString());



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
        int numFood = 0;
        String text = "";

        try{

            JSONObject root = new JSONObject(response);
            numFood = root.getInt("NumItems");
            downloadUrlTextView.setVisibility(View.VISIBLE);
            text = "Number of food items detected: " + numFood;
            downloadUrlTextView.setText(text);
            JSONArray array = root.getJSONArray("Items");
            //System.out.println(root.getString("FoodItem"));

            for(int i =0; i < array.length(); i++){
                JSONObject object = array.getJSONObject(i);
                foodFound = object.getString("FoodItem");
                foodScore = object.getDouble("Score");
                foodXmult = object.getDouble("CenterX");
                foodYmult = object.getDouble("CenterY");

                foodNames.add(foodFound);
                System.out.println(foodScore);
                foodScores.add(foodScore);
                int height = foodImageView.getMeasuredHeight();
                int width = foodImageView.getMeasuredWidth();
                double xOffset = (width * foodXmult) - 100;
                double yOffset = (height * foodYmult) - 100;
                System.out.println("height:" + height);
                System.out.println("width:" + width);

                RelativeLayout myLayout = (RelativeLayout)findViewById(R.id.relativeLayout);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
                params.leftMargin = (int) xOffset;
                params.topMargin = (int) yOffset;
                Button testButton = new Button(this);
                testButton.setId(i);
                final int id_ = testButton.getId();
                testButton.setBackgroundColor(0x900921FA);

                testButton.setBackgroundResource(R.drawable.round_button);



                myLayout.addView(testButton, params);
                Button btn1 = ((Button) findViewById(id_));
                System.out.println(id_);

                System.out.println(foodScores.get(id_));

                btn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //toResult(view);
                        Intent intent = new Intent(Camera.this, ResultActivity.class);
                        intent.putExtra("EXTRA_USERNAME", mUsername);
                        intent.putExtra("EXTRA_ID", mUserID);
                        intent.putExtra("FOOD_FOUND", foodNames.get(id_));
                        intent.putExtra("FOODS_SCORE", foodScores.get(id_));
                        startActivity(intent);
                       /* Bundle bundle = new Bundle();
                        bundle.putString("EXTRA_USERNAME",mUsername);
                        bundle.putString("EXTRA_ID", mUserID);
                        bundle.putString("FOOD_FOUND", foodNames.get(id_));
                        bundle.putDouble("FOODS_SCORE", foodScores.get(id_));

                        if(findViewById(R.id.fragment_container) != null)
                        {
                            ResultFragment firstfrag = new ResultFragment();
                            firstfrag.setArguments(bundle);
                            getFragmentManager().beginTransaction().add(R.id.fragment_container,firstfrag).commit();
                        }
                        */

                    }
                });

            }

            //foodImageView.setImageBitmap(bitmap);


            //JSONObject nested = root.getJSONObject("nested");
            //Log.d("TAG","flag value "+nested.getBoolean("flag"));

        }catch (JSONException e) {
            e.printStackTrace();
        }


    }





}
