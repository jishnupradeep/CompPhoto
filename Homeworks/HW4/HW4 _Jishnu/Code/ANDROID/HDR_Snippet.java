//Android Snippet for HW4: HDR + Tone Mapping
//Minimum Exposure: 33600
//Maximum Exposure: 358732928


public void captureExposureStack(View v) {


        //Getting max and min exposure values from Camera Sensor
        Range<Long> exposureRange = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
        Log.e(TAG, "Minimum Exposure: " + exposureRange.getLower());
        Log.e(TAG, "Maximum Exposure: " + exposureRange.getUpper());
        Long minimumExposure = exposureRange.getLower();
        Long maximumExposure = exposureRange.getUpper();
        Long prevExposure = minimumExposure;

        //Check if 2* exposure > maximumExposure
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

            } catch (CameraAccessException e) {
                Log.e(TAG, "Failed to build actual capture request", e);
            }
        }

    }



