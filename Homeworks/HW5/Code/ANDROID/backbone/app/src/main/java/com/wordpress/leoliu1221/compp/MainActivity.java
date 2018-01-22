package com.wordpress.leoliu1221.compp;

import android.app.Activity;
import android.content.Context;

import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;

import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.RggbChannelVector;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import android.widget.Button;
import android.widget.FrameLayout;


import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import static android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE;
import static android.hardware.camera2.CaptureRequest.LENS_FOCUS_DISTANCE;


public class MainActivity extends Activity implements SurfaceHolder.Callback {

    /**
     * A {@link CaptureRequest.Builder} for camera preview.
     */
    private CaptureRequest.Builder mPreviewBuilder;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    public final static String TAG = "Camera2CompPhoto";
    private SurfaceView sf;

    private FrameLayout main_frame;
    private SurfaceHolder surfaceHolder;

    private ImageReader mCaptureBuffer;
    private ImageReader rawCaptureBuffer;

    private Handler mBackgroundHandler;
    private Handler mForegroundHandler;

    private HandlerThread mBackgroundThread;

    CameraCaptureSession mCaptureSession;
    /**
     * A counter for tracking corresponding {@link CaptureRequest}s and {@link CaptureResult}s
     * across the {@link CameraCaptureSession} capture callbacks.
     */
    private final AtomicInteger mRequestCounter = new AtomicInteger();
    private TreeMap<Integer, CaptureResult> rawResults = new TreeMap<Integer, CaptureResult>();
    private LinkedList<Image> rawImages = new LinkedList<Image>();
    private LinkedList<CameraCharacteristics> rawChars = new LinkedList<CameraCharacteristics>();
    private LinkedList<Long> exposures = new LinkedList<Long>();
    private LinkedList<Float> focuses = new LinkedList<Float>();
    int id = 0;

