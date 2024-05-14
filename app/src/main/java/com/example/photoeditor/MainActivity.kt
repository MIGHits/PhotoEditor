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
import com.example.photoeditor.AffineTransform.AffineTransform
import com.example.photoeditor.Cube3d.Scene3D
import com.example.photoeditor.Cube3d.Tria3d
import com.example.photoeditor.Cube3d.vec3d
import com.example.photoeditor.R
import com.example.photoeditor.Tria2d
import com.example.photoeditor.UsefulFuns.createColor
import com.example.photoeditor.vec2d
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity()
{
    private lateinit var sourceImage:Bitmap
    private lateinit var firstTriangle:Tria2d
    private lateinit var secondTriangle:Tria2d
    private var curTriangle:Int = 1
    private var curPoints:Array<vec2d> = arrayOf(vec2d(0,0),vec2d(0,0),vec2d(0,0))
    private var curIndex = 0
    private val PICK_IMAGE = 1
    private lateinit var imageView: ImageView

    private var imageHeight:Int = 0
    private var imageWidth:Int = 0

    private var oldX = 0.0f
    private var oldY = 0.0f
    private var absAngleX = 0.2f
    private var absAngleY = 0.0f

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
            initMesh()
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
                    println(oldX)
                    oldX = event.x
                    oldY = event.y
                }

                MotionEvent.ACTION_MOVE -> {

                    absAngleX += (3.5f*(event.x - oldX)/imageView.width)
                    absAngleY -= (3.5f*(event.y - oldY)/imageView.width)
                    oldX = event.x
                    oldY = event.y
                    newScene.createRotationMatrix('y',absAngleX)
                    newScene.createRotationMatrix('x',absAngleY)
                    startSimulation()

                }
            }
            true
        }

    }

    fun initMesh() {
        newScene.createCamera(0.1f,1000.0f,90.0f,imageView.height.toFloat()/imageView.width.toFloat(),vec3d(arrayOf(0.0f,0.0f,0.0f)))
        newScene.createRotationMatrix('y',absAngleX)
        newScene.createRotationMatrix('x',absAngleY)
        newScene.loadMesh(arrayOf(
            Tria3d(arrayOf(
                vec3d(arrayOf(0.0f,0.0f,0.0f)),
                vec3d(arrayOf(0.0f,1.0f,0.0f)),
                vec3d(arrayOf(1.0f,1.0f,0.0f))),
                createColor(255,0,0,255)
            ),
            Tria3d(arrayOf(
                vec3d(arrayOf(0.0f,0.0f,0.0f)),
                vec3d(arrayOf(1.0f,1.0f,0.0f)),
                vec3d(arrayOf(1.0f,0.0f,0.0f))),
                createColor(255,0,0,255)
            ),

            Tria3d(arrayOf(
                vec3d(arrayOf(1.0f,0.0f,0.0f)),
                vec3d(arrayOf(1.0f,1.0f,0.0f)),
                vec3d(arrayOf(1.0f,1.0f,1.0f))),
                createColor(255,255,0,255)
            ),
            Tria3d(arrayOf(
                vec3d(arrayOf(1.0f,0.0f,0.0f)),
                vec3d(arrayOf(1.0f,1.0f,1.0f)),
                vec3d(arrayOf(1.0f,0.0f,1.0f))),
                createColor(255,255,0,255)
            ),

            Tria3d(arrayOf(
                vec3d(arrayOf(1.0f,0.0f,1.0f)),
                vec3d(arrayOf(1.0f,1.0f,1.0f)),
                vec3d(arrayOf(0.0f,1.0f,1.0f))),
                createColor(0,255,0,255)
            ),
            Tria3d(arrayOf(
                vec3d(arrayOf(1.0f,0.0f,1.0f)),
                vec3d(arrayOf(0.0f,1.0f,1.0f)),
                vec3d(arrayOf(0.0f,0.0f,1.0f))),
                createColor(0,255,0,255)
            ),

            Tria3d(arrayOf(
                vec3d(arrayOf(0.0f,0.0f,1.0f)),
                vec3d(arrayOf(0.0f,1.0f,1.0f)),
                vec3d(arrayOf(0.0f,1.0f,0.0f))),
                createColor(0,255,255,255)
            ),
            Tria3d(arrayOf(
                vec3d(arrayOf(0.0f,0.0f,1.0f)),
                vec3d(arrayOf(0.0f,1.0f,0.0f)),
                vec3d(arrayOf(0.0f,0.0f,0.0f))),
                createColor(0,255,255,255)
            ),

            Tria3d(arrayOf(
                vec3d(arrayOf(0.0f,1.0f,0.0f)),
                vec3d(arrayOf(0.0f,1.0f,1.0f)),
                vec3d(arrayOf(1.0f,1.0f,1.0f))),
                createColor(0,0,255,255)
            ),
            Tria3d(arrayOf(
                vec3d(arrayOf(0.0f,1.0f,0.0f)),
                vec3d(arrayOf(1.0f,1.0f,1.0f)),
                vec3d(arrayOf(1.0f,1.0f,0.0f))),
                createColor(0,0,255,255)
            ),

            Tria3d(arrayOf(
                vec3d(arrayOf(0.0f,0.0f,1.0f)),
                vec3d(arrayOf(0.0f,0.0f,0.0f)),
                vec3d(arrayOf(1.0f,0.0f,0.0f))),
                createColor(255,0,255,255)
            ),
            Tria3d(arrayOf(
                vec3d(arrayOf(0.0f,0.0f,1.0f)),
                vec3d(arrayOf(1.0f,0.0f,0.0f)),
                vec3d(arrayOf(1.0f,0.0f,1.0f))),
                createColor(255,0,255,255)
            )
        ))
        newScene.loadUV(arrayOf(
            Tria2d(arrayOf(
                vec2d(0,0),
                vec2d(0,85),
                vec2d(85,85)
            )),
            Tria2d(arrayOf(
                vec2d(0,0),
                vec2d(85,85),
                vec2d(85,0)
            )),
            Tria2d(arrayOf(
                vec2d(0,0),
                vec2d(0,85),
                vec2d(85,85)
            )),
            Tria2d(arrayOf(
                vec2d(0,0),
                vec2d(85,85),
                vec2d(85,0)
            )),
            Tria2d(arrayOf(
                vec2d(0,0),
                vec2d(0,85),
                vec2d(85,85)
            )),
            Tria2d(arrayOf(
                vec2d(0,0),
                vec2d(85,85),
                vec2d(85,0)
            )),
            Tria2d(arrayOf(
                vec2d(0,0),
                vec2d(0,85),
                vec2d(85,85)
            )),
            Tria2d(arrayOf(
                vec2d(0,0),
                vec2d(85,85),
                vec2d(85,0)
            )),
            Tria2d(arrayOf(
                vec2d(0,0),
                vec2d(0,85),
                vec2d(85,85)
            )),
            Tria2d(arrayOf(
                vec2d(0,0),
                vec2d(85,85),
                vec2d(85,0)
            )),
            Tria2d(arrayOf(
                vec2d(0,0),
                vec2d(0,85),
                vec2d(85,85)
            )),
            Tria2d(arrayOf(
                vec2d(0,0),
                vec2d(85,85),
                vec2d(85,0)
            ))
        ))
        startSimulation()
    }
    fun takePhoto(view: View){
        val pickPhoto = Intent(ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, PICK_IMAGE)

    }
    fun addPoint(x:Int,y:Int){

        curPoints[curIndex] = vec2d(x,y)
        curIndex++
        if (curIndex == curPoints.size) {
            curIndex = 0
            if (curTriangle == 1){
                firstTriangle = Tria2d(curPoints)
            }else{
                secondTriangle = Tria2d(curPoints)
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
        newScene.drawMesh(changedBitmap,this)
        imageView.setImageBitmap(changedBitmap)
    }
    fun startChanges(view: View) {

        if (firstTriangle != null && secondTriangle != null){
             val aff = AffineTransform(firstTriangle,secondTriangle)
             lifecycleScope.launch{
                 imageView.setImageBitmap(aff.process(sourceImage))
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
            imageView.setImageBitmap(sourceImage)
        }
    }


}