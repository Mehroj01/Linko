package com.neb.linko.businessUi.boosts

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.neb.linko.R
import com.neb.linko.api.Datasets
import com.neb.linko.databinding.ActivityBannerImgBinding
import rebus.permissionutils.PermissionEnum
import rebus.permissionutils.PermissionManager

class BannerImgActivity : AppCompatActivity() {

    lateinit var bannerImgBinding: ActivityBannerImgBinding
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference: StorageReference
    var mainBgUrl: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bannerImgBinding = ActivityBannerImgBinding.inflate(layoutInflater)
        setContentView(bannerImgBinding.root)

        sharedPreferences = getSharedPreferences("Base", MODE_PRIVATE)
        editor = sharedPreferences.edit()

        firebaseStorage = FirebaseStorage.getInstance()
        reference = firebaseStorage.getReference(Datasets.BANNERS.path)


        bannerImgBinding.bannerBg.setOnClickListener {
            requiredPermission()
        }

        bannerImgBinding.backBtn.setOnClickListener {
            finish()
        }

        bannerImgBinding.uploadBtn.setOnClickListener {
            if (mainBgUrl != "") {
                finish()
                editor.putString("BannerUri", mainBgUrl)
                editor.commit()
            } else {
                Toast.makeText(this, "banner image not selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun requiredPermission() {
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
                    mainImageContent()
                } else {
                    Toast.makeText(this, "Permissions are required!!!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .ask(this)
    }

    private fun mainImageContent() {
        getMainImageContent.launch("image/*")
    }

    private var getMainImageContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            bannerImgBinding.progress.visibility = View.VISIBLE
            if (uri == null) {
                bannerImgBinding.progress.visibility = View.INVISIBLE
                return@registerForActivityResult
            }
            val m = System.currentTimeMillis()
            val uploadTask = reference.child(m.toString()).putFile(uri)

            uploadTask.addOnSuccessListener {
                if (it.task.isSuccessful) {
                    val downloadUrl = it.metadata?.reference?.downloadUrl

                    downloadUrl?.addOnSuccessListener { imguri ->
                        mainBgUrl = imguri.toString()
                        bannerImgBinding.bannerBg.setImageURI(uri)
                        bannerImgBinding.plusTv.visibility = View.INVISIBLE
                        bannerImgBinding.bannerBgInfo.visibility = View.INVISIBLE
                    }
                }
                bannerImgBinding.progress.visibility = View.INVISIBLE

            }
            uploadTask.addOnFailureListener {
                bannerImgBinding.progress.visibility = View.INVISIBLE
            }
        }
}