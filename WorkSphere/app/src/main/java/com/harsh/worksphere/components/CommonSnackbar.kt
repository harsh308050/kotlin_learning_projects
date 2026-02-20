package com.harsh.worksphere.components

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import androidx.core.graphics.toColorInt

object CommonSnackbar {

    enum class SnackbarType {
        ERROR,      // Red background
        SUCCESS,    // Green background
        INFO        // Blue background
    }

    // For Activity usage
    fun show(
        activity: Activity,
        message: String,
        type: SnackbarType,
        duration: Int = Snackbar.LENGTH_LONG,
        actionText: String? = null,
        actionListener: View.OnClickListener? = null,
        cornerRadius: Float = 16f  // Default corner radius in dp
    ) {
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        show(rootView, message, type, duration, actionText, actionListener, cornerRadius)
    }

    // For Fragment usage
    fun show(
        fragment: Fragment,
        message: String,
        type: SnackbarType,
        duration: Int = Snackbar.LENGTH_LONG,
        actionText: String? = null,
        actionListener: View.OnClickListener? = null,
        cornerRadius: Float = 16f
    ) {
        val view = fragment.view ?: return
        show(view, message, type, duration, actionText, actionListener, cornerRadius)
    }

    // Core implementation with any View
    fun show(
        view: View,
        message: String,
        type: SnackbarType,
        duration: Int = Snackbar.LENGTH_LONG,
        actionText: String? = null,
        actionListener: View.OnClickListener? = null,
        cornerRadiusDp: Float = 16f
    ) {
        val snackbar = Snackbar.make(view, message, duration)

        // Set background color based on type
        val backgroundColor = when (type) {
            SnackbarType.ERROR -> "#D32F2F".toColorInt()
            SnackbarType.SUCCESS -> "#388E3C".toColorInt()
            SnackbarType.INFO -> "#1976D2".toColorInt()
        }

        // Convert dp to pixels
        val cornerRadiusPx = (cornerRadiusDp * view.resources.displayMetrics.density).toInt()

        // Create rounded drawable
        val roundedDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(backgroundColor)
            cornerRadius = cornerRadiusPx.toFloat()
        }

        val snackbarView = snackbar.view

        // Apply rounded background
        snackbarView.background = roundedDrawable
        snackbarView.backgroundTintList = null  // Remove any tint that might override

        // Optional: Add margin around snackbar for floating effect
        val params = snackbarView.layoutParams as ViewGroup.MarginLayoutParams
        val marginPx = (8 * view.resources.displayMetrics.density).toInt()
        params.setMargins(marginPx, marginPx, marginPx, marginPx)
        snackbarView.layoutParams = params

        // Optional action button
        if (!actionText.isNullOrEmpty() && actionListener != null) {
            snackbar.setAction(actionText, actionListener)
            snackbar.setActionTextColor(Color.WHITE)
        }

        snackbar.show()
    }

    // Extension functions for cleaner syntax
    fun Activity.showError(message: String, duration: Int = Snackbar.LENGTH_LONG, cornerRadius: Float = 16f) {
        show(this, message, SnackbarType.ERROR, duration, cornerRadius = cornerRadius)
    }
    fun Activity.showSuccessAndFinish(
        message: String,
        duration: Int = Snackbar.LENGTH_SHORT,
        delayMillis: Long = 1500,
        cornerRadius: Float = 16f
    ) {
        showSuccess(message, duration, cornerRadius)

        // Delay the finish to allow Snackbar to be seen
        window.decorView.postDelayed({
            if (!isFinishing && !isDestroyed) {
                finish()
            }
        }, delayMillis)
    }

    fun Activity.showSuccess(message: String, duration: Int = Snackbar.LENGTH_LONG, cornerRadius: Float = 16f) {
        show(this, message, SnackbarType.SUCCESS, duration, cornerRadius = cornerRadius)
    }

    fun Activity.showInfo(message: String, duration: Int = Snackbar.LENGTH_LONG, cornerRadius: Float = 16f) {
        show(this, message, SnackbarType.INFO, duration, cornerRadius = cornerRadius)
    }

    fun Fragment.showError(message: String, duration: Int = Snackbar.LENGTH_LONG, cornerRadius: Float = 16f) {
        show(this, message, SnackbarType.ERROR, duration, cornerRadius = cornerRadius)
    }

    fun Fragment.showSuccess(message: String, duration: Int = Snackbar.LENGTH_LONG, cornerRadius: Float = 16f) {
        show(this, message, SnackbarType.SUCCESS, duration, cornerRadius = cornerRadius)
    }

    fun Fragment.showInfo(message: String, duration: Int = Snackbar.LENGTH_LONG, cornerRadius: Float = 16f) {
        show(this, message, SnackbarType.INFO, duration, cornerRadius = cornerRadius)
    }
}