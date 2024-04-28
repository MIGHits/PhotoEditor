package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.net.Uri
import com.bumptech.glide.Glide
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import android.graphics.drawable.Drawable
import com.example.photoeditor.R
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs

class MainActivity : AppCompatActivity()
{
    private val PICK_IMAGE = 1
    private lateinit var imageView: ImageView
    private var imageUri : Uri? = null

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

        imageView = findViewById(R.id.image_save)
        val pickImageButton : Button = findViewById(R.id.photo_pick_button)

        pickImageButton.setOnClickListener {
            val pickPhoto = Intent(Intent.ACTION_PICK)
            pickPhoto.type = "image/*"
            startActivityForResult(pickPhoto, PICK_IMAGE)
        }

        imageView.setOnClickListener {
            if (imageUri != null)
            {
                imageView.setImageDrawable(null)
                imageUri = null
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE)
        {
            imageUri = data?.data

            Glide.with(this)
                .asBitmap()
                .load(imageUri)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val rotatedBitmap = rotateBitmap(resource,45.0)
                        imageView.setImageBitmap(rotatedBitmap)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Обработка отмены загрузки
                    }
                })
        }
    }

    fun rotateBitmap(sourceBitmap: Bitmap, angleInDegrees: Double): Bitmap {
        val angleInRadians = Math.toRadians(angleInDegrees)
        val cosAngle = Math.cos(angleInRadians)
        val sinAngle = Math.sin(angleInRadians)

        val sourceWidth = sourceBitmap.width
        val sourceHeight = sourceBitmap.height
        val centerX = sourceWidth / 2.0
        val centerY = sourceHeight / 2.0

        val rotatedWidth = (abs(sourceWidth * cosAngle) + abs(sourceHeight * sinAngle)).toInt()
        val rotatedHeight = (abs(sourceHeight * cosAngle) + abs(sourceWidth * sinAngle)).toInt()

        val rotatedBitmap = Bitmap.createBitmap(rotatedWidth, rotatedHeight, Bitmap.Config.ARGB_8888)

        for (y in 0 until rotatedHeight) {
            for (x in 0 until rotatedWidth) {
                val rotatedX = x - rotatedWidth / 2
                val rotatedY = y - rotatedHeight / 2
                val sourceX = (rotatedX * cosAngle - rotatedY * sinAngle + centerX).toInt()
                val sourceY = (rotatedY * cosAngle + rotatedX * sinAngle + centerY).toInt()

                if (sourceX in 0 until sourceWidth && sourceY in 0 until sourceHeight) {
                    rotatedBitmap.setPixel(x, y, sourceBitmap.getPixel(sourceX, sourceY))
                } else {
                    val backgroundColor = (this.window.decorView.background as? ColorDrawable)?.color ?: Color.WHITE
                    rotatedBitmap.setPixel(x, y, backgroundColor)
                }
            }
        }
        return rotatedBitmap
    }

}