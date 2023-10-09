package com.neb.linko.ui.category

import android.app.ActivityOptions
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.neb.linko.App
import com.neb.linko.R
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.FragmentCategoryBinding
import com.neb.linko.models.CategoriesModel
import com.neb.linko.models.StoreModel
import com.neb.linko.models.SubCategoryModel
import com.neb.linko.models.User
import com.neb.linko.ui.filter.FilterActivity
import com.neb.linko.ui.home.adapters.StoreAdapter
import com.neb.linko.ui.profile.ProfileViewModel
import com.neb.linko.ui.save.AddFavoriteStoreViewModel
import com.neb.linko.ui.search.SearchActivity
import com.neb.linko.ui.store.StoreActivity
import javax.inject.Inject

class CategoryFragment : Fragment() {

    @Inject
    lateinit var categoryViewModel: CategoryViewModel

    @Inject
    lateinit var profileViewModel: ProfileViewModel

    @Inject
    lateinit var addFavoriteStoreViewModel: AddFavoriteStoreViewModel

    lateinit var categoryBinding: FragmentCategoryBinding
    var category: CategoriesModel? = null
    lateinit var cat: ArrayList<SubCategoryModel?>
    lateinit var languageCache: LanguageCache
    lateinit var infoDialog: InfoDialog
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    var index = 0
    lateinit var stores: ArrayList<StoreModel?>
    lateinit var favorites: ArrayList<String>
    lateinit var explorers: ArrayList<StoreModel?>
    var uid: String = ""
    lateinit var user: User
    lateinit var storeAdapter: StoreAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        categoryBinding = FragmentCategoryBinding.inflate(inflater, container, false)
        App.appComponent.categoryFragment(this)

        languageCache =
            LanguageCache(activity?.getSharedPreferences("Base", MODE_PRIVATE)!!)

        categoryBinding.shimmerLayout.startShimmerAnimation()
        sharedPreferences = activity?.getSharedPreferences("Base", MODE_PRIVATE)!!
        editor = sharedPreferences.edit()
        if (sharedPreferences.getBoolean("info_dialog", true)) {
            showDialog()
        }

        try {
            category = arguments?.getSerializable("category") as CategoriesModel
        } catch (e: Exception) {
            e.printStackTrace()
        }

        categoryBinding.searchBox.setOnClickListener {
            val intent = Intent(activity?.applicationContext, SearchActivity::class.java)
            startActivity(intent)
        }

        categoryBinding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        getFavorites()

        if (category != null) {
            categoryBinding.tabLayout.removeAllTabs()
            setTitleCategories(category!!)

            stores = ArrayList()
            favorites = ArrayList()
            explorers = ArrayList()

            categoryBinding.titleText.text =
                if (languageCache.getLanguage()) category?.name else category?.nameArabic

            categoryViewModel.getStores(null, category!!, this)
                .observe(viewLifecycleOwner, Observer {
                    if (it != null) {
                        stores.clear()
                        stores.addAll(it)
                        getExplorers()
                        categoryBinding.noRecordView.visibility = View.INVISIBLE
                        storeAdapter.notifyDataSetChanged()
                        categoryBinding.countText.text =
                            if (languageCache.getLanguage()) "${it.size} Store" else "${it.size} متجر "
                        if (it.isEmpty()) categoryBinding.noRecordView.visibility = View.VISIBLE
                    } else {
                        categoryBinding.noRecordView.visibility = View.VISIBLE
                    }
                    categoryBinding.progress.visibility = View.INVISIBLE
                    stopHomeShimmer()
                })
        }

