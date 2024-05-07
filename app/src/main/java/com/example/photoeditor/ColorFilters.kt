package com.example.photoeditor

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.*

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

    }
}