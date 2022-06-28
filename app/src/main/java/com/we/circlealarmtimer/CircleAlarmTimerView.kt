/*
 *
 *  * Copyright 2015 Jiahuan
 *  * Copyright 2016 yinglan
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */
package com.we.circlealarmtimer

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.math.RoundingMode
import java.text.DecimalFormat

class CircleAlarmTimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // Paint
    //背景圆的画笔
    private var mCirclePaint: Paint? = null

    //刻度线的画笔
    private var mHighlightLinePaint: Paint? = null

    //绘制弧度的画笔
    private var mArcTrackPaint: Paint? = null

    //滑块2的画笔
    private var mCircleEndButtonPaint: Paint? = null

    //滑块1的画笔
    private var mCircleStartButtonPaint: Paint? = null

    //文字画笔
    private var mTimerTextPaint: Paint? = null

    // Dimension
    //间隙，一个标准距离
    private var mGapBetweenCircleAndLine = 0f

    //刻度线的宽度
    private var mLineWidth = 0f

    //滑块的半径
    private var mCircleButtonRadius = 0f

    //圆环轨迹的宽度
    private var mCircleStrokeWidth = 0f

    //文字大小
    private var mTimerTextSize = 0f

    //刻度线数目
    private var lineNumber = 0f

    //刻度线长度
    private var mLineLength = 0f

    // Color
    //圆的颜色
    private var mCircleColor = 0

    //滑块颜色
    private var mCircleButtonColor = 0

    //轨道颜色
    private var mArcTrackColor = 0

    //刻度线颜色
    private var mHighlightLineColor = 0

    //文字颜色
    private var mTimerTextColor = 0

    // Parameters
    private var mCx = 0f
    private var mCy = 0f
    private var mRadius = 0f
    private var mEndCurrentRadian = 0f
    private var mStartCurrentRadian = 0f
    private var mPreRadian = 0f
    private var mInCircleButton = false
    private var mInCircleButton1 = false
    private var ismInCircleButton = false

    //开始时间
    private var mStartCurrentTime = 0.0

    //结束时间
    private var mEndCurrentTime = 0.0
    private var mListener: OnTimeChangedListener? = null
    private var openTime = 0f
    private var closeTime = 0f

    //单位弧度
    private var unit = 0.0

    companion object {
        private const val TAG = "CircleTimerView"

        // Status
        private const val INSTANCE_STATUS = "instance_status"
        private const val STATUS_RADIAN = "status_radian"

        // Default dimension in dp/pt
        private const val DEFAULT_GAP_BETWEEN_CIRCLE_AND_LINE = 30f

        //刻度线宽度
        private const val DEFAULT_LINE_WIDTH = 2f

        //滑块的半径
        private const val DEFAULT_CIRCLE_BUTTON_RADIUS = 10f

        //圆环轨迹的宽度
        private const val DEFAULT_CIRCLE_STROKE_WIDTH = 5f

        //绘制的文字大小
        private const val DEFAULT_TIMER_TEXT_SIZE = 18f

        //刻度线长度
        private const val DEFAULT_LINE_LENGTH = 14f

        // Default color
        //背景颜色
        private var DEFAULT_CIRCLE_COLOR = Color.parseColor("#66ffffff")

        //滑块2的填充颜色
        private const val DEFAULT_CIRCLE_BUTTON_COLOR_2 = Color.WHITE

        //弧度轨道颜色
        private var DEFAULT_LINE_COLOR = Color.parseColor("#FFF7CD5B")

        //刻度线的颜色
        private const val DEFAULT_HIGHLIGHT_LINE_COLOR = Color.WHITE

        //滑块1颜色
        private const val DEFAULT_CIRCLE_BUTTON_COLOR_1 = Color.WHITE

        //文字颜色
        private var DEFAULT_TIMER_TEXT_COLOR = Color.parseColor("#822F7466")

        //绘制矩形区域
        lateinit var rect: RectF
    }

    init {

        // Set default dimension or read xml attributes
        initDefaultParameter(context)
        val typedArray: TypedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CircleAlarmTimerView, 0, 0)
        mCircleColor = typedArray.getColor(
            R.styleable.CircleAlarmTimerView_catv_bg_color,
            DEFAULT_CIRCLE_COLOR
        )
        mTimerTextColor = typedArray.getColor(
            R.styleable.CircleAlarmTimerView_catv_text_color,
            DEFAULT_TIMER_TEXT_COLOR
        )
        mTimerTextSize = typedArray.getDimension(
            R.styleable.CircleAlarmTimerView_catv_text_size,
            mTimerTextSize
        )

        mCircleButtonColor = typedArray.getColor(
            R.styleable.CircleAlarmTimerView_catv_circle_button_color,
            DEFAULT_CIRCLE_BUTTON_COLOR_2
        )
        mArcTrackColor = typedArray.getColor(
            R.styleable.CircleAlarmTimerView_catv_arc_track_color,
            DEFAULT_LINE_COLOR
        )
        mHighlightLineColor = typedArray.getColor(
            R.styleable.CircleAlarmTimerView_catv_line_color,
            DEFAULT_HIGHLIGHT_LINE_COLOR
        )
        typedArray.recycle()
        initialize()
    }

    private fun initDefaultParameter(context: Context) {
        mTimerTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TIMER_TEXT_SIZE,
            context.resources.displayMetrics
        )
        mGapBetweenCircleAndLine = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, DEFAULT_GAP_BETWEEN_CIRCLE_AND_LINE,
            context.resources.displayMetrics
        )
        mLineWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, DEFAULT_LINE_WIDTH, context.resources
                .displayMetrics
        )
        mCircleButtonRadius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CIRCLE_BUTTON_RADIUS, context
                .resources.displayMetrics
        )
        mCircleStrokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, DEFAULT_CIRCLE_STROKE_WIDTH, context
                .resources.displayMetrics
        )
        mLineLength = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, DEFAULT_LINE_LENGTH, context.resources
                .displayMetrics
        )
        lineNumber = 48f
        unit = 2 * Math.PI / lineNumber
    }

    private fun initialize() {
        Log.d(TAG, "initialize")
        // Init all paints
        mCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCircleEndButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mHighlightLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mArcTrackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTimerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCircleStartButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // CirclePaint
        mCirclePaint!!.color = mCircleColor
        mCirclePaint!!.style = Paint.Style.FILL

        // CircleButtonPaint
        mCircleEndButtonPaint!!.color = mCircleButtonColor
        mCircleEndButtonPaint!!.isAntiAlias = true
        mCircleEndButtonPaint!!.style = Paint.Style.FILL

        // LinePaint
        mArcTrackPaint!!.color = mArcTrackColor
        mArcTrackPaint!!.strokeWidth = mCircleStrokeWidth
        mArcTrackPaint!!.style = Paint.Style.STROKE

        // HighlightLinePaint
        mHighlightLinePaint!!.color = mHighlightLineColor
        mHighlightLinePaint!!.strokeWidth = mLineWidth
        mHighlightLinePaint!!.color = Color.parseColor("#FFFFFFFF")

        // TimerTextPaint
        mTimerTextPaint!!.color = mTimerTextColor
        mTimerTextPaint!!.textSize = mTimerTextSize
        mTimerTextPaint!!.textAlign = Paint.Align.CENTER

        // TimerColonPaint
        mCircleStartButtonPaint!!.color = DEFAULT_CIRCLE_BUTTON_COLOR_1
        mCircleStartButtonPaint!!.textAlign = Paint.Align.CENTER


    }

    override fun onDraw(canvas: Canvas) {
        //画外环
        canvas.drawCircle(mCx, mCy, mRadius - mGapBetweenCircleAndLine * 2, mCirclePaint!!)
        canvas.save()
        //绘制刻度和文字
        drawLineText(canvas)
        //旋转画布
        canvas.rotate(-90f, mCx, mCy)
        val rectDistance = (mRadius - mGapBetweenCircleAndLine * 2)
        rect = RectF(
            mCx - rectDistance,
            mCy - rectDistance,
            mCx + rectDistance,
            mCy + rectDistance
        )
        if (mStartCurrentRadian > mEndCurrentRadian) {
            canvas.drawArc(
                rect,
                Math.toDegrees(mStartCurrentRadian.toDouble()).toFloat(),
                Math.toDegrees((2 * Math.PI.toFloat()).toDouble())
                    .toFloat() - Math.toDegrees(mStartCurrentRadian.toDouble())
                    .toFloat() + Math.toDegrees(mEndCurrentRadian.toDouble())
                    .toFloat(),
                false,
                mArcTrackPaint!!
            )
        } else {
            canvas.drawArc(
                rect,
                Math.toDegrees(mStartCurrentRadian.toDouble()).toFloat(),
                Math.toDegrees(mEndCurrentRadian.toDouble())
                    .toFloat() - Math.toDegrees(mStartCurrentRadian.toDouble()).toFloat(),
                false,
                mArcTrackPaint!!
            )
        }
        canvas.restore()
        canvas.save()
        val centerY = measuredHeight / 2 - mRadius + mGapBetweenCircleAndLine * 2
        //通过不同条件绘制弧度
        drawArcTrack(canvas, centerY)
        canvas.restore()
        canvas.save()
        super.onDraw(canvas)
    }

    /**
     * 绘制圆弧
     */
    private fun drawArcTrack(canvas: Canvas, centerY: Float) {
        if (ismInCircleButton) {
            canvas.rotate(Math.toDegrees(mEndCurrentRadian.toDouble()).toFloat(), mCx, mCy)
            canvas.drawCircle(
                mCx,
                centerY,
                mCircleButtonRadius,
                mCircleEndButtonPaint!!
            )
            canvas.restore()
            canvas.save()
            canvas.rotate(Math.toDegrees(mStartCurrentRadian.toDouble()).toFloat(), mCx, mCy)
            canvas.drawCircle(
                mCx,
                centerY,
                mCircleButtonRadius,
                mCircleStartButtonPaint!!
            )
        } else {
            canvas.rotate(Math.toDegrees(mStartCurrentRadian.toDouble()).toFloat(), mCx, mCy)
            canvas.drawCircle(
                mCx,
                centerY,
                mCircleButtonRadius,
                mCircleStartButtonPaint!!
            )
            canvas.restore()
            canvas.save()
            canvas.rotate(Math.toDegrees(mEndCurrentRadian.toDouble()).toFloat(), mCx, mCy)
            canvas.drawCircle(
                mCx,
                centerY,
                mCircleButtonRadius,
                mCircleEndButtonPaint!!
            )
        }
    }

    private fun drawLineText(canvas: Canvas) {
        //刻度线起始点
        val startPoint =
            measuredWidth / 2 - mRadius + mCircleStrokeWidth / 2 + mGapBetweenCircleAndLine
        //刻度线结束点
        val endPoint = startPoint + mLineLength
        //文字旋转的轴坐标
        val textRotatePoint =
            measuredHeight / 2 - mRadius + mCircleStrokeWidth / 2 + mGapBetweenCircleAndLine / 2

        // 绘制外围表针和时钟数字
        var i = 0
        while (i < lineNumber) {
            canvas.save()
            canvas.rotate(360 / lineNumber * i, mCx, mCy)
            mHighlightLinePaint?.color = Color.parseColor("#66FFFFFF")
            canvas.drawLine(mCx, startPoint, mCy, endPoint, mHighlightLinePaint!!)
            if (i % 12 == 0) {
                mHighlightLinePaint?.color = mHighlightLineColor
                canvas.drawLine(mCx, startPoint, mCy, endPoint, mHighlightLinePaint!!)
                when (i) {
                    12 -> {
                        canvas.rotate(-90f, mCx, textRotatePoint)
                        canvas.drawText(
                            (i / 2).toString(), mCx, textRotatePoint + 20, mTimerTextPaint!!
                        )
                        canvas.rotate(90f, mCx, textRotatePoint)
                    }
                    24 -> {
                        canvas.rotate(-180f, mCx, textRotatePoint)
                        canvas.drawText(
                            (i / 2).toString(), mCx, textRotatePoint + 20, mTimerTextPaint!!
                        )
                        canvas.rotate(180f, mCx, textRotatePoint)
                    }
                    36 -> {
                        canvas.rotate(90f, mCx, textRotatePoint)
                        canvas.drawText(
                            (i / 2).toString(), mCx, textRotatePoint + 20, mTimerTextPaint!!
                        )
                        canvas.rotate(-90f, mCx, textRotatePoint)
                    }
                    else -> {
                        canvas.drawText(
                            (i / 2).toString(), mCx, textRotatePoint + 20, mTimerTextPaint!!
                        )
                    }
                }
            }
            canvas.restore()
            i++
        }
        canvas.save()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and event.actionMasked) {
            MotionEvent.ACTION_DOWN ->                 // If the point in the circle button
                if (mInCircleButton(event.x, event.y) && isEnabled) {
                    mInCircleButton = true
                    ismInCircleButton = false
                    mPreRadian = getRadian(event.x, event.y)
                    Log.d(TAG, "In circle button")
                } else if (mInCircleButton1(event.x, event.y) && isEnabled) {
                    mInCircleButton1 = true
                    ismInCircleButton = true
                    mPreRadian = getRadian(event.x, event.y)
                }
            MotionEvent.ACTION_MOVE -> if (mInCircleButton && isEnabled) {
                Log.d(TAG, "瞬间按下点的弧度$mPreRadian")
                val temp = getRadian(event.x, event.y)
                Log.d(TAG, "移动后的点的弧度" + temp + "关点弧度" + closeTime)
                if (mPreRadian > Math.toRadians(270.0) && temp < Math.toRadians(90.0)) {
                    mPreRadian -= (2 * Math.PI).toFloat()
                } else if (mPreRadian < Math.toRadians(90.0) && temp > Math.toRadians(270.0)) {
                    mPreRadian = (temp + (temp - 2 * Math.PI) - mPreRadian).toFloat()
                }
                mEndCurrentRadian += temp - mPreRadian
                Log.d(TAG, "变更后的当前结束弧度：$mEndCurrentRadian")
                mPreRadian = temp
                if (mEndCurrentRadian > 2 * Math.PI) {
                    mEndCurrentRadian -= (2 * Math.PI).toFloat()
                }
                if (mEndCurrentRadian < 0) {
                    mEndCurrentRadian += (2 * Math.PI).toFloat()
                }
                if (mEndCurrentRadian > closeTime) {
                    mEndCurrentRadian = closeTime
                }
                if (mEndCurrentRadian < mStartCurrentRadian + unit) {
                    mEndCurrentRadian = (mStartCurrentRadian + unit).toFloat()
                }
                if (mStartCurrentRadian + unit < mEndCurrentRadian) {
                    invalidate()
                }
            } else if (mInCircleButton1 && isEnabled) {
                val temp = getRadian(event.x, event.y)
                if (mPreRadian > Math.toRadians(270.0) && temp < Math.toRadians(90.0)) {
                    mPreRadian -= (2 * Math.PI).toFloat()
                } else if (mPreRadian < Math.toRadians(90.0) && temp > Math.toRadians(270.0)) {
                    mPreRadian = (temp + (temp - 2 * Math.PI) - mPreRadian).toFloat()
                }
                mStartCurrentRadian += temp - mPreRadian
                mPreRadian = temp
                if (mStartCurrentRadian > 2 * Math.PI) {
                    mStartCurrentRadian -= (2 * Math.PI).toFloat()
                }
                if (mStartCurrentRadian < 0) {
                    mStartCurrentRadian += (2 * Math.PI).toFloat()
                }
                if (mStartCurrentRadian < openTime) {
                    mStartCurrentRadian = openTime
                }
                if (mEndCurrentRadian - unit < mStartCurrentRadian) {
                    mStartCurrentRadian = (mEndCurrentRadian - unit).toFloat()
                }
                if (mStartCurrentRadian < mEndCurrentRadian - unit) {
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mInCircleButton && isEnabled) {
                    mInCircleButton = false
                    mEndCurrentRadian = verifyCurrentRadian(mEndCurrentRadian)
                    mEndCurrentTime = saveTwo(mEndCurrentRadian / (Math.PI * 2) * 24)
                } else if (mInCircleButton1 && isEnabled) {
                    mInCircleButton1 = false
                    mStartCurrentRadian = verifyCurrentRadian(mStartCurrentRadian)
                    mStartCurrentTime = saveTwo(mStartCurrentRadian / (Math.PI * 2) * 24)
                }
                if (null != mListener) {
                    if (ismInCircleButton) {
                        mListener!!.start(getTime(mStartCurrentTime))
                    } else {
                        mListener!!.end(getTime(mEndCurrentTime))
                    }
                }
                Log.d(TAG, closeTime.toString() + "营业时间one" + openTime)
                Log.d(TAG, mEndCurrentRadian.toString() + "营业时间two" + mStartCurrentRadian)
                invalidate()
            }
        }
        return true
    }

    //请求父控件不要拦截事件分发
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (mInCircleButton(event.x, event.y) && isEnabled ||
            mInCircleButton1(event.x, event.y) && isEnabled
        ) {
            parent.requestDisallowInterceptTouchEvent(true)
        }
        return super.dispatchTouchEvent(event)
    }

    private fun getHour(time: Double): String {
        val df = DecimalFormat("#")
        df.roundingMode = RoundingMode.HALF_DOWN
        return df.format(time)
    }

    private fun saveTwo(time: Double): Double {
        val df = DecimalFormat("##.##")
        df.roundingMode = RoundingMode.HALF_DOWN
        return df.format(time).toDouble()
    }

    private fun getTime(mStartCurrentTime: Double): String {
        val hour = getHour(mStartCurrentTime).toInt()
        return if (mStartCurrentTime - hour < 0.25) {
            "$hour:00"
        } else if (mStartCurrentTime - hour > 0.25 && mStartCurrentTime - hour < 0.75) {
            "$hour:30"
        } else {
            "${hour + 1}:00"
        }
    }

    //移动到正点的宽度线上
    private fun verifyCurrentRadian(mEndCurrentRadian: Float): Float {
        val lastCurrentRadian: Float
        //计算差值
        val differenceUnit = mEndCurrentRadian % unit
        lastCurrentRadian = if (differenceUnit > unit / 2) {
            (mEndCurrentRadian - differenceUnit + unit).toFloat()
        } else {
            (mEndCurrentRadian - differenceUnit).toFloat()
        }
        return lastCurrentRadian
    }

    /**
     * 设置开始时间
     *
     * @param startTime 9:30 或 9：00
     */
    fun setStartTime(startTime: String) {
        val split = startTime.split(":").toTypedArray()
        val hour = split[0].toInt()
        val minute = split[1].toInt()
        openTime = ((hour * 2 + minute % 30) * unit).toFloat()
        mStartCurrentRadian = openTime
        mStartCurrentTime = saveTwo(openTime / (Math.PI * 2) * 24)
        invalidate()
    }

    /**
     * 设置开始时间
     *
     * @param endTime 18:00
     */
    fun setEndTime(endTime: String) {
        val split = endTime.split(":").toTypedArray()
        val hour = split[0].toInt()
        val minute = split[1].toInt()
        closeTime = ((hour * 2 + minute % 30) * unit).toFloat()
        mEndCurrentRadian = closeTime
        mEndCurrentTime = saveTwo(closeTime / (Math.PI * 2) * 24)
        invalidate()
    }

    // Whether the down event inside circle button
    private fun mInCircleButton1(x: Float, y: Float): Boolean {
        val r = mRadius - mGapBetweenCircleAndLine * 2
        val x2 = (mCx + r * Math.sin(mStartCurrentRadian.toDouble())).toFloat()
        val y2 = (mCy - r * Math.cos(mStartCurrentRadian.toDouble())).toFloat()
        return Math.sqrt(((x - x2) * (x - x2) + (y - y2) * (y - y2)).toDouble()) < mCircleButtonRadius
    }

    // Whether the down event inside circle button
    private fun mInCircleButton(x: Float, y: Float): Boolean {
        val r = mRadius - mGapBetweenCircleAndLine * 2
        val x2 = (mCx + r * Math.sin(mEndCurrentRadian.toDouble())).toFloat()
        val y2 = (mCy - r * Math.cos(mEndCurrentRadian.toDouble())).toFloat()
        return Math.sqrt(((x - x2) * (x - x2) + (y - y2) * (y - y2)).toDouble()) < mCircleButtonRadius
    }

    // Use tri to cal radian
    private fun getRadian(x: Float, y: Float): Float {
        var alpha = Math.atan(((x - mCx) / (mCy - y)).toDouble()).toFloat()
        // Quadrant
        if (x > mCx && y > mCy) {
            // 2
            alpha += Math.PI.toFloat()
        } else if (x < mCx && y > mCy) {
            // 3
            alpha += Math.PI.toFloat()
        } else if (x < mCx && y < mCy) {
            // 4
            alpha = (2 * Math.PI + alpha).toFloat()
        }
        return alpha
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(TAG, "onMeasure")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // Ensure width = height
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        mCx = (width / 2).toFloat()
        mCy = (height / 2).toFloat()
        // Radius
        mRadius = if (mGapBetweenCircleAndLine + mCircleStrokeWidth >= mCircleButtonRadius) {
            width / 2 - mCircleStrokeWidth / 2
        } else {
            width / 2 - (mCircleButtonRadius - mGapBetweenCircleAndLine - mCircleStrokeWidth / 2)
        }
        setMeasuredDimension(width, height)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(INSTANCE_STATUS, super.onSaveInstanceState())
        bundle.putFloat(STATUS_RADIAN, mEndCurrentRadian)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            val bundle = state
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATUS))
            mEndCurrentRadian = bundle.getFloat(STATUS_RADIAN)
            mEndCurrentTime = 60 / (2 * Math.PI) * mEndCurrentRadian * 60
            return
        }
        super.onRestoreInstanceState(state)
    }

    fun setOnTimeChangedListener(listener: OnTimeChangedListener?) {
        if (null != listener) {
            mListener = listener
        }
    }

    interface OnTimeChangedListener {
        fun start(starting: String?)
        fun end(ending: String?)
    }

}