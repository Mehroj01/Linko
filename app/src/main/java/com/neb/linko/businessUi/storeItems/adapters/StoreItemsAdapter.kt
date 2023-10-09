package com.neb.linko.businessUi.storeItems.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.neb.linko.R
import com.neb.linko.databinding.StoreItemItemBinding
import com.neb.linko.models.ItemModel
import com.squareup.picasso.Picasso

class StoreItemsAdapter(
    var context: Context,
    var itemsList: ArrayList<ItemModel>,
    var itemClick: ItemClick
) :
    RecyclerView.Adapter<StoreItemsAdapter.MyStoreItemsViewHolder>() {

    inner class MyStoreItemsViewHolder(var storeItemItemBinding: StoreItemItemBinding) :
        RecyclerView.ViewHolder(storeItemItemBinding.root) {
        fun onBind(itemModel: ItemModel) {

            storeItemItemBinding.itemDeleteBtn.setOnClickListener {
                itemClick.deleteClick(itemModel.key ?: "")
            }

            storeItemItemBinding.itemEditBtn.setOnClickListener {
                itemClick.editItem(itemModel)
            }

            if (itemModel.imageUrl != null) {
                try {
                    Picasso.get().load(itemModel.imageUrl)
                        .into(storeItemItemBinding.img)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            storeItemItemBinding.cat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_item,
                0,
                0,
                0
            )
            storeItemItemBinding.cat.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.primary
                )
            )

            itemModel.store?.let {
                storeItemItemBinding.cat.text = "${itemModel.price ?: ""} KD"
            } ?: run {
                storeItemItemBinding.cat.text = "N/A"
            }

            storeItemItemBinding.storeName.text = itemModel.name
            storeItemItemBinding.details.text = itemModel.details
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyStoreItemsViewHolder {
        return MyStoreItemsViewHolder(
            StoreItemItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyStoreItemsViewHolder, position: Int) {
        holder.onBind(itemsList[position])
    }

    override fun getItemCount(): Int = itemsList.size

    interface ItemClick {
        fun editItem(itemModel: ItemModel)
        fun deleteClick(key: String)
    }
}