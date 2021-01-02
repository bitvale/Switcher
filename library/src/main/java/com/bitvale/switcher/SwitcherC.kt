package com.bitvale.switcher

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.core.animation.doOnStart
import com.bitvale.switcher.common.BOUNCE_ANIM_AMPLITUDE_IN
import com.bitvale.switcher.common.BOUNCE_ANIM_AMPLITUDE_OUT
import com.bitvale.switcher.common.BOUNCE_ANIM_FREQUENCY_IN
import com.bitvale.switcher.common.BOUNCE_ANIM_FREQUENCY_OUT
import com.bitvale.switcher.common.COLOR_ANIMATION_DURATION
import com.bitvale.switcher.common.SWITCHER_ANIMATION_DURATION
import com.bitvale.switcher.common.isLollipopOrAbove
import com.bitvale.switcher.common.lerp
import kotlin.math.min

/**
 * Created by Alexander Kolpakov (jquickapp@gmail.com) on 11-Jul-18
 * https://github.com/bitvale
 */
class SwitcherC @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Switcher(context, attrs, defStyleAttr) {

    private var switcherRadius = 0f

    override var iconProgress = 0f
        set(value) {
            if (field != value) {
                field = value

                val iconOffset = lerp(0f, iconRadius - iconCollapsedWidth / 2, value)
                iconRect.left =
                    (switcherRadius - iconCollapsedWidth / 2 - iconOffset) + shadowOffset
                iconRect.right =
                    (switcherRadius + iconCollapsedWidth / 2 + iconOffset) + shadowOffset

                val clipOffset = lerp(0f, iconClipRadius, value)
                iconClipRect.set(
                    iconRect.centerX() - clipOffset,
                    iconRect.centerY() - clipOffset,
                    iconRect.centerX() + clipOffset,
                    iconRect.centerY() + clipOffset
                )
                postInvalidateOnAnimation()
            }
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            val min = min(defWidth.toFloat(), defHeight.toFloat()).toInt()
            width = min
            height = min
        }

        if (!isLollipopOrAbove()) {
            width += switchElevation.toInt() * 2
            height += switchElevation.toInt() * 2
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        switcherRadius = (min(w.toFloat(), h.toFloat()) / 2f) - shadowOffset

        if (isLollipopOrAbove()) {
            outlineProvider = SwitchOutline(switcherRadius.toInt())
            elevation = switchElevation
        } else {
            shadowOffset = switchElevation
        }

        iconRadius = switcherRadius * 0.5f
        iconClipRadius = iconRadius / 2.25f
        iconCollapsedWidth = (iconRadius - iconClipRadius) * 1.1f

        iconHeight = iconRadius * 2f

        iconRect.set(
            (switcherRadius - iconCollapsedWidth / 2f) + shadowOffset,
            ((switcherRadius * 2f - iconHeight) / 2f) + shadowOffset / 2,
            (switcherRadius + iconCollapsedWidth / 2f) + shadowOffset,
            (switcherRadius * 2f - (switcherRadius * 2f - iconHeight) / 2f) + shadowOffset / 2
        )

        if (!isChecked) {
            iconRect.left =
                (switcherRadius - iconCollapsedWidth / 2f - (iconRadius - iconCollapsedWidth / 2f)) + shadowOffset
            iconRect.right =
                (switcherRadius + iconCollapsedWidth / 2f + (iconRadius - iconCollapsedWidth / 2f)) + shadowOffset

            iconClipRect.set(
                iconRect.centerX() - iconClipRadius,
                iconRect.centerY() - iconClipRadius,
                iconRect.centerX() + iconClipRadius,
                iconRect.centerY() + iconClipRadius
            )
        }

        if (!isLollipopOrAbove()) generateShadow()
    }

    override fun generateShadow() {
        if (switchElevation == 0f) return
        if (!isInEditMode) {
            if (shadow == null) {
                shadow = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
            } else {
                shadow?.eraseColor(Color.TRANSPARENT)
            }
            val c = Canvas(shadow as Bitmap)

            c.drawCircle(
                switcherRadius + shadowOffset, switcherRadius + shadowOffset / 2,
                switcherRadius, shadowPaint
            )
            val rs = RenderScript.create(context)
            val blur = ScriptIntrinsicBlur.create(rs, Element.U8(rs))
            val input = Allocation.createFromBitmap(rs, shadow)
            val output = Allocation.createTyped(rs, input.type)
            blur.setRadius(switchElevation)
            blur.setInput(input)
            blur.forEach(output)
            output.copyTo(shadow)
            input.destroy()
            output.destroy()
            blur.destroy()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        // shadow
        if (!isLollipopOrAbove() && switchElevation > 0f && !isInEditMode) {
            canvas?.drawBitmap(shadow as Bitmap, 0f, shadowOffset, null)
        }

        // switcher
        canvas?.drawCircle(
            switcherRadius + shadowOffset,
            switcherRadius + shadowOffset / 2,
            switcherRadius,
            switcherPaint
        )

        // icon
        canvas?.drawRoundRect(iconRect, switcherRadius, switcherRadius, iconPaint)

        // don't draw clip path if icon is collapsed (to prevent drawing small circle
        // on rounded rect when switch is isChecked)
        if (iconClipRect.width() > iconCollapsedWidth)
            canvas?.drawRoundRect(iconClipRect, iconRadius, iconRadius, iconClipPaint)

    }

    override fun animateSwitch() {
        animatorSet?.cancel()
        animatorSet = AnimatorSet()

        var amplitude = BOUNCE_ANIM_AMPLITUDE_IN
        var frequency = BOUNCE_ANIM_FREQUENCY_IN
        var newProgress = 1f

        if (isChecked) {
            amplitude = BOUNCE_ANIM_AMPLITUDE_OUT
            frequency = BOUNCE_ANIM_FREQUENCY_OUT
            newProgress = 0f
        }

        val iconAnimator = ValueAnimator.ofFloat(iconProgress, newProgress).apply {
            addUpdateListener {
                iconProgress = it.animatedValue as Float
            }
            interpolator = BounceInterpolator(amplitude, frequency)
            duration = SWITCHER_ANIMATION_DURATION
        }

        val toColor = if (isChecked) onColor else offColor

        iconClipPaint.color = toColor

        val colorAnimator = ValueAnimator().apply {
            addUpdateListener { currentColor = it.animatedValue as Int }
            setIntValues(currentColor, toColor)
            setEvaluator(ArgbEvaluator())
            duration = COLOR_ANIMATION_DURATION
        }

        animatorSet?.apply {
            doOnStart {
                listener?.invoke(isChecked)
            }
            playTogether(iconAnimator, colorAnimator)
            start()
        }
    }

    override fun setChecked(checked: Boolean, withAnimation: Boolean) {
        if (this.isChecked != checked) {
            this.isChecked = checked
            if (withAnimation && width != 0) {
                animateSwitch()
            } else {
                animatorSet?.cancel()
                if (!checked) {
                    currentColor = offColor
                    iconProgress = 1f
                } else {
                    currentColor = onColor
                    iconProgress = 0f
                }
                listener?.invoke(isChecked)
            }
        }
    }

    override fun forceUncheck() {
        currentColor = offColor
        iconProgress = 1f
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class SwitchOutline constructor(private val size: Int) : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(
                0,
                0,
                size * 2,
                size * 2,
                size.toFloat()
            )
        }
    }
}