package com.example.softwareengineering

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import model.DishCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CreateCategoryActivity : AppCompatActivity() {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var profile: ImageButton

    private lateinit var categoryName: EditText
    private lateinit var addButton: ImageButton
    private lateinit var kategorieArr: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_category)

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        profile = findViewById(R.id.profile_button)

        addButton = findViewById(R.id.submit_btn)

        //Menu navigation
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

        profile.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,ProfileActivity::class.java)
            startActivity(intent)
            finish()
        })

        logout.setOnClickListener(View.OnClickListener {
            FirebaseAuth.getInstance().signOut()
            var intent: Intent = Intent(applicationContext, login::class.java)
            startActivity(intent)
            finish()
        })

        kategorieArr = findViewById(R.id.kategorie_arr_btn)
        kategorieArr.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(applicationContext, DishCatActivity::class.java)
            startActivity(intent)
            finish()
        })

        addButton.setOnClickListener {
            categoryName = findViewById<EditText>(R.id.name_edit_text)
            val name = categoryName.text.toString()

            if (name.isNotEmpty()) {
                val database = Firebase.database.reference

                val cat = DishCategory(
                    id = database.child("categories").push().key,
                    name = name,
                )

                if (cat.id != null) {
                    database.child("categories").child(cat.id!!).setValue(cat).addOnSuccessListener {
                        Toast.makeText(this, "Nowa kategoria dodana pomyślnie", Toast.LENGTH_SHORT).show()
                        categoryName.text.clear()
                    }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Błąd podczas dodawania kategorii: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            } else {
                Toast.makeText(this, "Please enter a name for the category", Toast.LENGTH_SHORT).show()
            }
        }
    }
}