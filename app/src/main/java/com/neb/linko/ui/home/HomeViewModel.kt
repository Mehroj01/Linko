package com.neb.linko.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.neb.linko.api.Datasets
import com.neb.linko.models.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeViewModel @Inject constructor(var db: FirebaseFirestore) : ViewModel() {

    private var storesLiveDate = MutableLiveData<ArrayList<StoreModel>?>()
    var categoriesLiveData = MutableLiveData<ArrayList<CategoriesModel>?>()
    private var bannersLiveData = MutableLiveData<ArrayList<StoreModel>?>()
    private var trendingStores = MutableLiveData<ArrayList<StoreModel>?>()
    var pinList = ArrayList<String>()
    var boostsList = ArrayList<String>()
    val banners = ArrayList<StoreModel>()

    fun getStores(): LiveData<ArrayList<StoreModel>?> {
        return storesLiveDate
    }

    fun getTrending(): LiveData<ArrayList<StoreModel>?> {
        getSortBy() {
            if (it == null) {
                loadTrending(pinList, boostsList)
            }
        }
        return trendingStores
    }

    init {
        loadStores()
    }

    fun getSortBy(callback: (Exception?) -> Unit) {
        loadRegistrationBoosts { boosts ->
            loadUser { users ->
                boosts?.sortBy { it.boost?.type }
                pinList = ArrayList<String>()
                boostsList = ArrayList<String>()
                for (i in 0 until boosts?.size!!) {
                    if (boosts[i].boost?.type == 1) {
                        for (j in 0 until users?.size!!) {
                            if (users[j].key == boosts[i].userId) {
                                pinList.add(users[j].storeKey ?: "")
                            }
                        }
                    }
                    if (boosts[i].boost?.type == 2) {
                        for (j in 0 until users?.size!!) {
                            if (users[j].key == boosts[i].userId) {
                                boostsList.add(users[j].storeKey ?: "")
                            }
                        }
                    }
                }
                callback(null)
            }
        }
    }

    fun getCategories(): LiveData<ArrayList<CategoriesModel>?> {
        loadSubCategories() {
            loadCategories(it ?: ArrayList())
        }
        return categoriesLiveData
    }

    fun getBanners(): LiveData<ArrayList<StoreModel>?> {
        loadBanners()
        return bannersLiveData
    }

    private fun loadStores() {
        db.collection(Datasets.STORES.path)
            .addSnapshotListener() { snapshot, _ ->
                val stores = ArrayList<StoreModel>()
                snapshot?.documents?.forEach { l ->
                    var storeModel: StoreModel? = null
                    try {
                        storeModel = l.toObject(StoreModel::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (storeModel != null) {
                        if (storeModel.status != null) {
                            if (storeModel.status!!) {
                                if (storeModel.latestPromo != null) {
                                    if (storeModel.latestPromo?.claimsRequired ?: 0 <= storeModel.latestPromo?.claimsCount ?: 0 || (storeModel.latestPromo?.expirationTime != null && Date().time > storeModel.latestPromo?.expirationTime ?: 0)) {
                                        storeModel.latestPromo = null
                                    }
                                }
                                stores.add(storeModel)
                            }
                        }
                    }
                }
                val list = ArrayList<StoreModel>()
                list.addAll(stores.toSet().toList())
                storesLiveDate.postValue(list)
            }

    }

    private fun loadCategories(subcategoryList: ArrayList<SubCategoryModel>) {
        val categoriesList = ArrayList<CategoriesModel>()
        db.collection(Datasets.CATEGORIES.path)
            .orderBy("sortOrder", Query.Direction.ASCENDING).get()
            .addOnSuccessListener { it ->
                it.documents.forEach { l ->
                    var categories: CategoriesModel? = null
                    try {
                        categories = l.toObject(CategoriesModel::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    for (j in 0 until (categories?.subCategories?.size ?: 0)) {
                        for (i in 0 until subcategoryList.size) {
                            if (categories?.subCategories?.get(j)?.key == subcategoryList[i].key) {
                                categories?.subCategories?.set(j, subcategoryList[i])
                                break
                            }
                        }
                    }
                    if (categories != null) categoriesList.add(categories)
                }
                categoriesLiveData.postValue(categoriesList)
            }
            .addOnFailureListener {
                categoriesLiveData.postValue(null)
            }
    }

    private fun loadBanners() {
        if (banners.isEmpty()) {
            db.collection(Datasets.BANNERS.path).get()
                .addOnSuccessListener {

                    it.documents.forEach { l ->
                        var bannerModel: StoreModel? = null
                        try {
                            bannerModel = l.toObject(StoreModel::class.java)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        if (bannerModel != null) {
                            if (bannerModel.status == true) {
                                if ((bannerModel.endDate != null && bannerModel.endDate!! > Date().time) && (bannerModel.startDate != null && bannerModel.startDate!! <= Date().time)) {
                                    banners.add(bannerModel)
                                }
                            }
                        }
                    }
                    bannersLiveData.postValue(banners)
                }
                .addOnFailureListener {
                    bannersLiveData.postValue(null)
                }
        }
    }

    private fun loadTrending(pinnedList: ArrayList<String>, boostsList: ArrayList<String>) {
        db.collection(Datasets.STORES.path).whereEqualTo("isTrending", true).get()
            .addOnSuccessListener {
                val stores = ArrayList<StoreModel>()
                it.documents.forEach { l ->
                    var storeModel: StoreModel? = null
                    try {
                        storeModel = l.toObject(StoreModel::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (storeModel != null) {
                        if (storeModel.status != null) {
                            if (storeModel.status!!) {
                                if (storeModel.latestPromo != null) {
                                    if (storeModel.latestPromo?.claimsRequired ?: 0 <= storeModel.latestPromo?.claimsCount ?: 0 || (storeModel.latestPromo?.expirationTime != null && Date().time > storeModel.latestPromo?.expirationTime ?: 0)) {
                                        storeModel.latestPromo = null
                                    }
                                }
                                stores.add(storeModel)
                            }
                        }
                    }
                }
                var sortedList = ArrayList<StoreModel>()
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

                var storePromo = ArrayList<StoreModel>()
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

                trendingStores.postValue(list)
            }
            .addOnFailureListener {
                trendingStores.postValue(null)
            }
    }

    fun loadRegistrationBoosts(callback: (ArrayList<BoostRegistrations>?) -> Unit) {
        db.collection(Datasets.BOOST_REGISTRATIONS.path).orderBy("creationDate").get()
            .addOnSuccessListener {
                val boostsRegistrations = ArrayList<BoostRegistrations>()
                it.documents.forEach { l ->
                    var boostsRegister: BoostRegistrations? = null
                    try {
                        boostsRegister = l.toObject(BoostRegistrations::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (boostsRegister != null) {
                        if (boostsRegister.boost != null) {
                            if (boostsRegister.boost?.status != null) {
                                if (boostsRegister.boost?.status!!) {
                                    var time = boostsRegister.date?.split(" - ")
                                    var format = SimpleDateFormat("dd/MM/yyyy")
                                    var date1 = time?.first()?.let { d ->
                                        format.parse(d)
                                    }
                                    var date2 = time?.last()?.let { d ->
                                        format.parse(d)
                                    }
                                    var startDate = date1?.time!!
                                    var endDate = date2?.time!! + 1000 * 60 * 60 * 24
                                    if (Date().time in (startDate + 1) until endDate) {
                                        boostsRegistrations.add(boostsRegister)
                                    }
                                }
                            }
                        }
                    }
                }
                callback(boostsRegistrations)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    private fun loadUser(callback: (ArrayList<User>?) -> Unit) {
        db.collection(Datasets.USERS.path).get()
            .addOnSuccessListener {
                val users = ArrayList<User>()
                it.forEach { l ->
                    var user: User? = null
                    try {
                        user = l.toObject(User::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (user != null) users.add(user)
                }
                callback(users)
            }
            .addOnFailureListener {
                callback(null)
            }
    }

    private fun loadSubCategories(callback: (ArrayList<SubCategoryModel>?) -> Unit) {
        db.collection(Datasets.SUBCATEGORIES.path).get()
            .addOnSuccessListener {
                val categoriesList = ArrayList<SubCategoryModel>()
                it.documents.forEach { l ->
                    var subCategoryModel: SubCategoryModel? = null
                    try {
                        subCategoryModel = l.toObject(SubCategoryModel::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (subCategoryModel != null) categoriesList.add(subCategoryModel)
                }
                callback(categoriesList)
            }
            .addOnFailureListener {
                callback(null)
            }
    }
}
