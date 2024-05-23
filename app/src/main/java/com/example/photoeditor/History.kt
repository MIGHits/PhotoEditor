package com.example.photoeditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Stack

class History {
    companion object{

       suspend fun saveImageToCache(context: Context, bitmap: Bitmap): Uri = withContext(Dispatchers.Default) {
                val cachePath = File(context.cacheDir, "images")
                cachePath.mkdirs()
                val file = File(cachePath, "image_${System.currentTimeMillis()}.png")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
                Uri.fromFile(file)
        }

       fun addImageToUndoStack(uri: Uri?,undoStack:Stack<Uri>,redoStack:Stack<Uri>) {
            undoStack.push(uri)
            redoStack.clear()
        }

        fun undo(undoStack:Stack<Uri>,redoStack:Stack<Uri>): Uri? {
            return if (undoStack.isNotEmpty()) {
                val uri = undoStack.pop()
                redoStack.push(uri)
                uri
            } else {
                null
            }
        }

        fun redo(undoStack:Stack<Uri>,redoStack:Stack<Uri>): Uri? {
            return if (redoStack.isNotEmpty()) {
                val uri = redoStack.pop()
                undoStack.push(uri)
                uri
            } else {
                null
            }
        }

        fun loadImageFromUri(context: Context, uri: Uri?): Bitmap? {
            return try {
                val inputStream = uri?.let { context.contentResolver.openInputStream(it) }
                BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }
}