        storeAdapter = StoreAdapter(
            languageCache.getLanguage(),
            stores,
            favorites,
            explorers,
            object : StoreAdapter.StoreClick {
                override fun openStore(storeModel: StoreModel) {
                    val intent = Intent(activity?.applicationContext, StoreActivity::class.java)
                    val b = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
                    try {
                        intent.putExtra("store", Gson().toJson(storeModel))
                        startActivity(intent, b)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            context,
                            "store information is incomplete",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }

                override fun saveStore(storeModel: StoreModel) {
                    var s = true
                    for (favorite in favorites) {
                        if (favorite == storeModel.key) {
                            s = false
                            break
                        }
                    }
                    if (s && uid != "") {
                        categoryBinding.progress.visibility = View.VISIBLE
                        val a = favorites
                        a.add(storeModel.key!!)
                        val b = user
                        b.bookmarks = a
                        addFavoriteStoreViewModel.addUpdate(uid, user)
                            .observe(viewLifecycleOwner, Observer {
                                categoryBinding.progress.visibility = View.INVISIBLE
                                if (it) {
                                    user = b
                                    storeAdapter.notifyDataSetChanged()
                                }
                            })
                    }
                }

                override fun saveClick(storeModel: StoreModel) {
                    categoryBinding.progress.visibility = View.VISIBLE
                    val a = favorites
                    a.remove(storeModel.key)
                    val b = user
                    b.bookmarks = a
                    if (uid != "") {
                        addFavoriteStoreViewModel.getUpdate(uid, user)
                            .observe(viewLifecycleOwner, Observer {
                                categoryBinding.progress.visibility = View.INVISIBLE
                                if (it) {
                                    favorites.remove(storeModel.key)
                                    user = b
                                    storeAdapter.notifyDataSetChanged()
                                }
                            })
                    }
                }
            }, activity?.applicationContext!!
        )

        categoryBinding.rv.adapter = storeAdapter

        categoryBinding.filtersBtn.setOnClickListener {
            val intent = Intent(activity?.applicationContext, FilterActivity::class.java)
            val b = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
            try {
                intent.putExtra("cat", Gson().toJson(category))
                if (category != null) startActivity(intent, b)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    context,
                    "category information is incomplete",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        return categoryBinding.root
    }

    private fun getFavorites() {
        profileViewModel.getAuth().observe(viewLifecycleOwner, Observer { auth ->
            if (auth?.let { true } == true) {
                uid = auth.uid
                profileViewModel.getUser(auth.uid).observe(viewLifecycleOwner, Observer {
                    if (it != null) user = it
                    if (it?.bookmarks != null) {
                        if (it.bookmarks?.isNotEmpty()!!) {
                            favorites.clear()
                            favorites.addAll(it.bookmarks!!)
                            storeAdapter.notifyDataSetChanged()
                        }
                    }
                })
            }
        })
    }

    private fun setTitleCategories(categoryModel: CategoriesModel) {
        val language = languageCache.getLanguage()
        val allTab = categoryBinding.tabLayout.newTab()
        allTab.text = if (language) "All" else "الكل"
        allTab.tag = -1
        categoryBinding.tabLayout.addTab(allTab)
        categoryModel.subCategories?.sortBy { c -> c.sortOrder }
        for (subCategory in categoryModel.subCategories ?: ArrayList()) {
            val tab = categoryBinding.tabLayout.newTab()
            tab.text = if (language) subCategory.name else subCategory.nameArabic
            categoryBinding.tabLayout.addTab(tab)
            tab.tag = index
            index += 1
        }

        listenTab()

    }

    private fun listenTab() {
        categoryBinding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {

            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                categoryBinding.countText.text =
                    if (languageCache.getLanguage()) "0 Store" else "0 متجر "
                if (tab!!.tag == -1) {
                    categoryViewModel.getStores(null, category!!,this@CategoryFragment)
                } else {
                    categoryViewModel.getStores(
                        category?.subCategories?.get(tab.tag as Int),
                        category!!, this@CategoryFragment
                    )
                }
                categoryBinding.progress.visibility = View.VISIBLE
                categoryBinding.noRecordView.visibility = View.INVISIBLE
                stores.clear()
                storeAdapter.notifyDataSetChanged()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        changeLanguage()
    }

    private fun changeLanguage() {
        if (languageCache.getLanguage()) {
            categoryBinding.searchBox.textDirection = View.TEXT_DIRECTION_LTR
            categoryBinding.searchBox.hint = "Search for anything …"

        } else {
            categoryBinding.searchBox.textDirection = View.TEXT_DIRECTION_RTL
            categoryBinding.searchBox.hint = "ابحث عن أي شيء .."
            setTextFonts()
        }
    }

    private fun setTextFonts() {
        val typeface =
            ResourcesCompat.getFont(activity?.applicationContext!!, R.font.ge_dinar_one_medium)
        categoryBinding.searchBox.typeface = typeface
        categoryBinding.countText.typeface = typeface
    }

    private fun stopHomeShimmer() {
        categoryBinding.shimmerLayout.stopShimmerAnimation()
        categoryBinding.shimmerLayout.visibility = View.GONE
    }

    private fun showDialog() {
        infoDialog = InfoDialog(categoryBinding.root.context, object : InfoDialog.InfoClick {
            override fun skip() {
                infoDialog.dialogs.onBackPressed()
            }
        }, languageCache.getLanguage())
        infoDialog.networkDialog()
        editor.putBoolean("info_dialog", false)
        editor.commit()
    }

    private fun getExplorers() {
        explorers.clear()
        var j = 0
        while (true) {
            if (j == stores.size) break
            if (stores[j] != null) {
                if (stores[j]?.showAsExplore != null) {
                    if (stores[j]?.showAsExplore!!) {
                        explorers.add(stores[j])
                        stores.remove(stores[j])
                        j--
                    }
                }
            }
            j++
        }

        if (explorers.isNotEmpty()) {
            val a = ArrayList<StoreModel?>()
            for (i in 0 until stores.size) {
                if (i == 3) a.add(null)
                a.add(stores[i])
            }
            stores.clear()
            stores.addAll(a)
            explorers.sortedBy { l -> l?.savedSortOrder }
        }
    }
}