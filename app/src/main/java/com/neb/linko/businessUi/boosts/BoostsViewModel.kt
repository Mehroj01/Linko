package com.neb.linko.businessUi.boosts

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.neb.linko.api.Datasets
import com.neb.linko.businessUi.businessData.BusinessViewModel
import com.neb.linko.models.*
import com.neb.linko.repository.BoostsRepository
import com.neb.linko.retrofit.ApiClient
import com.neb.linko.utils.NetworkHelper
import com.neb.linko.utils.Resource
import com.neb.linko.utils.Util
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class BoostsViewModel @Inject constructor(
    var db: FirebaseFirestore,
    var auth: FirebaseAuth,
    var businessViewModel: BusinessViewModel
) : ViewModel() {

    private val repository = BoostsRepository(ApiClient.apiService(Util.BASE_URL))
    var boostsLiveDate = MutableLiveData<ArrayList<BoostModel>?>()
    var userKey: String? = null
    private val boostsResult = MutableLiveData<Resource<BoostsResponse>>()
    var situation: Boolean? = null

    init {
        getUser()
    }

    fun getUser() {
        userKey = auth.uid
    }

    fun getBoosts(): LiveData<ArrayList<BoostModel>?> {
        loadBoosts()
        return boostsLiveDate
    }

    private fun loadBoosts() {
        db.collection(Datasets.BOOSTS.path).get()
            .addOnSuccessListener {
                val boosts = ArrayList<BoostModel>()
                it.documents.forEach { l ->
                    var boostModel: BoostModel? = null
                    try {
                        boostModel = l.toObject(BoostModel::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (boostModel != null) {
                        boosts.add(boostModel)
                    }
                }
                boostsLiveDate.postValue(boosts)
            }
            .addOnFailureListener {
                boostsLiveDate.postValue(null)
            }
    }

    fun sentBoostsRequest(
        context: Context,
        key: String,
        date: String,
        bannerBg: String,
        dates: ArrayList<String>,
        price: Int
    ): LiveData<Resource<BoostsResponse>> {

        val networkHelper = NetworkHelper(context)
        if (networkHelper.isNetworkConnected()) {

            viewModelScope.launch {

                boostsResult.postValue(Resource.loading(null))

                val boostsRequest = BoostsRequest(key, date, userKey, bannerBg, dates, price)
                val response = repository.sendBoostsRequest(boostsRequest)

                if (response.isSuccessful) {
                    boostsResult.postValue(Resource.success(response.body()))
                } else {
                    boostsResult.postValue(
                        Resource.error(
                            response.errorBody()?.string().toString(),
                            null
                        )
                    )
                }

            }

        } else {
            boostsResult.postValue(Resource.error("No connection", null))
        }

        return boostsResult

    }

    fun getBusyTimes(
        boostModel: BoostModel,
        top: BoostModel?,
        pin: BoostModel?,
        callback: (ArrayList<String>?) -> Unit
    ) {
        val timestamp = Date().time - ((60 * 60 * 24) * 1000)
        if (boostModel.type == 0) {
            db.collection(Datasets.BOOSTS.path)
                .document(boostModel.key ?: "").collection("data")
                .whereGreaterThanOrEqualTo("date", timestamp)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        callback(null)
                        return@addSnapshotListener
                    }

                    if (snapshot == null) {
                        callback(null)
                        return@addSnapshotListener
                    }

                    val formatter = SimpleDateFormat("dd/MM/yyyy")
                    val items: ArrayList<String> = ArrayList()

                    snapshot.documents.forEach { it ->
                        val data = it.data
                        data?.let {
                            val users = it["users"] as? ArrayList<String> ?: ArrayList()
                            val created = it["date"] as? Long ?: 0

                            if (users.size >= boostModel.limitPerDay ?: 1 || users.contains(userKey)) {
                                val date = Date()
                                date.time = (created)
                                items.add(formatter.format(date))
                            }
                        }
                    }
                    callback(items)
                }
        } else if (boostModel.type == 1) {
            db.collection(Datasets.BOOSTS.path)
                .document(pin?.key ?: "").collection("data")
                .whereGreaterThanOrEqualTo("date", timestamp).get()
                .addOnSuccessListener { pinSnapshot ->
                    val formatter = SimpleDateFormat("dd/MM/yyyy")
                    val items: ArrayList<String> = ArrayList()
                    pinSnapshot.documents.forEach { it ->
                        val data = it.data
                        data?.let {
                            val users = it["users"] as? ArrayList<String> ?: ArrayList()
                            val created = it["date"] as? Long ?: 0
                            if (users.size >= boostModel.limitPerDay ?: 1 || users.contains(userKey)) {
                                val date = Date()
                                date.time = (created)
                                items.add(formatter.format(date))
                            }
                        }
                    }
                    db.collection(Datasets.BOOSTS.path)
                        .document(top?.key ?: "").collection("data")
                        .whereGreaterThanOrEqualTo("date", timestamp).get()
                        .addOnSuccessListener { topSnapshot ->
                            topSnapshot.documents.forEach { it ->
                                val data = it.data
                                data?.let {
                                    val users = it["users"] as? ArrayList<String> ?: ArrayList()
                                    val created = it["date"] as? Long ?: 0
                                    if (users.contains(userKey)) {
                                        val date = Date()
                                        date.time = (created)
                                        items.add(formatter.format(date))
                                    }
                                }
                            }
                            callback(items)
                        }
                        .addOnFailureListener {
                            callback(null)
                        }
                }
                .addOnFailureListener {
                    callback(null)
                }
        } else if (boostModel.type == 2) {
            db.collection(Datasets.BOOSTS.path)
                .document(top?.key ?: "").collection("data")
                .whereGreaterThanOrEqualTo("date", timestamp).get()
                .addOnSuccessListener { pinSnapshot ->
                    val formatter = SimpleDateFormat("dd/MM/yyyy")
                    val items: ArrayList<String> = ArrayList()
                    pinSnapshot.documents.forEach { it ->
                        val data = it.data
                        data?.let {
                            val users = it["users"] as? ArrayList<String> ?: ArrayList()
                            val created = it["date"] as? Long ?: 0
                            if (users.size >= boostModel.limitPerDay ?: 1 || users.contains(userKey)) {
                                val date = Date()
                                date.time = (created)
                                items.add(formatter.format(date))
                            }
                        }
                    }
                    db.collection(Datasets.BOOSTS.path)
                        .document(pin?.key ?: "").collection("data")
                        .whereGreaterThanOrEqualTo("date", timestamp).get()
                        .addOnSuccessListener { topSnapshot ->
                            topSnapshot.documents.forEach { it ->
                                val data = it.data
                                data?.let {
                                    val users = it["users"] as? ArrayList<String> ?: ArrayList()
                                    val created = it["date"] as? Long ?: 0
                                    if (users.contains(userKey)) {
                                        val date = Date()
                                        date.time = (created)
                                        items.add(formatter.format(date))
                                    }
                                }
                            }
                            callback(items)
                        }
                        .addOnFailureListener {
                            callback(null)
                        }
                }
                .addOnFailureListener { callback(null) }
        }
    }

    fun getBoostTimes(
        boostModel: BoostModel,
        callback: (ArrayList<String>?) -> Unit
    ) {
//        val timestamp = Date().time - ((60 * 60 * 24) * 1000)
        db.collection(Datasets.BOOST_REGISTRATIONS.path)
            .whereEqualTo("subCategoryId", businessViewModel.myStore?.subCategoryId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    callback(null)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    callback(null)
                    return@addSnapshotListener
                }

                val boostRegistrations = ArrayList<BoostRegistrations>()
                snapshot.documents.forEach { l ->
                    var boostRegistration: BoostRegistrations? = null
                    try {
                        boostRegistration = l.toObject(BoostRegistrations::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (boostRegistration != null) {
                        if (boostModel.type == 0) {
                            if (boostRegistration.boost?.type == 0) {
                                boostRegistrations.add(boostRegistration)
                            }
                        } else if (boostModel.type == 1 || boostModel.type == 2) {
                            if (boostRegistration.boost?.type == 1 || boostRegistration.boost?.type == 2) {
                                boostRegistrations.add(boostRegistration)
                            }
                        }
                    }
                }

                val dates = getBoostDates(boostRegistrations)

                when (boostModel.type) {
                    0 -> {
                        callback(
                            detectBannerTimes(
                                boostRegistrations,
                                dates,
                                boostModel.limitPerDay
                            )
                        )
                    }
                    1 -> {
                        callback(
                            detectBoostTimes(
                                boostRegistrations,
                                1,
                                dates,
                                boostModel.limitPerDay
                            )
                        )
                    }
                    2 -> {
                        callback(
                            detectBoostTimes(
                                boostRegistrations,
                                2,
                                dates,
                                boostModel.limitPerDay
                            )
                        )
                    }
                    else -> callback(null)
                }
            }
    }

    private fun getBoostDates(boostRegistrations: ArrayList<BoostRegistrations>): List<BoostDate> {
        val boostDates = ArrayList<BoostDate>()
        for (boostRegistration in boostRegistrations) {
            boostRegistration.dates?.forEach {
                boostDates.add(BoostDate(ArrayList(), it))
            }
        }
        return boostDates.toSet().toList()
    }

    private fun detectBannerTimes(
        boostRegistrations: ArrayList<BoostRegistrations>,
        dates: List<BoostDate>,
        limit: Int?
    ): ArrayList<String> {
        for (boostRegistration in boostRegistrations) {
            boostRegistration.dates?.forEach {
                for (i in dates.indices) {
                    if (dates[i].date == it) {
                        dates[i].users?.add(boostRegistration.userId ?: "")
                    }
                }
            }
        }

        val items = ArrayList<String>()

        for (date in dates) {
            if ((date.users?.size ?: 0) >= (limit ?: 1) || date.users?.contains(userKey)!!) {
                items.add(date.date ?: "")
            }
        }

        return items
    }

    private fun detectBoostTimes(
        boostRegistrations: ArrayList<BoostRegistrations>,
        i: Int,
        dates: List<BoostDate>,
        limit: Int?
    ): ArrayList<String> {
        val pin = i
        val boost = if (pin == 1) 2 else 1

        val pinList = ArrayList<BoostRegistrations>()
        val boostList = ArrayList<BoostRegistrations>()

        val pinDates = ArrayList<BoostDate>()
        pinDates.addAll(dates)
        val boostDates = ArrayList<BoostDate>()
        boostDates.addAll(dates)

        val items = ArrayList<String>()

        for (j in 0 until boostRegistrations.size) {
            if (boostRegistrations[j].boost?.type == pin) {
                pinList.add(boostRegistrations[j])
            } else if (boostRegistrations[j].boost?.type == boost) {
                boostList.add(boostRegistrations[j])
            }
        }

        for (boostRegistration in pinList) {
            boostRegistration.dates?.forEach {
                for (j in 0 until pinDates.size) {
                    if (pinDates[j].date == it) {
                        pinDates[j].users?.add(boostRegistration.userId ?: "")
                    }
                }
            }
        }

        for (date in pinDates) {
            if ((date.users?.size ?: 0) >= (limit ?: 1) || date.users?.contains(userKey)!!) {
                items.add(date.date ?: "")
            }
        }

        for (boostRegistration in boostList) {
            if (boostRegistration.userId == userKey) {
                boostRegistration.dates?.forEach {
                    for (j in 0 until boostDates.size) {
                        if (boostDates[j].date == it) {
                            boostDates[j].users?.add(boostRegistration.userId ?: "")
                        }
                    }
                }
            }
        }

        for (date in boostDates) {
            if (date.users?.contains(userKey)!!) {
                items.add(date.date ?: "")
            }
        }

        return items

    }

    fun detect(id: Long, paymentId: Long, context: Context, callback: (Boolean) -> Unit) {
        val networkHelper = NetworkHelper(context)
        if (networkHelper.isNetworkConnected()) {
            viewModelScope.launch {
                boostsResult.postValue(Resource.loading(null))
                val response = repository.detect(paymentId, id)
                if (response.isSuccessful) {
                    callback(Resource.success(response.body()).data?.error ?: true)
                } else {
                    callback(Resource.success(response.body()).data?.error ?: true)
                }
            }
        } else {
            callback(true)
        }
    }
}

class BoostDate {
    var users: ArrayList<String>? = null
    var date: String? = null

    constructor()

    constructor(users: ArrayList<String>?, date: String?) {
        this.users = users
        this.date = date
    }

}