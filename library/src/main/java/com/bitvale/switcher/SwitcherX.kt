package com.bitvale.switcher

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.graphics.withTranslation
import com.bitvale.switcher.commons.*

/**
 * Created by Alexander Kolpakov on 11/7/2018
 */
class SwitcherX @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : Switcher(context, attrs, defStyleAttr) {

    private val switcherRect = RectF(0f, 0f, 0f, 0f)
    private var switcherCornerRadius = 0f

    private var iconTranslateX = 0f

    private var onClickOffset = 0f
        set(value) {
            field = value
            switcherRect.left = value + shadowOffset
            switcherRect.top = value + shadowOffset / 2
            switcherRect.right = width.toFloat() - value - shadowOffset
            switcherRect.bottom = height.toFloat() - value - shadowOffset - shadowOffset / 2
            if (!isLollipopAndAbove()) generateShadow()
            invalidate()
        }

    override var iconProgress = 0f
        set(value) {
            if (field != value) {
                field = value

                val iconOffset = lerp(0f, iconRadius - iconCollapsedWidth / 2, value)
                iconRect.left = width - switcherCornerRadius - iconCollapsedWidth / 2 - iconOffset
                iconRect.right = width - switcherCornerRadius + iconCollapsedWidth / 2 + iconOffset

                val clipOffset = lerp(0f, iconClipRadius, value)
                iconClipRect.set(
                        iconRect.centerX() - clipOffset,
                        iconRect.centerY() - clipOffset,
                        iconRect.centerX() + clipOffset,
                        iconRect.centerY() + clipOffset
                )
                if (!isLollipopAndAbove()) generateShadow()
                postInvalidateOnAnimation()
            }
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            width = defWidth
            height = defHeight
        }

        if (!isLollipopAndAbove()) {
            width += switchElevation.toInt() * 2
            height += switchElevation.toInt() * 2
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        if (isLollipopAndAbove()) {
            outlineProvider = SwitchOutline(w, h)
            elevation = switchElevation
        } else {
            shadowOffset = switchElevation
            iconTranslateX = -shadowOffset
        }

        switcherRect.left = shadowOffset
        switcherRect.top = shadowOffset / 2
        switcherRect.right = width.toFloat() - shadowOffset
        switcherRect.bottom = height.toFloat() - shadowOffset - shadowOffset / 2

        switcherCornerRadius = (height - shadowOffset * 2) / 2f

        iconRadius = switcherCornerRadius * 0.6f
        iconClipRadius = iconRadius / 2.25f
        iconCollapsedWidth = iconRadius - iconClipRadius

        iconHeight = iconRadius * 2f

        iconRect.set(
                width - switcherCornerRadius - iconCollapsedWidth / 2,
                ((height - iconHeight) / 2f) - shadowOffset / 2,
                width - switcherCornerRadius + iconCollapsedWidth / 2,
                (height - (height - iconHeight) / 2f) - shadowOffset / 2
        )

        if (!isChecked) {
            iconRect.left =
                    width - switcherCornerRadius - iconCollapsedWidth / 2 - (iconRadius - iconCollapsedWidth / 2)
            iconRect.right =
                    width - switcherCornerRadius + iconCollapsedWidth / 2 + (iconRadius - iconCollapsedWidth / 2)

            iconClipRect.set(
                    iconRect.centerX() - iconClipRadius,
                    iconRect.centerY() - iconClipRadius,
                    iconRect.centerX() + iconClipRadius,
                    iconRect.centerY() + iconClipRadius
            )

            iconTranslateX = -(width - shadowOffset - switcherCornerRadius * 2)
        }

        if (!isLollipopAndAbove()) generateShadow()
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

            c.drawRoundRect(switcherRect, switcherCornerRadius, switcherCornerRadius, shadowPaint)

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
        if (!isLollipopAndAbove() && switchElevation > 0f && !isInEditMode) {
            canvas?.drawBitmap(shadow as Bitmap, 0f, shadowOffset, null)
        }

        // switcher
        canvas?.drawRoundRect(
                switcherRect,
                switcherCornerRadius,
                switcherCornerRadius,
                switcherPaint
        )

        // icon
        canvas?.withTranslation(
                x = iconTranslateX
        ) {
            drawRoundRect(iconRect, switcherCornerRadius, switcherCornerRadius, iconPaint)
            /* don't draw clip path if icon is collapsed (to prevent drawing small circle
            on rounded rect when switch is isChecked)*/
            if (iconClipRect.width() > iconCollapsedWidth)
                drawRoundRect(iconClipRect, iconRadius, iconRadius, iconClipPaint)
        }
    }

    override fun animateSwitch() {
        animatorSet?.cancel()
        animatorSet = AnimatorSet()

        onClickOffset = ON_CLICK_RADIUS_OFFSET

        var amplitude = BOUNCE_ANIM_AMPLITUDE_IN
        var frequency = BOUNCE_ANIM_FREQUENCY_IN
        var iconTranslateA = 0f
        var iconTranslateB = -(width - shadowOffset - switcherCornerRadius * 2)
        var newProgress = 1f

        if (isChecked) {
            amplitude = BOUNCE_ANIM_AMPLITUDE_OUT
            frequency = BOUNCE_ANIM_FREQUENCY_OUT
            iconTranslateA = iconTranslateB
            iconTranslateB = -shadowOffset
            newProgress = 0f
        }

        val switcherAnimator = ValueAnimator.ofFloat(iconProgress, newProgress).apply {
            addUpdateListener {
                iconProgress = it.animatedValue as Float
            }
            interpolator = BounceInterpolator(amplitude, frequency)
            duration = SWITCHER_ANIMATION_DURATION
        }

        val translateAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                val value = it.animatedValue as Float
                iconTranslateX = lerp(iconTranslateA, iconTranslateB, value)
            }
            doOnEnd { onClickOffset = 0f }
            duration = TRANSLATE_ANIMATION_DURATION
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
            playTogether(switcherAnimator, translateAnimator, colorAnimator)
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
                    iconTranslateX = -(width - shadowOffset - switcherCornerRadius * 2)
                } else {
                    currentColor = onColor
                    iconProgress = 0f
                    iconTranslateX = -shadowOffset
                }
                listener?.invoke(isChecked)
            }
        }
    }

    override fun forceUncheck() {
        currentColor = offColor
        iconProgress = 1f
        iconTranslateX = -(width - shadowOffset - switcherCornerRadius * 2)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private inner class SwitchOutline internal constructor(
            internal var width: Int,
            internal var height: Int
    ) :
            ViewOutlineProvider() {

        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, width, height, switcherCornerRadius)
        }
    }

}