package com.neb.linko.businessUi.analytics

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.neb.linko.api.Datasets
import com.neb.linko.models.StatsModel
import com.neb.linko.models.StoreModel
import com.neb.linko.models.User
import com.neb.linko.ui.profile.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsViewModel @Inject constructor(
    var db: FirebaseFirestore,
    var profileViewModel: ProfileViewModel,
    var auth: FirebaseAuth
) : ViewModel() {

    var stateLiveDate = MutableLiveData<StatsModel?>()
    private var bannersLiveData = MutableLiveData<ArrayList<StoreModel>?>()
    var user: User? = null

    private val YESTERDAY: Date
        get() {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, -1)
            return cal.time
        }

    fun getState(filter: String, owner: LifecycleOwner): LiveData<StatsModel?> {
        if (user != null) {
            updateFilter(filter, user!!)
        } else {
            profileViewModel.getUser(auth.currentUser?.uid ?: "")
                .observe(owner, androidx.lifecycle.Observer {
                    if (it != null) {
                        user = it
                        updateFilter(filter, user!!)
                    }
                })
        }
        return stateLiveDate
    }

    fun getBanners(): LiveData<ArrayList<StoreModel>?> {
        loadBanners()
        return bannersLiveData
    }

    private fun updateFilter(filter: String, user: User) {

        val formatter: SimpleDateFormat
        var statRef: Query? = null

        when (filter) {
            "Today" -> {
                formatter = SimpleDateFormat("ddMMyyyy")
                statRef = db.collection(
                    "/statistics/stores/daily/${user.storeKey}/linko_${
                        formatter.format(
                            Date()
                        )
                    }/"
                )
            }
            "Yesterday" -> {
                formatter = SimpleDateFormat("ddMMyyyy")
                statRef = db.collection(
                    "/statistics/stores/daily/${user.storeKey}/linko_${
                        formatter.format(YESTERDAY)
                    }/"
                )
            }
            "This Month" -> {
                formatter = SimpleDateFormat("MMyyyy")
                statRef = db.collection(
                    "/statistics/stores/monthly/${user.storeKey}/linko_${
                        formatter.format(
                            Date()
                        )
                    }/"
                )
            }
            "This Year" -> {
                formatter = SimpleDateFormat("yyyy")
                statRef = db.collection(
                    "/statistics/stores/yearly/${user.storeKey}/linko_${
                        formatter.format(
                            Date()
                        )
                    }/"
                )
            }
            "Overall" -> {
                db.collection("/statistics/stores/overall")
                    .document(user.storeKey!!)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null) {
                            var statsModel: StatsModel? = null
                            try {
                                statsModel = snapshot.toObject(StatsModel::class.java)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            stateLiveDate.postValue(statsModel)
                        } else {
                            stateLiveDate.postValue(null)
                        }
                    }
                return
            }
        }

        statRef?.addSnapshotListener { snapshot, _ ->

            val baseStatsModel = StatsModel()
            try {
                val statsList = snapshot?.toObjects(StatsModel::class.java)
                statsList?.forEach { stat ->
                    baseStatsModel.views += stat.views
                    baseStatsModel.bookmarkClicks += stat.bookmarkClicks
                    baseStatsModel.whatsappClicks += stat.whatsappClicks
                    baseStatsModel.locationClicks += stat.locationClicks
                    baseStatsModel.websiteClicks += stat.websiteClicks
                    baseStatsModel.phoneClicks += stat.phoneClicks
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            stateLiveDate.postValue(baseStatsModel)
        }
    }

    private fun loadBanners() {
        val banners = ArrayList<StoreModel>()
        db.collection(Datasets.ANALYTIC_BANNERS.path).whereEqualTo("status", true).get()
            .addOnSuccessListener {

                it.documents.forEach { l ->
                    var bannerModel: StoreModel? = null
                    try {
                        bannerModel = l.toObject(StoreModel::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (bannerModel != null) {
//                        if (bannerModel.status == true) {
//
//                            if (bannerModel.endDate != null && bannerModel.endDate!! > Date().time) {
//                                banners.add(bannerModel)
//                            } else if (bannerModel.endDate == null) {
//                                banners.add(bannerModel)
//                            }
//                        } else if (bannerModel.status == null) {
//                            banners.add(bannerModel)
//                        }
                        banners.add(bannerModel)
                    }
                }
                bannersLiveData.postValue(banners)
            }
            .addOnFailureListener {
                bannersLiveData.postValue(null)
            }
    }

}