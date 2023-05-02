package com.example.wm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkerParameters
import androidx.work.Worker

class MyWorker(context: Context, workerParameters: WorkerParameters):
    Worker(context, workerParameters){

    companion object{
        const val CHANNEL_ID = "channel" +
                "" +"_id"
        const val NOTIFICATION_ID = 1
    }

    override fun doWork(): Result {
        Log.d("do work success", "doWork: Success function called")
        showNotification()
        return Result.success()
    }

    private fun showNotification(){
        val intent = Intent(applicationContext,MainActivity::class.java)
           .apply { flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK}

        val pendingIntent = PendingIntent.getActivity(applicationContext,
           0, intent, PendingIntent.FLAG_IMMUTABLE)
// System.currentTimeMillis().toInt()

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(androidx.core.R.drawable.notification_icon_background) //ic_launcher_foreground
            .setContentTitle("Scheduled for 1 hour")
            .setContentText("Task is running")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOngoing(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelName = "Channel Name"
            val channelDescription = "Channel Description"
            val channelImportance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(CHANNEL_ID, channelName, channelImportance).apply {
                description= channelDescription }

            val notificationManager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }

        with(NotificationManagerCompat.from(applicationContext)){
            notify(NOTIFICATION_ID,notification.build())
        }

    }
}

