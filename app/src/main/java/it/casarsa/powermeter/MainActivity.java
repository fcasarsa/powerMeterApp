package it.casarsa.powermeter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DecimalFormat;


public class MainActivity extends ActionBarActivity {

    String TAG="powerMeter";
    Intent service;
    IntentFilter screenFilter;


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            PowerData powerData = extras.getParcelable("powerData");
            TextView text = (TextView) findViewById(R.id.textWatt);
            text.setText(powerData.getWattText());
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        service= new Intent(this, DataService.class);
        startService(service);
        registerReceiver(broadcastReceiver, new IntentFilter(DataService.BROADCAST_ACTION));

        screenFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, screenFilter);

    }



    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        Log.d(TAG,"onPause");
        stopService(service);
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(screenReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.d(TAG, "onResume");
        startService(service);
        registerReceiver(broadcastReceiver, new IntentFilter(DataService.BROADCAST_ACTION));
        registerReceiver(screenReceiver, new IntentFilter(screenFilter));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
