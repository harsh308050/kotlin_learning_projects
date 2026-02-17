package com.harsh.noteitform.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.harsh.noteitform.R
import java.util.Calendar

class CommonDateTimePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var header: CommonHeader
    private lateinit var btnSelect: MaterialButton
    private var isExpanded = true
    private var selectedDateTime: Calendar? = null

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.common_date_time_picker, this, true)
        initViews()
    }

    private fun initViews() {
        header = findViewById(R.id.dateTimePickerHeader)
        btnSelect = findViewById(R.id.btnSelect)
    }
    private var isRequiredField: Boolean = false
    fun setup(
        title: String,
        isRequired: Boolean,
        infoTooltip: String = "",
        placeholder: String = "mm/dd/yyyy hh:mm",
        isNested: Boolean = false,

        showHeader: Boolean = true,
        isExpandable: Boolean = false,
        isInitiallyExpanded: Boolean = true
    ) {
        this.isExpanded = isInitiallyExpanded
        this.isRequiredField = isRequired
        btnSelect.text = placeholder

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
                    showAddButton = false,
                    backgroundColorRes = backgroundColor,
                    showExpandButton = isExpandable,
                    isInitiallyExpanded = isInitiallyExpanded,
                    infoTooltip = infoTooltip
                ),
                listener = if (isExpandable) object : CommonHeader.OnHeaderActionListener {
                    override fun onExpandClick(expanded: Boolean) {
                        isExpanded = expanded
                        animateToggleDateTime()
                    }
                } else null
            )
        } else {
            header.visibility = View.GONE
        }

        btnSelect.setOnClickListener {
            showDateTimePicker()
        }

        toggleContent()
    }
    fun isRequired(): Boolean = isRequiredField
    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            context,
            R.style.OrangeDateTimePicker,

            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                TimePickerDialog(
                    context,
                    R.style.OrangeDateTimePicker,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)

                        selectedDateTime = calendar
                        updateButtonText()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateButtonText() {
        selectedDateTime?.let { cal ->
            val dateFormat = java.text.SimpleDateFormat("MM/dd/yyyy hh:mm a", java.util.Locale.getDefault())
            btnSelect.text = dateFormat.format(cal.time)
            btnSelect.setTextColor(context.resources.getColor(R.color.black, null))
        }
    }

    private fun toggleContent() {
        btnSelect.visibility = if (isExpanded) View.VISIBLE else View.GONE
    }

    fun getValue(): String {
        return selectedDateTime?.let { cal ->
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            dateFormat.format(cal.time)
        } ?: ""
    }
    private fun animateToggleDateTime() {
        if (isExpanded) {
            val anim = AnimationUtils.loadAnimation(context, R.anim.fade_in)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    btnSelect.visibility = View.VISIBLE
                }
                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            btnSelect.startAnimation(anim)
        }
        else{
            val anim = AnimationUtils.loadAnimation(context, R.anim.fade_out)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    btnSelect.visibility = View.GONE
                }
                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            })
            btnSelect.startAnimation(anim)
        }
    }

}