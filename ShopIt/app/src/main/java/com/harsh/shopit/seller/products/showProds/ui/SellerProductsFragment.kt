package com.harsh.shopit.seller.products.showProds.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harsh.shopit.R
import com.harsh.shopit.main.screens.shop.data.local.entity.WishlistedProductsEntity
import com.harsh.shopit.main.screens.wishlist.ui.adapter.OnWishlistActionListener
import com.harsh.shopit.main.screens.wishlist.ui.adapter.WishlistAdapter
import com.harsh.shopit.main.screens.wishlist.viewmodel.WishlistViewModel
import com.harsh.shopit.seller.products.addProd.ui.SellerAddProductActivity
import com.harsh.shopit.seller.products.addProd.viewmodel.SellerAddProductViewmodel
import com.harsh.shopit.seller.products.local.entity.SellerAddProductEntity
import com.harsh.shopit.seller.products.showProds.ui.adapter.OnDeleteActionListener
import com.harsh.shopit.seller.products.showProds.ui.adapter.OnEditActionListener
import com.harsh.shopit.seller.products.showProds.ui.adapter.ProductAdapter
import com.harsh.shopit.seller.products.showProds.viewmodel.ProductViewModel

class SellerProductsFragment : Fragment(R.layout.seller_fragment_products) {
    private lateinit var viewModel: ProductViewModel
    private lateinit var adapter: ProductAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val emptyState = view.findViewById<TextView>(R.id.emptyStateProducts)
        val recyclerView = view.findViewById<RecyclerView>(R.id.sellerProductsRecycler)

        adapter = ProductAdapter(
            object : OnDeleteActionListener {
                override fun onRemoveClick(product: SellerAddProductEntity) {
                    viewModel.removeProduct(product.prodId)
                }
            },
            object : OnEditActionListener {
                override fun onEditClick(product: SellerAddProductEntity) {
                    val intent = Intent(requireContext(), SellerAddProductActivity::class.java)
                    intent.putExtra("product_id", product.prodId)
                    startActivity(intent)
                }
            }
        )

        recyclerView.layoutManager =  LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[ProductViewModel::class.java]
        viewModel.allProds.observe(viewLifecycleOwner) { list ->
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