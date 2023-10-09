package com.neb.linko.ui.search.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.neb.linko.R
import com.neb.linko.databinding.SearchStoreItemBinding
import com.neb.linko.models.ItemModel
import com.neb.linko.models.StoreModel
import com.squareup.picasso.Picasso

class SearchStoreAdapter(
    var language: Boolean,
    var stores: ArrayList<StoreModel>,
    var itemsList: ArrayList<ItemModel>,
    var situation: Boolean,
    var context: Context,
    var searchClickListener: SearchClickListener
) :
    RecyclerView.Adapter<SearchStoreAdapter.SearchStoreViewHolder>() {

    inner class SearchStoreViewHolder(var searchStoreItemBinding: SearchStoreItemBinding) :
        RecyclerView.ViewHolder(searchStoreItemBinding.root) {
        fun onStoreBind(storeModel: StoreModel) {
            searchStoreItemBinding.cat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_search_cat,
                0,
                0,
                0
            )
            searchStoreItemBinding.ratingText.visibility = View.VISIBLE
            searchStoreItemBinding.price.visibility = View.GONE
            if (storeModel.imageUrl != null) {
                try {
                    Picasso.get().load(storeModel.imageUrl)
                        .into(searchStoreItemBinding.img)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            searchStoreItemBinding.storeName.text =
                if (language) storeModel.name else storeModel.nameArabic
            searchStoreItemBinding.details.text =
                if (language) storeModel.details else storeModel.detailsArabic

            searchStoreItemBinding.cat.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.av_gray
                )
            )
            storeModel.category?.let {
                searchStoreItemBinding.cat.text = if (language) it.name else it.nameArabic
            } ?: run {
                searchStoreItemBinding.cat.text = "N/A"
            }

            searchStoreItemBinding.ratingText.text = String.format("%.1f", storeModel.rating)

            searchStoreItemBinding.root.setOnClickListener {
                searchClickListener.clickStore(storeModel)
            }

        }

        fun onItemBind(itemModel: ItemModel) {
            searchStoreItemBinding.cat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_item,
                0,
                0,
                0
            )
            searchStoreItemBinding.ratingText.visibility = View.GONE
            searchStoreItemBinding.price.visibility = View.VISIBLE
            searchStoreItemBinding.price.text = "${itemModel.price ?: ""} KD"
            if (itemModel.imageUrl != null) {
                try {
                    Picasso.get().load(itemModel.imageUrl)
                        .into(searchStoreItemBinding.img)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            searchStoreItemBinding.storeName.text =
                if (language) itemModel.name else itemModel.nameArabic
            searchStoreItemBinding.details.text =
                if (language) itemModel.details else itemModel.detailsArabic

            searchStoreItemBinding.cat.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.primary
                )
            )
            itemModel.store?.let {
                searchStoreItemBinding.cat.text = if (language) it.name else it.nameArabic
            } ?: run {
                searchStoreItemBinding.cat.text = "N/A"
            }

            searchStoreItemBinding.root.setOnClickListener {
                searchClickListener.clickItem(itemModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchStoreViewHolder {
        return SearchStoreViewHolder(
            SearchStoreItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SearchStoreViewHolder, position: Int) {
        if (situation) holder.onStoreBind(stores[position]) else holder.onItemBind(itemsList[position])
    }

    override fun getItemCount(): Int {
        return if (situation) stores.size else itemsList.size
    }

    interface SearchClickListener {
        fun clickItem(itemModel: ItemModel)
        fun clickStore(storeModel: StoreModel)
    }

}