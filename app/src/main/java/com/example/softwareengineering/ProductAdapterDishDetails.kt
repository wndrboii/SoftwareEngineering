package com.example.softwareengineering

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import model.Skladnik
import java.text.DecimalFormat

class ProductAdapterDishDetails(
    private var productList: MutableList<Skladnik>,
    private var skladnikAmounts: MutableList<Int>,
    private val listener: ProductAdapterDishDetailsListener
) : RecyclerView.Adapter<ProductAdapterDishDetails.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.dish_details_products_item, parent, false)
        return ProductViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentItem = productList[position]
        val currentAmount = skladnikAmounts[position]

        holder.nameTextView.text = currentItem.name
        holder.caloriesTextView.text = "${roundToDecimal((currentItem.calories / 100) * currentAmount)} kcal"
        holder.proteinsTextView.text = "Białko: ${roundToDecimal((currentItem.protein  / 100) * currentAmount)}"
        holder.carbsTextView.text = "Węglowodany: ${roundToDecimal((currentItem.carbs  / 100) * currentAmount)}"
        holder.fatsTextView.text = "Tłuszcz: ${roundToDecimal((currentItem.fat  / 100) * currentAmount)}"
        holder.amountTextView.text = "${currentAmount}g"
    }

    override fun getItemCount(): Int = productList.size

    fun updateData(newProducts: MutableList<Skladnik>, newAmounts: MutableList<Int>) {
        productList.clear()
        skladnikAmounts.clear()
        productList.addAll(newProducts)
        skladnikAmounts.addAll(newAmounts)
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.product_name)
        val caloriesTextView: TextView = itemView.findViewById(R.id.kalorii_e_text)
        val proteinsTextView: TextView = itemView.findViewById(R.id.product_e_proteins)
        val carbsTextView: TextView = itemView.findViewById(R.id.product_e_carbs)
        val fatsTextView: TextView = itemView.findViewById(R.id.product_e_fats)
        val amountTextView: TextView = itemView.findViewById(R.id.product_amount)
    }

    interface ProductAdapterDishDetailsListener {
        fun onDeleteClick(position: Int)
        fun onEditClick(position: Int)
    }
    fun roundToDecimal(number: Double): Double {
        val decimalFormat = DecimalFormat("#.#")
        val formattedNumber = decimalFormat.format(number).replace(',', '.')
        return formattedNumber.toDouble()
    }
}