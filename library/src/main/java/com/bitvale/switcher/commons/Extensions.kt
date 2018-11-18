package com.bitvale.switcher.commons

import android.content.Context

/**
 * Created by Alexander Kolpakov on 11/17/2018
 */
fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}

fun Context.toDp(value: Int) = resources.displayMetrics.density * value
