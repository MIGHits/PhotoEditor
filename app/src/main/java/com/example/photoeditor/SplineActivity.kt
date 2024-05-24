package com.example.photoeditor

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

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
        val currentDate = Date()
        val dateFormat: DateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val dateText = dateFormat.format(currentDate)
        val timeFormat: DateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val timeText = timeFormat.format(currentDate)

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "spline-$dateText-$timeText.jpg")
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
        val pixels = IntArray(width * height)
        val resultPixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val numCores = Runtime.getRuntime().availableProcessors()
        val chunkSize = ceil(height.toDouble() / numCores).toInt()

        val deferredResults = (0 until numCores).map { core ->
            async {
                val startY = core * chunkSize
                val endY = minOf(startY + chunkSize, height)
                for (y in startY until endY) {
                    for (x in 0 until width) {
                        val neighbours = getNeighbours(pixels, x, y, width, height)
                        var count = 0
                        for (pixel in neighbours) {
                            if (Color.red(pixel) != 221 && Color.green(pixel) != 221 &&
                                Color.blue(pixel) != 221) count++
                        }
                        if (count == 0) {
                            resultPixels[y * width + x] = pixels[y * width + x]
                            continue
                        }
                        val (r, g, b) = getAverageColor(neighbours)
                        resultPixels[y * width + x] = Color.rgb(r, g, b)
                    }
                }
            }
        }

        deferredResults.forEach { it.await() }

        resultBitmap.setPixels(resultPixels, 0, width, 0, 0, width, height)
        resultBitmap
    }

    private fun getNeighbours(pixels: IntArray, x: Int, y: Int, width: Int, height: Int): List<Int> {
        val neighbors = mutableListOf<Int>()

        for (j in (y - 1)..(y + 1)) {
            for (i in (x - 1)..(x + 1)) {
                if (i in 0 until width && j in 0 until height) {
                    neighbors.add(pixels[j * width + i])
                }
            }
        }
        return neighbors
    }

    private fun getAverageColor(pixels: List<Int>): Triple<Int, Int, Int> {
        var red = 0
        var green = 0
        var blue = 0
        for (pixel in pixels) {
            red += Color.red(pixel)
            green += Color.green(pixel)
            blue += Color.blue(pixel)
        }

        return Triple(red / pixels.size, green / pixels.size, blue / pixels.size)
    }
}
