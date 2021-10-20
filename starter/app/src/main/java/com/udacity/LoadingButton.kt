package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates


private const val ANIMATION_DURATION = 1_500L


class LoadingButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var sweepAngle = 0f
    private val loadingBarRect = Rect()

    private val valueAnimator = ValueAnimator()
    private var buttonText = ""
    private var textLoading = ""
    private var textNormal = ""

    private val paint = Paint().apply {
        isAntiAlias = true
        textSize = 55f
    }

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when(new) {
            ButtonState.Clicked -> {
                buttonText = "Clicked"
                invalidate()
            }
            ButtonState.Loading -> {
                buttonText = resources.getString(R.string.button_loading)
            }
            ButtonState.Completed -> {
                buttonText = resources.getString(R.string.button_download)
                valueAnimator.cancel()
                sweepAngle = 0f
                loadingBarRect.right = 0
                invalidate()
            }
        }

    }

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            textNormal = getString(R.styleable.LoadingButton_textNormal).toString()
            textLoading = getString(R.styleable.LoadingButton_textLoading).toString()
        }
        buttonText = textNormal
    }

    override fun performClick(): Boolean {
        val steps = 100

        valueAnimator.setIntValues(0, steps)
        loadingBarRect.bottom = heightSize
        valueAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Int
            println("value - $value")
            loadingBarRect.right = value * widthSize / steps
            sweepAngle = (value * 360 / steps).toFloat()
            println("Sweep angle: $sweepAngle")
            invalidate()
        }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                isClickable = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                isClickable = true
                buttonText = textNormal
                loadingBarRect.right = 0
                sweepAngle = 0f
            }
        })
        valueAnimator.duration = 3000
        valueAnimator.start()

        return super.performClick()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawMainButton(canvas)
        drawLoadingBar(canvas)
        drawButtonTitle(canvas)
        drawLoadingCircle(canvas)
    }

    private fun drawMainButton(canvas: Canvas?) {
        paint.color = context.getColor(R.color.colorPrimary)
        canvas?.drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), paint)
    }

    private fun drawLoadingBar(canvas: Canvas?) {
        paint.color = context.getColor(R.color.colorPrimaryDark)
        canvas?.drawRect(0f, 0f, loadingBarRect.width().toFloat(), loadingBarRect.height().toFloat(), paint)
    }

    private fun drawButtonTitle(canvas: Canvas?) {
        paint.color = Color.WHITE
        var titleWidth = paint.measureText(buttonText)
        var centerX = widthSize / 2 - titleWidth / 2
        var centerY = heightSize / 2 - (paint.descent() + paint.ascent()) / 2
        canvas?.drawText(buttonText, centerX, centerY, paint )
    }

    private fun drawLoadingCircle(canvas: Canvas?) {
        var titleWidth = paint.measureText(buttonText)
        canvas?.save()
        canvas?.translate(widthSize / 2 + titleWidth / 2 + (paint.textSize/2), heightSize / 2 - paint.textSize / 2)
        paint.color = context.getColor(R.color.colorAccent)
        canvas?.drawArc(RectF(0f, 0f, paint.textSize, paint.textSize), 0F, sweepAngle, true,  paint)
        canvas?.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}