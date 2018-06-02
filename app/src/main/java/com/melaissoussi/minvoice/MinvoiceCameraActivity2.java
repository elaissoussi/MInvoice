package com.melaissoussi.minvoice;

import android.content.ContentValues;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.List;

public class MinvoiceCameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2
{
    // A tag for log output.
    private static final String TAG = MinvoiceCameraActivity.class.getSimpleName();

    // A key for storing the index of the active camera.
    private static final String STATE_CAMERA_INDEX = "cameraIndex";

    // A key for storing the index of the active image size.
    private static final String STATE_IMAGE_SIZE_INDEX = "imageSizeIndex";

    // An ID for items in the image size submenu.
    private static final int MENU_GROUP_ID_SIZE = 2;

    // The index of the active camera.
    private int mCameraIndex;

    // The index of the active image size.
    private int mImageSizeIndex;

    // Whether the active camera is front-facing.
    // If so, the camera view should be mirrored.
    private boolean mIsCameraFrontFacing;

    // The number of cameras on the device.
    private int mNumCameras;

    // The camera view.
    private CameraBridgeViewBase mCameraView;

    // The image sizes supported by the active camera.
    private List<Camera.Size> mSupportedImageSizes;

    // Whether the next camera frame should be saved as a photo.
    private boolean mIsPhotoPending;

    // A matrix that is used when saving photos.
    private Mat mBgr;

    // Whether an asynchronous menu action is in progress.
    // If so, menu interaction should be disabled.
    private boolean mIsMenuLocked;

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
                    mBgr = new Mat();
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

        final Window window = getWindow();

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (savedInstanceState != null)
        {
            mCameraIndex = savedInstanceState.getInt(STATE_CAMERA_INDEX, 0);
            mImageSizeIndex = savedInstanceState.getInt(STATE_IMAGE_SIZE_INDEX, 0);

        } else
        {
            mCameraIndex = 0;
            mImageSizeIndex = 0;
        }

        final Camera camera;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
        {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraIndex, cameraInfo);
            mIsCameraFrontFacing = (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);
            mNumCameras = Camera.getNumberOfCameras();
            camera = Camera.open(mCameraIndex);

        }
        else
        {   // pre-Gingerbread
            // Assume there is only 1 camera and it is rear-facing.
            mIsCameraFrontFacing = false;
            mNumCameras = 1;
            camera = Camera.open();
        }

        final Camera.Parameters parameters = camera.getParameters();
        camera.release();

        mSupportedImageSizes = parameters.getSupportedPreviewSizes();
        final Camera.Size size = mSupportedImageSizes.get(mImageSizeIndex);

        mCameraView = new JavaCameraView(this, mCameraIndex);
        mCameraView.setMaxFrameSize(size.width, size.height);
        mCameraView.setCvCameraViewListener(this);
        setContentView(mCameraView);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        // Save the current camera index.
        savedInstanceState.putInt(STATE_CAMERA_INDEX, mCameraIndex);

        // Save the current image size index.
        savedInstanceState.putInt(STATE_IMAGE_SIZE_INDEX, mImageSizeIndex);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onPause()
    {
        if (mCameraView != null) {
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
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        final Mat rgba = inputFrame.rgba();
        if (mIsPhotoPending)
        {
            mIsPhotoPending = false;
            takePhoto(rgba);
        }
        if (mIsCameraFrontFacing)
        {
            // Mirror (horizontally flip) the preview.
            Core.flip(rgba, rgba, 1);
        }
        return rgba;
    }

    private void takePhoto(final Mat rgba)
    {
            // Determine the path and metadata for the photo.
            final long currentTimeMillis = System.currentTimeMillis();
            final String appName = getString(R.string.app_name);
            final String galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            final String albumPath = galleryPath + File.separator + appName;
            final String photoPath = albumPath + File.separator + currentTimeMillis ;//+ LabActivity.PHOTO_FILE_EXTENSION;

            final ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, photoPath);
            //values.put(MediaStore.Images.Media.MIME_TYPE, LabActivity.PHOTO_MIME_TYPE);
            values.put(MediaStore.Images.Media.TITLE, appName);
            values.put(MediaStore.Images.Media.DESCRIPTION, appName);
            values.put(MediaStore.Images.Media.DATE_TAKEN, currentTimeMillis);
            // Ensure that the album directory exists.
            File album = new File(albumPath);

            if (!album.isDirectory() && !album.mkdirs())
            {
                Log.e(TAG, "Failed to create album directory at " + albumPath);
                //onTakePhotoFailed();
                return;
            }
            // Try to create the photo.
            Imgproc.cvtColor(rgba, mBgr, Imgproc.COLOR_RGBA2BGR, 3);
            if (!Imgcodecs.imwrite(photoPath, mBgr))
            {
                Log.e(TAG, "Failed to save photo to " + photoPath);
                //onTakePhotoFailed();
            }

        Log.d(TAG, "Photo saved successfully to " + photoPath);

        // Try to insert the photo into the MediaStore.
        Uri uri;

        try
        {
            uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
        catch (final Exception e)
        {
            Log.e(TAG, "Failed to insert photo into MediaStore");
            e.printStackTrace();
            // Since the insertion failed, delete the photo.
            File photo = new File(photoPath);
            if (!photo.delete()) {
                Log.e(TAG, "Failed to delete non-inserted photo");
            }
            //onTakePhotoFailed();
            return;
        }

        // Open the photo in LabActivity.
        /*final Intent intent = new Intent(this, LabActivity.class);
        intent.putExtra(LabActivity.EXTRA_PHOTO_URI, uri);
        intent.putExtra(LabActivity.EXTRA_PHOTO_DATA_PATH, photoPath);
        startActivity(intent);
        */
    }


    private void onTakePhotoFailed()
    {
        mIsMenuLocked = false;
        // Show an error message.
        final String errorMessage =
                getString(R.string.photo_error_message);
        runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                Toast.makeText(MinvoiceCameraActivity.this, errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_camera, menu);

        if (mNumCameras < 2)
        {
            // Remove the option to switch cameras, since there is
            // only 1.
            menu.removeItem(R.id.menu_next_camera);
        }

        int numSupportedImageSizes = mSupportedImageSizes.size();

        if (numSupportedImageSizes > 1)
        {
            final SubMenu sizeSubMenu = menu.addSubMenu(R.string.menu_image_size);

            for (int i = 0; i < numSupportedImageSizes; i++)
            {
                final Camera.Size size = mSupportedImageSizes.get(i);
                sizeSubMenu.add(MENU_GROUP_ID_SIZE, i, Menu.NONE, String.format("%dx%d", size.width, size.height));
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (mIsMenuLocked)
        {
            return true;
        }
        if (item.getGroupId() == MENU_GROUP_ID_SIZE) {
            mImageSizeIndex = item.getItemId();
            recreate();
            return true;
        }
        switch (item.getItemId())
        {
            case R.id.menu_next_camera:
                mIsMenuLocked = true;
                // With another camera index, recreate the activity.
                mCameraIndex++;
                if (mCameraIndex == mNumCameras)
                {
                    mCameraIndex = 0;
                }
                mImageSizeIndex = 0;
                recreate();
                return true;
            case R.id.menu_take_photo:
                mIsMenuLocked = true;
                // Next frame, take the photo.
                mIsPhotoPending = true;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
