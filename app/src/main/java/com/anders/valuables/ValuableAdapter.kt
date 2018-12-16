package com.anders.valuables

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.valuable_list_item.view.*

class ValuableAdapter(val context: Context, val items : ArrayList<ValuableItem>) : RecyclerView.Adapter<ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.valuable_list_item, parent, false)
        return ViewHolder(context, view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.valuableItemName.text        = items.get(position).name
        holder.valuableItemDescription.text = items.get(position).description
        holder.valuableItemPrice.text       = items.get(position).price.toString()
        holder.valuableItemImage.setImageBitmap(items.get(position).image)
    }

    fun removeItem(position: Int) {
        items.removeAt(position)
        notifyDataSetChanged()
    }
}

class ViewHolder (context: Context, view: View) : RecyclerView.ViewHolder(view) {
    val valuableItemName        :TextView   = view.valuable_item_name
    val valuableItemDescription :TextView   = view.valuable_item_description
    val valuableItemPrice       :TextView   = view.valuable_item_price
    val valuableItemImage       :ImageView  = view.valuable_item_image
}