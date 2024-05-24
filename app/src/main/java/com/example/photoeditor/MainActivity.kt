package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.photoeditor.Cube3d.CubeActivity
import com.example.photoeditor.FilterActivity
import com.example.photoeditor.R
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity()
{
    private val REQUEST_CODE_IMAGE_PICK = 100
    private val REQUEST_IMAGE_CAPTURE = 1
    private val PERMISSION_REQUEST_CODE = 2
    var vFilename: String = ""
    val androidVersion = Build.VERSION.RELEASE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setStatusBarColor(Color.parseColor("#5e6666"));
        window.setNavigationBarColor(Color.parseColor("#5e6666"));
        val photoPickButton = findViewById<ImageButton>(R.id.galleryButton)
        val cameraButton =  findViewById<ImageButton>(R.id.cameraButton)
        val cubeButton = findViewById<ImageButton>(R.id.cubeButton)

        cameraButton.setOnClickListener{

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CODE)
            } else {
                openCamera()
            }
        }

        photoPickButton.setOnClickListener {
            var permissions:Array<String>
            if(androidVersion.toInt()>12){
                permissions = arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                if(ContextCompat.checkSelfPermission(this,permissions[0])!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, permissions, 0)
                }
                else{
                    onGallery()
                }
            }
            else {
                permissions = arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                if (ContextCompat.checkSelfPermission(
                        this,
                        permissions[0]
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        permissions[1]
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(this, permissions, 0)
                } else {
                    onGallery()
                }
            }
        }

        cubeButton.setOnClickListener{
            var intent:Intent
            intent = Intent(this, CubeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onGallery()
    {
        val pickPhoto = Intent(Intent.ACTION_PICK)
        pickPhoto.type = "image/*"
        startActivityForResult(pickPhoto, REQUEST_CODE_IMAGE_PICK)
    }


    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")

        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // set filename
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        vFilename = "FOTO_" + timeStamp + ".jpg"

        // set direcory folder
        val directory = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, vFilename)
        val image_uri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);

        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
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

        val imageUri:Uri?
        var intent:Intent

        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
             imageUri = data?.data
            if (imageUri != null) {
                 intent = Intent(this, FilterActivity::class.java)
                intent.putExtra("imageUri", imageUri.toString())
                startActivity(intent)
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), vFilename)
            val PhotoUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file)
            if (PhotoUri!= null) {
                intent = Intent(this, FilterActivity::class.java)
                intent.putExtra("imageUri", PhotoUri.toString())
                startActivity(intent)
            }
        }
    }

}



