package com.melaissoussi.minvoice;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private Mat mGrey, mRgba, mIntermediateMat;

    private JavaCameraView javaCameraView;

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
    protected void onCreate(Bundle savedInstanceState)
    {
        //call super Oncreate
        super.onCreate(savedInstanceState);

        //fullscreen mode
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Keep the screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // set the layout
        setContentView(R.layout.activity_main);

        // setup the javaCameraView
        javaCameraView = (JavaCameraView) findViewById(R.id.camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
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

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        mGrey = inputFrame.gray();
        mRgba = inputFrame.rgba();

        detectText();
        return mRgba;
    }

    @Override
    public void onCameraViewStarted(int width, int height)
    {
        mIntermediateMat = new Mat();
        mGrey = new Mat(height, width, CvType.CV_8UC4);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped(){
        mGrey.release();
        mRgba.release();
    }

    private void detectText()
     {
            MatOfKeyPoint keypoint = new MatOfKeyPoint();
            List<KeyPoint> listpoint;
            KeyPoint kpoint;
            Mat mask = Mat.zeros(mGrey.size(), CvType.CV_8UC1);
            int rectanx1;
            int rectany1;
            int rectanx2;
            int rectany2;
            int imgsize = mGrey.height() * mGrey.width();
            Scalar zeos = new Scalar(0, 0, 0);

            List<MatOfPoint> contour2 = new ArrayList<MatOfPoint>();
            Mat kernel = new Mat(1, 50, CvType.CV_8UC1, Scalar.all(255));
            Mat morbyte = new Mat();
            Mat hierarchy = new Mat();

            Rect rectan3;
        //
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.MSER);
        detector.detect(mGrey, keypoint);

        listpoint = keypoint.toList();
        //
        for (int ind = 0; ind < listpoint.size(); ind++)
        {
            kpoint = listpoint.get(ind);
            rectanx1 = (int) (kpoint.pt.x - 0.5 * kpoint.size);
            rectany1 = (int) (kpoint.pt.y - 0.5 * kpoint.size);
            rectanx2 = (int) (kpoint.size);
            rectany2 = (int) (kpoint.size);
            if (rectanx1 <= 0)
                rectanx1 = 1;
            if (rectany1 <= 0)
                rectany1 = 1;
            if ((rectanx1 + rectanx2) > mGrey.width())
                rectanx2 = mGrey.width() - rectanx1;
            if ((rectany1 + rectany2) > mGrey.height())
                rectany2 = mGrey.height() - rectany1;
            Rect rectant = new Rect(rectanx1, rectany1, rectanx2, rectany2);
            try
            {

                Mat roi = new Mat(mask, rectant);
                roi.setTo(CONTOUR_COLOR);

            } catch (Exception ex) {
                Log.d("mylog", "mat roi error " + ex.getMessage());
            }
        }



    }
