package com.harsh.noteitform.data.model

data class FormModel(
    val `data`: Data,
    val errorcode: String,
    val msg: String,
    val status: String,
    val versionData: VersionData
)

data class Data(
    val influence: List<Influence>,
    val isConfidential: String,
    val type: List<Type>
)

data class Influence(
    val bgColor: String,
    val id: String,
    val influenceAlias: String
)

data class Type(
    val bgColor: String,
    val category: List<Category>,
    val displayOrder: String,
    val feedbackTypeId: String,
    val id: String,
    val influenceCompanyId: String,
    val typeAlias: String,
    val typeId: String
)

data class Category(
    val bgColor: String,
    val categoryName: String,
    val displayFor: String,
    val displayOrder: String,
    val id: String,
    val isAttachment: String,
    val isFollowup: String,
    val isValidityPeriodOn: String,
    val positionIds: String,
    val questionForm: List<QuestionForm>,
    val teamtagIds: String,
    val typeCompanyId: String,
    val validityIn: String,
    val validityPeriod: String
)

data class QuestionForm(
    val categoryId: String,
    val displayFor: String,
    val displayOrder: String,
    val fieldType: String,
    val fieldValue: String,
    val id: String,
    val isEditable: String,
    val isRequired: String,
    val max: String,
    val min: String,
    val offerAdditionalCommentOption: String,
    val parentId: String,
    val placeholder: String,
    val questionAdditionalInfo: String,
    val questionLabel: String,
    val questionSub: List<QuestionSub>
)


data class QuestionSub(
    val categoryId: String,
    val displayFor: String,
    val displayOrder: String,
    val fieldType: String,
    val fieldValue: String,
    val id: String,
    val isEditable: String,
    val isRequired: String,
    val max: String,
    val min: String,
    val offerAdditionalCommentOption: String,
    val parentId: String,
    val placeholder: String,
    val questionAdditionalInfo: String,
    val questionLabel: String
)

data class VersionData(
    val appMsg: String,
    val appUpdate: Int,
    val appVersion: String,
    val companyMfaStatus: String,
    val nonAuthorizedModules: List<Any?>,
    val positionId: String,
    val resetTeamMemberData: String,
    val userMfaStatus: String,
    val zendeskSupportOption: String
)