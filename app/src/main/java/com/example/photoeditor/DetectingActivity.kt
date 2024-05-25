package com.example.photoeditor

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.NativeCameraView.TAG
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfRect
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream

class DetectingActivity : AppCompatActivity(), CoroutineScope {
    private val job = Job()
    override val coroutineContext = Dispatchers.Main + job
    private val pickImage = 1
    private lateinit var imageView: ImageView
    private lateinit var pickImageButton: ImageButton
    private lateinit var takeAPhotoButton: ImageButton
    private lateinit var frameButton: ImageButton
    private lateinit var mosaicButton: ImageButton
    private lateinit var saveImageButton : ImageButton
    private lateinit var closeButton : ImageButton
    private lateinit var faceRectangles : MatOfRect
    private lateinit var matrix : Mat
    private lateinit var processedBitmap : Bitmap
    private var imageUri : Uri? = null
    private var imageDetected = false
    private var isMosaicMode = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detecting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!OpenCVLoader.initLocal()) {
            Log.e(TAG, "OpenCV initialization failed.")
        } else {
            Log.d(TAG, "OpenCV initialized successfully.")
        }

        imageView = findViewById(R.id.imageView)
        pickImageButton = findViewById(R.id.pickImageButton)
        takeAPhotoButton = findViewById(R.id.takeAPhotoButton)
        frameButton = findViewById(R.id.frameButton)
        mosaicButton = findViewById(R.id.mosaicButton)
        saveImageButton = findViewById(R.id.saveButton)
        closeButton = findViewById(R.id.closeButton)
        saveImageButton.isEnabled = false

        pickImageButton.setOnClickListener {
            val permissions = arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            if (ContextCompat.checkSelfPermission(this, permissions[0]) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, permissions, 0)
            }
            else
            {
                onGallery()
            }
        }

        frameButton.setOnClickListener {
            if (imageUri != null) {
                isMosaicMode = false
                applyDetecting(imageUri!!, true)
            } else {
                Toast.makeText(this, "Сначала загрузите изображение", Toast.LENGTH_SHORT).show()
            }
        }

        mosaicButton.setOnClickListener {
            if (imageUri != null) {
                isMosaicMode = true
                applyDetecting(imageUri!!, false)
                Toast.makeText(this, "Включен режим мозаики\nНажмите на лица", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Сначала загрузите изображение", Toast.LENGTH_SHORT).show()
            }
        }

        closeButton.setOnClickListener {
            if (imageUri != null)
            {
                imageView.setImageDrawable(null)
                imageUri = null
                saveImageButton.isEnabled = false
                closeButton.visibility = View.INVISIBLE
            }
        }
    }

    private fun onGallery()
    {
        val pickPhoto = Intent(Intent.ACTION_PICK)
        pickPhoto.type = "image/*"
        startActivityForResult(pickPhoto, pickImage)
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            val granted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (granted)
            {
                onGallery()
            }
            else
            {
                Toast.makeText(this, "Необходим доступ к фото и видео", Toast.LENGTH_SHORT).show()
                openAppSettings()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
            closeButton.visibility = View.VISIBLE
        }
    }

    private fun saveImageToMediaStore(bitmap: Bitmap) {
        val contentResolver = contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "detected_image.jpg")
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
            Toast.makeText(this, "Изображение сохранено", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Не удалось сохранить изображение", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertToImageCoordinates(event: MotionEvent, imageView: ImageView): Point {
        val imageMatrix = imageView.imageMatrix
        val values = FloatArray(9)
        imageMatrix.getValues(values)

        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]
        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]

        val x = (event.x - transX) / scaleX
        val y = (event.y - transY) / scaleY

        return Point(x.toDouble(), y.toDouble())
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun applyDetecting(imageUri: Uri, drawFrames: Boolean) {
        launch {
            val bitmap = withContext(Dispatchers.IO) {
                BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
            }

            val filteredBitmap = withContext(Dispatchers.Default) {
                detectingFaces(bitmap, drawFrames)
            }

            withContext(Dispatchers.Main) {
                imageView.setImageBitmap(filteredBitmap)
                processedBitmap = filteredBitmap
                saveImageButton.isEnabled = true
                saveImageButton.setOnClickListener {
                    saveImageToMediaStore(processedBitmap)
                }

                if (isMosaicMode && imageDetected) {
                    imageView.setOnTouchListener { v, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            val point = convertToImageCoordinates(event, imageView)
                            for (rect in faceRectangles.toArray()) {
                                if (rect.contains(point)) {
                                    applyMosaicToFace(rect)
                                    break
                                }
                            }
                        }
                        true
                    }
                } else {
                    imageView.setOnTouchListener(null)
                }
            }
        }
    }

    private fun applyMosaicToFace(rect: Rect) {
        launch {
            val source = (imageView.drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val subMatrix = matrix.submat(rect)
            val subBitmap = Bitmap.createBitmap(source, rect.x, rect.y, rect.width, rect.height)
            val blockSize: Int = calculateBlockSize(source.width * source.height)
            val mosaicBitmap = mosaic(subBitmap, blockSize)
            Utils.bitmapToMat(mosaicBitmap, subMatrix)
            val updatedBitmap = Bitmap.createBitmap(source)
            Utils.matToBitmap(matrix, updatedBitmap)
            withContext(Dispatchers.Main) {
                imageView.setImageBitmap(updatedBitmap)
                processedBitmap = updatedBitmap
            }
        }
    }

    private suspend fun detectingFaces(source: Bitmap, drawFrames: Boolean): Bitmap {
        try {
            // Битмап в матрицу изображения
            matrix = Mat()
            Utils.bitmapToMat(source, matrix)

            // Загрузка каскада Хаара
            withContext(Dispatchers.IO) {
                val inputStream = resources.openRawResource(R.raw.haarcascade_frontalface_alt)
                val cascadeFile = File.createTempFile("cascade", ".xml")
                val outputStream = FileOutputStream(cascadeFile)

                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                inputStream.close()
                outputStream.close()

                val faceCascadeClassifier = CascadeClassifier(cascadeFile.absolutePath)

                // Обнаружение лиц
                faceRectangles = MatOfRect()
                faceCascadeClassifier.detectMultiScale(matrix, faceRectangles)

                if (drawFrames) {
                    val thickness = calculateThickness(source.width * source.height)
                    for (rect in faceRectangles.toArray()) {
                        Imgproc.rectangle(
                            matrix,
                            rect.tl(),
                            rect.br(),
                            Scalar(255.0, 144.0, 27.0),
                            thickness
                        )
                    }
                }

                if (faceRectangles.toArray().isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DetectingActivity, "Лица не обнаружены", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                Utils.matToBitmap(matrix, source)

                imageDetected = true
            }

            return source
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@DetectingActivity, "Ошибка обработки изображения", Toast.LENGTH_SHORT).show()
            }
            return source
        }
    }

    private suspend fun mosaic(source: Bitmap, blockSize: Int): Bitmap {
        val width = source.width
        val height = source.height
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        coroutineScope {
            val pixels = IntArray(width * height)
            source.getPixels(pixels, 0, width, 0, 0, width, height)

            launch(Dispatchers.Default) {
                for (j in 0 until height step blockSize) {
                    for (i in 0 until width step blockSize) {
                        var count = 0
                        val colors = mutableListOf(0, 0, 0)

                        for (k in 0 until blockSize) {
                            for (n in 0 until blockSize) {
                                val x = i + k
                                val y = j + n
                                if (x < width && y < height) {
                                    val pixel = pixels[y * width + x]
                                    colors[0] += Color.red(pixel)
                                    colors[1] += Color.green(pixel)
                                    colors[2] += Color.blue(pixel)
                                    count++
                                }
                            }
                        }

                        val meanColor = Color.rgb(
                            colors[0] / count,
                            colors[1] / count,
                            colors[2] / count
                        )

                        for (k in 0 until blockSize) {
                            for (n in 0 until blockSize) {
                                val x = i + k
                                val y = j + n
                                if (x < width && y < height) {
                                    pixels[y * width + x] = meanColor
                                }
                            }
                        }
                    }
                }

                resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            }
        }

        return resultBitmap
    }

    private fun calculateThickness(pixels: Int): Int {
        return when {
            pixels < 450_000 -> 4
            pixels < 1_200_000 -> 10
            pixels < 11_000_000 -> 16
            else -> 22
        }
    }

    private fun calculateBlockSize(pixels: Int): Int {
        return when {
            pixels < 450_000 -> 10
            pixels < 1_200_000 -> 25
            pixels < 11_000_000 -> 40
            else -> 100
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}