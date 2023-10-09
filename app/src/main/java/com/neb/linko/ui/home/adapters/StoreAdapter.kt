package com.neb.linko.ui.home.adapters

import android.content.Context
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.neb.linko.R
import com.neb.linko.databinding.ExplorersContainerBinding
import com.neb.linko.databinding.StoreItemBinding
import com.neb.linko.models.StoreModel
import com.neb.linko.ui.category.adapters.ExploreAdapter
import com.squareup.picasso.Picasso

class StoreAdapter(
    var language: Boolean,
    var list: ArrayList<StoreModel?>,
    var favorites: ArrayList<String>,
    var explorers: ArrayList<StoreModel?>,
    var storeClick: StoreClick,
    var context: Context
) :
    RecyclerView.Adapter<StoreAdapter.StoreViewHolder>() {

    var storeModel1: StoreModel? = null
    var handler = Handler()
    var view: View? = null

    inner class StoreViewHolder(
        var storeItemBinding: StoreItemBinding,
        var explorersContainerBinding: ExplorersContainerBinding
    ) :
        RecyclerView.ViewHolder(view!!) {
        fun onBind(storeModel: StoreModel?) {
            if (storeModel != null) {
                storeItemBinding.storeName.text =
                    if (language) storeModel.name ?: "" else storeModel.nameArabic ?: ""


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

                var a = 0L
                var b = 0L

                storeItemBinding.item.setOnClickListener {
                    storeModel1 = storeModel

                    a = b
                    b = System.currentTimeMillis()

                    if (b - a < 260) {
                        handler.removeCallbacks(run)
                        b = 0
                        a = 0
                        storeClick.saveStore(storeModel)
                    } else {
                        handler.removeCallbacks(run)
                        handler.postDelayed(run, 250)
                    }
                }

                storeItemBinding.saveIc.setOnClickListener {
                    storeClick.saveClick(storeModel)
                }

                var favoriteSituation = false
                if (favorites.isNotEmpty()) {
                    for (favorite in favorites) {
                        if (storeModel.key != null && storeModel.key == favorite) {
                            favoriteSituation = true
                            break
                        }
                    }
                }
                if (favoriteSituation){
                    storeItemBinding.saveIc.visibility = View.VISIBLE
                } else{
                    storeItemBinding.saveIc.visibility = View.INVISIBLE
                }

                if (storeModel.latestPromo != null && storeModel.latestPromo?.percentage != null) {
                    storeItemBinding.sale.visibility = View.VISIBLE
                    storeItemBinding.saleTv.text = "%${storeModel.latestPromo?.percentage}"
                } else {
                    storeItemBinding.sale.visibility = View.INVISIBLE
                }
            } else {
                val exploreAdapter =
                    ExploreAdapter(explorers, object : ExploreAdapter.ExploreClick {
                        override fun exploreClick(storeModel: StoreModel) {
                            storeClick.openStore(storeModel)
                        }
                    })
                val linearLayoutManager =
                    LinearLayoutManager(
                        storeItemBinding.root.context,
                        LinearLayoutManager.HORIZONTAL,
                        !language
                    )
                explorersContainerBinding.exploreRv.layoutManager = linearLayoutManager
                explorersContainerBinding.exploreRv.adapter = exploreAdapter
                if (language){
                    explorersContainerBinding.exploreTv.gravity = Gravity.START
                    explorersContainerBinding.exploreTv.text = "Explore"
                } else{
                    val typeface = ResourcesCompat.getFont(context, R.font.ge_dinar_one_medium)
                    explorersContainerBinding.exploreTv.typeface = typeface
                    explorersContainerBinding.exploreTv.gravity = Gravity.START
                    explorersContainerBinding.exploreTv.text = "اكتشف.."
                }
            }
        }
    }

    var run = object : Runnable {
        override fun run() {
            storeClick.openStore(storeModel1!!)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val storeView = StoreItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val exploreView =
            ExplorersContainerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        view = if (viewType == 0) {
            storeView.root
        } else {
            exploreView.root
        }
        return StoreViewHolder(storeView, exploreView)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position] != null) {
            0
        } else {
            1
        }
    }

    override fun getItemCount(): Int = list.size

    interface StoreClick {
        fun openStore(storeModel: StoreModel)

        fun saveStore(storeModel: StoreModel)

        fun saveClick(storeModel: StoreModel)
    }

}