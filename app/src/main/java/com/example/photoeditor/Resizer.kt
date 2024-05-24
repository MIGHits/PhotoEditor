package com.example.photoeditor

import android.graphics.Bitmap
import android.graphics.Color
import com.example.photoeditor.SuperSampling.GausBlur
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.floor

class Resizer {
    companion object {

        suspend fun resize(sourceBitmap:Bitmap,resizeXScale:Double,resizeYScale:Double): Bitmap = withContext(Dispatchers.Default){

            //Вычисление будущего размера
            val newImgWidth = (sourceBitmap.width * resizeXScale).toInt()
            val newImgHeight = (sourceBitmap.height * resizeYScale).toInt()

            //Если изображение в основном увеличивается, то применяю билинейную интерполяцию
            if (resizeXScale + resizeYScale > 2.0){
                bilinearInterpolation(sourceBitmap,resizeXScale,resizeYScale,newImgWidth,newImgHeight)
            }

            //Если изображение осталось с текущим размером, то возвращаю текущее изображение
            if(resizeXScale ==1.0 && resizeYScale == 1.0){
                sourceBitmap
            }

            //Если изображение в основном уменьшается, то применяю трилинейную интерполяцию
            trilinearInterpolation(sourceBitmap,resizeXScale,resizeYScale,newImgWidth,newImgHeight)
        }

        private suspend fun bilinearInterpolation(sourceBitmap:Bitmap,resizeXScale:Double,resizeYScale:Double,newImgWidth:Int,newImgHeight:Int): Bitmap {

            //Перевожу исходную bitmap в intArray для ускорения получения значения пикселя
            val sourceImage = IntArray(sourceBitmap.width*sourceBitmap.height)
            sourceBitmap.getPixels(sourceImage,0,sourceBitmap.width,0,0,sourceBitmap.width,sourceBitmap.height)

            //Создаю IntArray с новыми размерами, в который будут записаны новые значения пикселей
            val pixels = IntArray(newImgWidth*newImgHeight)

            //Инициализация ядра размытия по гауссу
            val smooth = GausBlur(1)

            //Создание массива, в котором будут хринтся координаты точек, которые нужно сгладить
            val pointsToSmooth = ArrayList<vec2d>()

            val mutex = Mutex()
            withContext(Dispatchers.Default) {
                val numCores = Runtime.getRuntime().availableProcessors()
                val chunkSize = ceil(newImgHeight.toDouble() / numCores).toInt()

                val deferredResults = (0 until numCores).map { core ->
                    async {
                        val startY = core * chunkSize
                        val endY = minOf(startY + chunkSize, newImgHeight)
                        for (y in startY..<endY) {
                            for (x in 0 until newImgWidth) {

                                //Вычисления координат текущей точки из прошлого изображения
                                val originalX = x / resizeXScale
                                val originalY = y / resizeYScale
                                val x0 = floor(originalX).toInt().coerceIn(0, sourceBitmap.width - 1)
                                val y0 = floor(originalY).toInt().coerceIn(0, sourceBitmap.height - 1)
                                val x1 = ceil(originalX).toInt().coerceIn(0, sourceBitmap.width - 1)
                                val y1 = ceil(originalY).toInt().coerceIn(0, sourceBitmap.height - 1)

                                //Коэффициенты влияния пикселей
                                val dx = originalX - x0
                                val dy = originalY - y0

                                val px1 = sourceImage[x0 + y0 * sourceBitmap.width]
                                val px2 = sourceImage[x0 + y1 * sourceBitmap.width]
                                val px3 = sourceImage[x1 + y0 * sourceBitmap.width]
                                val px4 = sourceImage[x1 + y1 * sourceBitmap.width]

                                //Если пиксель граничный, то его нужно сгладить
                                if(smooth.checkPixelForDifference(sourceImage,x0,y0,sourceBitmap.width,sourceBitmap.height) || smooth.checkPixelForDifference(sourceImage,x0,y1,sourceBitmap.width,sourceBitmap.height)
                                    || smooth.checkPixelForDifference(sourceImage,x1,y0,sourceBitmap.width,sourceBitmap.height) || smooth.checkPixelForDifference(sourceImage,x1,y1,sourceBitmap.width,sourceBitmap.height)) {
                                    mutex.withLock{
                                        pointsToSmooth.add(vec2d(x.toFloat(),y.toFloat()))
                                    }
                                }

                                //Интерполяция между 4 пикселями в 1
                                val newC1 = calculateMiddleInterpolation(px1,px2,dx)
                                val newC2 = calculateMiddleInterpolation(px3,px4,dx)
                                pixels[x + y*newImgWidth] = calculateMiddleInterpolation(newC1,newC2,dy)
                            }
                        }
                    }
                }
                deferredResults.forEach { it.await() }
            }

            //Сглаживаем все найденные граничные пиксели
            for(i in 0..<pointsToSmooth.size){
                println(i)
                smooth.smoothPixel(pixels,pointsToSmooth[i].x.toInt(),pointsToSmooth[i].y.toInt(),newImgWidth,newImgHeight)
            }

            //Запись пикселей из IntArray в bitmap
            val resizedBitmap = Bitmap.createBitmap(newImgWidth, newImgHeight, Bitmap.Config.ARGB_8888)
            resizedBitmap.setPixels(pixels, 0, newImgWidth, 0, 0, newImgWidth,newImgHeight)

            return resizedBitmap
        }
        private suspend fun trilinearInterpolation(sourceBitmap:Bitmap, resizeXScale:Double, resizeYScale:Double, newImgWidth:Int, newImgHeight:Int) : Bitmap {

            //Вычисление размеров двух ближайших mipmap уровней
            var resizeFactor = 1.0
            while (resizeFactor > resizeXScale && resizeFactor > resizeYScale){
                resizeFactor *=  0.5
            }
            val resizeFactor2 =  resizeFactor*2

            //Вычисление с помощью билинейной интерполяции двух ближайших mipmap уровней и запись их пикселей в IntArray
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

            //Создание IntArray, которыЙ будет хранить новое изображение
            val resizedImage = IntArray(newImgWidth*newImgHeight)


            withContext(Dispatchers.Default) {
                val numCores = Runtime.getRuntime().availableProcessors()
                val chunkSize = ceil(newImgHeight.toDouble() / numCores).toInt()

                val deferredResults = (0 until numCores).map { core ->
                    async {
                        val startY = core * chunkSize
                        val endY = minOf(startY + chunkSize, newImgHeight)
                        for (y in startY..<endY) {

                            for (x in 0 until newImgWidth) {

                                //ПОВТОРЕНИЕ КОДА БИЛИНЕЙНОЙ ИНТЕРПОЛЯЦИИ ДЛЯ ДВУХ ИЗОБРАЖЕНИЙ

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
                    }
                }
                deferredResults.forEach { it.await() }
            }
            //Запись пикселей из IntArray в bitmap
            val resizedBitmap = Bitmap.createBitmap(newImgWidth, newImgHeight, Bitmap.Config.ARGB_8888)
            resizedBitmap.setPixels(resizedImage, 0, newImgWidth, 0, 0, newImgWidth,newImgHeight)

            return resizedBitmap
        }

        //Функция вычисления цвета между двумя пикселями с коеффициентом
        private fun calculateMiddleInterpolation(px1:Int,px2:Int,coef:Double) : Int{
            val newR = Color.red(px1) *(1-coef) + Color.red(px2) *coef
            val newG = Color.green(px1) *(1-coef) + Color.green(px2) *coef
            val newB = Color.blue(px1) *(1-coef) + Color.blue(px2) *coef
            return (255 shl 24) or (newR.toInt() shl 16) or (newG.toInt() shl 8) or newB.toInt()
        }
    }
}
