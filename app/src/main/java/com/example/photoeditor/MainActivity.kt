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
import com.example.photoeditor.FilterActivity
import com.example.photoeditor.R
import kotlin.math.abs
import kotlinx.coroutines.*
class MainActivity : AppCompatActivity()
{
    private val REQUEST_CODE_IMAGE_PICK = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setStatusBarColor(Color.parseColor("#b26c78"));
        window.setNavigationBarColor(Color.parseColor("#b26c78"));
        val photoPickButton = findViewById<Button>(R.id.photo_pick_button)
        photoPickButton.setOnClickListener {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                val intent = Intent(this, FilterActivity::class.java)
                intent.putExtra("imageUri", imageUri.toString())
                startActivity(intent)
            }
        }
    }

}



