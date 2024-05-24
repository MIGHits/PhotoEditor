package com.example.photoeditor.Cube3d

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.photoeditor.Affine
import com.example.photoeditor.R
import com.example.photoeditor.UsefulFuns.getBitmapFromAsset
import com.example.photoeditor.vec2d
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2

class CubeActivity : AppCompatActivity() {
    private var oldX = 0.0f
    private var oldY = 0.0f
    private var absAngleX = 0.0f
    private var absAngleY = 0.0f
    private var absAngleZ = 0.0f

    private var countOfFingers = 0
    private var sign = 1
    private lateinit var lastVec:vec2d

    private val newScene = Scene3D()
    lateinit var texture:IntArray
    var texWidth:Int = 0
    lateinit var imageView:ImageView
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.cube_activity)
        imageView = findViewById(R.id.cubeView)
        imageView.setOnTouchListener { v, event ->
            when (event.action) {

                MotionEvent.ACTION_MOVE -> {
                    println(1)
                    if (event.pointerCount == 1 && (countOfFingers == 0 || countOfFingers == 2)){
                        countOfFingers = 1
                        oldX = event.x
                        oldY = event.y
                    }
                    if (event.pointerCount == 2 && (countOfFingers == 0 || countOfFingers == 1)){
                        countOfFingers = 2
                        lastVec = vec2d(abs(event.getX(0)-event.getX(1)),event.getY(0)-event.getY(1))
                    }
                    if (event.pointerCount == 1 && (countOfFingers == 1 || countOfFingers == 0)) {
                        countOfFingers = 1
                        absAngleY = (3.5f*(event.x - oldX)/imageView.width)
                        absAngleX = -(3.5f*(event.y - oldY)/imageView.width)
                        absAngleZ = 0.0f
                        println(absAngleY)
                        oldX = event.x
                        oldY = event.y

                        newScene.createRotationMatrix('y',absAngleY)
                        newScene.createRotationMatrix('x',absAngleX)
                        newScene.createRotationMatrix('z',absAngleZ)
                        startSimulation()
                    }

                    if (event.pointerCount == 2) {
                        countOfFingers = 2

                        if (event.getX(0)-event.getX(1) <= 0.0f){
                            sign = -1
                        }else{
                            sign = 1
                        }
                        val curVec = vec2d(abs(event.getX(0)-event.getX(1)),event.getY(0)-event.getY(1))
                        val deltaAngle = sign*(atan2(curVec.x,curVec.y) - atan2(lastVec.x,lastVec.y))
                        println(curVec.x)
                        println(curVec.y)
                        lastVec = curVec

                        absAngleY = 0.0f
                        absAngleX = 0.0f
                        absAngleZ = (deltaAngle*1000)/(imageView.width)
                        newScene.createRotationMatrix('y',absAngleY)
                        newScene.createRotationMatrix('x',absAngleX)
                        newScene.createRotationMatrix('z',absAngleZ)
                        startSimulation()
                    }

                }
                MotionEvent.ACTION_UP -> {

                    countOfFingers=0
                }
            }

            true
        }

        imageView.viewTreeObserver.addOnGlobalLayoutListener {
            initMesh()
            startSimulation()
        }
    }

    fun initMesh() {
        val textureBitmap = getBitmapFromAsset(this, "texture.png")
        texture = IntArray(textureBitmap.width*textureBitmap.height)
        textureBitmap.getPixels(texture,0,textureBitmap.width,0,0,textureBitmap.width,textureBitmap.height)
        texWidth = textureBitmap.width

        newScene.createCamera(vec3d(0.0f,0.0f,0.0f,0.0f))
        newScene.projectionMatrix(0.1f,1000.0f,90.0f,
            imageView.height.toFloat()/imageView.width.toFloat())
        newScene.createRotationMatrix('y',absAngleX)
        newScene.createRotationMatrix('x',absAngleY)

        newScene.loadMesh(arrayOf(
            Tria3d(
                vec3d(0.0f,0.0f,0.0f,1.0f),
                vec3d(0.0f,1.0f,0.0f,1.0f),
                vec3d(1.0f,1.0f,0.0f,1.0f),
                vec2d(0.0f,0.0f),
                vec2d(0.0f,199.0f),
                vec2d(199.0f,199.0f)
            ),
            Tria3d(
                vec3d(0.0f,0.0f,0.0f,1.0f),
                vec3d(1.0f,1.0f,0.0f,1.0f),
                vec3d(1.0f,0.0f,0.0f,1.0f),
                vec2d(0.0f,0.0f),
                vec2d(199.0f,199.0f),
                vec2d(199.0f,0.0f)
            ),

            Tria3d(
                vec3d(1.0f,0.0f,0.0f,1.0f),
                vec3d(1.0f,1.0f,0.0f,1.0f),
                vec3d(1.0f,1.0f,1.0f,1.0f),
                vec2d(200.0f,0.0f),
                vec2d(200.0f,199.0f),
                vec2d(399.0f,199.0f)
            ),
            Tria3d(
                vec3d(1.0f,0.0f,0.0f,1.0f),
                vec3d(1.0f,1.0f,1.0f,1.0f),
                vec3d(1.0f,0.0f,1.0f,1.0f),
                vec2d(200.0f,0.0f),
                vec2d(399.0f,199.0f),
                vec2d(399.0f,0.0f)
            ),

            Tria3d(
                vec3d(1.0f,0.0f,1.0f,1.0f),
                vec3d(1.0f,1.0f,1.0f,1.0f),
                vec3d(0.0f,1.0f,1.0f,1.0f),
                vec2d(400.0f,0.0f),
                vec2d(400.0f,199.0f),
                vec2d(599.0f,199.0f)
            ),
            Tria3d(
                vec3d(1.0f,0.0f,1.0f,1.0f),
                vec3d(0.0f,1.0f,1.0f,1.0f),
                vec3d(0.0f,0.0f,1.0f,1.0f),
                vec2d(400.0f,0.0f),
                vec2d(599.0f,199.0f),
                vec2d(599.0f,0.0f)
            ),

            Tria3d(
                vec3d(0.0f,0.0f,1.0f,1.0f),
                vec3d(0.0f,1.0f,1.0f,1.0f),
                vec3d(0.0f,1.0f,0.0f,1.0f),
                vec2d(0.0f,200.0f),
                vec2d(0.0f,399.0f),
                vec2d(199.0f,399.0f)
            ),
            Tria3d(
                vec3d(0.0f,0.0f,1.0f,1.0f),
                vec3d(0.0f,1.0f,0.0f,1.0f),
                vec3d(0.0f,0.0f,0.0f,1.0f),
                vec2d(0.0f,200.0f),
                vec2d(199.0f,399.0f),
                vec2d(199.0f,200.0f)
            ),

            Tria3d(
                vec3d(0.0f,1.0f,0.0f,1.0f),
                vec3d(0.0f,1.0f,1.0f,1.0f),
                vec3d(1.0f,1.0f,1.0f,1.0f),
                vec2d(200.0f,200.0f),
                vec2d(200.0f,399.0f),
                vec2d(399.0f,399.0f)
            ),
            Tria3d(
                vec3d(0.0f,1.0f,0.0f,1.0f),
                vec3d(1.0f,1.0f,1.0f,1.0f),
                vec3d(1.0f,1.0f,0.0f,1.0f),
                vec2d(200.0f,200.0f),
                vec2d(399.0f,399.0f),
                vec2d(399.0f,200.0f)
            ),

            Tria3d(
                vec3d(0.0f,0.0f,1.0f,1.0f),
                vec3d(0.0f,0.0f,0.0f,1.0f),
                vec3d(1.0f,0.0f,0.0f,1.0f),
                vec2d(400.0f,200.0f),
                vec2d(400.0f,399.0f),
                vec2d(599.0f,399.0f)
            ),
            Tria3d(
                vec3d(0.0f,0.0f,1.0f,1.0f),
                vec3d(1.0f,0.0f,0.0f,1.0f),
                vec3d(1.0f,0.0f,1.0f,1.0f),
                vec2d(400.0f,200.0f),
                vec2d(599.0f,399.0f),
                vec2d(599.0f,200.0f)
            )
        ))

    }
    fun startSimulation(){
        val changedBitmap = Bitmap.createBitmap(imageView.width, imageView.height, Bitmap.Config.ARGB_8888)
        lifecycleScope.launch {
            newScene.drawMesh(changedBitmap,texture,texWidth)
            imageView.setImageBitmap(changedBitmap)
        }
    }
}