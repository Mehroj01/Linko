package com.neb.linko.businessUi.storePhotos

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
import com.neb.linko.databinding.ActivityStorePhotosBinding
import com.squareup.picasso.Picasso
import rebus.permissionutils.PermissionEnum
import rebus.permissionutils.PermissionManager
import javax.inject.Inject

class StorePhotosActivity : AppCompatActivity() {

    @Inject
    lateinit var businessViewModel: BusinessViewModel

    lateinit var storePhotosBinding: ActivityStorePhotosBinding
    lateinit var photosAdapter: PhotosAdapter
    lateinit var photosList: ArrayList<PhotosModel>
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference: StorageReference
    var mainBgUrl: String = ""
    var storeKey = ""
    var type: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storePhotosBinding = ActivityStorePhotosBinding.inflate(layoutInflater)
        setContentView(storePhotosBinding.root)
        App.appComponent.storePhotosActivity(this)

        firebaseStorage = FirebaseStorage.getInstance()
        reference = firebaseStorage.getReference(Datasets.STORES.path)

        photosList = ArrayList()
        photosList.add(PhotosModel(0, ""))
        photosList.add(PhotosModel(1, ""))
        photosList.add(PhotosModel(2, ""))
        photosList.add(PhotosModel(3, ""))
        photosList.add(PhotosModel(4, ""))

        photosAdapter = PhotosAdapter(photosList, object : PhotosAdapter.PhotosClick {
            override fun photoClick(photosModel: PhotosModel) {
                type = photosModel.id
                requiredPermission()
            }
        })

        storePhotosBinding.rv.adapter = photosAdapter

        storePhotosBinding.backBtn.setOnClickListener {
            finish()
        }

        storePhotosBinding.storeBg.setOnClickListener {
            type = -1
            requiredPermission()
        }

        storePhotosBinding.saveBtn.setOnClickListener {
            savePhotos()
        }

        getStorePhotos()

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

    private fun savePhotos() {
        if (storeKey != "") {
            storePhotosBinding.progress.visibility = View.VISIBLE
            val list = ArrayList<String>()
            for (photosModel in photosList) {
                if (photosModel.photoUrl != null && photosModel.photoUrl != "") {
                    list.add(photosModel.photoUrl!!)
                }
            }

            val updateUser = HashMap<String, Any?>()
            updateUser["bgImageUrl"] = mainBgUrl
            updateUser["imagesList"] = list

            FirebaseFirestore.getInstance().collection(Datasets.STORES.path)
                .document(storeKey)
                .update(updateUser as Map<String, Any>)
                .addOnSuccessListener {
                    storePhotosBinding.progress.visibility = View.INVISIBLE
                    finish()
                }
                .addOnFailureListener {
                    storePhotosBinding.progress.visibility = View.VISIBLE
                    finish()
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun mainImageContent() {
        getMainImageContent.launch("image/*")
    }

    private var getMainImageContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (storeKey != "") {
                storePhotosBinding.progress.visibility = View.VISIBLE
                if (uri == null) {
                    storePhotosBinding.progress.visibility = View.INVISIBLE
                    return@registerForActivityResult
                }
                val m = System.currentTimeMillis()
                val uploadTask = reference.child(storeKey).child(m.toString()).putFile(uri)

                uploadTask.addOnSuccessListener {
                    if (it.task.isSuccessful) {
                        val downloadUrl = it.metadata?.reference?.downloadUrl

                        downloadUrl?.addOnSuccessListener { imguri ->
                            if (type != null && type == -1) {
                                mainBgUrl = imguri.toString()
                                storePhotosBinding.storeBg.setImageURI(uri)
                            } else if (type != null) {
                                photosList[type!!].photoUrl = imguri.toString()
                                photosAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    storePhotosBinding.progress.visibility = View.INVISIBLE

                }
                uploadTask.addOnFailureListener {
                    storePhotosBinding.progress.visibility = View.INVISIBLE
                }
            }
        }

    private fun getStorePhotos() {
        storePhotosBinding.progress.visibility = View.VISIBLE
        businessViewModel.getMyStore(this).observe(this, Observer {
            if (it != null) {
                storeKey = it.key ?: ""
                if (it.imagesList != null) {
                    for (i in 0 until it.imagesList?.size!!) {
                        if (i < 5) {
                            photosList[i] = PhotosModel(i, it.imagesList!![i])
                        }
                    }
                }
                mainBgUrl = it.bgImageUrl ?: ""
                photosAdapter.notifyDataSetChanged()
                if (mainBgUrl != "") {
                    storePhotosBinding.plusTv.visibility = View.INVISIBLE
                    storePhotosBinding.storeBgInfo.visibility = View.INVISIBLE
                    try {
                        Picasso.get().load(mainBgUrl)
                            .into(storePhotosBinding.storeBg)
                    } catch (e: Exception) {
                        storePhotosBinding.plusTv.visibility = View.VISIBLE
                        storePhotosBinding.storeBgInfo.visibility = View.VISIBLE
                        e.printStackTrace()
                    }
                } else {
                    storePhotosBinding.plusTv.visibility = View.VISIBLE
                    storePhotosBinding.storeBgInfo.visibility = View.VISIBLE
                }
            }
            storePhotosBinding.progress.visibility = View.INVISIBLE
        })
    }
}