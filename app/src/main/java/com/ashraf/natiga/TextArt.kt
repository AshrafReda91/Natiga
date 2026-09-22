package com.ashraf.natiga

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.TextUtils
import android.util.TypedValue
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * الـ widget مابيقبلش خطوط مخصصة (الـ launcher بيرجّعها للخط الافتراضي)،
 * فبنرسم كل نص كصورة بالخط اللي عايزينه. النص بيترسم أبيض، والـ ImageView
 * بيلوّنه بـ android:tint من الـ colors — فالوضع الليلي بيفضل شغال لوحده.
 */
object TextArt {
    enum class Font(val res: Int, val bold: Boolean) {
        REGULAR(R.font.tajawal_regular, false),
        BOLD(R.font.tajawal_bold, true),
        LATIN_BOLD(R.font.carlito_bold, true)
    }

    private val cache = HashMap<Font, Typeface>()

    private fun typeface(c: Context, f: Font): Typeface = cache.getOrPut(f) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) c.resources.getFont(f.res)
        else if (f.bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }

    fun dp(c: Context, v: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.resources.displayMetrics)

    /**
     * @param maxWidthDp أقصى عرض — النص بيتلف على أكتر من سطر لو أطول (لحد maxLines)
     * @param tight من غير مسافات الخط فوق وتحت (للرقم الكبير)
     */
    fun render(
        c: Context, text: CharSequence, font: Font, sizeSp: Float,
        maxWidthDp: Float, maxLines: Int = 1, tight: Boolean = false
    ): Bitmap {
        val paint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            typeface = typeface(c, font)
            textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, sizeSp, c.resources.displayMetrics
            )
        }
        val maxW = max(1, dp(c, maxWidthDp).toInt())
        // أول مرة بأقصى عرض، وبعدين نضيّق على أطول سطر فعلي
        var layout = build(text, paint, maxW, maxLines, tight)
        var used = 0f
        for (i in 0 until layout.lineCount) used = max(used, layout.getLineWidth(i))
        val w = min(maxW, max(1, ceil(used).toInt()))
        if (w < maxW) layout = build(text, paint, w, maxLines, tight)

        val bmp = Bitmap.createBitmap(w, max(1, layout.height), Bitmap.Config.ARGB_8888)
        layout.draw(Canvas(bmp))
        return bmp
    }

    private fun build(text: CharSequence, p: TextPaint, w: Int, maxLines: Int, tight: Boolean): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, p, w)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_RTL)
            .setIncludePad(!tight)
            .setLineSpacing(0f, 1.05f)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
}
