package com.dsk.trackmyvisitor.model.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class UtilityService {

    private lateinit var currentPhotoPath: String
    private val imageDirectoryName = "VisitorManager"
    private val dateFormat = "yyyyMMdd_HHmmss"

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat(dateFormat).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "PNG_${timeStamp}_", /* prefix */
            ".png", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    fun createImageFile4(): File? {
        // External sdcard location
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            imageDirectoryName
        )
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("DSK ", "Unable to create directory.")
                return null
            }
        }
        val timeStamp = SimpleDateFormat(
            dateFormat,
            Locale.getDefault()
        ).format(Date())

        return File(mediaStorageDir.path + File.separator + "PNG_" + timeStamp + ".png")
    }

    @Throws(IOException::class)
    fun createFile(context: Context, filename: String, fileExtension: String): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            filename, fileExtension, storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    fun createFile4(filename: String, fileExtension: String): File? {
        val mediaStorageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            imageDirectoryName
        )
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("DSK ", "Unable to create directory.")
                return null
            }
        }
        return File(mediaStorageDir.path + File.separator + filename + fileExtension)
    }

    fun getBytesPNG(bitmap: Bitmap?): ByteArray? {
        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            return stream.toByteArray()
        }
        return null
    }

    fun createBitmap(v: View, isWantBG: Boolean): Bitmap? {

        //v.setVisibility(View.INVISIBLE);
        v.isDrawingCacheEnabled = true
        v.buildDrawingCache(true)
        if (isWantBG) {
            v.setBackgroundColor(Color.rgb(255, 255, 255))
        }
        var bitmap = Bitmap.createBitmap(v.drawingCache)
        v.isDrawingCacheEnabled = false
        var newBitmap = Bitmap.createBitmap(
            bitmap!!.width,
            bitmap.height, bitmap.config
        )
        val resizedBitmap = Bitmap.createScaledBitmap(
            bitmap,
            400,
            157,
            false
        )
        bitmap.recycle()
        newBitmap!!.recycle()
        return resizedBitmap
    }

    fun getPathFromURI(context: Context,uri: Uri, selection: String?): String {
        var path: String? = null
        val cursor = context.contentResolver.query(uri, null, selection, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }
}