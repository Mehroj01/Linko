package com.neb.linko.ui.search

import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import com.neb.linko.App
import com.neb.linko.api.Datasets
import com.neb.linko.models.ItemModel
import com.neb.linko.models.StoreModel
import com.neb.linko.ui.filter.Rating
import com.neb.linko.ui.home.HomeViewModel
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    var homeViewModel: HomeViewModel,
    var db: FirebaseFirestore
) : ViewModel() {

    var storesLiveDate = MutableLiveData<ArrayList<StoreModel>?>()
    var itemsLiveDate = MutableLiveData<ArrayList<ItemModel>?>()

    init {
        App.appComponent.searchViewModel(this)
        loadItems()
    }

    fun getStores(viewLifecycleOwner: LifecycleOwner): LiveData<ArrayList<StoreModel>?> {
        loadStores(viewLifecycleOwner)
        return storesLiveDate
    }

    fun getRating(stores: ArrayList<StoreModel>) {
        getRatings {
            storeSorts(it, stores)
        }
    }

    private fun storeSorts(
        ratings: ArrayList<Rating>?,
        storeModel: ArrayList<StoreModel>
    ) {
        if (ratings != null) {
            for (i in 0 until storeModel.size) {
                for (j in 0 until ratings.size) {
                    if (storeModel[i].key == ratings[j].key) {
                        storeModel[i].rating = setRating(ratings[j].number)
                    }
                }
            }
        }
        storesLiveDate.postValue(storeModel)
    }

    private fun setRating(number: ArrayList<Float>?): Float {
        var r = 0f
        for (i in 0 until number?.size!!) {
            r += number[i]
        }
        r /= number.size
        return r
    }

    fun getItems(): LiveData<ArrayList<ItemModel>?> {
        return itemsLiveDate
    }

    private fun loadStores(viewLifecycleOwner: LifecycleOwner) {
        homeViewModel.getStores().observe(viewLifecycleOwner, Observer {
            storesLiveDate.postValue(it)
            if (it != null) {
                getRating(it)
            }
        })
    }

    private fun loadItems() {
        db.collection(Datasets.ITEMS.path).get()
            .addOnSuccessListener {
                val items = ArrayList<ItemModel>()
                it.documents.forEach { l ->
                    var itemModel: ItemModel? = null
                    try {
                        itemModel = l.toObject(ItemModel::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (itemModel != null) {
                        items.add(itemModel)
                    }
                }
                itemsLiveDate.postValue(items)
            }

            .addOnFailureListener {
                itemsLiveDate.postValue(null)
            }
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

}