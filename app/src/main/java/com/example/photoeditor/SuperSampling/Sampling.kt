package com.example.photoeditor.SuperSampling

import android.graphics.Color.blue
import android.graphics.Color.green
import android.graphics.Color.red
import com.example.photoeditor.UsefulFuns.createColor
import kotlin.math.pow


val e =2.71828
val pi = 3.14159

//Функция сравнения двух пикселей на схожесть по цвету
fun compareTwoPixels(p1:Int,p2:Int):Boolean {
    val cmprFac = 255*0.2f
    if ((red(p1) >= red(p2)-cmprFac && red(p1) <= red(p2)+cmprFac)
        && (blue(p1) >= blue(p2)-cmprFac && blue(p1) <= blue(p2)+cmprFac)
        && (green(p1) >= green(p2)-cmprFac && green(p1) <= green(p2)+cmprFac)){
        return false
    }
    return true
}
fun gaussianFunction(x: Int, y: Int, sigma: Int): Double {
    return e.pow(-(x * x + y * y) / (2 * sigma * sigma)) / (2 * pi * sigma * sigma)
}
class GausBlur(sigma:Int){

    val radius = (sigma * 2)
    val kernelWidth = radius * 2 + 1

    val kernel = Array(kernelWidth) { Array(kernelWidth) { 0.0 } }
    var sum = 0.0
    init {
        for (x in -radius..radius) {

            for (y in -radius..radius) {

                val kernelValue = gaussianFunction(x, y, sigma);

                kernel[x + radius][y + radius] = kernelValue;
                sum += kernelValue;

            }
        }

        for (x in 0..<kernelWidth) {
            for (y in 0..<kernelWidth) {
                kernel[x][y] /= sum;
            }

        }
    }

    //Функция сравнивает пиксель с 4 пикселями вокруг
    fun checkPixelForDifference(image:IntArray, x:Int, y:Int, width:Int, height: Int):Boolean {
        if (x >= width-1 || y >= height-1 || x <= 0 || y <= 0) {
            return false
        }
        val isBot = compareTwoPixels(image[x + y * width], image[x + (y + 1) * width])
        val isRight = compareTwoPixels(image[x + y * width], image[x + 1 + y * width])

        if (isBot || isRight) {
            return true
        } else {
            return false
        }
    }
    fun smoothPixel(image:IntArray,x:Int,y:Int,width:Int,height:Int){
        var red = 0.0
        var blue = 0.0
        var green = 0.0
        var corRadiusXL = -radius
        var corRadiusYL = -radius
        var corRadiusXR = radius
        var corRadiusYR = radius
        if (x - radius < 0) {
            corRadiusXL = -x
        }
        if (x + radius > width - 1) {
            corRadiusXR = width - 1 - x
        }
        if (y - radius < 0) {
            corRadiusYL = -y
        }
        if (y + radius > height - 1) {
            corRadiusYR = height - 1 - y
        }
        for (kernelX in corRadiusXL..corRadiusXR) {
            for (kernelY in corRadiusYL..corRadiusYR) {
                val kernelValue = kernel[kernelX + radius][kernelY + radius]
                val pixel = image[x + kernelX + width * (y + kernelY)]
                red += red(pixel) * kernelValue
                green += green(pixel) * kernelValue
                blue += blue(pixel) * kernelValue
            }
        }
        image[x + width*y] = createColor(red.toInt(), green.toInt(), blue.toInt(), 255)

    }
}

