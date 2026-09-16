package com.example.managementproduct.util

import java.text.NumberFormat
import java.util.Locale

private val localeID = Locale("in", "ID")
private val rupiahFormat = NumberFormat.getCurrencyInstance(localeID)

fun Long.formatRupiah(): String {
    return rupiahFormat.format(this).replace(",00", "")
}
