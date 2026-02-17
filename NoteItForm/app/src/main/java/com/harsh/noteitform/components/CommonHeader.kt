package com.harsh.noteitform.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.harsh.noteitform.R

class CommonHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var headerContainer: ConstraintLayout
    private lateinit var tvTitle: TextView
    private lateinit var trailingContainer: LinearLayout
    private lateinit var tvCounter: TextView
    private lateinit var btnInfo: ImageButton
    private lateinit var btnAdd: ImageButton
    private lateinit var btnExpand: ImageButton
    private lateinit var btnDelete: ImageButton

    private var infoTooltipText: String = ""
    private var isExpanded = true
    private var menuConfig: MenuConfig? = null

    enum class FieldType {
        TEXTAREA, NESTED, SINGLE_CHOICE, MULTI_CHOICE, OTHER, COMMENT
    }

    data class MenuConfig(
        val fieldType: FieldType,
        val questionId: String? = null,
        val onActionClick: (ActionType, String?) -> Unit,
        val onCommentClick: ((String?) -> Unit)? = null
    )

    enum class ActionType {
        CREATE_ACTION, LINK_EXISTING_ACTION
    }

    data class Config(
        val title: String = "",
        val isRequired: Boolean = false,
        val backgroundColorRes: Int? = null,
        val counterText: String? = null,
        val showInfoButton: Boolean = false,
        val infoTooltip: String = "",
        val showAddButton: Boolean = false,
        val showExpandButton: Boolean = false,
        val isInitiallyExpanded: Boolean = true,
        val showDeleteButton: Boolean = false,
        val menuConfig: MenuConfig? = null
    )

    interface OnHeaderActionListener {
        fun onInfoClick() {}
        fun onAddClick() {}
        fun onExpandClick(isExpanded: Boolean) {}
        fun onDeleteClick() {}
    }

    init {
        orientation = VERTICAL
        inflate(context, R.layout.common_header, this)
        initViews()
    }

    private fun initViews() {
        headerContainer = findViewById(R.id.headerContainer)
        tvTitle = findViewById(R.id.tvTitle)
        trailingContainer = findViewById(R.id.trailingContainer)
        tvCounter = findViewById(R.id.tvCounter)
        btnInfo = findViewById(R.id.btnInfo)
        btnAdd = findViewById(R.id.btnAdd)
        btnExpand = findViewById(R.id.btnExpand)
        btnDelete = findViewById(R.id.btnDelete)
    }

    fun setup(config: Config, listener: OnHeaderActionListener? = null) {
        this.infoTooltipText = config.infoTooltip
        this.menuConfig = config.menuConfig

        tvTitle.text = if (config.isRequired) {
            "${config.title}*"
        } else {
            config.title
        }

        val bgColor = if (config.backgroundColorRes != null) {
            val color = ContextCompat.getColor(context, config.backgroundColorRes)
            headerContainer.backgroundTintList = ColorStateList.valueOf(color)
        } else {
            headerContainer.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
        }

        if (config.counterText != null) {
            tvCounter.text = config.counterText
            tvCounter.visibility = VISIBLE
        } else {
            tvCounter.visibility = GONE
        }

        btnInfo.visibility = if (config.showInfoButton || config.infoTooltip.isNotEmpty()) VISIBLE else GONE
        btnInfo.setOnClickListener {
            if (infoTooltipText.isNotEmpty()) {
                showInfoTooltip(infoTooltipText)
            }
            listener?.onInfoClick()
        }

        btnAdd.visibility = if (config.showAddButton) VISIBLE else GONE
        btnAdd.setOnClickListener {
            if (menuConfig != null) {
                showCustomPopupMenu()
            } else {
                listener?.onAddClick()
            }
        }

        isExpanded = config.isInitiallyExpanded
        btnExpand.visibility = if (config.showExpandButton) VISIBLE else GONE
        updateExpandIcon()
        btnExpand.setOnClickListener {
            isExpanded = !isExpanded
            updateExpandIcon()
            listener?.onExpandClick(isExpanded)
        }

        btnDelete.visibility = if (config.showDeleteButton) VISIBLE else GONE
        btnDelete.setOnClickListener {
            listener?.onDeleteClick()
        }

        trailingContainer.visibility = if (
            config.counterText != null ||
            config.showInfoButton ||
            config.showAddButton ||
            config.showExpandButton ||
            config.showDeleteButton
        ) VISIBLE else GONE
    }

    private fun showCustomPopupMenu() {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.custom_popup_menu, null)

        val popupWindow = PopupWindow(
            popupView,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            elevation = 16f
            isOutsideTouchable = true
        }

        val menuComment = popupView.findViewById<LinearLayout>(R.id.menuComment)
        val menuActionContainer = popupView.findViewById<LinearLayout>(R.id.menuActionContainer)
        val menuAction = popupView.findViewById<LinearLayout>(R.id.menuAction)
        val actionMenuIcon = popupView.findViewById<ImageView>(R.id.actionMenuIcon)
        val submenuContainer = popupView.findViewById<LinearLayout>(R.id.submenuContainer)
        val submenuCreateAction = popupView.findViewById<LinearLayout>(R.id.submenuCreateAction)
        val submenuLinkAction = popupView.findViewById<LinearLayout>(R.id.submenuLinkAction)

        val questionId = menuConfig?.questionId

        when (menuConfig?.fieldType) {
            FieldType.SINGLE_CHOICE, FieldType.MULTI_CHOICE -> {
                menuComment.visibility = View.VISIBLE
                menuActionContainer.visibility = View.VISIBLE

                menuComment.setOnClickListener {
                    menuConfig?.onCommentClick?.invoke(questionId)
                    popupWindow.dismiss()
                }

                submenuContainer.visibility = View.GONE
                actionMenuIcon.setImageResource(R.drawable.add)

                menuAction.setOnClickListener {
                    val isCurrentlyVisible = submenuContainer.visibility == View.VISIBLE
                    if (isCurrentlyVisible) {
                        submenuContainer.visibility = View.GONE
                        actionMenuIcon.setImageResource(R.drawable.add)
                    } else {
                        submenuContainer.visibility = View.VISIBLE
                        actionMenuIcon.setImageResource(R.drawable.minus)
                    }
                }
            }
            FieldType.TEXTAREA, FieldType.NESTED -> {
                menuComment.visibility = View.GONE
                menuActionContainer.visibility = View.VISIBLE

                submenuContainer.visibility = View.VISIBLE
                actionMenuIcon.setImageResource(R.drawable.minus)

                menuAction.isClickable = false
                menuAction.isFocusable = false
                menuAction.setBackgroundColor(Color.TRANSPARENT)
            }
            else -> {
                menuComment.visibility = View.GONE
                menuActionContainer.visibility = View.VISIBLE

                submenuContainer.visibility = View.VISIBLE
                actionMenuIcon.setImageResource(R.drawable.minus)
                menuAction.isClickable = false
                menuAction.isFocusable = false
                menuAction.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        submenuCreateAction.setOnClickListener {
            menuConfig?.onActionClick?.invoke(ActionType.CREATE_ACTION, questionId)
            popupWindow.dismiss()
        }

        submenuLinkAction.setOnClickListener {
            menuConfig?.onActionClick?.invoke(ActionType.LINK_EXISTING_ACTION, questionId)
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(btnAdd, 0, 8)
    }

    private fun updateExpandIcon() {
        val iconRes = if (isExpanded) {
            R.drawable.arrow_up
        } else {
            R.drawable.arrow_down
        }
        btnExpand.setImageResource(iconRes)
    }

    fun updateCounter(current: Int, max: Int) {
        tvCounter.text = "$current/$max"
    }


    fun setExpanded(expanded: Boolean, notifyListener: Boolean = false) {
        isExpanded = expanded
        updateExpandIcon()
        if (notifyListener) {
        }
    }


    private fun showInfoTooltip(message: String) {
        val inflater = LayoutInflater.from(context)
        val tooltipView = inflater.inflate(R.layout.tooltip_info, null)

        tooltipView.findViewById<TextView>(R.id.tooltipText).text = message

        val popupWindow = PopupWindow(
            tooltipView,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            elevation = 8f
            isOutsideTouchable = true
        }

        popupWindow.showAsDropDown(btnInfo, 0, -230)
    }
}