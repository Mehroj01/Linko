package com.neb.linko

import android.animation.Animator
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.neb.linko.api.Datasets
import com.neb.linko.databinding.ActivitySplashBinding
import com.neb.linko.utils.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SplashActivity : AppCompatActivity(), CoroutineScope {

    lateinit var splashBinding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashBinding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(splashBinding.root)

        splashBinding.animationView.addAnimatorListener(object : Animator.AnimatorListener {

            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                checkVersion()
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }

        })

        splashBinding.updateBtn.setOnClickListener {
            Toast.makeText(this, "Update", Toast.LENGTH_SHORT).show()
        }

    }

    override val coroutineContext: CoroutineContext get() = Job()

    fun checkVersion() {
        val db = FirebaseFirestore.getInstance()
        db.collection(Datasets.SETTINGS.path)
            .document("version")
            .get()
            .addOnSuccessListener { document ->

                val data = document.data
                data?.let {

                    val serverVersion: String = "${it["version"]}.0"
                    val buildVersion: String = BuildConfig.VERSION_NAME

                    if (serverVersion != buildVersion) {

                        splashBinding.animationView.visibility = View.GONE
                        splashBinding.updateView.visibility = View.VISIBLE

                    } else {
                        finishAffinity()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }

                }?: let {
                    finishAffinity()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }

            }
            .addOnFailureListener { exception ->

                finishAffinity()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
    }
}