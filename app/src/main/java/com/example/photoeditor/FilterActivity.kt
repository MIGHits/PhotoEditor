package com.example.photoeditor

import CarouselAdapter
import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.photoeditor.Affine.Companion.setTouchable
import com.example.photoeditor.Retouch.Companion.setRetouchable
import kotlinx.coroutines.*
import android.view.animation.AnimationUtils
import java.util.Stack


data class ItemData(val image:Int, val title:String)


class FilterActivity: AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.filter_activity)

        window.setStatusBarColor(Color.parseColor("#304352"))
        window.setNavigationBarColor(Color.parseColor("#d7d2cc"))
        val animation = AnimationUtils.loadAnimation(this,R.anim.fade_out)

        var resultBitmap:Bitmap

        val undoStack: Stack<Uri> = Stack()
        val redoStack: Stack<Uri> = Stack()

        val  undoButton:ImageButton = findViewById(R.id.undo)
        val redoButton:ImageButton = findViewById(R.id.redoButton)

        val effects: RecyclerView = findViewById(R.id.effectsMenu)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        val imageView = findViewById<ImageView>(R.id.customer_image)

        val backButton = findViewById<ImageButton>(R.id.backButton)

        val saveButton = findViewById<ImageButton>(R.id.saveButton)

        val firstTriangleBtn = findViewById<ImageView>(R.id.firstTriangle)
        val firstTriangleInfo = findViewById<TextView>(R.id.firsTextInfo)

        val secondTriangleBtn = findViewById<ImageView>(R.id.secondTriangle)
        val secondTriangleInfo = findViewById<TextView>(R.id.secondTextInfo)

        val startBtn = findViewById<ImageView>(R.id.AffineStart)
        val startInfo = findViewById<TextView>(R.id.AffineTextInfo)

        val loading = findViewById<LottieAnimationView>(R.id.loading_animation)

        val imageUriString = intent.getStringExtra("imageUri")

        val imageUri = Uri.parse(imageUriString)
        imageView.setImageURI(imageUri)

        var drawable = imageView.drawable as BitmapDrawable
        var bitmap = drawable.bitmap

        val itemList: List<ItemData> = listOf(
            ItemData(R.drawable.rotation_icon, "Поворот"),
            ItemData(R.drawable.scale_icon, "Масштаб"),
            ItemData(R.drawable.saturation, "Насыщенность"),
            ItemData(R.drawable.bright, "Яркость"),
            ItemData(R.drawable.filters_icon, "Фильтры"),
            ItemData(R.drawable.contrast, "Контраст"),
            ItemData(R.drawable.retouch, "Ретуширование"),
            ItemData(R.drawable.sharpen,"Нерезкое маскирование"),
            ItemData(R.drawable.affine,"Афинные преобразования"))

        recyclerView.addItemDecoration(SpacesItemDecoration(5))
        val adapter = CarouselAdapter(itemList, this)
        recyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        layoutManager.scrollToPositionWithOffset(0, resources.displayMetrics.widthPixels / 2)

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        val effectsList: List<ItemData> = listOf(
            ItemData(R.drawable.negativ_filter, "Негатив"),
            ItemData(R.drawable.red_filter, "Красный"),
            ItemData(R.drawable.green_filter, "Зеленый"),
            ItemData(R.drawable.blue_filter, "Синий"),
            ItemData(R.drawable.gray_scale, "Оттенки серого"),
            ItemData(R.drawable.mosaic_filter, "Мозаика"),
            ItemData(R.drawable.sepia_filter, "Сепия"),
            ItemData(R.drawable.gausian,"Размытие")
        )


        effects.addItemDecoration(SpacesItemDecoration(5))
        val effectsAdapter = EffectsMenuAdapter(effectsList, this)
        effects.adapter = effectsAdapter

        val effectsLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        effects.layoutManager = effectsLayoutManager
        layoutManager.scrollToPositionWithOffset(0, resources.displayMetrics.widthPixels / 2)
        val effectsSnapHelper = LinearSnapHelper()
        effectsSnapHelper.attachToRecyclerView(effects)


        var photoUri: Uri? = null
        lifecycleScope.launch {
            val initUri = History.saveImageToCache(this@FilterActivity,bitmap)
            History.addImageToUndoStack(initUri, undoStack, redoStack)
        }


        val declineButton = findViewById<ImageButton>(R.id.decline)
        val acceptButton = findViewById<ImageButton>(R.id.accept)

        val filterBar = findViewById<SeekBar>(R.id.filterBar)
        val barProgress = findViewById<TextView>(R.id.filterBarProgress)

        val retouchBar = findViewById<SeekBar>(R.id.retouchCoeffBar)
        val retouchBarProgress = findViewById<TextView>(R.id.retouchBarProgress)

        filterBar.parent as ViewGroup

        val rootView = findViewById<View>(android.R.id.content)
        rootView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                filterBar.visibility = View.INVISIBLE
                barProgress.visibility = View.INVISIBLE
                retouchBar.visibility = View.INVISIBLE
                retouchBarProgress.visibility = View.INVISIBLE
                return@setOnTouchListener true
            }
            false
        }


        fun saveImageToMediaStore(bitmap: Bitmap) {

            val contentResolver = contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "filtered_image.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val imageUri: Uri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return

            try {
                val outputStream = contentResolver.openOutputStream(imageUri)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
                outputStream?.flush()
                outputStream?.close()
                Toast.makeText(this, "Изображение сохранено в галерею", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Не удалось сохранить изображение", Toast.LENGTH_SHORT).show()
            }
        }

        fun loadingStart(image:ImageView){
            image.visibility = View.INVISIBLE
            loading.visibility = View.VISIBLE
        }


        fun initializeSeekBar(min:Int, max:Int, progress:Int){
            filterBar.min = min
            filterBar.max = max
            filterBar.progress = progress

            barProgress.text = filterBar.progress.toString()
            filterBar.visibility = View.VISIBLE
            barProgress.visibility = View.VISIBLE
        }

        backButton.setOnClickListener {
            finish()
        }

        declineButton.setOnClickListener {
            effects.visibility = View.INVISIBLE
            recyclerView.visibility = View.VISIBLE
            declineButton.visibility = View.INVISIBLE
            acceptButton.visibility = View.INVISIBLE
            imageView.setImageBitmap(bitmap)
            filterBar.visibility = View.INVISIBLE
            barProgress.visibility = View.INVISIBLE

            firstTriangleBtn.visibility = View.INVISIBLE
            firstTriangleInfo.visibility = View.INVISIBLE

            secondTriangleBtn.visibility = View.INVISIBLE
            secondTriangleInfo.visibility = View.INVISIBLE

            startBtn.visibility = View.INVISIBLE
            startInfo.visibility = View.INVISIBLE

            retouchBar.visibility = View.INVISIBLE
            retouchBarProgress.visibility = View.INVISIBLE

            imageView.setOnTouchListener(null)
        }

        acceptButton.setOnClickListener {
            drawable = imageView.drawable as BitmapDrawable
            bitmap = drawable.bitmap
            declineButton.visibility = View.INVISIBLE
            acceptButton.visibility = View.INVISIBLE
            effects.visibility = View.INVISIBLE
            recyclerView.visibility = View.VISIBLE
            filterBar.visibility = View.INVISIBLE
            barProgress.visibility = View.INVISIBLE
            firstTriangleBtn.visibility = View.INVISIBLE
            firstTriangleInfo.visibility = View.INVISIBLE

            secondTriangleBtn.visibility = View.INVISIBLE
            secondTriangleInfo.visibility = View.INVISIBLE

            startBtn.visibility = View.INVISIBLE
            startInfo.visibility = View.INVISIBLE

            retouchBar.visibility = View.INVISIBLE
            retouchBarProgress.visibility = View.INVISIBLE

            imageView.setOnTouchListener(null)

            lifecycleScope.launch {
                photoUri = History.saveImageToCache(this@FilterActivity, bitmap)
                History.addImageToUndoStack(photoUri, undoStack, redoStack)
            }
        }

        saveButton.setOnClickListener {
            saveImageToMediaStore(bitmap)
        }

        undoButton.setOnClickListener{
            val uri = History.undo(undoStack,redoStack)
            if(uri!=null) {
                bitmap = History.loadImageFromUri(this, uri)
                imageView.setImageBitmap(bitmap)
            }
        }

        redoButton.setOnClickListener{
            val uri = History.redo(undoStack,redoStack)
            if(uri!=null) {
                bitmap = History.loadImageFromUri(this, uri)
                imageView.setImageBitmap(bitmap)
            }
        }


        fun  onClick(funBitmap: Bitmap){
            resultBitmap = funBitmap
            imageView.setImageBitmap(resultBitmap)
            loading.visibility = View.INVISIBLE
            imageView.visibility = View.VISIBLE
            filterBar.isEnabled = true
            declineButton.visibility = View.VISIBLE
            acceptButton.visibility = View.VISIBLE
        }

        fun hideElements(){
            declineButton.visibility = View.INVISIBLE
            acceptButton.visibility = View.INVISIBLE

            filterBar.visibility = View.INVISIBLE
            barProgress.visibility = View.INVISIBLE

            firstTriangleBtn.visibility = View.INVISIBLE
            firstTriangleInfo.visibility = View.INVISIBLE

            secondTriangleBtn.visibility = View.INVISIBLE
            secondTriangleInfo.visibility = View.INVISIBLE

            startBtn.visibility = View.INVISIBLE
            startInfo.visibility = View.INVISIBLE

            retouchBar.visibility = View.INVISIBLE
            retouchBarProgress.visibility = View.INVISIBLE

        }

        effectsAdapter.clickListener = object : CarouselAdapter.OnItemClickListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemClick(position: Int, item: Int) {
                lifecycleScope.launch {
                    when (position) {
                        0 -> {
                            loadingStart(imageView)
                            hideElements()
                            resultBitmap =
                                ColorFilters.negative(bitmap)
                            onClick(resultBitmap)
                        }

                        1 -> {

                            loadingStart(imageView)
                            hideElements()
                           resultBitmap =
                                ColorFilters.redFilter(bitmap)
                            onClick(resultBitmap)
                        }

                        2 -> {

                            loadingStart(imageView)
                            hideElements()
                             resultBitmap =
                                ColorFilters.greenFilter(bitmap)
                            onClick(resultBitmap)
                        }

                        3 -> {
                            loadingStart(imageView)
                            hideElements()
                            resultBitmap =
                                ColorFilters.blueFilter(bitmap)
                            onClick(resultBitmap)
                        }

                        4 -> {

                            loadingStart(imageView)
                            hideElements()
                             resultBitmap =
                                ColorFilters.grayscale(bitmap)
                            onClick(resultBitmap)
                        }

                        5 -> {
                            hideElements()
                            initializeSeekBar(1,100,1)
                            filterBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                    barProgress.text = progress.toString()
                                }

                                override fun onStartTrackingTouch(rotationBar: SeekBar) {}

                                override fun onStopTrackingTouch(rotationBar: SeekBar) {

                                    lifecycleScope.launch{
                                        loadingStart(imageView)
                                        filterBar.isEnabled = false
                                         resultBitmap =
                                            ColorFilters.mosaic(bitmap, filterBar.progress)
                                        onClick(resultBitmap)

                                    }
                                }
                            })
                        }

                        6 -> {
                            loadingStart(imageView)
                            hideElements()
                            resultBitmap =
                                ColorFilters.sepia(bitmap)
                            onClick(resultBitmap)
                        }

                        7->{
                            hideElements()
                            initializeSeekBar(1,5,1)
                            filterBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                    barProgress.text = progress.toString()
                                }

                                override fun onStartTrackingTouch(filterBar: SeekBar) {}

                                override fun onStopTrackingTouch(filterBar: SeekBar) {

                                    lifecycleScope.launch{
                                        loadingStart(imageView)
                                        filterBar.isEnabled = false
                                        resultBitmap =
                                            ColorFilters.gaussianBlur(bitmap,filterBar.progress.toDouble())
                                        onClick(resultBitmap)

                                    }
                                }
                            })
                        }
                    }
                }
            }
        }

        adapter.clickListener = object : CarouselAdapter.OnItemClickListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemClick(position: Int, item: Int) {
                lifecycleScope.launch {
                    when (position) {
                        0 -> {

                            initializeSeekBar(-180,180,0)
                            barProgress.text = filterBar.progress.toString()+"°"


                            filterBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                    barProgress.text = progress.toString() + "°"
                                }

                                override fun onStartTrackingTouch(rotationBar: SeekBar) {}

                                override fun onStopTrackingTouch(rotationBar: SeekBar) {
                                    lifecycleScope.launch {
                                        imageView.visibility = View.INVISIBLE
                                        loading.visibility = View.VISIBLE
                                        rotationBar.isEnabled = false
                                        resultBitmap =
                                            ImageRotation.rotateBitmap(bitmap,rotationBar.progress.toDouble())
                                        onClick(resultBitmap)
                                    }

                                }
                            })
                        }

                        1->{
                            hideElements()
                            initializeSeekBar(10,600,100)
                            barProgress.text = (filterBar.progress/100.0).toString()

                            filterBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                    barProgress.text = (filterBar.progress/100.0).toString()
                                }

                                override fun onStartTrackingTouch(rotationBar: SeekBar) {}

                                override fun onStopTrackingTouch(rotationBar: SeekBar) {

                                    lifecycleScope.launch{
                                        loadingStart(imageView)
                                        rotationBar.isEnabled = false
                                        resultBitmap = Resizer.resize(bitmap,filterBar.progress/100.toDouble(),filterBar.progress/100.toDouble())
                                        onClick(resultBitmap)

                                    }
                                }
                            })
                        }

                        2 -> {

                            hideElements()
                            initializeSeekBar(0,20,10)
                            barProgress.text = (filterBar.progress/10.0).toString()

                            filterBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                    barProgress.text = (filterBar.progress/10.0).toString()
                                }

                                override fun onStartTrackingTouch(rotationBar: SeekBar) {}

                                override fun onStopTrackingTouch(rotationBar: SeekBar) {

                                    lifecycleScope.launch{
                                        loadingStart(imageView)
                                        filterBar.isEnabled = false
                                        resultBitmap =
                                            SaturationFilter.saturation(bitmap,(filterBar.progress/10.0).toFloat())
                                        onClick(resultBitmap)

                                    }
                                }
                            })
                        }

                        3 -> {
                            hideElements()
                            initializeSeekBar(0,100,0)
                            barProgress.text = filterBar.progress.toString()

                            filterBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                    barProgress.text = progress.toString()
                                }

                                override fun onStartTrackingTouch(rotationBar: SeekBar) {}

                                override fun onStopTrackingTouch(rotationBar: SeekBar) {

                                    lifecycleScope.launch{
                                        loadingStart(imageView)

                                        filterBar.isEnabled = false
                                        resultBitmap =
                                            ColorFilters.brightness(bitmap, filterBar.progress)
                                        onClick(resultBitmap)

                                    }
                                }
                            })
                        }

                        4 -> {
                            hideElements()
                            recyclerView.visibility = View.INVISIBLE
                            effects.visibility = View.VISIBLE

                            declineButton.visibility = View.VISIBLE
                            acceptButton.visibility = View.VISIBLE

                        }

                        5 -> {
                            hideElements()
                            initializeSeekBar(-100,100,0)
                            barProgress.text = filterBar.progress.toString()

                            filterBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                    barProgress.text = (filterBar.progress).toString()
                                }

                                override fun onStartTrackingTouch(rotationBar: SeekBar) {}

                                override fun onStopTrackingTouch(rotationBar: SeekBar) {

                                    lifecycleScope.launch{
                                        loadingStart(imageView)

                                        rotationBar.isEnabled = false
                                        resultBitmap =
                                            ColorFilters.contrast(bitmap, filterBar.progress)
                                        onClick(resultBitmap)

                                    }
                                }
                            })
                        }

                        6 -> {
                            hideElements()
                            initializeSeekBar(5,100,50)
                            barProgress.text = filterBar.progress.toString()

                            retouchBar.min = 50
                            retouchBar.max = 100
                            retouchBar.progress = 50
                            retouchBarProgress.text = (retouchBar.progress/100.0).toString()

                            retouchBar.visibility = View.VISIBLE
                            retouchBarProgress.visibility = View.VISIBLE

                            imageView.setRetouchable(filterBar.progress,1.0,imageView,acceptButton,declineButton)

                            filterBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                    barProgress.text = (filterBar.progress).toString()
                                }

                                override fun onStartTrackingTouch(rotationBar: SeekBar) {}

                                override fun onStopTrackingTouch(rotationBar: SeekBar) {

                                    lifecycleScope.launch{

                                        imageView.setRetouchable(filterBar.progress,retouchBar.progress/100.0,imageView,acceptButton,declineButton)
                                    }
                                }
                            })

                            retouchBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                    retouchBarProgress.text = (retouchBar.progress/100.0).toString()
                                }

                                override fun onStartTrackingTouch(retouchBar: SeekBar) {}

                                override fun onStopTrackingTouch(retouchBar: SeekBar) {

                                    lifecycleScope.launch{

                                        imageView.setRetouchable(filterBar.progress,retouchBar.progress/100.0,imageView,acceptButton,declineButton)
                                    }
                                }
                            })
                        }

                        7->{
                            hideElements()
                            initializeSeekBar(1,5,1)
                            barProgress.text = filterBar.min.toString()

                            filterBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                    barProgress.text = progress.toString()
                                }

                                override fun onStartTrackingTouch(filterBar: SeekBar) {}

                                override fun onStopTrackingTouch(filterBar: SeekBar) {

                                    lifecycleScope.launch{
                                        loadingStart(imageView)
                                        resultBitmap =
                                            ColorFilters.unsharpMasking(bitmap,filterBar.progress)
                                        onClick(resultBitmap)

                                    }
                                }
                            })
                        }

                        8->{
                            hideElements()
                            firstTriangleBtn.visibility = View.VISIBLE
                            firstTriangleInfo.visibility = View.VISIBLE
                            firstTriangleInfo.setSelected(true)

                            secondTriangleBtn.visibility = View.VISIBLE
                            secondTriangleInfo.visibility = View.VISIBLE
                            secondTriangleInfo.setSelected(true)

                            startBtn.visibility = View.VISIBLE
                            startInfo.visibility = View.VISIBLE


                            firstTriangleBtn.setOnClickListener{
                                Affine.createFirstTriangle()
                                firstTriangleBtn.startAnimation(animation)
                                imageView.setTouchable(imageView,bitmap)
                            }

                            secondTriangleBtn.setOnClickListener{
                                Affine.createSecondTriangle()
                                secondTriangleBtn.startAnimation(animation)
                            }

                            startBtn.setOnClickListener{
                                lifecycleScope.launch {
                                    startBtn.startAnimation(animation)
                                    resultBitmap = Affine.callAffine(bitmap)
                                    onClick(resultBitmap)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


