package com.silenthink.memoapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

object ImageUtils {
    
    private const val IMAGES_DIR = "memo_images"
    
    /**
     * 将图片URI保存到应用内部存储
     */
    fun saveImageToInternalStorage(context: Context, imageUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap != null) {
                val imagesDir = File(context.filesDir, IMAGES_DIR)
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }
                
                val filename = "${UUID.randomUUID()}.jpg"
                val imageFile = File(imagesDir, filename)
                
                val outputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                outputStream.close()
                bitmap.recycle()
                
                imageFile.absolutePath
            } else null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 删除图片文件
     */
    fun deleteImage(imagePath: String?): Boolean {
        if (imagePath.isNullOrEmpty()) return false
        
        return try {
            val file = File(imagePath)
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 检查图片文件是否存在
     */
    fun imageExists(imagePath: String?): Boolean {
        if (imagePath.isNullOrEmpty()) return false
        return File(imagePath).exists()
    }
} 