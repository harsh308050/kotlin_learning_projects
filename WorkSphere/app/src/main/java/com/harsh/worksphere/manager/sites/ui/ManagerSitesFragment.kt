package com.harsh.worksphere.manager.sites.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.harsh.worksphere.R
import com.harsh.worksphere.manager.sites.ui.adapter.SitesAdapter
import com.facebook.shimmer.ShimmerFrameLayout
import com.harsh.worksphere.manager.sites.viewmodel.SiteViewModel
import com.harsh.worksphere.components.CommonSnackbar.showError

class ManagerSitesFragment : Fragment(R.layout.manager_sites_fragment) {

    private lateinit var addSiteBtn: FrameLayout
    private lateinit var searchSiteBtn: ImageButton
    private lateinit var manageHeader: TextView
    private lateinit var searchField: TextInputEditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout

    private val viewModel: SiteViewModel by viewModels()
    private var sitesAdapter: SitesAdapter? = null
    private lateinit var shimmerLayout: ShimmerFrameLayout

    private var allSites: List<com.harsh.worksphere.manager.sites.data.model.SiteModel> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        setupRecyclerView(view)
        setupObservers()
        setupSearch()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchSites()
    }

    private fun initViews(view: View) {
        addSiteBtn = view.findViewById(R.id.addSiteBtn)
        searchSiteBtn = view.findViewById(R.id.site_search_btn)
        manageHeader = view.findViewById(R.id.manage_site_header)
        searchField = view.findViewById(R.id.site_search_field)
        recyclerView = view.findViewById(R.id.shopProdRecycler)
        shimmerLayout = view.findViewById(R.id.sites_shimmer_layout)
        emptyState = view.findViewById(R.id.sitesEmptyState)
    }

    private fun setupListeners() {
        addSiteBtn.setOnClickListener {
            val intent = Intent(requireContext(), ManagerAddSiteActivity::class.java)
            startActivity(intent)
        }

        searchSiteBtn.setOnClickListener {
            if (searchField.visibility == View.GONE) {
                manageHeader.visibility = View.GONE
                searchField.visibility = View.VISIBLE
                searchField.requestFocus()
                showKeyboard(searchField)
                searchSiteBtn.setImageResource(R.drawable.close)
            } else {
                hideKeyboard(searchField)
                searchField.clearFocus()
                searchField.text?.clear()
                searchField.visibility = View.GONE
                manageHeader.visibility = View.VISIBLE
                searchSiteBtn.setImageResource(R.drawable.search)
                // Reset to show all sites
                sitesAdapter?.submitList(allSites)
            }
        }
    }

    private fun setupSearch() {
        searchField.addTextChangedListener { text ->
            val query = text?.toString()?.trim()?.lowercase() ?: ""
            filterSites(query)
        }
    }

    private fun filterSites(query: String) {
        if (query.isEmpty()) {
            sitesAdapter?.submitList(allSites)
            return
        }

        val filtered = allSites.filter { site ->
            site.siteName.lowercase().contains(query) ||
                    site.clientName.lowercase().contains(query) ||
                    site.location.address.lowercase().contains(query) ||
                    site.supervisorName.lowercase().contains(query)
        }
        sitesAdapter?.submitList(filtered)
    }

    private fun setupRecyclerView(view: View) {
        sitesAdapter = SitesAdapter(
            onItemClick = { site ->
                val intent = Intent(requireContext(), ManagerAddSiteActivity::class.java).apply {
                    putExtra("SITE_DATA", site)
                    putExtra("IS_EDIT_MODE", true)
                }
                startActivity(intent)
            },
            onDeleteClick = { site ->
                viewModel.deleteSite(site.siteId)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = sitesAdapter
    }

    private fun setupObservers() {
        viewModel.sites.observe(viewLifecycleOwner) { sites ->
            allSites = sites
            sitesAdapter?.submitList(sites)
            // Show empty state only when not loading and list is empty
            val isLoading = viewModel.isLoadingSites.value == true
            emptyState.isVisible = sites.isEmpty() && !isLoading
            recyclerView.isVisible = sites.isNotEmpty()
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { showError(it) }
        }

        viewModel.isLoadingSites.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                shimmerLayout.isVisible = true
                shimmerLayout.startShimmer()
                recyclerView.isVisible = false
                emptyState.isVisible = false
            } else {
                shimmerLayout.stopShimmer()
                shimmerLayout.isVisible = false
            }
        }
    }

    private fun showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}