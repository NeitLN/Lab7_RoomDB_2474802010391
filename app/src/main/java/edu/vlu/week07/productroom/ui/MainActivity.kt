package edu.vlu.week07.productroom.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import edu.vlu.week07.productroom.R
import edu.vlu.week07.productroom.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep the 16dp padding from the layout and add the system bar insets on top of it
        val pad = binding.root.paddingLeft
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left + pad, systemBars.top + pad,
                systemBars.right + pad, systemBars.bottom + pad)
            insets
        }

        adapter = ProductAdapter(
            onFavoriteClick = { p, fav -> viewModel.setFavorite(p, fav) },
            onDeleteClick = { p -> viewModel.deleteProduct(p) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Observe LiveData: the list refreshes by itself after every insert/delete
        viewModel.allProducts.observe(this) { list ->
            adapter.submitList(list)
        }

        binding.btnAdd.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
            val qty = binding.etQuantity.text.toString().toIntOrNull() ?: 0
            if (name.isBlank()) {
                Toast.makeText(this, R.string.err_blank_name, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.addProduct(name, price, qty)
            binding.etName.text?.clear()
            binding.etPrice.text?.clear()
            binding.etQuantity.text?.clear()
        }

        binding.btnClearAll.setOnClickListener { viewModel.clearAll() }
    }
}
