package com.bitvale.switcher.commons

import android.content.Context
import android.util.TypedValue

/**
 * Created by Alexander Kolpakov on 11/17/2018
 */
fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}

fun Context.toDp(value: Int) = resources.displayMetrics.density * value

fun Context.toPx(value: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics).toInt()

fun isLollipopAndAbove(): Boolean {
    return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP
}
