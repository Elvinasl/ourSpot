package softmates.ourspot;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.renderscript.Allocation;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.util.DebugUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by elvinaslukasevicius on 20/06/2017.
 */

public class NotificationUtils {
    public static final int NOTIFICATION_ID = 1;
    public static final String ACTION_1 = "action_1";
    public static final String ACTION_2 = "action_2";
    public static final String ACTION_3 = "action_3";

    public static void displayNotification(Context context, String latLocation, String longLocation) {

        Intent action1Intent = new Intent(context, NotificationActionService.class)
                .setAction(latLocation + ":" + longLocation + ":" + ACTION_1);
        Intent action2Intent = new Intent(context, NotificationActionService.class)
                .setAction(latLocation + ":" + longLocation + ":" + ACTION_2);
        Intent action3Intent = new Intent(context, NotificationActionService.class)
                .setAction(latLocation + ":" + longLocation + ":" + ACTION_3);

        PendingIntent action1PendingIntent = PendingIntent.getService(context, 0,
                action1Intent, PendingIntent.FLAG_ONE_SHOT);
        PendingIntent action2PendingIntent = PendingIntent.getService(context, 0,
                action2Intent, PendingIntent.FLAG_ONE_SHOT);
        PendingIntent action3PendingIntent = PendingIntent.getService(context, 0,
                action3Intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.p)
                        .setContentTitle("ourSpot")
                        .setPriority(Notification.PRIORITY_MAX)
                        .setContentText("Is there any parking space nearby?")
                        .addAction(new NotificationCompat.Action(0,
                                "Yes", action1PendingIntent))
                        .addAction(new NotificationCompat.Action(0,
                                "No", action2PendingIntent))
                        .addAction(new NotificationCompat.Action(0,
                                "Blacklist", action3PendingIntent));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }



    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            String action = intent.getAction();
            String[] splitLocation = action.split(":");
            if (ACTION_1.equals(splitLocation[2])) {
                // TODO: handle action 1.
                try {
                    new connectionMng().sendLocation(splitLocation[0], splitLocation[1], "Y", ourSpot.id(getApplicationContext()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
            }
            if (ACTION_2.equals(splitLocation[2])) {
                // TODO: handle action 2.
                try {
                    new connectionMng().sendLocation(splitLocation[0], splitLocation[1], "N", ourSpot.id(getApplicationContext()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
            }
            if (ACTION_3.equals(splitLocation[2])) {
                // TODO: handle action 3.
                new connectionMng().sendBlacklist(splitLocation[0], splitLocation[1], ourSpot.id(getApplicationContext()));
                NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
                Log.d("action","3");
            }
        }
    }
}