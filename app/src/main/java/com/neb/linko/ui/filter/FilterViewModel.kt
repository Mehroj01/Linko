package com.neb.linko.ui.filter

import androidx.lifecycle.*
import com.neb.linko.App
import com.neb.linko.models.CategoriesModel
import com.neb.linko.models.StoreModel
import com.neb.linko.models.SubCategoryModel
import com.neb.linko.ui.category.CategoryViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterViewModel @Inject constructor(var categoryViewModel: CategoryViewModel) : ViewModel() {

    init {
        App.appComponent.showFilterViewModel(this)
    }

    var filteredLiveDate = MutableLiveData<ArrayList<StoreModel>?>()

    fun getFiltered(
        subCategoryModel: SubCategoryModel?,
        categoriesModel: CategoriesModel,
        filterList: ArrayList<String>,
        brandList: ArrayList<String>,
        viewLifecycleOwner: LifecycleOwner
    ): LiveData<ArrayList<StoreModel>?> {
        loadStores(subCategoryModel, categoriesModel, filterList, brandList, viewLifecycleOwner)
        return filteredLiveDate
    }

    private fun loadStores(
        subCategoryModel: SubCategoryModel?,
        categoriesModel: CategoriesModel,
        filterList: ArrayList<String>,
        brandList: ArrayList<String>,
        viewLifecycleOwner: LifecycleOwner
    ) {
        categoryViewModel.getStores(subCategoryModel, categoriesModel,viewLifecycleOwner)
            .observe(viewLifecycleOwner, Observer {
                if (it != null && it.isNotEmpty()) {
                    val stores = ArrayList<StoreModel>()
                    if (filterList.isNotEmpty()) {
                        it.forEach { l ->
                            val store = sortFilter(l, filterList)
                            if (store != null) stores.add(store)
                        }
                    } else {
                        stores.addAll(it)
                    }
                    filteredLiveDate.postValue(sortBrands(stores, brandList))
                } else {
                    filteredLiveDate.postValue(null)
                }
            })
    }

    private fun sortBrands(
        stores: ArrayList<StoreModel>,
        brandList: ArrayList<String>
    ): ArrayList<StoreModel> {
        if (brandList.isNotEmpty()) {

            stores.removeAll { s ->
                val arabicBrand = s.brandArabic
                val usBrand = s.brands
                !sorting(arabicBrand ?: ArrayList(), usBrand ?: ArrayList(), brandList)
            }
        }
        return stores
    }

    private fun sorting(
        arabicBrand: ArrayList<String>,
        usBrand: ArrayList<String>,
        brandList: ArrayList<String>
    ): Boolean {
        if (arabicBrand.isNotEmpty()) {
            for (i in 0 until brandList.size) {
                for (j in 0 until arabicBrand.size) {
                    if (brandList[i] == arabicBrand[j]) {
                        return true
                    }
                }
            }
        }

        if (usBrand.isNotEmpty()) {
            for (i in 0 until brandList.size) {
                for (j in 0 until usBrand.size) {
                    if (brandList[i] == usBrand[j]) {
                        return true
                    }
                }
            }
        }

        return false
    }

    private fun sortFilter(
        storeModel: StoreModel,
        filterList: ArrayList<String>
    ): StoreModel? {
        val arabicFilter = storeModel.filtersArabic ?: ArrayList()
        val enFilter = storeModel.filters ?: ArrayList()

        for (i in 0 until arabicFilter.size) {
            for (j in 0 until filterList.size) {
                if (arabicFilter[i] == filterList[j]) {
                    return storeModel
                }
            }
        }

        for (i in 0 until enFilter.size) {
            for (j in 0 until filterList.size) {
                if (enFilter[i] == filterList[j]) {
                    return storeModel
                }
            }
        }

        return null

    }

}