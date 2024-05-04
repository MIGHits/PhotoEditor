package com.example.photoeditor

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.*
import kotlin.math.*

class Brightness {
    companion object{
        suspend fun brightnessFilter(source: Bitmap): Bitmap = coroutineScope {
            val resultBitmap = source.copy(source.config, true)
            val width = source.width
            val height = source.height
            val brightnessValue = 50

                (0 until height).forEach { y ->
                    launch(Dispatchers.IO) {
                        for (x in 0 until width) {
                            val pixel = source.getPixel(x, y)
                            val alpha = Color.alpha(pixel)

                            val red = min(Color.red(pixel) + brightnessValue, 255)
                            val green = min(Color.green(pixel) + brightnessValue, 255)
                            val blue = min(Color.blue(pixel) + brightnessValue, 255)

                            val newPixel = Color.argb(alpha, red, green, blue)
                            resultBitmap.setPixel(x, y, newPixel)
                        }
                    }
                }
            resultBitmap
        }
    }
}