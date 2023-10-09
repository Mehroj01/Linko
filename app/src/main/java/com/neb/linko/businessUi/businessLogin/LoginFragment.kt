package com.neb.linko.businessUi.businessLogin

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.neb.linko.R
import com.neb.linko.api.Datasets
import com.neb.linko.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    lateinit var loginBinding: FragmentLoginBinding
    var passwordShow = false
    var uid: String? = null
    var storeKey: String? = null
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        loginBinding = FragmentLoginBinding.inflate(inflater, container, false)

        sharedPreferences = activity?.getSharedPreferences("Base", MODE_PRIVATE)!!
        editor = sharedPreferences.edit()

        uid = arguments?.getString("userUid")
        storeKey = arguments?.getString("storeKey")

        loginBinding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        loginBinding.passwordShow.setOnClickListener {
            if (!passwordShow) {
                loginBinding.passwordText.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                passwordShow = true
            } else {
                loginBinding.passwordText.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                passwordShow = false
            }
            val pos = loginBinding.passwordText.text?.length
            loginBinding.passwordText.setSelection(pos ?: 0)
        }

        loginBinding.apply {
            loginBtn.setOnClickListener {
                closedKeyboard()
                if (emailText.text?.trimStart().toString() != "" && passwordText.text?.trimStart()
                        .toString() != ""
                ) {
                    loginStore(passwordText.text.toString(), emailText.text.toString()) {
                        if (it) {
                            openBusinessActivity()
                        } else {
                            Toast.makeText(context, "information error", Toast.LENGTH_SHORT).show()
                        }
                        loginBinding.progress.visibility = View.INVISIBLE
                    }
                } else {
                    Toast.makeText(context, "the data is incomplete", Toast.LENGTH_SHORT).show()
                }
            }
        }

        loginBinding.registerBtn.setOnClickListener {
            val url = "https://api.whatsapp.com/send?phone=+96560650544"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        loginBinding.contactBtn.setOnClickListener {
            val url = "https://api.whatsapp.com/send?phone=+96560650544"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        return loginBinding.root
    }

    private fun openBusinessActivity() {
        editor.putBoolean("openBusiness", true)
        editor.commit()
        findNavController().navigate(R.id.businessProfileFragment)
    }

    private fun loginStore(password: String, email: String, callback: (Boolean) -> Unit) {
        loginBinding.progress.visibility = View.VISIBLE
        FirebaseFirestore.getInstance().collection(Datasets.STORES_LOGIN.path)
            .whereEqualTo("userKey", uid)
            .whereEqualTo("storeKey", storeKey)
            .whereEqualTo("email", email)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    callback(false)
                } else {
                    callback(true)
                }
            }.addOnFailureListener {
                callback(false)
            }
    }

    private fun closedKeyboard() {
        val view = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}