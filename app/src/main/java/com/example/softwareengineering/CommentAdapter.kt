package com.example.softwareengineering

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareengineering.model.Comment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(private val commentList: List<Comment>) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = commentList[position]

        holder.commentDescription.text = comment.text
        holder.commentRating.text = comment.ocena.toString()
        holder.bindEmail(comment.userId)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.commentDate.text =  dateFormat.format(Date(comment.date))
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentDescription: TextView = itemView.findViewById(R.id.comment_description)
        val commentRating: TextView = itemView.findViewById(R.id.comment_ratings)
        val commentUserName: TextView = itemView.findViewById(R.id.comment_user_name)
        val commentDate: TextView = itemView.findViewById(R.id.comment_date)

        fun bindEmail(userId: String) {
            val database = Firebase.database.reference
            database.child("users").child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val email = dataSnapshot.child("email").value as? String
                    if (email != null) {
                        commentUserName.text = email
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //...
                }
            })
        }
    }
}

