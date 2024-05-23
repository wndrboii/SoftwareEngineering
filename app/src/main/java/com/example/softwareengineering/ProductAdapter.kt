import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareengineering.R
import com.example.softwareengineering.model.Skladnik
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.NonDisposableHandle.parent

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

        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid

        if (currentUserId == currentItem.userId) {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.isEnabled = true
            holder.editButton.visibility = View.VISIBLE
            holder.editButton.isEnabled = true
            holder.deleteNonActiveButton.visibility = View.GONE
            holder.deleteNonActiveButton.isEnabled = false
            holder.editNonActiveButton.visibility = View.GONE
            holder.editNonActiveButton.isEnabled = false
        } else {
            holder.deleteNonActiveButton.visibility = View.VISIBLE
            holder.deleteNonActiveButton.isEnabled = true
            holder.editNonActiveButton.visibility = View.VISIBLE
            holder.editNonActiveButton.isEnabled = true
            holder.deleteButton.visibility = View.GONE
            holder.deleteButton.isEnabled = false
            holder.editButton.visibility = View.GONE
            holder.editButton.isEnabled = false
        }
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
        val deleteNonActiveButton: AppCompatImageView = itemView.findViewById(R.id.remove_nonactive_btn)
        val editButton: AppCompatImageView = itemView.findViewById(R.id.edit_btn)
        val editNonActiveButton: AppCompatImageView = itemView.findViewById(R.id.edit_nonactive_btn)

        init {
            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(position)
                }
            }
            editButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(position)
                }
            }
            deleteNonActiveButton.setOnClickListener {
                Toast.makeText(itemView.context, "Tylko autorzy składników mogą usuwać lub edytować składniki", Toast.LENGTH_SHORT).show();
            }
            editNonActiveButton.setOnClickListener {
                Toast.makeText(itemView.context, "Tylko autorzy składników mogą usuwać lub edytować składniki", Toast.LENGTH_SHORT).show();
            }
        }
    }

    interface ProductAdapterListener {
        fun onDeleteClick(position: Int)
        fun onEditClick(position: Int)
    }
}
