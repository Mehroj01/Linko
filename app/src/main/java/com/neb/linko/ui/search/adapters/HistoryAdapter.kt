package com.neb.linko.ui.search.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.neb.linko.databinding.HistoryItemBinding

class HistoryAdapter(var list: ArrayList<String>, var historyClick: HistoryClick) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(var historyItemBinding: HistoryItemBinding) :
        RecyclerView.ViewHolder(historyItemBinding.root) {
        fun onBind(str: String) {
            historyItemBinding.name.text = str
            historyItemBinding.name.setOnClickListener {
                historyClick.click(str)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
            HistoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size

    interface HistoryClick {
        fun click(str: String)
    }
}