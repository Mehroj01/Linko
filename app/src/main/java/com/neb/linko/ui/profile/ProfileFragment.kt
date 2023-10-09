package com.neb.linko.ui.profile

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.neb.linko.App
import com.neb.linko.BottomNavigationClick
import com.neb.linko.R
import com.neb.linko.businessUi.createNewStore.CreateYourStoreActivity
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.FragmentProfileBinding
import com.neb.linko.models.User
import com.neb.linko.ui.about.AboutActivity
import com.neb.linko.ui.editProfile.EditProfileActivity
import com.neb.linko.ui.registerscreens.RegisterActivity
import com.neb.linko.utils.NetworkHelper
import com.squareup.picasso.Picasso
import javax.inject.Inject

class ProfileFragment : Fragment() {

    lateinit var profileBinding: FragmentProfileBinding

    @Inject
    lateinit var profileViewModel: ProfileViewModel
    var user: FirebaseUser? = null
    var profile: User? = null
    lateinit var networkHelper: NetworkHelper
    lateinit var languageCache: LanguageCache
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var spinnerAdapter: SpinnerAdapter
    lateinit var countryList: ArrayList<CountryModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileBinding = FragmentProfileBinding.inflate(inflater, container, false)
        App.appComponent.profile(this)
        sharedPreferences = activity?.getSharedPreferences("Base", MODE_PRIVATE)!!
        editor = sharedPreferences.edit()
        languageCache = LanguageCache(sharedPreferences)

