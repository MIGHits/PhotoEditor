package com.example.photoeditor

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.*
import kotlinx.coroutines.runBlocking
import kotlin.math.*

class ImageRotation {
    companion object {
        suspend fun rotateBitmap(sourceBitmap: Bitmap, angleInDegrees: Double, activity: Activity): Bitmap = coroutineScope {
            val angleInRadians = Math.toRadians(angleInDegrees)
            val cosAngle = cos(angleInRadians)
            val sinAngle = sin(angleInRadians)

            val sourceWidth = sourceBitmap.width
            val sourceHeight = sourceBitmap.height
            val centerX = sourceWidth / 2.0
            val centerY = sourceHeight / 2.0

            val rotatedWidth = (abs(sourceWidth * cosAngle) + abs(sourceHeight * sinAngle)).toInt()
            val rotatedHeight = (abs(sourceHeight * cosAngle) + abs(sourceWidth * sinAngle)).toInt()

            val rotatedBitmap = Bitmap.createBitmap(rotatedWidth, rotatedHeight, Bitmap.Config.ARGB_8888)

            val pixels = IntArray(rotatedWidth * rotatedHeight)

            val halfRotatedWidth = rotatedWidth / 2
            val halfRotatedHeight = rotatedHeight / 2

            launch(Dispatchers.Default) {
                for (y in 0 until rotatedHeight) {
                    for (x in 0 until rotatedWidth) {
                        val rotatedX = x - halfRotatedWidth
                        val rotatedY = y - halfRotatedHeight
                        val sourceX = (rotatedX * cosAngle - rotatedY * sinAngle + centerX).toInt()
                        val sourceY = (rotatedY * cosAngle + rotatedX * sinAngle + centerY).toInt()

                        val pixel = if (sourceX in 0 until sourceWidth && sourceY in 0 until sourceHeight) {
                            sourceBitmap.getPixel(sourceX, sourceY)
                        } else {
                           Color.TRANSPARENT
                        }

                        pixels[y * rotatedWidth + x] = pixel
                    }
                }
                rotatedBitmap.setPixels(pixels, 0, rotatedWidth, 0, 0, rotatedWidth, rotatedHeight)
            }

            rotatedBitmap
        }

        fun getActivityBackgroundColor(activity: Activity): Int {
            val background = activity.window.decorView.background
            return if (background is ColorDrawable) {
                background.color
            } else {
                // Если фон не является цветом, возвращаем белый цвет
                Color.WHITE
            }
        }
    }
}