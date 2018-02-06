package io.greyfox.shooteralert;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.greyfox.shooteralert.app.AppConfig;
import io.greyfox.shooteralert.app.AppController;
import io.greyfox.shooteralert.helper.SessionManager;

import static io.greyfox.shooteralert.app.AppConfig.APP_NAME;
import static io.greyfox.shooteralert.app.AppConfig.NOTIFICATION_BROADCAST_ACTION;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";
    LocalBroadcastManager mBroadcaster;
    private SessionManager session;

    @Override
    public void onCreate() {
        super.onCreate();
        mBroadcaster = LocalBroadcastManager.getInstance(this);
        session = new SessionManager(getApplicationContext());
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {

            try {
                Intent intent = new Intent();
                intent.setAction(NOTIFICATION_BROADCAST_ACTION);
                mBroadcaster.sendBroadcast(intent);

                if (AppController.getInstance().isBackground) {
                if (session.getNotificationSetting())
                    sendNotification("New Shooting 30 miles away tap for more info.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (remoteMessage.getNotification() != null) {

            Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */

    private void sendNotification(String messageBody) {

        Intent intent = new Intent(this, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(APP_NAME)
                .setContentText(messageBody)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification n;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            n = notificationBuilder.build();
        } else {
            n = notificationBuilder.getNotification();
        }

        n.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;


        notificationManager.notify(0 /* ID of notification */, n);
    }
}
