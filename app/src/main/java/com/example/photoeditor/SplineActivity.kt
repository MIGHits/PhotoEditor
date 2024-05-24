package com.example.photoeditor

import android.content.ContentValues
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.Toast
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope

var isRemovingEnabled = false
var isMovingEnabled = false

class SplineActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var splineButton: ImageButton
    private lateinit var clearButton: ImageButton
    private lateinit var saveButton: ImageButton
    private lateinit var removeDotsButton: ImageButton
    private lateinit var moveDotsButton: ImageButton

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
        removeDotsButton = findViewById(R.id.removeDotsButton)
        moveDotsButton = findViewById(R.id.moveDotsButton)

        clearButton.setOnClickListener {
            drawingView.clear()
            drawingView.setDrawingEnabled(true)
            isRemovingEnabled = false
            isMovingEnabled = false
        }

        splineButton.setOnClickListener {
            if (drawingView.canMakeSpline()) {
                drawingView.setDrawingEnabled(false)
                drawingView.setDrawSplineFlag(true)
            } else {
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
            lifecycleScope.launch {
                saveImageToMediaStore(antiAliasing(bitmap))
            }
        }

        removeDotsButton.setOnClickListener {
            if (isMovingEnabled) {
                Toast.makeText(this, "Сейчас Вы не можете удалять точки", Toast.LENGTH_SHORT).show()
            } else {
                if (isRemovingEnabled) {
                    isRemovingEnabled = false
                    Toast.makeText(this, "Выключен режим удаления точек", Toast.LENGTH_SHORT).show()
                } else {
                    isRemovingEnabled = true
                    Toast.makeText(this, "Включен режим удаления точек", Toast.LENGTH_SHORT).show()
                }
            }
        }

        moveDotsButton.setOnClickListener {
            if (isRemovingEnabled) {
                Toast.makeText(this, "Сейчас Вы не можете перемещать точки", Toast.LENGTH_SHORT)
                    .show()
            }
            else {
                if (isMovingEnabled) {
                    isMovingEnabled = false
                    Toast.makeText(this, "Выключен режим перемещения точек", Toast.LENGTH_SHORT)
                        .show()
                }
                else {
                    isMovingEnabled = true
                    Toast.makeText(this, "Включен режим перемещения точек", Toast.LENGTH_SHORT)
                        .show()
                }
            }
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
            Toast.makeText(this, "Изображение сохранено", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Не удалось сохранить изображение", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun antiAliasing(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
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
            }
        }

        resultBitmap
    }
}
