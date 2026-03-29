package com.kanyandula.nyasa.ui.navigation

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import com.github.drjacky.imagepicker.ImagePicker

fun createImagePickerIntent(activity: Activity): Intent =
    ImagePicker.with(activity)
        .crop(1130F, 961F)
        .galleryOnly()
        .setOutputFormat(Bitmap.CompressFormat.JPEG)
        .galleryMimeTypes(arrayOf("image/png", "image/jpg", "image/jpeg"))
        .createIntent()
