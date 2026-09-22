package com.ashraf.natiga

// بيملى الـ layout بتاع الـ widget — نفس الكود للـ widget وللمعاينة جوه التطبيق
interface ViewTarget {
    fun text(id: Int, s: CharSequence)
    fun visible(id: Int, show: Boolean)
    fun background(id: Int, res: Int)
}

object WidgetFill {
    fun fill(d: TriDate, t: ViewTarget) {
        t.text(R.id.day_name, d.dayName)
        t.text(R.id.season, d.season.name)

        // النص اليمين: القبطي
        t.text(R.id.day_num, d.copticDay)
        t.text(R.id.day_num_friday, d.copticDay)
        t.visible(R.id.day_num, !d.isFriday)
        t.visible(R.id.day_num_friday, d.isFriday)
        t.text(R.id.coptic, d.copticMonthYear)

        // النص الشمال: الميلادي والهجري + تمييز الأيام البيض
        t.text(R.id.gregorian, d.gregorian)
        t.text(R.id.hijri, d.hijri)
        t.text(R.id.white_day, d.whiteDay ?: "")
        t.visible(R.id.white_day, d.whiteDay != null)
        t.background(R.id.greg_hijri_box, if (d.whiteDay != null) R.drawable.moon_bg else 0)

        // تأمل الشهر
        t.text(R.id.proverb, d.insight.proverb)
        t.text(R.id.theme, d.insight.theme)
        t.text(R.id.advice, d.insight.advice)
    }
}
