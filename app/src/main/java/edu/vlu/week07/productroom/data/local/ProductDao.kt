package edu.vlu.week07.productroom.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    // Part 4 debugging exercise: a plain (non-suspend) insert that runs on the caller's thread
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProductBlocking(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): LiveData<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): ProductEntity?

    @Query("UPDATE products SET is_favorite = :favorite WHERE id = :productId")
    suspend fun setFavorite(productId: Int, favorite: Boolean)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
