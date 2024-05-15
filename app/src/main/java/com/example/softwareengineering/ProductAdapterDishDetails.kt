import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareengineering.R
import com.example.softwareengineering.model.Skladnik

class ProductAdapterDishDetails(
    private var productList: MutableList<Skladnik>,
    private val listener: ProductAdapterDishDetailsListener
) :
    RecyclerView.Adapter<ProductAdapterDishDetails.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.dish_details_products_item, parent, false)
        return ProductViewHolder(itemView)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }

    private var onDeleteClickListener: OnDeleteClickListener? = null

    fun setOnDeleteClickListener(listener: OnDeleteClickListener) {
        this.onDeleteClickListener = listener
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentItem = productList[position]

        holder.nameTextView.text = currentItem.name
        holder.caloriesTextView.text = "Calories: ${currentItem.calories}"
        holder.proteinsTextView.text = "Proteins: ${currentItem.protein}"
        holder.carbsTextView.text = "Carbs: ${currentItem.carbs}"
        holder.fatsTextView.text = "Fats: ${currentItem.fat}"

    }

    override fun getItemCount() = productList.size

    fun updateData(newProducts: MutableList<Skladnik>) {
        productList.clear()
        productList = newProducts
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.product_name)
        val caloriesTextView: TextView = itemView.findViewById(R.id.kalorii_e_text)
        val proteinsTextView: TextView = itemView.findViewById(R.id.product_e_proteins)
        val carbsTextView: TextView = itemView.findViewById(R.id.product_e_carbs)
        val fatsTextView: TextView = itemView.findViewById(R.id.product_e_fats)
    }

    interface ProductAdapterDishDetailsListener {
        fun onDeleteClick(position: Int)
        fun onEditClick(position: Int)
    }
}
