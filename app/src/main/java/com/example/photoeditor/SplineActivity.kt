package com.example.photoeditor

import android.content.ContentValues
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast

fun createBasicPolynomial(xValues: DoubleArray, i: Int): (Double) -> Double {
    fun basicPolynomial(x: Double): Double {
        var divider = 1.0
        xValues.forEachIndexed { index, value ->
            if (index != i) {
                divider *= (x - value)
            }
        }
        return divider
    }
    return ::basicPolynomial
}

fun createLagrangePolynomial(xValues: DoubleArray, yValues: DoubleArray): (Double) -> Double {
    val basicPolynomials = mutableListOf<(Double) -> Double>()

    xValues.forEachIndexed { index, _ ->
        basicPolynomials.add(createBasicPolynomial(xValues, index))
    }

    fun LagrangePolynomial(x: Double): Double {
        var result = 0.0
        yValues.forEachIndexed { index, value ->
            result += value * basicPolynomials[index](x) / basicPolynomials[index](xValues[index])
        }
        return result
    }

    return ::LagrangePolynomial
}


class DrawingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr)
{
    private var scaleFactor = 1f
    private var drawSplineFlag = false
    private var isDrawingEnabled = true
    private val pointRadius = 30f
    private val pointPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val spline = Paint().apply {
        color = Color.parseColor("#38b3e3")
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 30f
        alpha = 255
    }
    private var points = mutableListOf<PointF>()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.scale(scaleFactor, scaleFactor)

