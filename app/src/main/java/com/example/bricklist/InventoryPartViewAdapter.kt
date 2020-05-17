package com.example.bricklist

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.inventory_part_item.view.*

class InventoryPartViewAdapter
    : RecyclerView.Adapter<InventoryPartViewAdapter.InventoryViewHolder>() {

    var inventoryParts = listOf<InventoryPartData>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class InventoryViewHolder(view: View)
            : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.nameText
        val colorAndCodeText: TextView = view.colorAndCodeText
        val quantityText: TextView = view.quantityText
        val image: ImageView = view.brickImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inventory_part_item, parent,false)
        return InventoryViewHolder(view)
    }

    override fun getItemCount() = inventoryParts.size

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.nameText.text = inventoryParts[position].nameText
        holder.colorAndCodeText.text = inventoryParts[position].colorAndCodeText
        holder.quantityText.text = inventoryParts[position].quantityText
        val drawable = inventoryParts[position].image
        if (drawable != null) {
            holder.image.setImageDrawable(inventoryParts[position].image)
        }
    }

    data class InventoryPartData(
        val nameText: String,
        val colorAndCodeText: String,
        val quantityText: String,
        val image: Drawable? = null
    )
}
