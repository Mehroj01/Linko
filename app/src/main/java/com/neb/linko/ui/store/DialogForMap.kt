package com.neb.linko.ui.store

import android.app.ProgressDialog
import android.content.Context
import android.widget.TextView
import com.neb.linko.R

class DialogForMap(var title: String, private var context: Context, var myClick: MapClick) {
    lateinit var dialogs: ProgressDialog

    fun networkDialog() {
        val progressDialog = ProgressDialog(context)
        progressDialog.show()
        progressDialog.setContentView(R.layout.map_popup)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val open = progressDialog.findViewById<TextView>(R.id.open_map)
        val skip = progressDialog.findViewById<TextView>(R.id.skip_map)
        val titleMap = progressDialog.findViewById<TextView>(R.id.title_map)

        titleMap.text = title

        skip.setOnClickListener {
            myClick.skip()
        }

        open.setOnClickListener {
            myClick.openMap()
        }
        dialogs = progressDialog
    }

    interface MapClick {
        fun skip()
        fun openMap()
    }
}