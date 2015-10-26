package com.aptdemo.yzhao.mobile;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;


import java.io.IOException;
import java.util.List;

/**
 * Created by Yicong on 10/24/15.
 */
public class PhotoPreview extends SurfaceView implements SurfaceHolder.Callback {
    private final String TAG = "PhotoPreview";
    Camera mCamera;
    SurfaceHolder mHolder;
    int cameraId;
    int displayOrientation;

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
        mHolder = holder;
        refreshCamera(mCamera, cameraId);
        /*try{
            determinDisplayOrientation();
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e){
            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        }*/
    }
    public void refreshCamera(Camera camera, int camera_id){
        if (mHolder.getSurface() == null){
            return;
        }
        try{
            mCamera.stopPreview();
        }catch (Exception e){

        }
        mCamera = camera;
        determinDisplayOrientation();
        setupCamera();
        try{
            cameraId = camera_id;
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
        // mCamera.setDisplayOrientation(90);
        refreshCamera(mCamera, cameraId);
    }
    public void determinDisplayOrientation(){
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int rotation = ((WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(displayOrientation);
    }

    private void setupCamera() {
        Camera.Parameters params = mCamera.getParameters();

        Camera.Size bestPreviewSize = determineBestPreviewSize(params);
        Camera.Size bestPictureSize = determineBestPictureSize(params);

        params.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);

        mCamera.setParameters(params);
    }

    private Camera.Size determineBestPreviewSize(Camera.Parameters params){
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        return determineBestSize(sizes, Consts.PREVIEW_SIZE_MAX_WIDTH);
    }
    private Camera.Size determineBestPictureSize(Camera.Parameters params){
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        return determineBestSize(sizes, Consts.PICTURE_SIZE_MAX_WIDTH);
    }
    protected Camera.Size determineBestSize(List<Camera.Size> sizes, int widthThreshold){
        Camera.Size bestSize = null;

        for (Camera.Size currentSize: sizes){
            boolean isDesiredRatio = (currentSize.width / 4) == (currentSize.height / 3);
            boolean isBetterSize = (bestSize == null) || (currentSize.width > bestSize.width);
            boolean isInBounds = currentSize.width <= widthThreshold;

            if (isDesiredRatio && isInBounds && isBetterSize){
                bestSize = currentSize;
            }
        }

        if (bestSize == null){
            Log.e(TAG, "failt to find best size");
            return sizes.get(0);
        }
        return bestSize;
    }
    public int getDisplayOrientation(){
        return displayOrientation;
    }
}
