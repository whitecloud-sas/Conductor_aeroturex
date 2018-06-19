package com.whitecloud.hm.whiteclouduser;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class SplashScreen extends Activity {

    private static final int REQUEST_READ_PHONE_STATE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    //Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
            }, REQUEST_READ_PHONE_STATE);
        } else {

            //redirect to MainActivity after 2 seconds
            Thread timerThread = new Thread() {
                public void run() {
                    try {
                        sleep(0000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        Intent intent = new Intent(SplashScreen.this, Inicio_sesion.class);
                        startActivity(intent);
                    }
                }
            };
            timerThread.start();
        }
    }
}
