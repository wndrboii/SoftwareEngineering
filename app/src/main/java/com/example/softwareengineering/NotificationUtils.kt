import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.softwareengineering.R
import com.example.softwareengineering.model.DailyNutrition
import java.text.SimpleDateFormat
import java.util.*

class NotificationUtils {
    private val CHANNEL_ID = "YourChannelId"

    // Method to schedule the notification
    @SuppressLint("UnspecifiedImmutableFlag", "ScheduleExactAlarm")
    fun scheduleNotification(context: Context, dailyNutrition: DailyNutrition) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val targetTime = dateFormat.parse(dailyNutrition.time)

        val calendar = Calendar.getInstance()
        if (targetTime != null) {
            calendar.time = targetTime
        }

        val requestCode = 0
        val notificationIntent = Intent(context, NotificationReceiver::class.java)
        notificationIntent.putExtra("NOTIFICATION_MESSAGE", "Your notification message")
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            notificationIntent,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    // Method to create and show the notification
    fun showNotification(context: Context, message: String) {
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Notification Title")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_posilki)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel for devices running Android 8.0 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Your Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("NOTIFICATION_MESSAGE")
        // Show the notification
        if (message != null) {
            NotificationUtils().showNotification(context, message)
        }
    }
}