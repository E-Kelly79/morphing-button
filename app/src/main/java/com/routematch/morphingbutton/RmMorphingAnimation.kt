package com.routematch.morphingbutton

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams
import androidx.annotation.NonNull

class RmMorphingAnimation {

    interface Listener {
        fun onAnimationEnd()
    }

    companion object {
        class Params private constructor(@NonNull button: RmMorphingButton) {


            var fromCornerRadius = 0f
            var toCornerRadius = 0f
            var fromHeight = 0
            var toHeight = 0
            var fromWidth = 0
            var toWidth = 0
            var fromColor = 0
            var toColor = 0
            var duration = 0
            var fromStrokeWidth = 0
            var toStrokeWidth = 0
            var fromStrokeColor = 0
            var toStrokeColor = 0
            val button: RmMorphingButton
            var animationListener: Listener? = null

            fun duration(duration: Int): Params {
                this.duration = duration
                return this
            }

            fun listener(@NonNull animationListener: Listener?): Params {
                this.animationListener = animationListener
                return this
            }

            fun color(fromColor: Int, toColor: Int): Params {
                this.fromColor = fromColor
                this.toColor = toColor
                return this
            }

            fun cornerRadius(fromCornerRadius: Int, toCornerRadius: Int): Params {
                this.fromCornerRadius = fromCornerRadius.toFloat()
                this.toCornerRadius = toCornerRadius.toFloat()
                return this
            }

            fun height(fromHeight: Int, toHeight: Int): Params {
                this.fromHeight = fromHeight
                this.toHeight = toHeight
                return this
            }

            fun width(fromWidth: Int, toWidth: Int): Params {
                this.fromWidth = fromWidth
                this.toWidth = toWidth
                return this
            }

            fun strokeWidth(fromStrokeWidth: Int, toStrokeWidth: Int): Params {
                this.fromStrokeWidth = fromStrokeWidth
                this.toStrokeWidth = toStrokeWidth
                return this
            }

            fun strokeColor(fromStrokeColor: Int, toStrokeColor: Int): Params {
                this.fromStrokeColor = fromStrokeColor
                this.toStrokeColor = toStrokeColor
                return this
            }

            companion object {
                fun create(@NonNull button: RmMorphingButton): Params {
                    return Params(button)
                }
            }

            init {
                this.button = button
            }
        }
    }

    private var mParams: Params? = null

    fun RmMorphingAnimation(@NonNull params: Params?) {
        mParams = params
    }

    fun start() {
        val background: StrokeGradientDrawable = mParams!!.button.getDrawableNormal()!!
        val cornerAnimation =
            ObjectAnimator.ofFloat(background, "cornerRadius", mParams!!.fromCornerRadius, mParams!!.toCornerRadius)
        val strokeWidthAnimation =
            ObjectAnimator.ofInt(background, "strokeWidth", mParams!!.fromStrokeWidth, mParams!!.toStrokeWidth)
        val strokeColorAnimation =
            ObjectAnimator.ofInt(background, "strokeColor", mParams!!.fromStrokeColor, mParams!!.toStrokeColor)
        strokeColorAnimation.setEvaluator(ArgbEvaluator())
        val bgColorAnimation =
            ObjectAnimator.ofInt(background, "color", mParams!!.fromColor, mParams!!.toColor)
        bgColorAnimation.setEvaluator(ArgbEvaluator())
        val heightAnimation = ValueAnimator.ofInt(mParams!!.fromHeight, mParams!!.toHeight)
        heightAnimation.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams: LayoutParams = mParams!!.button.layoutParams
            layoutParams.height = `val`
            mParams!!.button.layoutParams = layoutParams
        }
        val widthAnimation = ValueAnimator.ofInt(mParams!!.fromWidth, mParams!!.toWidth)
        widthAnimation.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams: LayoutParams = mParams!!.button.layoutParams
            layoutParams.width = `val`
            mParams!!.button.layoutParams = layoutParams
        }
        val animatorSet = AnimatorSet()
        animatorSet.duration = mParams!!.duration.toLong()
        animatorSet.playTogether(
            strokeWidthAnimation, strokeColorAnimation, cornerAnimation, bgColorAnimation,
            heightAnimation, widthAnimation
        )
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (mParams!!.animationListener != null) {
                    mParams!!.animationListener!!.onAnimationEnd()
                }
            }
        })
        animatorSet.start()
    }
}