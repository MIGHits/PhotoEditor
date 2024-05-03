package com.example.photoeditor

import CarouselAdapter
import OnItemClickListener
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.FieldPosition
import kotlin.math.abs

class FilterActivity: AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.filter_activity)
            window.setStatusBarColor(Color.parseColor("#b26c78"));
            window.setNavigationBarColor(Color.parseColor("#b26c78"));

            val imageUriString = intent.getStringExtra("imageUri")
            val imageUri = Uri.parse(imageUriString)
            val imageView = findViewById<ImageView>(R.id.customer_image)
            imageView.setImageURI(imageUri)

            val drawable = imageView.drawable as BitmapDrawable
            val bitmap = drawable.bitmap

            val rotationBar = findViewById<SeekBar>(R.id.rotationBar)
            val rotationBarProgress = findViewById<TextView>(R.id.rotationDegree)

            rotationBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    // Обновляем значение в TextView
                    rotationBarProgress.text = progress.toString()+"°"

                }

                override fun onStartTrackingTouch(rotationBar: SeekBar) {
                    // Вызывается, когда пользователь начинает перемещать ползунок
                }

                override fun onStopTrackingTouch(rotationBar: SeekBar) {
                    suspend fun rotation(degrees:Double,bitmap:Bitmap){
                        val rotatedBitmap =
                            ImageRotation.rotateBitmap(bitmap, degrees, this@FilterActivity)
                        imageView.setImageBitmap(rotatedBitmap)
                    }

                    rotationBar.isEnabled = false

                    lifecycleScope.launch {
                        rotation(rotationBar.progress.toDouble(),bitmap)
                    }

                    rotationBar.postDelayed({
                        rotationBar.isEnabled = true
                    }, 2000)
                }
            })

            val images = listOf(R.drawable.rotation_icon, R.drawable.scale_icon, R.drawable.negative_icon)
            val itemClickListeners = listOf(
                object : OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        rotationBar.visibility = View.VISIBLE
                        rotationBarProgress.visibility = View.VISIBLE
                    }
                },
                object : OnItemClickListener {
                    override fun onItemClick(position: Int) {

                    }
                },
                object : OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        // Логика для третьего элемента

                    }
                }
            )
            val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
            recyclerView.addItemDecoration(SpacesItemDecoration(5))
            val adapter = CarouselAdapter(images,itemClickListeners,this)
            recyclerView.adapter = adapter

            val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.layoutManager = layoutManager
            layoutManager.scrollToPositionWithOffset(0, resources.displayMetrics.widthPixels / 2)

            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(recyclerView)


        }




 }
