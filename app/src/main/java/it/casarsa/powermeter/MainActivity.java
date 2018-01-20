package it.casarsa.powermeter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;


public class MainActivity extends ActionBarActivity {

    String TAG = "powerMeter";
    Intent service;
    IntentFilter screenFilter;
    MediaPlayer mp ;
    Context mContext;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (intent.hasExtra("powerData")) {
                String json = intent.getStringExtra("powerData");

                JSONObject powerDataJson = null;
                try {
                    powerDataJson = new JSONObject(json);
                    DecimalFormat decimalFormat = new DecimalFormat();
                    decimalFormat.applyLocalizedPattern("###.##0");
                    ((TextView) findViewById(R.id.textWatt)).setText(decimalFormat.format(powerDataJson.getDouble("W")));
                    ((TextView) findViewById(R.id.textThrottle)).setText(decimalFormat.format(powerDataJson.getDouble("throttle")* 100) + "%");
                    if (powerDataJson.getBoolean("alert")) {
                        try {
                            if (mp.isPlaying()) return;
                            mp.start(); // no need to call prepare(); create() does that for you

                        } catch (IllegalArgumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IllegalStateException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(context, "null data", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        private boolean screenOff;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                screenOff = true;
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                screenOff = false;
            }
            /*
            Intent i = new Intent(context, DataService.class);
            i.putExtra("screen_state", screenOff);
            context.startService(i);
            */
            Log.d(TAG, "ScreenOff:" + screenOff);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        service = new Intent(this, DataService.class);
        startService(service);
        registerReceiver(broadcastReceiver, new IntentFilter(DataService.BROADCAST_ACTION));
        mp = new MediaPlayer();
        mp = MediaPlayer.create(this,R.raw.red);

        screenFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, screenFilter);

        View main = findViewById(android.R.id.content);
        main.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

        // keep screen on while in foreground:

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        Log.d(TAG, "onPause");
//        stopService(service);
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(screenReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.d(TAG, "onResume");
//        startService(service);
        registerReceiver(broadcastReceiver, new IntentFilter(DataService.BROADCAST_ACTION));
        registerReceiver(screenReceiver, new IntentFilter(screenFilter));

    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        Log.d(TAG, "onStop");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "onBackPressed");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int SETTINGS_RESULT = 1;

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getApplicationContext(), UserSettingActivity.class);
            startActivityForResult(i, SETTINGS_RESULT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
