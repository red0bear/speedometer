package com.gladic.speedmeasures;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public  class MainActivity extends AppCompatActivity {

    private servicespeedmeasures mService ;
    private boolean mBound = false;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mService = ((servicespeedmeasures.LocalBinder)service).getService();
            if(!enable_foreground) {
                mService.settextView(tspeed, speedconvention, distancemade);
                mService.setdistancedone(distancedone);
                mBound = true;
            }else
            {
                mService.foreground_enable();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    private static final String PREFS_NAME = "com.gladic.speedmeasures";
    private static final String PREFS_DISTANCE_STRING = "distancestring";
    private static final String PREFS_FOREGROUND_STRING ="foregroundenabled";

    private String measureunit ="";

    private TextView tspeed;
    private TextView speedconvention;
    private TextView distancemade;

    private int speedconvert = 0;

    private Button conversormeasure;
    private Button latlongtoast;
    private Button toastgoogle;
    private Button toastwaze;

    private Button cleardistance;

    private Button foreground;

    private Context ctx;

    private float[] results = {0, 0, 0, 0};;
    private float distancedone=0;

    private boolean enable_foreground = false;

    DecimalFormat df = new DecimalFormat();

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ctx = getApplicationContext();

        mPrefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        distancedone = mPrefs.getFloat(PREFS_DISTANCE_STRING,0);
        enable_foreground = mPrefs.getBoolean(PREFS_FOREGROUND_STRING,false);

        tspeed = (TextView) findViewById(R.id.SPEED);
        speedconvention = (TextView) findViewById(R.id.speedconvention);
        distancemade = (TextView) findViewById(R.id.distancemade);

        conversormeasure = (Button) findViewById(R.id.speedconversor);
        latlongtoast = (Button) findViewById(R.id.toastlatlong);
        toastgoogle = (Button) findViewById(R.id.toastgoogle);
        toastwaze = (Button) findViewById(R.id.toastwaze);

        foreground = (Button) findViewById(R.id.foreground);

        cleardistance = (Button) findViewById(R.id.cleardistance);

        df.setMaximumFractionDigits(3);

        if(distancedone > 1000)
        {
            if(speedconvert == 0)
                measureunit = "km";
            else
                measureunit = "mi";

            if(!enable_foreground)
                distancemade.setText(df.format((speedconvert == 0)?distancedone / 1000 :distancedone * 0.6213712f) + measureunit);
        }else
        {
            if(speedconvert == 0)
                measureunit = "m";
            else
                measureunit = "mi";

            if(!enable_foreground)
                distancemade.setText(String.valueOf((speedconvert == 0)?distancedone:distancedone * 0.6213712f) + measureunit);
        }


        foreground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enable_foreground = !enable_foreground;
                Toast.makeText(ctx, "Done" , Toast.LENGTH_LONG).show();
            }
        });

        conversormeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(speedconvert == 0)
                {
                    mService.setspeedconversor(1);
                    speedconvert=1;
                    speedconvention.setText("MI/H");
                    conversormeasure.setText("MI/H");

                }else {
                    speedconvert = 0 ;
                    mService.setspeedconversor(0);
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
                ClipData clip = ClipData.newPlainText("latlong", mService.getlatitude() + " " + mService.getlongitude());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(ctx, mService.getlatitude() + " " + mService.getlongitude() , Toast.LENGTH_LONG).show();
            }
        });

        toastgoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("google", "https://maps.google.com/maps?q=" + mService.getlatitude() + ',' + mService.getlongitude());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(ctx, "Done" , Toast.LENGTH_LONG).show();
            }
        });

        toastwaze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("waze", "https://waze.com/ul?ll=" + mService.getlatitude() + ',' + mService.getlongitude());
                clipboard.setPrimaryClip(clip);

                Toast.makeText(ctx, "Done" , Toast.LENGTH_LONG).show();
            }
        });

        cleardistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                  distancedone = 0;
                //  distancemade.setText(0 + "");
                if (mBound) {

                    mService.cleantotaldistance();

                    Toast.makeText(ctx, "Done", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, servicespeedmeasures.class);

        if(enable_foreground)
        {
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(this, 0, intent,
                            PendingIntent.FLAG_IMMUTABLE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Notification notification =
                        new Notification.Builder(this, "CHANNEL_DEFAULT_IMPORTANCE")
                                .setContentTitle(getText(R.string.notification_title))
                                .setContentText(getText(R.string.notification_message))
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setContentIntent(pendingIntent)
                                .setTicker(getText(R.string.ticker_text))
                                .build();
            }

            // Notification ID cannot be 0.
            this.startForegroundService(intent);
        }
        else
        {
            this.bindService(intent, connection, Context.BIND_AUTO_CREATE);
            mBound = true;
            startService(intent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(!enable_foreground)
        {
            unbindService(connection);
            mBound = false;
            stopService(new Intent(this,servicespeedmeasures.class));
        }

    }

    @Override
    public void onPause()
    {
        final SharedPreferences.Editor edit = mPrefs.edit();

        distancedone = mService.getdistancedone();
        //Log.d("bound", "distance" + distancedone);

        if(speedconvert == 0)
        {}
        else
        {
            distancedone = distancedone / 0.6213712f;
        }

        edit.putFloat(PREFS_DISTANCE_STRING, distancedone);
        edit.putBoolean(PREFS_FOREGROUND_STRING,enable_foreground);
        edit.commit();

        super.onPause();
    }
}