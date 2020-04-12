package com.bitvale.switcher

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.ColorInt
import androidx.core.animation.doOnStart
import com.bitvale.switcher.commons.BOUNCE_ANIM_AMPLITUDE_IN
import com.bitvale.switcher.commons.BOUNCE_ANIM_AMPLITUDE_OUT
import com.bitvale.switcher.commons.BOUNCE_ANIM_FREQUENCY_IN
import com.bitvale.switcher.commons.BOUNCE_ANIM_FREQUENCY_OUT
import com.bitvale.switcher.commons.COLOR_ANIMATION_DURATION
import com.bitvale.switcher.commons.KEY_CHECKED
import com.bitvale.switcher.commons.STATE
import com.bitvale.switcher.commons.SWITCHER_ANIMATION_DURATION
import com.bitvale.switcher.commons.isLollipopAndAbove
import com.bitvale.switcher.commons.lerp
import com.bitvale.switcher.commons.toPx
import kotlin.math.min


/**
 * Created by Alexander Kolpakov on 11/7/2018
 */
class SwitcherC @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var switcherRadius = 0f
    private var iconRadius = 0f
    private var iconClipRadius = 0f
    private var iconCollapsedWidth = 0f
    private var defHeight = 0
    private var defWidth = 0
    var isChecked = true
        private set

    @ColorInt
    var onColor = 0
    @ColorInt
    var offColor = 0
    @ColorInt
    private var iconColor = 0

    private val switcherPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val iconRect = RectF(0f, 0f, 0f, 0f)
    private val iconClipRect = RectF(0f, 0f, 0f, 0f)
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val iconClipPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var animatorSet: AnimatorSet? = AnimatorSet()

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var shadow: Bitmap? = null
    private var shadowOffset = 0f

    @ColorInt
    private var currentColor = 0
        set(value) {
            field = value
            switcherPaint.color = value
            iconClipPaint.color = value
        }

    private var switchElevation = 0f
    private var iconHeight = 0f

    // from rounded rect to circle and back
    private var iconProgress = 0f
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

    init {
        attrs?.let { retrieveAttributes(attrs, defStyleAttr) }
        setOnClickListener { setChecked(!isChecked) }
    }

    @SuppressLint("CustomViewStyleable")
    private fun retrieveAttributes(attrs: AttributeSet, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.Switcher,
            defStyleAttr,
            R.style.Switcher
        )

        switchElevation = typedArray.getDimension(R.styleable.Switcher_elevation, 0f)

        onColor = typedArray.getColor(R.styleable.Switcher_switcher_on_color, 0)
        offColor = typedArray.getColor(R.styleable.Switcher_switcher_off_color, 0)
        iconColor = typedArray.getColor(R.styleable.Switcher_switcher_icon_color, 0)

        isChecked = typedArray.getBoolean(R.styleable.Switcher_android_checked, true)

        if (!isChecked) iconProgress = 1f

        currentColor = if (isChecked) onColor else offColor

        iconPaint.color = iconColor

        defHeight = typedArray.getDimensionPixelOffset(R.styleable.Switcher_switcher_height, 0)
        defWidth = typedArray.getDimensionPixelOffset(R.styleable.Switcher_switcher_width, 0)

        typedArray.recycle()

        if (!isLollipopAndAbove() && switchElevation > 0f) {
            shadowPaint.colorFilter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
            shadowPaint.alpha = 51 // 20%
            setShadowBlurRadius(switchElevation)
            setLayerType(LAYER_TYPE_SOFTWARE, null)
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

        if (!isLollipopAndAbove()) {
            width += switchElevation.toInt() * 2
            height += switchElevation.toInt() * 2
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (isLollipopAndAbove()) {
            outlineProvider = SwitchOutline(w, h)
            elevation = switchElevation
        } else {
            shadowOffset = switchElevation
        }

        switcherRadius = (min(w.toFloat(), h.toFloat()) / 2f) - shadowOffset

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

        if (!isLollipopAndAbove()) generateShadow()
    }

    private fun generateShadow() {
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

    fun applyColorChange() {
        animatorSet?.cancel()
        if (!isChecked) {
            currentColor = offColor
            iconProgress = 1f
        } else {
            currentColor = onColor
            iconProgress = 0f
        }
    }

    override fun onDraw(canvas: Canvas?) {
        // shadow
        if (!isLollipopAndAbove() && switchElevation > 0f && !isInEditMode) {
            canvas?.drawBitmap(shadow as Bitmap, 0f, shadowOffset, null)
        }

        // switcher
        canvas?.drawCircle(
            switcherRadius + shadowOffset, switcherRadius + shadowOffset / 2,
            switcherRadius, switcherPaint
        )

        // icon
        canvas?.drawRoundRect(iconRect, switcherRadius, switcherRadius, iconPaint)
        /* don't draw clip path if icon is collapsed (to prevent drawing small circle
        on rounded rect when switch is isChecked)*/
        if (iconClipRect.width() > iconCollapsedWidth)
            canvas?.drawRoundRect(iconClipRect, iconRadius, iconRadius, iconClipPaint)

    }

    private fun animateSwitch() {
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

    private var listener: ((isChecked: Boolean) -> Unit)? = null

    /**
     * Register a callback to be invoked when the isChecked state of this switch
     * changes.
     *
     * @param listener the callback to call on isChecked state change
     */
    fun setOnCheckedChangeListener(listener: (isChecked: Boolean) -> Unit) {
        this.listener = listener
    }

    /**
     * <p>Changes the isChecked state of this switch.</p>
     *
     * @param checked true to check the switch, false to uncheck it
     * @param withAnimation use animation
     */
    fun setChecked(checked: Boolean, withAnimation: Boolean = true) {
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
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()
        return Bundle().apply {
            putBoolean(KEY_CHECKED, isChecked)
            putParcelable(STATE, super.onSaveInstanceState())
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable(STATE))
            isChecked = state.getBoolean(KEY_CHECKED)
            if (!isChecked) forceUncheck()
        }
    }

    private fun forceUncheck() {
        currentColor = offColor
        iconProgress = 1f
    }

    private fun setShadowBlurRadius(elevation: Float) {
        val maxElevation = context.toPx(24f)
        switchElevation = min(25f * (elevation / maxElevation), 25f)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private inner class SwitchOutline internal constructor(
        internal var width: Int,
        internal var height: Int
    ) :
        ViewOutlineProvider() {

        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(
                0,
                0,
                (switcherRadius * 2).toInt(),
                (switcherRadius * 2).toInt(),
                switcherRadius
            )
        }
    }
}