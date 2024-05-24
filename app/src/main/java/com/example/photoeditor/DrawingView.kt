package com.example.photoeditor

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.absoluteValue

fun catmullRom(points: List<PointF>, t: Float): PointF {
    val p0 = points[0]
    val p1 = points[1]
    val p2 = points[2]
    val p3 = points[3]

    val t2 = t * t
    val t3 = t * t * t

    val x = 0.5f * ((2f * p1.x) +
            (-p0.x + p2.x) * t +
            (2f * p0.x - 5f * p1.x + 4f * p2.x - p3.x) * t2 +
            (-p0.x + 3f * p1.x - 3f * p2.x + p3.x) * t3)

    val y = 0.5f * ((2f * p1.y) +
            (-p0.y + p2.y) * t +
            (2f * p0.y - 5f * p1.y + 4f * p2.y - p3.y) * t2 +
            (-p0.y + 3f * p1.y - 3f * p2.y + p3.y) * t3)

    return PointF(x, y)
}

class DrawingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var drawSplineFlag = false
    private var isDrawingEnabled = true
    private val pointRadius = 20f
    private val touchRadius = 45f
    private var selectedPoint: PointF? = null

    private val pointPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val linePaint = Paint().apply {
        color = Color.parseColor("#38b3e3")
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }

    private val spline = Paint().apply {
        color = Color.parseColor("#38b3e3")
        style = Paint.Style.STROKE
        strokeWidth = 10f
    }
    private var points = mutableListOf<PointF>()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.parseColor("#dddddd"))

        drawPoints(canvas, points)

        if (points.size > 1 && !drawSplineFlag) {
            for (i in 0 until points.size - 1) {
                canvas.drawLine(points[i].x, points[i].y, points[i + 1].x, points[i + 1].y, linePaint)
            }
        }

        if (drawSplineFlag) {
            drawSpline(canvas)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isRemovingEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val point = getPointAt(event.x, event.y)
                    if (point != null) {
                        points.remove(point)
                        invalidate()
                        return true
                    }
                }
            }
            return false
        }

        if (isMovingEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    selectedPoint = getPointAt(event.x, event.y)
                    return selectedPoint != null
                }
                MotionEvent.ACTION_MOVE -> {
                    selectedPoint?.let {
                        it.x = event.x
                        it.y = event.y
                        invalidate()
                        return true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    selectedPoint = null
                    return true
                }
            }
            return false
        }

        if (isDrawingEnabled) {
            return when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    addPoint(event.x, event.y)
                    true
                }

                else -> false
            }
        }

        return super.onTouchEvent(event)
    }

    private fun drawPoints(canvas: Canvas, points: MutableList<PointF>) {
        points.forEach { point ->
            canvas.drawCircle(point.x, point.y, pointRadius, pointPaint)
        }
    }

    fun setDrawingEnabled(flag: Boolean) {
        isDrawingEnabled = flag
    }

    fun setDrawSplineFlag(flag: Boolean) {
        drawSplineFlag = flag
        invalidate()
    }

    fun clear() {
        points.clear()
        drawSplineFlag = false
        invalidate()
    }

    fun canMakeSpline(): Boolean {
        return points.size > 2
    }

    private fun addPoint(x: Float, y: Float) {
        points.add(PointF(x, y))
        invalidate()
    }

    private fun getPointAt(x: Float, y: Float): PointF? {
        return points.find { (it.x - x).absoluteValue < touchRadius && (it.y - y).absoluteValue < touchRadius }
    }

    private fun drawSpline(canvas: Canvas) {
        if (points.size < 3) return

        val newPoints = mutableListOf<PointF>()
        newPoints.add(points[0])
        newPoints.add(points[0])
        newPoints.addAll(points)
        newPoints.add(points.last())
        newPoints.add(points.last())

        for (i in 0 until newPoints.size - 3) {
            var previousPoint = catmullRom(newPoints.subList(i, i + 4), 0f)
            for (t in 1..100) {
                val point = catmullRom(newPoints.subList(i, i + 4), t / 100f)
                canvas.drawLine(previousPoint.x, previousPoint.y, point.x, point.y, spline)
                previousPoint = point
            }
        }

        drawPoints(canvas, points)
    }
}