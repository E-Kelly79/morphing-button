package com.routematch.morphingbutton

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Path.Direction.CW
import android.graphics.RectF
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import androidx.annotation.NonNull
import androidx.core.view.ViewCompat
import com.routematch.morphingbutton.R.color
import com.routematch.morphingbutton.RmMorphingAnimation.Listener

class RmProgressButton(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : RmMorphingButton(context, attrs, defStyleAttr) {
    private var mColor1 = 0
    private var mColor2 = 0
    private var mColor3 = 0
    private var mColor4 = 0
    private var mProgressCornerRadius = 0
    private var mProgressBar: ProgressBar? = null

    private var mIsRunning = false


    private fun init(context: Context) {
        val res = context.resources
        mColor1 = res.getColor(color.mb_gray)
        mColor2 = res.getColor(color.mb_blue)
        mColor3 = res.getColor(color.mb_gray)
        mColor4 = res.getColor(color.mb_blue)
        if (VERSION.SDK_INT <= VERSION_CODES.JELLY_BEAN_MR2) { // clipPath only available on hardware for 18+
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
    }

    override fun onDraw(@NonNull canvas: Canvas) {
        super.onDraw(canvas)
        if (!mAnimationInProgress && mIsRunning) {
            if (mProgressBar == null) {
                mProgressBar = ProgressBar(this)
                setupProgressBarBounds()
                mProgressBar!!.setColorScheme(mColor1, mColor2, mColor3, mColor4)
                mProgressBar!!.start()
            }
            mProgressBar!!.draw(canvas)
        }
    }

    @JvmName("name")
      fun morph(params: Params?) {
         super.morph(params!!)
        mIsRunning = false

    }

    fun morphToProgress(backgroundColor: Int, progressCornerRadius: Int, width: Int, height: Int, duration: Int,
        progressColor1: Int) {

        morphToProgress(backgroundColor, progressCornerRadius, width, height, duration, backgroundColor,
            progressColor1, backgroundColor, progressColor1)
    }

    fun morphToProgress(backgroundColor: Int, progressCornerRadius: Int, width: Int, height: Int, duration: Int,
        progressColor1: Int, progressColor2: Int) {

        morphToProgress(backgroundColor, progressCornerRadius, width, height, duration, progressColor1,
            progressColor2, progressColor1, progressColor2)
    }

    fun morphToProgress(backgroundColor: Int, progressCornerRadius: Int, width: Int, height: Int, duration: Int,
        progressColor1: Int, progressColor2: Int, progressColor3: Int, progressColor4: Int) {
        mProgressCornerRadius = progressCornerRadius
        mColor1 = progressColor1
        mColor2 = progressColor2
        mColor3 = progressColor3
        mColor4 = progressColor4
        val longRoundedSquare: Params = Params.create()
            .duration(duration)
            .cornerRadius(mProgressCornerRadius)
            .width(width)
            .height(height)
            .color(backgroundColor)
            .colorPressed(backgroundColor)
            .animationListener(object : Listener {
                override fun onAnimationEnd() {
                    mIsRunning = true
                    invalidate()
                }
            })
        morph(longRoundedSquare)
    }

    private fun setupProgressBarBounds() {
        val indicatorHeight: Int = height
        val bottom = (measuredHeight - indicatorHeight) as Int
        mProgressBar!!.setBounds(0, bottom, measuredWidth, measuredHeight, mProgressCornerRadius)
    }

    class ProgressBar(private val mParent: View) {
        private val mPaint = Paint()
        private val mClipRect = RectF()
        private var mTriggerPercentage = 0f
        private var mStartTime: Long = 0
        private var mFinishTime: Long = 0
        private var mRunning = false
        // Colors used when rendering the animation,
        private var mColor1: Int
        private var mColor2: Int
        private var mColor3: Int
        private var mColor4: Int
        private var mCornerRadius = 0
        private val mBounds = RectF()
        /**
         * Set the four colors used in the progress animation. The first color will
         * also be the color of the bar that grows in response to a user swipe
         * gesture.
         *
         * @param color1 Integer representation of a color.
         * @param color2 Integer representation of a color.
         * @param color3 Integer representation of a color.
         * @param color4 Integer representation of a color.
         */
        fun setColorScheme(color1: Int, color2: Int, color3: Int, color4: Int) {
            mColor1 = color1
            mColor2 = color2
            mColor3 = color3
            mColor4 = color4
        }

        /**
         * Start showing the progress animation.
         */
        fun start() {
            if (!mRunning) {
                mTriggerPercentage = 0f
                mStartTime = AnimationUtils.currentAnimationTimeMillis()
                mRunning = true
                mParent.postInvalidate()
            }
        }

        fun draw(canvas: Canvas) {
            val clipPath = Path()
            clipPath.addRoundRect(
                mBounds,
                mCornerRadius.toFloat(),
                mCornerRadius.toFloat(),
                CW
            )
            val width = mBounds.width().toInt()
            val height = mBounds.height().toInt()
            val cx = width / 2
            val cy = height / 2
            var drawTriggerWhileFinishing = false
            var restoreCount = canvas.save()
            canvas.clipPath(clipPath)
            if (mRunning || mFinishTime > 0) {
                val now = AnimationUtils.currentAnimationTimeMillis()
                val elapsed = (now - mStartTime) % ANIMATION_DURATION_MS
                val iterations = (now - mStartTime) / ANIMATION_DURATION_MS
                val rawProgress = elapsed / (ANIMATION_DURATION_MS / 100f)
                // If we're not running anymore, that means we're running through
// the finish animation.
                if (!mRunning) { // If the finish animation is done, don't draw anything, and
// don't repost.
                    if (now - mFinishTime >= FINISH_ANIMATION_DURATION_MS) {
                        mFinishTime = 0
                        return
                    }
                    // Otherwise, use a 0 opacity alpha layer to clear the animation
// from the inside out. This layer will prevent the circles from
// drawing within its bounds.
                    val finishElapsed =
                        (now - mFinishTime) % FINISH_ANIMATION_DURATION_MS
                    val finishProgress =
                        finishElapsed / (FINISH_ANIMATION_DURATION_MS / 100f)
                    val pct = finishProgress / 100f
                    // Radius of the circle is half of the screen.
                    val clearRadius =
                        width / 2 * INTERPOLATOR.getInterpolation(pct)
                    mClipRect[cx - clearRadius, 0f, cx + clearRadius] = height.toFloat()
                    canvas.saveLayerAlpha(mClipRect, 0, 0)
                    // Only draw the trigger if there is a space in the center of
// this refreshing view that needs to be filled in by the
// trigger. If the progress view is just still animating, let it
// continue animating.
                    drawTriggerWhileFinishing = true
                }
                // First fill in with the last color that would have finished drawing.
                if (iterations == 0L) {
                    canvas.drawColor(mColor1)
                } else {
                    if (rawProgress >= 0 && rawProgress < 25) {
                        canvas.drawColor(mColor4)
                    } else if (rawProgress >= 25 && rawProgress < 50) {
                        canvas.drawColor(mColor1)
                    } else if (rawProgress >= 50 && rawProgress < 75) {
                        canvas.drawColor(mColor2)
                    } else {
                        canvas.drawColor(mColor3)
                    }
                }
                // Then draw up to 4 overlapping concentric circles of varying radii, based on how far
// along we are in the cycle.
// progress 0-50 draw mColor2
// progress 25-75 draw mColor3
// progress 50-100 draw mColor4
// progress 75 (wrap to 25) draw mColor1
                if (rawProgress in 0.0..25.0) {
                    val pct = (rawProgress + 25) * 2 / 100f
                    drawCircle(canvas, cx.toFloat(), cy.toFloat(), mColor1, pct)
                }
                if (rawProgress in 0.0..50.0) {
                    val pct = rawProgress * 2 / 100f
                    drawCircle(canvas, cx.toFloat(), cy.toFloat(), mColor2, pct)
                }
                if (rawProgress in 25.0..75.0) {
                    val pct = (rawProgress - 25) * 2 / 100f
                    drawCircle(canvas, cx.toFloat(), cy.toFloat(), mColor3, pct)
                }
                if (rawProgress in 50.0..100.0) {
                    val pct = (rawProgress - 50) * 2 / 100f
                    drawCircle(canvas, cx.toFloat(), cy.toFloat(), mColor4, pct)
                }
                if (rawProgress in 75.0..100.0) {
                    val pct = (rawProgress - 75) * 2 / 100f
                    drawCircle(canvas, cx.toFloat(), cy.toFloat(), mColor1, pct)
                }
                if (mTriggerPercentage > 0 && drawTriggerWhileFinishing) { // There is some portion of trigger to draw. Restore the canvas,
// then draw the trigger. Otherwise, the trigger does not appear
// until after the bar has finished animating and appears to
// just jump in at a larger width than expected.
                    canvas.restoreToCount(restoreCount)
                    restoreCount = canvas.save()
                    canvas.clipPath(clipPath)
                    drawTrigger(canvas, cx, cy)
                }
                // Keep running until we finish out the last cycle.
                ViewCompat.postInvalidateOnAnimation(mParent)
            } else { // Otherwise if we're in the middle of a trigger, draw that.
                if (mTriggerPercentage > 0 && mTriggerPercentage <= 1.0) {
                    drawTrigger(canvas, cx, cy)
                }
            }
            canvas.restoreToCount(restoreCount)
        }

        private fun drawTrigger(canvas: Canvas, cx: Int, cy: Int) {
            mPaint.color = mColor1
            canvas.drawCircle(cx.toFloat(), cy.toFloat(), cx * mTriggerPercentage, mPaint)
        }

        /**
         * Draws a circle centered in the view.
         *
         * @param canvas the canvas to draw on
         * @param cx the center x coordinate
         * @param cy the center y coordinate
         * @param color the color to draw
         * @param pct the percentage of the view that the circle should cover
         */
        private fun drawCircle(
            canvas: Canvas,
            cx: Float,
            cy: Float,
            color: Int,
            pct: Float
        ) {
            mPaint.color = color
            canvas.save()
            canvas.translate(cx, cy)
            val radiusScale = INTERPOLATOR.getInterpolation(pct)
            canvas.scale(radiusScale, radiusScale)
            canvas.drawCircle(0f, 0f, cx, mPaint)
            canvas.restore()
        }

        /**
         * Set the drawing bounds of this SwipeProgressBar.
         */
        fun setBounds(left: Int, top: Int, right: Int, bottom: Int, cornerRadius: Int) {
            mBounds.left = left.toFloat()
            mBounds.top = top.toFloat()
            mBounds.right = right.toFloat()
            mBounds.bottom = bottom.toFloat()
            mCornerRadius = cornerRadius
        }

        companion object {
            // Default progress animation colors are grays.
            private const val COLOR1 = -0x4d000000
            private const val COLOR2 = -0x80000000
            private const val COLOR3 = 0x4d000000
            private const val COLOR4 = 0x1a000000
            // The duration of the animation cycle.
            private const val ANIMATION_DURATION_MS = 2000
            // The duration of the animation to clear the bar.
            private const val FINISH_ANIMATION_DURATION_MS = 1000
            // Interpolator for varying the speed of the animation.
            private val INTERPOLATOR: Interpolator = AccelerateDecelerateInterpolator()
        }

        init {
            mColor1 = COLOR1
            mColor2 = COLOR2
            mColor3 = COLOR3
            mColor4 = COLOR4
        }
    }
}