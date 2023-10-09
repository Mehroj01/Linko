package com.neb.linko.ui.ratings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.neb.linko.App
import com.neb.linko.R
import com.neb.linko.api.Datasets
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.ActivityRatingBinding
import com.neb.linko.ui.registerscreens.RegisterActivity
import javax.inject.Inject

class RatingActivity : AppCompatActivity() {

    @Inject
    lateinit var db: FirebaseFirestore

    lateinit var userKey: String

    lateinit var languageCache: LanguageCache

    lateinit var ratingBinding: ActivityRatingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ratingBinding = ActivityRatingBinding.inflate(layoutInflater)
        setContentView(ratingBinding.root)
        App.appComponent.ratingActivity(this)
        languageCache = LanguageCache(getSharedPreferences("Base", MODE_PRIVATE)!!)

        val storeKey = intent.getStringExtra("storeKey")
        userKey = intent.getStringExtra("userKey") ?: ""
        val storeUsName = intent.getStringExtra("storeUsName")
        val storeArName = intent.getStringExtra("storeArName")

        ratingBinding.submitBtn.setOnClickListener {
            if (userKey != "") {
                if (storeKey != null) {
                    ratingBinding.progress.visibility = View.VISIBLE
                    addRating(storeKey, userKey, ratingBinding.ratingBar.rating)
                } else Toast.makeText(this, "store key not found", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        }

        //How to work callback
//        ratingBinding.submitBtn.setOnClickListener {
//            addRating(storeKey,userKey,ratingBinding.ratingBar.rating){
//
//            }
//        }

        ratingBinding.laterBtn.setOnClickListener { finish() }

        ratingBinding.feedbackText.text =
            if (languageCache.getLanguage()) "How was your order experiences from ${storeUsName ?: ""}" else " كيف كانت تجربتك من ${storeArName ?: ""}"

        ratingBinding.laterBtn.text =
            if (languageCache.getLanguage()) "Not now, maybe later!" else "ليس الآن، ربما لاحقاً"

        ratingBinding.submitBtn.text = if (languageCache.getLanguage()) "Submit" else "أرسل"
    }

    fun addRating(
        storeKey: String,
        userKey: String,
        rating: Float,
//        callback: (Exception?) -> Unit
    ) {
        /******************  For Rating ****************/
        db.collection(Datasets.RATINGS.path).document(storeKey)
            .set(mapOf(userKey to rating), SetOptions.merge())
            .addOnSuccessListener {
                ratingBinding.progress.visibility = View.INVISIBLE
                finish()
//                callback(null)
            }.addOnFailureListener {
                ratingBinding.progress.visibility = View.INVISIBLE
                Toast.makeText(this, "try again", Toast.LENGTH_SHORT).show()
//                callback(it)
            }
    }

    override fun onResume() {
        super.onResume()
        if (userKey != "") {
            try {
                userKey = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (!languageCache.getLanguage()) setTextFonts()
    }

    private fun setTextFonts() {
        val typeface =
            ResourcesCompat.getFont(this, R.font.ge_dinar_one_medium)
        ratingBinding.feedbackText.typeface = typeface
        ratingBinding.submitBtn.typeface = typeface
        ratingBinding.laterBtn.typeface = typeface
    }

}