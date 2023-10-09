package com.neb.linko.ui.registerscreens

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.neb.linko.api.Datasets
import com.neb.linko.ui.registerscreens.experience.models.Country
import com.neb.linko.ui.registerscreens.experience.models.Interest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignUpViewModel @Inject constructor(var db: FirebaseFirestore) : ViewModel() {

    var interestLiveData = MediatorLiveData<ArrayList<String>?>()
    var countryLiveData = MediatorLiveData<ArrayList<Country>?>()

    fun getInterests(): LiveData<ArrayList<String>?> {
        return interestLiveData
    }

    fun getCounty(): LiveData<ArrayList<Country>?> {
        return countryLiveData
    }

    init {
        loadInterest()
        loadCountry()
    }

    private fun loadInterest() {
        val interests = ArrayList<String>()
        db.collection(Datasets.INTERESTS.path).get()
            .addOnSuccessListener {
                it.documents.forEach { l ->
                    var interest: Interest? = null
                    try {
                        interest = l.toObject(Interest::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (interest != null) interests.add(interest.name!!)
                }
                interestLiveData.postValue(interests)
            }
            .addOnFailureListener {
                interestLiveData.postValue(null)
            }
    }

    private fun loadCountry() {
        var countrys = ArrayList<Country>()
        db.collection(Datasets.COUNTRY.path).get()
            .addOnSuccessListener {
                it.documents.forEach { l ->
                    var countryModel: Country? = null
                    try {
                        countryModel = l.toObject(Country::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (countryModel != null) countrys.add(countryModel)
                }
                countryLiveData.postValue(countrys)
            }

            .addOnFailureListener {
                countryLiveData.postValue(null)
            }
    }

}