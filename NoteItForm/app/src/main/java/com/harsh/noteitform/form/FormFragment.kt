package com.harsh.noteitform.form

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.gson.GsonBuilder
import com.harsh.noteitform.R
import com.harsh.noteitform.components.*
import com.harsh.noteitform.data.model.*
import com.harsh.noteitform.form.repository.FormRepository

class FormFragment : Fragment(R.layout.fragment_form) {

    private var typeId: String? = null
    private var feedbackTypeId: String? = null
    private var selectedCategoryId: String? = null
    private var typeData: Type? = null
    private var categories: List<Category> = emptyList()

    private val fieldViewsMap = mutableMapOf<String, View>()
    private val nestedFieldsMap = mutableMapOf<String, CommonNestedField>()
    private val commentsMap = mutableMapOf<String, String>()


    private lateinit var dropdownCard: MaterialCardView
    private lateinit var selectedValue: TextView
    private lateinit var hiddenSpinner: Spinner
    private lateinit var formContainer: LinearLayout
    private lateinit var conversationHeader: CommonHeader
    private lateinit var submitBtn: MaterialButton

    private var currentSignatureView: CommonSignature? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        currentSignatureView?.handleGalleryResult(result.resultCode, result.data)
        currentSignatureView = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        typeId = arguments?.getString("type_id")
        feedbackTypeId = arguments?.getString("feedback_type_id")
        typeData = typeId?.let { FormRepository.getTypeById(it) }
        categories = typeId?.let { FormRepository.getCategoriesForType(it) } ?: emptyList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        conversationHeader = view.findViewById(R.id.conversationHeader)
        dropdownCard = view.findViewById(R.id.categoryDropdown)
        selectedValue = view.findViewById(R.id.selectedCategoryValue)
        hiddenSpinner = view.findViewById(R.id.hiddenSpinner)
        formContainer = view.findViewById(R.id.formContainer)
        submitBtn = view.findViewById(R.id.formSubmitButton)
        setupCategoryDropdown()
        setupTypeHeader()
        setupSubmitButton()
    }

    private fun setupSubmitButton() {
        submitBtn.setOnClickListener {
            if (validateForm()) {
                val formData = collectFormData()
                submitForm(formData)
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        fieldViewsMap.forEach { (questionId, view) ->
            val required = when (view) {
                is CommonTextAreaField -> view.isRequired()
                is CommonSingleChoice -> view.isRequired()
                is CommonMultiChoice -> view.isRequired()
                is CommonDateTimePicker -> view.isRequired()
                is CommonSignature -> view.isRequired()
                else -> false
            }

            if (required) {
                val value = getFieldValue(view)
                val isValueEmpty = when (value) {
                    is String -> value.isEmpty()
                    is List<*> -> value.isEmpty()
                    else -> true
                }

                if (isValueEmpty) {
                    isValid = false

                    Toast.makeText(
                        context,
                        "Please fill required field: $questionId",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        nestedFieldsMap.forEach { (parentId, nestedField) ->
            val nestedValues = nestedField.getNestedValues()
            nestedField.getNestedViews().forEach { view ->
                val questionId = view.tag?.toString() ?: return@forEach

                val required = when (view) {
                    is CommonTextAreaField -> view.isRequired()
                    is CommonSingleChoice -> view.isRequired()
                    is CommonMultiChoice -> view.isRequired()
                    is CommonDateTimePicker -> view.isRequired()
                    is CommonSignature -> view.isRequired()
                    else -> false
                }

                if (required) {
                    val value = nestedValues[questionId]
                    val isValueEmpty = when (value) {
                        is String -> value.isEmpty()
                        is List<*> -> value.isEmpty()
                        else -> true
                    }

                    if (isValueEmpty) {
                        isValid = false
                        Toast.makeText(
                            context,
                            "Please fill required nested field: $questionId",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        return isValid
    }

    private fun collectFormData(): FormResModel {
        val answers = mutableListOf<Pair<String, Any>>()
        val comments = mutableMapOf<String, String>()


        fieldViewsMap.forEach { (questionId, view) ->
            val value = getFieldValue(view)
            val isEmpty = when (value) {
                is String -> value.isEmpty()
                is List<*> -> value.isEmpty()
                else -> true
            }

            if (!isEmpty) {
                answers.add(questionId to value)
            }

            val comment = getFieldComment(view)
            if (comment.isNotEmpty()) {
                comments[questionId] = comment
            }
        }


        nestedFieldsMap.forEach { (parentId, nestedField) ->
            val nestedValues = nestedField.getNestedValues()
            val nestedComments = nestedField.getNestedComments()

            nestedValues.forEach { (subQuestionId, value) ->
                when (value) {
                    is String -> if (value.isNotEmpty()) answers.add(subQuestionId to value)
                    is List<*> -> if (value.isNotEmpty()) answers.add(subQuestionId to value)
                    else -> {}
                }
            }

            nestedComments.forEach { (subQuestionId, comment) ->
                if (comment.isNotEmpty()) {
                    comments[subQuestionId] = comment
                }
            }
        }


        val questionFormList = answers.map { mapOf(it.first to it.second) }
        val commentList = if (comments.isNotEmpty()) listOf(comments) else emptyList()

        return FormResModel(
            feedbackTypeId = feedbackTypeId ?: "",
            feedbackTypeWiseId = selectedCategoryId ?: "",
            userId = "5858",
            questionForm = questionFormList,
            questionComment = commentList,
            updatedDate = java.util.Locale.getDefault().toString(),
        )
    }

    private fun getFieldValue(view: View): Any {
        return when (view) {
            is CommonTextAreaField -> view.getValue()
            is CommonSingleChoice -> view.getValue()
            is CommonMultiChoice -> view.getValue()
            is CommonDateTimePicker -> view.getValue()
            is CommonSignature -> {
                val bitmap = view.getSignatureBitmap()
                bitmap?.let { saveBitmapAndGetFilename(it) } ?: ""
            }

            else -> ""
        }
    }

    private fun getFieldComment(view: View): String {
        return when (view) {
            is CommonSingleChoice -> view.getComment()
            is CommonMultiChoice -> view.getComment()
            else -> ""
        }
    }

    private fun saveBitmapAndGetFilename(bitmap: Bitmap): String {
        val filename = "${System.currentTimeMillis()}.png"
        return filename
    }

    private fun submitForm(formData: FormResModel) {

        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(formData)
        Log.d("FormSubmission", "")
        Log.d("FormSubmission", "----- JSON FORMAT -----")
        Log.d("FormSubmission", jsonString)


        Toast.makeText(context, "Form data Submitted.", Toast.LENGTH_LONG).show()
    }

    private fun setupCategoryDropdown() {
        val sortedCategories =
            categories.sortedBy { it.displayOrder.toIntOrNull() ?: Int.MAX_VALUE }

        val categoryNames = mutableListOf("Select Category")
        categoryNames.addAll(sortedCategories.map { it.categoryName })

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.item_spinner_dropdown,
            categoryNames
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: android.view.ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView

                if (position == 0) {
                    textView.setTextColor(resources.getColor(R.color.dark_grey))
                } else {
                    textView.setTextColor(resources.getColor(R.color.black))
                }

                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        hiddenSpinner.adapter = adapter

        hiddenSpinner.setSelection(0)
        selectedValue.text = "Select Category"

        dropdownCard.setOnClickListener {
            hiddenSpinner.performClick()
        }

        hiddenSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position == 0) {
                        selectedValue.text = "Select Category"
                        formContainer.visibility = View.GONE
                        selectedCategoryId = null
                        return
                    }

                    val selectedCategory = sortedCategories[position - 1]
                    selectedCategoryId = selectedCategory.id
                    selectedValue.text = selectedCategory.categoryName
                    selectedValue.setTextColor(resources.getColor(R.color.black))

                    showCategoryQuestions(selectedCategory)
                    submitBtn.visibility = View.VISIBLE

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupTypeHeader() {
        val headerTitle = "${typeData?.typeAlias} Conversation Form"

        conversationHeader.setup(
            config = CommonHeader.Config(
                title = headerTitle,
                isRequired = true,
                showInfoButton = false,
                showAddButton = false,
                showExpandButton = false
            )
        )
    }

    private fun showCategoryQuestions(category: Category) {

        formContainer.removeAllViews()
        nestedFieldsMap.clear()


        val questions = category.questionForm.sortedBy {
            it.displayOrder.toIntOrNull() ?: Int.MAX_VALUE
        }

        questions.forEach { question ->
            val fieldView = createQuestionField(question)
            fieldView.visibility = View.INVISIBLE
            formContainer.addView(fieldView)
        }


        val slideUp = android.view.animation.AnimationUtils.loadAnimation(
            context,
            R.anim.slide_down_to_bottom
        )

        slideUp.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {
                formContainer.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: android.view.animation.Animation?) {

                animateChildrenStaggered()
            }

            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
        })

        formContainer.startAnimation(slideUp)


        submitBtn.visibility = View.VISIBLE
        val fadeIn = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_in)
        fadeIn.startOffset = 300
        submitBtn.startAnimation(fadeIn)
    }

    private fun animateChildrenStaggered() {

        for (i in 0 until formContainer.childCount) {
            val child = formContainer.getChildAt(i)
            val childAnim = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.fade_in)
            childAnim.startOffset = (i * 100).toLong()
            childAnim.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {
                    child.visibility = View.VISIBLE
                }
                override fun onAnimationEnd(animation: android.view.animation.Animation?) {}
                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })
            child.startAnimation(childAnim)
        }
    }


    private fun createQuestionField(question: QuestionForm): View {
        val isRequired = question.isRequired == "1"
        val placeholder = question.placeholder
        val minLength = question.min.toIntOrNull() ?: 0
        val maxLength = question.max.toIntOrNull() ?: Int.MAX_VALUE
        val subject = question.questionLabel == "Subject"
        val questionId = question.id

        val view = when (question.fieldType) {
            "textarea" -> CommonTextAreaField(requireContext()).apply {
                setup(
                    config = CommonTextAreaField.Config(
                        title = question.questionLabel,
                        placeholder = placeholder,
                        isRequired = isRequired,
                        minLength = minLength,
                        maxLength = maxLength,
                        isSubjectField = subject,
                        infoTooltip = question.questionAdditionalInfo,
                        questionId = questionId,
                        onActionClick = { actionType, qId ->
                            handleActionClick(actionType, qId)
                        }
                    )
                )
                tag = questionId
            }

            "singlechoice" -> CommonSingleChoice(requireContext()).apply {
                val options = question.fieldValue.split(";").map { it.trim() }
                setup(
                    title = question.questionLabel,
                    options = options,
                    isRequired = isRequired,
                    infoTooltip = question.questionAdditionalInfo,
                    questionId = questionId,
                    onActionClick = { actionType, qId ->
                        handleActionClick(actionType, qId,)
                    },
                    onCommentClick = { qId ->
                        handleCommentClick(qId)
                    }
                )
                tag = questionId
            }

            "multichoice" -> CommonMultiChoice(requireContext()).apply {
                val options = question.fieldValue.split(";").map { it.trim() }
                setup(
                    title = question.questionLabel,
                    options = options,
                    isRequired = isRequired,
                    infoTooltip = question.questionAdditionalInfo,
                    questionId = questionId,
                    onActionClick = { actionType, qId ->
                        handleActionClick(actionType, qId)
                    },
                    onCommentClick = { qId ->
                        handleCommentClick(qId)
                    }
                )
                tag = questionId
            }

            "datetime" -> CommonDateTimePicker(requireContext()).apply {
                setup(
                    title = question.questionLabel,
                    isRequired = isRequired,
                    infoTooltip = question.questionAdditionalInfo
                )
                tag = questionId
            }

            "signature" -> CommonSignature(requireContext()).apply {
                config(
                    title = question.questionLabel,
                    isRequired = isRequired,
                    infoTooltip = question.questionAdditionalInfo
                )
                setGalleryLauncher(
                    gallery = galleryLauncher,
                    onLaunch = { signature ->
                        this@FormFragment.currentSignatureView = signature
                    }
                )
                tag = questionId
            }

            "nested" -> CommonNestedField(requireContext()).apply {
                setup(
                    title = question.questionLabel,
                    isRequired = isRequired,
                    infoTooltip = question.questionAdditionalInfo,
                    questionId = questionId,
                    onActionClick = { actionType, qId ->
                        handleActionClick(actionType, qId)
                    }
                )

                val sortedSubQuestions = question.questionSub.sortedBy {
                    it.displayOrder.toIntOrNull() ?: Int.MAX_VALUE
                }

                sortedSubQuestions.forEach { subQuestion ->
                    val subView = createSubQuestionView(subQuestion)
                    subView.tag = subQuestion.id
                    addNestedView(subView)
                }

                nestedFieldsMap[questionId] = this
                tag = questionId
            }

            else -> CommonTextAreaField(requireContext()).apply {
                setup(
                    config = CommonTextAreaField.Config(
                        title = question.questionLabel,
                        placeholder = placeholder,
                        isRequired = isRequired,
                        minLength = minLength,
                        maxLength = maxLength,
                        infoTooltip = question.questionAdditionalInfo,
                        questionId = questionId,
                        onActionClick = { actionType, qId ->
                            handleActionClick(actionType, qId)
                        }
                    )
                )
                tag = questionId
            }
        }
        fieldViewsMap[questionId] = view
        return view
    }

    private fun createSubQuestionView(subQuestion: QuestionSub): View {
        val isRequired = subQuestion.isRequired == "1"
        val placeholder = subQuestion.placeholder
        val minLength = subQuestion.min.toIntOrNull() ?: 0
        val maxLength = subQuestion.max.toIntOrNull() ?: Int.MAX_VALUE
        val subQuestionId = subQuestion.id

        return when (subQuestion.fieldType) {
            "textarea" -> CommonTextAreaField(requireContext()).apply {
                setup(
                    config = CommonTextAreaField.Config(
                        title = subQuestion.questionLabel,
                        placeholder = placeholder,
                        isRequired = isRequired,
                        minLength = minLength,
                        maxLength = maxLength,
                        isSubjectField = false,
                        isNested = true,
                        infoTooltip = subQuestion.questionAdditionalInfo,
                        questionId = subQuestionId,
                        showHeader = true,
                        isExpandable = true,
                        isInitiallyExpanded = false,
                        onActionClick = { actionType, qId ->
                            handleActionClick(actionType, qId)
                        }
                    )
                )
                tag = subQuestionId
            }

            "singlechoice" -> CommonSingleChoice(requireContext()).apply {
                val options = subQuestion.fieldValue.split(";").map { it.trim() }
                setup(
                    title = subQuestion.questionLabel,
                    options = options,
                    isRequired = isRequired,
                    infoTooltip = subQuestion.questionAdditionalInfo,
                    questionId = subQuestionId,
                    showHeader = true,
                    isNested = true,
                    isExpandable = true,
                    isInitiallyExpanded = false,
                    onActionClick = { actionType, qId ->
                        handleActionClick(actionType, qId)
                    },
                    onCommentClick = { qId ->
                        handleCommentClick(qId)
                    }
                )
                tag = subQuestionId
            }

            "multichoice" -> CommonMultiChoice(requireContext()).apply {
                val options = subQuestion.fieldValue.split(";").map { it.trim() }
                setup(
                    title = subQuestion.questionLabel,
                    options = options,
                    isRequired = isRequired,
                    infoTooltip = subQuestion.questionAdditionalInfo,
                    questionId = subQuestionId,
                    showHeader = true,
                    isNested = true,
                    isExpandable = true,
                    isInitiallyExpanded = false,
                    onActionClick = { actionType, qId ->
                        handleActionClick(actionType, qId)
                    },
                    onCommentClick = { qId ->
                        handleCommentClick(qId)
                    }
                )
                tag = subQuestionId
            }

            "datetime" -> CommonDateTimePicker(requireContext()).apply {
                setup(
                    title = subQuestion.questionLabel,
                    isRequired = isRequired,
                    infoTooltip = subQuestion.questionAdditionalInfo,
                    placeholder = "mm/dd/yyyy hh:mm",
                    isNested = true,
                    showHeader = true,
                    isExpandable = true,
                    isInitiallyExpanded = false
                )
                tag = subQuestionId
            }

            "signature" -> CommonSignature(requireContext()).apply {
                config(
                    title = subQuestion.questionLabel,
                    isRequired = isRequired,
                    infoTooltip = subQuestion.questionAdditionalInfo,
                    isNested = true,
                    isExpandable = true,
                    isInitiallyExpanded = false
                )
                setGalleryLauncher(
                    gallery = galleryLauncher,
                    onLaunch = { signature ->
                        this@FormFragment.currentSignatureView = signature
                    }
                )
                tag = subQuestionId
            }

            else -> CommonTextAreaField(requireContext()).apply {
                setup(
                    config = CommonTextAreaField.Config(
                        title = subQuestion.questionLabel,
                        placeholder = placeholder,
                        isRequired = isRequired,
                        minLength = minLength,
                        maxLength = maxLength,
                        infoTooltip = subQuestion.questionAdditionalInfo,
                        questionId = subQuestionId,
                        showHeader = true,
                        isExpandable = true,
                        isNested = true,
                        isInitiallyExpanded = false,
                        onActionClick = { actionType, qId ->
                            handleActionClick(actionType, qId)
                        }
                    )
                )
                tag = subQuestionId
            }
        }
    }

    private fun handleActionClick(
        actionType: CommonHeader.ActionType,
        questionId: String?,
    ) {
        when (actionType) {
            CommonHeader.ActionType.CREATE_ACTION -> {
                Toast.makeText(
                    context,
                    "Action $questionId",
                    Toast.LENGTH_LONG
                ).show()
            }

            CommonHeader.ActionType.LINK_EXISTING_ACTION -> {
                Toast.makeText(
                    context,
                    "Linked Action $questionId",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun handleCommentClick(questionId: String?) {
        questionId?.let { id ->
            commentsMap[id] = ""
        }
    }

    companion object {
        fun newInstance(typeId: String, feedbackTypeId: String): FormFragment {
            return FormFragment().apply {
                arguments = Bundle().apply {
                    putString("type_id", typeId)
                    putString("feedback_type_id", feedbackTypeId)
                }
            }
        }
    }
}