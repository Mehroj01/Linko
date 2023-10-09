package com.neb.linko.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.neb.linko.api.Datasets
import com.neb.linko.models.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileViewModel @Inject constructor(var db: FirebaseFirestore, var auth: FirebaseAuth) :
    ViewModel() {

    var user = MutableLiveData<FirebaseUser>()
    var profile = MutableLiveData<User?>()
    var countryLiveDate = MutableLiveData<ArrayList<CountryModel>?>()


    fun getAuth(): LiveData<FirebaseUser> {
        loadAuth()
        return user
    }

    fun getUser(uid: String): LiveData<User?> {
        loadUser(uid)
        return profile
    }

    fun getCountry():LiveData<ArrayList<CountryModel>?>{
        loadCountry()
        return countryLiveDate
    }

    fun reset() {
        loadAuth()
    }

    private fun loadUser(uid: String) {
        db.collection(Datasets.USERS.path).document(uid).get()
            .addOnSuccessListener {
                var user: User? = null
                try {
                    user = it.toObject(User::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (user != null) profile.postValue(user)
            }
            .addOnFailureListener {
                profile.postValue(null)
            }

    }

    private fun loadAuth() {
        user.postValue(auth.currentUser)
    }

    fun loadCountry() {
        db.collection(Datasets.COUNTRY.path).get()
            .addOnSuccessListener {
                val countryList = ArrayList<CountryModel>()
                it.documents.forEach {c->
                    var country: CountryModel? = null
                    try {
                        country = c.toObject(CountryModel::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (country != null) countryList.add(country)
                }
                countryLiveDate.postValue(countryList)
            }
            .addOnFailureListener {
                countryLiveDate.postValue(null)
            }
    }

}