package com.harsh.noteitform.components


import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.harsh.noteitform.R

class CommonCommentField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var header: CommonHeader
    private lateinit var textArea: CommonTextAreaField
    private var isExpanded = true
    private var onDeleteListener: (() -> Unit)? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.common_comment_field, this, true)
        initViews()
    }

    private fun initViews() {
        header = findViewById(R.id.commentHeader)
        textArea = findViewById(R.id.commentTextArea)
    }

    fun setup(
        title: String = "Comment",
        placeholder: String = "Enter your comment...",
        onDelete: (() -> Unit)? = null
    ) {
        this.onDeleteListener = onDelete

        header.setup(
            config = CommonHeader.Config(
                title = title,
                isRequired = false,
                showInfoButton = false,
                showAddButton = false,
                showExpandButton = true,
                isInitiallyExpanded = true,
                showDeleteButton = true
            ),
            listener = object : CommonHeader.OnHeaderActionListener {
                override fun onExpandClick(isExpanded: Boolean) {
                    this@CommonCommentField.isExpanded = isExpanded
                    textArea.visibility = if (isExpanded) View.VISIBLE else View.GONE
                }

                override fun onDeleteClick() {
                    onDeleteListener?.invoke()
                }
            }
        )

        textArea.setup(
            config = CommonTextAreaField.Config(
                title = "",
                placeholder = placeholder,
                isRequired = false,
                minLength = 0,
                maxLength = 500,
                showHeader = false
            )
        )
    }
    private fun animateToggleEditText() {
        if (isExpanded) {
            val anim = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    textArea.visibility = View.VISIBLE
                }
                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            textArea.startAnimation(anim)
        }
        else{
            val anim = AnimationUtils.loadAnimation(context, R.anim.fade_out)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    textArea.visibility = View.GONE
                }
                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            textArea.startAnimation(anim)
        }
    }
    fun getValue(): String = textArea.getValue()
}