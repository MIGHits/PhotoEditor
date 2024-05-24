package com.example.photoeditor

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.MotionEvent
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.example.photoeditor.AffineTransform.AffineTransform
import com.example.photoeditor.Cube3d.Tria3d
import com.example.photoeditor.Cube3d.vec3d
import com.example.photoeditor.UsefulFuns.createColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt


class Affine {
    companion object {
        var radius = 0.0f
        val rgb = arrayOf(createColor(255,0,0,255), createColor(0,255,0,255), createColor(0,0,255,255))
        var curPoints: Array<vec2d> =
            arrayOf(vec2d(0.0f, 0.0f), vec2d(0.0f, 0.0f), vec2d(0.0f, 0.0f))
        var curIndex = 0
        var curTriangle: Int = 1
        var affineTriangle: Tria3d = Tria3d(
            vec3d(1.0f, 1.0f, 0.0f),
            vec3d(2.0f, 2.0f, 0.0f),
            vec3d(2.0f, 3.0f, 0.0f),
            vec2d(1.0f, 1.0f),
            vec2d(2.0f, 2.0f),
            vec2d(2.0f, 3.0f)
        )

         fun addPoint(x: Float, y: Float, imageView: ImageView, bitmap:Bitmap) {
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

        @SuppressLint("ClickableViewAccessibility")
        fun ImageView.setTouchable(imageView: ImageView, bitmap: Bitmap) {
            radius = bitmap.width.toFloat()
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

            affineTriangle.p[0].y = bitmap.height - affineTriangle.p[0].y
            affineTriangle.p[1].y = bitmap.height - affineTriangle.p[1].y
            affineTriangle.p[2].y = bitmap.height - affineTriangle.p[2].y
            affineTriangle.t[0].y = bitmap.height - affineTriangle.t[0].y
            affineTriangle.t[1].y = bitmap.height - affineTriangle.t[1].y
            affineTriangle.t[2].y = bitmap.height - affineTriangle.t[2].y

            val newAffine = AffineTransform(affineTriangle)
            return newAffine.process(bitmap)
        }

    }
}