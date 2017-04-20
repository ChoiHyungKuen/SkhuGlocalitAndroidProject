package com.example.user_16.skhuglocalitandroidproject.BookDream;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.example.user_16.skhuglocalitandroidproject.R;
import com.google.firebase.messaging.RemoteMessage;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        //추가한것
        Intent intent = new Intent(this, MatchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("title", remoteMessage.getData().get("title"));
        intent.putExtra("giveUser", remoteMessage.getData().get("giveUser"));
        intent.putExtra("requestUser", remoteMessage.getData().get("requestUser"));
        intent.putExtra("date", remoteMessage.getData().get("date"));
        intent.putExtra("time", remoteMessage.getData().get("time"));
        intent.putExtra("where", remoteMessage.getData().get("where"));
        intent.putExtra("content", remoteMessage.getData().get("content"));
        intent.putExtra("phone", remoteMessage.getData().get("phone"));
        sendNotification(intent);
    }

    private void sendNotification(Intent messageBody) {
    /*        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 *//* Request code *//*, intent,
                PendingIntent.FLAG_ONE_SHOT);*/

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, messageBody, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Book:Dream 메시지!")
                .setContentText("Book:Dream이 왔습니다!")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}
