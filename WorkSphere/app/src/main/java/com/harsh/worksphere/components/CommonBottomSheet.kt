// com.harsh.worksphere.components/CommonBottomSheet.kt
package com.harsh.worksphere.components

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.material.R.style
import com.harsh.worksphere.R

class CommonBottomSheet : DialogFragment() {

    data class Config(
        val title: String = "Select Item",
        val emptyMessage: String = "No items found",
        val contentLayoutRes: Int,
        val contentBinder: (View) -> Unit,
        val onClose: (() -> Unit)? = null
    )

    private var titleText: TextView? = null
    private var contentContainer: FrameLayout? = null
    private var progressBar: ProgressBar? = null
    private var emptyText: TextView? = null
    private var closeButton: ImageButton? = null

    private var config: Config? = null

    // Pending states to apply after view creation
    private var pendingLoadingState: Boolean? = null
    private var pendingEmptyState: Boolean? = null

    companion object {
        fun newInstance(config: Config): CommonBottomSheet {
            return CommonBottomSheet().apply {
                arguments = Bundle().apply {
                    // Store config in arguments for recreation
                }
                this.config = config
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retain instance to survive config changes
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), style.Animation_Material3_BottomSheetDialog)

        val view = LayoutInflater.from(context).inflate(R.layout.common_bottomsheet_layout, null)
        dialog.setContentView(view)

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                (resources.displayMetrics.heightPixels * 0.7).toInt()
            )
            setGravity(Gravity.BOTTOM)
            attributes.windowAnimations = style.Animation_Material3_BottomSheetDialog
        }

        initViews(view)
        setupContent()

        // Apply any pending states
        applyPendingStates()

        return dialog
    }

    private fun initViews(view: View) {
        titleText = view.findViewById(R.id.bottomsheet_title)
        contentContainer = view.findViewById(R.id.content_container)
        progressBar = view.findViewById(R.id.progressBar)
        emptyText = view.findViewById(R.id.emptyText)
        closeButton = view.findViewById(R.id.closeBottomSheet)

        config?.let { cfg ->
            titleText?.text = cfg.title
            emptyText?.text = cfg.emptyMessage
        }

        closeButton?.setOnClickListener {
            config?.onClose?.invoke()
            dismiss()
        }
    }

    private fun setupContent() {
        config?.let { cfg ->
            val contentView = LayoutInflater.from(context).inflate(cfg.contentLayoutRes, contentContainer, true)
            cfg.contentBinder(contentView)
        }
    }

    private fun applyPendingStates() {
        pendingLoadingState?.let { isLoading ->
            setLoadingInternal(isLoading)
            pendingLoadingState = null
        }
        pendingEmptyState?.let { isEmpty ->
            setEmptyStateInternal(isEmpty)
            pendingEmptyState = null
        }
    }

    private fun setLoadingInternal(isLoading: Boolean) {
        progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        contentContainer?.visibility = if (isLoading) View.GONE else View.VISIBLE
        emptyText?.visibility = View.GONE
    }

    private fun setEmptyStateInternal(show: Boolean) {
        emptyText?.visibility = if (show) View.VISIBLE else View.GONE
        contentContainer?.visibility = if (show) View.GONE else View.VISIBLE
        progressBar?.visibility = View.GONE
    }

    // Public methods - queue states if view not ready
    fun setLoading(isLoading: Boolean) {
        if (!isViewReady()) {
            pendingLoadingState = isLoading
            return
        }
        setLoadingInternal(isLoading)
    }

    fun showEmptyState(show: Boolean) {
        if (!isViewReady()) {
            pendingEmptyState = show
            return
        }
        setEmptyStateInternal(show)
    }

    fun hideEmptyState() {
        if (!isViewReady()) {
            pendingEmptyState = false
            return
        }
        emptyText?.visibility = View.GONE
        contentContainer?.visibility = View.VISIBLE
    }

    fun updateTitle(newTitle: String) {
        titleText?.text = newTitle
    }

    private fun isViewReady(): Boolean {
        return progressBar != null && contentContainer != null && emptyText != null
    }

    override fun onDestroyView() {
        // Don't null out views if retaining instance
        if (!retainInstance) {
            titleText = null
            contentContainer = null
            progressBar = null
            emptyText = null
            closeButton = null
        }
        super.onDestroyView()
    }
}