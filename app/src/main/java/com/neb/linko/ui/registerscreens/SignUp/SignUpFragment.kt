package com.neb.linko.ui.registerscreens.SignUp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.neb.linko.R
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.FragmentSignUpBinding
import com.neb.linko.ui.registerscreens.experience.EperienceFragment

class SignUpFragment : Fragment() {

    private lateinit var signUpBinding: FragmentSignUpBinding
    var stopSpeakerClick: EperienceFragment.StopSpeakerClick? = null
    lateinit var languageCache: LanguageCache


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        signUpBinding = FragmentSignUpBinding.inflate(inflater, container, false)

        languageCache =
            LanguageCache(activity?.getSharedPreferences("Base", AppCompatActivity.MODE_PRIVATE)!!)

        signUpBinding.nextBtn.setOnClickListener {
            val number = signUpBinding.phoneText.text.toString()
            if (number.trim() != "") {
                val bundle = Bundle()
                val country = signUpBinding.countryCode.selectedCountryCode
                bundle.putString("phone_number", "$country$number")
                findNavController().navigate(R.id.pinFragment, bundle)
            }
        }

        signUpBinding.skipBtn.setOnClickListener {
            stopSpeakerClick?.click()
        }

//        signUpBinding.skipBtn.text = if (languageCache.getLanguage()) "Next" else "التالي"

        return signUpBinding.root
    }

    override fun onResume() {
        super.onResume()
        signUpBinding.phoneText.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        signUpBinding.titleText.text =
            if (languageCache.getLanguage()) "Enter your phone \nnumber" else "ادخل رقم هاتفك"
        signUpBinding.phoneText.hint =
            if (languageCache.getLanguage()) {
                signUpBinding.phoneText.textDirection = View.TEXT_DIRECTION_LTR
                "Enter your phone number"
            } else {
                signUpBinding.phoneText.textDirection = View.TEXT_DIRECTION_RTL
                "ادخل رقم هاتفك"
            }

        signUpBinding.nextBtn.text =
            if (languageCache.getLanguage()) "Next" else "التالي"

        signUpBinding.skipBtn.text =
            if (languageCache.getLanguage()) "Skip for now" else "تجاهل حالياً"

        if (!languageCache.getLanguage()) setTextFonts()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EperienceFragment.StopSpeakerClick) {
            stopSpeakerClick = context as EperienceFragment.StopSpeakerClick
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSpeakerClick = null
    }

    override fun onPause() {
        super.onPause()
        closedKeyboard()
    }

    private fun closedKeyboard() {
        val view = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun setTextFonts() {
        val typeface =
            ResourcesCompat.getFont(activity?.applicationContext!!, R.font.ge_dinar_one_medium)
        signUpBinding.titleText.typeface = typeface
        signUpBinding.phoneText.typeface = typeface
        signUpBinding.skipBtn.typeface = typeface
        signUpBinding.nextBtn.typeface = typeface
    }
}