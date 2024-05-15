import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareengineering.R
import com.example.softwareengineering.model.Skladnik
import com.example.softwareengineering.model.Tip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class TipsAdapter(
    private var tipList: MutableList<Tip>,
    private val listener: TipAdapterListener
) :
    RecyclerView.Adapter<TipsAdapter.TipViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.activity_tips_item, parent, false)
        return TipViewHolder(itemView)
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }

    private var onDeleteClickListener: OnDeleteClickListener? = null

    fun setOnDeleteClickListener(listener: OnDeleteClickListener) {
        this.onDeleteClickListener = listener
    }

    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        val currentItem = tipList[position]

        holder.tipTopic.text = currentItem.topic
        holder.tipDescription.text = currentItem.text
        currentItem.userId?.let { holder.bindEmail(it) }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.tipDate.text =  dateFormat.format(Date(currentItem.date))

        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid

        if (currentUserId == currentItem.userId) {
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

    override fun getItemCount() = tipList.size

    fun updateData(newTips: MutableList<Tip>) {
        tipList.clear()
        tipList = newTips
        notifyDataSetChanged()
    }

    inner class TipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tipDescription: TextView = itemView.findViewById(R.id.tip_description)
        val tipTopic: TextView = itemView.findViewById(R.id.tip_topic)
        val tipUserName: TextView = itemView.findViewById(R.id.tip_user_name)
        val tipDate: TextView = itemView.findViewById(R.id.tip_date)
        val deleteButton: AppCompatImageView = itemView.findViewById(R.id.remove_btn)
        val editButton: AppCompatImageView = itemView.findViewById(R.id.edit_btn)
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
        fun bindEmail(userId: String) {
            val database = Firebase.database.reference
            database.child("users").child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val email = dataSnapshot.child("email").value as? String
                    if (email != null) {
                        tipUserName.text = email
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //...
                }
            })
        }
    }

    interface TipAdapterListener {
        fun onDeleteClick(position: Int)
        fun onEditClick(position: Int)
    }
}