    private Button jpgButton,rawButton, hdrButton, focalButton, changeButton, flashButton;
    private Activity activity = this;
    CameraManager manager;
    String cameraId;
    CameraCharacteristics characteristics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main_frame = (FrameLayout) findViewById(R.id.main_frame);
        sf = (SurfaceView) findViewById(R.id.cameraPreview);
        surfaceHolder = sf.getHolder();
        surfaceHolder.addCallback(this);
        jpgButton = (Button) findViewById(R.id.jpgButton);
        jpgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG,"clicked jpg capture. ");
                captureJPEG();
            }
        });
        rawButton = (Button) findViewById(R.id.rawButton);
        rawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "clicked Raw Capture. ");
                captureRAW();
            }
        });
        hdrButton = (Button) findViewById(R.id.hdrButton);
        hdrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureExposureStack(v);
            }
        });
        focalButton = (Button) findViewById(R.id.focalStackButton);
        focalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureFocalStack(v);
            }
        });
        changeButton = (Button) findViewById(R.id.changeCamButton);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCamera(v);
            }
        });
        flashButton = (Button) findViewById(R.id.flashButton);
        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Flash/Noflash Capture Selected");
                //TODO:hw3

                //Setting the foreground to be black
                final int black_color = 0xFF000000;
                final Drawable f_black_color = new ColorDrawable(black_color);
                main_frame.setForeground(f_black_color);
                Log.e(TAG, "Setting Black Color as Foreground");

                //Taking a  picture as jpg
                captureJPEG();


                //Setting the foreground to be white with 200 ms delay
                final int white_color = 0xFFFFFFFF;
                final Drawable f_white_color = new ColorDrawable(white_color);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Log.e(TAG, "Setting White Color as Foreground");
                        main_frame.setForeground(f_white_color);
                        //Taking picture as jpg
                        captureJPEG();

                    }
                }, 300);

                //set the foreground to be transparent with 400 ms delay
                handler.postDelayed(new Runnable() {
                    public void run() {
                        main_frame.setForeground(null);
                        //TODO: take picture as jpg
                        //captureJPEG();


                    }
                }, 400);

                //same thing as repaint
                invalidateOptionsMenu();
            }
        });

    }


    /**
     * Called when our {@code Activity} loses focus. <p>Tears everything back down.</p>
     */
    @Override
    protected void onPause() {
        super.onPause();
        try {
            // Ensure SurfaceHolderCallback#surfaceChanged() will run again if the user returns
            sf.getHolder().setFixedSize(/*width*/0, /*height*/0);
            // Cancel any stale preview jobs
            if (mCaptureSession != null) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
        } finally {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        }
        // Finish processing posted messages, then join on the handling thread
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
        } catch (InterruptedException ex) {
            Log.e(TAG, "Background worker thread was interrupted while joined", ex);
        }
        // Close the ImageReader now that the background thread has stopped
        if (mCaptureBuffer != null) mCaptureBuffer.close();
        if (rawCaptureBuffer != null) rawCaptureBuffer.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //how many cameras do I have?

    /**
     * @return list of available cameras in String[]
     * @throws CameraAccessException
     */
    public String[] getCameraIdList() throws CameraAccessException {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        return manager.getCameraIdList();

    }

    /**
     * @param id the id for the camera
     * @return
     * @throws CameraAccessException
     */
    public CameraCharacteristics cameraDetails(String id) throws CameraAccessException {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        return manager.getCameraCharacteristics(id);
    }

    /**
     * get the camera characteristics of the current camera.
     *
     * @return
     * @throws CameraAccessException
     */
    public CameraCharacteristics getCharacteristics() throws CameraAccessException {
        if (manager != null) {
            return manager.getCameraCharacteristics(manager.getCameraIdList()[id]);
        } else {
            return characteristics;
        }
    }



    /**
     * open the camera device for i
     *
     * @param i
     */
    public void openCamera(int i) {
        try {
            manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraId = manager.getCameraIdList()[i];

            characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // Bigger is better when it comes to saving our image
            // TODO:hw3
            // changing largestSize to be a different one if you want.
            // you can simply say largestSize = map.getOutputSizes(ImageFormat.JPEG)[#] where # cannot be greater than length of all available output sizes.
            Size largestSize = Collections.max(
                    Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                    new CompareSizesByArea());
            // Prepare an ImageReader in case the user wants to capture images
            Log.i(TAG, "Capture size: " + largestSize);
            Log.i(TAG, "SurfaceView size: " +
                    sf.getWidth() + 'x' + sf.getHeight());

            Log.i(TAG, Arrays.toString(map.getOutputSizes(SurfaceHolder.class)));

            Size optimalSize = chooseBigEnoughSize(
                    map.getOutputSizes(SurfaceHolder.class), sf.getWidth(), sf.getHeight());
            // Set the SurfaceHolder to use the camera's largest supported size
            surfaceHolder.setFixedSize(optimalSize.getWidth(),
                    optimalSize.getHeight());
            Log.i(TAG, "Preview size: " + optimalSize);
            mCaptureBuffer = ImageReader.newInstance(largestSize.getWidth(),
                    largestSize.getHeight(), ImageFormat.JPEG, /*maxImages*/5);
            mCaptureBuffer.setOnImageAvailableListener(
                    mImageCaptureListener, mBackgroundHandler);
            Size[] rawSizes = map.getOutputSizes(ImageFormat.RAW_SENSOR);
            Log.e(TAG, "raw sizes:" + Arrays.toString(rawSizes));
            Size rawSize = rawSizes[rawSizes.length - 1];
            Log.e(TAG, "raw size: " + rawSize.toString());
            //TODO:hw2
            //TODO: change the # as needed.
            rawCaptureBuffer = ImageReader.newInstance(rawSize.getWidth(), rawSize.getHeight(), ImageFormat.RAW_SENSOR, 51/* #of captures allowed */);
            rawCaptureBuffer.setOnImageAvailableListener(mRawCaptureListener, mBackgroundHandler);
            manager.openCamera(cameraId, mStateCallback, mForegroundHandler);
        } catch (CameraAccessException ex) {
            Log.e(TAG, "not able to open camera");
            ex.printStackTrace();
        }
    }

    // onImageAvailableListener, called when image is avaiable.
    final ImageReader.OnImageAvailableListener mImageCaptureListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    // Save the image once we get a chance
                    //TODO: change filename as needed
                    //You can change the last null to a name you like. Dont forget to add '.jpg' at the end
                    //e.g. if you want for your exposure stack you can put exposures.remove().toString()+".jpg"
                    //e.g. if you want for your focus stack you can put focuses.remove().toSTring()+".jpg"
                    //damn i will just make it here:
                    if(exposures.size()>0){
                        mBackgroundHandler.post(new CapturedImageSaver(activity, reader.acquireLatestImage(), null, null,exposures.remove().toString()+".jpg"/*filename*/));
                    }
                    else if(focuses.size()>0){
                        mBackgroundHandler.post(new CapturedImageSaver(activity,reader.acquireLatestImage(),null,null,focuses.remove().toString()+".jpg"));
                    }
                    else {
                        mBackgroundHandler.post(new CapturedImageSaver(activity, reader.acquireLatestImage(), null, null, null/*filename*/));
                    }
                    // Control flow continues in CapturedImageSaver#run()
                    Log.e(TAG, "image ready");
                    // Control flow continues in CapturedImageSaver#run()
                }
            };
    // onImageAvailableListener, called when raw image is avaiable.
    final ImageReader.OnImageAvailableListener mRawCaptureListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    //save the raw image on rawImages linked list
                    //We do not post request to image saver because we still need to wait for capture result. Unlike jpeg images.
                    rawImages.add(reader.acquireLatestImage());
                }
            };
    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles the still JPEG and RAW capture
     * request.
     */
    private final CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {

            int requestId = (int) request.getTag();
            //rawResults.put(requestId, result);
            Log.e(TAG, "raw results size" + rawResults.size());
            Log.e(TAG, "raw image size" + rawImages.size());
            String tag = request.getTag().toString();
            //You can change the last null to a name you like. Dont forget to add '.dng' at the end
            if(rawImages.size()!=0 && rawChars.size()!=0) {
                mBackgroundHandler.post(new CapturedImageSaver(activity, rawImages.remove(), rawChars.remove(), result, tag+".dng"));
            }
            else{
                Log.e(TAG,"raw image list size: "+rawImages.size());
                Log.e(TAG, "raw result list size: " + rawResults.size());
            }
            // Control flow continues in CapturedImageSaver#run()

        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request,
                                    CaptureFailure failure) {
            Log.e(TAG, "Capture failed!");
        }

    };


    @Override
    public void onResume() {
        super.onResume();
        mBackgroundThread = new HandlerThread("background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        mForegroundHandler = new Handler(getMainLooper());
        Log.e(TAG, "onResume");

    }

    //camera state call back
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            Log.e(TAG, "onOpened");
            mCameraDevice = camera;
            List<Surface> outputs = Arrays.asList(rawCaptureBuffer.getSurface(), sf.getHolder().getSurface(), mCaptureBuffer.getSurface());
            try {
                mCameraDevice.createCaptureSession(outputs, mCaptureSessionListener, mBackgroundHandler);
            } catch (CameraAccessException e) {
                Log.e(TAG, "conf error?");
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.e(TAG, "on disconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.e(TAG, "on error");
        }
    };
    /**
     * Callbacks invoked upon state changes in our {@code CameraCaptureSession}. <p>These are run on
     * {@code mBackgroundThread}.</p>
     */
    final CameraCaptureSession.StateCallback mCaptureSessionListener =
            new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    Log.i(TAG, "Finished configuring camera outputs");
                    mCaptureSession = session;
                    SurfaceHolder holder = sf.getHolder();
                    if (holder != null) {
                        try {
                            // Build a request for preview footage
                            CaptureRequest.Builder requestBuilder =
                                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                            requestBuilder.addTarget(holder.getSurface());
                            CaptureRequest previewRequest = requestBuilder.build();
                            //requestBuilder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.CONTROL_AE_MODE_ON);

                            // Start displaying preview images
                            try {
                                session.setRepeatingRequest(previewRequest, /*listener*/null,
                                /*handler*/null);
                            } catch (CameraAccessException ex) {
                                Log.e(TAG, "Failed to make repeating preview request", ex);
                            }
                        } catch (CameraAccessException ex) {
                            Log.e(TAG, "Failed to build preview request", ex);
                        }
                    } else {
                        Log.e(TAG, "Holder didn't exist when trying to formulate preview request");
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(TAG, "conf error.....");
                }
            };



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "surfaceCreated");
        openCamera(id);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the respective requested values.
     *
     * @param choices The list of sizes that the camera supports for the intended output class
     * @param width   The minimum desired width
     * @param height  The minimum desired height
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    static Size chooseBigEnoughSize(Size[] choices, int width, int height) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<Size>();
        for (Size option : choices) {
            if (option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    /**
     * capture a jpeg image.
     */
    public void captureJPEG() {
        if (mCaptureSession != null) {
            try {
                CaptureRequest.Builder requester =
                        mCameraDevice.createCaptureRequest(mCameraDevice.TEMPLATE_MANUAL);
                requester.addTarget(mCaptureBuffer.getSurface());

                try {
                    // This handler can be null because we aren't actually attaching any callback
                    mCaptureSession.capture(requester.build(), null, mBackgroundHandler);
                } catch (CameraAccessException ex) {
                    Log.e(TAG, "Failed to file actual capture request", ex);
                }
            } catch (CameraAccessException ex) {
                Log.e(TAG, "Failed to build actual capture request", ex);
            }
        } else {
            Log.e(TAG, "User attempted to perform a capture outside the session");
        }
    }


    /**
     * Capture one or series of raw images. with specific settings.
     */
    public void captureRAW() {
        //TODO:hw2
        //Set different capture numbers, but no larger than the buffer size (defaults to 51)
        for (int i = 0; i < 51; i++) {
            //sleep the system clock for 200 ms
            SystemClock.sleep(200);
            if (mCaptureSession != null) {
                Log.e(TAG, "setting up raw capture call backs");
                try {
                    CaptureRequest.Builder requester =
                            mCameraDevice.createCaptureRequest(mCameraDevice.TEMPLATE_MANUAL);
                    requester.addTarget(rawCaptureBuffer.getSurface());
                    requester.setTag(mRequestCounter.getAndIncrement());

                    //TODO:hw2
                    //Set different gains
                    //Gains = k* Sensitivity (true)
                    //TODO: getting range of sensitivities supported using SENSOR_INFO_SENSITIVITY_RANGE:
                    Range<Integer> sstt = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                    Log.e(TAG, "Lower range: " + sstt.getLower());
                    Log.e(TAG, "Upper Range: " + sstt.getUpper());

                    //TODO:getting lower and higher sensitivity
                    Integer lower_sstt = 100;
                    Integer higher_sstt = 1600;
                    Log.e(TAG, "lowersstt " + lower_sstt);
                    Log.e(TAG, "highersstt " + higher_sstt);
                    //TODO:Change the capture request's sensitivity
                    requester.set(CaptureRequest.SENSOR_SENSITIVITY, 1600); //Gain values will be set as 100 and 1600
                    try {
                        // This handler can be null because we aren't actually attaching any callback
                        rawChars.add(getCharacteristics());
                        mCaptureSession.capture(requester.build(), mCaptureCallback, mBackgroundHandler);
                    } catch (CameraAccessException ex) {
                        Log.e(TAG, "Failed to file actual capture request", ex);
                    }
                } catch (CameraAccessException ex) {
                    Log.e(TAG, "Failed to build actual capture request", ex);
                }
            } else {
                Log.e(TAG, "User attempted to perform a capture outside the session");
            }
        }
    }

    /**
     * Capture a series of photos with changing focal distance
     *
     * @param v
     */
    public void captureFocalStack(View v) {
        //HW:5

        //To get minimum and maximum focal
        float minimumLens = characteristics.get(CameraCharacteristics.LENS_INFO_HYPERFOCAL_DISTANCE);
        float maximumLens = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);

        Log.e(TAG, "Minimum_Focus_Distance: " + minimumLens);
        Log.e(TAG, "Maximum_Focus_Distance: " + maximumLens);

        float prev_focus = minimumLens;
        Log.e(TAG, "in captureFocalStack");
        //check if capture session is null
        if (mCaptureSession != null) {
            Log.e(TAG, "prevLens: " + prev_focus);
            //TODO: check if focus distance after changing is in range
            while (prev_focus * 1.1 < maximumLens) {
                //sleep system clock for 20 ms
                SystemClock.sleep(20);
                Log.e(TAG, "in captureFocalStack while loop");
                try {
                    //Setting current focus to be 1.5 * previous focus
                    float curr_focus;
                    curr_focus = (float) 1.1 * prev_focus;
                    focuses.add(curr_focus);
                    CaptureRequest.Builder requester = mCameraDevice.createCaptureRequest(mCameraDevice.TEMPLATE_MANUAL);

                    //Turning off Auto Focus for Camera
                    requester.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);
                    requester.addTarget(mCaptureBuffer.getSurface());

                    //Setting Focus Distance to current focus
                    requester.set(LENS_FOCUS_DISTANCE, curr_focus);

                    //set previous focus = current focus
                    prev_focus = curr_focus;
                    try {
                        Log.e(TAG, "Current Focus: " + curr_focus);

                        mCaptureSession.capture(requester.build(), /*listener*/null, /*handler*/null);
                    } catch (CameraAccessException ex) {
                        Log.e(TAG, "Failed to file actual capture request", ex);
                    }
                } catch (CameraAccessException ex) {
                    Log.e(TAG, "Failed to build actual capture request", ex);
                }
            }
        } else {
            Log.e(TAG, "User attempted to perform a capture outside the session");
        }

    }

    /**
     * Change the camera from back to front, or from front to back.
     *
     * @param v View from where its called.
     */
    public void changeCamera(View v) {
        onPause();
        mBackgroundThread = new HandlerThread("background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        mForegroundHandler = new Handler(getMainLooper());
        //TODO:hw1
        //TODO: Makesure you change the camera (0 to 1 or 1 to 1)
        if (id==0)
            id++;
        else id--;
        openCamera(id);
        Log.e(TAG, "opening camera. camera id: " + id);

    }

    /**
     * Capture a sequence of photo with different exposures times.
     *
     * @param v
     */
    public void captureExposureStack(View v) {

        //TODO:HW4
        //TODO: Getting min and max exposures.

        //Getting max and min exposure values from Camera Sensor
        Range<Long> exposureRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
        Log.e(TAG, "Minimum Exposure: " + exposureRange.getLower());
        Log.e(TAG, "Maximum Exposure: " + exposureRange.getUpper());
        Long minimumExposure = exposureRange.getLower();
        Long maximumExposure = exposureRange.getUpper();
        Long prevExposure = minimumExposure;

        //check if 2* exposure > maximumExposure
        while (prevExposure + prevExposure < maximumExposure) {
            try {
                //sleep the system for 20ms between each capture.
                SystemClock.sleep(20);

                //Exposure Time has been updated as factor of 2
                prevExposure = prevExposure + prevExposure;

                //Creating a Capture Request
                CaptureRequest.Builder requester = mCameraDevice.createCaptureRequest(mCameraDevice.TEMPLATE_MANUAL);

                //Setting Requester Response Time
                requester.set(CaptureRequest.SENSOR_EXPOSURE_TIME, prevExposure);

                //Adding surface
                requester.addTarget(mCaptureBuffer.getSurface());

                //Check capture session and make capture request
                if (mCaptureSession != null)
                    exposures.add(prevExposure);
                    mCaptureSession.capture(requester.build(), null, null);
                    Log.e(TAG, "Image exposure: " + prevExposure);

            } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to build actual capture request", e);
            }
        }

    }

}
