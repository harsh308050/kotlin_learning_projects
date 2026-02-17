package com.harsh.noteitform.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import com.harsh.noteitform.R

class CommonNestedField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var header: CommonHeader
    private lateinit var nestedContainer: LinearLayout
    private lateinit var nestedCard: MaterialCardView

    private val nestedViews = mutableListOf<View>()

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.common_nested_field, this, true)
        initViews()
    }

    private fun initViews() {
        header = findViewById(R.id.nestedHeader)
        nestedContainer = findViewById(R.id.nestedContainer)
        nestedCard = findViewById(R.id.nestedCard)
    }

    fun setup(
        title: String,
        isRequired: Boolean,
        infoTooltip: String = "",
        questionId: String? = null,
        onActionClick: ((CommonHeader.ActionType, String?) -> Unit)? = null
    ) {
        header.setup(
            config = CommonHeader.Config(
                title = title,
                isRequired = isRequired,
                showInfoButton = true,
                showAddButton = true,
                showExpandButton = false,
                infoTooltip = infoTooltip,
                menuConfig = CommonHeader.MenuConfig(
                    fieldType = CommonHeader.FieldType.NESTED,
                    questionId = questionId,
                    onActionClick = { actionType, qId ->
                        onActionClick?.invoke(actionType, qId)
                    }
                )
            )
        )

        nestedContainer.removeAllViews()
        nestedViews.clear()
    }

    fun addNestedView(view: View) {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            if (nestedContainer.childCount > 0) {
                setMargins(0, 16, 0, 0)
            }
        }
        view.layoutParams = params

        nestedViews.add(view)
        nestedContainer.addView(view)
    }

    fun getNestedViews(): List<View> = nestedViews.toList()

    fun getNestedValues(): Map<String, Any> {
        val values = mutableMapOf<String, Any>()

        nestedViews.forEach { view ->
            val value = when (view) {
                is CommonTextAreaField -> view.getValue()
                is CommonSingleChoice -> view.getValue()
                is CommonMultiChoice -> view.getValue()
                is CommonDateTimePicker -> view.getValue()
                is CommonSignature -> view.getSignatureBitmap()?.let {
                    "signature_${view.tag}.png"
                } ?: ""
                else -> null
            }

            val key = view.tag?.toString() ?: ""
            if (value != null && value != "") {
                values[key] = value
            }
        }

        return values
    }

    fun getNestedComments(): Map<String, String> {
        val comments = mutableMapOf<String, String>()

        nestedViews.forEach { view ->
            val questionId = view.tag?.toString() ?: return@forEach

            val comment = when (view) {
                is CommonSingleChoice -> view.getComment()
                is CommonMultiChoice -> view.getComment()
                else -> ""
            }

            if (comment.isNotEmpty()) {
                comments[questionId] = comment
            }
        }

        return comments
    }
}