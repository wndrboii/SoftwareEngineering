package com.example.softwareengineering

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.softwareengineering.model.Skladnik
import com.example.softwareengineering.model.Tip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EditTipActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var tipRef: DatabaseReference
    private lateinit var tipId: String
    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var topic: EditText
    private lateinit var text: EditText
    private lateinit var addButton: ImageButton

    private lateinit var tipsArr: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_tip)

        database = FirebaseDatabase.getInstance()
        tipRef = database.getReference("tips")

        tipId = intent.getStringExtra("podpowiedz") ?: ""

        topic = findViewById<EditText>(R.id.tip_topic)
        text = findViewById<EditText>(R.id.tip_text)

        tipRef.child(tipId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tip = snapshot.getValue(Tip::class.java)
                tip?.let {
                    topic.setText(tip.topic)
                    text.setText(tip.text)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        val addButton = findViewById<ImageButton>(R.id.submit_btn)
        addButton.setOnClickListener {

            val Topic = topic.text.toString()
            val Text = text.text.toString()

            if (Topic.isBlank() || Text.isBlank()) {
                Toast.makeText(this, "Wszystkie pola muszą być wypełnione poprawnie", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = currentUser?.uid
            val tip = Tip(tipId, Topic, Text, currentUserId)
            tipRef.child(tipId).setValue(tip)
                .addOnSuccessListener {
                    Toast.makeText(this, "Tip updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Error updating tip: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        tipsArr = findViewById(R.id.tips_arr_btn)

        home.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        })

        categories.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(applicationContext, CategoriesActivity::class.java)
            startActivity(intent)
            finish()
        })

        logout.setOnClickListener(View.OnClickListener {
            FirebaseAuth.getInstance().signOut()
            var intent: Intent = Intent(applicationContext, login::class.java)
            startActivity(intent)
            finish()
        })

        tipsArr.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(applicationContext, TipsActivity::class.java)
            startActivity(intent)
            finish()
        })

    }
}
