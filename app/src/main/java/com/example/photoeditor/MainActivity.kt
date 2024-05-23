package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.photoeditor.Cube3d.Scene3D
import com.example.photoeditor.Cube3d.Tria3d
import com.example.photoeditor.Cube3d.vec3d
import com.example.photoeditor.R
import com.example.photoeditor.Resize.resize
import com.example.photoeditor.Tria2d
import com.example.photoeditor.UsefulFuns.createColor
import com.example.photoeditor.UsefulFuns.getBitmapFromAsset
import com.example.photoeditor.vec2d
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2

class MainActivity : AppCompatActivity()
{
    lateinit var texture:IntArray
    var texWidth:Int = 0
    private lateinit var sourceImage:Bitmap
    private lateinit var firstTriangle:Tria2d
    private lateinit var secondTriangle:Tria2d
    private var curTriangle:Int = 1
    private var curPoints:Array<vec2d> = arrayOf(vec2d(0.0f,0.0f),vec2d(0.0f,0.0f),vec2d(0.0f,0.0f))
    private var curIndex = 0
    private val PICK_IMAGE = 1
    private lateinit var imageView: ImageView

    private var imageHeight:Int = 0
    private var imageWidth:Int = 0

    private var oldX = 0.0f
    private var oldY = 0.0f
    private var absAngleX = 0.0f
    private var absAngleY = 0.0f
    private var absAngleZ = 0.0f

    private var countOfFingers = 0
    private var sign = 1
    private lateinit var lastVec:vec2d

    private val newScene = Scene3D()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imageView = findViewById(R.id.imageToShow)

        imageView.viewTreeObserver.addOnGlobalLayoutListener {
            //initMesh()
            //sampling()
        }

        imageView.setOnTouchListener { v, event ->
            //if (event.action == MotionEvent.ACTION_DOWN){
            //    val x =  imageWidth*(event.x)/imageView.width
            //    val y =  imageHeight*(imageView.height - event.y)/imageView.height
            //    Log.d("P","Point added")
            //    addPoint(x.toInt(),y.toInt())
            //    true
            //}else{
            //    false
            //}

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                }

