package com.cardioflex.motor.utils

fun parseInt(text: String): Int {
    if (text.isEmpty()) return 0
    return Integer.parseInt(text)
}