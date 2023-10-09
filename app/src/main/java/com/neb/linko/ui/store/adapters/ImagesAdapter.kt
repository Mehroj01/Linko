package com.neb.linko.ui.store.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neb.linko.databinding.ImagesItemBinding
import com.squareup.picasso.Picasso

class ImagesAdapter(var images: ArrayList<String>) :
    RecyclerView.Adapter<ImagesAdapter.ImagesHolder>() {

    inner class ImagesHolder(var imagesItemBinding: ImagesItemBinding) :
        RecyclerView.ViewHolder(imagesItemBinding.root) {
        fun onBind(url: String) {
            try {
                Picasso.get().load(url).into(imagesItemBinding.images)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesHolder {
        return ImagesHolder(
            ImagesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ImagesHolder, position: Int) {
        holder.onBind(images[position])
    }

    override fun getItemCount(): Int = images.size
}