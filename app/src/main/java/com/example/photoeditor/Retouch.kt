package com.example.photoeditor

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import kotlinx.coroutines.*
import kotlin.math.*

data class Point(var x: Int, var y: Int)

class Retouch {
    companion object {

        @SuppressLint("ClickableViewAccessibility", "SuspiciousIndentation")
        fun ImageView.setRetouchable(brushRadius: Int, retouchCoeff: Double,imageView:ImageView,accept:ImageButton,decline:ImageButton) {
            val drawable = drawable as? BitmapDrawable
            val bitmap = drawable!!.bitmap
            val mutableBitmap = bitmap!!.copy(Bitmap.Config.ARGB_8888, true)
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_MOVE) {
                    val x = (bitmap!!.width * (event.x) / imageView.width).toInt()
                    val y = (bitmap.height * (event.y) / imageView.height).toInt()

                    CoroutineScope(Dispatchers.Main).launch {
                        val retouchedBitmap = RetouchFilter(
                            mutableBitmap,
                            brushRadius,
                            x,
                            y,
                            retouchCoeff
                        )
                        setImageBitmap(retouchedBitmap)
                        accept.visibility = View.VISIBLE
                        decline.visibility = View.VISIBLE
                    }

                }
                true
            }
        }

        suspend fun RetouchFilter(
            bitmap: Bitmap,
            brushRadius: Int,
            centerX: Int,
            centerY: Int,
            retouchCoeff: Double
        ): Bitmap = withContext(Dispatchers.Default) {
            val width = bitmap.width
            val height = bitmap.height

            val rectLeftTop = Point(max(0, centerX - brushRadius), max(0, centerY - brushRadius))
            val rectRightBot = Point(min(width, centerX + brushRadius), min(height, centerY + brushRadius))

            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            val totalRed = FloatArray(1)
            val totalGreen = FloatArray(1)
            val totalBlue = FloatArray(1)
            val totalAlpha = FloatArray(1)
            val totalWeight = FloatArray(1)

            val brushRadiusSquared = brushRadius * brushRadius
            val sigma = brushRadius / 3.0
            val twoSigmaSquared = 2 * sigma * sigma

            val precomputedWeights = FloatArray(brushRadius + 1) { r ->
                exp(-r * r / twoSigmaSquared).toFloat()
            }

            fun calculateAverageColor() {
                for (y in rectLeftTop.y until rectRightBot.y) {
                    for (x in rectLeftTop.x until rectRightBot.x) {
                        val dx = x - centerX
                        val dy = y - centerY
                        val distanceSquared = dx * dx + dy * dy
                        if (distanceSquared <= brushRadiusSquared) {
                            val distance = sqrt(distanceSquared.toDouble()).toInt()
                            val weight = precomputedWeights[distance]
                            val pixel = pixels[y * width + x]
                            totalRed[0] += Color.red(pixel) * weight
                            totalGreen[0] += Color.green(pixel) * weight
                            totalBlue[0] += Color.blue(pixel) * weight
                            totalAlpha[0] += Color.alpha(pixel) * weight
                            totalWeight[0] += weight
                        }
                    }
                }
            }

            suspend fun applyRetouchEffect(avgRed: Int, avgGreen: Int, avgBlue: Int, avgAlpha: Int) {
                coroutineScope {
                    val numCores = Runtime.getRuntime().availableProcessors()
                    val chunkSize = ceil((rectRightBot.y - rectLeftTop.y).toDouble() / numCores).toInt()
                    val deferredResults = (0 until numCores).map { core ->
                        async {
                            val startY = rectLeftTop.y + core * chunkSize
                            val endY = minOf(startY + chunkSize, rectRightBot.y)
                            for (y in startY until endY) {
                                for (x in rectLeftTop.x until rectRightBot.x) {
                                    val dx = x - centerX
                                    val dy = y - centerY
                                    val distanceSquared = dx * dx + dy * dy
                                    if (distanceSquared <= brushRadiusSquared) {
                                        val pixel = pixels[y * width + x]
                                        val distance = sqrt(distanceSquared.toDouble())
                                        val weight = sin((distance / brushRadius * PI / 2) * retouchCoeff).toFloat()
                                        val newRed = (Color.red(pixel) * weight + avgRed * (1 - weight)).toInt()
                                        val newGreen = (Color.green(pixel) * weight + avgGreen * (1 - weight)).toInt()
                                        val newBlue = (Color.blue(pixel) * weight + avgBlue * (1 - weight)).toInt()
                                        val newAlpha = (Color.alpha(pixel) * weight + avgAlpha * (1 - weight)).toInt()
                                        pixels[y * width + x] = Color.argb(newAlpha, newRed, newGreen, newBlue)
                                    }
                                }
                            }
                        }
                    }

                    deferredResults.awaitAll()
                }
            }

            calculateAverageColor()

            val avgRed = (totalRed[0] / totalWeight[0]).toInt()
            val avgGreen = (totalGreen[0] / totalWeight[0]).toInt()
            val avgBlue = (totalBlue[0] / totalWeight[0]).toInt()
            val avgAlpha = (totalAlpha[0] / totalWeight[0]).toInt()

            applyRetouchEffect(avgRed, avgGreen, avgBlue, avgAlpha)

            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        }
    }
}
