package com.neb.linko.ui.registerscreens.pin

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.neb.linko.R
import com.neb.linko.api.Datasets
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.FragmentPinBinding
import com.neb.linko.ui.registerscreens.experience.EperienceFragment
import java.util.concurrent.TimeUnit

class PinFragment : Fragment() {
    lateinit var pinBinding: FragmentPinBinding
    lateinit var auth: FirebaseAuth
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    lateinit var languageCache: LanguageCache
    var stopSpeakerClick: EperienceFragment.StopSpeakerClick? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        pinBinding = FragmentPinBinding.inflate(inflater, container, false)

        languageCache =
            LanguageCache(activity?.getSharedPreferences("Base", AppCompatActivity.MODE_PRIVATE)!!)

        pinBinding.titleText.text =
            if (languageCache.getLanguage()) "Enter the 6-digit \ncode sent to you at" else "أدخل الأرقام الـ6 التي\nستصلك الآن على رقم"

        pinBinding.skipBtn.text =
            if (languageCache.getLanguage()) "I haven\'t received a code" else "لم يصلني رمز التفعيل بعد!"

        pinBinding.titleText.setOnClickListener {
            findNavController().navigate(R.id.eperienceFragment)
        }

        pinBinding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("en")

        val phoneNumber = "+${arguments?.getString("phone_number", "")}"
        sentVerificationCode(phoneNumber)

        if (languageCache.getLanguage()) {
            pinBinding.phoneText.gravity = Gravity.START
        } else {
            pinBinding.phoneText.gravity = Gravity.END
        }
        pinBinding.phoneText.text = "$phoneNumber"

        pinBinding.skipBtn.setOnClickListener {
            try {
                resendCode(phoneNumber)
                Toast.makeText(context, "Sent again", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "token not detected", Toast.LENGTH_SHORT).show()
            }
        }

//        binding.passwordEt.addTextChangedListener {
//            if (it.toString().length == 6) {
//                verifyCode()
//            }
//        }
        
        pinBinding.progress.visibility = View.VISIBLE
        
        pinBinding.progress.setOnClickListener {
            Toast.makeText(context, "Please wait!", Toast.LENGTH_SHORT).show()
        }

        return pinBinding.root
    }

    fun sentVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(Activity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun resendCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(Activity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    private var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
//            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
//            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
//            Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            pinBinding.progress.visibility = View.GONE
            addEtListeners()
            storedVerificationId = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        pinBinding.progress.visibility = View.VISIBLE
        auth.signInWithCredential(credential)
            .addOnCompleteListener(Activity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
//                    Log.d(TAG, "signInWithCredential:success")
                    val user = task.result?.user
                    FirebaseFirestore.getInstance().collection(Datasets.USERS.path)
                        .document(user?.uid ?: "").get()
                        .addOnSuccessListener {
                            if (it.data == null) {
                                pinBinding.progress.visibility = View.INVISIBLE
                                findNavController().navigate(R.id.eperienceFragment)
                            } else {
                                pinBinding.progress.visibility = View.INVISIBLE
                                stopSpeakerClick?.click()
                            }
                        }
                        .addOnFailureListener {
                            pinBinding.progress.visibility = View.INVISIBLE
                            findNavController().navigate(R.id.eperienceFragment)
                        }
                } else {
                    Toast.makeText(context, "error code", Toast.LENGTH_SHORT).show()
                    // Sign in failed, display a message and update the UI
//                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    private fun verifyCode(code: String) {
        if (code.length == 6) {
            try {
                val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
                signInWithPhoneAuthCredential(credential)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addEtListeners() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        pinBinding.etOne.requestFocus()
        pinBinding.etOne.doAfterTextChanged {
            it?.let { editable ->
                if (editable.length == 1) {
                    pinBinding.etTwo.requestFocus()
                }
            }
        }

        pinBinding.etTwo.addTextChangedListener {
            it?.let { editable ->
                if (editable.length == 1) {
                    pinBinding.etThree.requestFocus()
                } else if (editable.isEmpty()) {
                    pinBinding.etOne.requestFocus()
                }
            }
        }

        pinBinding.etThree.addTextChangedListener {
            it?.let { editable ->
                if (editable.length == 1) {
                    pinBinding.etFour.requestFocus()
                } else if (editable.isEmpty()) {
                    pinBinding.etTwo.requestFocus()
                }
            }
        }

        pinBinding.etFour.addTextChangedListener {
            it?.let { editable ->
                if (editable.length == 1) {
                    pinBinding.etFive.requestFocus()
                } else if (editable.isEmpty()) {
                    pinBinding.etThree.requestFocus()
                }
            }
        }

        pinBinding.etFive.addTextChangedListener {
            it?.let { editable ->
                if (editable.length == 1) {
                    pinBinding.etSix.requestFocus()
                } else if (editable.isEmpty()) {
                    pinBinding.etFour.requestFocus()
                }
            }
        }

        pinBinding.etSix.addTextChangedListener {
            it?.let { editable ->
                if (editable.length == 1) {

                    val code = pinBinding.etOne.text.toString() +
                            pinBinding.etTwo.text.toString() +
                            pinBinding.etThree.text.toString() +
                            pinBinding.etFour.text.toString() +
                            pinBinding.etFive.text.toString() +
                            pinBinding.etSix.text.toString()
                    if (code.length === 6) {
                        verifyCode(code)
                    }

                } else if (editable.isEmpty()) {
                    pinBinding.etFive.requestFocus()
                }
            }
        }

        pinBinding.etFour.setOnKeyListener(View.OnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (keyCode == KeyEvent.KEYCODE_DEL && pinBinding.etFour.text!!.isEmpty()) {
                pinBinding.etThree.requestFocus()
            }
            false
        })
        pinBinding.etFive.setOnKeyListener(View.OnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (keyCode == KeyEvent.KEYCODE_DEL && pinBinding.etFour.text!!.isEmpty()) {
                pinBinding.etFour.requestFocus()
            }
            false
        })
        pinBinding.etFour.setOnKeyListener(View.OnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (keyCode == KeyEvent.KEYCODE_DEL && pinBinding.etFour.text!!.isEmpty()) {
                pinBinding.etThree.requestFocus()
            }
            false
        })
        pinBinding.etThree.setOnKeyListener(View.OnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (keyCode == KeyEvent.KEYCODE_DEL && pinBinding.etThree.text!!.isEmpty()) {
                pinBinding.etTwo.requestFocus()
            }
            false
        })
        pinBinding.etTwo.setOnKeyListener(View.OnKeyListener { v, keyCode, event -> //You can identify which key pressed buy checking keyCode value with KeyEvent.KEYCODE_
            if (keyCode == KeyEvent.KEYCODE_DEL && pinBinding.etTwo.text!!.isEmpty()) {
                pinBinding.etOne.requestFocus()
            }
            false
        })
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

    override fun onResume() {
        super.onResume()
        if (!languageCache.getLanguage()) setTextFonts()
    }

    private fun setTextFonts() {
        val typeface =
            ResourcesCompat.getFont(activity?.applicationContext!!, R.font.ge_dinar_one_medium)
        pinBinding.titleText.typeface = typeface
        pinBinding.skipBtn.typeface = typeface
    }
}