        points = points.sortedBy { it.x }.toMutableList()
        for (point in points) {
            canvas.drawCircle(point.x, point.y, pointRadius, pointPaint)
        }
        for (i in 0 until points.size - 1)
        {
            canvas.drawLine(points[i].x, points[i].y, points[i+1].x, points[i+1].y, spline)
        }
        if (drawSplineFlag)
        {
            canvas.drawRGB(221, 221, 221)
            invalidate()
            drawSpline(canvas)

        }
        canvas.restore()
    }

    fun setScaleFactor(factor: Float) {
        scaleFactor = factor
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (isDrawingEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    addPoint(event.x, event.y)
                    true
                }
                else -> false
            }
        } else {
            false
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

    fun canMakeSpline() : Boolean
    {
        return points.size > 2
    }

    private fun addPoint(x: Float, y: Float) {
        points.add(PointF(x, y))
        invalidate()
    }

    private fun drawSpline(canvas: Canvas) {
        val epsilon = 1e-6
        var xValues = doubleArrayOf()
        var yValues = doubleArrayOf()
        if (points.size > 2)
        {

            val sortedPoints = points.sortedBy { it.x }
            for (point in sortedPoints) {
                val xWithNoise = point.x + (Math.random() - 0.5) * epsilon
                xValues += xWithNoise
                yValues += point.y.toDouble()
            }
            val LagrangePolynomial = createLagrangePolynomial(xValues, yValues)

            var stepsList = doubleArrayOf()
            var yList = doubleArrayOf()
            stepsList = stepsList.plus(sortedPoints[0].x.toDouble())
            yList = yList.plus(LagrangePolynomial(sortedPoints[0].x.toDouble()))
            val step = 0.1
            var i = 0
            while (stepsList[i] + step <= sortedPoints[sortedPoints.size - 1].x) {
                stepsList = stepsList.plus(stepsList[i] + step)
                yList = yList.plus(LagrangePolynomial(stepsList[i] + step))
                i++
            }
            stepsList = stepsList.plus(sortedPoints[sortedPoints.size - 1].x.toDouble())
            yList = yList.plus(LagrangePolynomial(sortedPoints[sortedPoints.size - 1].x.toDouble()))

            for (i in 0 until stepsList.size - 1)
            {
                val x = (stepsList[i] + (width / 2).toFloat()).toFloat()
                val y = (yList[i] + (height / 2).toFloat()).toFloat()

                val nextX = (stepsList[i + 1] + (width / 2).toFloat()).toFloat()
                val nextY = (yList[i + 1] + (height / 2).toFloat()).toFloat()
                canvas.drawLine(x, y, nextX, nextY, spline)
            }

            for (i in xValues.indices)
            {
                canvas.drawCircle((xValues[i] + (width / 2)).toFloat(),
                    ((yValues[i] + (height / 2).toFloat()).toFloat()), pointRadius, pointPaint)
            }
        }
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var drawingView: DrawingView
    private lateinit var splineButton: Button
    private lateinit var clearButton: Button
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_spline)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        drawingView = findViewById(R.id.drawingView)
        clearButton = findViewById(R.id.clearButton)
        splineButton = findViewById(R.id.splineButton)
        saveButton = findViewById(R.id.saveButton)
        saveButton.isEnabled = false

        clearButton.setOnClickListener {
            drawingView.clear()
            splineButton.isEnabled = true
            drawingView.setDrawingEnabled(true)
            drawingView.setScaleFactor(1f)
            saveButton.isEnabled = false
        }

        splineButton.setOnClickListener {
            if (drawingView.canMakeSpline()) {
                splineButton.isEnabled = false
                drawingView.setDrawingEnabled(false)
                drawingView.setScaleFactor(0.5f)
                drawingView.setDrawSplineFlag(true)
                saveButton.isEnabled = true
            }
            else
            {
                Toast.makeText(this, "Поставьте как минимум 3 точки", Toast.LENGTH_SHORT).show()
            }
        }

        saveButton.setOnClickListener {
            val bitmap = Bitmap.createBitmap(
                drawingView.width,
                drawingView.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawingView.draw(canvas)
            saveImageToMediaStore(antiAliasing(bitmap))
        }

    }

    private fun saveImageToMediaStore(bitmap: Bitmap) {
        val contentResolver = contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "spline.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri: Uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return

        try {
            val outputStream = contentResolver.openOutputStream(imageUri)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            outputStream?.flush()
            outputStream?.close()
            Toast.makeText(this, "Изображение сохранено в MediaStore", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Не удалось сохранить изображение", Toast.LENGTH_SHORT).show()
        }
    }

    private fun antiAliasing(source: Bitmap) : Bitmap {
        val width = source.width
        val height = source.height
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width)
        {
            for (y in 0 until height)
            {
//                val pixel = source.getPixel(x, y)
//                if (Color.red(pixel) != 56 && Color.green(pixel) != 179 && Color.blue(pixel) != 227 &&
//                    Color.red(pixel) != 221 && Color.green(pixel) != 221 && Color.blue(pixel) != 221) {
                val neighbours = mutableListOf<PointF>()
                if ((x - 1) > 0 && (y - 1) > 0) {
                    neighbours.add(PointF((x - 1).toFloat(), (y - 1).toFloat()))
                }
                if ((y - 1) > 0) {
                    neighbours.add(PointF(x.toFloat(), (y - 1).toFloat()))
                }
                if ((x + 1) < width && (y - 1) > 0) {
                    neighbours.add(PointF((x + 1).toFloat(), (y - 1).toFloat()))
                }
                if ((x - 1) > 0) {
                    neighbours.add(PointF((x - 1).toFloat(), y.toFloat()))
                }
                if ((x + 1) < width) {
                    neighbours.add(PointF((x + 1).toFloat(), y.toFloat()))
                }
                if ((x - 1) > 0 && (y + 1) < height) {
                    neighbours.add(PointF((x - 1).toFloat(), (y + 1).toFloat()))
                }
                if ((y + 1) < height) {
                    neighbours.add(PointF(x.toFloat(), (y + 1).toFloat()))
                }
                if ((x + 1) < width && (y + 1) < height) {
                    neighbours.add(PointF((x + 1).toFloat(), (y + 1).toFloat()))
                }

                var red = 0
                var green = 0
                var blue = 0
                for (point in neighbours) {
                    val pixel = source.getPixel(point.x.toInt(), point.y.toInt())
                    red += Color.red(pixel)
                    green += Color.green(pixel)
                    blue += Color.blue(pixel)
                }
                red /= neighbours.size
                green /= neighbours.size
                blue /= neighbours.size
                val newPixel = Color.rgb(
                    red.coerceIn(0, 255),
                    green.coerceIn(0, 255),
                    blue.coerceIn(0, 255)
                )
                resultBitmap.setPixel(x, y, newPixel)
//                }
//                else
//                {
//                    resultBitmap.setPixel(x, y, pixel)
//                }
            }
        }

        return resultBitmap
    }
}