package com.example.softwareengineering

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareengineering.model.Posilki

class PosilkiAdapter(
    private var posilkiList: MutableList<Posilki>,
    private val listener: PosilkiAdapterListener
) : RecyclerView.Adapter<PosilkiAdapter.PosilkiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PosilkiViewHolder {
        val layoutResId = when (viewType) {
            0 -> R.layout.posilki_item
            1 -> R.layout.posilki_item_small
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }

        val itemView = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return PosilkiViewHolder(itemView)
    }

    override fun getItemViewType(position: Int): Int {
        return position % 2
    }

    override fun onBindViewHolder(holder: PosilkiViewHolder, position: Int) {
        val currentItem = posilkiList[position]

        holder.nameTextView.text = currentItem.name

        val photoUrl = currentItem.photoUrl
        Glide.with(holder.itemView.context)
            .load(photoUrl)
            .into(holder.photoImageView)
    }

    override fun getItemCount() = posilkiList.size

    fun updateData(newPosilki: MutableList<Posilki>) {
        posilkiList.clear()
        posilkiList.addAll(newPosilki)
        notifyDataSetChanged()
    }

    inner class PosilkiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoImageView: AppCompatImageView = itemView.findViewById(R.id.image_holder)
        val nameTextView: TextView = itemView.findViewById(R.id.posilki_name)

        init {
            photoImageView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDishClick(position)
                }
            }
        }
    }

    interface PosilkiAdapterListener {
        fun onDishClick(position: Int)
    }
}