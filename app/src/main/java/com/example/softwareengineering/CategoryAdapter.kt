import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareengineering.R
import com.example.softwareengineering.model.Posilki
import com.example.softwareengineering.model.ProductCategory
import com.google.firebase.auth.FirebaseAuth

class CategoryAdapter(
    private var catList: MutableList<ProductCategory>,
    private val listener: CategoryAdapterListener
) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.activity_category_item, parent, false)
        return CategoryViewHolder(itemView)
    }


    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }

    private var onDeleteClickListener: OnDeleteClickListener? = null

    fun setOnDeleteClickListener(listener: OnDeleteClickListener) {
        this.onDeleteClickListener = listener
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val currentItem = catList[position]

        holder.bind(currentItem)
        holder.itemView.setOnClickListener {
            listener.onCatClick(position)
        }
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid
        val isCreatedByCurrentUser = currentItem.userId == currentUserId

        // Set visibility and clickability of edit and delete buttons based on the creator
        if (isCreatedByCurrentUser) {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.isEnabled = true
            holder.editButton.visibility = View.VISIBLE
            holder.editButton.isEnabled = true
        } else {
            holder.deleteButton.visibility = View.INVISIBLE
            holder.deleteButton.isEnabled = false
            holder.editButton.visibility = View.INVISIBLE
            holder.editButton.isEnabled = false
        }
    }

    override fun getItemCount() = catList.size

    fun updateData(newCat: MutableList<ProductCategory>) {
        catList.clear()
        catList = newCat
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.category_name)
        val deleteButton: AppCompatImageView = itemView.findViewById(R.id.remove_btn)
        val editButton: AppCompatImageView = itemView.findViewById(R.id.edit_btn)

        private var currentCat: ProductCategory? = null

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

        }

        fun bind(cats: ProductCategory) {
            currentCat = cats

            nameTextView.text = cats.name

        }
    }

    interface CategoryAdapterListener {
        fun onDeleteClick(position: Int)
        fun onEditClick(position: Int)
        fun onCatClick(position: Int)
    }
}