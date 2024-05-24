package com.example.photoeditor

import CarouselAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
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
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.*


data class ItemData(val image:Int, val title:String)
class FilterActivity: AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.filter_activity)

        window.setStatusBarColor(Color.parseColor("#304352"));
        window.setNavigationBarColor(Color.parseColor("#d7d2cc"));

       fun saveImageToMediaStore(bitmap: Bitmap) {
            val contentResolver = contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "filtered_image.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val imageUri: Uri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return

            try {
                val outputStream = contentResolver.openOutputStream(imageUri)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                outputStream?.flush()
                outputStream?.close()
                Toast.makeText(this, "Изображение сохранено в MediaStore", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Не удалось сохранить изображение", Toast.LENGTH_SHORT).show()
            }
        }

        val effects:RecyclerView = findViewById(R.id.effectsMenu)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        val imageView = findViewById<ImageView>(R.id.customer_image)

        val backButton = findViewById<ImageButton>(R.id.backButton)

        val saveButton = findViewById<ImageButton>(R.id.saveButton)

        val loading = findViewById<LottieAnimationView>(R.id.loading_animation)

        backButton.setOnClickListener {
            finish()
        }

        val imageUriString = intent.getStringExtra("imageUri")
        val imageUri = Uri.parse(imageUriString)
        imageView.setImageURI(imageUri)

        var drawable = imageView.drawable as BitmapDrawable
        var bitmap = drawable.bitmap

        val declineButton = findViewById<ImageButton>(R.id.decline)
        val acceptButton = findViewById<ImageButton>(R.id.accept)

        declineButton.setOnClickListener{
            effects.visibility = View.INVISIBLE
            recyclerView.visibility = View.VISIBLE
            declineButton.visibility = View.INVISIBLE
            acceptButton.visibility = View.INVISIBLE
            imageView.setImageBitmap(bitmap)
        }

        acceptButton.setOnClickListener{
            drawable = imageView.drawable as BitmapDrawable
            bitmap = drawable.bitmap
            declineButton.visibility = View.INVISIBLE
            acceptButton.visibility = View.INVISIBLE
            effects.visibility = View.INVISIBLE
            recyclerView.visibility = View.VISIBLE
        }

        saveButton.setOnClickListener{
            saveImageToMediaStore(bitmap)
        }

        val rotationBar = findViewById<SeekBar>(R.id.rotationBar)
        val rotationBarProgress = findViewById<TextView>(R.id.rotationDegree)
        val parentLayout:ViewGroup = rotationBar.parent as ViewGroup

        val rootView = findViewById<View>(android.R.id.content)
        rootView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                rotationBar.visibility = View.INVISIBLE
                rotationBarProgress.visibility = View.INVISIBLE
                return@setOnTouchListener true
            }
            false
        }

        rotationBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                rotationBarProgress.text = progress.toString() + "°"
            }

            override fun onStartTrackingTouch(rotationBar: SeekBar) {}

            override fun onStopTrackingTouch(rotationBar: SeekBar) {
                suspend fun rotation(degrees: Double, bitmap: Bitmap) {
                    imageView.visibility = View.INVISIBLE
                    loading.visibility = View.VISIBLE
                    val rotatedBitmap =
                        ImageRotation.rotateBitmap(bitmap, degrees)
                    imageView.setImageBitmap(rotatedBitmap)
                    loading.visibility = View.INVISIBLE
                    imageView.visibility = View.VISIBLE
                    rotationBar.isEnabled = true
                    declineButton.visibility = View.VISIBLE
                    acceptButton.visibility = View.VISIBLE
                }

                rotationBar.isEnabled = false

                lifecycleScope.launch {
                    rotation(rotationBar.progress.toDouble(), bitmap)
                }
            }
        })

        val itemList:List<ItemData> = listOf(
            ItemData(R.drawable.rotation_icon,"Поворот"),
            ItemData(R.drawable.scale_icon,"Масштаб"),
            ItemData(R.drawable.saturation,"Насыщенность"),
            ItemData(R.drawable.bright,"Яркость"),
            ItemData(R.drawable.filters_icon,"Фильтры"),
            ItemData(R.drawable.contrast,"Контраст")
        )

        recyclerView.addItemDecoration(SpacesItemDecoration(5))
        val adapter = CarouselAdapter(itemList, this)
        recyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        layoutManager.scrollToPositionWithOffset(0, resources.displayMetrics.widthPixels / 2)

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        val effectsList:List<ItemData> = listOf(
            ItemData(R.drawable.negativ_filter,"Негатив"),
            ItemData(R.drawable.red_filter,"Красный"),
            ItemData(R.drawable.green_filter,"Зеленый"),
            ItemData(R.drawable.blue_filter,"Синий"),
            ItemData(R.drawable.gray_scale,"Оттенки серого"),
            ItemData(R.drawable.mosaic_filter,"Мозаика"),
            ItemData(R.drawable.sepia_filter,"Сепия"),
        )


        effects.addItemDecoration(SpacesItemDecoration(5))
        val effectsAdapter = EffectsMenuAdapter(effectsList,this)
        effects.adapter = effectsAdapter

        val effectsLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        effects.layoutManager = effectsLayoutManager
        layoutManager.scrollToPositionWithOffset(0,resources.displayMetrics.widthPixels / 2)
        val effectsSnapHelper = LinearSnapHelper()
        effectsSnapHelper.attachToRecyclerView(effects)

        effectsAdapter.clickListener = object: CarouselAdapter.OnItemClickListener {
            override fun onItemClick(position: Int, item: Int) {
                when(position){
                    0->{
                        suspend fun negative(bitmap: Bitmap) {
                            imageView.visibility = View.INVISIBLE
                            loading.visibility = View.VISIBLE
                            val resultBitmap =
                                ColorFilters.negative(bitmap)
                            imageView.setImageBitmap(resultBitmap)
                            imageView.visibility = View.VISIBLE
                            loading.visibility = View.INVISIBLE
                        }
                        lifecycleScope.launch {
                            negative(bitmap)
                        }
                    }
                    1->{
                        suspend fun red(bitmap: Bitmap) {
                            imageView.visibility = View.INVISIBLE
                            loading.visibility = View.VISIBLE
                            val resultBitmap =
                                ColorFilters.redFilter(bitmap)
                            imageView.setImageBitmap(resultBitmap)
                            imageView.visibility = View.VISIBLE
                            loading.visibility = View.INVISIBLE
                        }
                        lifecycleScope.launch {
                            red(bitmap)
                        }
                    }
                    2->{
                        suspend fun green(bitmap: Bitmap) {
                            imageView.visibility = View.INVISIBLE
                            loading.visibility = View.VISIBLE
                            val resultBitmap =
                                ColorFilters.greenFilter(bitmap)
                            imageView.setImageBitmap(resultBitmap)
                            imageView.visibility = View.VISIBLE
                            loading.visibility = View.INVISIBLE
                        }
                        lifecycleScope.launch {
                            green(bitmap)
                        }
                    }
                    3->{
                        suspend fun blue(bitmap: Bitmap) {
                            imageView.visibility = View.INVISIBLE
                            loading.visibility = View.VISIBLE
                            val resultBitmap =
                                ColorFilters.blueFilter(bitmap)
                            imageView.setImageBitmap(resultBitmap)
                            imageView.visibility = View.VISIBLE
                            loading.visibility = View.INVISIBLE
                        }
                        lifecycleScope.launch {
                            blue(bitmap)
                        }
                    }
                    4->{
                        suspend fun grayScale(bitmap: Bitmap) {
                            imageView.visibility = View.INVISIBLE
                            loading.visibility = View.VISIBLE
                            val resultBitmap =
                                ColorFilters.grayscale(bitmap)
                            imageView.setImageBitmap(resultBitmap)
                            imageView.visibility = View.VISIBLE
                            loading.visibility = View.INVISIBLE
                        }
                        lifecycleScope.launch {
                            grayScale(bitmap)
                        }
                    }
                    5->{
                        suspend fun mosaic(bitmap: Bitmap) {
                            imageView.visibility = View.INVISIBLE
                            loading.visibility = View.VISIBLE
                            val resultBitmap =
                                ColorFilters.mosaic(bitmap,20)
                            imageView.setImageBitmap(resultBitmap)
                            imageView.visibility = View.VISIBLE
                            loading.visibility = View.INVISIBLE
                        }
                        lifecycleScope.launch {
                            mosaic(bitmap)
                        }
                    }
                    6->{
                        suspend fun sepia(bitmap: Bitmap) {
                            imageView.visibility = View.INVISIBLE
                            loading.visibility = View.VISIBLE
                            val resultBitmap =
                                ColorFilters.sepia(bitmap)
                            imageView.setImageBitmap(resultBitmap)
                            imageView.visibility = View.VISIBLE
                            loading.visibility = View.INVISIBLE
                        }
                        lifecycleScope.launch {
                          sepia(bitmap)
                        }
                    }
                }
            }

        }

        adapter.clickListener = object : CarouselAdapter.OnItemClickListener {
            override fun onItemClick(position: Int,item: Int) {
                when(position) {
                    0-> {
                        rotationBar.visibility = View.VISIBLE
                        rotationBarProgress.visibility = View.VISIBLE
                    }
                    2->{
                        suspend fun saturationFilter(bitmap: Bitmap) {
                            imageView.visibility = View.INVISIBLE
                            loading.visibility = View.VISIBLE
                            val saturatedBitmap =
                                SaturationFilter.saturation(bitmap,2f)
                            imageView.setImageBitmap(saturatedBitmap)
                            imageView.visibility = View.VISIBLE
                            loading.visibility = View.INVISIBLE
                            declineButton.visibility = View.VISIBLE
                            acceptButton.visibility = View.VISIBLE
                        }
                        lifecycleScope.launch {
                            saturationFilter(bitmap)
                        }
                    }
                    3->{
                        suspend fun brightness(bitmap: Bitmap) {
                            imageView.visibility = View.INVISIBLE
                            loading.visibility = View.VISIBLE
                            val resultBitmap =
                               ColorFilters.brightness(bitmap,100)
                            imageView.setImageBitmap(resultBitmap)
                            imageView.visibility = View.VISIBLE
                            loading.visibility = View.INVISIBLE
                            declineButton.visibility = View.VISIBLE
                            acceptButton.visibility = View.VISIBLE
                        }
                        lifecycleScope.launch {
                            brightness(bitmap)
                        }
                    }
                    4->{
                        recyclerView.visibility = View.INVISIBLE
                        effects.visibility = View.VISIBLE
                        declineButton.visibility = View.VISIBLE
                        acceptButton.visibility = View.VISIBLE

                    }
                    5->{
                        suspend fun contrast(bitmap: Bitmap) {
                            imageView.visibility = View.INVISIBLE
                            loading.visibility = View.VISIBLE
                            val resultBitmap =
                                ColorFilters.contrast(bitmap,20)
                            imageView.setImageBitmap(resultBitmap)

                            imageView.visibility = View.VISIBLE
                            loading.visibility = View.INVISIBLE

                            declineButton.visibility = View.VISIBLE
                            acceptButton.visibility = View.VISIBLE
                        }
                        lifecycleScope.launch {
                            contrast(bitmap)
                        }
                    }
                }
            }
        }
    }
}


