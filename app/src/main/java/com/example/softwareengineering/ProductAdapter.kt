import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareengineering.R
import com.example.softwareengineering.model.Skladnik

class ProductAdapter(
    private var productList: MutableList<Skladnik>,
    private val listener: ProductAdapterListener
) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.product_item, parent, false)
        return ProductViewHolder(itemView)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }

    private var onDeleteClickListener: OnDeleteClickListener? = null

    fun setOnDeleteClickListener(listener: OnDeleteClickListener) {
        this.onDeleteClickListener = listener
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentItem = productList[position]

        holder.nameTextView.text = currentItem.name
        holder.caloriesTextView.text = currentItem.calories.toString()
        holder.proteinsTextView.text = currentItem.protein.toString()
        holder.carbsTextView.text = currentItem.carbs.toString()
        holder.fatsTextView.text = currentItem.fat.toString()

    }

    override fun getItemCount() = productList.size

    fun updateData(newProducts: MutableList<Skladnik>) {
        productList.clear()
        productList = newProducts
        notifyDataSetChanged()
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.product_name)
        val caloriesTextView: TextView = itemView.findViewById(R.id.kalorii_edit_text)
        val proteinsTextView: TextView = itemView.findViewById(R.id.product_proteins)
        val carbsTextView: TextView = itemView.findViewById(R.id.product_carbs)
        val fatsTextView: TextView = itemView.findViewById(R.id.product_fats)
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

    interface ProductAdapterListener {
        fun onDeleteClick(position: Int)
        fun onEditClick(position: Int)
    }
}
