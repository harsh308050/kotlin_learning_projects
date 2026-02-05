package com.harsh.shopit.main.screens.wishlist.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harsh.shopit.R
import com.harsh.shopit.main.screens.shop.data.local.entity.WishlistedProductsEntity
import com.harsh.shopit.main.screens.wishlist.ui.adapter.OnWishlistActionListener
import com.harsh.shopit.main.screens.wishlist.ui.adapter.WishlistAdapter
import com.harsh.shopit.main.screens.wishlist.viewmodel.WishlistViewModel

class WishlistFragment : Fragment(R.layout.customer_fragment_wishlist) {
    private lateinit var viewModel: WishlistViewModel
    private lateinit var adapter: WishlistAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val emptyState = view.findViewById<TextView>(R.id.emptyStateWishlist)
        val recyclerView = view.findViewById<RecyclerView>(R.id.favProdRecycler)

        adapter = WishlistAdapter(
            object : OnWishlistActionListener {
                override fun onRemoveClick(product: WishlistedProductsEntity) {
                    viewModel.removeFromWishlist(product.id)
                }
            }
        )

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[WishlistViewModel::class.java]

        viewModel.wishlistedProds.observe(viewLifecycleOwner) { list ->
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