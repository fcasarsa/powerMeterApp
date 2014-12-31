package it.casarsa.powermeter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


public class DataService extends Service {

    static String TAG ="SERVICE";
    int mNotificationId = 001;
    NotificationManager mNotifyMgr;
    NotificationCompat.Builder mBuilder;
    public static String BROADCAST_ACTION = "it.casarsa.powerMeter";

    BroadcastReceiver mReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        new backGroundReceiver().execute();
        // build notification
         mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.lightning_icon)
                        .setContentTitle("Power")
                        .setContentText("initializing");

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());


        return Service.START_STICKY;
    }


    public class backGroundReceiver extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            Log.d(TAG, "Background service started");
            while (true) {
                try {
                    // data initialization
                    int i = 0;
                    while (true) {
                        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        if (mWifi.isConnected()) {
                            String updatesUrl = "http://192.168.99.10:3000";
                            String request = getDataFromWebService(updatesUrl);
                            publishProgress(request);
                            SystemClock.sleep(5000);
                        } else {
                            Log.d(TAG, "Wifi is off");
                            publishProgress();
                            SystemClock.sleep(5000);
                        }
                    }
                } catch (Exception e) {
                    SystemClock.sleep(10000);
                    publishProgress();
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... item) {
            // ((ArrayAdapter<String>) getListAdapter()).add(item[0]);
            Intent intent = new Intent(BROADCAST_ACTION);

            if (item.length > 0) {
                Log.d(TAG, "Background service results" + item[0]);
                PowerData powerData = new PowerData(item[0]);
                intent.putExtra("powerData", powerData);
                mBuilder.setContentText(powerData.getWattText()+"W  " + powerData.getPowerFactorText());
                // Because the ID remains unchanged, the existing notification is
                // updated.
                mNotifyMgr.notify(
                        mNotificationId,
                        mBuilder.build());
            } else {
                // no data means error so remove notification
                mNotifyMgr.cancel(mNotificationId);
            }



            sendBroadcast(intent);


        }


        @Override
        protected void onPostExecute(Void unused) {

        }

    }

    private static String getDataFromWebService(String url) throws IOException,
            MalformedURLException, JSONException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url)
                .openConnection();

        InputStream in = conn.getInputStream();

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(
                    new DoneHandlerInputStream(in)));
            for (String line = r.readLine(); line != null; line = r.readLine()) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            in.close();
        }
    }

    public String downloadData() {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://192.168.99.10:3000");
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } else {
                Log.e(TAG, "Failed to download file");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "on Create");
    };

    @Override
    public void onDestroy() {
        Log.d(TAG, "on Destroy");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}