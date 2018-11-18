package com.bitvale.switcher

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.ColorInt
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
) : View(context, attrs, defStyleAttr) {

    private var iconRadius = 0f
    private var iconClipRadius = 0f
    private var iconCollapsedWidth = 0f
    private var checked = true

    @ColorInt
    private var onColor = 0
    @ColorInt
    private var offColor = 0
    @ColorInt
    private var iconColor = 0

    private val switcherRect = RectF(0f, 0f, 0f, 0f)
    private val switcherPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val iconRect = RectF(0f, 0f, 0f, 0f)
    private val iconClipRect = RectF(0f, 0f, 0f, 0f)
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val iconClipPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var animatorSet: AnimatorSet? = AnimatorSet()

    private var onClickRadiusOffset = 0f
        set(value) {
            field = value
            switcherRect.left = value
            switcherRect.top = value
            switcherRect.right = width.toFloat() - value
            switcherRect.bottom = height.toFloat() - value
            invalidate()
        }

    @ColorInt
    private var currentColor = 0
        set(value) {
            field = value
            switcherPaint.color = value
            iconClipPaint.color = value
        }

    private var switcherCornerRadius = 0f
    private var switchElevation = 0f
    private var iconHeight = 0f

    private var iconTranslateX = 0f

    // from rounded rect to circle and back
    private var iconProgress = 0f
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
                postInvalidateOnAnimation()
            }
        }

    init {
        attrs?.let { retrieveAttributes(attrs, defStyleAttr) }
        setOnClickListener { animateSwitch() }
    }

    private fun retrieveAttributes(attrs: AttributeSet, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Switcher,
                defStyleAttr, R.style.Switcher)

        switchElevation = typedArray.getDimension(R.styleable.Switcher_android_elevation, 0f)

        onColor = typedArray.getColor(R.styleable.Switcher_switcher_on_color, 0)
        offColor = typedArray.getColor(R.styleable.Switcher_switcher_off_color, 0)
        iconColor = typedArray.getColor(R.styleable.Switcher_switcher_icon_color, 0)

        checked = typedArray.getBoolean(R.styleable.Switcher_android_checked, true)

        if (!checked) iconProgress = 1f

        currentColor = if (checked) onColor
        else offColor

        iconPaint.color = iconColor

        typedArray.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        switcherRect.right = width.toFloat()
        switcherRect.bottom = height.toFloat()

        switcherCornerRadius = height / 2f

        iconRadius = switcherCornerRadius * 0.6f
        iconClipRadius = iconRadius / 2.25f
        iconCollapsedWidth = iconRadius - iconClipRadius

        iconHeight = iconRadius * 2f

        iconRect.set(
                width - switcherCornerRadius - iconCollapsedWidth / 2,
                (height - iconHeight) / 2f,
                width - switcherCornerRadius + iconCollapsedWidth / 2,
                height - (height - iconHeight) / 2f
        )

        if (!checked) {
            iconRect.left = width - switcherCornerRadius - iconCollapsedWidth / 2 - (iconRadius - iconCollapsedWidth / 2)
            iconRect.right = width - switcherCornerRadius + iconCollapsedWidth / 2 + (iconRadius - iconCollapsedWidth / 2)

            iconClipRect.set(
                    iconRect.centerX() - iconClipRadius,
                    iconRect.centerY() - iconClipRadius,
                    iconRect.centerX() + iconClipRadius,
                    iconRect.centerY() + iconClipRadius
            )

            iconTranslateX = -(width - switcherCornerRadius * 2)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outlineProvider = SwitchOutline(w, h)
            elevation = switchElevation
        }
    }

    override fun onDraw(canvas: Canvas?) {
        // switcher
        canvas?.drawRoundRect(switcherRect, switcherCornerRadius, switcherCornerRadius, switcherPaint)

        // icon
        canvas?.withTranslation(
                x = iconTranslateX
        ) {
            drawRoundRect(iconRect, switcherCornerRadius, switcherCornerRadius, iconPaint)
            // don't draw clip path if icon is collapsed state (to prevent drawing small circle
            // on rounded rect when switch is checked)
            if (iconClipRect.width() > iconCollapsedWidth)
                drawRoundRect(iconClipRect, iconRadius, iconRadius, iconClipPaint)
        }
    }

    private fun animateSwitch() {
        animatorSet?.cancel()
        animatorSet = AnimatorSet()

        onClickRadiusOffset = ON_CLICK_RADIUS_OFFSET

        var amplitude = BOUNCE_ANIM_AMPLITUDE_IN
        var frequency = BOUNCE_ANIM_FREQUENCY_IN
        var iconTranslateA = 0f
        var iconTranslateB = -(width - switcherCornerRadius * 2)
        var newProgress = 1f

        if (!checked) {
            amplitude = BOUNCE_ANIM_AMPLITUDE_OUT
            frequency = BOUNCE_ANIM_FREQUENCY_OUT
            iconTranslateA = iconTranslateB
            iconTranslateB = 0f
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
            doOnEnd {
                onClickRadiusOffset = 0f
            }
            duration = TRANSLATE_ANIMATION_DURATION
        }

        val toColor = if (!checked) onColor else offColor

        iconClipPaint.color = toColor

        val colorAnimator = ValueAnimator().apply {
            addUpdateListener {
                currentColor = it.animatedValue as Int
            }
            setIntValues(currentColor, toColor)
            setEvaluator(ArgbEvaluator())
            duration = COLOR_ANIMATION_DURATION
        }

        animatorSet?.apply {
            doOnStart {
                checked = !checked
                listener?.invoke(checked)
            }
            playTogether(switcherAnimator, translateAnimator, colorAnimator)
            start()
        }
    }

    private var listener: ((isChecked: Boolean) -> Unit)? = null

    /**
     * Register a callback to be invoked when the checked state of this switch
     * changes.
     *
     * @param listener the callback to call on checked state change
     */
    fun setOnCheckedChangeListener(listener: (isChecked: Boolean) -> Unit) {
        this.listener = listener
    }

    /**
     * <p>Changes the checked state of this switch.</p>
     *
     * @param checked true to check the switch, false to uncheck it
     */
    fun setChecked(checked: Boolean) {
        if (this.checked != checked) {
            this.checked = checked
            animateSwitch()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()
        return Bundle().apply {
            putBoolean(KEY_CHECKED, checked)
            putParcelable(STATE, super.onSaveInstanceState())
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable(STATE))
            checked = state.getBoolean(KEY_CHECKED)
            if (!checked) forceCheck()
        }
    }

    private fun forceCheck() {
        currentColor = offColor
        iconProgress = 1f
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private inner class SwitchOutline internal constructor(internal var width: Int, internal var height: Int) :
            ViewOutlineProvider() {

        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, width, height, switcherCornerRadius)
        }
    }
}