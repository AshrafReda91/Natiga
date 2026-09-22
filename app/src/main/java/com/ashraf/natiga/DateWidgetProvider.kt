package com.ashraf.natiga

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.SizeF
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

    // لما الـ widget يكبر أو يصغر: نعيد رسم النصوص على العرض الجديد
    override fun onAppWidgetOptionsChanged(
        context: Context, manager: AppWidgetManager, id: Int, newOptions: android.os.Bundle
    ) {
        updateWidget(context, manager, id)
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
            val opts = manager.getAppWidgetOptions(id)

            // أندرويد ١٢+: الـ launcher بيدينا المقاسات الفعلية، فبنرسم نسخة لكل مقاس
            // وهو بيختار اللي على قد الـ widget بالظبط
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                @Suppress("DEPRECATION")
                val sizes = opts.getParcelableArrayList<SizeF>(AppWidgetManager.OPTION_APPWIDGET_SIZES)
                    ?.filter { it.width > 0 && it.height > 0 }?.distinct()
                if (!sizes.isNullOrEmpty()) {
                    manager.updateAppWidget(id, RemoteViews(sizes.associateWith { build(context, d, it.width) }))
                    return
                }
            }
            // أقدم من كده: MAX_WIDTH أقرب لعرض الوضع الرأسي على أغلب الـ launchers
            val w = maxOf(
                opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0),
                opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0)
            ).let { if (it > 0) it.toFloat() else 320f }
            manager.updateAppWidget(id, build(context, d, w))
        }

        private fun build(context: Context, d: TriDate, widthDp: Float): RemoteViews {
            val v = RemoteViews(context.packageName, R.layout.widget_date)
            WidgetFill.fill(context, d, widthDp, object : ViewTarget {
                override fun image(id: Int, bmp: Bitmap, description: CharSequence) {
                    v.setImageViewBitmap(id, bmp)
                    v.setContentDescription(id, description)
                }
                override fun visible(id: Int, show: Boolean) =
                    v.setViewVisibility(id, if (show) View.VISIBLE else View.GONE)
                override fun background(id: Int, res: Int) =
                    v.setInt(id, "setBackgroundResource", res)
            })

            val open = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            v.setOnClickPendingIntent(R.id.root, open)
            return v
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
