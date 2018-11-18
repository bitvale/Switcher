package com.bitvale.switcher.commons

/**
 * Created by Alexander Kolpakov on 11/17/2018
 */
fun lerp(a: Float, b: Float, t: Float): Float {
    return a + (b - a) * t
}