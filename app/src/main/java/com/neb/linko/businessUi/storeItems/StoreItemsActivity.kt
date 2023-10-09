package com.neb.linko.businessUi.storeItems

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.neb.linko.App
import com.neb.linko.R
import com.neb.linko.api.Datasets
import com.neb.linko.businessUi.storeItems.adapters.StoreItemsAdapter
import com.neb.linko.databinding.ActivityStoreItemsBinding
import com.neb.linko.databinding.CreateStoreBottomDialogBinding
import com.neb.linko.databinding.CreateStoreItemDialogBinding
import com.neb.linko.models.ItemModel
import com.squareup.picasso.Picasso
import rebus.permissionutils.PermissionEnum
import rebus.permissionutils.PermissionManager
import javax.inject.Inject

class StoreItemsActivity : AppCompatActivity() {

    @Inject
    lateinit var itemsViewModel: ItemsViewModel

    var userKey = ""

    lateinit var storeItemsBinding: ActivityStoreItemsBinding
    lateinit var myItems: ArrayList<ItemModel>
    lateinit var storeItemsAdapter: StoreItemsAdapter
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var reference: StorageReference
    var itemImageUri: String? = null
    var view: CreateStoreItemDialogBinding? = null
    var enOrAr = true
    var enName = ""
    var enDesk = ""
    var arName = ""
    var arDesk = ""
    var editItemKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storeItemsBinding = ActivityStoreItemsBinding.inflate(layoutInflater)
        setContentView(storeItemsBinding.root)
        App.appComponent.storeItemsActivity(this)

        userKey = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        firebaseStorage = FirebaseStorage.getInstance()
        reference = firebaseStorage.getReference(Datasets.STORES.path)

        storeItemsBinding.backBtn.setOnClickListener {
            finish()
        }

        storeItemsBinding.addItemBtn.setOnClickListener {
            editItemKey = null
            createItemShowDialog()
        }

        myItems = ArrayList()
        storeItemsAdapter = StoreItemsAdapter(this, myItems, object : StoreItemsAdapter.ItemClick {
            override fun editItem(itemModel: ItemModel) {
                itemEditOption(itemModel)
            }

            override fun deleteClick(key: String) {
                val builder = AlertDialog.Builder(this@StoreItemsActivity)
                    .setMessage("Do you want to delete the item?")

                builder.setPositiveButton("close") { _, _ ->

                }

                builder.setNegativeButton("delete") { _, _ ->
                    itemsViewModel.deleteItem(key) {
                        getMyItems()
                    }
                }
                builder.show()
            }
        })
        storeItemsBinding.rv.adapter = storeItemsAdapter

        getMyItems()

