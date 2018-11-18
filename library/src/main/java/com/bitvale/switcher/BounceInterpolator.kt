package com.bitvale.switcher

import android.view.animation.Interpolator

/**
 * Created by Evgenii Neumerzhitckii
 * (read more on https://evgenii.com/blog/spring-button-animation-on-android/)
 */
class BounceInterpolator(private val amplitude: Double, private val frequency: Double) : Interpolator {
    override fun getInterpolation(time: Float): Float {
        return (-1 * Math.pow(Math.E, -time / amplitude) * Math.cos(frequency * time) + 1).toFloat()
    }
}