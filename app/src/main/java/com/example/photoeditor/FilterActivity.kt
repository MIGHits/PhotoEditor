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
import android.widget.ImageButton
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
import kotlinx.coroutines.*
import java.text.FieldPosition
import kotlin.math.abs

class FilterActivity: AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.filter_activity)

            window.setStatusBarColor(Color.parseColor("#b26c78"));
            window.setNavigationBarColor(Color.parseColor("#b26c78"));

            val imageView = findViewById<ImageView>(R.id.customer_image)

            val backButton = findViewById<ImageButton>(R.id.backButton)

            backButton.setOnClickListener{
                finish()
            }


            val imageUriString = intent.getStringExtra("imageUri")
            val imageUri = Uri.parse(imageUriString)
            imageView.setImageURI(imageUri)

            var drawable = imageView.drawable as BitmapDrawable
            var bitmap = drawable.bitmap

            val rotationBar = findViewById<SeekBar>(R.id.rotationBar)
            val rotationBarProgress = findViewById<TextView>(R.id.rotationDegree)

            rotationBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    rotationBarProgress.text = progress.toString()+"°"
                }

                override fun onStartTrackingTouch(rotationBar: SeekBar) {

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

            val images = listOf(R.drawable.rotation_icon, R.drawable.scale_icon,
                R.drawable.saturation,R.drawable.bright)
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
                        suspend fun saturationFilter(bitmap: Bitmap){
                            val saturatedBitmap =
                                SaturationFilter.saturation(bitmap)
                            imageView.setImageBitmap(saturatedBitmap)
                        }
                        lifecycleScope.launch {
                            saturationFilter(bitmap)
                        }
                        //drawable = imageView.drawable as BitmapDrawable
                        //bitmap = drawable.bitmap
                    }
                },
                object : OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        suspend fun brightness(bitmap: Bitmap){
                            val resultBitmap =
                                Brightness.brightnessFilter(bitmap)
                            imageView.setImageBitmap(resultBitmap)
                        }

                        lifecycleScope.launch {
                            brightness(bitmap)
                        }

                        //drawable = imageView.drawable as BitmapDrawable
                        // bitmap = drawable.bitmap
                    }
                },
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
