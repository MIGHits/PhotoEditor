package com.example.photoeditor

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.exp

class ColorFilters {
    companion object{

        private operator fun <E> MutableList<E>.set(i: Int, value: Int) {}

        suspend fun brightness(source: Bitmap, brightnessValue: Int): Bitmap {
            val width = source.width
            val height = source.height
            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            coroutineScope {
                val pixels = IntArray(width * height)
                source.getPixels(pixels, 0, width, 0, 0, width, height)

                launch(Dispatchers.Default) {
                    pixels.forEachIndexed { index, color ->
                        val red = Color.red(color)
                        val green = Color.green(color)
                        val blue = Color.blue(color)

                        val r = (red + brightnessValue).coerceAtMost(255)
                        val g = (green + brightnessValue).coerceAtMost(255)
                        val b = (blue + brightnessValue).coerceAtMost(255)

                        val newColor = Color.rgb(r, g, b)

                        pixels[index] = newColor
                    }

                    resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                }
            }

            return resultBitmap
        }

        suspend fun negative(source: Bitmap): Bitmap {
            val width = source.width
            val height = source.height
            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            coroutineScope {
                val pixels = IntArray(width * height)
                source.getPixels(pixels, 0, width, 0, 0, width, height)

                launch(Dispatchers.Default) {
                    pixels.forEachIndexed { index, color ->
                        val red = Color.red(color)
                        val green = Color.green(color)
                        val blue = Color.blue(color)

                        val r = 255 - red
                        val g = 255 - green
                        val b = 255 - blue

                        val newColor = Color.rgb(r, g, b)

                        pixels[index] = newColor
                    }

                    resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                }
            }

            return resultBitmap
        }

        suspend fun redFilter(source: Bitmap): Bitmap {
            val width = source.width
            val height = source.height
            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            coroutineScope {
                val pixels = IntArray(width * height)
                source.getPixels(pixels, 0, width, 0, 0, width, height)

                launch(Dispatchers.Default) {
                    pixels.forEachIndexed { index, color ->

                        val newColor = Color.rgb(Color.red(color), 0, 0)

                        pixels[index] = newColor
                    }

                    resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                }
            }

            return resultBitmap
        }

        suspend fun greenFilter(source: Bitmap): Bitmap {
            val width = source.width
            val height = source.height
            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            coroutineScope {
                val pixels = IntArray(width * height)
                source.getPixels(pixels, 0, width, 0, 0, width, height)

                launch(Dispatchers.Default) {
                    pixels.forEachIndexed { index, color ->

                        val newColor = Color.rgb(0, Color.green(color), 0)

                        pixels[index] = newColor
                    }

                    resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                }
            }

            return resultBitmap
        }

        suspend fun blueFilter(source: Bitmap): Bitmap {
            val width = source.width
            val height = source.height
            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            coroutineScope {
                val pixels = IntArray(width * height)
                source.getPixels(pixels, 0, width, 0, 0, width, height)

                launch(Dispatchers.Default) {
                    pixels.forEachIndexed { index, color ->

                        val newColor = Color.rgb(0, 0, Color.blue(color))

                        pixels[index] = newColor
                    }

                    resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                }
            }

            return resultBitmap
        }

        suspend fun grayscale(source: Bitmap): Bitmap {
            val width = source.width
            val height = source.height
            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            coroutineScope {
                val pixels = IntArray(width * height)
                source.getPixels(pixels, 0, width, 0, 0, width, height)

                launch(Dispatchers.Default) {
                    pixels.forEachIndexed { index, color ->
                        val red = Color.red(color)
                        val green = Color.green(color)
                        val blue = Color.blue(color)

                        val r = (red + green + blue) / 3
                        val g = (red + green + blue) / 3
                        val b = (red + green + blue) / 3

                        val newColor = Color.rgb(r, g, b)

                        pixels[index] = newColor
                    }

                    resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                }
            }

            return resultBitmap
        }

        suspend fun blackout(source: Bitmap, blackoutValue: Int): Bitmap {
            val width = source.width
            val height = source.height
            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            coroutineScope {
                val pixels = IntArray(width * height)
                source.getPixels(pixels, 0, width, 0, 0, width, height)

                launch(Dispatchers.Default) {
                    pixels.forEachIndexed { index, color ->
                        val red = Color.red(color)
                        val green = Color.green(color)
                        val blue = Color.blue(color)

                        val r = (red + green + blue) / (blackoutValue + 3)
                        val g = (red + green + blue) / (blackoutValue + 3)
                        val b = (red + green + blue) / (blackoutValue + 3)

                        val newColor = Color.rgb(r, g, b)

                        pixels[index] = newColor
                    }

                    resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                }
            }

            return resultBitmap
        }

        suspend fun mosaic(source: Bitmap, blockSize: Int): Bitmap {
            val width = source.width
            val height = source.height
            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            coroutineScope {
                val pixels = IntArray(width * height)
                source.getPixels(pixels, 0, width, 0, 0, width, height)

                launch(Dispatchers.Default) {
                    for (j in 0 until height step blockSize) {
                        for (i in 0 until width step blockSize) {
                            var count = 0
                            val colors = mutableListOf(0, 0, 0)

                            for (k in 0 until blockSize) {
                                for (n in 0 until blockSize) {
                                    val x = i + k
                                    val y = j + n
                                    if (x < width && y < height) {
                                        val pixel = pixels[y * width + x]
                                        colors[0] += Color.red(pixel)
                                        colors[1] += Color.green(pixel)
                                        colors[2] += Color.blue(pixel)
                                        count++
                                    }
                                }
                            }

                            val meanColor = Color.rgb(
                                colors[0] / count,
                                colors[1] / count,
                                colors[2] / count
                            )

                            for (k in 0 until blockSize) {
                                for (n in 0 until blockSize) {
                                    val x = i + k
                                    val y = j + n
                                    if (x < width && y < height) {
                                        pixels[y * width + x] = meanColor
                                    }
                                }
                            }
                        }
                    }

                    resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                }
            }

            return resultBitmap
        }

        suspend fun sepia(source: Bitmap): Bitmap {
            val width = source.width
            val height = source.height
            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            coroutineScope {
                val pixels = IntArray(width * height)
                source.getPixels(pixels, 0, width, 0, 0, width, height)

                launch(Dispatchers.Default) {
                    pixels.forEachIndexed { index, color ->
                        val red = Color.red(color)
                        val green = Color.green(color)
                        val blue = Color.blue(color)

                        val r = (0.393 * red + 0.769 * green + 0.189 * blue).toInt().coerceAtMost(255)
                        val g = (0.349 * red + 0.686 * green + 0.168 * blue).toInt().coerceAtMost(255)
                        val b = (0.272 * red + 0.534 * green + 0.131 * blue).toInt().coerceAtMost(255)

                        val newColor = Color.rgb(r, g, b)

                        pixels[index] = newColor
                    }

                    resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                }
            }

            return resultBitmap
        }

        suspend fun contrast(source: Bitmap, alpha: Int): Bitmap {
            val width = source.width
            val height = source.height
            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val contrast = ((100 + alpha) / 100.0)*((100 + alpha) / 100.0)

            coroutineScope {
                val pixels = IntArray(width * height)
                source.getPixels(pixels, 0, width, 0, 0, width, height)

                launch(Dispatchers.Default) {
                    pixels.forEachIndexed { index, color ->
                        val red = Color.red(color)
                        val green = Color.green(color)
                        val blue = Color.blue(color)

                        val colors = mutableListOf(red.toDouble(), green.toDouble(), blue.toDouble())

                        for (i in 0 until colors.size)
                        {
                            colors[i] /= 255
                            colors[i] -= 0.5
                            colors[i] *= contrast
                            colors[i] += 0.5
                            colors[i] *= 255
                        }

                        val r = colors[0].toInt().coerceAtMost(255)
                        val g = colors[1].toInt().coerceAtMost(255)
                        val b = colors[2].toInt().coerceAtMost(255)

                        val newColor = Color.rgb(r, g, b)

                        pixels[index] = newColor
                    }

                    resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
                }
            }

            return resultBitmap
        }

        fun gaussianFunction(x: Int, y: Int, sigma: Double): Double {
            return exp(-(x * x + y * y) / (2 * sigma * sigma)) / (2 * PI * sigma * sigma)
        }

        suspend fun gaussianBlur(source: Bitmap, sigma: Double): Bitmap {
            val width = source.width
            val height = source.height
            val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val radius = (sigma * 2).toInt()
            val kernelWidth = radius * 2 + 1
            val kernel = Array(kernelWidth) { DoubleArray(kernelWidth) }
            var sum = 0.0

            for (x in -radius..radius) {
                for (y in -radius..radius) {
                    val kernelValue = gaussianFunction(x, y, sigma)
                    kernel[x + radius][y + radius] = kernelValue
                    sum += kernelValue
                }
            }

            for (x in 0 until kernelWidth) {
                for (y in 0 until kernelWidth) {
                    kernel[x][y] /= sum
                }
            }

            val pixels = IntArray(width * height)
            val resultPixels = IntArray(width * height)
            source.getPixels(pixels, 0, width, 0, 0, width, height)

            withContext(Dispatchers.Default) {
                val numCores = Runtime.getRuntime().availableProcessors()
                val chunkSize = ceil(height.toDouble() / numCores).toInt()

                val deferredResults = (0 until numCores).map { core ->
                    async {
                        val startY = core * chunkSize
                        val endY = minOf(startY + chunkSize, height)
                        for (y in startY until endY) {
                            for (x in 0 until width) {
                                var red = 0.0
                                var green = 0.0
                                var blue = 0.0
                                var alpha = 0.0

                                for (kernelX in -radius..radius) {
                                    for (kernelY in -radius..radius) {
                                        val currentX = x + kernelX
                                        val currentY = y + kernelY
                                        if (currentX in 0 until width && currentY in 0 until height) {
                                            val pixel = pixels[currentY * width + currentX]
                                            val kernelValue = kernel[kernelX + radius][kernelY + radius]

                                            red += (Color.red(pixel) * kernelValue)
                                            green += (Color.green(pixel) * kernelValue)
                                            blue += (Color.blue(pixel) * kernelValue)
                                            alpha += (Color.alpha(pixel) * kernelValue)
                                        }
                                    }
                                }

                                val newColor = Color.argb(
                                    alpha.toInt(),
                                    red.toInt(),
                                    green.toInt(),
                                    blue.toInt()
                                )
                                resultPixels[y * width + x] = newColor
                            }
                        }
                    }
                }

                deferredResults.forEach { it.await() }
            }

            resultBitmap.setPixels(resultPixels, 0, width, 0, 0, width, height)
            return resultBitmap
        }

        suspend fun unsharpMasking(bitmap: Bitmap, amount: Int): Bitmap = withContext(Dispatchers.Default) {
            val gaussianBitmap = gaussianBlur(bitmap, 1.0)
            val width = bitmap.width
            val height = bitmap.height
            val resultBitmap = bitmap.copy(bitmap.config, true)
            val pixels = IntArray(width * height)
            val gaussianPixels = IntArray(width * height)

            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            gaussianBitmap.getPixels(gaussianPixels, 0, width, 0, 0, width, height)

            for (i in pixels.indices) {
                val pixelMinuend = pixels[i]
                val pixelSubtrahend = gaussianPixels[i]

                val red = (Color.red(pixelMinuend) - Color.red(pixelSubtrahend)) * amount
                val green = (Color.green(pixelMinuend) - Color.green(pixelSubtrahend)) * amount
                val blue = (Color.blue(pixelMinuend) - Color.blue(pixelSubtrahend)) * amount

                val resultRed = red.coerceIn(0, 255)
                val resultGreen = green.coerceIn(0, 255)
                val resultBlue = blue.coerceIn(0, 255)

                pixels[i] = Color.rgb(
                    (Color.red(pixelMinuend) + resultRed).coerceIn(0, 255),
                    (Color.green(pixelMinuend) + resultGreen).coerceIn(0, 255),
                    (Color.blue(pixelMinuend) + resultBlue).coerceIn(0, 255)
                )
            }

            resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

            resultBitmap
        }
    }
}