package edu.vlu.week07.productroom.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import edu.vlu.week07.productroom.data.local.AppDatabase
import edu.vlu.week07.productroom.data.local.ProductEntity
import edu.vlu.week07.productroom.data.repository.ProductRepository
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProductRepository
    val allProducts: LiveData<List<ProductEntity>>

    init {
        val productDao = AppDatabase.getDatabase(application).productDao()
        repository = ProductRepository(productDao)
        allProducts = repository.allProducts
    }

    fun addProduct(name: String, price: Double, quantity: Int) {
        viewModelScope.launch {
            repository.insert(ProductEntity(name = name, price = price, quantity = quantity))
        }
    }

    fun setFavorite(product: ProductEntity, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.setFavorite(product.id, isFavorite)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.delete(product)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
}
