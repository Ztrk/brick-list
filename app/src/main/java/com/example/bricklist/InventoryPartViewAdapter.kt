package com.example.bricklist

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.inventory_part_item.view.*

class InventoryPartViewAdapter(private val onPlusButtonClickListener: (View, Int) -> Unit,
                               private val onMinusButtonClickListener: (View, Int) -> Unit)
    : RecyclerView.Adapter<InventoryPartViewAdapter.InventoryViewHolder>() {

    var inventoryParts = listOf<InventoryPartData>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class InventoryViewHolder(val view: View)
            : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.nameText
        val colorAndCodeText: TextView = view.colorAndCodeText
        val quantityText: TextView = view.quantityText
        val image: ImageView = view.brickImage
        val plusButton: ImageButton = view.plusButton
        val minusButton: ImageButton = view.minusButton
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.inventory_part_item, parent,false)
        val viewHolder = InventoryViewHolder(view)

        viewHolder.plusButton.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onPlusButtonClickListener(it, position)
            }
        }
        viewHolder.minusButton.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onMinusButtonClickListener(it, position)
            }
        }

        return viewHolder
    }

    override fun getItemCount() = inventoryParts.size

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.nameText.text = inventoryParts[position].nameText
        holder.colorAndCodeText.text = inventoryParts[position].colorAndCodeText
        holder.quantityText.text = inventoryParts[position].quantityText
        inventoryParts[position].image?.let {
            holder.image.setImageDrawable(it)
        }
        inventoryParts[position].background?.let {
            holder.view.background = it
        }
    }

    data class InventoryPartData(
        val nameText: String,
        val colorAndCodeText: String,
        val quantityText: String,
        val image: Drawable? = null,
        val background: Drawable? = null
    )
}
