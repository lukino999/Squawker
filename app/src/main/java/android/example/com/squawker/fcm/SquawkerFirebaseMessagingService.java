package android.example.com.squawker.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import timber.log.Timber;

public class SquawkerFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "squawker";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //
        Map<String, String> data = remoteMessage.getData();

        if (data.size() > 0) {
            Timber.d("onMessageReceived: %s, %s", SquawkContract.COLUMN_DATE, data.get(SquawkContract.COLUMN_DATE));
            Timber.d("onMessageReceived: %s, %s", SquawkContract.COLUMN_AUTHOR_KEY, data.get(SquawkContract.COLUMN_AUTHOR_KEY));
            Timber.d("onMessageReceived: %s, %s", SquawkContract.COLUMN_AUTHOR, data.get(SquawkContract.COLUMN_AUTHOR));
            Timber.d("onMessageReceived: %s, %s", SquawkContract.COLUMN_MESSAGE, data.get(SquawkContract.COLUMN_MESSAGE));

            // add to database
            ContentValues values = new ContentValues();
            values.put(SquawkContract.COLUMN_DATE, data.get(SquawkContract.COLUMN_DATE));
            values.put(SquawkContract.COLUMN_AUTHOR_KEY, data.get(SquawkContract.COLUMN_AUTHOR_KEY));
            values.put(SquawkContract.COLUMN_AUTHOR, data.get(SquawkContract.COLUMN_AUTHOR));
            values.put(SquawkContract.COLUMN_MESSAGE, data.get(SquawkContract.COLUMN_MESSAGE));
            getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI, values);

            sendNotification(data);
        }
    }

    public void sendNotification(Map<String, String> data) {

        Intent intent = new Intent(this, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Create the pending intent to launch the activity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, +PendingIntent.FLAG_ONE_SHOT);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        //Get an instance of NotificationManager//
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_duck)
                        .setContentTitle(data.get(SquawkContract.COLUMN_AUTHOR))
                        .setContentText(data.get(SquawkContract.COLUMN_MESSAGE))
                        .setContentIntent(pendingIntent)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);


        // Gets an instance of the NotificationManager service//
        NotificationManager mNotificationManager =

                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // When you issue multiple notifications about the same type of event,
        // it’s best practice for your app to try to update an existing notification
        // with this new information, rather than immediately creating a new notification.
        // If you want to update this notification at a later date, you need to assign it an ID.
        // You can then use this ID whenever you issue a subsequent notification.
        // If the previous notification is still visible, the system will update this existing notification,
        // rather than create a new one. In this example, the notification’s ID is 001//


        if (mNotificationManager != null) {
            mNotificationManager.notify(1, mBuilder.build());
        }
    }
}
