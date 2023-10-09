package com.neb.linko.ui.home

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.neb.linko.App
import com.neb.linko.BottomNavigationClick
import com.neb.linko.R
import com.neb.linko.businessUi.boosts.BoostsActivity
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.FragmentHomeBinding
import com.neb.linko.models.CategoriesModel
import com.neb.linko.models.StoreModel
import com.neb.linko.models.User
import com.neb.linko.ui.home.adapters.CarouselAdapter
import com.neb.linko.ui.home.adapters.CategoryAdapter
import com.neb.linko.ui.home.adapters.StoreAdapter
import com.neb.linko.ui.profile.ProfileViewModel
import com.neb.linko.ui.save.AddFavoriteStoreViewModel
import com.neb.linko.ui.search.SearchActivity
import com.neb.linko.ui.store.StoreActivity
import com.neb.linko.utils.NetworkHelper
import javax.inject.Inject


class HomeFragment : Fragment() {
    @Inject
    lateinit var homeViewModel: HomeViewModel

    @Inject
    lateinit var profileViewModel: ProfileViewModel

    @Inject
    lateinit var addFavoriteStoreViewModel: AddFavoriteStoreViewModel

    lateinit var homeBinding: FragmentHomeBinding
    lateinit var carouselBannerList: ArrayList<StoreModel>
    lateinit var handler: Handler
    lateinit var categoriesList: ArrayList<CategoriesModel>
    lateinit var categoryAdapter: CategoryAdapter
    lateinit var stores: ArrayList<StoreModel?>
    lateinit var storeAdapter: StoreAdapter
    lateinit var carouselAdapter: CarouselAdapter
    lateinit var favorites: ArrayList<String>
    var user: User? = null
    var country: String = "KWT"
    var uid: String = ""
    lateinit var explorers: ArrayList<StoreModel?>
    var bannerCarouselSituation = false
    lateinit var networkHelper: NetworkHelper
    lateinit var languageCache: LanguageCache
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        App.appComponent.home(this)
        carouselBannerList = ArrayList()
        categoriesList = ArrayList()
        stores = ArrayList()
        favorites = ArrayList()
        explorers = ArrayList()
        sharedPreferences = activity?.getSharedPreferences("Base", Context.MODE_PRIVATE)!!
        country = sharedPreferences.getString("country", "KWT")!!
        languageCache = LanguageCache(sharedPreferences)

        networkHelper = NetworkHelper(activity?.applicationContext!!)
        if (!networkHelper.isNetworkConnected()) {
            Toast.makeText(context, "Internet disconnected!!!", Toast.LENGTH_SHORT).show()
        }

        homeBinding.shimmerLayout.startShimmerAnimation()

//        homeBinding.catTitle.setOnClickListener {
//            var intent = Intent(activity?.applicationContext, BoostsActivity::class.java)
//            startActivity(intent)
//        }

        //Navigation Back Button Click
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                bottomNavigationClick?.homeClick()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        homeBinding.searchBox.setOnClickListener {
            val intent = Intent(activity?.applicationContext, SearchActivity::class.java)
            intent.putExtra("country", country)
            startActivity(intent)
        }

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

