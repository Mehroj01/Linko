package com.neb.linko.businessUi.boosts.adapters

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neb.linko.R
import com.neb.linko.databinding.BoostsItemBinding
import com.neb.linko.models.BoostModel

class BoostsAdapters(
    var boostsList: ArrayList<BoostModel>,
    var boostsClick: BoostsClick,
    var resources: Resources
) :
    RecyclerView.Adapter<BoostsAdapters.MyBoostsViewHolder>() {

    inner class MyBoostsViewHolder(var boostsItemBinding: BoostsItemBinding) :
        RecyclerView.ViewHolder(boostsItemBinding.root) {
        fun onBind(boostModel: BoostModel) {
            when (boostModel.type) {
                0 -> {
                    boostsItemBinding.img.setImageResource(R.drawable.ic_boost_item_1)
                    boostsItemBinding.boostDeck.text = "Your Store will publish on the main screen on Linko. Priority is given to those who book first "
                    boostsItemBinding.boostBtn.setBackgroundResource(R.drawable.primary_btn_bg)
                    boostsItemBinding.boostItem.setCardBackgroundColor(resources.getColor(R.color.boost_black_item))
                }
                1 -> {
                    boostsItemBinding.img.setImageResource(R.drawable.ic_boost_item_2)
                    boostsItemBinding.boostDeck.text =
                        "Your store will be pinned to be the first one in your store category for 24 hours"
                    boostsItemBinding.boostBtn.setBackgroundResource(R.drawable.primary_btn_bg)
                    boostsItemBinding.boostItem.setCardBackgroundColor(resources.getColor(R.color.boost_black_item))
                }
                2 -> {
                    boostsItemBinding.img.setImageResource(R.drawable.ic_boost_item_3)
                    boostsItemBinding.boostDeck.text =
                        "Book your place among the first 5 stores in your category for 24 hours"
                    boostsItemBinding.boostBtn.setBackgroundResource(R.drawable.black_btn_bg)
                    boostsItemBinding.boostItem.setCardBackgroundColor(resources.getColor(R.color.boost_primary_item))
                }
                else -> {
                    boostsItemBinding.img.setImageResource(0)
                    boostsItemBinding.boostDeck.text = ""
                    boostsItemBinding.boostBtn.setBackgroundResource(0)
                    boostsItemBinding.boostItem.setCardBackgroundColor(0)
                }
            }
            boostsItemBinding.title.text = boostModel.title
            boostsItemBinding.price.text = "${boostModel.price} KWD"

            boostsItemBinding.boostItem.setOnClickListener {
                boostsClick.click(boostModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBoostsViewHolder {
        return MyBoostsViewHolder(
            BoostsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyBoostsViewHolder, position: Int) {
        holder.onBind(boostsList[position])
    }

    override fun getItemCount(): Int = boostsList.size

    interface BoostsClick {
        fun click(boostModel: BoostModel)
    }

}