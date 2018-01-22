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