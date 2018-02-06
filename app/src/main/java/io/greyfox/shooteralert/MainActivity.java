package io.greyfox.shooteralert;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import io.greyfox.shooteralert.app.AppConfig;
import io.greyfox.shooteralert.app.AppController;
import io.greyfox.shooteralert.app.ShootInfo;
import io.greyfox.shooteralert.helper.NetworkManager;

import static io.greyfox.shooteralert.app.AppConfig.NOTIFICATION_BROADCAST_ACTION;

public class MainActivity extends TabActivity {

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getMetadata();
        }
    };

    private TabHost tabHost;
    private AVLoadingIndicatorView avi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        avi = (AVLoadingIndicatorView) findViewById(R.id.avi);

        getMetadata();
        //loadCSV();

        tabHost = (TabHost)findViewById(android.R.id.tabhost);

        TabHost.TabSpec tab1 = tabHost.newTabSpec("First Tab");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("Second Tab");
        TabHost.TabSpec tab3 = tabHost.newTabSpec("Third tab");

        // Set the Tab name and Activity
        // that will be opened when particular Tab will be selected
        tab1.setIndicator("Stats");
        tab1.setContent(new Intent(this, StatsActivity.class));

        tab2.setIndicator("Map");
        tab2.setContent(new Intent(this, MapsActivity.class));

        tab3.setIndicator("Settings");
        tab3.setContent(new Intent(this, SettingActivity.class));

        /** Add the tabs  to the TabHost to display. */
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);

        tabHost.setCurrentTab(1);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                // display the name of the tab whenever a tab is changed
                for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
                    //tabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#FF0000")); // unselected
                    TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title); //Unselected Tabs
                    tv.setTextColor(Color.parseColor("#eaeaea"));
                }
                //tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab()).setBackgroundColor(Color.parseColor("#0000FF")); // selected
                TextView tv = (TextView) tabHost.getCurrentTabView().findViewById(android.R.id.title); //for Selected Tab
                tv.setTextColor(Color.parseColor("#ffffff"));

            }
        });

        for(int i = 0; i < tabHost.getTabWidget().getChildCount(); i ++)
        {
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextSize(22);
            tv.setAllCaps(false);
            if (i == 1)
                tv.setTextColor(Color.parseColor("#ffffff"));
            else
                tv.setTextColor(Color.parseColor("#eaeaea"));
        }
    }

    private void loadCSV() {
        try {
            InputStreamReader csvStreamReader = new InputStreamReader(
                    MainActivity.this.getAssets().open(
                            "shooting_tb.csv"));

            String next[] = {};
            CSVReader reader = new CSVReader(csvStreamReader);
            for (;;) {
                next = reader.readNext();
                if (next != null) {
                    if (next[1].contains("2017")) {
                        ShootInfo newItem = new ShootInfo();
                        newItem.id = Integer.parseInt(next[0]);
                        newItem.incident_date = next[1];
                        newItem.state = next[2];
                        newItem.city = next[3];
                        newItem.address = next[4];
                        newItem.killed = Integer.parseInt(next[5]);
                        newItem.injured = Integer.parseInt(next[6]);
                        newItem.url1 = next[7];
                        newItem.url2 = next[8];
                        newItem.latitude = Double.parseDouble(next[9]);
                        newItem.longitude = Double.parseDouble(next[10]);

                        AppConfig.addShoot(newItem);
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        if (isApplicationSentToBackground(this)){
            AppController.getInstance().isBackground = true;
        }
        super.onPause();
    }

    public boolean isApplicationSentToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (!tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        AppController.getInstance().isBackground = false;

        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver((mBroadcastReceiver), filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    private void getMetadata() {

        AppConfig.removeData();
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);

        Map<String, String> params = new HashMap<String, String>();
        params.put("year", String.valueOf(year));

        avi.show();

        NetworkManager networkManager = new NetworkManager(getApplicationContext(), Request.Method.GET, "metadata.php", params);
        networkManager.setNetworkListener(new NetworkManager.NetworkListener(){

            @Override
            public void onResult(JSONObject result) {
                avi.hide();
                try {
                    JSONArray array = result.getJSONArray("shoot");
                    for (int i = 0; i < array.length(); i ++) {
                        JSONObject obj = array.getJSONObject(i);
                        ShootInfo newItem = new ShootInfo();
                        newItem.id = obj.getInt("id");
                        newItem.incident_date = obj.getString("incident_date");
                        newItem.state = obj.getString("state");
                        newItem.city = obj.getString("city");
                        newItem.address = obj.getString("address");
                        newItem.killed = obj.getInt("killed");
                        newItem.injured = obj.getInt("injured");
                        newItem.url1 = obj.getString("url1");
                        newItem.url2 = obj.getString("url2");
                        newItem.latitude = obj.getDouble("latitude");
                        newItem.longitude = obj.getDouble("longitude");

                        AppConfig.addShoot(newItem);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(String error) {
                avi.hide();
            }
        });
    }
}
