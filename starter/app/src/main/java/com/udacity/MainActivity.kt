package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.Manifest
import android.app.NotificationChannel
import android.graphics.Color
import android.media.RingtoneManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Environment
import androidx.core.app.TaskStackBuilder


private const val CHANNEL_ID = "channelId"

class MainActivity : AppCompatActivity() {

    private var downloadId: Long = 0
    private lateinit var url: URL
    private var status = "Fail"

    private lateinit var downloadManager: DownloadManager
    private lateinit var channelId: String
    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        channelId = getString(R.string.loadapp_notification_channel_id)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        radio_group.setOnCheckedChangeListener { radioGroup, i ->
            url = when(i) {
                R.id.radio_retrofit -> URL.RETROFIT
                R.id.radio_glide -> URL.GLIDE
                R.id.radio_loadapp -> URL.LOADAPP
                else -> URL.RETROFIT
            }
        }

        custom_button.setOnClickListener {
            if (this::url.isInitialized){

                if(getConnectionType(applicationContext) > 0){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        custom_button.buttonState = ButtonState.Loading
                        download()
                    } else {
                        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            PermissionInfo.PROTECTION_DANGEROUS)
                    }
                } else {
                    error()
                    Toast.makeText(this, getString(R.string.no_network_toast), Toast.LENGTH_SHORT).show()
                }
            } else {
                error()
                Toast.makeText(this, getString(R.string.select_option_toast), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId == id) {
                status = "Success"
                custom_button.buttonState = ButtonState.Completed
                sendNotification()
            }
        }
    }

    private fun error() {
        custom_button.buttonState = ButtonState.Completed
    }

    private fun download() {
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val request =
            DownloadManager.Request(Uri.parse(url.path))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"/selectedDownload.zip" )

        downloadId = downloadManager.enqueue(request)

        val cursor = downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor.moveToFirst()) {
            when(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) {
                DownloadManager.STATUS_FAILED -> {
                    status = "Failed"
                    custom_button.buttonState = ButtonState.Completed
                }
                DownloadManager.STATUS_SUCCESSFUL -> {
                    status = "Success"
                }
            }
        }
    }


    // Modified and adapted from previous course - android-kotlin-notifications.app - EggTimer
    private fun sendNotification(){
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra("fileName", url.title)
            putExtra("status", status)
        }

        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        } as PendingIntent


        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val action = NotificationCompat.Action(R.drawable.ic_cloud_download, getString(R.string.notification_button), pendingIntent)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_cloud_download)
            .setContentTitle(url.title)
            .setContentText(url.message)
            .addAction(action)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)


        notificationManager.notify(0, notificationBuilder.build())


    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,  "LoadApp Channel", NotificationManager.IMPORTANCE_HIGH).apply {
                description = getString(R.string.download_complete)
                enableLights(true)
                lightColor = Color.WHITE
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    /*
    This function was shared from StackOverflow
     */
    private fun getConnectionType(context: Context): Int {
        var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 2
                    } else if (hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 1
                    } else if (hasTransport(NetworkCapabilities.TRANSPORT_VPN)){
                        result = 3
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        result = 2
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        result = 1
                    } else if(type == ConnectivityManager.TYPE_VPN) {
                        result = 3
                    }
                }
            }
        }
        return result
    }
}


