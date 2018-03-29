package com.example.michael.prototypev2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
import java.util.UUID;

public class Camera extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    String mUsername;
    String mUserID;

    private ProgressBar mProgressBar;
    private Button mCameraButton;
    private TextView downloadUrl;
    private Uri mImageUri;


    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    private ImageView mImageView;

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
    }




    public void takePhoto(View view){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                //Log.i(TAG, "IOException");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                //mImageUri = Uri.fromFile(photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri(photoFile));
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);

            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //try {
                Toast.makeText(this, "Entered on Activity result", Toast.LENGTH_LONG).show();

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();

            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();


            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            //bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
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

                    //Uri url = taskSnapshot.getDownloadUrl();
                    //downloadUrl.setText(url.toString());
                   // downloadUrl.setVisibility(View.VISIBLE);
                }
            });

                /*mImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(mCurrentPhotoPath));
                //mImageView.setImageBitmap(mImageBitmap);
                Bundle extras = data.getExtras();

                Bitmap intentBitmap = (Bitmap) extras.get("data");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                intentBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] cameraData = baos.toByteArray();



                //test storage stuff
                String path = "SnapNSnack/" + UUID.randomUUID() + ".png";
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

                        Uri url = taskSnapshot.getDownloadUrl();
                        downloadUrl.setText(url.toString());
                        downloadUrl.setVisibility(View.VISIBLE);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
            */
        }
    }


    private File createImageFile() throws IOException{
        //create and image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir    /* directory */
        );
        //save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    public Uri setImageUri(File photoFile){
        Uri imgUri = Uri.fromFile(photoFile);
        return imgUri;

    }
/*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        switch(requestCode){
            case TAKE_PICTURE:
                if(resultCode == Activity.RESULT_OK){
                    Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    Image
                }
        }
    }
*/
/*
    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Ensure that there is a camera activity to handle the intent
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            //create the file where the photo should go
            File photoFile = null;
            try{
                photoFile = createImageFile();

            }catch (IOException ex){
                //error occured
            }
            //should only execute after the file was successfully created
            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(this,"com.example.android.michael.prototypev2.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


     @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mImageUri != null) {
            outState.putString("cameraImageUri", mImageUri.toString());
        }
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("cameraImageUri")) {
            mImageUri = Uri.parse(savedInstanceState.getString("cameraImageUri"));
        }
    }
*/


}
