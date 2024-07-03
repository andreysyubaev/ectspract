package com.bignerdranch.android.z2.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bignerdranch.android.z2.ui.adapters.ProductAdapter
import com.bignerdranch.android.z2.ui.models.Product
import com.example.android.z2.databinding.FragmentProductsBinding

class ProductsFragment : Fragment() {

    private lateinit var binding: FragmentProductsBinding
    private lateinit var products: List<Product>
    private lateinit var context:  Context
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductsBinding.inflate(inflater, container, false)

        products = listOf(
            Product(1,"кошка"),
            Product(2,"собака"),
            Product(3,"лось"),
            Product(4,"сфчик"),
            Product(5,"бара"),
            Product(6,"афганчик"),
            Product(7,"скуфчик"),
            Product(8,"бананчик"),
            Product(9,"яблочко"),
            Product(10,"оптимуспрайм"),
        )

        context = this.requireContext()
        adapter = ProductAdapter.create(context)
        binding.rvProducts.layoutManager = LinearLayoutManager(context)
        binding.rvProducts.adapter = adapter

        adapter.refreshProducts(products)



        val dashboardViewModel = ViewModelProvider(this).get(ProductsViewModel::class.java)

        return binding.root
    }

}