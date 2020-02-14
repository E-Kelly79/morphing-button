package com.routematch.morphingbutton

import android.graphics.drawable.GradientDrawable

class StrokeGradientDrawable(gradientDrawable: Any?) {
    private var mStrokeWidth = 0
    private var mStrokeColor = 0

    private var mGradientDrawable: GradientDrawable? = null
    private var mRadius = 0f
    private var mColor = 0

    fun StrokeGradientDrawable(drawable: GradientDrawable?) {
        mGradientDrawable = drawable
    }

    fun getStrokeWidth(): Int {
        return mStrokeWidth
    }

    fun setStrokeWidth(strokeWidth: Int) {
        mStrokeWidth = strokeWidth
        mGradientDrawable!!.setStroke(strokeWidth, getStrokeColor())
    }

    fun getStrokeColor(): Int {
        return mStrokeColor
    }

    fun setStrokeColor(strokeColor: Int) {
        mStrokeColor = strokeColor
        mGradientDrawable!!.setStroke(getStrokeWidth(), strokeColor)
    }

    fun setCornerRadius(radius: Float) {
        mRadius = radius
        mGradientDrawable!!.cornerRadius = radius
    }

    fun setColor(color: Int) {
        mColor = color
        mGradientDrawable!!.setColor(color)
    }

    fun getColor(): Int {
        return mColor
    }

    fun getRadius(): Float {
        return mRadius
    }

    fun getGradientDrawable(): GradientDrawable? {
        return mGradientDrawable
    }
}