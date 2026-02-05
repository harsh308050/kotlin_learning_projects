package com.harsh.shopit.extensions

import android.widget.EditText

fun EditText.isEmpty(): Boolean {
    return this.text.toString().trim().isEmpty()
}

fun EditText.isNotEmpty(): Boolean {
    return this.text.toString().trim().isNotEmpty()
}
fun EditText.isNullOrEmpty(): Boolean {
    return this.text.isNullOrEmpty()
}
fun EditText.isNotNullOrEmpty(): Boolean {
    return !this.text.isNullOrEmpty()
}