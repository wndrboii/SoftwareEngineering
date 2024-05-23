package com.example.softwareengineering

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareengineering.model.Posilki

class PosilkiAdapter(
    private var posilkiList: MutableList<Posilki>,
    private val listener: PosilkiAdapterListener
) : RecyclerView.Adapter<PosilkiAdapter.PosilkiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PosilkiViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.posilki_item, parent, false)
        return PosilkiViewHolder(itemView)
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

    @SuppressLint("ClickableViewAccessibility")
    inner class PosilkiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoImageView: AppCompatImageView = itemView.findViewById(R.id.image_holder)
        val nameTextView: TextView = itemView.findViewById(R.id.posilki_name)
        private val overlayLayout: ConstraintLayout = itemView.findViewById(R.id.overlay_Layout)

        init {
            var isTouch: Boolean = false
            photoImageView.setOnLongClickListener {
                isTouch = true
                showOverlay()
                false
            }

            overlayLayout.setOnClickListener {
                isTouch = false
                hideOverlay()
            }

            photoImageView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && !isTouch) {
                    listener.onDishClick(position)
                }
            }

            overlayLayout.findViewById<ImageView>(R.id.edit_btn).setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(position)
                }
            }

            overlayLayout.findViewById<ImageView>(R.id.remove_btn).setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(position)
                }
            }
        }

        private fun showOverlay() {
            overlayLayout.visibility = View.VISIBLE
        }

        private fun hideOverlay() {
            overlayLayout.visibility = View.GONE
        }
    }

    interface PosilkiAdapterListener {
        fun onDishClick(position: Int)
        fun onDeleteClick(position: Int)
        fun onEditClick(position: Int)
    }
}