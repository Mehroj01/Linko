package com.neb.linko.ui.filter.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neb.linko.R
import com.neb.linko.databinding.FilterItemBinding
import com.neb.linko.ui.registerscreens.experience.models.SelectItem

class FilterAdapter(
    var list: ArrayList<SelectItem>,
    var filterClick: FilterClick,
    var resources: Resources
) :
    RecyclerView.Adapter<FilterAdapter.FilterHolder>() {

    inner class FilterHolder(var filterItemBinding: FilterItemBinding) :
        RecyclerView.ViewHolder(filterItemBinding.root) {
        fun onBind(item: SelectItem) {
            filterItemBinding.itemTv.text = item.str

            if (item.isSelect!!) {
                filterItemBinding.itemTv.setTextColor(resources.getColor(R.color.primary))
                filterItemBinding.item.setBackgroundResource(R.drawable.category_item_bg)
            } else {
                filterItemBinding.itemTv.setTextColor(resources.getColor(R.color.av_gray))
                filterItemBinding.item.setBackgroundResource(R.drawable.category_default_item_bg)
            }

            filterItemBinding.item.setOnClickListener {
                filterClick.click(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterHolder {
        return FilterHolder(
            FilterItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FilterHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size

    interface FilterClick {
        fun click(selectItem: SelectItem)
    }

}