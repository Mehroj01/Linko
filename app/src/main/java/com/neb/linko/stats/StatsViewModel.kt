package com.neb.linko.stats

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.neb.linko.App
import com.neb.linko.api.Datasets
import com.neb.linko.models.StoreModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class StatsViewModel @Inject constructor(var db: FirebaseFirestore) : ViewModel() {

    init {
        App.appComponent.statsViewModel(this)
    }

    fun updateStats(
        store: StoreModel?,
        statName: String,
        increment: Boolean,
        callback: (Boolean?) -> Unit
    ) {
        if (store != null) {
            update(store, statName, increment) { s ->
                if (s == null) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
        }else{
            callback(false)
        }
    }

    private fun update(
        store: StoreModel,
        statName: String,
        increment: Boolean,
        callback: (Exception?) -> Unit
    ) {
        val batch = db.batch()

        var formatter = SimpleDateFormat("HH")
        val dayFormatter = SimpleDateFormat("HH")
        val date = Date()

        /******************  For Stores ****************/
        val ref = db.collection(Datasets.STATISTICS.path).document("stores")
        /// Daily Stats
        formatter = SimpleDateFormat("ddMMyyyy")
        batch.set(
            ref.collection("daily").document(store.key!!)
                .collection("linko_" + formatter.format(date))
                .document("linko_" + dayFormatter.format(date)),
            mapOf(
                statName to FieldValue.increment(
                    if (increment) {
                        1
                    } else {
                        -1
                    }
                )
            ),
            SetOptions.merge()
        )

        /// Monthly Stats
        formatter = SimpleDateFormat("MMyyyy")
        batch.set(
            ref.collection("monthly").document(store.key!!)
                .collection("linko_" + formatter.format(date)).document("data"),
            mapOf(
                statName to FieldValue.increment(
                    if (increment) {
                        1
                    } else {
                        -1
                    }
                )
            ),
            SetOptions.merge()
        )

        /// Yearly Stats
        formatter = SimpleDateFormat("yyyy")
        batch.set(
            ref.collection("yearly").document(store.key!!)
                .collection("linko_" + formatter.format(date)).document("data"),
            mapOf(
                statName to FieldValue.increment(
                    if (increment) {
                        1
                    } else {
                        -1
                    }
                )
            ),
            SetOptions.merge()
        )

        /// Overall Stats
        batch.set(
            ref.collection("overall").document(store.key!!),
            mapOf(
                statName to FieldValue.increment(
                    if (increment) {
                        1
                    } else {
                        -1
                    }
                )
            ),
            SetOptions.merge()
        )

        /******************  For Admins ****************/
        val adminRef = db.collection(Datasets.STATISTICS.path).document("admin")
        /// Daily Stats
        formatter = SimpleDateFormat("ddMMyyyy")
        batch.set(
            adminRef.collection("daily").document("linko_" + formatter.format(date))
                .collection("linko_" + dayFormatter.format(date)).document("data"),
            mapOf(
                statName to FieldValue.increment(
                    if (increment) {
                        1
                    } else {
                        -1
                    }
                )
            ),
            SetOptions.merge()
        )

        batch.set(
            adminRef.collection("daily").document("linko_" + formatter.format(date)),
            mapOf(
                statName to FieldValue.increment(
                    if (increment) {
                        1
                    } else {
                        -1
                    }
                )
            ),
            SetOptions.merge()
        )

        /// Monthly Stats
        formatter = SimpleDateFormat("MMyyyy")
        batch.set(
            adminRef.collection("monthly").document("linko_" + formatter.format(date)),
            mapOf(
                statName to FieldValue.increment(
                    if (increment) {
                        1
                    } else {
                        -1
                    }
                )
            ),
            SetOptions.merge()
        )

        /// Yearly Stats
        formatter = SimpleDateFormat("yyyy")
        batch.set(
            adminRef.collection("yearly").document("linko_" + formatter.format(date)),
            mapOf(
                statName to FieldValue.increment(
                    if (increment) {
                        1
                    } else {
                        -1
                    }
                )
            ),
            SetOptions.merge()
        )

        /// Overall Stats
        batch.set(
            adminRef.collection("overall").document("data"),
            mapOf(
                statName to FieldValue.increment(
                    if (increment) {
                        1
                    } else {
                        -1
                    }
                )
            ),
            SetOptions.merge()
        )

        batch.commit().addOnSuccessListener {
            callback(null)
        }.addOnFailureListener {
            callback(it)
        }
    }

}