        showBottomSheetDialog()

    }

    private fun itemEditOption(i: ItemModel) {
        createItemShowDialog()
        enName = i.name ?: ""
        enDesk = i.details ?: ""
        arName = i.nameArabic ?: ""
        arDesk = i.detailsArabic ?: ""
        itemImageUri = i.imageUrl
        editItemKey = i.key
        if (view != null) {
            if (itemImageUri != null) {
                try {
                    Picasso.get().load(itemImageUri)
                        .into(view?.image)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            view?.price?.setText("${i.price}")
        }
        getSituation()
    }

    private fun createItemShowDialog() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetTheme)
        view = CreateStoreItemDialogBinding.inflate(LayoutInflater.from(this), null, false)
        view?.addCloseBtn?.setOnClickListener {
            dialog.dismiss()
        }

        view?.imageCard?.setOnClickListener {
            requiredPermission()
        }

        view?.englishBtn?.setOnClickListener {
            enOrAr = true
            updateValues()
        }

        view?.arabicBtn?.setOnClickListener {
            enOrAr = false
            updateValues()
        }
        view?.closeBtn?.setOnClickListener {
            dialog.dismiss()
        }

        view?.saveBtn?.setOnClickListener {
            if (enOrAr) {
                enName = view?.itemTitle?.text.toString()
                enDesk = view?.description?.text.toString()
            } else {
                arName = view?.itemTitle?.text.toString()
                arDesk = view?.description?.text.toString()
            }
//            Toast.makeText(this, "$enName , $enDesk , $arName , $arDesk", Toast.LENGTH_SHORT).show()
            if (itemImageUri != null && enName.trimStart() != "" && enDesk.trimStart() != "" && arName.trimStart() != "" && arDesk.trimStart() != "" && view?.price?.text.toString()
                    .trimStart() != ""
            ) {
                view?.progress?.visibility = View.VISIBLE
                if (editItemKey == null) {
                    itemsViewModel.addItem(
                        enName,
                        enDesk,
                        arName,
                        arDesk,
                        itemImageUri ?: "",
                        view?.price?.text.toString().toFloat()
                    ) { finished(it) }
                } else {
                    itemsViewModel.editItem(
                        editItemKey ?: "",
                        enName,
                        enDesk,
                        arName,
                        arDesk,
                        itemImageUri ?: "",
                        view?.price?.text.toString().toFloat()
                    ) { finished(it) }
                }
            } else {
                Toast.makeText(this, "the data is incomplete", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setContentView(view?.root!!)
        dialog.show()
        getSituation()
    }

    private fun finished(it: Boolean?) {
        view?.progress?.visibility = View.INVISIBLE
        if (it == null) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        } else {
            view?.mainLayout?.visibility = View.INVISIBLE
            view?.successView?.visibility = View.VISIBLE
            itemImageUri = null
            enName = ""
            enDesk = ""
            arName = ""
            arDesk = ""
            enOrAr = true
            view = null
            getMyItems()
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
            if (userKey != "" && view != null) {
                view?.progress?.visibility = View.VISIBLE
                if (uri == null) {
                    view?.progress?.visibility = View.INVISIBLE
                    return@registerForActivityResult
                }
                val m = System.currentTimeMillis()
                val uploadTask = reference.child(userKey).child(m.toString()).putFile(uri)

                uploadTask.addOnSuccessListener {
                    if (it.task.isSuccessful) {
                        val downloadUrl = it.metadata?.reference?.downloadUrl

                        downloadUrl?.addOnSuccessListener { imguri ->
                            itemImageUri = imguri.toString()
                            view?.image?.setImageURI(uri)
                        }
                    }
                    view?.progress?.visibility = View.INVISIBLE

                }
                uploadTask.addOnFailureListener {
                    view?.progress?.visibility = View.INVISIBLE
                }
            }
        }

    private fun getMyItems() {
        storeItemsBinding.progress.visibility = View.VISIBLE
        itemsViewModel.getMyItems(this).observe(this, Observer {
            if (it != null) {
                myItems.clear()
                myItems.addAll(it)
                storeItemsAdapter.notifyDataSetChanged()
            }
            storeItemsBinding.progress.visibility = View.INVISIBLE
        })
    }

    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetTheme)
        val view = CreateStoreBottomDialogBinding.inflate(LayoutInflater.from(this), null, false)
        view.createStoreDismissDialog.setOnClickListener {
            dialog.dismiss()
        }

        view.createStoreDismissDialog.text = "NEXT"
        view.createStoreInfo.text = "Search is the most commonly used\n" +
                "feature by customers to find what\n" +
                "they are looking for.\n\nAdding what items you provide will\n" +
                "attract a lot more attention to your store!"

        view.createStoreTitle.text = "Tip!"

        view.titleImg.setImageResource(R.drawable.ic_dialog_tip)

        dialog.setContentView(view.root)
        dialog.show()
    }

    private fun getSituation() {
        view?.apply {
            if (enOrAr) {
                itemTitle.setText(enName)
                description.setText(enDesk)
                englishBtn.setTextColor(resources.getColor(R.color.primary))
                englishBtn.setBackgroundResource(R.drawable.category_item_bg)
                arabicBtn.setTextColor(resources.getColor(R.color.av_gray))
                arabicBtn.setBackgroundResource(R.drawable.category_default_item_bg)
            } else {
                itemTitle.setText(arName)
                description.setText(arDesk)
                arabicBtn.setTextColor(resources.getColor(R.color.primary))
                arabicBtn.setBackgroundResource(R.drawable.category_item_bg)
                englishBtn.setTextColor(resources.getColor(R.color.av_gray))
                englishBtn.setBackgroundResource(R.drawable.category_default_item_bg)
            }
        }
    }

    private fun updateValues() {
        if (enOrAr) {
            arName = view?.itemTitle?.text.toString()
            arDesk = view?.description?.text.toString()
        } else {
            enName = view?.itemTitle?.text.toString()
            enDesk = view?.description?.text.toString()
        }
        getSituation()
    }
}