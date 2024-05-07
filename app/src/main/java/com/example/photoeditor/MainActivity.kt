package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.argb
import android.graphics.drawable.ColorDrawable
import android.provider.MediaStore
import android.widget.ImageButton
import androidx.core.content.FileProvider
import com.example.photoeditor.FilterActivity
import com.example.photoeditor.R
import kotlin.math.abs
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity()
{
    private val REQUEST_CODE_IMAGE_PICK = 100
    private val REQUEST_IMAGE_CAPTURE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setStatusBarColor(Color.parseColor("#5e6666"));
        window.setNavigationBarColor(Color.parseColor("#5e6666"));
        val photoPickButton = findViewById<ImageButton>(R.id.galleryButton)
        val cameraButton =  findViewById<ImageButton>(R.id.cameraButton)

        cameraButton.setOnClickListener{
            dispatchTakePictureIntent()
        }

        photoPickButton.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri? {
        val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
        tempFile.deleteOnExit()
        val out = FileOutputStream(tempFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.close()
        return FileProvider.getUriForFile(this, "com.example.yourapp.fileprovider", tempFile)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val imageUri:Uri?
        val intent:Intent

        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
             imageUri = data?.data
            if (imageUri != null) {
                 intent = Intent(this, FilterActivity::class.java)
                intent.putExtra("imageUri", imageUri.toString())
                startActivity(intent)
            }
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            if ( imageBitmap != null) {
                 imageUri = saveBitmapToFile(imageBitmap)
                 intent = Intent(this, FilterActivity::class.java)
                intent.putExtra("imageUri", imageUri.toString())
                startActivity(intent)
            }

        }
    }

}



