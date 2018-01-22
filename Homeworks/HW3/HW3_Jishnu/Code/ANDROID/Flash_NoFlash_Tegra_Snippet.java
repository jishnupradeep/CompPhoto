//HW3: Flash/NoFlash Photography
//This is the main snippet from the code for Tegra Tablet that simulated flash in order to capture the JPEG images


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