        homeViewModel.getTrending().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                stores.clear()
                it.forEach { s ->
                    if (s.country == country) {
                        stores.add(s)
                    }
                }
                getExplorers()
                storeAdapter.notifyDataSetChanged()
                stopHomeShimmer()
            }
        })

        homeViewModel.getCategories().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                categoriesList.clear()
                it.forEach { c ->
                    if (country == c.country) {
                        categoriesList.add(c)
                    }
                }
                categoryAdapter.notifyDataSetChanged()
            }
        })

        carouselAdapter = CarouselAdapter(carouselBannerList, childFragmentManager)
        homeBinding.imageSlider.adapter = carouselAdapter

        homeBinding.tabs.setupWithViewPager(homeBinding.imageSlider)

        categoryAdapter = CategoryAdapter(
            categoriesList,
            activity?.applicationContext!!,
            languageCache.getLanguage(),
            object : CategoryAdapter.CategoryClick {
                override fun click(categoriesModel: CategoriesModel) {
                    if (categoriesModel.name != "trending" && categoriesModel.name != "Trending") {
                        val bundle = Bundle()
                        bundle.putSerializable("category", categoriesModel)
                        findNavController().navigate(R.id.categoryFragment, bundle)
                    }
                }
            })

        homeBinding.catListView.adapter = categoryAdapter

        storeAdapter =
            StoreAdapter(
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
                        if (s && uid != "" && user != null) {
                            homeBinding.progress.visibility = View.VISIBLE
                            val a = favorites
                            a.add(storeModel.key!!)
                            val b = user
                            b?.bookmarks = a
                            addFavoriteStoreViewModel.addUpdate(uid, user!!)
                                .observe(viewLifecycleOwner, Observer {
                                    homeBinding.progress.visibility = View.INVISIBLE
                                    if (it) {
//                                favorites.add(storeModel.key!!)
                                        user = b
                                        storeAdapter.notifyDataSetChanged()
                                    }
                                })
                        }
                    }

                    override fun saveClick(storeModel: StoreModel) {
                        homeBinding.progress.visibility = View.VISIBLE
                        val a = favorites
                        a.remove(storeModel.key)
                        val b = user
                        b?.bookmarks = a
                        if (uid != "" && user != null) {
                            addFavoriteStoreViewModel.getUpdate(uid, user!!)
                                .observe(viewLifecycleOwner, Observer {
                                    homeBinding.progress.visibility = View.INVISIBLE
                                    if (it) {
//                                        favorites.remove(storeModel.key)
                                        user = b
                                        storeAdapter.notifyDataSetChanged()
                                    }
                                })
                        }
                    }
                }, activity?.applicationContext!!
            )

        homeBinding.listView.adapter = storeAdapter

        return homeBinding.root
    }

    private fun stopHomeShimmer() {
        homeBinding.shimmerLayout.stopShimmerAnimation()
        homeBinding.shimmerLayout.visibility = View.GONE
    }

    private fun getExplorers() {
        var j = 0
        explorers.clear()
        while (true) {
            if (j == stores.size) break
            if (stores[j] != null) {
                if (stores[j]?.showAsExplore != null) {
                    if (stores[j]?.showAsExplore!!) {
                        explorers.add(stores[j]!!)
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


    var run = object : Runnable {
        override fun run() {
            if (carouselBannerList.size - 1 != homeBinding.imageSlider.currentItem) {
                homeBinding.imageSlider.currentItem = homeBinding.imageSlider.currentItem + 1
            } else {
                homeBinding.imageSlider.currentItem = 0
            }
            handler.postDelayed(this, 5000)
        }
    }

    fun dpToPx(dp: Int, mContext: Context): Int {
        val displayMetrics: DisplayMetrics = mContext.resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
            .toInt()
    }

    var bottomNavigationClick: BottomNavigationClick? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationClick) {
            bottomNavigationClick = context as BottomNavigationClick
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bottomNavigationClick = null
    }

    override fun onResume() {
        super.onResume()
        languageCache =
            LanguageCache(activity?.getSharedPreferences("Base", Context.MODE_PRIVATE)!!)
        if (languageCache.getLanguage()) {
            homeBinding.searchBox.textDirection = View.TEXT_DIRECTION_LTR
            homeBinding.searchBox.hint = "Search for anything …"

            val params = homeBinding.catTitle.layoutParams as LinearLayout.LayoutParams
            params.gravity = Gravity.START
            homeBinding.catTitle.layoutParams = params
            homeBinding.catTitle.text = "Categories"
        } else {
            homeBinding.searchBox.textDirection = View.TEXT_DIRECTION_RTL
            homeBinding.searchBox.hint = "ابحث عن أي شيء .. "

            val params = homeBinding.catTitle.layoutParams as LinearLayout.LayoutParams
            params.gravity = Gravity.END
            homeBinding.catTitle.layoutParams = params
            homeBinding.catTitle.text = "الأقسام"
            setTextFonts()
        }

        val linearLayoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                !languageCache.getLanguage()
            )
        homeBinding.catListView.layoutManager = linearLayoutManager

        categoryAdapter.notifyDataSetChanged()
        storeAdapter.notifyDataSetChanged()

        homeViewModel.getBanners().observe(viewLifecycleOwner, Observer {
            val countryBanners = ArrayList<StoreModel>()
            it?.forEach { l ->
                if (l.country == country) {
                    countryBanners.add(l)
                }
            }
            if (countryBanners != null && countryBanners.size > 0) {
                carouselBannerList.clear()
                carouselBannerList.addAll(countryBanners)
                carouselAdapter.notifyDataSetChanged()
                if (!bannerCarouselSituation) {
                    handler = Handler()
                    handler.postDelayed(run, 5000)
                    bannerCarouselSituation = true
                }
            } else {
                carouselBannerList.clear()
                homeBinding.container.removeView(homeBinding.imageSlider)
                homeBinding.container.removeView(homeBinding.tabs)
//                val params = homeBinding.mainToolBar.layoutParams
//                params.height = dpToPx(120, requireContext())
//                homeBinding.mainToolBar.layoutParams = params
            }
        })
    }

    private fun setTextFonts() {
        val typeface =
            ResourcesCompat.getFont(activity?.applicationContext!!, R.font.ge_dinar_one_medium)
        homeBinding.searchBox.typeface = typeface
        homeBinding.catTitle.typeface = typeface
    }

}