package com.neb.linko.businessUi.boosts

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.neb.linko.App
import com.neb.linko.databinding.ActivityPaymentBinding
import java.util.*
import javax.inject.Inject


class PaymentActivity : AppCompatActivity() {

    @Inject
    lateinit var boostsViewModel: BoostsViewModel

    lateinit var paymentBinding: ActivityPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paymentBinding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(paymentBinding.root)
        App.appComponent.paymentActivity(this)

        paymentBinding.backBtn.setOnClickListener {
            finish()
        }

        paymentBinding.progress.setOnClickListener {
            Toast.makeText(this, "Please wait!", Toast.LENGTH_SHORT).show()
        }

        paymentBinding.progress.visibility = View.VISIBLE

        val url = intent.getStringExtra("payUrl")

        val webSettings = paymentBinding.webView.settings
        webSettings.javaScriptEnabled = true

        paymentBinding.webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                paymentBinding.progress.visibility = View.INVISIBLE

                Log.d(
                    "MyApplication", consoleMessage.message() + " -- From line "
                            + consoleMessage.lineNumber() + " of "
                            + consoleMessage.sourceId()
                )
                return super.onConsoleMessage(consoleMessage)
            }
        }

        paymentBinding.webView.loadUrl(url ?: "")

        paymentBinding.webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if ((url?.lowercase(Locale.getDefault()))!!.contains(("https://us-central1-heylinko.cloudfunctions.net/api/v1/").toLowerCase())) {
                    val uri = Uri.parse(url)
                    val paymentId = uri.getQueryParameter("paymentId")
                    val id = uri.getQueryParameter("Id")
                    detect((id ?: "0").toLong(), (paymentId ?: "0").toLong())
                } else {
                    if (url.lowercase(Locale.getDefault())
                            .contains(("https://linko.com/failure/").toLowerCase())
                    ) {
                        boostsViewModel.situation = true
                        stopActivity()
                    } else {
                        view?.loadUrl(url ?: "")
                    }
                }
                return true
            }
        }

    }

    private fun stopActivity() {
        Handler().postDelayed({
            finish()
        }, 6000)
    }

    private fun detect(id: Long, paymentId: Long) {
        boostsViewModel.detect(id, paymentId, this) {
            boostsViewModel.situation = it
            stopActivity()
        }
    }
}