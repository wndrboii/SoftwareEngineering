import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareengineering.R
import com.example.softwareengineering.model.DishCategory
import com.example.softwareengineering.model.ProductCategory

class DishCatAdapter(
    private var catList: MutableList<DishCategory>,
    private val listener: DishCatAdapterListener
) : RecyclerView.Adapter<DishCatAdapter.DishCatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DishCatViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.dish_cat_item, parent, false)
        return DishCatViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DishCatViewHolder, position: Int) {
        val currentItem = catList[position]
        holder.bind(currentItem)

        holder.itemView.setOnClickListener {
            listener.onCatClick(position)
        }
    }

    override fun getItemCount() = catList.size

    fun updateData(newCat: MutableList<DishCategory>) {
        catList.clear()
        catList.addAll(newCat)
        notifyDataSetChanged()
    }

    inner class DishCatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.category_name)

        fun bind(category: DishCategory) {
            nameTextView.text = category.name
        }
    }

    interface DishCatAdapterListener {
        fun onCatClick(position: Int)
    }
}