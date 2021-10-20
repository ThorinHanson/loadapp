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
    private var title: String
    private lateinit var loading: String
    private lateinit var default: String

    private val paint = Paint().apply {
        isAntiAlias = true
        textSize = 55f
    }

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when(new) {
            ButtonState.Clicked -> {
                title = "Clicked"
                invalidate()
            }
            ButtonState.Loading -> {
                title = resources.getString(R.string.button_loading)
            }
            ButtonState.Completed -> {
                title = resources.getString(R.string.button_download)
                valueAnimator.cancel()
                sweepAngle = 0f
                loadingBarRect.right = 0
                invalidate()
            }
        }

    }

    init {
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            default = getString(R.styleable.LoadingButton_textNormal).toString()
            loading = getString(R.styleable.LoadingButton_textLoading).toString()
        }
        title = default
        isClickable = true
    }

    override fun performClick(): Boolean {
        val steps = 100
        valueAnimator.duration = 5000

        valueAnimator.setIntValues(0, steps)
        loadingBarRect.bottom = heightSize

        valueAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Int
            loadingBarRect.right = value * widthSize / steps
            sweepAngle = (value * 360 / steps).toFloat()
            invalidate()
        }

        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                isClickable = false
            }

            override fun onAnimationEnd(animation: Animator?) {
                isClickable = true
                title = default
                loadingBarRect.right = 0
                sweepAngle = 0f
            }
        })
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
        var titleWidth = paint.measureText(title)
        var centerX = widthSize / 2 - titleWidth / 2
        var centerY = heightSize / 2 - (paint.descent() + paint.ascent()) / 2
        canvas?.drawText(title, centerX, centerY, paint )
    }

    private fun drawLoadingCircle(canvas: Canvas?) {
        var titleWidth = paint.measureText(title)
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