package edu.vlu.week07.productroom.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.vlu.week07.productroom.R
import edu.vlu.week07.productroom.data.local.ProductEntity
import edu.vlu.week07.productroom.databinding.ItemProductBinding

class ProductAdapter(
    private val onFavoriteClick: (ProductEntity, Boolean) -> Unit,
    private val onDeleteClick: (ProductEntity) -> Unit
) : ListAdapter<ProductEntity, ProductAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))

    inner class VH(val b: ItemProductBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ProductEntity) {
            b.tvName.text = item.name
            b.tvDetail.text = b.root.context.getString(
                R.string.product_detail, item.price.toString(), item.quantity)
            b.ivFavorite.setImageResource(
                if (item.isFavorite) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
            b.ivFavorite.setOnClickListener { onFavoriteClick(item, !item.isFavorite) }
            b.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ProductEntity>() {
            override fun areItemsTheSame(a: ProductEntity, b: ProductEntity) = a.id == b.id
            override fun areContentsTheSame(a: ProductEntity, b: ProductEntity) = a == b
        }
    }
}
