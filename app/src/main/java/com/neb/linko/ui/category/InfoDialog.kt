package com.neb.linko.ui.category

import android.app.ProgressDialog
import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.neb.linko.R

class InfoDialog(private var context: Context, var infoClick: InfoClick, var language: Boolean) {
    lateinit var dialogs: ProgressDialog

    fun networkDialog() {
        val progressDialog = ProgressDialog(context)
        progressDialog.show()
        progressDialog.setContentView(R.layout.save_info_dialog)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        var skip = progressDialog.findViewById<LinearLayout>(R.id.skip_info)
        var tv1 = progressDialog.findViewById<TextView>(R.id.tv1)
        var tv2 = progressDialog.findViewById<TextView>(R.id.tv2)
        var saveTv = progressDialog.findViewById<TextView>(R.id.save_tv_dialog)

        if (!language) {
            tv1.text = "ضغطة متكررة\nلحفظ أي متجر!"
            tv2.text = "طبعاً بس إذا كنت مسجل ؛)"
            saveTv.text = "وصلت!"
            val typeface = ResourcesCompat.getFont(context, R.font.ge_dinar_one_medium)
            tv1.typeface = typeface
            tv2.typeface = typeface
            saveTv.typeface = typeface
        }

        skip.setOnClickListener {
            infoClick.skip()
        }

        dialogs = progressDialog
    }

    interface InfoClick {
        fun skip()
    }
}