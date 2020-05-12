package com.example.bricklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.inventory_item.view.*

class InventoryViewAdapter(private val onClickListener: (View, Inventory) -> Unit)
    : RecyclerView.Adapter<InventoryViewAdapter.InventoryViewHolder>() {

    var inventories = listOf<Inventory>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class InventoryViewHolder(view: View)
            : RecyclerView.ViewHolder(view) {
        val nameView: TextView = view.nameText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inventory_item, parent,false)
        val viewHolder = InventoryViewHolder(view)

        view.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onClickListener(it, inventories[position])
            }
        }

        return viewHolder
    }

    override fun getItemCount() = inventories.size

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.nameView.text = inventories[position].name
    }
}
