package com.example.photoeditor

import android.graphics.Bitmap
import android.graphics.Color
import com.example.photoeditor.SuperSampling.GausBlur
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.floor

class Resizer {
    companion object {

        suspend fun resize(sourceBitmap:Bitmap,resizeXScale:Double,resizeYScale:Double): Bitmap = withContext(Dispatchers.Default){

            val newImgWidth:Int = (sourceBitmap.width * resizeXScale).toInt()
            val newImgHeight = (sourceBitmap.height * resizeYScale).toInt()

            if (resizeXScale + resizeYScale > 2.0){
                bilinearInterpolation(sourceBitmap,resizeXScale,resizeYScale,newImgWidth,newImgHeight)
            }
            if(resizeXScale ==1.0 && resizeYScale == 1.0){
                sourceBitmap
            }
            trilinearInterpolation(sourceBitmap,resizeXScale,resizeYScale,newImgWidth,newImgHeight)
        }
        private fun bilinearInterpolation(sourceBitmap:Bitmap,resizeXScale:Double,resizeYScale:Double,newImgWidth:Int,newImgHeight:Int): Bitmap {

            val sourceImage = IntArray(sourceBitmap.width*sourceBitmap.height)
            sourceBitmap.getPixels(sourceImage,0,sourceBitmap.width,0,0,sourceBitmap.width,sourceBitmap.height)

            val pixels = IntArray(newImgWidth*newImgHeight)

            val resizedBitmap = Bitmap.createBitmap(newImgWidth, newImgHeight, Bitmap.Config.ARGB_8888)
            val smooth = GausBlur()
            val arr = ArrayList<vec2d>()
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

                    val px1 = sourceImage[x0 + y0 * sourceBitmap.width]
                    val px2 = sourceImage[x0 + y1 * sourceBitmap.width]
                    val px3 = sourceImage[x1 + y0 * sourceBitmap.width]
                    val px4 = sourceImage[x1 + y1 * sourceBitmap.width]

                    val newC1 = calculateMiddleInterpolation(px1,px2,dx)
                    val newC2 = calculateMiddleInterpolation(px3,px4,dx)
                    if(smooth.comparePixel(sourceImage,x0,y0,sourceBitmap.width,sourceBitmap.height) || smooth.comparePixel(sourceImage,x0,y1,sourceBitmap.width,sourceBitmap.height)
                        || smooth.comparePixel(sourceImage,x1,y0,sourceBitmap.width,sourceBitmap.height) || smooth.comparePixel(sourceImage,x1,y1,sourceBitmap.width,sourceBitmap.height) ){
                        arr.add(vec2d(x.toFloat(),y.toFloat()))
                    }
                    pixels[x + y*newImgWidth] = calculateMiddleInterpolation(newC1,newC2,dy)
                }
            }
            for(i in 0..<arr.size){
                smooth.smoothPixel(pixels,arr[i].x.toInt(),arr[i].y.toInt(),newImgWidth,newImgHeight)
            }
            resizedBitmap.setPixels(pixels, 0, newImgWidth, 0, 0, newImgWidth,newImgHeight)
            return resizedBitmap
        }
        private fun trilinearInterpolation(sourceBitmap:Bitmap,resizeXScale:Double,resizeYScale:Double,newImgWidth:Int,newImgHeight:Int) : Bitmap {

            var resizeFactor = 1.0
            while (resizeFactor > resizeXScale && resizeFactor > resizeYScale){
                resizeFactor *=  0.5
            }
            val resizeFactor2 =  resizeFactor*2

            val firstLevelWidth = (sourceBitmap.width*resizeFactor).toInt()
            val firstLevelHeight = (sourceBitmap.height*resizeFactor).toInt()
            val firstLevelBitmap = bilinearInterpolation(sourceBitmap,resizeFactor,resizeFactor,firstLevelWidth,firstLevelHeight)
            val firstLevelImage = IntArray(firstLevelWidth*firstLevelHeight)
            firstLevelBitmap.getPixels(firstLevelImage,0,firstLevelWidth,0,0,firstLevelWidth,firstLevelHeight)

            val secondLevelWidth = (sourceBitmap.width*resizeFactor2).toInt()
            val secondLevelHeight = (sourceBitmap.height*resizeFactor2).toInt()
            val secondLevelBitmap = bilinearInterpolation(sourceBitmap,resizeFactor2,resizeFactor2,secondLevelWidth,secondLevelHeight)
            val secondLevelImage = IntArray(secondLevelWidth*secondLevelHeight)
            secondLevelBitmap.getPixels(secondLevelImage,0,secondLevelWidth,0,0,secondLevelWidth,secondLevelHeight)

            val resizedImage = IntArray(newImgWidth*newImgHeight)
            val resizedBitmap = Bitmap.createBitmap(newImgWidth, newImgHeight, Bitmap.Config.ARGB_8888)
            for (y in 0 until newImgHeight) {

                for (x in 0 until newImgWidth) {

                    val originalX = x / (resizeXScale/resizeFactor)
                    val originalY = y / (resizeYScale/resizeFactor)
                    val x0 = floor(originalX).toInt().coerceIn(0, firstLevelWidth - 1)
                    val y0 = floor(originalY).toInt().coerceIn(0, firstLevelHeight - 1)
                    val x1 = ceil(originalX).toInt().coerceIn(0, firstLevelWidth - 1)
                    val y1 = ceil(originalY).toInt().coerceIn(0, firstLevelHeight - 1)
                    val dx = originalX - x0
                    val dy = originalY - y0

                    val px1 = firstLevelImage[x0 + y0 * firstLevelWidth]
                    val px2 = firstLevelImage[x0 + y1 * firstLevelWidth]
                    val px3 = firstLevelImage[x1 + y0 * firstLevelWidth]
                    val px4 = firstLevelImage[x1 + y1 * firstLevelWidth]

                    val newC1 = calculateMiddleInterpolation(px1,px2,dx)
                    val newC2 = calculateMiddleInterpolation(px3,px4,dx)
                    val newC3 = calculateMiddleInterpolation(newC1,newC2,dy)

                    val originalXS = x / (resizeXScale/resizeFactor2)
                    val originalYS = y / (resizeYScale/resizeFactor2)
                    val x0S = floor(originalXS).toInt().coerceIn(0, secondLevelWidth - 1)
                    val y0S = floor(originalYS).toInt().coerceIn(0, secondLevelHeight - 1)
                    val x1S = ceil(originalXS).toInt().coerceIn(0, secondLevelWidth - 1)
                    val y1S = ceil(originalYS).toInt().coerceIn(0, secondLevelHeight - 1)
                    val dxS = originalXS - x0S
                    val dyS = originalYS - y0S

                    val px1S = secondLevelImage[x0S + y0S * secondLevelWidth]
                    val px2S = secondLevelImage[x0S + y1S * secondLevelWidth]
                    val px3S = secondLevelImage[x1S + y0S * secondLevelWidth]
                    val px4S = secondLevelImage[x1S + y1S * secondLevelWidth]

                    val newC1S = calculateMiddleInterpolation(px1S,px2S,dxS)
                    val newC2S = calculateMiddleInterpolation(px3S,px4S,dxS)
                    val newC3S = calculateMiddleInterpolation(newC1S,newC2S,dyS)
                    resizedImage[x + y * newImgWidth] = calculateMiddleInterpolation(newC3,newC3S,0.5)
                }
            }
            resizedBitmap.setPixels(resizedImage, 0, newImgWidth, 0, 0, newImgWidth,newImgHeight)
            return resizedBitmap
        }

        private fun calculateMiddleInterpolation(px1:Int,px2:Int,coef:Double) : Int{
            val newR = Color.red(px1) *(1-coef) + Color.red(px2) *coef
            val newG = Color.green(px1) *(1-coef) + Color.green(px2) *coef
            val newB = Color.blue(px1) *(1-coef) + Color.blue(px2) *coef
            return (255 shl 24) or (newR.toInt() shl 16) or (newG.toInt() shl 8) or newB.toInt()
        }
    }
}
