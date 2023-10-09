package com.neb.linko.ui.web

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.neb.linko.api.Datasets
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.ActivityWebBinding
import com.neb.linko.models.StoreModel

class WebActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebBinding
    private var store: StoreModel? = null

    private var isTerms = false
    private var isPrivacy = false

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        intent.extras?.let { bundle ->

            bundle.getBoolean("isTerms").let {
                isTerms = it
            }

            bundle.getBoolean("isPrivacy").let {
                isPrivacy = it
            }
        }
    }

    lateinit var languageCache: LanguageCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.extras?.let { bundle ->
            bundle.getString("storeData")?.let {
                store = Gson().fromJson(it, StoreModel::class.java)
            }

            bundle.getBoolean("isTerms").let {
                isTerms = it
            }

            bundle.getBoolean("isPrivacy").let {
                isPrivacy = it
            }
        }

        binding = ActivityWebBinding.inflate(layoutInflater)
        setContentView(binding.root)

        languageCache = LanguageCache(getSharedPreferences("Base", MODE_PRIVATE))

        binding.titleText.text = if (languageCache.getLanguage()) "About Us" else "معلومات عنا"

        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = SSLTolerentWebViewClient()

//        var progressHud = KProgressHUD.create(this)
//            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
//            .setCancellable(false)
//            .setAnimationSpeed(2)
//            .setDimAmount(0.5f)

        binding.webView.getSettings().loadWithOverviewMode = true
        binding.webView.getSettings().useWideViewPort = true
        binding.webView.getSettings().domStorageEnabled = true
        binding.webView.webChromeClient = WebChromeClient()
//        binding.webView.webViewClient = object : WebViewClient() {
//            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//                progressHud.show()
//                view.loadUrl(url)
//                return true
//            }
//
//            override fun onPageFinished(view: WebView, url: String) {
//                progressHud.dismiss()
//            }
//        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        if (isTerms || isPrivacy) {

            binding.titleText.text =
                if (isTerms) "<![CDATA[Terms & Policies]]>" else "Privacy Policy"
            binding.moreBtn.visibility = View.GONE
            val mainDB = FirebaseFirestore.getInstance()
            mainDB.collection(Datasets.SETTINGS.path).document(if (isTerms) "terms" else "privacy")
                .addSnapshotListener { snapshot, error ->
                    snapshot?.data?.let { resp ->
                        resp.get("data")?.let {
                            binding.webView.loadUrl(it as String)
                        }
                    }
                }

        } else if (store != null) {
            store!!.webUrl?.let { url ->
                binding.webView.loadUrl(url)
            }

            binding.titleText.text =
                if (languageCache.getLanguage()) store!!.name ?: "About Us" else store!!.nameArabic
                    ?: "معلومات عنا"
            binding.moreBtn.visibility = View.VISIBLE
            binding.moreBtn.setOnClickListener {

                AlertDialog.Builder(this)
                    .setTitle(if (languageCache.getLanguage()) "Select an option" else "اختر أحد الخيارات")
                    .setPositiveButton(if (languageCache.getLanguage()) "Share Via" else "نشر من خلال…") { dialog, i ->
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(store!!.webUrl)
                        startActivity(i)

                    }
                    .setNegativeButton(if (languageCache.getLanguage()) "Report this store" else "الإبلاغ عن هذا الموقع") { dialog, i ->
                        val intent = Intent(this, ReportActivity::class.java).apply {
                            this.putExtra("storeData", Gson().toJson(store))
                        }
                        startActivity(intent)
                    }
                    .setNeutralButton(if (languageCache.getLanguage()) "Cancel" else "إلغاء") { dialog, i ->
                        dialog.dismiss()
                    }
                    .show()
            }

        } else {

        }
    }

    private class SSLTolerentWebViewClient : WebViewClient() {
        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler,
            error: SslError?
        ) {
            handler.proceed() // Ignore SSL certificate errors
        }
    }
}