package com.neb.linko.ui.category

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.neb.linko.models.CategoriesModel
import com.neb.linko.models.StoreModel
import com.neb.linko.models.SubCategoryModel
import com.neb.linko.ui.home.HomeViewModel
import javax.inject.Inject

class CategoryViewModel @Inject constructor(
    var db: FirebaseFirestore,
    var homeViewModel: HomeViewModel
) : ViewModel() {
    private var storesLiveDate = MutableLiveData<ArrayList<StoreModel>?>()

    fun getStores(
        subCategoryModel: SubCategoryModel?,
        categoriesModel: CategoriesModel,
        owner: LifecycleOwner
    ): LiveData<ArrayList<StoreModel>?> {
        loadStores(
            subCategoryModel,
            categoriesModel,
            homeViewModel.pinList,
            homeViewModel.boostsList,
            owner
        )
        return storesLiveDate
    }

    private fun loadStores(
        subCategoryModel: SubCategoryModel?,
        categoriesModel: CategoriesModel,
        pinnedList: ArrayList<String>,
        boostsList: ArrayList<String>,
        owner: LifecycleOwner
    ) {
        homeViewModel.getStores().observe(owner) {
            if (it != null) {
                val filterList = ArrayList<StoreModel>()
                if (subCategoryModel == null) {
                    filterList.addAll(it.filter { l -> l.categoryId == categoriesModel.key })
                } else {
                    filterList.addAll(it.filter { l -> l.categoryId == categoriesModel.key && l.subCategoryId == subCategoryModel.key })
                }
                val stores = ArrayList<StoreModel>()
                filterList.forEach { l ->
                    val storeModel = l
                    if (storeModel.status != null) {
                        if (storeModel.status!!) {
                            if (storeModel.latestPromo != null) {
                                if (storeModel.latestPromo?.claimsRequired ?: 0 <= storeModel.latestPromo?.claimsCount ?: 0) {
                                    storeModel.latestPromo = null
                                }
                            }
                            stores.add(storeModel)
                        }
                    }

                }
                val sortedList = ArrayList<StoreModel>()

                for (i in 0 until pinnedList.size) {
                    stores.removeAll { s ->
                        if (pinnedList[i] == s.key) {
                            sortedList.add(s)
                            true
                        } else {
                            false
                        }
                    }
                }

                for (i in 0 until boostsList.size) {
                    stores.removeAll { s ->
                        if (boostsList[i] == s.key) {
                            sortedList.add(s)
                            true
                        } else {
                            false
                        }
                    }
                }

                val storePromo = ArrayList<StoreModel>()
                stores.forEach { l ->
                    if (l.latestPromo != null) {
                        storePromo.add(l)
                    }
                }
                storePromo.sortBy { l -> l.latestPromo?.percentage }
                storePromo.reverse()
                sortedList.addAll(storePromo)

                stores.forEach { l ->
                    if (l.latestPromo == null) {
                        sortedList.add(l)
                    }
                }
                val list = ArrayList<StoreModel>()
                list.addAll(sortedList.toSet().toList())
                storesLiveDate.postValue(list)
            } else {
                storesLiveDate.postValue(null)
            }
        }
    }
}