package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_PICK
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.photoeditor.R
import com.example.photoeditor.Resize.resize

class MainActivity : AppCompatActivity()
{


    private lateinit var sourceImage:Bitmap




    private val PICK_IMAGE = 1
    private lateinit var imageView: ImageView

    private var imageHeight:Int = 0
    private var imageWidth:Int = 0





    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.cubic_page)

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


    fun takePhoto(view: View){
        val pickPhoto = Intent(ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, PICK_IMAGE)

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