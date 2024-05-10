package com.example.photoeditor.Cube3d

import android.graphics.Bitmap
import com.example.photoeditor.UsefulFuns.calculateLengthOfVec
import com.example.photoeditor.vec2d
import com.example.photoeditor.UsefulFuns.findMax3
import com.example.photoeditor.UsefulFuns.findMin3
import com.example.photoeditor.UsefulFuns.multiply4x4MatVec
import com.example.photoeditor.UsefulFuns.normilazeVec
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun drawLine(source:Bitmap, pointOne: vec3d, pointTwo: vec3d, color:Int):Bitmap{
    val changedBitmap = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val deltaX = pointTwo.x - pointOne.x
    val deltaY = pointTwo.y - pointOne.y
    val absDeltaX = abs(deltaX)
    val absDeltaY = abs(deltaY)

    var accretion = 0.0

    if (absDeltaX >= absDeltaY){
        var y = pointOne.y
        val direction:Int
        if (deltaY != 0.0f){
            if (deltaY > 0){
                direction = 1
            }else{
                direction = -1
            }
        }else{
            direction = 0
        }
        val addElement:Int
        val endX:Float
        var curX = pointOne.x
        if (deltaX > 0){
            endX = pointTwo.x
            addElement = 1
        }else{
            endX = pointTwo.x
            addElement = -1
        }
        while(curX * addElement <= endX * addElement){
            println(curX)
            source.setPixel(curX.toInt(),y.toInt(),color)
            accretion += absDeltaY
            if (accretion >= absDeltaX){
                accretion -= absDeltaX
                y += direction
            }
            curX += addElement
        }
    }else{
        var x = pointOne.x
        val direction:Int
        if (deltaX != 0.0f){
            if (deltaX > 0){
                direction = 1
            }else{
                direction = -1
            }
        }else{
            direction = 0
        }
        val addElement:Int
        val endY:Float
        var curY = pointOne.y
        if (deltaY > 0){
            endY = pointTwo.y
            addElement = 1
        }else{
            endY = pointTwo.y
            addElement = -1
        }
        while(curY * addElement<= endY * addElement ){
            println(curY)
            source.setPixel(x.toInt(),curY.toInt(),color)
            accretion += absDeltaX
            if (accretion >= absDeltaY){
                accretion -= absDeltaY
                x += direction
            }
            curY += addElement
        }
    }
    return changedBitmap
}
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
fun drawTriangle(pixels:IntArray, triangle: Tria3d, width:Int, height:Int){
    val box = findBox2D(triangle, width, height)
    for(y in box.topLeft.y.toInt()..box.botRight.y.toInt()){
        for(x in box.topLeft.x.toInt()..box.botRight.x.toInt()){
            if (isPointInTriangle(vec2d(x,y),triangle) ){
                pixels[x + y*width] = triangle.clr
            }
        }
    }
}
fun drawTriangle1(image:Bitmap, triangle: Tria3d){
    drawLine(image,triangle.p[0],triangle.p[1],triangle.clr)
    drawLine(image,triangle.p[1],triangle.p[2],triangle.clr)
    drawLine(image,triangle.p[2],triangle.p[0],triangle.clr)
}
//-------------------------------------------------------------------------------

fun applyTransformMatrixToTria(oldTria:Tria3d,matrix:mat4x4):Tria3d{
    val newTriaTransformed = Tria3d(arrayOf(
        vec3d(arrayOf(0.0f,0.0f,0.0f)),
        vec3d(arrayOf(0.0f,0.0f,0.0f)),
        vec3d(arrayOf(0.0f,0.0f,0.0f))),
        oldTria.clr
    )
    multiply4x4MatVec(oldTria.p[0],newTriaTransformed.p[0],matrix)
    multiply4x4MatVec(oldTria.p[1],newTriaTransformed.p[1],matrix)
    multiply4x4MatVec(oldTria.p[2],newTriaTransformed.p[2],matrix)
    return newTriaTransformed
}

