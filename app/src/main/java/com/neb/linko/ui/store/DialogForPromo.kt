package com.neb.linko.ui.store

import android.app.ProgressDialog
import android.content.Context
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import com.neb.linko.R

class DialogForPromo(private var context: Context, var myClick: MyClick, var language: Boolean) {
    lateinit var dialogs: ProgressDialog

    fun networkDialog() {
        val progressDialog = ProgressDialog(context)
        progressDialog.show()
        progressDialog.setContentView(R.layout.promo_open_dialog)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val skip = progressDialog.findViewById<AppCompatButton>(R.id.skip)
        val open = progressDialog.findViewById<AppCompatButton>(R.id.open)
        val tv = progressDialog.findViewById<TextView>(R.id.promo_open_dialog_tv)
        tv.text =
            if (language) "Do you want to enter the store now?" else "هل تريد فتح المتجر الآن؟"
        skip.text = if (language) "Not now" else "ليس الآن"
        open.text = if (language) "Open now" else "فتح المتجر"
        skip.setOnClickListener {
            myClick.skip()
        }

        open.setOnClickListener {
            myClick.openPromoStore()
        }
        if (!language){
            val typeface = ResourcesCompat.getFont(context, R.font.ge_dinar_one_medium)
            skip.typeface = typeface
            open.typeface = typeface
            tv.typeface = typeface
        }

        dialogs = progressDialog
    }

    interface MyClick {
        fun skip()
        fun openPromoStore()
    }
}