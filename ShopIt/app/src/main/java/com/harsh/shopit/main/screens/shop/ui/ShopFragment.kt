package com.harsh.shopit.main.screens.shop.ui

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harsh.shopit.R
import com.harsh.shopit.main.screens.shop.data.model.Product
import com.harsh.shopit.main.screens.shop.ui.adapter.OnProductActionListener
import com.harsh.shopit.main.screens.shop.ui.adapter.ShopAdapter
import com.harsh.shopit.main.screens.shop.viewModel.ShopViewModel
import com.harsh.shopit.main.utils.Resource

class ShopFragment : Fragment(R.layout.customer_fragment_shop) ,
    OnProductActionListener {
    override fun onFavClick(product: Product) {
        viewModel.toggleWishlist(product)
    }
    fun getproducts(view : View){
        val loader = view.findViewById<ProgressBar>(R.id.prodProgressBar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.shopProdRecycler)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.setHasFixedSize(true)
        shopAdapter = ShopAdapter(arrayListOf(),this
            )
        recyclerView.adapter = shopAdapter

        viewModel = ViewModelProvider(this)[ShopViewModel::class.java]

        viewModel._products.observe(viewLifecycleOwner) {
            result ->
                when (result) {
                    is Resource.Loading -> {
                        loader.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                    is Resource.Success -> {
                        loader.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        shopAdapter.updateList(result.data)
                    }
                    is Resource.Error -> {
                        loader.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
        viewModel.fetchProducts()
    }

    private lateinit var shopAdapter: ShopAdapter
    private lateinit var viewModel: ShopViewModel
    private val wishlistedIds = mutableSetOf<Int>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getproducts(view)
        viewModel.wishlistedIds.observe(viewLifecycleOwner) { ids ->
            wishlistedIds.clear()
            wishlistedIds.addAll(ids)
            shopAdapter.updateWishlistIds(wishlistedIds)
        }
    }
}