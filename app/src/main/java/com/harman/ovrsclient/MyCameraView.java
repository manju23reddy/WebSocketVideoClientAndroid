package com.harman.ovrsclient;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;

/**
 * Created by Alvin on 2016-05-20.
 */
public class MyCameraView extends SurfaceView implements SurfaceHolder.Callback,Camera.PreviewCallback{
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int width;
    private int height;
    public ByteArrayOutputStream mFrameBuffer;
    private Context con;

    /**
     * Constructor of the MyCameraView
     * @param context
     * @param camera
     */
    public MyCameraView(Context context, Camera camera){
        super(context);
        con=context;
        mCamera=camera;
        mHolder=getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    /**
     * set preview to the camera
     * @param holder
     */
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (null != mCamera) {
                mCamera.setPreviewDisplay(holder);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * surface destroyed function
     * @param holder
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }


    /**
     * surface changed function
     * @param holder
     * @param format
     * @param w
     * @param h
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        try{
            if (null != mCamera) {
                mCamera.stopPreview();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        try{
            if (null != mCamera) {
                //Configration Camera Parameter(full-size)
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPreviewSize(120, 100);
                this.width = parameters.getPreviewSize().width;
                this.height = parameters.getPreviewSize().height;
                parameters.setPreviewFormat(ImageFormat.NV21);
                mCamera.setParameters(parameters);
                // mCamera.setDisplayOrientation(90);
                mCamera.setPreviewCallback(this);
                mCamera.startPreview();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * frame call back function
     * @param data
     * @param camera
     */
    public void onPreviewFrame(byte[] data,Camera camera){
        try{
            //convert YuvImage(NV21) to JPEG Image data
            YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21,this.width,this.height,null);

            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0,0,this.width,this.height),100,baos);
            mFrameBuffer = baos;
        }catch(Exception e){
            Log.d("parse","errpr");
        }
    }

    public ByteArrayOutputStream getCameraView(){
        return mFrameBuffer;
    }



}

