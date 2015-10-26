package com.aptdemo.yzhao.mobile;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Yicong on 10/24/15.
 */
public class PhotoPreview extends SurfaceView implements SurfaceHolder.Callback {
    private final String TAG = "PhotoPreview";
    Camera mCamera;
    SurfaceHolder mHolder;

    PhotoPreview(Context context){
        super(context);
        //mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        // android set type automatically when needed, only needed for api < 11
        //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder){
        Log.w(TAG, "surface created");
        try{
            mCamera.setPreviewDisplay(holder); // TODO: use holder or mHolder ??
            mCamera.startPreview();
        } catch (IOException e){
            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }
    public void refreshCamera(Camera camera){
        if (mHolder.getSurface() == null){
            return;
        }
        try{
            mCamera.stopPreview();
        }catch (Exception e){

        }
        setCamera(camera);
        try{
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }catch(Exception e){
            Log.w(TAG, "Error starting camera preview " + e.getMessage());
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        /*if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
        }
        mHolder.removeCallback(this);*/
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h){
        refreshCamera(mCamera);
    }
    public void setCamera(Camera camera){
        mCamera = camera;
    }


}
