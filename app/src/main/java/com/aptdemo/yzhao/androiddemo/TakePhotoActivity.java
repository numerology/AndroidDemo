package com.aptdemo.yzhao.androiddemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class TakePhotoActivity extends ActionBarActivity {
    private static final String TAG = "Take private photo";
    Context context = this;
    private String userEmail;
    private Button TakePhotoBtn;
    private Button UsePhotoBtn;
    private Button ReturnToStreamsBtn;
    private FrameLayout CameraFrameLayout;
    private ImageView DisplayImage;

    //variable for photo
    private Bitmap currentPhoto = null;
    private Bitmap drawablePhoto = null;

    private Camera mCamera;
    private PhotoPreview mPhotoPreview;

    int mNumberOfCameras;
    int cameraId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // remove title from window
        setContentView(R.layout.activity_take_photo);
        userEmail = getIntent().getStringExtra(Consts.USER_EMAIL_NAME);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TakePhotoBtn = (Button) findViewById(R.id.take_photo_take_picture);
        UsePhotoBtn = (Button) findViewById(R.id.take_photo_use_picture);
        ReturnToStreamsBtn = (Button) findViewById(R.id.take_photo_return_to_streams);
        CameraFrameLayout = (FrameLayout) findViewById(R.id.take_photo_camera_layout);
        DisplayImage = (ImageView) findViewById(R.id.take_photo_display_image);

        UsePhotoBtn.setVisibility(View.INVISIBLE);

        TakePhotoBtn.setOnClickListener(takePhotoHandler);
        UsePhotoBtn.setOnClickListener(usePhotoHandler);
        ReturnToStreamsBtn.setOnClickListener(returnToStreamsHandler);

        mPhotoPreview = new PhotoPreview(context);
        CameraFrameLayout.addView(mPhotoPreview);

        /*
        boolean opened = safeCameraOpen(cameraId);
        Log.w(TAG, "camera opened: " + Boolean.toString(opened));
        mPhotoPreview = new PhotoPreview(context, mCamera);
        //((ViewGroup) findViewById(R.id.take_photo_camera_layout)).addView(mPhotoPreview, 0);
        CameraFrameLayout.addView(mPhotoPreview);*/
    }

    View.OnClickListener usePhotoHandler = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if (currentPhoto == null){
                Log.e(TAG, "no photo when use photo clicked! ");
                Toast.makeText(context, "No photo to use!", Toast.LENGTH_SHORT).show();
                return;
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            currentPhoto.compress(Bitmap.CompressFormat.PNG, 100, os);
            DisplayImage.setImageBitmap(null);
            drawablePhoto.recycle();
            byte[] bytes = os.toByteArray();
            Intent currentIntent = getIntent();
            currentIntent.putExtra("data", bytes);
            currentPhoto.recycle();
            currentPhoto = null;
            setResult(RESULT_OK, currentIntent);
            finish();
        }
    };

    View.OnClickListener returnToStreamsHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent viewAllStreamIntent = new Intent(context, ViewAllStream.class); //TODO: should prevent returning to take photo after moving to startActivity
            viewAllStreamIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            viewAllStreamIntent.putExtra(Consts.USER_EMAIL_NAME, userEmail);
            startActivity(viewAllStreamIntent);
        }
    };

    View.OnClickListener takePhotoHandler = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            if (currentPhoto == null){
                //take a new photo if no currentPhoto available
                mCamera.takePicture(null, null, mPicture);
            } else {
                //remove photo and prepare to take a new one
                currentPhoto.recycle();
                currentPhoto = null; // remove existing photo
                UsePhotoBtn.setVisibility(View.INVISIBLE); // hide use photo button
                DisplayImage.setVisibility(View.GONE);
                boolean opened = safeCameraOpen(cameraId);
                if (opened){
                    //mPhotoPreview = new PhotoPreview(context);
                    mPhotoPreview.refreshCamera(mCamera, cameraId);
                    CameraFrameLayout.addView(mPhotoPreview);
                } else {
                    Toast.makeText(context, "Fail to open camera", Toast.LENGTH_SHORT);
                    Log.w(TAG, "fail to open the camera");
                }
            }
        }
    };

    private boolean safeCameraOpen(int id){
        boolean qOpened = false;
        try{
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
            qOpened = (mCamera != null);
        } catch(Exception e){
            Log.e(TAG,"failed to open Camera");
            e.printStackTrace();
        }
        return qOpened;
    }

    private void releaseCameraAndPreview(){
        if (mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() { // asynchronous
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //add writing to file
            if (data != null && data.length > 0){
                if(currentPhoto != null){
                    currentPhoto.recycle();
                }
                if(drawablePhoto != null){
                    drawablePhoto.recycle();
                }
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 4;
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                currentPhoto = BitmapFactory.decodeByteArray(data, 0, data.length, opts); // picture is too large....
                int rotation = mPhotoPreview.getDisplayOrientation();
                currentPhoto.compress(Bitmap.CompressFormat.JPEG, Consts.PHOTO_QUALITY, os);
                byte[] array = os.toByteArray();
                currentPhoto.recycle();
                currentPhoto = BitmapFactory.decodeByteArray(array, 0, array.length);
                if (rotation != 0){
                    Bitmap oldBitmap = currentPhoto;
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotation);
                    currentPhoto = Bitmap.createBitmap(
                            oldBitmap,
                            0,
                            0,
                            oldBitmap.getWidth(),
                            oldBitmap.getHeight(),
                            matrix,
                            false
                    );
                    oldBitmap.recycle();
                }
                drawablePhoto = currentPhoto.copy(Bitmap.Config.ARGB_8888, true);
                UsePhotoBtn.setVisibility(View.VISIBLE);
                // releaseCameraAndPreview(); already done in removing preview
                CameraFrameLayout.removeView(mPhotoPreview);
                DisplayImage.setVisibility(View.VISIBLE);
                DisplayImage.setImageBitmap(drawablePhoto);
                releaseCameraAndPreview();
            }
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        if (mCamera == null){
            mNumberOfCameras = Camera.getNumberOfCameras();
            Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < mNumberOfCameras; i++){
                Camera.getCameraInfo(i, mCameraInfo);
                if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK){ // find id for camera
                    cameraId = i;
                }
            }
            mCamera = Camera.open(cameraId);
        }
        Log.w(TAG, "preview null: " + Boolean.toString(mPhotoPreview == null));
        Log.w(TAG, "camera null: " + Boolean.toString(mCamera == null));
        mPhotoPreview.refreshCamera(mCamera, cameraId);
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCameraAndPreview();
    }

}
