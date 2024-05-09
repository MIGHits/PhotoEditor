package com.example.photoeditor

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.graphics.scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.*
import kotlinx.coroutines.runBlocking
import kotlin.math.*

class ImageRotation {
    companion object {
        suspend fun rotateBitmap(
            sourceBitmap: Bitmap,
            angleInDegrees: Double
        ): Bitmap = withContext(Dispatchers.Default) {
            val angleInRadians = Math.toRadians(angleInDegrees)
            val cosAngle = cos(angleInRadians)
            val sinAngle = sin(angleInRadians)

            val sourceWidth = sourceBitmap.width
            val sourceHeight = sourceBitmap.height
            val centerX = sourceWidth / 2.0
            val centerY = sourceHeight / 2.0

            val rotatedWidth = (abs(sourceWidth * cosAngle) + abs(sourceHeight * sinAngle)).toInt()
            val rotatedHeight = (abs(sourceWidth * sinAngle) + abs(sourceHeight * cosAngle)).toInt()

            val rotatedBitmap = Bitmap.createBitmap(rotatedWidth, rotatedHeight, Bitmap.Config.ARGB_8888)
            val pixels = IntArray(rotatedWidth * rotatedHeight)

            var minX = rotatedWidth
            var minY = rotatedHeight
            var maxX = 0
            var maxY = 0

            for (y in 0 until rotatedHeight) {
                for (x in 0 until rotatedWidth) {
                    val rotatedX = x - rotatedWidth / 2.0
                    val rotatedY = y - rotatedHeight / 2.0
                    val sourceX = (rotatedX * cosAngle - rotatedY * sinAngle + centerX).toInt()
                    val sourceY = (rotatedY * cosAngle + rotatedX * sinAngle + centerY).toInt()

                    val pixel = if (sourceX in 0 until sourceWidth && sourceY in 0 until sourceHeight) {
                        sourceBitmap.getPixel(sourceX, sourceY)
                    } else {
                        Color.TRANSPARENT
                    }

                    pixels[y * rotatedWidth + x] = pixel

                    if (pixel != Color.TRANSPARENT) {
                        minX = minOf(minX, x)
                        maxX = maxOf(maxX, x)
                        minY = minOf(minY, y)
                        maxY = maxOf(maxY, y)
                    }
                }
            }

            rotatedBitmap.setPixels(pixels, 0, rotatedWidth, 0, 0, rotatedWidth, rotatedHeight)

            val croppedWidth = maxX - minX + 1
            val croppedHeight = maxY - minY + 1
            val croppedBitmap = Bitmap.createBitmap(rotatedBitmap, minX, minY, croppedWidth, croppedHeight)

            return@withContext croppedBitmap
        }
    }
}