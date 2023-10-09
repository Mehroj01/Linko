package com.neb.linko.ui.category.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neb.linko.databinding.ExploreItemBinding
import com.neb.linko.models.StoreModel
import com.squareup.picasso.Picasso

class ExploreAdapter(var explorers: ArrayList<StoreModel?>, var exploreClick: ExploreClick) :
    RecyclerView.Adapter<ExploreAdapter.ExploreViewHolder>() {

    inner class ExploreViewHolder(var exploreItemBinding: ExploreItemBinding) :
        RecyclerView.ViewHolder(exploreItemBinding.root) {
        fun onBind(storeModel: StoreModel?) {
            try {
                Picasso.get().load(storeModel?.imageUrl).into(exploreItemBinding.exploreImg)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            exploreItemBinding.item.setOnClickListener {
                exploreClick.exploreClick(storeModel!!)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreViewHolder {
        return ExploreViewHolder(
            ExploreItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ExploreViewHolder, position: Int) {
        holder.onBind(explorers[position])
    }

    override fun getItemCount(): Int = explorers.size

    interface ExploreClick {
        fun exploreClick(storeModel: StoreModel)
    }
}