package com.bitvale.switcher.common

import android.content.Context
import android.util.TypedValue

/**
 * Created by Alexander Kolpakov (jquickapp@gmail.com) on 17-Nov-18
 * https://github.com/bitvale
 */
fun lerp(a: Float, b: Float, t: Float): Float =
    a + (b - a) * t

fun Context.toPx(value: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics).toInt()

fun isLollipopOrAbove(): Boolean =
    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP

