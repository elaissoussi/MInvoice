package com.melaissoussi.minvoice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class MinvoiceCameraActivity3 extends AppCompatActivity implements PortraitCameraView.CvCameraViewListener2
{
    // A tag for log output.
    private static final String TAG = MinvoiceCameraActivity3.class.getSimpleName();

    // Minoice Camera view
    private PortraitCameraView mCameraView;

    // The OpenCV loader callback.
    private BaseLoaderCallback mBaseLoaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case BaseLoaderCallback.SUCCESS:
                {
                    mCameraView.enableView();
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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // setup window
        Window window = getWindow();
        // fullscreen
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // setup the layout
        setContentView(R.layout.activity_minvoice_camera);

        // setup the javaCameraView
        mCameraView = (PortraitCameraView) findViewById(R.id.minvoice_camera_view);
        mCameraView.setVisibility(SurfaceView.VISIBLE);
        mCameraView.setCvCameraViewListener(this);
    }


    @Override
    public void onPause()
    {
        if (mCameraView != null)
        {
            mCameraView.disableView();
        }
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (OpenCVLoader.initDebug())
        {
            Log.i(TAG,"OpenCV is loaded successfully ");
            mBaseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
        else
        {
            Log.e(TAG,"OpenCV is not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mBaseLoaderCallback);
        }
    }

    @Override
    public void onDestroy()
    {
        if (mCameraView != null)
        {
            mCameraView.disableView();
        }
        super.onDestroy();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        return inputFrame.rgba();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() {}

}
