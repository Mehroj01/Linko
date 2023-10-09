package com.neb.linko.businessUi.storePromoCodes

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.neb.linko.api.Datasets
import com.neb.linko.businessUi.businessData.BusinessViewModel
import com.neb.linko.models.PromoModel
import com.neb.linko.models.StoreModel
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.set

@Singleton
class PromoCodesViewModel @Inject constructor(
    var db: FirebaseFirestore,
    var businessViewModel: BusinessViewModel
) : ViewModel() {

    var promoLiveDate = MutableLiveData<ArrayList<PromoModel>?>()
    var store: StoreModel? = null

    fun getMyPromos(owner: LifecycleOwner): LiveData<ArrayList<PromoModel>?> {
        getStoreKey(owner) {
            if (it != null) {
                store = it
                if (store?.latestPromo != null) {
                    loadPromos(it.key ?: "", store?.latestPromo!!)
                }
            } else {
                promoLiveDate.postValue(null)
            }
        }
        return promoLiveDate
    }

    private fun loadPromos(s: String, storePromoModel: PromoModel) {
        db.collection(Datasets.PROMOS.path).whereEqualTo("storeKey", s)
            .orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener {
                val promos = ArrayList<PromoModel>()
                promos.add(storePromoModel)
                it.documents.forEach { l ->
                    var promoModel: PromoModel? = null
                    try {
                        promoModel = l.toObject(PromoModel::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (promoModel != null && promoModel.key != storePromoModel.key) {
                        promos.add(promoModel)
                    }
                }
                promoLiveDate.postValue(promos)
            }
            .addOnFailureListener {
                promoLiveDate.postValue(null)
            }
    }

    private fun getStoreKey(owner: LifecycleOwner, callback: (store: StoreModel?) -> Unit) {
        businessViewModel.getMyStore(owner).observe(owner, Observer {
            if (it != null) {
                callback(it)
            } else {
                callback(null)
            }
        })
    }

    fun createPromoCode(
        promoDetails: String,
        promoCode: String,
        expiry: Int,
        percentage: Int,
        claimsRequired: Int,
        callback: (result: Boolean?) -> Unit?
    ) {
        if (store != null) {
            val batch = db.batch()
            val ref = db.collection(Datasets.STORES.path).document(store?.key ?: "")
            val refList = db.collection(Datasets.PROMOS.path).document()
            val updateStore = HashMap<String, Any>()

            var expirationTime = Date().time + (expiry * 1000 * 60 * 60 * 24)

            val promoDict = mapOf(
                "claims" to null,
                "promoDetails" to promoDetails,
                "percentage" to percentage,
                "key" to refList.id,
                "status" to true,
                "expiry" to expiry,
                "storeKey" to store?.key,
                "promoCode" to promoCode,
                "timestamp" to FieldValue.serverTimestamp(),
                "expirationTime" to expirationTime,
                "creationDate" to Date().time,
                "claimsRequired" to claimsRequired,
                "claimsCount" to 0
            )

            updateStore["latestPromo"] = promoDict

            batch.set(refList, promoDict)
            batch.update(ref, updateStore as Map<String, Any>)
            batch.commit().addOnSuccessListener {
                callback(true)
                return@addOnSuccessListener
            }.addOnFailureListener {
                callback(null)
                return@addOnFailureListener
            }
        }
    }

    fun edit(
        newPromoModel: PromoModel?,
        callback: (result: Boolean?) -> Unit?
    ) {
        if (store != null) {
            val batch = db.batch()
            val ref = db.collection(Datasets.STORES.path).document(store?.key ?: "")
            val updateStore = HashMap<String, PromoModel?>()
            updateStore["latestPromo"] = newPromoModel

            batch.update(ref, updateStore as Map<String, PromoModel?>)
            batch.commit().addOnSuccessListener {
                callback(true)
                return@addOnSuccessListener
            }.addOnFailureListener {
                callback(null)
                return@addOnFailureListener
            }
        }
    }

}