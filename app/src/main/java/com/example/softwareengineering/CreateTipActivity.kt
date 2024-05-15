package com.example.softwareengineering

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.softwareengineering.model.Skladnik
import com.example.softwareengineering.model.Tip

class CreateTipActivity : AppCompatActivity() {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var topic: EditText
    private lateinit var text: EditText
    private lateinit var addButton: ImageButton

    private lateinit var goback: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_tip)

        val addButton = findViewById<ImageButton>(R.id.submit_btn)
        addButton.setOnClickListener {
            topic = findViewById<EditText>(R.id.tip_topic)
            text = findViewById<EditText>(R.id.tip_text)

            val Topic = topic.text.toString()
            val Text = text.text.toString()

            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = currentUser?.uid

            if (Topic.isNotEmpty() && Text.isNotEmpty()) {
                val database = Firebase.database.reference
                val tip = Tip(
                    id = database.child("tips").push().key,
                    topic = Topic,
                    text = Text,
                    userId = currentUserId
                )

                if (tip.id != null) {
                    database.child("tips").child(tip.id!!).setValue(tip)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Nowa podpowiedź dodana pomyślnie",
                                Toast.LENGTH_SHORT
                            ).show()
                            topic.text.clear()
                            text.text.clear()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Błąd podczas dodawania podpowiedzi: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            } else {
                Toast.makeText(this, "Wszystkie pola muszą być wypełnione poprawnie", Toast.LENGTH_SHORT).show()
            }
        }

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        goback = findViewById(R.id.goback_btn)

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

        goback.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext, TipsActivity::class.java)
            startActivity(intent)
            finish()
        })

    }
}