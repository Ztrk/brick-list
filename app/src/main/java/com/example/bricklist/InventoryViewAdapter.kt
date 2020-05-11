package com.example.bricklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class InventoryViewAdapter()
        : RecyclerView.Adapter<InventoryViewAdapter.InventoryViewHolder>() {

    var inventories = listOf<Inventory>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class InventoryViewHolder(private val view: View)
            : RecyclerView.ViewHolder(view) {
        val nameView: TextView = view.findViewById(R.id.nameText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inventory_item, parent,false)
        return InventoryViewHolder(view)
    }

    override fun getItemCount() = inventories.size

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.nameView.text = inventories[position].name
    }
}