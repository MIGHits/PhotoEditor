package com.example.photoeditor

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.MotionEvent
import android.widget.ImageView
import com.example.photoeditor.AffineTransform.AffineTransform
import com.example.photoeditor.Cube3d.Tria3d
import com.example.photoeditor.Cube3d.vec3d
import com.example.photoeditor.UsefulFuns.createColor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


class Affine {
    companion object {
        var radius = 0.0f
        val rgb = arrayOf(createColor(255,0,0,255), createColor(0,255,0,255), createColor(0,0,255,255))
        var curIndex = 0
        var curTriangle: Int = -1
        lateinit var affineTriangle: Tria3d

         fun addPoint(x: Float, y: Float, imageView: ImageView, bitmap:Bitmap) {
             if (curTriangle != -1){
                 val bitmapWithPoint = bitmap.copy(bitmap.config,true)

                 if (curTriangle == 1) {
                     affineTriangle.t[curIndex] = vec2d(x, y)
                     for(pointIndex in 0..2){
                         if(affineTriangle.t[pointIndex].x == -1.0f){
                             continue
                         }
                         createPoint(affineTriangle.t[pointIndex],bitmapWithPoint,rgb[pointIndex])
                     }
                 } else {
                     affineTriangle.p[curIndex] = vec3d(x, y, 0.0f)
                     for(pointIndex in 0..2){
                         if(affineTriangle.p[pointIndex].x == -1.0f){
                             continue
                         }
                         createPoint(affineTriangle.p[pointIndex],bitmapWithPoint,rgb[pointIndex])
                     }

                 }
                 imageView.setImageBitmap(bitmapWithPoint)
                 curIndex++
                 if (curIndex == 3) {
                     curIndex = 0
                 }
             }

        }

        @SuppressLint("ClickableViewAccessibility")
        fun ImageView.setTouchable(imageView: ImageView, bitmap: Bitmap) {
            radius = bitmap.width.toFloat()/2
            affineTriangle =  Tria3d(vec3d(-1.0f, -1.0f, 0.0f),
                                     vec3d(-1.0f, -1.0f, 0.0f),
                                     vec3d(-1.0f, -1.0f, 0.0f),
                                     vec2d(-1.0f, -1.0f),
                                     vec2d(-1.0f, -1.0f),
                                     vec2d(-1.0f, -1.0f))

            imageView.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val x = bitmap.width * (event.x) / imageView.width
                    val y = bitmap.height * (event.y) / imageView.height
                    addPoint(x, y, imageView, bitmap)
                    true
                } else {
                    false
                }
            }

        }
        fun createPoint(point:vec2d, bitmap:Bitmap, color:Int){

            val leftY = max(0.0f,point.y.toInt()-sqrt(radius)).toInt()
            val rightY = min(bitmap.height.toFloat()-1,point.y.toInt()+sqrt(radius)).toInt()
            val leftX = max(0.0f,point.x.toInt()-sqrt(radius)).toInt()
            val rightX = min(bitmap.width.toFloat()-1,point.x.toInt()+sqrt(radius)).toInt()

            for (py in  leftY.. rightY) {
                for (px in leftX .. rightX) {
                    val dx = px - point.x
                    val dy = py - point.y
                    val distanceSquared = dx * dx + dy * dy
                    if (distanceSquared <= radius) {
                        bitmap.setPixel(px,py, color)
                    }
                }
            }
        }
        fun createPoint(point:vec3d, bitmap:Bitmap, color:Int){

            val leftY = max(0.0f,point.y.toInt()-sqrt(radius)).toInt()
            val rightY = min(bitmap.height.toFloat()-1,point.y.toInt()+sqrt(radius)).toInt()
            val leftX = max(0.0f,point.x.toInt()-sqrt(radius)).toInt()
            val rightX = min(bitmap.width.toFloat()-1,point.x.toInt()+sqrt(radius)).toInt()

            for (py in  leftY.. rightY) {
                for (px in leftX .. rightX) {
                    val dx = px - point.x
                    val dy = py - point.y
                    val distanceSquared = dx * dx + dy * dy
                    if (distanceSquared <= radius) {
                        bitmap.setPixel(px,py, color)
                    }
                }
            }
        }
        fun createFirstTriangle() {
            curIndex = 0
            curTriangle = 1
        }

        fun createSecondTriangle() {
            curIndex = 0
            curTriangle = 2
        }

        suspend fun callAffine(bitmap: Bitmap): Bitmap {

            val finalTransformedTriangle =  Tria3d(vec3d(affineTriangle.p[0].x, affineTriangle.p[0].y, 0.0f),
                                                   vec3d(affineTriangle.p[1].x, affineTriangle.p[1].y, 0.0f),
                                                   vec3d(affineTriangle.p[2].x, affineTriangle.p[2].y, 0.0f),
                                                   vec2d(affineTriangle.t[0].x, affineTriangle.t[0].y),
                                                   vec2d(affineTriangle.t[1].x, affineTriangle.t[1].y),
                                                   vec2d(affineTriangle.t[2].x, affineTriangle.t[2].y))

            finalTransformedTriangle.p[0].y = bitmap.height - affineTriangle.p[0].y
            finalTransformedTriangle.p[1].y = bitmap.height - affineTriangle.p[1].y
            finalTransformedTriangle.p[2].y = bitmap.height - affineTriangle.p[2].y
            finalTransformedTriangle.t[0].y = bitmap.height - affineTriangle.t[0].y
            finalTransformedTriangle.t[1].y = bitmap.height - affineTriangle.t[1].y
            finalTransformedTriangle.t[2].y = bitmap.height - affineTriangle.t[2].y

            val newAffine = AffineTransform(finalTransformedTriangle)
            return newAffine.process(bitmap)
        }

    }
}