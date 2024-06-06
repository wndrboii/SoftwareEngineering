package com.example.softwareengineering
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import model.Measurement
import java.text.SimpleDateFormat
import java.util.*

class MeasurementAdapter(
    private var measList: MutableList<Measurement>,
    private val listener: MeasurementAdapterListener
) :
    RecyclerView.Adapter<MeasurementAdapter.MeasurementViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasurementViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.measurement_item, parent, false)
        return MeasurementViewHolder(itemView)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }

    private var onDeleteClickListener: OnDeleteClickListener? = null

    fun setOnDeleteClickListener(listener: OnDeleteClickListener) {
        this.onDeleteClickListener = listener
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MeasurementViewHolder, position: Int) {
        val currentItem = measList[position]
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        holder.date.text = dateFormat.format(Date(currentItem.date))
        holder.height.text = "Wzrost: ${currentItem.height}"
        holder.weight.text = "Waga: ${currentItem.weight}"
        holder.shoulder.text = "Obwód barków: ${currentItem.shoulder}"
        holder.arm.text = "Obwód ramienia: ${currentItem.arm}"
        holder.chest.text = "Obwód klatki pierściowej: ${currentItem.chest}"
        holder.waist.text = "Obwód talii: ${currentItem.waist}"
        holder.hips.text = "Obwód bioder: ${currentItem.hips}"
        holder.thigh.text = "Obwód ud: ${currentItem.thigh}"
        holder.calves.text = "Obwód łydki: ${currentItem.calves}"


    }

    override fun getItemCount() = measList.size

    fun updateData(newMeases: MutableList<Measurement>) {
        measList.clear()
        measList = newMeases
        notifyDataSetChanged()
    }

    inner class MeasurementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date : TextView = itemView.findViewById(R.id.date)
        val height: TextView = itemView.findViewById(R.id.height)
        val weight: TextView = itemView.findViewById(R.id.weight)
        val shoulder: TextView = itemView.findViewById(R.id.shoulder)
        val arm: TextView = itemView.findViewById(R.id.arm)
        val chest: TextView = itemView.findViewById(R.id.chest)
        val waist: TextView = itemView.findViewById(R.id.waist)
        val hips: TextView = itemView.findViewById(R.id.hips)
        val thigh: TextView = itemView.findViewById(R.id.thigh)
        val calves: TextView = itemView.findViewById(R.id.calves)

        val deleteButton: AppCompatImageView = itemView.findViewById(R.id.remove_btn)
        val editBotton: AppCompatImageView = itemView.findViewById(R.id.edit_btn)

        init {
            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(position)
                }
            }
            editBotton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(position)
                }
            }
        }
    }

    interface MeasurementAdapterListener {
        fun onDeleteClick(position: Int)
        fun onEditClick(position: Int)
    }
}
