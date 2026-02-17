package com.harsh.noteitform.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import com.harsh.noteitform.R

class CommonMultiChoice @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var header: CommonHeader
    private lateinit var optionsContainer: LinearLayout
    private lateinit var cardView: MaterialCardView
    private var commentField: CommonCommentField? = null
    private val checkBoxes = mutableListOf<CheckBox>()
    private var isExpanded = true

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.common_multi_choice, this, true)
        initViews()
    }

    private fun initViews() {
        header = findViewById(R.id.multiChoiceHeader)
        cardView = findViewById(R.id.cardView)
        optionsContainer = findViewById(R.id.checkboxContainer)
    }
    private var isRequiredField: Boolean = false
    fun setup(
        title: String,
        options: List<String>,
        isRequired: Boolean,
        infoTooltip: String = "",
        questionId: String? = null,
        showHeader: Boolean = true,
        isNested: Boolean = false,
        isExpandable: Boolean = false,
        isInitiallyExpanded: Boolean = true,
        onActionClick: ((CommonHeader.ActionType, String?) -> Unit)? = null,
        onCommentClick: ((String?) -> Unit)? = null
    ) {
        this.isExpanded = isInitiallyExpanded
        this.isRequiredField = isRequired
        val menuConfig = CommonHeader.MenuConfig(
            fieldType = CommonHeader.FieldType.MULTI_CHOICE,
            questionId = questionId,
            onActionClick = { actionType, qId ->
                onActionClick?.invoke(actionType, qId)
            },
            onCommentClick = {
                onCommentClick?.invoke(questionId)
                addCommentField()
            }
        )

        if (showHeader) {
            header.visibility = View.VISIBLE
            val backgroundColor = if (isNested) {
                R.color.light_grey
            } else {
                null
            }
            header.setup(
                config = CommonHeader.Config(
                    title = title,
                    isRequired = isRequired,
                    showInfoButton = true,
                    showAddButton = true,
                    backgroundColorRes = backgroundColor,
                    showExpandButton = isExpandable,
                    isInitiallyExpanded = isInitiallyExpanded,
                    infoTooltip = infoTooltip,
                    menuConfig = menuConfig
                ),
                listener = if (isExpandable) object : CommonHeader.OnHeaderActionListener {
                    override fun onExpandClick(expanded: Boolean) {
                        isExpanded = expanded
                        animateToggleMultiChoice()
                    }
                } else null
            )
        } else {
            header.visibility = View.GONE
        }

        optionsContainer.removeAllViews()
        checkBoxes.clear()

        options.forEach { option ->
            val checkBox = CheckBox(context).apply {
                text = option
                textSize = 14f
                setTextColor(resources.getColor(R.color.black, null))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            checkBoxes.add(checkBox)
            optionsContainer.addView(checkBox)
        }

        toggleContent()
    }
    fun isRequired(): Boolean = isRequiredField
    private fun toggleContent() {
        val contentVisibility = if (isExpanded) View.VISIBLE else View.GONE
        cardView.visibility = contentVisibility
        commentField?.visibility = contentVisibility
    }
    private fun animateToggleMultiChoice() {
        if (isExpanded) {
            val anim = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    cardView.visibility =  View.VISIBLE
                    commentField?.visibility  = View.VISIBLE
                }
                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            cardView.startAnimation(anim)
            commentField?.startAnimation(anim)
        }
        else{
            val anim = AnimationUtils.loadAnimation(context, R.anim.fade_out)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    cardView.visibility =  View.GONE
                    commentField?.visibility  = View.GONE
                }
                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            cardView.startAnimation(anim)
            commentField?.startAnimation(anim)
        }
    }
    private fun addCommentField() {
        removeCommentField()

        commentField = CommonCommentField(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 0)
            }
            setup(
                title = "Comment",
                placeholder = "Enter your comment...",
                onDelete = {
                    removeCommentField()
                }
            )
        }

        optionsContainer.addView(commentField)
        if (!isExpanded) {
            commentField?.visibility = View.GONE
        }
    }

    private fun removeCommentField() {
        commentField?.let {
            optionsContainer.removeView(it)
            commentField = null
        }
    }

    fun getValue(): List<String> {
        return checkBoxes.filter { it.isChecked }.map { it.text.toString() }
    }

    fun getComment(): String {
        return commentField?.getValue() ?: ""
    }

}