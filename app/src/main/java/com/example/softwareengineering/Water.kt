package com.example.softwareengineering

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.softwareengineering.model.Water

class Water : AppCompatActivity() {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
//    private lateinit var profile: ImageButton

    private lateinit var water_edit: EditText

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water)

        val addButton = findViewById<ImageButton>(R.id.submit_btn)
        addButton.setOnClickListener {

            water_edit = findViewById<EditText>(R.id.water_edit)

            val goal = water_edit.text.toString().toInt()


            val database = Firebase.database.reference
            val water_goal = Water(
                id = database.child("water_goals").push().key,
                amount = goal
            )


            if (water_goal.id != null) {
                database.child("water_goals").child(water_goal.id!!).setValue(water_goal).addOnSuccessListener {
                    Toast.makeText(this, "Nowy cel podany pomyślnie", Toast.LENGTH_SHORT).show()
                    water_edit.text.clear()
                }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Błąd podczas dodawania celu: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

        }

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
//        profile = findViewById(R.id.profile_button)

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

//        profile.setOnClickListener(View.OnClickListener{
//            var intent : Intent = Intent(applicationContext,ProfileActivity::class.java)
//            startActivity(intent)
//            finish()
//        })

    }
}