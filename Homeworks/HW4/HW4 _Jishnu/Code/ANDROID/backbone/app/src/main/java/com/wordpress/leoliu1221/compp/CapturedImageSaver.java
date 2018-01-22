package com.wordpress.leoliu1221.compp;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by leoliu on 3/22/15.
 */
/**
 * Deferred processor responsible for saving snapshots to disk. <p>This is run on
 * {@code mBackgroundThread}.</p>
 */
public class CapturedImageSaver implements Runnable {
    /** The image to save. */
    private Image mImage;
    public static ArrayList<DngCreator> dgList = new ArrayList<DngCreator>();
    static final String CAPTURE_FILENAME_PREFIX = "CompP";
    private DngCreator d=null;
    CameraCharacteristics mCharacteristics = null;
    private Context mContext;
    private CaptureResult result=null;
    private String fileName;
    public CapturedImageSaver(Context context,Image capture,CameraCharacteristics c,CaptureResult r,String fileName) {
        mImage = capture;
        this.mCharacteristics = c;
        this.mContext = context;
        this.result = r;
        this.fileName = fileName;
    }

    /**
     *
     * @return yyyy-MM-dd HH:mm:ss formated date as string
     */
    public static String getCurrentTimeStamp(){
        try {
            Calendar cal = Calendar.getInstance();
            int millisecond = cal.get(Calendar.MILLISECOND);
            int second = cal.get(Calendar.SECOND);
            int minute = cal.get(Calendar.MINUTE);
            //12 hour format
            int hour = cal.get(Calendar.HOUR);
            StringBuilder sb= new StringBuilder();
            sb.append(hour);
            sb.append(":");
            sb.append(minute);
            sb.append(":");
            sb.append(second);
            sb.append(":");
            sb.append(millisecond);
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(MainActivity.TAG, "Directory not created");
        }
        return file;
    }

    /**
     * Cleanup the given {@link OutputStream}.
     *
     * @param outputStream the stream to close.
     */
    private static void closeOutput(OutputStream outputStream) {
        if (null != outputStream) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
           }
        }
    }
    @Override
    public void run() {
        File mFile = null;
        boolean success = false;
            if(mImage == null){
                return;
            }
            int format = mImage.getFormat();
            String suffix = getCurrentTimeStamp() + CAPTURE_FILENAME_PREFIX;
            switch (format) {
                case ImageFormat.JPEG: {
                    ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    FileOutputStream output = null;
                    if(fileName==null){
                        fileName = "JPEG_" + suffix + ".jpg";
                    }
                    mFile = new File(Environment.
                            getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                            fileName);
                    try {
                        output = new FileOutputStream(mFile);
                        output.write(bytes);
                        success = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        mImage.close();
                        closeOutput(output);
                    }
                    break;
                }
                case ImageFormat.RAW_SENSOR:{
                    if(result==null){
                        Log.e(MainActivity.TAG,"ERROR: processing raw but capture result is null");
                    }
                    DngCreator dngCreator = new DngCreator(mCharacteristics, result);
                    FileOutputStream output = null;
                    if(fileName==null){
                        fileName = "RAW_" + suffix + ".dng";
                    }
                    mFile = new File(Environment.
                            getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                            fileName);
                    Log.e(MainActivity.TAG,"writing raw image");
                    try {
                        output = new FileOutputStream(mFile);
                        dngCreator.writeImage(output, mImage);
                        success = true;
                        Log.e(MainActivity.TAG,"raw image written");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        mImage.close();
                        closeOutput(output);
                    }
                    break;
                }
                default: {
                    Log.e(MainActivity.TAG, "Cannot save image, unexpected image format:" + format);
                    break;
                }
        }

        if (success) {
            MediaScannerConnection.scanFile(mContext, new String[]{mFile.getPath()},
                /*mimeTypes*/null, new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {
                    // Do nothing
                }

                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Log.i(MainActivity.TAG, "Scanned " + path + ":");
                    Log.i(MainActivity.TAG, "-> uri=" + uri);
                }
            });
        }
    }
}