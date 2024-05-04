package com.example.photoeditor

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.*
import kotlin.math.*

class SaturationFilter {
    companion object {
        suspend fun saturation(source: Bitmap): Bitmap = coroutineScope {
            val resultBitmap = source.copy(source.config, true)
            val width = source.width
            val height = source.height
            val saturationValue = 0.2f

            (0 until height).forEach { y ->
                launch(Dispatchers.IO) {
                    for (x in 0 until width) {
                        val pixel = source.getPixel(x, y)
                        val hsv = FloatArray(3)
                        Color.colorToHSV(pixel, hsv)
                        hsv[1] = min(hsv[1] + saturationValue, 1f)
                        val newPixel = Color.HSVToColor(hsv)
                        resultBitmap.setPixel(x, y, newPixel)
                    }
                }
            }
            resultBitmap
        }
    }
}