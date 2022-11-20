package com.msd.appwidget.ip

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

class AppWidgetAlarm(private val context: Context) {

    fun startAlarm() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MILLISECOND, INTERVAL_IN_MILLIS.toInt())
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC, calendar.timeInMillis, INTERVAL_IN_MILLIS, getPendingSelfIntent(context, INTENT_ACTION_AUTO_UPDATE) )
    }

    fun stopAlarm() {
        val pendingIntent = PendingIntent.getBroadcast(context, ALARM_ID, Intent(INTENT_ACTION_AUTO_UPDATE), PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}