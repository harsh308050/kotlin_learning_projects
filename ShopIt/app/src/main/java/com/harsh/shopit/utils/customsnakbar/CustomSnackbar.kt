package com.harsh.shopit.utils.customsnakbar
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.harsh.shopit.R

object CustomSnackbar {
    fun show(
        view: View,
        message: String,
        type: snackbarTypes,
        duration: Int = Snackbar.LENGTH_SHORT
    ) {
        val snackbar = Snackbar.make(view, message, duration)

        val bgColor = when (type) {
            snackbarTypes.SUCCESS -> ContextCompat.getColor(view.context,R.color.green)
            snackbarTypes.ERROR -> ContextCompat.getColor(view.context,R.color.red)
            snackbarTypes.INFO -> ContextCompat.getColor(view.context,R.color.primary)
        }

        snackbar.setBackgroundTint(bgColor)
        snackbar.setTextColor(Color.WHITE)
        snackbar.show()
    }
    fun success(view: View, message: String) =
        show(view, message, snackbarTypes.SUCCESS)

    fun error(view: View, message: String) =
        show(view, message, snackbarTypes.ERROR)

    fun info(view: View, message: String) =
        show(view, message, snackbarTypes.INFO)
}
