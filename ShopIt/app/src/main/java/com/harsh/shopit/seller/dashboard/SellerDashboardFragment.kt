package com.harsh.shopit.seller.dashboard

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harsh.shopit.R
import com.harsh.shopit.seller.dashboard.adapter.SellerDashboardAdapter
import com.harsh.shopit.seller.dashboard.viewmodel.SellerDashboardViewmodel

class SellerDashboardFragment : Fragment(R.layout.seller_fragment_dashboard) {
    private lateinit var viewModel: SellerDashboardViewmodel
    private lateinit var adapter: SellerDashboardAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val emptyState = view.findViewById<TextView>(R.id.emptyState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.sellerDashboardProdRecycler)

        adapter = SellerDashboardAdapter()

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[SellerDashboardViewmodel::class.java]

        viewModel.topProducts.observe(viewLifecycleOwner) { list ->
            if(list.isEmpty()){
                emptyState.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }else{
                emptyState.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.submitList(list)
            }
        }
    }
}