package com.kanyandula.nyasa.ui.navigation

import android.content.Context
import android.widget.Toast
import com.kanyandula.nyasa.ui.UiEvent

fun handleStandardEvent(context: Context, event: UiEvent): Boolean {
    return when (event) {
        is UiEvent.ShowToast -> {
            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            true
        }
        is UiEvent.ShowErrorDialog -> {
            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            true
        }
        is UiEvent.ShowSuccessDialog -> {
            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            true
        }
        else -> false
    }
}
