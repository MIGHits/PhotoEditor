package com.example.photoeditor.SuperSampling

import android.graphics.Bitmap
import android.graphics.Color.blue
import android.graphics.Color.green
import android.graphics.Color.red
import com.example.photoeditor.UsefulFuns.createColor
import kotlin.math.pow


val e =2.71828
val pi = 3.14159
fun gaussianFunction(x: Int, y: Int, sigma: Double): Double {
    return e.pow(-(x * x + y * y) / (2 * sigma * sigma)) / (2 * pi * sigma * sigma)
}
fun comparePixel(p1:Int,p2:Int):Boolean {
    val cmprFac = 255*0.2f
    if ((red(p1) >= red(p2)-cmprFac && red(p1) <= red(p2)+cmprFac)
        && (blue(p1) >= blue(p2)-cmprFac && blue(p1) <= blue(p2)+cmprFac)
        && (green(p1) >= green(p2)-cmprFac && green(p1) <= green(p2)+cmprFac)){
        return false
    }
    return true
}
class MLAA(){

    val sigma = 1.0
    val radius = (1 * 2)
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
    fun comparePixel(image:Bitmap,x:Int,y:Int):Boolean {
        if (x >= image.width-1 || y >= image.height-1) {
            return false
        }
        val isBot = comparePixel(image.getPixel(x, y), image.getPixel(x, y + 1))
        val isRight = comparePixel(image.getPixel(x + 1, y), image.getPixel(x, y))

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


