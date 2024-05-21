package com.example.softwareengineering

import NotificationUtils
import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
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
import com.example.softwareengineering.model.DailyNutrition
import com.example.softwareengineering.model.Posilki
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DailyAdapter(
    private var posilkiList: MutableList<DailyNutrition>,
    private val listener: PosilkiAdapterListener,
    private val notificationUtils: NotificationUtils
) : RecyclerView.Adapter<DailyAdapter.PosilkiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PosilkiViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_daily, parent, false)
        return PosilkiViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PosilkiViewHolder, position: Int) {
        val currentItem = posilkiList[position]
        // Call the scheduleNotification method of yourClass
        notificationUtils.scheduleNotification(holder.itemView.context, currentItem)

        val database = FirebaseDatabase.getInstance()
        val posilkiRef = database.getReference("dishes")
        val posilekId = currentItem.posilekId
        posilkiRef.child(posilekId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dish = snapshot.getValue(Posilki::class.java)
                if (dish != null) {
                    holder.nameTextView.text = dish.name
                }
                val photoUrl = dish?.photoUrl
                Glide.with(holder.itemView.context)
                    .load(photoUrl)
                    .into(holder.photoImageView)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })
        holder.timeTextView.text = currentItem.time
    }

    override fun getItemCount() = posilkiList.size

    fun updateData(newPosilki: MutableList<DailyNutrition>) {
        posilkiList.clear()
        posilkiList.addAll(newPosilki)
        notifyDataSetChanged()
    }

    @SuppressLint("ClickableViewAccessibility")
    inner class PosilkiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoImageView: AppCompatImageView = itemView.findViewById(R.id.image_holder)
        val nameTextView: TextView = itemView.findViewById(R.id.posilki_name)
        val timeTextView: TextView = itemView.findViewById(R.id.posilki_time)
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

            overlayLayout.findViewById<ImageView>(R.id.success_btn).setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onSuccessClick(position)
                }
            }

            overlayLayout.findViewById<ImageView>(R.id.delete_btn).setOnClickListener {
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
        fun onSuccessClick(position: Int)
    }
}