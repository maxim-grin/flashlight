package com.grin.flashlight;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;


public class MainActivity extends Activity {

    ImageButton btnSwitch;

    private Camera camera;
    private boolean isFlashOn;
    private boolean hasFlash;
    private Camera.Parameters parameters;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /*
         * First check if device is supporting flashlight or not
         */
        hasFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            // device doesn't support flash
            // show alert message and close the application

            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Error!");
            alertDialog.setMessage("Sorry, your device doesn't support flashlight!");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    //closing the application
                    finish();
                }
            });
            alertDialog.show();
            return;
        }

        // get the camera
        getCamera();

        //flash switch button
        btnSwitch = (ImageButton) findViewById(R.id.btnSwitch);

        /**
         * Switch click event to toggle flash on/off
         */
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFlashOn) {
                    // turn off flash
                    turnOffFlash();
                } else {
                    // turn on flash
                    turnOnFlash();
                }
            }
        });
    }

    /**
     * Getting camera parameters
     */
    //
    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                parameters = camera.getParameters();
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } catch (RuntimeException e) {
                Log.e("Camera error. Failed to open. Error: ", e.getMessage());
            }
        }
    }

    /**
     * Turning on flash
     */
    private void turnOnFlash() {
        if (!isFlashOn) {
            if (camera == null || parameters == null) {
                return;
            }
            // play sound
            playSound();

            parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            camera.startPreview();
            isFlashOn = true;

            // changing button switch image
            toggleButtonImage();
        }
    }

    /**
     * Turning off flash
     */
    private void turnOffFlash() {
        if (isFlashOn) {
            if (camera == null || parameters == null) {
                return;
            }
            // play sound
            playSound();

            parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
            camera.stopPreview();
            isFlashOn = false;

            // changing button switch image
            toggleButtonImage();
        }
    }

    /**
     * Toggle switch button images
     * changing image state to on/off
     */
    private void toggleButtonImage() {
        if (isFlashOn) {
            btnSwitch.setImageResource(R.drawable.btn_switch_on);
        } else {
            btnSwitch.setImageResource(R.drawable.btn_switch_off);
        }

    }

    /**
     * Play sound
     * will play button toggle sound on flash on/off
     */
    private void playSound() {
        if (isFlashOn) {
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.light_switch_off);
        } else {
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.light_switch_on);
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
            }
        });
        mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // on pause turn off the flash
        turnOffFlash();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // on resume turn on the flash
        if (hasFlash) {
            turnOffFlash();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // on start get camera parameters
        getCamera();

        isFlashOn = true;
        // turn off flash on start
        turnOffFlash();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // on stop release the camera
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}
