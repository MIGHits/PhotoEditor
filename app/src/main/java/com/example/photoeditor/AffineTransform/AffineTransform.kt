package com.example.photoeditor.AffineTransform

import android.graphics.Bitmap
import com.example.photoeditor.Cube3d.Tria3d
import kotlinx.coroutines.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class AffineTransform(origTriangle: Tria3d) {
    //Вычисление коэффициентов для уравнения, составленного из матрицы афинного преобразования
    //Подробнее можно найти здесь: https://habr.com/ru/articles/463349/
    private var origTriangleDet = det(arrayOf(arrayOf(origTriangle.t[0].x,origTriangle.t[1].x,origTriangle.t[2].x)
        ,arrayOf(origTriangle.t[0].y,origTriangle.t[1].y,origTriangle.t[2].y)
        ,arrayOf(1.0f,1.0f,1.0f)),3)

    private var x1Coef = det(arrayOf(arrayOf(origTriangle.p[0].x,origTriangle.p[1].x,origTriangle.p[2].x)
        ,arrayOf(origTriangle.t[0].y,origTriangle.t[1].y,origTriangle.t[2].y)
        , arrayOf(1.0f,1.0f,1.0f)),3) / origTriangleDet

    private var y1Coef = -det(arrayOf(arrayOf(origTriangle.p[0].x,origTriangle.p[1].x,origTriangle.p[2].x)
        ,arrayOf(origTriangle.t[0].x,origTriangle.t[1].x,origTriangle.t[2].x)
        , arrayOf(1.0f,1.0f,1.0f)),3) / origTriangleDet

    private var free1Coef = det(arrayOf(arrayOf(origTriangle.p[0].x,origTriangle.p[1].x,origTriangle.p[2].x)
        ,arrayOf(origTriangle.t[0].x,origTriangle.t[1].x,origTriangle.t[2].x)
        ,arrayOf(origTriangle.t[0].y,origTriangle.t[1].y,origTriangle.t[2].y)),3)  / origTriangleDet

    private var x2Coef = det(arrayOf(arrayOf(origTriangle.p[0].y,origTriangle.p[1].y,origTriangle.p[2].y)
        ,arrayOf(origTriangle.t[0].y,origTriangle.t[1].y,origTriangle.t[2].y)
        , arrayOf(1.0f,1.0f,1.0f)),3)  / origTriangleDet

    private var y2Coef = -det(arrayOf(arrayOf(origTriangle.p[0].y,origTriangle.p[1].y,origTriangle.p[2].y)
        ,arrayOf(origTriangle.t[0].x,origTriangle.t[1].x,origTriangle.t[2].x)
        , arrayOf(1.0f,1.0f,1.0f)),3)  / origTriangleDet

    private var free2Coef = det(arrayOf(arrayOf(origTriangle.p[0].y,origTriangle.p[1].y,origTriangle.p[2].y)
        ,arrayOf(origTriangle.t[0].x,origTriangle.t[1].x,origTriangle.t[2].x)
        ,arrayOf(origTriangle.t[0].y,origTriangle.t[1].y,origTriangle.t[2].y)),3)  / origTriangleDet

    //Некоторые коэффициенты уравнений, для уменьшения числа рассчетов на каждый пиксель
    private var coefEquation1 = y1Coef * free2Coef - y2Coef * free1Coef
    private var coefEquation2 = x2Coef * free1Coef - free2Coef * x1Coef
    private var coefEquation3 = y2Coef * x1Coef - y1Coef * x2Coef
    private var coefEquation4 = x1Coef * y2Coef - x2Coef * y1Coef

    //Функция преобразования всего изображения по преобразованиям двух треугольников
    suspend fun process(sourceBitmap:Bitmap):Bitmap{

        val changedBitmap = Bitmap.createBitmap(sourceBitmap.width, sourceBitmap.height, Bitmap.Config.ARGB_8888)

        //Запись начального изображения в IntArray для более быстрого взаимодействия
        val newPixels = IntArray(sourceBitmap.width*sourceBitmap.height)
        changedBitmap.getPixels(newPixels,0,sourceBitmap.width,0,0,sourceBitmap.width,sourceBitmap.height)

        //Создание IntArray, в котором будет хранится финальное изображение
        val oldPixels = IntArray(sourceBitmap.width*sourceBitmap.height)
        sourceBitmap.getPixels(oldPixels,0,sourceBitmap.width,0,0,sourceBitmap.width,sourceBitmap.height)

        //Вычисление нового положения изображения при афинных трансформациях
        val yNewEnd = min(max(max(newPos(2,0,0),newPos(2,0,sourceBitmap.height)),max(newPos(2,sourceBitmap.width,sourceBitmap.height),newPos(2,sourceBitmap.width,0))),sourceBitmap.height)
        val yNewStart = max(min(min(newPos(2,0,0),newPos(2,0,sourceBitmap.height)),min(newPos(2,sourceBitmap.width,sourceBitmap.height),newPos(2,sourceBitmap.width,0))),0)
        val xNewEnd = min(max(max(newPos(1,0,0),newPos(1,0,sourceBitmap.height)),max(newPos(1,sourceBitmap.width,sourceBitmap.height),newPos(1,sourceBitmap.width,0))),sourceBitmap.width)
        val xNewStart = max(min(min(newPos(1,0,0),newPos(1,0,sourceBitmap.height)),min(newPos(1,sourceBitmap.width,sourceBitmap.height),newPos(1,sourceBitmap.width,0))),0)

        withContext(Dispatchers.Default) {
            val numCores = Runtime.getRuntime().availableProcessors()
            val chunkSize = ceil((yNewEnd-yNewStart).toDouble() / numCores).toInt()

            val deferredResults = (0 until numCores).map { core ->
                async {
                    val startY = core * chunkSize + yNewStart
                    val endY = minOf(startY + chunkSize, yNewEnd)

                    for (y in startY..<endY) {
                        for (x in xNewStart..<xNewEnd) {
                            //Нахождение пикселя, соответствующего текущему пикселю из старого изображения
                            val oldX = (y2Coef * x - y1Coef * y + coefEquation1) / (coefEquation3)
                            val oldY = (x1Coef * y - x2Coef * x + coefEquation2) / (coefEquation4)

                            if (oldX < sourceBitmap.width && oldX >= 0.0 && oldY < sourceBitmap.height && oldY >= 0.0) {
                                newPixels[x + sourceBitmap.width * (sourceBitmap.height-y-1)] =oldPixels[oldX.toInt() + sourceBitmap.width * (sourceBitmap.height- oldY.toInt()-1)]
                            }
                        }
                    }

                }
            }
            deferredResults.forEach { it.await() }
        }

        //Запись пикселей из IntArray в bitmap
        changedBitmap.setPixels(newPixels, 0, sourceBitmap.width, 0, 0, sourceBitmap.width, sourceBitmap.height)

        return changedBitmap
    }

    //Данная функция возвращает новое положение координаты из старого после афинных преобразований
    private fun newPos(type:Int,x:Int,y:Int):Int{
        if (type == 1){
            return (x1Coef*x + y1Coef*y + free1Coef).toInt()
        }else{
            return (x2Coef*x + y2Coef*y + free2Coef).toInt()
        }

    }




}