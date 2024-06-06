package com.example.softwareengineering
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import model.DailyNutrition
import model.Posilki
import java.text.SimpleDateFormat
import java.util.*



class NotificationUtils {
    private val CHANNEL_ID = "YourChannelId"

    // Method to schedule the notification
    @SuppressLint("UnspecifiedImmutableFlag")
    fun scheduleNotification(context: Context, dailyNutrition: DailyNutrition) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val targetTime = dateFormat.parse(dailyNutrition.time)

        val calendar = Calendar.getInstance()
        if (targetTime != null) {
            calendar.time = targetTime
        }

        // Adjust `dailyNutrition.day` according to `Calendar`'s day constants
        val targetDayOfWeek =
            if (dailyNutrition.day == 7) Calendar.SUNDAY else dailyNutrition.day + 1

        // Set the day of the week
        calendar.set(Calendar.DAY_OF_WEEK, targetDayOfWeek)

        // Ensure that we are setting the alarm in the future, not the past
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
        }

        val database = FirebaseDatabase.getInstance()
        val posilkiRef = database.getReference("dishes")
        val posilekId = dailyNutrition.posilekId
        posilkiRef.child(posilekId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dish = snapshot.getValue(Posilki::class.java)
                if (dish != null) {
                    val requestCode = dailyNutrition.id?.toIntOrNull() ?: 0
                    val dayInPolish = getDayInPolish(dailyNutrition.day)
                    val notificationIntent = Intent(context, NotificationReceiver::class.java)
                    notificationIntent.putExtra(
                        "NOTIFICATION_MESSAGE",
                        "$dayInPolish o ${dailyNutrition.time} jest ${dish.name} do zjedzenia!"
                    )

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

                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                        pendingIntent
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })
    }

    // Method to create and show the notification
    fun showNotification(context: Context, message: String) {
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Przypomninie!")
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
    fun getDayInPolish(day: Int): String {
        return when (day) {
            1 -> "W poniedziałek"
            2 -> "We wtorek"
            3 -> "W środę"
            4 -> "W czwartek"
            5 -> "W piątek"
            6 -> "W sobotę"
            7 -> "W niedzielę"
            else -> "Nieznany dzień"
        }
    }
}