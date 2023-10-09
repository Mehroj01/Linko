package com.neb.linko.ui.registerscreens.experience

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.neb.linko.App
import com.neb.linko.R
import com.neb.linko.api.Datasets
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.FragmentEperienceBinding
import com.neb.linko.models.User
import com.neb.linko.ui.registerscreens.SignUpViewModel
import com.neb.linko.ui.registerscreens.experience.adapter.CityAdapter
import com.neb.linko.ui.registerscreens.experience.models.Country
import com.neb.linko.ui.registerscreens.experience.models.SelectItem
import java.util.*
import javax.inject.Inject

class EperienceFragment : Fragment() {

    lateinit var eperienceBinding: FragmentEperienceBinding

    @Inject
    lateinit var signUpViewModel: SignUpViewModel
    lateinit var interestAdapter: CityAdapter
    lateinit var cityAdapter: CityAdapter
    lateinit var languageAdapter: CityAdapter
    lateinit var ageAdapter: CityAdapter

    lateinit var auth: FirebaseAuth

    lateinit var interestItemsList: ArrayList<SelectItem>
    lateinit var cityItemsList: ArrayList<SelectItem>
    lateinit var languageItemsList: ArrayList<SelectItem>
    lateinit var ageItemsList: ArrayList<SelectItem>

    lateinit var countrys: ArrayList<Country>

    var stopSpeakerClick: StopSpeakerClick? = null
    lateinit var languageCache: LanguageCache

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        eperienceBinding = FragmentEperienceBinding.inflate(inflater, container, false)
        App.appComponent.experience(this)

        languageCache =
            LanguageCache(activity?.getSharedPreferences("Base", AppCompatActivity.MODE_PRIVATE)!!)

        interestItemsList = ArrayList()
        cityItemsList = ArrayList()
        languageItemsList = ArrayList()
        ageItemsList = ArrayList()

        auth = FirebaseAuth.getInstance()

        eperienceBinding.back.setOnClickListener {
            stopSpeakerClick?.click()
        }

