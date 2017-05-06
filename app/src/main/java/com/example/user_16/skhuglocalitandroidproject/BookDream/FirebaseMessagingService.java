package com.example.user_16.skhuglocalitandroidproject.BookDream;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.user_16.skhuglocalitandroidproject.R;
import com.google.firebase.messaging.RemoteMessage;

import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";

    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //추가한것
        String state = remoteMessage.getData().get("state");
        if(state==null)
            return;
        if(state.contains("giveMatch")) {

            Intent intent = new Intent(this, MatchActivity.class);
            intent.putExtra("state", state);
            intent.putExtra("title", remoteMessage.getData().get("title"));
            intent.putExtra("giveUser", remoteMessage.getData().get("giveUser"));
            intent.putExtra("requestUser", remoteMessage.getData().get("requestUser"));
            intent.putExtra("message", remoteMessage.getData().get("message"));
            Log.d("왔썹", remoteMessage.getData().get("requestUser"));
            intent.putExtra("date", remoteMessage.getData().get("date"));
            intent.putExtra("time", remoteMessage.getData().get("time"));
            intent.putExtra("where", remoteMessage.getData().get("where"));
            intent.putExtra("content", remoteMessage.getData().get("content"));
            intent.putExtra("phone", remoteMessage.getData().get("phone"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            sendNotification(intent);
        }
        else if(state.equals("requestMatch")) {

            Intent intent = new Intent(this, GiveMatchMessageActivity.class);
            intent.putExtra("state", state);
            intent.putExtra("title", remoteMessage.getData().get("title"));
            intent.putExtra("giveUser", remoteMessage.getData().get("giveUser"));
            intent.putExtra("requestUser", remoteMessage.getData().get("requestUser"));
            intent.putExtra("message", remoteMessage.getData().get("message"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            sendNotification(intent);
        }
        else if(state.equals("accept") || state.equals("cancel")) {
            Intent intent = new Intent();
            intent.putExtra("state", state);
            intent.putExtra("message", remoteMessage.getData().get("message"));
            sendNotification(intent);
        }
    }

    private void sendNotification(Intent messageBody) {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, messageBody, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.appicon)
                .setContentTitle("Book:Dream 메시지!")
                .setContentText("아래로 밀어 확인해주세요! " + messageBody.getStringExtra("message"))
                .setAutoCancel(true)
                .setSound(defaultSoundUri);

        SharedPreferences ring_pref = getSharedPreferences("init_Info",MODE_PRIVATE);
        if(!ring_pref.getString("uri","").equals("")) {
            notificationBuilder.setSound(Uri.parse(ring_pref.getString("uri","")));
        } else {
            notificationBuilder.setSound(defaultSoundUri);
        }
        if(messageBody.getStringExtra("state").contains("giveMatch") ||
                messageBody.getStringExtra("state").equals("requestMatch"))
            notificationBuilder.setContentIntent(pendingIntent);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle(notificationBuilder);
        bigTextStyle.setBigContentTitle("Book:Dream 메시지!");
        bigTextStyle.bigText(messageBody.getStringExtra("message"));
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1 /* ID of notification */, notificationBuilder.build());
    }

}
