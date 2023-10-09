package com.neb.linko.ui.web

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.neb.linko.R
import com.neb.linko.api.Datasets
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.ActivityReportBinding
import com.neb.linko.models.StoreModel
import java.util.*

class ReportActivity : AppCompatActivity() {
    private lateinit var reportBinding: ActivityReportBinding
    var store: StoreModel? = null
    var auth = FirebaseAuth.getInstance().currentUser
    var type = 0
    lateinit var languageCache: LanguageCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reportBinding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(reportBinding.root)

        store = Gson().fromJson(intent.getStringExtra("storeData"), StoreModel::class.java)

        languageCache = LanguageCache(getSharedPreferences("Base", MODE_PRIVATE))

        reportBinding.backBtn.setOnClickListener {
            finish()
        }

        reportBinding.saveBtn.setOnClickListener {
            if (store?.key != null) {
                reportBinding.progress.visibility = View.VISIBLE
                reportStore(
                    mapOf(
                        "storeId" to store?.key!!,
                        "created" to Date().time,
                        "userId" to if (auth?.let { true } == true) auth?.uid!! else "",
                        "details" to reportBinding.reportDetails.text.toString(),
                        "type" to type
                    )
                ) { error ->
                    if (error != null) {
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                    } else {
                        finish()
                    }
                    reportBinding.progress.visibility = View.INVISIBLE
                }
            }

        }

        reportBinding.webBtn.setOnClickListener {
            updateBtnState(reportBinding.webBtn)
            type = 0
        }

        reportBinding.scamBtn.setOnClickListener {
            updateBtnState(reportBinding.scamBtn)
            type = 1
        }

        reportBinding.advertBtn.setOnClickListener {
            updateBtnState(reportBinding.advertBtn)
            type = 2
        }

    }

    private fun updateBtnState(btn: AppCompatButton) {
        reportBinding.webBtn.setBackgroundResource(R.drawable.report_corner_bg)
        reportBinding.scamBtn.setBackgroundResource(R.drawable.report_corner_bg)
        reportBinding.advertBtn.setBackgroundResource(R.drawable.report_corner_bg)

        btn.setBackgroundResource(R.drawable.report_corner_bg_selected)
    }

    fun reportStore(dict: Map<String, Any>, callback: (Exception?) -> Unit) {

        FirebaseFirestore.getInstance().collection(Datasets.REPORTS.path).add(dict)
            .addOnSuccessListener { callback(null) }.addOnFailureListener { callback(null) }
    }

    override fun onResume() {
        super.onResume()
        reportBinding.title.text =
            if (languageCache.getLanguage()) "Report this store" else "الإبلاغ عن هذا الموقع"
        reportBinding.webBtn.text =
            if (languageCache.getLanguage()) "This website doesn\'t work" else "هذا الموقع لا يعمل!"
        reportBinding.scamBtn.text =
            if (languageCache.getLanguage()) "I think this is a scam" else "أظن هذا الموقع مشبوه"
        reportBinding.advertBtn.text =
            if (languageCache.getLanguage()) "They don\'t deliver as advertised" else "ليس لديهم خدمة توصيل كما يروّجون"
        reportBinding.deskInfo.text = if (languageCache.getLanguage()) "Talk to us" else "تحدث معنا"
        reportBinding.saveBtn.text = if (languageCache.getLanguage()) "Save" else "حفظ"
        if (languageCache.getLanguage()) {
            reportBinding.reportDetails.textDirection = View.TEXT_DIRECTION_LTR
            reportBinding.reportDetails.hint = "Tell us what you think about this store.."
        } else {
            reportBinding.reportDetails.textDirection = View.TEXT_DIRECTION_RTL
            reportBinding.reportDetails.hint = "حدثنا عن المشكلة …"
            setTextFonts()
        }
    }
    private fun setTextFonts() {
        val typeface = ResourcesCompat.getFont(this, R.font.ge_dinar_one_medium)
        reportBinding.title.typeface = typeface
        reportBinding.webBtn.typeface = typeface
        reportBinding.scamBtn.typeface = typeface
        reportBinding.advertBtn.typeface = typeface
        reportBinding.deskInfo.typeface = typeface
        reportBinding.saveBtn.typeface = typeface
        reportBinding.reportDetails.typeface = typeface
    }
}