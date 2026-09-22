package com.ashraf.natiga

import android.content.Context
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.icu.util.CopticCalendar
import android.icu.util.GregorianCalendar
import android.icu.util.IslamicCalendar

data class TriDate(
    val dayName: String,
    val isFriday: Boolean,
    val copticDay: String,        // رقم اليوم القبطي (الرقم الكبير — النص اليمين)
    val copticMonthYear: String,  // الشهر والسنة القبطية
    val gregorian: String,        // التاريخ الميلادي كامل (النص الشمال)
    val hijri: String,            // التاريخ الهجري كامل (النص الشمال)
    val whiteDay: String?,        // الأيام البيض (١٣ و١٤ و١٥ هجري) — null في باقي الأيام
    val season: Season,
    val insight: Insight
)

object Prefs {
    const val HIJRI_OFFSET = "hijri_offset"
    const val ARABIC_DIGITS = "arabic_digits"
    fun get(c: Context): SharedPreferences = c.getSharedPreferences("natiga", Context.MODE_PRIVATE)
    fun hijriOffset(c: Context): Int = get(c).getInt(HIJRI_OFFSET, 0)
    fun arabicDigits(c: Context): Boolean = get(c).getBoolean(ARABIC_DIGITS, true)
}

object DateCalc {
    private val DAYS = arrayOf("الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
    private val GREG = arrayOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )
    private val HIJRI = arrayOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )
    private val COPTIC = arrayOf(
        "توت", "بابه", "هاتور", "كيهك", "طوبة", "أمشير", "برمهات",
        "برمودة", "بشنس", "بؤونة", "أبيب", "مسرى", "النسيء"
    )

    fun today(context: Context): TriDate {
        val ar = Prefs.arabicDigits(context)
        val now = System.currentTimeMillis()

        val g = GregorianCalendar()
        g.timeInMillis = now
        val dow = g.get(Calendar.DAY_OF_WEEK) // الأحد = 1

        val h = IslamicCalendar()
        h.setCalculationType(IslamicCalendar.CalculationType.ISLAMIC_UMALQURA)
        h.timeInMillis = now
        val offset = Prefs.hijriOffset(context)
        if (offset != 0) h.add(Calendar.DATE, offset)
        val hDay = h.get(Calendar.DAY_OF_MONTH)

        val c = CopticCalendar()
        c.timeInMillis = now
        val cMonth = c.get(Calendar.MONTH)

        return TriDate(
            dayName = DAYS[dow - 1],
            isFriday = dow == Calendar.FRIDAY,
            copticDay = num(c.get(Calendar.DAY_OF_MONTH), ar),
            copticMonthYear = "${COPTIC[cMonth]} ${num(c.get(Calendar.YEAR), ar)} ش",
            gregorian = "${num(g.get(Calendar.DAY_OF_MONTH), ar)} ${GREG[g.get(Calendar.MONTH)]} ${num(g.get(Calendar.YEAR), ar)}",
            hijri = "${num(hDay, ar)} ${HIJRI[h.get(Calendar.MONTH)]} ${num(h.get(Calendar.YEAR), ar)} هـ",
            whiteDay = when (hDay) {
                13 -> "🌔 أول الأيام البيض"
                14 -> "🌕 ليلة البدر — اكتمال القمر"
                15 -> "🌖 آخر الأيام البيض"
                else -> null
            },
            season = Insights.seasonFor(cMonth),
            insight = Insights.forCopticMonth(cMonth)
        )
    }

    fun num(n: Int, arabic: Boolean): String {
        val s = n.toString()
        if (!arabic) return s
        return s.map { ch -> if (ch in '0'..'9') '٠' + (ch - '0') else ch }.joinToString("")
    }
}
