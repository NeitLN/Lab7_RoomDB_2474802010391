package edu.vlu.week07.productroom.data.repository

import androidx.lifecycle.LiveData
import edu.vlu.week07.productroom.data.local.ProductDao
import edu.vlu.week07.productroom.data.local.ProductEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository(
    private val productDao: ProductDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val allProducts: LiveData<List<ProductEntity>> = productDao.getAllProducts()

    suspend fun insert(product: ProductEntity) = withContext(ioDispatcher) {
        productDao.insertProduct(product)
    }

    suspend fun update(product: ProductEntity) = withContext(ioDispatcher) {
        productDao.updateProduct(product)
    }

    suspend fun delete(product: ProductEntity) = withContext(ioDispatcher) {
        productDao.deleteProduct(product)
    }

    suspend fun getById(productId: Int): ProductEntity? = withContext(ioDispatcher) {
        productDao.getProductById(productId)
    }

    suspend fun setFavorite(productId: Int, isFavorite: Boolean) = withContext(ioDispatcher) {
        productDao.setFavorite(productId, isFavorite)
    }

    suspend fun deleteAll() = withContext(ioDispatcher) {
        productDao.deleteAll()
    }
}
