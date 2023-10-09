package com.neb.linko.businessUi.createNewStore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.neb.linko.R
import com.neb.linko.api.Datasets
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.ActivityCreateYourStoreBinding
import com.neb.linko.databinding.CreateStoreBottomDialogBinding
import com.neb.linko.models.Registrations

class CreateYourStoreActivity : AppCompatActivity() {

    lateinit var createYourStoreBinding: ActivityCreateYourStoreBinding
    var kwd: String? = null
    var uid: String? = null
    lateinit var languageCache: LanguageCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createYourStoreBinding = ActivityCreateYourStoreBinding.inflate(layoutInflater)
        setContentView(createYourStoreBinding.root)
        languageCache = LanguageCache(getSharedPreferences("Base", MODE_PRIVATE))

        createYourStoreBinding.backBtn.setOnClickListener {
            finish()
        }

        showBottomSheetDialog()

        uid = intent?.getStringExtra("userUid")

        createYourStoreBinding.radioClaim.isChecked = true

        createYourStoreBinding.kwd75.setOnClickListener {
            selectPriceType(0)
        }

        createYourStoreBinding.kwd250.setOnClickListener {
            selectPriceType(1)
        }

        createYourStoreBinding.sendRequest.setOnClickListener {
            createYourStoreBinding.progress.visibility = View.VISIBLE
            sendRequest()
        }

    }

    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetTheme)
        val view = CreateStoreBottomDialogBinding.inflate(LayoutInflater.from(this), null, false)
        view.createStoreDismissDialog.setOnClickListener {
            dialog.dismiss()
        }

        if (languageCache.getLanguage()) {
            view.createStoreTitle.text = "Tip!"
            view.createStoreInfo.text = getString(R.string.create_store_dialog_en_str)
            view.createStoreDismissDialog.text = "CLOSE"
        } else {
            view.createStoreTitle.text = "معلومة!"
            view.createStoreInfo.text = getString(R.string.create_store_dialog_ar_str)
            view.createStoreDismissDialog.text = "CLOSE"
//            val typeface =
//                ResourcesCompat.getFont(this, R.font.ge_dinar_one_medium)
//            view.createStoreTitle.typeface = typeface
//            view.createStoreInfo.typeface = typeface
//            view.createStoreDismissDialog.typeface = typeface
        }

        dialog.setContentView(view.root)
        dialog.show()
    }

    private fun selectPriceType(type: Int) {
        when (type) {
            0 -> {
                createYourStoreBinding.kwd75.setBackgroundResource(R.drawable.create_store_kwd_true_bg)
                createYourStoreBinding.kwd250.setBackgroundResource(R.drawable.create_store_kwd_false_bg)
                kwd = type.toString()
            }
            1 -> {
                createYourStoreBinding.kwd75.setBackgroundResource(R.drawable.create_store_kwd_false_bg)
                createYourStoreBinding.kwd250.setBackgroundResource(R.drawable.create_store_kwd_true_bg)
                kwd = type.toString()
            }
            else -> {
                createYourStoreBinding.kwd75.setBackgroundResource(R.drawable.create_store_kwd_false_bg)
                createYourStoreBinding.kwd250.setBackgroundResource(R.drawable.create_store_kwd_false_bg)
                kwd = null
            }
        }
    }

    private fun sendRequest() {
        createYourStoreBinding.apply {
            if (storeLinkEt.text.trimStart().toString() != "" && instagramAccountEt.text.trimStart()
                    .toString() != "" && ownerNumberEt.text.trimStart()
                    .toString() != "" && contactEmailEt.text.trimStart()
                    .toString() != "" && kwd != null && uid != null
            ) {
                createUnicalPath {
                    val registrations = Registrations(
                        contactEmailEt.text.toString(),
                        instagramAccountEt.text.toString(),
                        if (radioClaim.isChecked) false else if (radioCreate.isChecked) true else null,
                        storeLinkEt.text.toString(),
                        ownerNumberEt.text.toString(),
                        kwd,
                        uid
                    )
                    FirebaseFirestore.getInstance()
                        .collection(Datasets.REGISTRATIONS.path).document(it).set(registrations)
                        .addOnSuccessListener {
                            createYourStoreBinding.progress.visibility = View.INVISIBLE
                            createStoreSuccessContainer.visibility = View.VISIBLE
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@CreateYourStoreActivity,
                                "ERROR",
                                Toast.LENGTH_SHORT
                            ).show()
                            createYourStoreBinding.progress.visibility = View.INVISIBLE
                        }
                }
            } else {
                createYourStoreBinding.progress.visibility = View.INVISIBLE
                Toast.makeText(
                    this@CreateYourStoreActivity,
                    "the data is incomplete",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun createUnicalPath(callback: (String) -> Unit) {
        callback(FirebaseDatabase.getInstance().reference.push().key ?: "")
    }

    override fun onResume() {
        super.onResume()
        changeLanguage()
    }

    private fun changeLanguage() {
        if (languageCache.getLanguage()) {
            createYourStoreBinding.title.text = "Add New Store"
            createYourStoreBinding.addTv.text = "Add"
            createYourStoreBinding.yourStoreTv.text = "Your Store"
            createYourStoreBinding.radioClaim.text = "Claim existing store"
            createYourStoreBinding.radioCreate.text = "Create new store"
            createYourStoreBinding.storeLinkEt.hint = "Add your store link"
            createYourStoreBinding.instagramAccountEt.hint = "Instagram account"
            createYourStoreBinding.ownerNumberEt.hint = "Owner number"
            createYourStoreBinding.contactEmailEt.hint = "Contact e-mail"
            createYourStoreBinding.price75.text = "75 KWD"
            createYourStoreBinding.timePrice75.text = "3 months"
            createYourStoreBinding.price250.text = "250 KWD"
            createYourStoreBinding.timePrice250.text = "1 year"
            createYourStoreBinding.sendRequest.text = "Send Request"
            createYourStoreBinding.createStoreSuccessTitle.text = "Thanks"
            createYourStoreBinding.createStoreSuccessInfo.text =
                getString(R.string.create_store_success_text_en)
            createYourStoreBinding.radioGroup.layoutDirection = View.LAYOUT_DIRECTION_LTR
        } else {
            createYourStoreBinding.title.text = "إضافة متجر جديد"
            createYourStoreBinding.addTv.text = "أضاف"
            createYourStoreBinding.yourStoreTv.text = "متجرك الخاص"
            createYourStoreBinding.radioClaim.text = "التحكم بمتجر موجود"
            createYourStoreBinding.radioCreate.text = "إضافة متجر جديد"
            createYourStoreBinding.storeLinkEt.hint = "أضف رابط متجرك الإلكتروني"
            createYourStoreBinding.instagramAccountEt.hint = "حساب الانستقرام"
            createYourStoreBinding.ownerNumberEt.hint = "رقم هاتف المسؤول"
            createYourStoreBinding.contactEmailEt.hint = "البريد الإلكتروني"
            createYourStoreBinding.price75.text = "75 د.ك"
            createYourStoreBinding.timePrice75.text = "3 أشهر"
            createYourStoreBinding.price250.text = "250 د.ك"
            createYourStoreBinding.timePrice250.text = "سنة واحدة"
            createYourStoreBinding.sendRequest.text = "أرسل الطلب"
            createYourStoreBinding.createStoreSuccessTitle.text = "شكراً"
            createYourStoreBinding.createStoreSuccessInfo.text =
                getString(R.string.create_store_success_text_ar)
            createYourStoreBinding.radioGroup.layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
    }
}