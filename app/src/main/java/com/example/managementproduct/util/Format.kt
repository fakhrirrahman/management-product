package com.example.managementproduct.util

import java.text.NumberFormat
import java.util.Locale

fun Long.formatRupiah(): String {
    val localeID = Locale("in", "ID")
    val format = NumberFormat.getCurrencyInstance(localeID)
    return format.format(this).replace(",00", "")
}