        //Navigation Back Button Click
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                bottomNavigationClick?.profileClick()
            }
        }

        getCountres()

        spinnerAdapter = SpinnerAdapter(countryList)
        profileBinding.spinner.adapter = spinnerAdapter

        profileBinding.spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                editor.putString("country", countryList[position].shortname)
                editor.commit()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        loadProfile()

        profileBinding.editProfile.setOnClickListener {
            if (user?.let { true } == true) {
                if (profile != null) {
                    val intent =
                        Intent(activity?.applicationContext, EditProfileActivity::class.java)
                    val b = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
                    intent.putExtra("profile", Gson().toJson(profile))
                    startActivity(intent, b)
                } else {
                    startActivity(Intent(context, RegisterActivity::class.java))
                }
            } else {
                networkHelper = NetworkHelper(activity?.applicationContext!!)
                if (networkHelper.isNetworkConnected()) {
                    startActivity(Intent(context, RegisterActivity::class.java))
                } else {
                    Toast.makeText(context, "Internet disconnected!!!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        profileBinding.editBtn.setOnClickListener {
            if (user?.let { true } == true) {
                if (profile != null) {
                    val intent =
                        Intent(activity?.applicationContext, EditProfileActivity::class.java)
                    val b = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
                    intent.putExtra("profile", Gson().toJson(profile))
                    startActivity(intent, b)
                } else {
                    startActivity(Intent(context, RegisterActivity::class.java))
                }
            } else {
                startActivity(Intent(context, RegisterActivity::class.java))
            }
        }

        profileBinding.info.setOnClickListener {
            startActivity(Intent(context, AboutActivity::class.java))
        }

        profileBinding.language.setOnClickListener {
            val builder = AlertDialog.Builder(context)
                .setTitle(if (languageCache.getLanguage()) "Language" else "اللغة")
                .setMessage(if (languageCache.getLanguage()) "Select an language" else "اختر لغة")

            builder.setNegativeButton("English") { _, _ ->
                languageCache.updateLanguage("us")
//                Toast.makeText(context, languageCache.getLanguage().toString(), Toast.LENGTH_SHORT)
//                    .show()
                changeLanguage()
            }


            builder.setPositiveButton(if (languageCache.getLanguage()) "Arabic" else "عربي") { _, _ ->
                languageCache.updateLanguage("ar")
//                Toast.makeText(context, languageCache.getLanguage().toString(), Toast.LENGTH_SHORT)
//                    .show()
                changeLanguage()
            }

            builder.show()
        }

        profileBinding.profileAddStoreBtn.setOnClickListener {
            if (user?.let { true } == true) {
                if (profile != null) {
                    val intent =
                        Intent(activity?.applicationContext!!, CreateYourStoreActivity::class.java)
                    intent.putExtra("userUid", user?.uid)
                    activity?.startActivity(intent)
                } else {
                    startActivity(Intent(context, RegisterActivity::class.java))
                }
            } else {
                networkHelper = NetworkHelper(activity?.applicationContext!!)
                if (networkHelper.isNetworkConnected()) {
                    startActivity(Intent(context, RegisterActivity::class.java))
                } else {
                    Toast.makeText(context, "Internet disconnected!!!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        profileBinding.profileBusinessStoreBtn.setOnClickListener {
            if (sharedPreferences.getBoolean("openBusiness", false)) {
                findNavController().navigate(R.id.businessProfileFragment)
            } else {
                if (user?.let { true } == true) {
                    if (profile != null) {
                        val bundle = Bundle()
                        bundle.putString("userUid", user?.uid)
                        bundle.putString("storeKey", profile?.storeKey)
                        findNavController().navigate(R.id.loginFragment, bundle)
                    } else {
                        startActivity(Intent(context, RegisterActivity::class.java))
                    }
                } else {
                    networkHelper = NetworkHelper(activity?.applicationContext!!)
                    if (networkHelper.isNetworkConnected()) {
                        startActivity(Intent(context, RegisterActivity::class.java))
                    } else {
                        Toast.makeText(context, "Internet disconnected!!!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        return profileBinding.root
    }

    private fun getCountres() {
        profileBinding.progress.visibility = View.VISIBLE
        countryList = ArrayList()
        profileViewModel.getCountry().observe(viewLifecycleOwner, Observer {
            if (it != null){
                countryList.clear()
                countryList.addAll(it)
                spinnerAdapter.notifyDataSetChanged()
                selectCountryIcon(sharedPreferences.getString("country", "KWT")!!)
            }
            profileBinding.progress.visibility = View.INVISIBLE
        })
    }

    private fun selectCountryIcon(icName: String) {
        for (i in 0 until countryList.size){
            if (countryList[i].shortname == icName){
                profileBinding.spinner.setSelection(i)
                break
            }
        }
    }

    private fun setValues(l: User) {
        if (l.imageUrl != null) {
            try {
                Picasso.get().load(l.imageUrl)
                    .into(profileBinding.profileImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (l.name != null && l.name != "") {
            profileBinding.username.text =
                if (languageCache.getLanguage()) "Hi ${l.name ?: "Guest"}!" else "${l.name ?: "صديقنا"}أهلاً "
            changeLanguage()
        }
        profile = l

        profile?.storeKey?.let {
            profileBinding.bottomC.visibility = View.VISIBLE
            profileBinding.profileBusinessStoreBtn.visibility = View.VISIBLE
            profileBinding.profileAddStoreBtn.visibility = View.GONE
        } ?: run {
            profileBinding.bottomC.visibility = View.VISIBLE
            profileBinding.profileBusinessStoreBtn.visibility = View.GONE
            profileBinding.profileAddStoreBtn.visibility = View.VISIBLE
        }

    }

    override fun onResume() {
        super.onResume()
        profileViewModel.reset()
        loadProfile()
        changeLanguage()
    }

    var bottomNavigationClick: BottomNavigationClick? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BottomNavigationClick) {
            bottomNavigationClick = context as BottomNavigationClick
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bottomNavigationClick = null
    }


    private fun changeLanguage() {
        val typeface =
            ResourcesCompat.getFont(
                activity?.applicationContext!!,
                if (languageCache.getLanguage()) R.font.poppins_medium else R.font.ge_dinar_one_medium
            )
        val userTypeFace =
            ResourcesCompat.getFont(
                activity?.applicationContext!!,
                if (languageCache.getLanguage()) R.font.poppins_bold else R.font.ge_dinar_one_medium
            )
        profileBinding.apply {
            if (languageCache.getLanguage()) {
                username.text = "Hi ${profile?.name ?: "Guest"}!"
                editTv.text = "Edit My Profile"
                languageTv.text = "Language"
                aboutTv.text = "About us"
            } else {
                username.text = "${profile?.name ?: "صديقنا "}أهلاً "
                editTv.text = "الإعدادات"
                languageTv.text = "اللغة"
                aboutTv.text = "عن لينكو"
            }
            username.typeface = userTypeFace
            editTv.typeface = typeface
            languageTv.typeface = typeface
            aboutTv.typeface = typeface
        }
    }

    fun loadProfile() {
        profileViewModel.getAuth().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                user = it
                if (user?.phoneNumber != null) profileBinding.userNumber.text = user?.phoneNumber
                if (user?.uid != null) {
                    profileViewModel.getUser(user?.uid!!)
                        .observe(viewLifecycleOwner, Observer { l ->
                            if (l != null) {
                                setValues(l)
                            }
                        })
                }
            }
        })
    }
}