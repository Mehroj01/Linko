package com.neb.linko.ui.save

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.neb.linko.App
import com.neb.linko.BottomNavigationClick
import com.neb.linko.R
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.FragmentSaveBinding
import com.neb.linko.databinding.SaveTopLayoutForArBinding
import com.neb.linko.databinding.SaveTopLayoutForEnBinding
import com.neb.linko.models.StoreModel
import com.neb.linko.models.User
import com.neb.linko.ui.category.adapters.ExploreAdapter
import com.neb.linko.ui.profile.ProfileViewModel
import com.neb.linko.ui.store.StoreActivity
import javax.inject.Inject

class SaveFragment : Fragment() {

    @Inject
    lateinit var profileViewModel: ProfileViewModel

    @Inject
    lateinit var saveViewModel: SaveViewModel

    @Inject
    lateinit var addFavoriteStoreViewModel: AddFavoriteStoreViewModel

    lateinit var saveBinding: FragmentSaveBinding

    lateinit var bookmarks: ArrayList<StoreModel>
    lateinit var stores: ArrayList<StoreModel>
    lateinit var bookmarkAdapter: BookmarkAdapter
    lateinit var favorites: ArrayList<String>
    var uid: String = ""
    lateinit var user: User
    var filterSituation = false
    lateinit var exploreAdapter: ExploreAdapter
    lateinit var languageCache: LanguageCache
    lateinit var exploresList: ArrayList<StoreModel?>
    var filterBtn: ImageView? = null
    var storesCountsTv: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        saveBinding = FragmentSaveBinding.inflate(inflater, container, false)
        App.appComponent.save(this)

        favorites = ArrayList()

        bookmarks = ArrayList()

        stores = ArrayList()

        exploresList = ArrayList()

        getFavorites()

        languageCache =
            LanguageCache(activity?.getSharedPreferences("Base", Context.MODE_PRIVATE)!!)


        saveViewModel.getExplorersStores().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                exploresList.clear()
                exploresList.addAll(it.sortedBy { l ->
                    l.savedSortOrder
                })
                exploreAdapter.notifyDataSetChanged()
            }
        })

        exploreAdapter = ExploreAdapter(exploresList, object : ExploreAdapter.ExploreClick {
            override fun exploreClick(storeModel: StoreModel) {
                showStore(storeModel)
            }
        })
        saveBinding.exploreRv.adapter = exploreAdapter

        //Navigation Back Button Click
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                bottomNavigationClick?.saveClick()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        bookmarkAdapter = BookmarkAdapter(
            languageCache.getLanguage(),
            stores,
            object : BookmarkAdapter.BookmarksClick {
                override fun openStore(storeModel: StoreModel) {
                    showStore(storeModel)
                }

                override fun saveStore(storeModel: StoreModel) {
                    saveBinding.progress.visibility = View.VISIBLE
                    val a = favorites
                    a.remove(storeModel.key)
                    val b = user
                    b.bookmarks = a
                    if (uid != "") {
                        addFavoriteStoreViewModel.getUpdate(uid, user)
                            .observe(viewLifecycleOwner, Observer {
                                saveBinding.progress.visibility = View.INVISIBLE
                                if (it) {
                                    favorites.remove(storeModel.key)
                                    bookmarks.remove(storeModel)
                                    stores.remove(storeModel)
                                    user = b
                                    storesCountsTv?.text =
                                        if (languageCache.getLanguage()) "${bookmarks.size} Stores" else " متاجر${bookmarks.size}"
                                    bookmarkAdapter.notifyDataSetChanged()
                                }
                            })
                    }
                }
            })

        saveBinding.rv.adapter = bookmarkAdapter

        return saveBinding.root
    }

    private fun showStore(storeModel: StoreModel) {
        val intent = Intent(activity?.applicationContext, StoreActivity::class.java)
        val b = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
        try {
            intent.putExtra("store", Gson().toJson(storeModel))
            startActivity(intent, b)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "store information is incomplete", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun filterStores() {
        if (filterSituation) {
            filterBtn?.setImageResource(R.drawable.ic_percentage)
        } else {
            filterBtn?.setImageResource(R.drawable.ic_percent_unchecked)
        }
        stores.clear()
        if (filterSituation) {
            for (bookmark in bookmarks) {
                if (bookmark.latestPromo != null) stores.add(bookmark)
            }
        } else {
            stores.addAll(bookmarks)
        }
        storesCountsTv?.text =
            if (languageCache.getLanguage()) "${stores.size} Store" else " متاجر${stores.size}"
        bookmarkAdapter.notifyDataSetChanged()
    }

    private fun getFavorites() {
        profileViewModel.getAuth().observe(viewLifecycleOwner, Observer { auth ->
            if (auth?.let { true } == true) {
                uid = auth.uid
                profileViewModel.getUser(auth.uid).observe(viewLifecycleOwner, Observer {
                    if (it?.bookmarks != null) {
                        if (it.bookmarks?.isNotEmpty()!!) {
                            user = it
                            favorites.clear()
                            favorites.addAll(it.bookmarks!!)
                            getStores()
                        }
                    }
                })
            }
        })
    }

    private fun getStores() {
        saveViewModel.getStores(favorites, viewLifecycleOwner)
            .observe(viewLifecycleOwner, Observer {
                if (it != null) {
                    bookmarks.clear()
                    bookmarks.addAll(it)
                    bookmarks.reverse()
                    if (filterBtn != null && storesCountsTv != null) {
                        filterStores()
                    }
                }
            })
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
            val topLayout = SaveTopLayoutForEnBinding.inflate(LayoutInflater.from(context))
            filterBtn = topLayout.filtersBtn
            storesCountsTv = topLayout.countStores
            topLayout.root.layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            saveBinding.container.removeAllViews()
            saveBinding.container.addView(topLayout.root)

        } else {
            val topLayout = SaveTopLayoutForArBinding.inflate(LayoutInflater.from(context))
            filterBtn = topLayout.filtersBtn
            storesCountsTv = topLayout.countStores
            topLayout.root.layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            saveBinding.container.removeAllViews()
            saveBinding.container.addView(topLayout.root)

            val typeface =
                ResourcesCompat.getFont(activity?.applicationContext!!, R.font.ge_dinar_one_medium)
            topLayout.savedText.typeface = typeface
            topLayout.countStores.typeface = typeface
            topLayout.tt.typeface = typeface
            topLayout.bt.typeface = typeface
        }

        filterStores()

        filterBtn?.setOnClickListener {
            filterSituation = !filterSituation
            filterStores()
        }

        val linearLayoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                !languageCache.getLanguage()
            )
        saveBinding.exploreRv.layoutManager = linearLayoutManager

        exploreAdapter.notifyDataSetChanged()
        bookmarkAdapter.notifyDataSetChanged()
    }
}