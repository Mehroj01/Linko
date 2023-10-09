package com.neb.linko.ui.store

import android.app.AlertDialog
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.neb.linko.App
import com.neb.linko.R
import com.neb.linko.api.Datasets
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.ActivityStoreBinding
import com.neb.linko.databinding.StoreCenterBarArBinding
import com.neb.linko.databinding.StoreCenterBarEnBinding
import com.neb.linko.models.PromoModel
import com.neb.linko.models.StoreModel
import com.neb.linko.models.User
import com.neb.linko.stats.StatsViewModel
import com.neb.linko.ui.profile.ProfileViewModel
import com.neb.linko.ui.ratings.RatingActivity
import com.neb.linko.ui.registerscreens.RegisterActivity
import com.neb.linko.ui.save.AddFavoriteStoreViewModel
import com.neb.linko.ui.store.adapters.ImagesAdapter
import com.neb.linko.ui.web.WebActivity
import com.squareup.picasso.Picasso
import rebus.permissionutils.PermissionEnum
import rebus.permissionutils.PermissionManager
import java.util.*
import javax.inject.Inject


class StoreActivity : AppCompatActivity() {
    lateinit var storeBinding: ActivityStoreBinding
    lateinit var imagesAdapter: ImagesAdapter

    @Inject
    lateinit var profileViewModel: ProfileViewModel

    @Inject
    lateinit var addFavoriteStoreViewModel: AddFavoriteStoreViewModel

    @Inject
    lateinit var db: FirebaseFirestore

    @Inject
    lateinit var statsViewModel: StatsViewModel

    lateinit var favorites: ArrayList<String>


    var uid: String = ""
    var user: User? = null
    var store: StoreModel? = null
    lateinit var languageCache: LanguageCache
    lateinit var dialogForPromo: DialogForPromo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storeBinding = ActivityStoreBinding.inflate(layoutInflater)
        setContentView(storeBinding.root)
        App.appComponent.storeActivity(this)

        favorites = ArrayList()

        languageCache = LanguageCache(getSharedPreferences("Base", MODE_PRIVATE)!!)

        store = Gson().fromJson(intent.getStringExtra("store"), StoreModel::class.java)
        statsViewModel.updateStats(store, "views", true) {}
        if (store?.key != null && store?.key != "") getRating(store?.key!!)

