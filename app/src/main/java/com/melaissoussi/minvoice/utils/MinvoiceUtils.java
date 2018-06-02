package com.melaissoussi.minvoice.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by melaissoussi on 01/11/17.
 */

public class MinvoiceUtils
{
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    public static final String YYYY_M_MDD_H_HMMSS = "yyyyMMdd_HHmmss";


    private MinvoiceUtils()
    {

    }

    public static File getAlbumDirectory()
    {
        File albumDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());
        return albumDirectory;
    }

    public static File createImageFile() throws IOException
    {
        Date date = new Date();

        String timeStamp = new SimpleDateFormat(YYYY_M_MDD_H_HMMSS).format(date);

        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";

        File albumF = getAlbumDirectory();

        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);

        return imageF;
    }
}
