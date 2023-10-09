package com.neb.linko.ui.home.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.neb.linko.R
import com.neb.linko.databinding.CategoryItemBinding
import com.neb.linko.models.CategoriesModel
import info.androidhive.fontawesome.FontDrawable


class CategoryAdapter(
    var categoriesList: ArrayList<CategoriesModel>,
    var context: Context,
    var language: Boolean,
    var categoryClick: CategoryClick
) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(var categoryItemBinding: CategoryItemBinding) :
        RecyclerView.ViewHolder(categoryItemBinding.root) {
        fun onBind(categories: CategoriesModel, position: Int) {
            if (position == 0) {
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(if (language) 37 else 12, 10, if (language) 12 else 37, 10)
                categoryItemBinding.categoriesContainer.layoutParams = params
            }else if (categoriesList.size-1 == position){
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(if (language) 12 else 37, 10,if (language) 37 else 12, 10)
                categoryItemBinding.categoriesContainer.layoutParams = params
            }else{
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(12, 10, 12, 10)
                categoryItemBinding.categoriesContainer.layoutParams = params
            }
            categoryItemBinding.tv.text =
                if (language) categories.name ?: "" else categories.nameArabic ?: ""

            if (categories.name == "Trending") {
                categoryItemBinding.img.setImageResource(R.drawable.trending)
                categoryItemBinding.categoriesContainer.setBackgroundResource(R.drawable.category_item_bg)
            } else {
                var resourceId =
                    getResourceByName(context, "${categories.icon}_solid".replace("-", "_"))
                if (resourceId <= 0) {
                    resourceId = getResourceByName(context, "fa_info_solid".replace("-", "_"))
                }

                val icon = FontDrawable(context, resourceId, true, false)
                icon.setTextColor(Color.BLACK)
                categoryItemBinding.img.setImageDrawable(icon)
                categoryItemBinding.categoriesContainer.setBackgroundResource(R.drawable.category_default_item_bg)
            }

            categoryItemBinding.categoriesContainer.setOnClickListener {
                categoryClick.click(categories)
            }

        }
    }

    fun getResourceByName(mContext: Context, name: String?): Int {
        name?.let {
            val resources = mContext.resources
            return resources.getIdentifier(
                name, "string",
                mContext.packageName
            )
        } ?: run {
            return 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            CategoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.onBind(categoriesList[position], position)
    }

    override fun getItemCount(): Int = categoriesList.size

    interface CategoryClick {
        fun click(categoriesModel: CategoriesModel)
    }

}