        //interestAdapter
        signUpViewModel.getInterests().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                it.forEach { l ->
                    interestItemsList.add(SelectItem(l, false))
                }
                interestAdapter.notifyDataSetChanged()
            }
        })

        //Navigation Back Button Click
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                stopSpeakerClick?.click()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        interestAdapter = CityAdapter(interestItemsList, object : CityAdapter.ExperienceClick {
            override fun click(selectItem: SelectItem) {
                if (selectItem.isSelect!!) {
                    interestItemsList[interestItemsList.indexOf(selectItem)] =
                        SelectItem(selectItem.str, false)
                } else {
                    interestItemsList[interestItemsList.indexOf(selectItem)] =
                        SelectItem(selectItem.str, true)
                }
                interestAdapter.notifyDataSetChanged()
            }
        }, resources)

        eperienceBinding.interestRv.adapter = interestAdapter

        //cityAdapter
        signUpViewModel.getCounty().observe(viewLifecycleOwner, Observer {
            if (it != null) {
                countrys = ArrayList()
                it.forEach { l ->
                    l.cities?.forEach { j ->
                        cityItemsList.add(SelectItem(j, false))
                    }
                    countrys.add(l)
                }
                cityAdapter.notifyDataSetChanged()
            }
        })

        cityAdapter = CityAdapter(cityItemsList, object : CityAdapter.ExperienceClick {
            override fun click(selectItem: SelectItem) {
                val updateItem = SelectItem(selectItem.str, true)
                cityItemsList[cityItemsList.indexOf(selectItem)] = updateItem
                for (i in 0 until cityItemsList.size) {
                    if (cityItemsList[i].isSelect!! && cityItemsList[i] != updateItem) {
                        cityItemsList[i] = SelectItem(cityItemsList[i].str, false)
                    }
                }
                cityAdapter.notifyDataSetChanged()
            }
        }, resources)
        eperienceBinding.cityRv.adapter = cityAdapter

        //languageAdapter
        languageItemsList.add(SelectItem("Arabic", false))
        languageItemsList.add(SelectItem("English", false))

        languageAdapter = CityAdapter(languageItemsList, object : CityAdapter.ExperienceClick {
            override fun click(selectItem: SelectItem) {
                val updateItem = SelectItem(selectItem.str, true)
                languageItemsList[languageItemsList.indexOf(selectItem)] = updateItem
                for (i in 0 until languageItemsList.size) {
                    if (languageItemsList[i].isSelect!! && languageItemsList[i] != updateItem) {
                        languageItemsList[i] = SelectItem(languageItemsList[i].str, false)
                    }
                }
                languageAdapter.notifyDataSetChanged()
            }
        }, resources)
        eperienceBinding.languageRv.adapter = languageAdapter

        //ageAdapter
        ageItemsList.add(SelectItem("15-20", false))
        ageItemsList.add(SelectItem("20-25", false))
        ageItemsList.add(SelectItem("25-30", false))
        ageItemsList.add(SelectItem("30-35", false))
        ageItemsList.add(SelectItem("35-40", false))
        ageItemsList.add(SelectItem("40-45", false))
        ageItemsList.add(SelectItem("45-50", false))
        ageItemsList.add(SelectItem("50-55", false))
        ageItemsList.add(SelectItem("55-60", false))
        ageItemsList.add(SelectItem("60-65", false))
        ageItemsList.add(SelectItem("65-70", false))

        ageAdapter = CityAdapter(ageItemsList, object : CityAdapter.ExperienceClick {
            override fun click(selectItem: SelectItem) {
                val updateItem = SelectItem(selectItem.str, true)
                ageItemsList[ageItemsList.indexOf(selectItem)] = updateItem
                for (i in 0 until ageItemsList.size) {
                    if (ageItemsList[i].isSelect!! && ageItemsList[i] != updateItem) {
                        ageItemsList[i] = SelectItem(ageItemsList[i].str, false)
                    }
                }
                ageAdapter.notifyDataSetChanged()
            }
        }, resources)
        eperienceBinding.ageRv.adapter = ageAdapter

        eperienceBinding.saveBtn.setOnClickListener {
            val interestSituation = detectInterestList(interestItemsList)
            val citySituation = detectList(cityItemsList)
            val languageSituation = detectList(languageItemsList)
            val ageSituation = detectList(ageItemsList)

            if (interestSituation.isNotEmpty() && citySituation != "" && languageSituation != "" && ageSituation != "") {
                eperienceBinding.progress.visibility = View.VISIBLE
                val user = User()
                user.age = ageSituation
                user.city = citySituation
                user.country = detectCountry(citySituation)
                user.creationDate = Date().time
                user.details = ""
                user.imagePath = ""
                user.interestsList = interestSituation
                user.key = auth.uid
                user.language = languageSituation
                user.name = auth.currentUser?.displayName
                user.phone = auth.currentUser?.phoneNumber
                FirebaseFirestore.getInstance().collection(Datasets.USERS.path).document(auth.uid!!)
                    .set(user)
                    .addOnSuccessListener {
                        eperienceBinding.progress.visibility = View.INVISIBLE
                        stopSpeakerClick?.click()
                    }
                    .addOnFailureListener {
                        eperienceBinding.progress.visibility = View.INVISIBLE
                        stopSpeakerClick?.click()
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "The information is incomplete", Toast.LENGTH_SHORT).show()
            }
        }

        return eperienceBinding.root
    }

    private fun detectCountry(citySituation: String): String {
        var c: String? = null
        try {
            for (i in 0 until countrys.size) {
                for (j in 0 until countrys[i].cities?.size!!) {
                    if (countrys[i].cities!![j] == citySituation) {
                        c = countrys[i].shortname
                        break
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return c ?: ""
    }

    private fun detectInterestList(list: ArrayList<SelectItem>): ArrayList<String> {
        val s = ArrayList<String>()
        for (selectItem in list) {
            if (selectItem.isSelect!!) {
                s.add(selectItem.str!!)
            }
        }
        return s
    }

    private fun detectList(list: ArrayList<SelectItem>): String {
        var s: String = ""
        for (selectItem in list) {
            if (selectItem.isSelect == true) {
                s = selectItem.str!!
                break
            }
        }
        return s
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is StopSpeakerClick) {
            stopSpeakerClick = context as StopSpeakerClick
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSpeakerClick = null
    }

    interface StopSpeakerClick {
        fun click()
    }

    override fun onResume() {
        super.onResume()
        if (languageCache.getLanguage()) {
            eperienceBinding.tv1.text = "For a better"
            eperienceBinding.tv2.text = "experience"
            eperienceBinding.tv3.text = ", select"
            eperienceBinding.tv4.text = "your choices"
            eperienceBinding.interestTv.text = "Your interest"
            eperienceBinding.cityTv.text = "Your City"
            eperienceBinding.languageTv.text = "Main language"
            eperienceBinding.ageTv.text = "Your age"
            eperienceBinding.saveBtn.text = "Save"

        } else {
            eperienceBinding.tv1.text = "لتجربة أفضل ،\nحدد اختياراتك"
            eperienceBinding.tv2.text = ""
            eperienceBinding.tv3.text = ""
            eperienceBinding.tv4.text = ""

            eperienceBinding.interestTv.text = "اهتماماتك"
            eperienceBinding.cityTv.text = "مدينتك"
            eperienceBinding.languageTv.text = "لغتك الرئيسية"
            eperienceBinding.ageTv.text = "عمرك"
            eperienceBinding.saveBtn.text = "حفظ"
            setTextFonts()
        }

        val staggeredGridLayoutManager =
            StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.HORIZONTAL
            )
        staggeredGridLayoutManager.reverseLayout = !languageCache.getLanguage()
        eperienceBinding.interestRv.layoutManager = staggeredGridLayoutManager

        val linearLayoutManager2 =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                !languageCache.getLanguage()
            )
        eperienceBinding.cityRv.layoutManager = linearLayoutManager2

        val linearLayoutManager3 =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                !languageCache.getLanguage()
            )
        eperienceBinding.languageRv.layoutManager = linearLayoutManager3

        val linearLayoutManager4 =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                !languageCache.getLanguage()
            )
        eperienceBinding.ageRv.layoutManager = linearLayoutManager4

        interestAdapter.notifyDataSetChanged()
        cityAdapter.notifyDataSetChanged()
        languageAdapter.notifyDataSetChanged()
        ageAdapter.notifyDataSetChanged()
    }

    private fun setTextFonts() {
        val typeface =
            ResourcesCompat.getFont(activity?.applicationContext!!, R.font.ge_dinar_one_medium)
        eperienceBinding.tv1.typeface = typeface
//        eperienceBinding.tv2.typeface = typeface
//        eperienceBinding.tv3.typeface = typeface
//        eperienceBinding.tv4.typeface = typeface
//
        eperienceBinding.interestTv.typeface = typeface
        eperienceBinding.cityTv.typeface = typeface
        eperienceBinding.languageTv.typeface = typeface
        eperienceBinding.ageTv.typeface = typeface
        eperienceBinding.saveBtn.typeface = typeface
    }
}