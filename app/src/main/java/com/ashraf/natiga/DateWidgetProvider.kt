package com.ashraf.natiga

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import java.util.Calendar

class DateWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) updateWidget(context, manager, id)
        scheduleMidnight(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_MIDNIGHT,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED -> updateAll(context)
        }
    }

    override fun onDisabled(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(midnightIntent(context))
    }

    companion object {
        private const val ACTION_MIDNIGHT = "com.ashraf.natiga.MIDNIGHT"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, DateWidgetProvider::class.java))
            for (id in ids) updateWidget(context, manager, id)
            if (ids.isNotEmpty()) scheduleMidnight(context)
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val d = DateCalc.today(context)
            val v = RemoteViews(context.packageName, R.layout.widget_date)
            v.setTextViewText(R.id.month_year, d.monthYear)
            v.setTextViewText(R.id.day_num, d.dayNum)
            v.setTextViewText(R.id.day_num_friday, d.dayNum)
            v.setViewVisibility(R.id.day_num, if (d.isFriday) View.GONE else View.VISIBLE)
            v.setViewVisibility(R.id.day_num_friday, if (d.isFriday) View.VISIBLE else View.GONE)
            v.setTextViewText(R.id.day_name, d.dayName)
            v.setTextViewText(R.id.hijri, d.hijri)
            v.setTextViewText(R.id.coptic, d.coptic)

            val open = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            v.setOnClickPendingIntent(R.id.root, open)
            manager.updateAppWidget(id, v)
        }

        // تحديث تلقائي بعد نص الليل بخمس ثواني
        private fun scheduleMidnight(context: Context) {
            val next = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 5)
                set(Calendar.MILLISECOND, 0)
            }
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.setAndAllowWhileIdle(AlarmManager.RTC, next.timeInMillis, midnightIntent(context))
        }

        private fun midnightIntent(context: Context): PendingIntent {
            val i = Intent(context, DateWidgetProvider::class.java).setAction(ACTION_MIDNIGHT)
            return PendingIntent.getBroadcast(
                context, 1, i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
