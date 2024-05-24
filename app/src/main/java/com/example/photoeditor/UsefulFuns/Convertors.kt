package com.example.photoeditor.UsefulFuns

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.io.InputStream

fun getBitmapFromAsset(context: Context, filePath: String): Bitmap {
    val assetManager = context.assets
    var inputStream: InputStream? = null
    lateinit var bitmap: Bitmap
    try {
        inputStream = assetManager.open(filePath)
        bitmap = BitmapFactory.decodeStream(inputStream)
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        try {
            inputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    return bitmap
}