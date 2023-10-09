package com.neb.linko.ui.save

import androidx.lifecycle.*
import com.google.firebase.firestore.FirebaseFirestore
import com.neb.linko.App
import com.neb.linko.api.Datasets
import com.neb.linko.models.StoreModel
import com.neb.linko.ui.home.HomeViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveViewModel @Inject constructor(var db: FirebaseFirestore) : ViewModel() {

    @Inject
    lateinit var homeViewModel: HomeViewModel

    var storesLiveDate = MutableLiveData<ArrayList<StoreModel>?>()

    var savedExplorersLiveDate = MediatorLiveData<ArrayList<StoreModel>?>()

    init {
        App.appComponent.saveViewModel(this)
    }

    fun getExplorersStores(): LiveData<ArrayList<StoreModel>?> {
        getSavedExplorers()
        return savedExplorersLiveDate
    }

    fun getStores(
        list: ArrayList<String>,
        viewLifecycleOwner: LifecycleOwner
    ): LiveData<ArrayList<StoreModel>?> {
        loadStores(list, viewLifecycleOwner)
        return storesLiveDate
    }

    private fun loadStores(list: ArrayList<String>, viewLifecycleOwner: LifecycleOwner) {
        homeViewModel.getStores().observe(viewLifecycleOwner, Observer {
            if (it != null && it.isNotEmpty()) {
                val stores = ArrayList<StoreModel>()
                for (s in list) {
                    for (j in it) {
                        if (s == j.key) {
                            stores.add(j)
                        }
                    }
                }
                storesLiveDate.postValue(stores)
            } else {
                storesLiveDate.postValue(null)
            }
        })
    }

    private fun getSavedExplorers() {
        db.collection(Datasets.STORES.path)
            .whereEqualTo("shouldSave", true).get()
            .addOnSuccessListener {
                val savedExplorers = ArrayList<StoreModel>()
                it?.documents?.forEach { l ->
                    var storeModel: StoreModel? = null
                    try {
                        storeModel = l.toObject(StoreModel::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (storeModel != null) {
                        savedExplorers.add(storeModel)
                    }
                }
                savedExplorersLiveDate.postValue(savedExplorers)
            }
            .addOnFailureListener {
                savedExplorersLiveDate.postValue(null)
            }
    }
}