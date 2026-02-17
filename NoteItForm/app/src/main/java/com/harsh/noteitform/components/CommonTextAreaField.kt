package com.harsh.noteitform.components

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.harsh.noteitform.R

class CommonTextAreaField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var header: CommonHeader? = null
    private lateinit var editText: EditText
    private var isExpanded = true

    data class Config(
        val title: String = "",
        val placeholder: String = "",
        val isRequired: Boolean = false,
        val minLength: Int = 0,
        val maxLength: Int = Int.MAX_VALUE,
        val isSubjectField: Boolean = false,
        val infoTooltip: String = "",
        val showHeader: Boolean = true,
        val questionId: String? = null,
        val isExpandable: Boolean = false,
        val isInitiallyExpanded: Boolean = true,
        val isNested: Boolean = false,
        val onActionClick: ((CommonHeader.ActionType, String?) -> Unit)? = null
    )

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.common_textarea_field, this, true)
        initViews()
    }

    private fun initViews() {
        header = findViewById(R.id.fieldHeader)
        editText = findViewById(R.id.textAreaFieldInput)
    }

    fun isRequired(): Boolean {

        return config?.isRequired ?: false
    }
    private var config: Config? = null

    fun setup(config: Config) {
        this.isExpanded = config.isInitiallyExpanded

        if (config.showHeader) {
            header?.visibility = View.VISIBLE
            val backgroundColor = if (config.isNested) {
                R.color.light_grey
            } else {
                null
            }

            val headerConfig = CommonHeader.Config(
                title = config.title,
                isRequired = config.isRequired,
                counterText = if (config.isSubjectField) "0/${config.maxLength}" else null,
                showInfoButton = !config.isSubjectField,
                showAddButton = !config.isSubjectField,
                showExpandButton = config.isExpandable,
                isInitiallyExpanded = config.isInitiallyExpanded,
                backgroundColorRes = backgroundColor,
                infoTooltip = config.infoTooltip,
                menuConfig = if (!config.isSubjectField) CommonHeader.MenuConfig(
                    fieldType = CommonHeader.FieldType.TEXTAREA,
                    questionId = config.questionId,
                    onActionClick = { actionType, qId ->
                        config.onActionClick?.invoke(actionType, qId)
                    }
                ) else null
            )

            header?.setup(
                config = headerConfig,
                listener = if (config.isExpandable) object : CommonHeader.OnHeaderActionListener {
                    override fun onExpandClick(expanded: Boolean) {
                        isExpanded = expanded
                        animateToggleEditText()
                    }
                } else null
            )
        } else {
            header?.visibility = View.GONE
        }

        editText.hint = config.placeholder

        if (config.maxLength < Int.MAX_VALUE) {
            editText.filters = arrayOf(InputFilter.LengthFilter(config.maxLength))
        }

        editText.addTextChangedListener { text ->
            val length = text?.length ?: 0
            if (config.isSubjectField && config.showHeader) {
                header?.updateCounter(length, config.maxLength)
            }

            val value = text?.toString() ?: ""
            when {
                config.isRequired && value.isEmpty() -> editText.error = "Required"
                config.minLength > 0 && value.length < config.minLength ->
                    editText.error = "Min ${config.minLength} characters"
                else -> editText.error = null
            }
        }

       toggleEditText()
    }

    private fun toggleEditText() {
        editText.visibility = if (isExpanded) View.VISIBLE else View.GONE
    }
    private fun animateToggleEditText() {
        if (isExpanded) {
            val anim = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    editText.visibility = View.VISIBLE
                }
                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            editText.startAnimation(anim)
        }
        else{
            val anim = AnimationUtils.loadAnimation(context, R.anim.fade_out)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    editText.visibility = View.GONE
                }
                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            editText.startAnimation(anim)
        }
    }
    fun getValue(): String = editText.text?.toString() ?: ""



}