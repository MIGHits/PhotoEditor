package com.example.photoeditor.UsefulFuns

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.photoeditor.Cube3d.Tria3d
import com.example.photoeditor.Cube3d.vec3d
import com.example.photoeditor.Tria2d
import com.example.photoeditor.vec2d
import java.io.IOException
import java.io.InputStream

fun convertVec3dTo2d(vec:vec3d):vec2d{
    return vec2d(vec.x.toInt(),vec.y.toInt())
}
fun convertTria3dTo2d(tria: Tria3d):Tria2d{
    return Tria2d(arrayOf(convertVec3dTo2d(tria.p[0]),convertVec3dTo2d(tria.p[1]),convertVec3dTo2d(tria.p[2])))
}
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