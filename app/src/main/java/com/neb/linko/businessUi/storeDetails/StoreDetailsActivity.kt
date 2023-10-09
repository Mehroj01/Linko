package com.neb.linko.businessUi.storeDetails

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.neb.linko.App
import com.neb.linko.businessUi.businessData.BusinessViewModel
import com.neb.linko.databinding.ActivityStoreDetailsBinding
import com.neb.linko.models.StoreModel
import javax.inject.Inject

class StoreDetailsActivity : AppCompatActivity() {

    @Inject
    lateinit var businessViewModel: BusinessViewModel

    lateinit var storeDetailsBinding: ActivityStoreDetailsBinding

    var store: StoreModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storeDetailsBinding = ActivityStoreDetailsBinding.inflate(layoutInflater)
        setContentView(storeDetailsBinding.root)
        App.appComponent.storeDetailsActivity(this)

        storeDetailsBinding.backBtn.setOnClickListener {
            finish()
        }

        storeDetailsBinding.locationsBtn.setOnClickListener {
            val intent = Intent(this, AddLocationActivity::class.java)
            startActivity(intent)
        }

        businessViewModel.getMyStore(this).observe(this, Observer {
            if (it != null) {
                storeDetailsBinding.apply {
                    link.setText(it.webUrl)
                    instagram.setText(it.instagram)
                    whatsapp.setText(it.whatsapp)
                    callOne.setText(it.phone)
                    callTwo.setText(it.phoneSec)
                    emailText.setText(it.email)
                    store = it
                }
            }
        })


        storeDetailsBinding.apply {
            saveBtn.setOnClickListener {
                progress.visibility = View.VISIBLE
                businessViewModel.editStore(
                    link.text.toString(),
                    instagram.text.toString(),
                    whatsapp.text.toString(),
                    callOne.text.toString(),
                    callTwo.text.toString(),
                    emailText.text.toString()
                ).observe(this@StoreDetailsActivity, Observer {
                    if (it) {
                        finish()
                    } else {
                        Toast.makeText(this@StoreDetailsActivity, "Error", Toast.LENGTH_SHORT)
                            .show()
                    }
                    progress.visibility = View.INVISIBLE
                })
            }
        }

    }
}