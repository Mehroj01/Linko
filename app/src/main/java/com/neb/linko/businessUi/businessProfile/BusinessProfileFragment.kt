package com.neb.linko.businessUi.businessProfile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.neb.linko.App
import com.neb.linko.R
import com.neb.linko.businessUi.analytics.AnalyticsActivity
import com.neb.linko.businessUi.boosts.BoostsActivity
import com.neb.linko.businessUi.businessData.BusinessViewModel
import com.neb.linko.businessUi.businessEditProfile.BusinessEditProfileActivity
import com.neb.linko.businessUi.storeDetails.StoreDetailsActivity
import com.neb.linko.businessUi.storeItems.StoreItemsActivity
import com.neb.linko.businessUi.storePhotos.StorePhotosActivity
import com.neb.linko.businessUi.storePromoCodes.PromoCodesActivity
import com.neb.linko.databinding.FragmentBusinessProfileBinding
import com.neb.linko.models.StoreModel
import com.squareup.picasso.Picasso
import javax.inject.Inject

class BusinessProfileFragment : Fragment() {

    @Inject
    lateinit var businessViewModel: BusinessViewModel

    lateinit var businessProfileBinding: FragmentBusinessProfileBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        businessProfileBinding = FragmentBusinessProfileBinding.inflate(inflater, container, false)
        App.appComponent.businessProfile(this)

        sharedPreferences = activity?.getSharedPreferences("Base", Context.MODE_PRIVATE)!!
        editor = sharedPreferences.edit()

        //Navigation Back Button Click
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack(R.id.profileFragment, false)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        businessProfileBinding.backBtn.setOnClickListener {
            findNavController().popBackStack(R.id.profileFragment, false)
        }

        businessProfileBinding.logoutBtn.setOnClickListener {
            logoutBusinessActivity()
        }

        return businessProfileBinding.root
    }

    override fun onResume() {
        super.onResume()
        loadDate()
    }

    private fun loadDate() {
        businessProfileBinding.progress.visibility = View.VISIBLE
        businessViewModel.getMyStore(viewLifecycleOwner).observe(viewLifecycleOwner, Observer {
            if (it != null) {
                businessProfileBinding.detailsText.text = it.email
                businessProfileBinding.nameText.text = "Hi ${it.name}"

                if (it.imageUrl != null) {
                    try {
                        Picasso.get().load(it.imageUrl)
                            .into(businessProfileBinding.mainImage)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                setOnClickFun()
            } else {
                Toast.makeText(context, "data did not come", Toast.LENGTH_SHORT).show()
            }
            businessProfileBinding.progress.visibility = View.INVISIBLE
        })
    }

    private fun setOnClickFun() {
        businessProfileBinding.editBtn.setOnClickListener {
            val intent =
                Intent(activity?.applicationContext!!, BusinessEditProfileActivity::class.java)
            activity?.startActivity(intent)
        }

        businessProfileBinding.analyticsBtn.setOnClickListener {
            val intent = Intent(activity?.applicationContext, AnalyticsActivity::class.java)
            activity?.startActivity(intent)
        }

        businessProfileBinding.storeDetailsBtn.setOnClickListener {
            val intent = Intent(activity?.applicationContext, StoreDetailsActivity::class.java)
            activity?.startActivity(intent)
        }

        businessProfileBinding.storePhotosBtn.setOnClickListener {
            val intent = Intent(activity?.applicationContext, StorePhotosActivity::class.java)
            activity?.startActivity(intent)
        }

        businessProfileBinding.storeItemsBtn.setOnClickListener {
            val intent = Intent(activity?.applicationContext, StoreItemsActivity::class.java)
            activity?.startActivity(intent)
        }

        businessProfileBinding.promoCodesBtn.setOnClickListener {
            val intent = Intent(activity?.applicationContext, PromoCodesActivity::class.java)
            activity?.startActivity(intent)
        }

        businessProfileBinding.businessBoostBtn.setOnClickListener {
            val intent = Intent(activity?.applicationContext, BoostsActivity::class.java)
            activity?.startActivity(intent)
        }
    }

    private fun logoutBusinessActivity() {
        editor.putBoolean("openBusiness", false)
        editor.commit()
        findNavController().popBackStack(R.id.profileFragment, false)
    }
}