        storeBinding.apply {
            backBtn.setOnClickListener {
                finish()
            }

            if (store?.imagesList != null) {
                imagesAdapter = ImagesAdapter(store?.imagesList!!)
                rv.adapter = imagesAdapter
            }

            if (store?.bgImageUrl != null) {
                try {
                    Picasso.get().load(store?.bgImageUrl)
                        .into(mainImg)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            callBtn.text = if (languageCache.getLanguage()) "Call" else "اتصال"
            callBtn.setOnClickListener {
                val permissions: java.util.ArrayList<PermissionEnum> =
                    java.util.ArrayList<PermissionEnum>()
                permissions.add(PermissionEnum.CALL_PHONE)

                PermissionManager.Builder()
                    .permissions(permissions)
                    .askAgain(true)
                    .askAgainCallback {

                        AlertDialog.Builder(this@StoreActivity)
                            .setTitle("Linko")
                            .setMessage("Permissions are required")
                            .setPositiveButton("OK") { dialogInterface, i -> it.result(true) }
                            .setNegativeButton("Not Now") { dialogInterface, i -> it.result(false) }
                            .show()

                    }.callback { allPermissionsGranted, permissionsDeined ->

                        if (allPermissionsGranted) {
                            val phone = store?.phone
                            val phoneSec = store?.phoneSec

                            if ((phone != null || phone != "") && (phoneSec != null || phoneSec != "")) {
                                val builder = AlertDialog.Builder(this@StoreActivity)
                                    .setTitle("Call")
                                    .setMessage("Select an option")

                                if (phone != null && phone.isNotEmpty()) {
                                    builder.setPositiveButton(
                                        phone
                                    ) { _, _ ->
                                        val intent = Intent(Intent.ACTION_CALL)
                                        intent.data = Uri.parse("tel:${phone}")
                                        startActivity(intent)
                                    }
                                }

                                if (phoneSec != null && !phoneSec.isEmpty()) {
                                    builder.setNegativeButton(
                                        phoneSec
                                    ) { _, _ ->
                                        val intent = Intent(Intent.ACTION_CALL)
                                        intent.data = Uri.parse("tel:${phoneSec}")
                                        startActivity(intent)
                                    }
                                }
                                statsViewModel.updateStats(store, "phoneClicks", true) {}
                                builder.show()
                            }
                        }
                    }
                    .ask(this@StoreActivity)
            }


            whatsappBtn.text = if (languageCache.getLanguage()) "WhatsApp" else "الوتسب"
            whatsappBtn.setOnClickListener {
                store?.whatsapp?.let { whatsapp ->
                    val url = "https://api.whatsapp.com/send?phone=$whatsapp"
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(url)
                    startActivity(i)
                    statsViewModel.updateStats(store, "whatsappClicks", true) {}
                }
            }

            locationBtn.text = if (languageCache.getLanguage()) "Location" else "الموقع"
            locationBtn.setOnClickListener {
                val intent = Intent(this@StoreActivity, MapActivity::class.java)
                    .apply {
                        this.putExtra("location", Gson().toJson(store))
                    }
                statsViewModel.updateStats(store, "locationClicks", true) {}
                startActivity(intent)
            }

            ratingText.setOnClickListener {
                if (uid != "") {
                    val intent = Intent(this@StoreActivity, RatingActivity::class.java)
                        .apply {
                            this.putExtra("storeKey", store?.key)
                            this.putExtra("userKey", user?.key)
                            this.putExtra("storeUsName", store?.name)
                            this.putExtra("storeArName", store?.nameArabic)
                        }
                    startActivity(intent)
                } else {
                    startActivity(Intent(this@StoreActivity, RegisterActivity::class.java))
                }
            }

            storeBtn.setOnClickListener {
                if (store?.webUrl != null) {
                    statsViewModel.updateStats(store, "websiteClicks", true) {}
                    val intent = Intent(this@StoreActivity, WebActivity::class.java).apply {
                        this.putExtra("storeData", Gson().toJson(store))
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this@StoreActivity, "web url not available", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }

        permissions()

    }

    private fun permissions() {
        val permissions: java.util.ArrayList<PermissionEnum> =
            java.util.ArrayList<PermissionEnum>()
        permissions.add(PermissionEnum.CALL_PHONE)

        PermissionManager.Builder()
            .permissions(permissions)
            .askAgain(true)
            .askAgainCallback {

                AlertDialog.Builder(this@StoreActivity)
                    .setTitle("Linko")
                    .setMessage("Permissions are required")
                    .setPositiveButton("OK") { dialogInterface, i -> it.result(true) }
                    .setNegativeButton("Not Now") { dialogInterface, i -> it.result(false) }
                    .show()

            }.callback { allPermissionsGranted, permissionsDeined ->

            }
            .ask(this@StoreActivity)

        storeBinding.bookmarkBtn.setOnClickListener {
            if (uid == "") {
                startActivity(Intent(this, RegisterActivity::class.java))
            } else if (user != null) {
                var situation = true
                for (favorite in favorites) {
                    if (store?.key == favorite) {
                        situation = false
                        break
                    }
                }

                if (situation) {
                    storeBinding.progress.visibility = View.VISIBLE
                    val a = favorites
                    a.add(store?.key!!)
                    val b = user
                    b?.bookmarks = a
                    addFavoriteStoreViewModel.addUpdate(uid, user!!)
                        .observe(this, Observer {
                            storeBinding.progress.visibility = View.INVISIBLE
                            if (it) {
                                storeBinding.bookmarkBtn.setImageResource(R.drawable.bookmark_selected)
                                storeBinding.bookmarkBtn.setBackgroundResource(R.drawable.bookmark_bg_selected)
                                user = b
                            }
                        })
                    statsViewModel.updateStats(store, "bookmarkClicks", true) {}
                } else {
                    storeBinding.progress.visibility = View.VISIBLE
                    val a = favorites
                    a.remove(store?.key)
                    val b = user
                    b?.bookmarks = a
                    addFavoriteStoreViewModel.getUpdate(uid, user!!)
                        .observe(this, Observer {
                            storeBinding.progress.visibility = View.INVISIBLE
                            if (it) {
                                favorites.remove(store?.key)
                                user = b
                                storeBinding.bookmarkBtn.setImageResource(R.drawable.ic_bookmark_gray)
                                storeBinding.bookmarkBtn.setBackgroundResource(R.drawable.bookmark_bg)
                            }
                        })

                    statsViewModel.updateStats(store, "bookmarkClicks", false) {}
                }
            }
        }

    }

    private fun getFavorites() {
        profileViewModel.getAuth().observe(this, Observer { auth ->
            if (auth?.let { true } == true) {
                uid = auth.uid
                profileViewModel.getUser(auth.uid).observe(this, Observer {
                    if (it != null) user = it
                    if (it?.bookmarks != null) {
                        if (it.bookmarks?.isNotEmpty()!!) {
                            user = it
                            favorites.clear()
                            favorites.addAll(it.bookmarks!!)
                            for (favorite in favorites) {
                                if (favorite == store?.key) {
                                    storeBinding.bookmarkBtn.setImageResource(R.drawable.bookmark_selected)
                                    storeBinding.bookmarkBtn.setBackgroundResource(R.drawable.bookmark_bg_selected)
                                    break
                                }
                            }
                        }
                    }
                })
            }
        })
    }

    private fun getRating(storeKey: String) {
        db.collection(Datasets.RATINGS.path).document(storeKey).get()
            .addOnSuccessListener {

                if (it != null && it.data != null && it.data?.size != 0) {
                    var rating = 0F
                    it.data?.forEach { l ->
                        try {
                            rating += l.value.toString().toFloat()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    rating /= it.data?.size!!
                    storeBinding.ratingText.text = rating.toString()
                }
            }.addOnFailureListener {}
    }

    override fun onRestart() {
        super.onRestart()
        if (store?.key != null && store?.key != "") getRating(store?.key!!)
    }

    override fun onResume() {
        super.onResume()
        getFavorites()
        changeLanguage()
    }

    private fun changeLanguage() {
        val storeAvatarIc: ImageView
        val storeAvatar: CardView
        val storeNameLbl: AppCompatTextView
        val detailsLbl: AppCompatTextView
        val promoView: LinearLayoutCompat
        val promoLbl: AppCompatTextView
        if (languageCache.getLanguage()) {
            storeBinding.highlightLbl.gravity = Gravity.LEFT
            storeBinding.highlightLbl.text = "highlight"

            val centerLayout = StoreCenterBarEnBinding.inflate(LayoutInflater.from(this))

            storeAvatarIc = centerLayout.storeAvatarIc
            storeAvatar = centerLayout.storeAvatar
            storeNameLbl = centerLayout.storeNameLbl
            detailsLbl = centerLayout.detailsLbl
            promoView = centerLayout.promoView
            promoLbl = centerLayout.promoLbl
            centerLayout.promoTv.text = "Clain now"

            centerLayout.root.layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            storeBinding.centerBar.removeAllViews()
            storeBinding.centerBar.addView(centerLayout.root)

        } else {
            storeBinding.highlightLbl.gravity = Gravity.RIGHT
            storeBinding.highlightLbl.text = "الصور"

            val centerLayout = StoreCenterBarArBinding.inflate(LayoutInflater.from(this))

            storeAvatarIc = centerLayout.storeAvatarIc
            storeAvatar = centerLayout.storeAvatar
            storeNameLbl = centerLayout.storeNameLbl
            detailsLbl = centerLayout.detailsLbl
            promoView = centerLayout.promoView
            promoLbl = centerLayout.promoLbl
            centerLayout.promoTv.text = "استخدم الخصم"
            var p = centerLayout.promoTv

            centerLayout.root.layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            storeBinding.centerBar.removeAllViews()
            storeBinding.centerBar.addView(centerLayout.root)
            val typeface = ResourcesCompat.getFont(this, R.font.ge_dinar_one_medium)
            storeBinding.whatsappBtn.typeface = typeface
            storeBinding.locationBtn.typeface = typeface
            storeBinding.callBtn.typeface = typeface
            storeNameLbl.typeface = typeface
            detailsLbl.typeface = typeface
            promoLbl.typeface = typeface
            p.typeface = typeface
            storeBinding.highlightLbl.typeface = typeface
            storeBinding.saveTv.typeface = typeface
        }

        if (store?.imageUrl != null) {
            try {
                Picasso.get().load(store?.imageUrl)
                    .into(storeAvatarIc)
                storeAvatar.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        storeNameLbl.text =
            if (languageCache.getLanguage()) store?.name ?: "" else store?.nameArabic ?: ""
        detailsLbl.text =
            if (languageCache.getLanguage()) store?.details ?: "" else store?.detailsArabic ?: ""

        storeBinding.saveTv.text =
            if (languageCache.getLanguage()) "Go to Store" else "اذهب للمتجر"

        promoView.visibility = View.GONE
        storeBinding.discountLbl.visibility = View.GONE
        storeBinding.ratingText.text = store?.rating.let { it.toString() }

        store?.latestPromo?.let { promo ->
            if (promo.expirationTime == null || Date().time < promo.expirationTime ?: 0) {
                promoView.visibility = View.VISIBLE
                promo.promoDetails?.let { details ->
                    if (details.isEmpty()) {
                        storeBinding.discountLbl.visibility = View.GONE
                    } else {
                        storeBinding.discountLbl.visibility = View.VISIBLE
                        storeBinding.discountLbl.text = details
                    }
                }

                promoLbl.text = String.format("%d%% %s", promo.percentage?.let { it } ?: 0,
                    if (languageCache.getLanguage()) "OFF" else "خصم")
            }
        }

        promoView.setOnClickListener {
            storeBinding.progress.visibility = View.VISIBLE
            db.collection(Datasets.STORES.path).document(store?.key ?: "").get()
                .addOnSuccessListener {
                    storeBinding.progress.visibility = View.INVISIBLE
                    if (it != null) {
                        var storeModelForPromo: StoreModel? = null
                        try {
                            storeModelForPromo = it.toObject(StoreModel::class.java)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        if (storeModelForPromo != null) {
                            if (storeModelForPromo.latestPromo != null) {
                                if (storeModelForPromo.latestPromo?.claimsRequired ?: 0 <= storeModelForPromo.latestPromo?.claimsCount ?: 0 || (storeModelForPromo.latestPromo?.expirationTime != null && Date().time > storeModelForPromo.latestPromo?.expirationTime ?: 0)) {
                                    promoView.visibility = View.INVISIBLE
                                    storeBinding.discountLbl.visibility = View.GONE
                                    Toast.makeText(
                                        this,
                                        if (languageCache.getLanguage()) "promotion count is expired" else "انتهى عدد الترويج",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    showDialog(storeModelForPromo)
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    storeBinding.progress.visibility = View.INVISIBLE
                }
        }


        val linearLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, !languageCache.getLanguage())
        storeBinding.rv.layoutManager = linearLayoutManager

        imagesAdapter.notifyDataSetChanged()
    }

    private fun showDialog(storeForPromo: StoreModel) {
        dialogForPromo = DialogForPromo(this, object : DialogForPromo.MyClick {
            override fun skip() {
                dialogForPromo.dialogs.onBackPressed()
            }

            override fun openPromoStore() {
                dialogForPromo.dialogs.onBackPressed()
                if (store?.webUrl != null) {
                    val intent = Intent(this@StoreActivity, WebActivity::class.java).apply {
                        this.putExtra("storeData", Gson().toJson(store))
                    }
                    startActivity(intent)
                    statsViewModel.updateStats(store, "websiteClicks", true) {}
                    copyText(storeForPromo.latestPromo?.promoCode ?: "")
                    if (storeForPromo.latestPromo?.claimsCount != null) {
                        storeForPromo.latestPromo?.claimsCount =
                            storeForPromo.latestPromo?.claimsCount!! + 1
                    } else {
                        storeForPromo.latestPromo?.claimsCount = 1
                    }
                    updateStore(storeForPromo)
                } else {
                    Toast.makeText(this@StoreActivity, "web url not available", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }, languageCache.getLanguage())
        dialogForPromo.networkDialog()
    }

    private fun updateStore(storeForPromo: StoreModel) {
        val updateStore = HashMap<String, PromoModel?>()
        updateStore["latestPromo"] = storeForPromo.latestPromo
        db.collection(Datasets.STORES.path).document(storeForPromo.key ?: "")
            .update(updateStore as Map<String, Any>)
        val updatePromo = HashMap<String, Int?>()
        updatePromo["claimsCount"] = storeForPromo.latestPromo?.claimsCount
        db.collection(Datasets.PROMOS.path).document(storeForPromo.latestPromo?.key ?: "")
            .update(updatePromo as Map<String, Any>)
    }

    private fun copyText(text: String) {

        val clipboard =
            getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = ClipData.newPlainText("EditText", text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show()
    }
}