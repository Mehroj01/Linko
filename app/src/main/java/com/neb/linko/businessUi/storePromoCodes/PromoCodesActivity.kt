package com.neb.linko.businessUi.storePromoCodes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.neb.linko.App
import com.neb.linko.R
import com.neb.linko.businessUi.storePromoCodes.adapters.PromoAdapter
import com.neb.linko.databinding.ActivityPromoCodesBinding
import com.neb.linko.databinding.CreatePromoDialogBinding
import com.neb.linko.databinding.CreateStoreBottomDialogBinding
import com.neb.linko.models.PromoModel
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class PromoCodesActivity : AppCompatActivity() {

    @Inject
    lateinit var promoCodeViewModel: PromoCodesViewModel

    lateinit var promoCodesBinding: ActivityPromoCodesBinding

    lateinit var myPromosList: ArrayList<PromoModel>

    lateinit var promoAdapter: PromoAdapter

    var view: CreatePromoDialogBinding? = null

    var dayCount = 3
    var requiredCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        promoCodesBinding = ActivityPromoCodesBinding.inflate(layoutInflater)
        setContentView(promoCodesBinding.root)
        App.appComponent.promoCodesActivity(this)

        promoCodesBinding.backBtn.setOnClickListener {
            finish()
        }

        promoCodesBinding.createPromoBtn.setOnClickListener {
            createCodeShowDialog(false, null)
        }

        myPromosList = ArrayList()
        promoAdapter = PromoAdapter(myPromosList, object : PromoAdapter.EditPromoCodeClick {
            override fun click(promoModel: PromoModel) {
                createCodeShowDialog(true, promoModel)
            }
        })
        promoCodesBinding.rv.adapter = promoAdapter

        getMyPromos()

        showBottomSheetDialog()
    }

    private fun createCodeShowDialog(edit: Boolean, promoModel: PromoModel?) {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetTheme)
        view = CreatePromoDialogBinding.inflate(LayoutInflater.from(this), null, false)
        view?.addCloseBtn?.setOnClickListener {
            dialog.dismiss()
        }

        view?.closeBtn?.setOnClickListener {
            dialog.dismiss()
        }

        view?.oneDay?.setOnClickListener {
            dayClick(1)
        }
        view?.twoDay?.setOnClickListener {
            dayClick(2)
        }
        view?.threeDay?.setOnClickListener {
            dayClick(3)
        }

        view?.minusBtn?.setOnClickListener {
            if (requiredCount > 0) {
                requiredCount -= 1
                view?.requiredCountEt?.setText("$requiredCount")
            }
        }

        view?.plusBtn?.setOnClickListener {
            requiredCount += 1
            view?.requiredCountEt?.setText("$requiredCount")
        }

        view?.requiredCountEt?.addTextChangedListener {
            requiredCount = if (it.toString() != "") {
                it.toString().toInt()
            } else {
                0
            }
        }

        view?.saveBtn?.setOnClickListener {
            val promoCode = view?.promoCodeEt?.text.toString()
            val percent = view?.percentEt?.text.toString()
            val desc = view?.descriptionEt?.text.toString()
            if (requiredCount != 0 && promoCode.trimStart() != "" && percent.trimStart() != "" && desc.trimStart() != "") {
                if (percent.toInt() != 0) {
                    view?.progress?.visibility = View.VISIBLE
                    if (edit) {
                        val newPromo = promoModel
                        newPromo?.promoDetails = desc
                        newPromo?.percentage = percent.toInt()
                        newPromo?.status = true
                        newPromo?.expiry = dayCount
                        newPromo?.expirationTime = Date().time + (dayCount * 1000 * 60 * 60 * 24)
                        newPromo?.promoCode = promoCode
                        newPromo?.claimsRequired = requiredCount

                        promoCodeViewModel.edit(newPromo) { finished(it) }
                    } else {
                        promoCodeViewModel.createPromoCode(
                            desc,
                            promoCode,
                            dayCount,
                            percent.toInt(),
                            requiredCount
                        ) { finished(it) }
                    }
                } else {
                    Toast.makeText(this, "percent should not be equal to 0", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(this, "the data is incomplete", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.setContentView(view?.root!!)
        dialog.show()

        if (edit) {
            dayClick(promoModel?.expiry ?: 1)
            requiredCount = promoModel?.claimsRequired ?: 0
            view?.requiredCountEt?.setText("$requiredCount")
            view?.promoCodeEt?.setText("${promoModel?.promoCode}")
            view?.percentEt?.setText("${promoModel?.percentage ?: 0}")
            view?.descriptionEt?.setText("${promoModel?.promoDetails}")
        }
    }

    private fun finished(it: Boolean?) {
        view?.progress?.visibility = View.INVISIBLE
        if (it == null) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        } else {
            view?.mainLayout?.visibility = View.INVISIBLE
            view?.successView?.visibility = View.VISIBLE
            dayCount = 3
            view = null
            requiredCount = 0
            getMyPromos()
        }
    }

    private fun dayClick(i: Int) {
        dayCount = i
        view?.apply {
            when (i) {
                1 -> {
                    oneDay.setTextColor(resources.getColor(R.color.white))
                    twoDay.setTextColor(resources.getColor(R.color.av_gray))
                    threeDay.setTextColor(resources.getColor(R.color.av_gray))
                    oneDay.setBackgroundResource(R.drawable.days_btn__true_bg)
                    twoDay.setBackgroundResource(R.drawable.days_btn__false_bg)
                    threeDay.setBackgroundResource(R.drawable.days_btn__false_bg)
                }
                2 -> {
                    twoDay.setTextColor(resources.getColor(R.color.white))
                    oneDay.setTextColor(resources.getColor(R.color.av_gray))
                    threeDay.setTextColor(resources.getColor(R.color.av_gray))
                    twoDay.setBackgroundResource(R.drawable.days_btn__true_bg)
                    oneDay.setBackgroundResource(R.drawable.days_btn__false_bg)
                    threeDay.setBackgroundResource(R.drawable.days_btn__false_bg)
                }
                3 -> {
                    threeDay.setTextColor(resources.getColor(R.color.white))
                    twoDay.setTextColor(resources.getColor(R.color.av_gray))
                    oneDay.setTextColor(resources.getColor(R.color.av_gray))
                    threeDay.setBackgroundResource(R.drawable.days_btn__true_bg)
                    oneDay.setBackgroundResource(R.drawable.days_btn__false_bg)
                    twoDay.setBackgroundResource(R.drawable.days_btn__false_bg)
                }
            }
        }
    }

    private fun getMyPromos() {
        promoCodesBinding.progress.visibility = View.VISIBLE
        promoCodeViewModel.getMyPromos(this).observe(this, Observer {
            if (it != null) {
                myPromosList.clear()
                myPromosList.addAll(it)
                promoAdapter.notifyDataSetChanged()
            }
            promoCodesBinding.progress.visibility = View.INVISIBLE
        })
    }

    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetTheme)
        val view = CreateStoreBottomDialogBinding.inflate(LayoutInflater.from(this), null, false)
        view.createStoreDismissDialog.setOnClickListener {
            dialog.dismiss()
        }

        view.createStoreDismissDialog.text = "NEXT"
        view.createStoreInfo.text = "Offers will keep your customers happy \n" +
                "and coming back, and we’ll always put \n" +
                "you on the top of the list if you do so.\n\nKeep this code exclusive to Linko to \n" +
                "track effectiveness and reach."

        view.createStoreTitle.text = "Tip!"

        view.titleImg.setImageResource(R.drawable.ic_promo_tip)

        dialog.setContentView(view.root)
        dialog.show()
    }
}