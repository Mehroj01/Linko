package com.neb.linko.ui.filter

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.neb.linko.App
import com.neb.linko.api.Datasets
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.ActivityShowFilterStoresBinding
import com.neb.linko.models.CategoriesModel
import com.neb.linko.models.StoreModel
import com.neb.linko.models.User
import com.neb.linko.ui.home.adapters.StoreAdapter
import com.neb.linko.ui.profile.ProfileViewModel
import com.neb.linko.ui.save.AddFavoriteStoreViewModel
import com.neb.linko.ui.store.StoreActivity
import javax.inject.Inject

class ShowFilterStoresActivity : AppCompatActivity() {

    @Inject
    lateinit var db: FirebaseFirestore

    @Inject
    lateinit var filterViewModel: FilterViewModel

    @Inject
    lateinit var profileViewModel: ProfileViewModel

    @Inject
    lateinit var addFavoriteStoreViewModel: AddFavoriteStoreViewModel

    lateinit var showFilterStoresBinding: ActivityShowFilterStoresBinding

    lateinit var storeAdapter: StoreAdapter

    var category: CategoriesModel? = null
    lateinit var user: User
    var uid: String = ""
    lateinit var favorites: ArrayList<String>
    lateinit var explorers: ArrayList<StoreModel?>
    lateinit var stores: ArrayList<StoreModel?>
    lateinit var languageCache: LanguageCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showFilterStoresBinding = ActivityShowFilterStoresBinding.inflate(layoutInflater)
        setContentView(showFilterStoresBinding.root)
        App.appComponent.showFilterActivity(this)
        favorites = ArrayList()
        explorers = ArrayList()
        stores = ArrayList()

        languageCache =
            LanguageCache(getSharedPreferences("Base", AppCompatActivity.MODE_PRIVATE)!!)

        try {
            category = Gson().fromJson(intent.getStringExtra("cat"), CategoriesModel::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        showFilterStoresBinding.backBtn.setOnClickListener {
            finish()
        }

        val filterList = intent.getStringArrayListExtra("filterList") ?: ArrayList<String>()
        val brandList = intent.getStringArrayListExtra("brandList") ?: ArrayList<String>()
        val rating = intent.getIntExtra("rating", 0)

        setAdapterDate()

        if (category != null) {
            showFilterStoresBinding.progress.visibility = View.VISIBLE
            filterViewModel.getFiltered(
                null,
                category!!,
                filterList as ArrayList<String>,
                brandList as ArrayList<String>,
                this
            ).observe(this, Observer {
                if (it != null) {
                    getRatings { r ->
                        storeSorts(r, rating, it)
                    }
                }
            })
        }

        profileViewModel.getAuth().observe(this, Observer { auth ->
            if (auth?.let { true } == true) {
                uid = auth.uid
                profileViewModel.getUser(auth.uid).observe(this, Observer {
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

    private fun getRatings(callback: (ArrayList<Rating>?) -> Unit) {
        db.collection(Datasets.RATINGS.path).get()
            .addOnSuccessListener {
                val ratings = ArrayList<Rating>()
                it?.documents?.forEach { l ->
                    val list = ArrayList<Float>()
                    l.data?.values?.forEach { j ->
                        var rating: Float? = null
                        try {
                            rating = j.toString().toFloat()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        if (rating != null) {
                            list.add(rating)
                        }
                    }
                    if (list.isNotEmpty()) {
                        ratings.add(Rating(list, l.id))
                    }
                }
                callback(ratings)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    private fun storeSorts(
        ratings: ArrayList<Rating>?,
        rating: Int,
        storeModel: ArrayList<StoreModel>
    ) {
        if (ratings != null) {
            for (i in 0 until storeModel.size) {
                var s: StoreModel? = null
                for (j in 0 until ratings.size) {
                    if (storeModel[i].key == ratings[j].key) {
                        storeModel[i].rating = setRating(ratings[j].number)
                        s = storeModel[i]
                    }
                }
//                if (s != null) storeModel.remove(s)
            }
        }

        storeModel

        if (rating != 0) {
            var i = 0
            while (true) {
                if (i == storeModel.size) break
                if (storeModel[i].rating < rating) {
                    storeModel.remove(storeModel[i])
                    i--
                }
                i++
            }
        }
        showFilterStoresBinding.progress.visibility = View.INVISIBLE
        stores.clear()
        storeModel.forEach { l ->
            if (l.latestPromo != null) {
                stores.add(l)
            }
        }

        storeModel.forEach { l ->
            if (l.latestPromo == null) {
                stores.add(l)
            }
        }
        if (stores.isEmpty()) showFilterStoresBinding.noRecordView.visibility = View.VISIBLE else showFilterStoresBinding.noRecordView.visibility = View.INVISIBLE
        getExplorers()
        storeAdapter.notifyDataSetChanged()
    }

    private fun setAdapterDate() {
        storeAdapter = StoreAdapter(languageCache.getLanguage(),stores, favorites, explorers, object : StoreAdapter.StoreClick {
            override fun openStore(storeModel: StoreModel) {
                val intent = Intent(this@ShowFilterStoresActivity, StoreActivity::class.java)
                val b = ActivityOptions.makeSceneTransitionAnimation(this@ShowFilterStoresActivity).toBundle()
                try {
                    intent.putExtra("store", Gson().toJson(storeModel))
                    startActivity(intent, b)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@ShowFilterStoresActivity, "store information is incomplete", Toast.LENGTH_SHORT)
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
                    showFilterStoresBinding.progress.visibility = View.VISIBLE
                    val a = favorites
                    a.add(storeModel.key!!)
                    val b = user
                    b.bookmarks = a
                    addFavoriteStoreViewModel.addUpdate(uid, user)
                        .observe(this@ShowFilterStoresActivity, Observer {
                            showFilterStoresBinding.progress.visibility = View.INVISIBLE
                            if (it) {
//                                favorites.add(storeModel.key!!)
                                user = b
                                storeAdapter.notifyDataSetChanged()
                            }
                        })
                }
            }

            override fun saveClick(storeModel: StoreModel) {
                showFilterStoresBinding.progress.visibility = View.VISIBLE
                val a = favorites
                a.remove(storeModel.key)
                val b = user
                b.bookmarks = a
                if (uid != "") {
                    addFavoriteStoreViewModel.getUpdate(uid, user)
                        .observe(this@ShowFilterStoresActivity, Observer {
                            showFilterStoresBinding.progress.visibility = View.INVISIBLE
                            if (it) {
                                favorites.remove(storeModel.key)
                                user = b
                                storeAdapter.notifyDataSetChanged()
                            }
                        })
                }
            }
        },this)

        showFilterStoresBinding.filterRv.adapter = storeAdapter
    }

    private fun setRating(number: ArrayList<Float>?): Float {
        var r = 0f
        for (i in 0 until number?.size!!) {
            r += number[i]
        }
        r /= number.size
        return r
    }

    private fun getExplorers() {
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

    override fun onResume() {
        super.onResume()
        showFilterStoresBinding.title.text = if (languageCache.getLanguage()) "Filter" else "تصنيف"
    }

}

class Rating {
    var number: ArrayList<Float>? = null
    var key: String? = null

    constructor()

    constructor(number: ArrayList<Float>?, key: String?) {
        this.number = number
        this.key = key
    }

}