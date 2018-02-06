package io.greyfox.shooteralert;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import io.greyfox.shooteralert.app.AppController;
import io.greyfox.shooteralert.helper.SessionManager;

import static io.greyfox.shooteralert.app.AppConfig.NOTIFICATION_BROADCAST_ACTION;

/**
 * Created by volkov on 12/22/16.
 */

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager mBroadcaster = LocalBroadcastManager.getInstance(this);;
        Intent intent = new Intent();
        intent.setAction(NOTIFICATION_BROADCAST_ACTION);
        mBroadcaster.sendBroadcast(intent);

        finish();
    }
}
