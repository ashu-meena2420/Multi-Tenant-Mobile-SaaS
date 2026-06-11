package com.example.propmanager.util

import java.text.NumberFormat
import java.util.Locale

private val indianLocale = Locale.forLanguageTag("en-IN")

/** Formats a Double as Indian Rupee currency e.g. ₹1,50,000 */
fun formatRupees(amount: Double): String {
    val nf = NumberFormat.getNumberInstance(indianLocale)
    nf.maximumFractionDigits = 0
    nf.minimumFractionDigits = 0
    return "₹${nf.format(amount)}"
}

/** Validates a 10-digit Indian mobile number */
fun isValidIndianPhone(phone: String): Boolean =
    phone.matches(Regex("^[6-9]\\d{9}$"))
