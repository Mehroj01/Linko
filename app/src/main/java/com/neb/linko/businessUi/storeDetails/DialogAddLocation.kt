package com.neb.linko.businessUi.storeDetails

import android.app.ProgressDialog
import android.content.Context
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import com.neb.linko.R

class DialogAddLocation(
    private var context: Context,
    var oldName: String,
    var status: Boolean,
    var addLocationClick: AddLocationClick
) {
    lateinit var dialogs: ProgressDialog

    fun networkDialog() {
        val progressDialog = ProgressDialog(context)
        progressDialog.show()
        progressDialog.setContentView(R.layout.add_location_dialog)
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val save = progressDialog.findViewById<TextView>(R.id.save)
        val skip = progressDialog.findViewById<TextView>(R.id.skip_map)
        val name = progressDialog.findViewById<EditText>(R.id.name_et)

        skip.setOnClickListener {
            addLocationClick.skip()
        }

        save.setOnClickListener {
            addLocationClick.save(name.text.toString())
        }

        if (status) {
            save.text = "Change"
            name.setText(oldName)
        } else {
            save.text = "Save"
        }

        dialogs = progressDialog
        dialogs.window?.clearFlags(
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        )
    }

    interface AddLocationClick {
        fun skip()
        fun save(name: String)
    }
}