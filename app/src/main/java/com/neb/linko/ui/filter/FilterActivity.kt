package com.neb.linko.ui.filter

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.neb.linko.R
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.ActivityFilterBinding
import com.neb.linko.models.CategoriesModel
import com.neb.linko.ui.filter.adapters.FilterAdapter
import com.neb.linko.ui.registerscreens.experience.models.SelectItem

class FilterActivity : AppCompatActivity() {

    lateinit var filterBinding: ActivityFilterBinding
    var category: CategoriesModel? = null
    lateinit var filterAdapter: FilterAdapter
    lateinit var brands: FilterAdapter
    lateinit var filterList: ArrayList<SelectItem>
    lateinit var brandList: ArrayList<SelectItem>
    var f = false
    var b = false
    var rating = 0
    lateinit var languageCache: LanguageCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        filterBinding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(filterBinding.root)

        filterBinding.backBtn.setOnClickListener {
            finish()
        }

        languageCache =
            LanguageCache(getSharedPreferences("Base", AppCompatActivity.MODE_PRIVATE)!!)

        changeLanguage()

        filterBinding.cancelBtn.setOnClickListener {
            if (f) {
                for (i in 0 until filterList.size) {
                    filterList[i] = SelectItem(filterList[i].str, false)
                }
                filterAdapter.notifyDataSetChanged()
            }
            if (b) {
                for (i in 0 until brandList.size) {
                    brandList[i] = SelectItem(brandList[i].str, false)
                }
                brands.notifyDataSetChanged()
            }
            clearRatings()
        }