                MotionEvent.ACTION_MOVE -> {
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
                    println("fdfdfdfdfd")
                }
            }

        true
        }

    }
    fun sampling(){
        val textureBitmap = getBitmapFromAsset(this, "MLAATest.png")

        imageView.setImageBitmap(textureBitmap)
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
                vec2d(199.0f,199.0f),
                createColor(255,0,0,255)
            ),
            Tria3d(
                vec3d(0.0f,0.0f,0.0f,1.0f),
                vec3d(1.0f,1.0f,0.0f,1.0f),
                vec3d(1.0f,0.0f,0.0f,1.0f),
                vec2d(0.0f,0.0f),
                vec2d(199.0f,199.0f),
                vec2d(199.0f,0.0f),
                createColor(255,0,0,255)
            ),

            Tria3d(
                vec3d(1.0f,0.0f,0.0f,1.0f),
                vec3d(1.0f,1.0f,0.0f,1.0f),
                vec3d(1.0f,1.0f,1.0f,1.0f),
                vec2d(200.0f,0.0f),
                vec2d(200.0f,199.0f),
                vec2d(399.0f,199.0f),
                createColor(255,255,0,255)
            ),
            Tria3d(
                vec3d(1.0f,0.0f,0.0f,1.0f),
                vec3d(1.0f,1.0f,1.0f,1.0f),
                vec3d(1.0f,0.0f,1.0f,1.0f),
                vec2d(200.0f,0.0f),
                vec2d(399.0f,199.0f),
                vec2d(399.0f,0.0f),
                createColor(255,255,0,255)
            ),

            Tria3d(
                vec3d(1.0f,0.0f,1.0f,1.0f),
                vec3d(1.0f,1.0f,1.0f,1.0f),
                vec3d(0.0f,1.0f,1.0f,1.0f),
                vec2d(400.0f,0.0f),
                vec2d(400.0f,199.0f),
                vec2d(599.0f,199.0f),
                createColor(0,255,0,255)
            ),
            Tria3d(
                vec3d(1.0f,0.0f,1.0f,1.0f),
                vec3d(0.0f,1.0f,1.0f,1.0f),
                vec3d(0.0f,0.0f,1.0f,1.0f),
                vec2d(400.0f,0.0f),
                vec2d(599.0f,199.0f),
                vec2d(599.0f,0.0f),
                createColor(0,255,0,255)
            ),

            Tria3d(
                vec3d(0.0f,0.0f,1.0f,1.0f),
                vec3d(0.0f,1.0f,1.0f,1.0f),
                vec3d(0.0f,1.0f,0.0f,1.0f),
                vec2d(0.0f,200.0f),
                vec2d(0.0f,399.0f),
                vec2d(199.0f,399.0f),
                createColor(0,255,0,255)
            ),
            Tria3d(
                vec3d(0.0f,0.0f,1.0f,1.0f),
                vec3d(0.0f,1.0f,0.0f,1.0f),
                vec3d(0.0f,0.0f,0.0f,1.0f),
                vec2d(0.0f,200.0f),
                vec2d(199.0f,399.0f),
                vec2d(199.0f,200.0f),
                createColor(0,255,255,255)
            ),

            Tria3d(
                vec3d(0.0f,1.0f,0.0f,1.0f),
                vec3d(0.0f,1.0f,1.0f,1.0f),
                vec3d(1.0f,1.0f,1.0f,1.0f),
                vec2d(200.0f,200.0f),
                vec2d(200.0f,399.0f),
                vec2d(399.0f,399.0f),
                createColor(0,255,255,255)
            ),
            Tria3d(
                vec3d(0.0f,1.0f,0.0f,1.0f),
                vec3d(1.0f,1.0f,1.0f,1.0f),
                vec3d(1.0f,1.0f,0.0f,1.0f),
                vec2d(200.0f,200.0f),
                vec2d(399.0f,399.0f),
                vec2d(399.0f,200.0f),
                createColor(0,0,255,255)
            ),

            Tria3d(
                vec3d(0.0f,0.0f,1.0f,1.0f),
                vec3d(0.0f,0.0f,0.0f,1.0f),
                vec3d(1.0f,0.0f,0.0f,1.0f),
                vec2d(400.0f,200.0f),
                vec2d(400.0f,399.0f),
                vec2d(599.0f,399.0f),
                createColor(0,0,255,255)
            ),
            Tria3d(
                vec3d(0.0f,0.0f,1.0f,1.0f),
                vec3d(1.0f,0.0f,0.0f,1.0f),
                vec3d(1.0f,0.0f,1.0f,1.0f),
                vec2d(400.0f,200.0f),
                vec2d(599.0f,399.0f),
                vec2d(599.0f,200.0f),
                createColor(255,0,255,255)
            )
        ))

        startSimulation()
    }
    fun takePhoto(view: View){
        val pickPhoto = Intent(ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, PICK_IMAGE)

    }
    fun addPoint(x:Float,y:Float){

        curPoints[curIndex] = vec2d(x,y)
        curIndex++
        if (curIndex == curPoints.size) {
            curIndex = 0
            if (curTriangle == 1){
                firstTriangle = Tria2d(vec2d(curPoints[0].x,curPoints[0].y),vec2d(curPoints[1].x,curPoints[1].y),vec2d(curPoints[2].x,curPoints[2].y))
            }else{
                secondTriangle = Tria2d(vec2d(curPoints[0].x,curPoints[0].y),vec2d(curPoints[1].x,curPoints[1].y),vec2d(curPoints[2].x,curPoints[2].y))
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
    fun startSimulation(){

        val changedBitmap = Bitmap.createBitmap(imageView.width, imageView.height, Bitmap.Config.ARGB_8888)
        newScene.drawMesh(changedBitmap,texture,texWidth)
        imageView.setImageBitmap(changedBitmap)
    }
    fun startChanges(view: View) {

        if (firstTriangle != null && secondTriangle != null){
             //val aff = AffineTransform(firstTriangle,secondTriangle)
             lifecycleScope.launch{
                 //imageView.setImageBitmap(aff.process(sourceImage))
             }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE)
        {
            val imageUri = data?.data
            sourceImage = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            imageHeight = sourceImage.height
            imageWidth = sourceImage.width
            imageView.setImageBitmap(resize(sourceImage,0.5,0.5))
        }
    }


}