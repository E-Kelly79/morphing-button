package com.routematch.morphingbutton

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.RectF
import android.util.AttributeSet
import androidx.annotation.NonNull

class RmLinearProgressButton(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    RmMorphingButton(context, attrs, defStyleAttr), MorphingProgress {
    val MAX_PROGRESS = 100
    val MIN_PROGRESS = 0

    private var mProgress = 0
    private var mProgressColor = 0
    private var mProgressCornerRadius = 0
    private var mPaint: Paint? = null
    private var mRectF: RectF? = null


    override fun onDraw(@NonNull canvas: Canvas) {
        super.onDraw(canvas)
        if (!mAnimationInProgress && mProgress > MIN_PROGRESS && mProgress <= MAX_PROGRESS) {
            if (mPaint == null) {
                mPaint = Paint()
                mPaint!!.isAntiAlias = true
                mPaint!!.style = FILL
                mPaint!!.color = mProgressColor
            }
            if (mRectF == null) {
                mRectF = RectF()
            }
            mRectF!!.right = (width / MAX_PROGRESS * mProgress).toFloat()
            mRectF!!.bottom = height.toFloat()
            canvas.drawRoundRect(mRectF, mProgressCornerRadius.toFloat(), mProgressCornerRadius.toFloat(), mPaint)
        }
    }

    @JvmName("name")
    fun morph(@NonNull params: Params?) {
        super.morph(params!!)
        mProgress = MIN_PROGRESS
        mPaint = null
        mRectF = null
    }

    override fun setProgress(progress: Int) {
        mProgress = progress
        invalidate()
    }

    fun morphToProgress(
        color: Int,
        progressColor: Int,
        progressCornerRadius: Int,
        width: Int,
        height: Int,
        duration: Int
    ) {
        mProgressCornerRadius = progressCornerRadius
        mProgressColor = progressColor
        val longRoundedSquare: Params = Params.create()
            .duration(duration)
            .cornerRadius(mProgressCornerRadius)
            .width(width)
            .height(height)
            .color(color)
            .colorPressed(color)
        morph(longRoundedSquare)
    }
}