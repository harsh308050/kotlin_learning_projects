package com.harsh.noteitform.data.model

data class FormResModel(
    val feedbackTypeId: String,
    val feedbackTypeWiseId: String,
    val questionComment: List<Map<String, String>>,
    val questionForm: List<Map<String, Any>>,
    val updatedDate: String,
    val userId: String,
)