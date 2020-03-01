package com.bitvale.switcher.commons

import android.content.Context
import android.util.TypedValue

/**
 * Created by Alexander Kolpakov on 11/17/2018
 */
fun lerp(a: Float, b: Float, t: Float): Float =
    a + (b - a) * t

fun Context.toPx(value: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics).toInt()

fun isLollipopAndAbove(): Boolean =
    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP

