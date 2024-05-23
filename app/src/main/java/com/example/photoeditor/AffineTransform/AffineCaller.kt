package com.example.photoeditor.AffineTransform

import android.graphics.Bitmap
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.example.photoeditor.Cube3d.Tria3d
import com.example.photoeditor.Cube3d.vec3d
import com.example.photoeditor.R
import com.example.photoeditor.vec2d

private var curPoints:Array<vec2d> = arrayOf(vec2d(0.0f,0.0f), vec2d(0.0f,0.0f), vec2d(0.0f,0.0f))
private var curIndex = 0
private var curTriangle:Int = 1
private var affineTriangle:Tria3d = Tria3d(
    vec3d(0.0f,0.0f,0.0f),
    vec3d(0.0f,0.0f,0.0f),
    vec3d(0.0f,0.0f,0.0f),
    vec2d(0.0f,0.0f),
    vec2d(0.0f,0.0f),
    vec2d(0.0f,0.0f)
)
private lateinit var imageView:ImageView
fun addPoint(x:Float,y:Float){

    curPoints[curIndex] = vec2d(x,y)
    curIndex++
    if (curIndex == curPoints.size) {
        curIndex = 0
        if (curTriangle == 1){
            affineTriangle.t = arrayOf(
                vec2d(curPoints[0].x, curPoints[0].y),
                vec2d(curPoints[1].x, curPoints[1].y),
                vec2d(curPoints[2].x, curPoints[2].y)
            )
        }else{
            affineTriangle.p = arrayOf(vec3d(curPoints[0].x, curPoints[0].y,0.0f),vec3d(
                curPoints[1].x,
                curPoints[1].y,0.0f),vec3d(curPoints[2].x, curPoints[2].y,0.0f))
        }
    }
}
fun setListenerToView(){
    imageView = findViewById(R.id.imageToShow)
    imageView.setOnTouchListener { v, event ->
        if (event.action == MotionEvent.ACTION_DOWN){
            val x =  bitmap.width*(event.x)/ imageView.width
            val y =  bitmap.height*(imageView.height - event.y)/ imageView.height
            Log.d("P","Point added")
            addPoint(x.toInt(),y.toInt())
            true
        }else{
            false
        }
    }
}
fun createFirstTriangle(view: View){
    curIndex = 0
    curTriangle = 1
}
fun createSecondTriangle(view: View){
    curIndex = 0
    curTriangle = 2
}
fun callAffine(): Bitmap {
    val newAffine = AffineTransform(affineTriangle)

    return newAffine.process(bitmap)

}
