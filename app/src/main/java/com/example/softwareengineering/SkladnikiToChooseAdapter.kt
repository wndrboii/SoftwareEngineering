package com.example.softwareengineering

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.NumberPicker
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import model.SkladPosilku
import model.Skladnik

class SkladnikiToChooseAdapter(
    private val products: MutableList<Skladnik>,
    private val posilkiId: String,
    private val onProductSelected: (Skladnik, Int) -> Unit
) : RecyclerView.Adapter<SkladnikiToChooseAdapter.IngredientViewHolder>() {

    private val skladPosilkuList: MutableList<SkladPosilku> = mutableListOf()
    val amountMap: MutableMap<String?, Int> = mutableMapOf()
    fun setData(newProducts: List<Skladnik>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    fun getData(): List<Skladnik> {
        return products
    }
    fun getSkladPosilkuList(): List<SkladPosilku> {
        return skladPosilkuList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.skladniki_to_choose_item2, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)

        // Set the amount from the map for the product
        holder.amountNumber.value = amountMap[product.id] ?: 0
        holder.amountNumber.setOnValueChangedListener { _, _, newVal ->
            // Update the amount in the map when the value changes
            amountMap[product.id] = newVal
        }
    }

    override fun getItemCount(): Int = products.size

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.product_name)
        private val kaloriiTextView: TextView = itemView.findViewById(R.id.kalorii_edit_text)
        private val checkBox: CheckBox = itemView.findViewById(R.id.ingredientCheckBox)
        val amountNumber: NumberPicker = itemView.findViewById(R.id.numberPicker)

        @SuppressLint("SetTextI18n")
        fun bind(product: Skladnik) {
            nameTextView.text = product.name
            kaloriiTextView.text = "${product.calories} kcal"
            amountNumber.minValue = 0
            amountNumber.maxValue = 1000

            checkBox.isChecked = skladPosilkuList.any { it.skladnikId == product.id }
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Add a SkladPosilku item if the checkbox is checked
                    val skladPosilku = product.id?.let {
                        SkladPosilku(
                            posilkiId = posilkiId,
                            skladnikId = it,
                            amount = amountMap[product.id] ?: 0
                        )
                    }
                    if (skladPosilku != null) {
                        skladPosilkuList.add(skladPosilku)
                    }
                } else {
                    // Remove the SkladPosilku item if the checkbox is unchecked
                    val skladPosilkuToRemove = skladPosilkuList.find { it.skladnikId == product.id }
                    skladPosilkuList.remove(skladPosilkuToRemove)
                }
            }
        }
    }
}

