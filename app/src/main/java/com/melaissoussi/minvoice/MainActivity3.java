package com.melaissoussi.minvoice;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class MainActivity3 extends Activity implements CameraBridgeViewBase.CvCameraViewListener2
{
    private static final String TAG = MainActivity3.class.getSimpleName();

    private JavaCameraView javaCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //call super Oncreate
        super.onCreate(savedInstanceState);

        //fullscreen mode
        Window window = getWindow();

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Keep the screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set the layout
        setContentView(R.layout.activity_main);

        // setup the javaCameraView
        javaCameraView = (JavaCameraView) findViewById(R.id.minvoice_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (OpenCVLoader.initDebug())
        {
            Log.e(TAG,"OpenCV is loaded successfully ");
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        } else
        {
            Log.e(TAG,"OpenCV is not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this,baseLoaderCallback);
        }

    }

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case BaseLoaderCallback.SUCCESS:
                {
                    javaCameraView.enableView();
                    break;
                }
                default:
                {
                    super.onManagerConnected(status);
                    break;
                }

            }

        }
    };

    @Override
    public void onCameraViewStarted(int width, int height)
    {
        //
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        return inputFrame.rgba();
    }


    @Override
    public void onCameraViewStopped()
    {
        //
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (javaCameraView != null)
        {
            javaCameraView.disableView();
        }
    }


    public void onDestroy()
    {
        super.onDestroy();
        if (javaCameraView != null)
        {
            javaCameraView.disableView();
        }
    }

    }
