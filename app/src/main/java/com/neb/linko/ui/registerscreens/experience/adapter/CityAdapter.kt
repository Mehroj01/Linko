package com.neb.linko.ui.registerscreens.experience.adapter

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neb.linko.R
import com.neb.linko.databinding.ExperienceItemBinding
import com.neb.linko.ui.registerscreens.experience.models.SelectItem

class CityAdapter(
    var list: ArrayList<SelectItem>,
    var experienceClick: ExperienceClick,
    var resources: Resources
) :
    RecyclerView.Adapter<CityAdapter.CityHolder>() {

    inner class CityHolder(var experienceItemBinding: ExperienceItemBinding) :
        RecyclerView.ViewHolder(experienceItemBinding.root) {
        fun onBind(item: SelectItem) {
            experienceItemBinding.itemTv.text = item.str

            if (item.isSelect!!) {
                experienceItemBinding.itemTv.setTextColor(resources.getColor(R.color.primary))
                experienceItemBinding.item.setBackgroundResource(R.drawable.category_item_bg)
            } else {
                experienceItemBinding.itemTv.setTextColor(resources.getColor(R.color.av_gray))
                experienceItemBinding.item.setBackgroundResource(R.drawable.category_default_item_bg)
            }

            experienceItemBinding.item.setOnClickListener {
                experienceClick.click(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityHolder {
        return CityHolder(
            ExperienceItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CityHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size

    interface ExperienceClick {
        fun click(selectItem: SelectItem)
    }

}