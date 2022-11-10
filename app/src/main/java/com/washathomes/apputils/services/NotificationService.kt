package com.washathomes.apputils.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.washathomes.R
import com.washathomes.views.splash.SplashActivity
import java.util.*

class NotificationService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("received", "ahmad")
        val data = remoteMessage.data
        val title = data["title"]
        val body = data["body"]
        //        if (title.equals("Orders")){
//            SavedData.title = body;
//        }
//        SavedData.orderId = data.get("body");

        displayNotification(applicationContext, title, body)

        val intent = Intent("Push")
        this.sendBroadcast(intent)
    }

    private fun displayNotification(context: Context, title: String?, body: String?) {
        val intent = Intent(context, SplashActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_IMMUTABLE)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, "default")
                .setSmallIcon(R.drawable.blue_logo)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default",
                "Default_channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        if (notificationManager != null) {
            val id = (Date().time / 1000L % Int.MAX_VALUE).toInt()
            notificationManager.notify(id, notificationBuilder.build())
        }
    }

}
