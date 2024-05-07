package com.example.photoeditor

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.ColorUtils.HSLToColor
import androidx.core.graphics.ColorUtils.RGBToHSL
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import kotlin.math.*

class SaturationFilter {
    companion object {
        fun RGBToHSL(red: Int, green: Int, blue: Int, hsl: FloatArray) {
            val r = red / 255f
            val g = green / 255f
            val b = blue / 255f

            val max = max(r, max(g, b))
            val min = min(r, min(g, b))
            val delta = max - min

            val h = if (delta == 0f) {
                0
            } else if (max == r) {
                (60 * (((g - b) / delta) % 6))
            } else if (max == g) {
                60 * (((b - r) / delta) + 2)
            } else {
                60 * (((r - g) / delta) + 4)
            }

            val l = (max + min) / 2

            val s = if (delta == 0f) {
                0
            } else {
                delta / (1 - abs(2*l - 1))
            }

            hsl[0] = h.toFloat()
            hsl[1] = s.toFloat()
            hsl[2] = l
        }

        fun HSLToColor(h: Float, s: Float, l: Float): Int {
            var r : Float
            var g : Float
            var b : Float

            val c = (1 - abs(2*l - 1)) * s
            val x = c * (1 - abs((h / 60) % 2 - 1))
            val m = l - c / 2

            if (h >= 0 && h < 60) { r = c; g = x; b = 0f }
            else if ( h >= 60 && h < 120) { r = x; g = c; b = 0f }
            else if ( h >= 120 && h < 180) { r = 0f; g = c; b = x }
            else if ( h >= 180 && h < 240) { r = 0f; g = x; b = c }
            else if ( h >= 240 && h < 300) { r = x; g = 0f; b = c }
            else { r = c; g = 0f; b = x }

            val red = ((r + m) * 255).toInt()
            val green = ((g + m) * 255).toInt()
            val blue = ((b + m) * 255).toInt()

            return Color.rgb(red, green, blue)
        }
        suspend fun saturation(source: Bitmap, saturationFactor: Float): Bitmap {
            val width = source.width
            val height = source.height
            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            coroutineScope {
                val pixels = IntArray(width * height)
                source.getPixels(pixels, 0, width, 0, 0, width, height)

                launch(Dispatchers.Default) {
                    pixels.forEachIndexed { index, color ->
                        val hsl = FloatArray(3)
                        val r = Color.red(color)
                        val g = Color.green(color)
                        val b = Color.blue(color)

                        RGBToHSL(r, g, b, hsl)

                        hsl[1] = (hsl[1] * saturationFactor).coerceAtMost(1f)

                        val newColor = HSLToColor(hsl[0], hsl[1], hsl[2])

                        pixels[index] = newColor
                    }

                    resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                }
            }

            return resultBitmap
        }
    }
}