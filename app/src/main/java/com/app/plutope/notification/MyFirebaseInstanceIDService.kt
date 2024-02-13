package com.app.plutope.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.app.plutope.R
import com.app.plutope.ui.base.BaseActivity
import com.app.plutope.utils.extras.PreferenceHelper
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class MyFirebaseInstanceIDService : FirebaseMessagingService() {
    private var classTag: String = javaClass.name
    private val requestCode = 1001
    private val channelID = "PLUTO_PE_CHANNEL_ID"
    private var currentNotificationId = 0
    private var notificationGroupKey = "PLUTO_PE_NOTIFICATION_GROUP_KEY"
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        val notificationData = remoteMessage.notification

        val title = notificationData?.title
        val message = notificationData?.body
        createNotification(title!!, message!!)

    }


    private fun createNotification(
        title: String,
        message: String
    ) {
        val notificationId = ++currentNotificationId
        val intent = Intent(this, BaseActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        FirebaseAnalytics.getInstance(this).logEvent("Notification_Event") {
            param("Title", title)
            param("Message", message)

        }

        /**
         *  @author - Pravin patel
         * Temporary not used pendingIntent it is managed by notification type witch is not implemented yet
         * if you need to redirect when click notification in app just un-comment this code -> ( //.setContentIntent(pendingIntent))
         **/

        val pendingIntent =
            if (PreferenceHelper.getInstance().menomonicWallet != "") {
                NavDeepLinkBuilder(applicationContext).setComponentName(BaseActivity::class.java)
                    .setGraph(R.navigation.nav_graph)
                    .setDestination(R.id.dashboard)
                    .setArguments(bundleOf(Pair("id", "")))
                    .createPendingIntent()

            } else {
                PendingIntent.getActivity(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }


        val mBuilder = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.drawable.img_logo_circle)
            .setColor(Color.TRANSPARENT)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(notificationGroupKey)


        /*   if (currentNotificationId == 1) {
               mBuilder.setGroupSummary(true)
           } else {
               val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
               notificationManager.notify(notificationGroupKey, currentNotificationId - 1, mBuilder.build())
           }*/



        mBuilder.color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
        val mNotificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(
            channelID,
            resources.getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.TRANSPARENT
        notificationChannel.enableVibration(true)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationChannel.canShowBadge()


        // notificationChannel.vibrationPattern = longArrayOf(100 , 200 , 300 , 400 , 500 , 400 , 300 , 200 , 400)
        mBuilder.setChannelId(channelID)
        mNotificationManager.createNotificationChannel(notificationChannel)
        mNotificationManager.notify(notificationId, mBuilder.build())
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        scope.launch {
            val pref = PreferenceHelper.getInstance()
            pref.firebaseToken = p0
            updateToken(p0)
        }
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }


    private fun updateToken(token: String) {
        /* val apiHelper = RetrofitBuilder.makeRetrofitService(this)
         val call = apiHelper.notificationToken(token)

         call.enqueue(object : Callback<ResponseBody> {
             override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                 t.printStackTrace()
             }

             override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                 if (!response.isSuccessful) {
                     return
                 }
             }
         })*/
    }

    /*  private fun sendMessage(notification_type: String, object_id: String) {
          val broadcaster = LocalBroadcastManager.getInstance(baseContext)
          val intent = Intent(getNotificationType)
          intent.putExtra(key_notification_type, notification_type)
          intent.putExtra(key_notification_get_id, object_id)
          broadcaster.sendBroadcast(intent)
      }*/

}
