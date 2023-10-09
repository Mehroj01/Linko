package com.neb.linko.businessUi.businessData

import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.neb.linko.api.Datasets
import com.neb.linko.models.StoreLocation
import com.neb.linko.models.StoreModel
import com.neb.linko.models.User
import com.neb.linko.ui.profile.ProfileViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusinessViewModel @Inject constructor(
    var db: FirebaseFirestore,
    var profileViewModel: ProfileViewModel,
    var auth: FirebaseAuth
) : ViewModel() {

    var userViewModel = MutableLiveData<User?>()
    var myStoreViewModel = MutableLiveData<StoreModel?>()
    var editLocationViewModel = MutableLiveData<Boolean>()
    var editStoreViewModel = MutableLiveData<Boolean>()
    var user: User? = null
    var myStore: StoreModel? = null

    fun getUser(owner: LifecycleOwner): LiveData<User?> {
        loadUser(owner)
        return userViewModel
    }

    fun loadUser(owner: LifecycleOwner) {
        profileViewModel.getUser(auth.currentUser?.uid ?: "").observe(owner, Observer {
            if (it != null) {
                userViewModel.postValue(it)
                user = it
                getMyStore(owner)
            } else {
                userViewModel.postValue(null)
            }
        })
    }

    fun getMyStore(owner: LifecycleOwner): LiveData<StoreModel?> {
        if (user != null) {
            loadMyStore()
        } else {
            loadUser(owner)
        }
        return myStoreViewModel
    }

    fun loadMyStore() {
        db.collection(Datasets.STORES.path).document(user?.storeKey ?: "").get()
            .addOnSuccessListener {
                if (it != null) {
                    var storeModel: StoreModel? = null
                    try {
                        storeModel = it.toObject(StoreModel::class.java)
                    } catch (e: Exception) {
                        e.message
                        e.printStackTrace()
                    }
//                    if (storeModel != null) {
//                        myStoreViewModel.postValue(storeModel)
//                        myStore = storeModel
//                    }

                    myStoreViewModel.postValue(storeModel)
                    myStore = storeModel
                }
            }
            .addOnFailureListener {
                myStoreViewModel.postValue(null)
            }
    }

    fun addLocation(locations:ArrayList<StoreLocation?>): LiveData<Boolean> {
        if (myStore != null && user != null) {
            val updateStoreLocation = HashMap<String, ArrayList<StoreLocation?>?>()
            updateStoreLocation["storeLocations"] = locations
            FirebaseFirestore.getInstance().collection(Datasets.STORES.path)
                .document(myStore?.key ?: "")
                .update(updateStoreLocation as Map<String, ArrayList<StoreLocation>>)
                .addOnSuccessListener {
                    editLocationViewModel.postValue(true)
                }
                .addOnFailureListener {
                    editLocationViewModel.postValue(false)
                }
        } else {
            editLocationViewModel.postValue(false)
        }
        return editLocationViewModel
    }

    fun editStore(
        link: String,
        instagram: String,
        whatsapp: String,
        callOne: String,
        callTwo: String,
        emailText: String
    ): LiveData<Boolean> {

        if (myStore != null && user != null) {
            val updateStoreLocation = HashMap<String, String>()
            if (link.trim() != "") updateStoreLocation["webUrl"] = link
            if (instagram.trim() != "") updateStoreLocation["instagram"] = instagram
            if (whatsapp.trim() != "") updateStoreLocation["whatsapp"] = whatsapp
            if (callOne.trim() != "") updateStoreLocation["phone"] = callOne
            if (callTwo.trim() != "") updateStoreLocation["phoneSec"] = callTwo
            if (emailText.trim() != "") updateStoreLocation["email"] = emailText
            FirebaseFirestore.getInstance().collection(Datasets.STORES.path)
                .document(myStore?.key ?: "")
                .update(updateStoreLocation as Map<String, String>)
                .addOnSuccessListener {
                    editStoreViewModel.postValue(true)
                }
                .addOnFailureListener {
                    editStoreViewModel.postValue(false)
                }
        } else {
            editStoreViewModel.postValue(false)
        }

        return editStoreViewModel
    }

}