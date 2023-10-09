package com.neb.linko.businessUi.storeItems

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.neb.linko.api.Datasets
import com.neb.linko.businessUi.businessData.BusinessViewModel
import com.neb.linko.models.ItemModel
import com.neb.linko.models.StoreModel
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.set

@Singleton
class ItemsViewModel @Inject constructor(
    var db: FirebaseFirestore,
    var businessViewModel: BusinessViewModel
) : ViewModel() {

    var myItemsLiveData = MutableLiveData<ArrayList<ItemModel>?>()
    var store: StoreModel? = null

    fun getMyItems(owner: LifecycleOwner): LiveData<ArrayList<ItemModel>?> {
        getStoreKey(owner) {
            if (it != null) {
                store = it
                loadMyItems(store?.key ?: "")
            }
        }
        return myItemsLiveData
    }

    private fun loadMyItems(key: String) {
        db.collection(Datasets.ITEMS.path)
            .whereEqualTo("storeKey", key)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
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
                myItemsLiveData.postValue(items)
            }
            .addOnFailureListener {
                myItemsLiveData.postValue(null)
            }
    }

    fun addItem(
        enName: String,
        details: String,
        nameArabic: String,
        detailsArabic: String,
        imgUri: String,
        price: Float,
        callback: (result: Boolean?) -> Unit?
    ) {
        if (store != null) {
            val batch = db.batch()
            val ref = db.collection(Datasets.ITEMS.path).document()

            val item = ItemModel()
            item.key = ref.id
            item.name = enName
            item.price = price
            item.storeKey = store?.key
            item.status = true
            item.details = details
            item.currency = "KWD"
            item.nameArabic = nameArabic
            item.detailsArabic = detailsArabic
            item.imageUrl = imgUri

            val promoDict = mapOf(
                "key" to item.key,
                "name" to item.name,
                "price" to item.price,
                "storeKey" to item.storeKey,
                "status" to item.status,
                "details" to item.details,
                "currency" to "KWD",
                "country" to store?.country,
                "timestamp" to Date().time,
                "nameArabic" to item.nameArabic,
                "detailsArabic" to item.detailsArabic,
                "imageUrl" to item.imageUrl,
                "store" to mapOf(
                    "key" to store?.key,
                    "name" to store?.name,
                    "nameArabic" to store?.nameArabic
                )
            )

            batch.set(ref, promoDict)
            batch.commit().addOnSuccessListener {
                callback(true)
                return@addOnSuccessListener
            }.addOnFailureListener {
                callback(null)
                return@addOnFailureListener
            }
        } else {
            callback(null)
        }
    }

    fun editItem(
        key: String,
        enName: String,
        details: String,
        nameArabic: String,
        detailsArabic: String,
        imgUri: String,
        price: Float,
        callback: (result: Boolean?) -> Unit?
    ) {
        if (store != null) {
            val updateItem = HashMap<String, Any?>()
            updateItem["name"] = enName
            updateItem["price"] = price
            updateItem["details"] = details
            updateItem["nameArabic"] = nameArabic
            updateItem["detailsArabic"] = detailsArabic
            updateItem["imageUrl"] = imgUri

            FirebaseFirestore.getInstance().collection(Datasets.ITEMS.path)
                .document(key)
                .update(updateItem as Map<String, Any>)
                .addOnSuccessListener {
                    callback(true)
                    return@addOnSuccessListener
                }
                .addOnFailureListener {
                    callback(null)
                    return@addOnFailureListener
                }
        } else {
            callback(null)
        }
    }

    fun deleteItem(key: String, callback: (result: Boolean?) -> Unit?) {
        db.collection(Datasets.ITEMS.path).document(key).delete().addOnSuccessListener {
            callback(true)
        }.addOnFailureListener {
            callback(null)
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

}