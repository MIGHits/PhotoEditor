package com.example.photoeditor

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.ceil
import kotlin.math.floor

class Resize {
    companion object {
        fun callResize(sourceBitmap: Bitmap, resizeXScale: Double, resizeYScale: Double): Bitmap {
            val newImgWidth: Int = (sourceBitmap.width * resizeXScale).toInt()
            val newImgHeight = (sourceBitmap.height * resizeYScale).toInt()
            return if (resizeXScale + resizeYScale >= 2.0) {
                bilinearInterpolation(
                    sourceBitmap,
                    resizeXScale,
                    resizeYScale,
                    newImgWidth,
                    newImgHeight
                )
            } else {
                trilinearInterpolation(
                    sourceBitmap,
                    resizeXScale,
                    resizeYScale,
                    newImgWidth,
                    newImgHeight
                )
            }
        }

        private fun bilinearInterpolation(
            sourceBitmap: Bitmap,
            resizeXScale: Double,
            resizeYScale: Double,
            newImgWidth: Int,
            newImgHeight: Int
        ): Bitmap {
            val resizedBitmap =
                Bitmap.createBitmap(newImgWidth, newImgHeight, Bitmap.Config.ARGB_8888)

            for (y in 0 until newImgHeight) {
                for (x in 0 until newImgWidth) {
                    val originalX = x / resizeXScale
                    val originalY = y / resizeYScale
                    val x0 = floor(originalX).toInt().coerceIn(0, sourceBitmap.width - 1)
                    val y0 = floor(originalY).toInt().coerceIn(0, sourceBitmap.height - 1)
                    val x1 = ceil(originalX).toInt().coerceIn(0, sourceBitmap.width - 1)
                    val y1 = ceil(originalY).toInt().coerceIn(0, sourceBitmap.height - 1)
                    val dx = originalX - x0
                    val dy = originalY - y0

                    val px1 = sourceBitmap.getPixel(x0, y0)
                    val px2 = sourceBitmap.getPixel(x0, y1)
                    val px3 = sourceBitmap.getPixel(x1, y0)
                    val px4 = sourceBitmap.getPixel(x1, y1)

                    val newC1 = calculateMiddleInterpolation(px1, px2, dx)
                    val newC2 = calculateMiddleInterpolation(px3, px4, dx)

                    resizedBitmap.setPixel(x, y, calculateMiddleInterpolation(newC1, newC2, dy))
                }
            }
            return resizedBitmap
        }

        fun trilinearInterpolation(
            sourceBitmap: Bitmap,
            resizeXScale: Double,
            resizeYScale: Double,
            newImgWidth: Int,
            newImgHeight: Int
        ): Bitmap {
            var resizeFactor = 1.0
            while (resizeFactor > resizeXScale && resizeFactor > resizeYScale) {
                resizeFactor *= 0.5
            }
            val resizeFactor2 = resizeFactor * 2
            val firstLevel = bilinearInterpolation(
                sourceBitmap,
                resizeFactor,
                resizeFactor,
                (sourceBitmap.width * resizeFactor).toInt(),
                (sourceBitmap.height * resizeFactor).toInt()
            )

            val secondLevel = bilinearInterpolation(
                sourceBitmap,
                resizeFactor2,
                resizeFactor2,
                (sourceBitmap.width * resizeFactor2).toInt(),
                (sourceBitmap.height * resizeFactor2).toInt()
            )

            val resizedBitmap =
                Bitmap.createBitmap(newImgWidth, newImgHeight, Bitmap.Config.ARGB_8888)
            for (y in 0 until newImgHeight) {

                for (x in 0 until newImgWidth) {

                    val originalX = x / (resizeXScale / resizeFactor)
                    val originalY = y / (resizeYScale / resizeFactor)
                    val x0 = floor(originalX).toInt().coerceIn(0, firstLevel.width - 1)
                    val y0 = floor(originalY).toInt().coerceIn(0, firstLevel.height - 1)
                    val x1 = ceil(originalX).toInt().coerceIn(0, firstLevel.width - 1)
                    val y1 = ceil(originalY).toInt().coerceIn(0, firstLevel.height - 1)
                    val dx = originalX - x0
                    val dy = originalY - y0

                    val px1 = firstLevel.getPixel(x0, y0)
                    val px2 = firstLevel.getPixel(x0, y1)
                    val px3 = firstLevel.getPixel(x1, y0)
                    val px4 = firstLevel.getPixel(x1, y1)

                    val newC1 = calculateMiddleInterpolation(px1, px2, dx)
                    val newC2 = calculateMiddleInterpolation(px3, px4, dx)
                    val newC3 = calculateMiddleInterpolation(newC1, newC2, dy)

                    val originalXS = x / (resizeXScale / resizeFactor2)
                    val originalYS = y / (resizeYScale / resizeFactor2)
                    val x0S = floor(originalXS).toInt().coerceIn(0, secondLevel.width - 1)
                    val y0S = floor(originalYS).toInt().coerceIn(0, secondLevel.height - 1)
                    val x1S = ceil(originalXS).toInt().coerceIn(0, secondLevel.width - 1)
                    val y1S = ceil(originalYS).toInt().coerceIn(0, secondLevel.height - 1)
                    val dxS = originalXS - x0S
                    val dyS = originalYS - y0S

                    val px1S = secondLevel.getPixel(x0S, y0S)
                    val px2S = secondLevel.getPixel(x0S, y1S)
                    val px3S = secondLevel.getPixel(x1S, y0S)
                    val px4S = secondLevel.getPixel(x1S, y1S)

                    val newC1S = calculateMiddleInterpolation(px1S, px2S, dxS)
                    val newC2S = calculateMiddleInterpolation(px3S, px4S, dxS)
                    val newC3S = calculateMiddleInterpolation(newC1S, newC2S, dyS)
                    resizedBitmap.setPixel(x, y, calculateMiddleInterpolation(newC3, newC3S, 0.5))
                }
            }

            return resizedBitmap
        }

        private fun calculateMiddleInterpolation(px1: Int, px2: Int, coef: Double): Int {
            val newR = Color.red(px1) * (1 - coef) + Color.red(px2) * coef
            val newG = Color.green(px1) * (1 - coef) + Color.green(px2) * coef
            val newB = Color.blue(px1) * (1 - coef) + Color.blue(px2) * coef
            return (255 shl 24) or (newR.toInt() shl 16) or (newG.toInt() shl 8) or newB.toInt()
        }
    }
}