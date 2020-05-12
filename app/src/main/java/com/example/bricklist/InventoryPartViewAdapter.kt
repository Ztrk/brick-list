package com.example.bricklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.inventory_part_item.view.*

class InventoryPartViewAdapter
    : RecyclerView.Adapter<InventoryPartViewAdapter.InventoryViewHolder>() {

    var inventoryParts = listOf<InventoryPart>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class InventoryViewHolder(view: View)
            : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.nameText
        val colorText: TextView = view.colorText
        val codeText: TextView = view.codeText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inventory_part_item, parent,false)
        return InventoryViewHolder(view)
    }

    override fun getItemCount() = inventoryParts.size

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.nameText.text = inventoryParts[position].name
        holder.colorText.text = inventoryParts[position].color
        holder.codeText.text = inventoryParts[position].itemCode
    }
}
