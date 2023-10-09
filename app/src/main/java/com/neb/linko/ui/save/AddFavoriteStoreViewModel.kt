package com.neb.linko.ui.save

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.neb.linko.api.Datasets
import com.neb.linko.models.PromoModel
import com.neb.linko.models.User
import javax.inject.Inject

class AddFavoriteStoreViewModel @Inject constructor(var db: FirebaseFirestore) : ViewModel() {

    var livedata = MediatorLiveData<Boolean>()
    var livedata2 = MediatorLiveData<Boolean>()

    fun getUpdate(uid: String, user: User): LiveData<Boolean> {
        update(uid, user)
        return livedata
    }

    fun addUpdate(uid: String, user: User): LiveData<Boolean> {
        adding(uid, user)
        return livedata2
    }

    private fun adding(uid: String, user: User) {
        val updateStore = HashMap<String, ArrayList<String>?>()
        updateStore["bookmarks"] = user.bookmarks
        db.collection(Datasets.USERS.path).document(uid)
            .update(updateStore as Map<String, Any>)
            .addOnSuccessListener { livedata2.postValue(true) }
            .addOnFailureListener { livedata2.postValue(false) }
    }

    private fun update(uid: String, user: User) {

        val updateStore = HashMap<String, ArrayList<String>?>()
        updateStore["bookmarks"] = user.bookmarks
        db.collection(Datasets.USERS.path).document(uid)
            .update(updateStore as Map<String, Any>)
            .addOnSuccessListener { livedata.postValue(true) }
            .addOnFailureListener { livedata.postValue(false) }
    }
}