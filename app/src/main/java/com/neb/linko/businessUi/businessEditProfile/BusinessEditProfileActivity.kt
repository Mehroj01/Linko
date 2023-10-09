package com.neb.linko.businessUi.businessEditProfile

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.neb.linko.App
import com.neb.linko.R
import com.neb.linko.api.Datasets
import com.neb.linko.businessUi.businessData.BusinessViewModel
import com.neb.linko.databinding.ActivityBusinessEditProfileBinding
import com.squareup.picasso.Picasso
import rebus.permissionutils.PermissionEnum
import rebus.permissionutils.PermissionManager
import javax.inject.Inject

class BusinessEditProfileActivity : AppCompatActivity() {

    @Inject
    lateinit var businessViewModel: BusinessViewModel

    lateinit var businessEditProfileBinding: ActivityBusinessEditProfileBinding
    lateinit var reference: StorageReference
    lateinit var firebaseStorage: FirebaseStorage
    var enOrAr = true
    var enStoreName = ""
    var arStoreName = ""
    var enName = ""
    var enDesk = ""
    var arName = ""
    var arDesk = ""
    var imgUrl = ""
    var uid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        businessEditProfileBinding = ActivityBusinessEditProfileBinding.inflate(layoutInflater)
        setContentView(businessEditProfileBinding.root)
        App.appComponent.businessEditProfile(this)
        firebaseStorage = FirebaseStorage.getInstance()
        reference = firebaseStorage.getReference(Datasets.STORES.path)
        getMyStore()

        businessEditProfileBinding.backBtn.setOnClickListener {
            finish()
        }

        businessEditProfileBinding.englishBtn.setOnClickListener {
            enOrAr = true
            updateValues()
        }

        businessEditProfileBinding.arabicBtn.setOnClickListener {
            enOrAr = false
            updateValues()
        }

        getSituation()

        businessEditProfileBinding.saveBtn.setOnClickListener {
            if (enOrAr) {
                enName = businessEditProfileBinding.fullNameText.text.toString()
                enDesk = businessEditProfileBinding.fullDeckText.text.toString()
            } else {
                arName = businessEditProfileBinding.fullNameText.text.toString()
                arDesk = businessEditProfileBinding.fullDeckText.text.toString()
            }
            if (uid != "") {
                if (enName.trimStart() != "" && enDesk.trimStart() != "" && arName.trimStart() != "" && arDesk.trimStart() != "") {
                    businessEditProfileBinding.progress.visibility = View.VISIBLE

                    val updateUser = HashMap<String, String?>()
                    updateUser["name"] = enName
                    updateUser["nameArabic"] = arName
                    updateUser["details"] = enDesk
                    updateUser["detailsArabic"] = arDesk
                    updateUser["imageUrl"] = imgUrl

                    FirebaseFirestore.getInstance().collection(Datasets.STORES.path)
                        .document(uid)
                        .update(updateUser as Map<String, String>)
                        .addOnSuccessListener {
                            businessEditProfileBinding.progress.visibility = View.INVISIBLE
                            finish()
                        }
                        .addOnFailureListener {
                            businessEditProfileBinding.progress.visibility = View.VISIBLE
                            finish()
                            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        businessEditProfileBinding.profileImage.setOnClickListener {
            if (uid != "") {
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
        }

    }

    private fun updateValues() {
        if (enOrAr) {
            arName = businessEditProfileBinding.fullNameText.text.toString()
            arDesk = businessEditProfileBinding.fullDeckText.text.toString()
        } else {
            enName = businessEditProfileBinding.fullNameText.text.toString()
            enDesk = businessEditProfileBinding.fullDeckText.text.toString()
        }
        getSituation()
    }

    private fun newMethodImage() {
        getImageContent.launch("image/*")
    }

    private var getImageContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uid != "") {
                businessEditProfileBinding.progress.visibility = View.VISIBLE
                if (uri == null) {
                    businessEditProfileBinding.progress.visibility = View.INVISIBLE
                    return@registerForActivityResult
                }
                val m = System.currentTimeMillis()
                val uploadTask = reference.child(uid).child(m.toString()).putFile(uri)

                uploadTask.addOnSuccessListener {
                    if (it.task.isSuccessful) {
                        val downloadUrl = it.metadata?.reference?.downloadUrl

                        downloadUrl?.addOnSuccessListener { imguri ->
                            imgUrl = imguri.toString()
                            businessEditProfileBinding.profileImage.setImageURI(uri)
                        }
                    }
                    businessEditProfileBinding.progress.visibility = View.INVISIBLE

                }
                uploadTask.addOnFailureListener {
                    businessEditProfileBinding.progress.visibility = View.INVISIBLE
                }
            }
        }

    private fun getMyStore() {
        businessEditProfileBinding.progress.visibility = View.VISIBLE
        businessViewModel.getMyStore(this).observe(this, Observer {
            if (it != null) {
                enStoreName = it.name ?: ""
                arStoreName = it.nameArabic ?: ""
                enName = it.name ?: ""
                arName = it.nameArabic ?: ""
                enDesk = it.details ?: ""
                arDesk = it.detailsArabic ?: ""
                imgUrl = it.imageUrl ?: ""
                try {
                    Picasso.get().load(it.imageUrl).into(businessEditProfileBinding.profileImage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                uid = it.key ?: ""
                getSituation()
            }
            businessEditProfileBinding.progress.visibility = View.INVISIBLE
        })
    }

    private fun getSituation() {
        businessEditProfileBinding.apply {
            if (enOrAr) {
                username.text = enStoreName
                fullNameText.setText(enName)
                fullDeckText.setText(enDesk)
                englishTv.setTextColor(resources.getColor(R.color.primary))
                englishBtn.setBackgroundResource(R.drawable.category_item_bg)
                arabicTv.setTextColor(resources.getColor(R.color.av_gray))
                arabicBtn.setBackgroundResource(R.drawable.category_default_item_bg)
            } else {
                username.text = arStoreName
                fullNameText.setText(arName)
                fullDeckText.setText(arDesk)
                arabicTv.setTextColor(resources.getColor(R.color.primary))
                arabicBtn.setBackgroundResource(R.drawable.category_item_bg)
                englishTv.setTextColor(resources.getColor(R.color.av_gray))
                englishBtn.setBackgroundResource(R.drawable.category_default_item_bg)
            }
        }
    }
}