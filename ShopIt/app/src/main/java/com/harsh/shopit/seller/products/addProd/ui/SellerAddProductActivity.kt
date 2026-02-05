package com.harsh.shopit.seller.products.addProd.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.material.button.MaterialButton
import com.harsh.shopit.R
import com.harsh.shopit.extensions.isNotNullOrEmpty
import com.harsh.shopit.main.utils.Resource
import com.harsh.shopit.seller.products.local.entity.SellerAddProductEntity
import com.harsh.shopit.seller.products.addProd.viewmodel.SellerAddProductViewmodel
import com.harsh.shopit.utils.customsnakbar.CustomSnackbar
import com.harsh.shopit.utils.notification.NotificationHelper

class SellerAddProductActivity : AppCompatActivity() {
    private lateinit var viewModel: SellerAddProductViewmodel

    private var isEditMode = false
    private var productId: Int = -1
    private val prodImage by lazy {
        findViewById<ImageView>(R.id.addedProdImage)
    }
    private var selectedImageUri: String? = null

    private val addProdImage by lazy {
        findViewById<FrameLayout>(R.id.addProdImage)
    }
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                selectedImageUri = uri.toString()
                prodImage.visibility = ImageView.VISIBLE
                addProdImage.visibility = FrameLayout.GONE
                prodImage.setImageURI(uri)
            }
        }


    fun navback() {
        val navback = findViewById<ImageButton>(R.id.navback)
        navback.setOnClickListener { finish() }
    }

    fun addProductBtnHandler() {
        val addProdImage = findViewById<FrameLayout>(R.id.addProdImage)
        val prodName = findViewById<EditText>(R.id.prodNameField)
        val prodPrice = findViewById<EditText>(R.id.prodPriceField)
        val prodQuantity = findViewById<EditText>(R.id.prodQuantityField)
        val prodDescription = findViewById<EditText>(R.id.prodDescField)
        val prodCategory = findViewById<EditText>(R.id.prodCategoryField)
        val prodStockSwitch =
            findViewById<SwitchCompat>(R.id.prodStockSwitch)
        val addProdBtn = findViewById<MaterialButton>(R.id.addProdBtn)
        addProdImage.setOnClickListener {
            imagePicker.launch("image/*")
        }
        addProdBtn.setOnClickListener {
            if (selectedImageUri == null) {
                CustomSnackbar.error(
                    findViewById(android.R.id.content),
                    "Please Select Product Image"
                )
            } else if (!prodName.isNotNullOrEmpty()) {
                prodName.error = "Product Name is required"
                prodName.requestFocus()
            } else if (!prodPrice.isNotNullOrEmpty()) {
                prodPrice.error = "Price is required"
                prodPrice.requestFocus()
            } else if (!prodQuantity.isNotNullOrEmpty()) {
                prodQuantity.error = "Quantity is required"
                prodQuantity.requestFocus()
            } else if (!prodCategory.isNotNullOrEmpty()) {
                prodCategory.error = "Product Category is required"
                prodCategory.requestFocus()
            } else if (!prodDescription.isNotNullOrEmpty()) {
                prodDescription.error = "Product Description is required"
                prodDescription.requestFocus()
            } else {
                val product = SellerAddProductEntity(
                    if (isEditMode) productId else 0,
                    prodName.text.toString(),
                    prodPrice.text.toString(),
                    prodQuantity.text.toString(),
                    prodDescription.text.toString(),
                    prodCategory.text.toString(),
                    prodStockSwitch.isChecked,
                    selectedImageUri.toString()
                )
                if (isEditMode) {
                    viewModel.updateProduct(product)
                } else {
                    viewModel.addProduct(product)
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun observeAddProductState() {

        val loader = findViewById<ProgressBar>(R.id.addProdLoader)
        val addProdBtn = findViewById<Button>(R.id.addProdBtn)
        val prodName = findViewById<EditText>(R.id.prodNameField)
        viewModel._products.observe(this) { result ->
            when (result) {
                is Resource.Loading -> {
                    loader.visibility = View.VISIBLE
                    addProdBtn.text = ""
                }

                is Resource.Success -> {
                    loader.visibility = View.GONE
                    addProdBtn.text = getString(R.string.add_product)
                    CustomSnackbar.success(
                        findViewById(android.R.id.content),
                        "Product Added Successfully"
                    )
                    NotificationHelper.showNotification(
                        this,
                        "Product Added Successfully",
                        "${prodName.text} is Added"
                    )
                    finish()
                }

                is Resource.Error -> {
                    loader.visibility = View.GONE
                    addProdBtn.text = getString(R.string.add_product)
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun loadProduct(id: Int) {
        val prodName = findViewById<EditText>(R.id.prodNameField)
        val prodPrice = findViewById<EditText>(R.id.prodPriceField)
        val prodQuantity = findViewById<EditText>(R.id.prodQuantityField)
        val prodDescription = findViewById<EditText>(R.id.prodDescField)
        val prodCategory = findViewById<EditText>(R.id.prodCategoryField)
        val prodStockSwitch =
            findViewById<SwitchCompat>(R.id.prodStockSwitch)
        viewModel.getProductById(id).observe(this) { product ->
            product?.let {
                prodName.setText(it.prodName)
                prodPrice.setText(it.prodPrice)
                prodQuantity.setText(it.prodQuantity)
                prodDescription.setText(it.prodDescription)
                prodCategory.setText(it.prodCategory)
                prodStockSwitch.isChecked = it.prodStock
                selectedImageUri = it.prodImage
            }
        }
    }

    fun changeTxt(editMode: Boolean){
        val addProdAppbarTitle = findViewById<TextView>(R.id.appbarAddProdTitle)
        addProdAppbarTitle.text =  if(editMode == true) "Edit Product" else "Add Product"
        val addProdBtn = findViewById<MaterialButton>(R.id.addProdBtn)
        addProdBtn.text = if(editMode == true) "Update" else "Add Product"
    }
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.seller_activity_add_product)
        viewModel = ViewModelProvider(this)[SellerAddProductViewmodel::class.java]
        addProductBtnHandler()
        navback()
        observeAddProductState()
        productId = intent.getIntExtra("product_id", -1)

        if (productId != -1) {
            isEditMode = true
            changeTxt(isEditMode)
            loadProduct(productId)
        }
    }
}