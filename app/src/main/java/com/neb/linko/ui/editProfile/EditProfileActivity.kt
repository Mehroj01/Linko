package com.neb.linko.ui.editProfile

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.neb.linko.R
import com.neb.linko.api.Datasets
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.ActivityEditProfileBinding
import com.neb.linko.models.User
import com.squareup.picasso.Picasso
import rebus.permissionutils.PermissionEnum
import rebus.permissionutils.PermissionManager

class EditProfileActivity : AppCompatActivity() {

    lateinit var editProfileBinding: ActivityEditProfileBinding
    lateinit var reference: StorageReference
    lateinit var firebaseStorage: FirebaseStorage
    var user: User? = null
    lateinit var languageCache: LanguageCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editProfileBinding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(editProfileBinding.root)
        firebaseStorage = FirebaseStorage.getInstance()
        reference = firebaseStorage.getReference("users")
        languageCache = LanguageCache(getSharedPreferences("Base", MODE_PRIVATE)!!)

        editProfileBinding.backBtn.setOnClickListener {
            finish()
        }

        user = Gson().fromJson(intent.getStringExtra("profile"), User::class.java)
        if (user != null) setValues(user!!)

        editProfileBinding.profileImage.setOnClickListener {
            val permissions: ArrayList<PermissionEnum> = ArrayList<PermissionEnum>()
            permissions.add(PermissionEnum.READ_EXTERNAL_STORAGE)

            PermissionManager.Builder()
                .permissions(permissions)
                .askAgain(true)
                .askAgainCallback {

                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.app_name))
                        .setMessage("Permissions are required")
                        .setPositiveButton("Ok") { dialogInterface, i -> it.result(true) }
                        .setNegativeButton("Not Now") { dialogInterface, i -> it.result(false) }
                        .show()

                }.callback { allPermissionsGranted, permissionsDeined ->

                    if (allPermissionsGranted) {
                        newMethodImage()
                    } else {
                        Toast.makeText(this, "Permissions are required!!!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .ask(this)
        }

        editProfileBinding.saveBtn.setOnClickListener {
            if (user != null) {
                editProfileBinding.progress.visibility = View.VISIBLE
                if (editProfileBinding.fullNameText.text.toString() != "") user?.name =
                    editProfileBinding.fullNameText.text.toString()
                val updateUser = HashMap<String, String?>()
                updateUser["name"] = user?.name
                updateUser["imagePath"] = user?.imagePath
                updateUser["imageUrl"] = user?.imageUrl
                FirebaseFirestore.getInstance().collection(Datasets.USERS.path)
                    .document(user?.key ?: "")
                    .update(updateUser as Map<String, String>)
                    .addOnSuccessListener {
                        editProfileBinding.progress.visibility = View.INVISIBLE
                        finish()
                    }
                    .addOnFailureListener {
                        editProfileBinding.progress.visibility = View.VISIBLE
                        finish()
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                    }

            }
        }
    }

    private fun newMethodImage() {
        getImageContent.launch("image/*")
    }

    private var getImageContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            editProfileBinding.progress.visibility = View.VISIBLE
            if (uri == null) {
                editProfileBinding.progress.visibility = View.INVISIBLE
                return@registerForActivityResult
            }
            val m = System.currentTimeMillis()
            val uploadTask = reference.child(m.toString()).putFile(uri)

            uploadTask.addOnSuccessListener {
                if (it.task.isSuccessful) {
                    val downloadUrl = it.metadata?.reference?.downloadUrl

                    downloadUrl?.addOnSuccessListener { imguri ->
                        user?.imagePath = imguri.path
                        user?.imageUrl = imguri.toString()
                        editProfileBinding.profileImage.setImageURI(uri)
                    }
                }
                editProfileBinding.progress.visibility = View.INVISIBLE

            }
            uploadTask.addOnFailureListener {
                editProfileBinding.progress.visibility = View.INVISIBLE
            }
        }

    private fun setValues(l: User) {
        if (l.imageUrl != null) {
            try {
                Picasso.get().load(l.imageUrl)
                    .into(editProfileBinding.profileImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (l.name != null && l.name != "") {
            editProfileBinding.username.text = l.name
        }
        changeLanguage()
    }

    override fun onResume() {
        super.onResume()
        changeLanguage()
    }

    private fun changeLanguage() {
        editProfileBinding.apply {
            var n = ""
            if (languageCache.getLanguage()) {
                topTitle.text = "User Details"
                editProfileBinding.fullNameText.textDirection = View.TEXT_DIRECTION_LTR
                editProfileBinding.fullNameText.hint = "Enter Name"
                editProfileBinding.nameIndicator.text = "Name"
                editProfileBinding.saveBtn.text = "Save"
                n = "Your Name"
            } else {
                topTitle.text = "تفاصيل العضوية"
                editProfileBinding.fullNameText.textDirection = View.TEXT_DIRECTION_RTL
                editProfileBinding.fullNameText.hint = "ادخل اسمك"
                editProfileBinding.nameIndicator.text = "الاسم"
                editProfileBinding.saveBtn.text = "حفظ"
                n = "اسمك هنا"
                setTextFonts()
            }
            if (user?.name != null && user?.name != "") {
                username.text = user?.name
            } else {
                username.text = n
            }
        }
    }

    private fun setTextFonts() {
        val typeface = ResourcesCompat.getFont(this, R.font.ge_dinar_one_medium)
        editProfileBinding.topTitle.typeface = typeface
        editProfileBinding.fullNameText.typeface = typeface
        editProfileBinding.nameIndicator.typeface = typeface
        editProfileBinding.saveBtn.typeface = typeface
        editProfileBinding.username.typeface = typeface
    }

}