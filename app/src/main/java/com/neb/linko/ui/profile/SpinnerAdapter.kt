package com.neb.linko.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.neb.linko.databinding.SpinnerItemBinding
import com.squareup.picasso.Picasso

class SpinnerAdapter(var list: ArrayList<CountryModel>) : BaseAdapter() {
    override fun getCount(): Int = list.size

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var item = SpinnerItemBinding.inflate(LayoutInflater.from(parent!!.context), parent, false)
        try {
            Picasso.get().load(list[position].flag?:"")
                .into(item.countryImg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return item.root
    }
}

class CountryModel{

    var flag: String?=null
    var shortname: String?=null

    constructor()

    constructor(flag: String?, shortname: String?) {
        this.flag = flag
        this.shortname = shortname
    }

}