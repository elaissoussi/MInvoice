package com.melaissoussi.minvoice;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.melaissoussi.minvoice.utils.MinvoiceUtils;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private Button btnCamera;
    private ImageView capturedImage;
    private Uri outputFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Typeface font = Typeface.createFromAsset( getAssets(), "fontawesome-webfont.ttf" );
        btnCamera = (Button) findViewById(R.id.btnCamera);

        capturedImage= (ImageView) findViewById(R.id.capturedImage);

        btnCamera.setTypeface(font);

        btnCamera.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openCamera();
            }

        });
    }


    private void openCamera()  {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // set flag to give temporary permission to external app to use your FileProvider
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        File file = null;
        try
        {
            file = MinvoiceUtils.createImageFile();

        } catch (IOException e)
        {
            e.printStackTrace();
        }

        // generate URI, I defined authority as the application ID in the Manifest, the last param is file I want to open
        outputFileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            /* Decode the JPEG file into a Bitmap */
            Bitmap bitmap = BitmapFactory.decodeFile(outputFileUri.getPath());

		    /* Associate the Bitmap to the ImageView */
            capturedImage.setImageBitmap(bitmap);

            Log.i(TAG,"Image reference " + outputFileUri );
            //Bitmap bp = (Bitmap) data.getExtras().get("data");
            //capturedImage.setImageBitmap(bp);
        }
    }

}
