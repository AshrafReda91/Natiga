package com.ashraf.natiga

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_minus).setOnClickListener { changeOffset(-1) }
        findViewById<Button>(R.id.btn_plus).setOnClickListener { changeOffset(1) }
        findViewById<Button>(R.id.btn_digits).setOnClickListener {
            Prefs.get(this).edit()
                .putBoolean(Prefs.ARABIC_DIGITS, !Prefs.arabicDigits(this)).apply()
            refresh()
        }

        val pin = findViewById<Button>(R.id.btn_pin)
        val manager = AppWidgetManager.getInstance(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.isRequestPinAppWidgetSupported) {
            pin.setOnClickListener {
                manager.requestPinAppWidget(
                    ComponentName(this, DateWidgetProvider::class.java), null, null
                )
            }
        } else {
            pin.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun changeOffset(delta: Int) {
        val v = (Prefs.hijriOffset(this) + delta).coerceIn(-2, 2)
        Prefs.get(this).edit().putInt(Prefs.HIJRI_OFFSET, v).apply()
        refresh()
    }

    private fun refresh() {
        val d = DateCalc.today(this)
        val ar = Prefs.arabicDigits(this)

        // المعاينة = نفس layout الـ widget
        findViewById<TextView>(R.id.month_year).text = d.monthYear
        findViewById<TextView>(R.id.day_num).text = d.dayNum
        findViewById<TextView>(R.id.day_num_friday).text = d.dayNum
        findViewById<TextView>(R.id.day_num).visibility = if (d.isFriday) View.GONE else View.VISIBLE
        findViewById<TextView>(R.id.day_num_friday).visibility = if (d.isFriday) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.day_name).text = d.dayName
        findViewById<TextView>(R.id.hijri).text = d.hijri
        findViewById<TextView>(R.id.coptic).text = d.coptic

        val off = Prefs.hijriOffset(this)
        val sign = if (off > 0) "+" else if (off < 0) "−" else ""
        findViewById<TextView>(R.id.offset_value).text =
            if (off == 0) "بدون تعديل" else "$sign${DateCalc.num(kotlin.math.abs(off), ar)} يوم"

        findViewById<Button>(R.id.btn_digits).text =
            if (ar) "الأرقام: ١٢٣ — اضغط للتحويل لـ 123" else "الأرقام: 123 — اضغط للتحويل لـ ١٢٣"

        DateWidgetProvider.updateAll(this)
    }
}
