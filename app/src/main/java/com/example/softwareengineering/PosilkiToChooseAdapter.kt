package com.example.softwareengineering

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import model.Posilki

class PosilkiToChooseAdapter(
    private val dishes: MutableList<Posilki>,
    private val onDishSelected: (Posilki) -> Unit
) : RecyclerView.Adapter<PosilkiToChooseAdapter.IngredientViewHolder>() {

    fun setData(newDishes: List<Posilki>) {
        dishes.clear()
        dishes.addAll(newDishes)
        notifyDataSetChanged()
    }

    fun getData(): List<Posilki> {
        return dishes
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_posilki_to_choose, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val dish = dishes[position]
        holder.bind(dish)
    }

    override fun getItemCount(): Int = dishes.size

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.dish_name)
        private val kaloriiTextView: TextView = itemView.findViewById(R.id.kalorii_edit_text)
        private val proteinsTextView: TextView = itemView.findViewById(R.id.dish_proteins)
        private val carbsTextView: TextView = itemView.findViewById(R.id.dish_carbs)
        private val fatsTextView: TextView = itemView.findViewById(R.id.dish_fats)
        private val checkBox: CheckBox = itemView.findViewById(R.id.ingredientCheckBox)

        fun bind(dish: Posilki) {
            nameTextView.text = dish.name
            kaloriiTextView.text = "0"
            proteinsTextView.text = "0"
            carbsTextView.text = "0"
            fatsTextView.text = "0"

            checkBox.isChecked = dish.checked

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                dish.checked = isChecked
            }
        }
    }
}