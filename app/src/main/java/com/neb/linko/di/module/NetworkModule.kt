package com.neb.linko.di.module

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NetworkModule {

    @Provides
    @Singleton
    fun db(): FirebaseFirestore {
        val fireStore = FirebaseFirestore.getInstance()
//        val settings = FirebaseFirestoreSettings.Builder()
//            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
//            .build()
//        fireStore.firestoreSettings = settings
        return fireStore
    }

    @Provides
    @Singleton
    fun auth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}