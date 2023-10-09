package com.neb.linko.ui.save

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neb.linko.models.StoreModel
import com.squareup.picasso.Picasso

class BookmarkAdapter(
    var language: Boolean,
    var list: ArrayList<StoreModel>,
    var bookmarksClick: BookmarksClick
) :
    RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

    inner class BookmarkViewHolder(var storeItemBinding: com.neb.linko.databinding.StoreItemBinding) :
        RecyclerView.ViewHolder(storeItemBinding.root) {
        fun onBind(storeModel: StoreModel) {

            storeItemBinding.storeName.text =
                if (language) storeModel.name ?: "" else storeModel.nameArabic ?: ""

            storeItemBinding.saveIc.visibility = View.VISIBLE

            if (storeModel.bgImageUrl != null) {
                try {
                    Picasso.get().load(storeModel.bgImageUrl)
                        .into(storeItemBinding.imgBg)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (storeModel.imageUrl != null) {
                try {
                    Picasso.get().load(storeModel.imageUrl)
                        .into(storeItemBinding.storeAvatarIc)
                    storeItemBinding.storeAvatar.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            storeItemBinding.item.setOnClickListener {
                bookmarksClick.openStore(storeModel)
            }

            storeItemBinding.saveIc.setOnClickListener {
                bookmarksClick.saveStore(storeModel)
            }

            if (storeModel.latestPromo != null && storeModel.latestPromo?.percentage != null) {
                storeItemBinding.sale.visibility = View.VISIBLE
                storeItemBinding.saleTv.text = "%${storeModel.latestPromo?.percentage}"
            } else {
                storeItemBinding.sale.visibility = View.INVISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        return BookmarkViewHolder(
            com.neb.linko.databinding.StoreItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size

    interface BookmarksClick {
        fun openStore(storeModel: StoreModel)

        fun saveStore(storeModel: StoreModel)
    }

}