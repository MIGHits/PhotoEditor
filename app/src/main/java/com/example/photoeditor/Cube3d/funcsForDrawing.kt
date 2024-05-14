package com.example.photoeditor.Cube3d

import android.content.Context
import android.graphics.Bitmap
import com.example.photoeditor.AffineTransform.AffineTransform
import com.example.photoeditor.Tria2d
import com.example.photoeditor.UsefulFuns.convertTria3dTo2d
import com.example.photoeditor.vec2d
import com.example.photoeditor.UsefulFuns.findMax3
import com.example.photoeditor.UsefulFuns.findMin3
import com.example.photoeditor.UsefulFuns.getBitmapFromAsset
import com.example.photoeditor.UsefulFuns.multiply4x4MatVec
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun isPointInTriangle(p:vec2d, triangle: Tria3d):Boolean{
    val a = triangle.p[0]
    val b = triangle.p[1]
    val c = triangle.p[2]
    val aSide = (a.y - b.y)*p.x + (b.x - a.x)*p.y + (a.x*b.y - b.x*a.y)
    val bSide = (b.y - c.y)*p.x + (c.x - b.x)*p.y + (b.x*c.y - c.x*b.y)
    val cSide = (c.y - a.y)*p.x + (a.x - c.x)*p.y + (c.x*a.y - a.x*c.y)
    return (aSide >= 0 && bSide >= 0 && cSide >= 0 ) || (aSide < 0 && bSide < 0 && cSide < 0)
}
fun findBox2D(triangle: Tria3d, width:Int, height:Int):Box2D{
    var ptOne = triangle.p[0]
    var ptTwo = triangle.p[1]
    var ptThree = triangle.p[2]
    val result = Box2D()
    result.topLeft = vec3d(arrayOf(
        max(findMin3(ptOne.x,ptTwo.x,ptThree.x),0.0f),
        max(findMin3(ptOne.y,ptTwo.y,ptThree.y),0.0f),
        0.0f))
    result.botRight = vec3d(arrayOf(
        min(findMax3(ptOne.x,ptTwo.x,ptThree.x),width.toFloat()),
        min(findMax3(ptOne.y,ptTwo.y,ptThree.y),height.toFloat()),
        0.0f))
    return result
}
fun drawTriangle(pixels:IntArray, triangle: Tria3d,triangleUV:Tria2d, width:Int, height:Int,context:Context){

    val textureBitmap = getBitmapFromAsset(context, "texture.png")

    val texture = IntArray(textureBitmap.width*textureBitmap.height)
    textureBitmap.getPixels(texture,0,textureBitmap.width,0,0,textureBitmap.width,textureBitmap.height)


    val aff = AffineTransform(triangleUV, convertTria3dTo2d(triangle))
    val box = findBox2D(triangle, width, height)
    var oldPos:Array<Double>
    for(y in box.topLeft.y.toInt()..box.botRight.y.toInt()){
        for(x in box.topLeft.x.toInt()..box.botRight.x.toInt()){
            if (isPointInTriangle(vec2d(x,y),triangle) ){
                oldPos = aff.oldPos(x,y)
                if (oldPos[0] < 86 && oldPos[0] >= 0.0 && oldPos[1] < 86 && oldPos[1] >= 0.0) {
                    pixels[x + y*width] = texture[oldPos[0].toInt() + oldPos[1].toInt()*86]
                }
            }
        }
    }
}


