package com.neb.linko.businessUi.storePromoCodes.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neb.linko.databinding.PromoRvItemBinding
import com.neb.linko.models.PromoModel

class PromoAdapter(
    var promosList: ArrayList<PromoModel>,
    var editPromoCodeClick: EditPromoCodeClick
) :
    RecyclerView.Adapter<PromoAdapter.MyPromoViewHolder>() {

    inner class MyPromoViewHolder(var promoRvItemBinding: PromoRvItemBinding) :
        RecyclerView.ViewHolder(promoRvItemBinding.root) {
        fun onBind(promoModel: PromoModel) {
            promoRvItemBinding.promoEt.setText(promoModel.promoCode)
            promoRvItemBinding.promoClaimedCount.text = "${promoModel.claimsCount ?: 0} Claimed"
            promoRvItemBinding.promoEditBtn.setOnClickListener {
                editPromoCodeClick.click(promoModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPromoViewHolder {
        return MyPromoViewHolder(
            PromoRvItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyPromoViewHolder, position: Int) {
        holder.onBind(promosList[position])
    }

    override fun getItemCount(): Int = promosList.size

    interface EditPromoCodeClick {
        fun click(promoModel: PromoModel)
    }

}