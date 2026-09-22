package com.ashraf.natiga

import android.content.Context
import android.graphics.Bitmap
import com.ashraf.natiga.TextArt.Font

// بيملى الـ layout بتاع الـ widget — نفس الكود للـ widget وللمعاينة جوه التطبيق
interface ViewTarget {
    fun image(id: Int, bmp: Bitmap, description: CharSequence)
    fun visible(id: Int, show: Boolean)
    fun background(id: Int, res: Int)
}

object WidgetFill {
    /** @param widthDp عرض الـ widget الفعلي — عشان النصوص الطويلة تتلف على قد العمود */
    fun fill(c: Context, d: TriDate, widthDp: Float, t: ViewTarget) {
        // عرض الأعمدة (نفس الـ paddings والفواصل اللي في widget_date.xml) — للنصوص اللي بتتلف.
        // النصوص اللي سطر واحد بتترسم بعرضها الطبيعي، ولو أعرض من مكانها الـ ImageView بيصغّرها بدل ما تتقص
        val dateCol = (widthDp - 20f - 17f) / 2f
        val insightCol = (widthDp - 28f - 17f) / 2f

        fun draw(id: Int, text: String, font: Font, sp: Float, maxW: Float, lines: Int = 1, tight: Boolean = false) =
            t.image(id, TextArt.render(c, text, font, sp, maxW, lines, tight), text)

        // الشريط العلوي
        draw(R.id.day_name, d.dayName, Font.BOLD, 14f, widthDp)
        draw(R.id.season, d.season.name, Font.REGULAR, 12f, widthDp)

        // النص اليمين: القبطي — الرقم الكبير بـ Carlito لو الأرقام 123
        val num = if (d.latinDigits)
            TextArt.render(c, d.copticDay, Font.LATIN_BOLD, 46f, dateCol, tight = true)
        else
            TextArt.render(c, d.copticDay, Font.BOLD, 40f, dateCol, tight = true)
        t.image(R.id.day_num, num, d.copticDay)
        t.image(R.id.day_num_friday, num, d.copticDay)
        t.visible(R.id.day_num, !d.isFriday)
        t.visible(R.id.day_num_friday, d.isFriday)
        draw(R.id.coptic, d.copticMonthYear, Font.BOLD, 15f, widthDp)

        // النص الشمال: الميلادي والهجري + تمييز الأيام البيض
        draw(R.id.gregorian, d.gregorian, Font.BOLD, 15f, widthDp)
        draw(R.id.hijri, d.hijri, Font.REGULAR, 14f, widthDp)
        if (d.whiteDay != null) draw(R.id.white_day, d.whiteDay, Font.BOLD, 12f, dateCol - 8f, lines = 2)
        t.visible(R.id.white_day, d.whiteDay != null)
        t.background(R.id.greg_hijri_box, if (d.whiteDay != null) R.drawable.moon_bg else 0)

        // تأمل الشهر: المثل والمعنى يمين، والتوجيه شمال
        draw(R.id.proverb, d.insight.proverb, Font.BOLD, 13f, insightCol, lines = 2)
        draw(R.id.theme, d.insight.theme, Font.BOLD, 14f, insightCol, lines = 2)
        draw(R.id.advice, d.insight.advice, Font.REGULAR, 12f, insightCol, lines = 4)
    }
}
