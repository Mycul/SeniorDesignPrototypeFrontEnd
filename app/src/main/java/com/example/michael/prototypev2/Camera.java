package com.example.michael.prototypev2;

import android.app.Activity;
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
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Camera extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;


    String mUsername;
    String mUserID;

    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private String pictureImagePath = "";
    private ProgressBar mProgressBar;
    private Button mCameraButton;

    File photoFile;

    String mCurrentPhotoPath;

    private TextView downloadUrlTextView;


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
                
                
                
                
      		startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
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
    }








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
                    mProgressBar.setVisibility(View.GONE);
                    mCameraButton.setEnabled(true);

                 
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    downloadUrlTextView.setText(downloadUrl.toString());
                    downloadUrlTextView.setVisibility(View.VISIBLE);
                }
            });
            
            /*
            //Uri file = Uri.fromFile(photoFile);
            Uri uri = data.getData();

            mProgressBar.setVisibility(View.VISIBLE);
            mCameraButton.setEnabled(false);
            StorageReference filepath = storage.child("Photos").child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    mProgressBar.setVisibility(View.GONE);
                    mCameraButton.setEnabled(true);


                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    String stringDL = downloadUrl.toString();
                    downloadUrlTextView.setText(downloadUrl.toString());
                    downloadUrlTextView.setVisibility(View.VISIBLE);
                }
            });
        */

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




}
