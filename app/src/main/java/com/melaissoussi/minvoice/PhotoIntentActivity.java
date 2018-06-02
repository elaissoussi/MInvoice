package com.melaissoussi.minvoice;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PhotoIntentActivity extends Activity
{
	//
	public static final File EXTERNAL_STORAGE_DIRECTORY = Environment.getExternalStorageDirectory();    //
	//
	private static final String JPEG_FILE_PREFIX = "IMG_";
	//
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	//
	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	//
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
	//
	private static final String ALBUM_NAME = "minvoice";

	// Standard storage location for digital camera files
	private static final String CAMERA_DIR = "/dcim/";

	private static final String AUTHORITY="com.commonsware.android.cp.v4file";

	//
	public static final String ALBUM_DIRECTORY_PATH_ = EXTERNAL_STORAGE_DIRECTORY + CAMERA_DIR + ALBUM_NAME;
	public static final String YYYY_M_MDD_H_HMMSS = "yyyyMMdd_HHmmss";

	//
	private ImageView mImageView;
	//
	private Bitmap mImageBitmap;
	//
	private String mCurrentPhotoPath;

	//
	Button.OnClickListener mTakePicOnClickListener = new Button.OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			dispatchTakePictureIntent();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_main);

		mImageView = (ImageView) findViewById(R.id.imageView1);
		mImageBitmap = null;

		Button picBtn = (Button) findViewById(R.id.btnIntend);

		picBtn.setOnClickListener(mTakePicOnClickListener);
	}

	private void dispatchTakePictureIntent()
	{
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		File f = null;

		try
		{
			f = setUpPhotoFile();
			mCurrentPhotoPath = f.getAbsolutePath();
			Uri uriForFile = Uri.fromFile(f);
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);

		} catch (IOException e)
		{
			e.printStackTrace();
			f = null;
			mCurrentPhotoPath = null;
		}
		startActivityForResult(takePictureIntent, 0);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == RESULT_OK && data != null)
		{
			convertImageToBitmap();
			galleryAddPhoto();
			mCurrentPhotoPath = null;
		}
	}

	private void convertImageToBitmap()
	{
		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0))
		{
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

		/* Associate the Bitmap to the ImageView */
		mImageView.setImageBitmap(bitmap);
		mImageView.setVisibility(View.VISIBLE);
	}

	private void galleryAddPhoto()
	{
		Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
	}

	private File getAlbumDirectory()
	{
		File albumDirectory = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			albumDirectory = new File (ALBUM_DIRECTORY_PATH_);
			boolean isAlbumDirCreated = albumDirectory.mkdirs();

			if (!isAlbumDirCreated)
			{
				Log.d("LOG Minvoice", "failed to create directory");
				return null;
			}
		}
		else
		{
			Log.v("LOG Minvoice", "External storage is not mounted READ/WRITE.");
		}

		return albumDirectory;
	}

	private File createTempImageFile() throws IOException
	{
		// Create an image file name
		Date date = new Date();

		String timeStamp = new SimpleDateFormat(YYYY_M_MDD_H_HMMSS).format(date);

		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";

		File albumF = getAlbumDirectory();

		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);

		return imageF;
	}

	private File setUpPhotoFile() throws IOException
	{
		File f = createTempImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		return f;
	}


}