//Android Snippet for HW4: HDR + Tone Mapping
//Minimum Focal Distance: 0.6666
//Maximum Focal Distance: 10.0

    public void captureFocalStack(View v) {
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
                    //Setting current focus to be 1.1 * previous focus
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
