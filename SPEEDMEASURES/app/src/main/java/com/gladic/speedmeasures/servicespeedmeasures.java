package com.gladic.speedmeasures;

import static java.lang.System.exit;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;

import java.text.DecimalFormat;

/*
* https://developer.android.com/guide/components/services.html
* https://developer.android.com/guide/components/bound-services.html
* https://stackoverflow.com/questions/39507506/how-to-execute-background-task-when-android-app-is-closed-set-to-background
* */

public class servicespeedmeasures extends Service implements LocationListener {

    private IBinder binder;

    boolean allowRebind; // indicates whether onRebind should be used

    protected LocationManager locationManager;

    private String startlongitude,startlatitude,endlongitude,endlatitude;

    private String measureunit ="";

    private TextView tspeed;
    private TextView speedconvention;
    private TextView distancemade;

    private int speedconvert = 0;

    private float[] results = {0, 0, 0, 0};;
    private float distancedone=0;

    private boolean foreground_enabled = false;

    DecimalFormat df = new DecimalFormat();


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */


    @Override
    public void onCreate() {

        binder = new LocalBinder();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return -1;
            exit(-1);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    public class LocalBinder extends Binder {
        servicespeedmeasures getService() {
            // Return this instance of LocalService so clients can call public methods
            return servicespeedmeasures.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        Log.d("bound", "bound");
        return binder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return allowRebind;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }
    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        /*AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                while(true){}
            }
        });
        */
        return android.app.Service.START_STICKY;
    }

    @Override
    public void onLocationChanged(Location location) {

        if(location.isFromMockProvider())
        {
            return;
        }

        int speed =0;

        startlatitude = endlatitude;
        startlongitude = endlongitude;

        endlatitude = String.valueOf(location.getLatitude());
        endlongitude = String.valueOf(location.getLongitude());

        switch(speedconvert) {
            case 0 :
                speed = (int) ((location.getSpeed() * 3600) / 1000);
                break;
            case 1:
                speed = (int) (location.getSpeed() * 2.2369);
                break;
        }

        String get_final_velocity = (speed > 9) ? "0" + String.valueOf(speed) : (speed > 99) ? String.valueOf(speed) : "00" + String.valueOf(speed);

        if(!foreground_enabled)
            tspeed.setText(get_final_velocity);

        if(endlatitude == null || startlatitude == null)
        {}
        else {
            location.distanceBetween(Float.valueOf(startlatitude), Float.valueOf(startlongitude), Float.valueOf(endlatitude), Float.valueOf(endlongitude), results);

            if (results.length > 0 && speed > 0) {
                distancedone = distancedone + results[0];

                if(distancedone > 1000)
                {
                    if(speedconvert == 0)
                        measureunit = "km";
                    else
                        measureunit = "mi";

                    if(!foreground_enabled)
                        distancemade.setText(df.format((speedconvert == 0)?distancedone / 1000 :distancedone * 0.6213712f) + measureunit);
                }else
                {
                    if(speedconvert == 0)
                        measureunit = "m";
                    else
                        measureunit = "mi";

                    if(!foreground_enabled)
                        distancemade.setText(String.valueOf((speedconvert == 0)?distancedone:distancedone * 0.6213712f) + measureunit);
                }

            }
        }
    }

    public void settextView(TextView tspeed,TextView speedconvention,TextView distancemade)
    {
        this.tspeed = tspeed;
        this.speedconvention = speedconvention;
        this.distancemade = distancemade;
    }

    public String getlatitude()
    {
        return endlatitude;
    }

    public String getlongitude()
    {
        return endlongitude;
    }

    public float getdistancedone()
    {
        return distancedone;
    }

    public void setdistancedone(float value)
    {
        distancedone = value;
    }

    public void cleantotaldistance()
    {
        distancedone = 0;
        distancemade.setText(0 + "");
    }

    public void setspeedconversor(int value)
    {
        speedconvert = value;
    }

    public void foreground_enable(){foreground_enabled = !foreground_enabled;}

}
