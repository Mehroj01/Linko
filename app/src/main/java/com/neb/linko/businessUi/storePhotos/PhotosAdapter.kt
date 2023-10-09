package com.neb.linko.businessUi.storePhotos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neb.linko.databinding.StorePhotosImagesItemBinding
import com.squareup.picasso.Picasso

class PhotosAdapter(var photosList: ArrayList<PhotosModel>, var photosClick: PhotosClick) :
    RecyclerView.Adapter<PhotosAdapter.MyPhotosViewHolder>() {

    inner class MyPhotosViewHolder(var storePhotosImagesItemBinding: StorePhotosImagesItemBinding) :
        RecyclerView.ViewHolder(storePhotosImagesItemBinding.root) {
        fun onBind(photosModel: PhotosModel) {
            storePhotosImagesItemBinding.apply {
                cardForImg.setOnClickListener {
                    photosClick.photoClick(photosModel)
                }
                if (photosModel.photoUrl != "") {
                    plusTv.visibility = View.INVISIBLE
                    images.visibility = View.VISIBLE
                    try {
                        Picasso.get().load(photosModel.photoUrl).into(images)
                    } catch (e: Exception) {
                        plusTv.visibility = View.VISIBLE
                        images.visibility = View.INVISIBLE
                        e.printStackTrace()
                    }
                } else {
                    plusTv.visibility = View.VISIBLE
                    images.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPhotosViewHolder {
        return MyPhotosViewHolder(
            StorePhotosImagesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MyPhotosViewHolder, position: Int) {
        holder.onBind(photosList[position])
    }

    override fun getItemCount(): Int = photosList.size

    interface PhotosClick {
        fun photoClick(photosModel: PhotosModel)
    }

}