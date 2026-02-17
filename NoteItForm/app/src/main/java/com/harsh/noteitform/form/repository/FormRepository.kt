package com.harsh.noteitform.form.repository

import android.content.Context
import com.google.gson.Gson
import com.harsh.noteitform.data.model.Category
import com.harsh.noteitform.data.model.FormModel
import com.harsh.noteitform.data.model.QuestionForm
import com.harsh.noteitform.data.model.Type

object FormRepository {
    private var formData: FormModel? = null
    fun init(context: Context) {
        if (formData == null) {
            val jsonString = context.assets.open("json/data.json").bufferedReader().use { it.readText() }
            formData = Gson().fromJson(jsonString, FormModel::class.java)
        }
    }
    fun getTypes(): List<Type> {
        return formData?.data?.type ?: emptyList()
    }

    fun getTypesSorted(): List<Type> {
        return getTypes().sortedBy { it.displayOrder.toIntOrNull() ?: Int.MAX_VALUE }
    }
    fun getTypeById(typeId: String): Type? {
        return getTypes().find { it.id == typeId }
    }
    fun getCategoriesForType(typeId: String): List<Category> {
        return getTypeById(typeId)?.category ?: emptyList()
    }
}