        try {
            category = Gson().fromJson(intent.getStringExtra("cat"), CategoriesModel::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (category != null) {

            if (category?.filters != null && category?.filters?.isNotEmpty()!!) {
                filterList = ArrayList()
                f = true
                val fil =
                    if (languageCache.getLanguage()) category?.filters else category?.filtersArabic
                fil?.forEach { l ->
                    filterList.add(SelectItem(l, false))
                }
                filterAdapter = FilterAdapter(filterList, object : FilterAdapter.FilterClick {
                    override fun click(selectItem: SelectItem) {
                        if (selectItem.isSelect!!) {
                            filterList[filterList.indexOf(selectItem)] =
                                SelectItem(selectItem.str, false)
                        } else {
                            filterList[filterList.indexOf(selectItem)] =
                                SelectItem(selectItem.str, true)
                        }
                        filterAdapter.notifyDataSetChanged()
                    }
                }, resources)
                filterBinding.filtersRv.adapter = filterAdapter
            } else {
                filterBinding.containers.removeView(filterBinding.filterContainer)
            }

            if (category?.brands != null && category?.brands?.isNotEmpty()!!) {
                brandList = ArrayList()
                b = true
                val bra =
                    if (languageCache.getLanguage()) category?.brands else category?.brandArabic
                bra?.forEach { l ->
                    brandList.add(SelectItem(l, false))
                }
                brands = FilterAdapter(brandList, object : FilterAdapter.FilterClick {
                    override fun click(selectItem: SelectItem) {
                        if (selectItem.isSelect!!) {
                            brandList[brandList.indexOf(selectItem)] =
                                SelectItem(selectItem.str, false)
                        } else {
                            brandList[brandList.indexOf(selectItem)] =
                                SelectItem(selectItem.str, true)
                        }
                        brands.notifyDataSetChanged()
                    }
                }, resources)
                filterBinding.brandsRv.adapter = brands
            } else {
                filterBinding.containers.removeView(filterBinding.brandsContainer)
            }

        }

        filterBinding.showResult.setOnClickListener {
            val intent = Intent(this, ShowFilterStoresActivity::class.java)
            if (f) {
                val fList = ArrayList<String>()
                for (selectItem in filterList) {
                    if (selectItem.isSelect != null && selectItem.isSelect!!) {
                        fList.add(selectItem.str!!)
                    }
                }
                intent.putExtra("filterList", fList)
            }
            if (b) {
                val bList = ArrayList<String>()
                for (brandItem in brandList) {
                    if (brandItem.isSelect != null && brandItem.isSelect!!) {
                        bList.add(brandItem.str!!)
                    }
                }
                intent.putExtra("brandList", bList)
            }
            intent.putExtra("rating", rating)
            try {
                intent.putExtra("cat", Gson().toJson(category))
                if (category != null) startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this,
                    "category information is incomplete",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        filterBinding.apply {
            rateOne.setOnClickListener {
                if (rating == 1) {
                    clearRatings()
                    it.setBackgroundResource(R.drawable.rate_bg)
                } else {
                    clearRatings()
                    rating = 1
                    it.setBackgroundResource(R.drawable.rate_bg_selected)
                }
            }
            rateTwo.setOnClickListener {
                if (rating == 2) {
                    clearRatings()
                    it.setBackgroundResource(R.drawable.rate_bg)
                } else {
                    clearRatings()
                    rating = 2
                    it.setBackgroundResource(R.drawable.rate_bg_selected)
                }
            }
            rateThree.setOnClickListener {
                if (rating == 3) {
                    clearRatings()
                    it.setBackgroundResource(R.drawable.rate_bg)
                } else {
                    clearRatings()
                    rating = 3
                    it.setBackgroundResource(R.drawable.rate_bg_selected)
                }
            }
            rateFour.setOnClickListener {
                if (rating == 4) {
                    clearRatings()
                    it.setBackgroundResource(R.drawable.rate_bg)
                } else {
                    clearRatings()
                    rating = 4
                    it.setBackgroundResource(R.drawable.rate_bg_selected)
                }
            }
            rateFive.setOnClickListener {
                if (rating == 5) {
                    clearRatings()
                    it.setBackgroundResource(R.drawable.rate_bg)
                } else {
                    clearRatings()
                    rating = 5
                    it.setBackgroundResource(R.drawable.rate_bg_selected)
                }
            }
        }
    }

    private fun changeLanguage() {
        val gridLayoutManager1 =
            GridLayoutManager(
                this,
                3,
                GridLayoutManager.VERTICAL,
                !languageCache.getLanguage()
            )
        filterBinding.filtersRv.layoutManager = gridLayoutManager1

        val gridLayoutManager2 =
            GridLayoutManager(
                this,
                3,
                GridLayoutManager.VERTICAL,
                !languageCache.getLanguage()
            )
        filterBinding.brandsRv.layoutManager = gridLayoutManager2

        if (languageCache.getLanguage()) {
            filterBinding.filtersTv.text = "Filter"
            filterBinding.brandsTv.text = "Brand"
            filterBinding.ratingText.text = "Rating"
            filterBinding.showResult.text = "Show results"
            filterBinding.title.text = "Filter"
            filterBinding.ratingLayout.gravity = Gravity.START
        } else {
            filterBinding.filtersTv.text = "تصنيف"
            filterBinding.brandsTv.text = "العلامة التجارية"
            filterBinding.ratingText.text = "التقييم"
            filterBinding.showResult.text = "عرض النتائج"
            filterBinding.title.text = "تصنيف"
            filterBinding.ratingLayout.gravity = Gravity.END
            setTextFonts()
        }
    }

    private fun clearRatings() {
        rating = 0
        filterBinding.apply {
            rateOne.setBackgroundResource(R.drawable.rate_bg)
            rateTwo.setBackgroundResource(R.drawable.rate_bg)
            rateThree.setBackgroundResource(R.drawable.rate_bg)
            rateFour.setBackgroundResource(R.drawable.rate_bg)
            rateFive.setBackgroundResource(R.drawable.rate_bg)
        }
    }

    override fun onResume() {
        super.onResume()
        filterBinding.cancelBtn.text = if (languageCache.getLanguage()) "Cancel" else "إلغاء"
        if (!languageCache.getLanguage()) setTextFonts()
    }

    private fun setTextFonts() {
        val typeface = ResourcesCompat.getFont(this, R.font.ge_dinar_one_medium)
        filterBinding.cancelBtn.typeface = typeface
        filterBinding.filtersTv.typeface = typeface
        filterBinding.brandsTv.typeface = typeface
        filterBinding.ratingText.typeface = typeface
        filterBinding.showResult.typeface = typeface
        filterBinding.title.typeface = typeface
    }
}