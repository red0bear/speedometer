package com.gladic.speedmeasures;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public  class MainActivity extends AppCompatActivity implements LocationListener {
    protected LocationManager locationManager;

    private static final String PREFS_NAME = "com.gladic.speedmeasures";
    private static final String PREFS_DISTANCE_STRING = "distancestring";

    private String startlongitude,startlatitude,endlongitude,endlatitude;

    private TextView tspeed;
    private TextView speedconvention;
    private TextView distancemade;

    private int speedconvert = 0;

    private Button conversormeasure;
    private Button latlongtoast;
    private Button toastgoogle;
    private Button toastwaze;

    private Button cleardistance;

    private Context ctx;

    private float[] results = {0, 0, 0, 0};;
    private float distancedone=0;

    DecimalFormat df = new DecimalFormat();

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ctx = getApplicationContext();

        mPrefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        tspeed = (TextView) findViewById(R.id.SPEED);
        speedconvention = (TextView) findViewById(R.id.speedconvention);
        distancemade = (TextView) findViewById(R.id.distancemade);

        conversormeasure = (Button) findViewById(R.id.speedconversor);
        latlongtoast = (Button) findViewById(R.id.toastlatlong);
        toastgoogle = (Button) findViewById(R.id.toastgoogle);
        toastwaze = (Button) findViewById(R.id.toastwaze);

        cleardistance = (Button) findViewById(R.id.cleardistance);

        distancedone = mPrefs.getFloat(PREFS_DISTANCE_STRING,0);

        df.setMaximumFractionDigits(3);

        if(distancedone > 1000)
        {
            distancemade.setText(df.format(distancedone / 1000) + "km");
        }else
        {
            distancemade.setText(df.format(distancedone) + "metros");
        }


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        conversormeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(speedconvert == 0)
                {
                    speedconvert = 1;
                    speedconvention.setText("MI/H");
                    conversormeasure.setText("MI/H");

                }else {
                    speedconvert = 0;
                    speedconvention.setText("KM/H");
                    conversormeasure.setText("KM/H");
                }

                Toast.makeText(ctx, "Done" , Toast.LENGTH_LONG).show();
            }
        });

        latlongtoast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("latlong", endlatitude + " " + endlongitude);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(ctx, endlatitude + " " + endlongitude , Toast.LENGTH_LONG).show();
            }
        });

        toastgoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("google", "https://maps.google.com/maps?q=" + endlatitude + ',' + endlongitude);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(ctx, "Done" , Toast.LENGTH_LONG).show();
            }
        });

        toastwaze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("waze", "https://waze.com/ul?ll=" + endlatitude + ',' + endlongitude);
                clipboard.setPrimaryClip(clip);

                Toast.makeText(ctx, "Done" , Toast.LENGTH_LONG).show();
            }
        });

        cleardistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                distancedone = 0;
                distancemade.setText(0 + "");

                Toast.makeText(ctx, "Done" , Toast.LENGTH_LONG).show();
            }
        });
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
        tspeed.setText(get_final_velocity);

        if(endlatitude == null || startlatitude == null)
        {}
        else {
            location.distanceBetween(Float.valueOf(startlatitude), Float.valueOf(startlongitude), Float.valueOf(endlatitude), Float.valueOf(endlongitude), results);

            if (results.length > 0 && speed > 0) {
                distancedone = distancedone + results[0];

                if(distancedone > 1000)
                {
                    distancemade.setText(df.format((speedconvert == 0)?distancedone / 1000 + " km":distancedone * 0.6213712f + " Kmi") );
                }else
                {
                    distancemade.setText(df.format((speedconvert == 0)?distancedone+ " metros":distancedone * 0.6213712f + " mi") );
                }
                
            }
        }


    }

    @Override
    public void onPause()
    {
        final SharedPreferences.Editor edit = mPrefs.edit();

        if(speedconvert == 0)
        {}
        else
        {
            distancedone = distancedone / 0.6213712f;
        }

        edit.putFloat(PREFS_DISTANCE_STRING, distancedone);
        edit.commit();

        super.onPause();
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
      //  Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
       // Log.d("Latitude","status");
    }
}