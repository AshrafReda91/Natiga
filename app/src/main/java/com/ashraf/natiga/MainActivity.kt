package com.ashraf.natiga

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
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

        // accordion اللوحة: ضغطة تفتحها وضغطة تقفلها
        val art = findViewById<ImageView>(R.id.art_image)
        val arrow = findViewById<TextView>(R.id.art_arrow)
        findViewById<View>(R.id.art_header).setOnClickListener {
            val open = art.visibility != View.VISIBLE
            art.visibility = if (open) View.VISIBLE else View.GONE
            arrow.text = if (open) "▴" else "▾"
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
        val ar = Prefs.arabicDigits(this)

        // المعاينة = نفس layout الـ widget
        val dm = resources.displayMetrics
        val previewW = dm.widthPixels / dm.density - 40f // padding الشاشة ٢٠dp من كل ناحية
        WidgetFill.fill(this, DateCalc.today(this), previewW, object : ViewTarget {
            override fun image(id: Int, bmp: Bitmap, description: CharSequence) {
                findViewById<ImageView>(id).apply {
                    setImageBitmap(bmp)
                    contentDescription = description
                }
            }
            override fun visible(id: Int, show: Boolean) {
                findViewById<View>(id).visibility = if (show) View.VISIBLE else View.GONE
            }
            override fun background(id: Int, res: Int) {
                findViewById<View>(id).setBackgroundResource(res)
            }
        })

        val off = Prefs.hijriOffset(this)
        val sign = if (off > 0) "+" else if (off < 0) "−" else ""
        findViewById<TextView>(R.id.offset_value).text =
            if (off == 0) "بدون تعديل" else "$sign${DateCalc.num(kotlin.math.abs(off), ar)} يوم"

        findViewById<Button>(R.id.btn_digits).text =
            if (ar) "الأرقام: ١٢٣ — اضغط للتحويل لـ 123" else "الأرقام: 123 — اضغط للتحويل لـ ١٢٣"

        DateWidgetProvider.updateAll(this)
    }
}
