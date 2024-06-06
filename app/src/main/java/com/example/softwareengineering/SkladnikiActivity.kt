package com.example.softwareengineering

import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import model.Skladnik

class SkladnikiActivity : AppCompatActivity() {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var profile: ImageButton
    private lateinit var nameOfProduct: EditText
    private lateinit var caloriesEditText: EditText
    private lateinit var proteinsEditText: EditText
    private lateinit var carbsEditText: EditText
    private lateinit var fatsEditText: EditText

    private lateinit var skladnikiArr: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skladniki)

        val rootView = findViewById<ScrollView>(R.id.scroll_view)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            rootView.getWindowVisibleDisplayFrame(r)
            val screenHeight = rootView.rootView.height

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            val keypadHeight = screenHeight - r.bottom

            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                // keyboard is opened
                rootView.setPadding(0, 0, 0, keypadHeight - 300)
            } else {
                // keyboard is closed
                rootView.setPadding(0, 0, 0, 0)
            }
        }

        val addButton = findViewById<ImageButton>(R.id.submit_btn)
        addButton.setOnClickListener {
            nameOfProduct = findViewById<EditText>(R.id.name_edit_text)
            caloriesEditText = findViewById<EditText>(R.id.kalorii_edit_text)
            proteinsEditText = findViewById<EditText>(R.id.protein_edit_text)
            carbsEditText = findViewById<EditText>(R.id.wegl_edit_text)
            fatsEditText = findViewById<EditText>(R.id.tł_edit_text)

            val name = nameOfProduct.text.toString()
            val calories = caloriesEditText.text.toString().toDoubleOrNull()
            val proteins = proteinsEditText.text.toString().toDoubleOrNull()
            val carbs = carbsEditText.text.toString().toDoubleOrNull()
            val fats = fatsEditText.text.toString().toDoubleOrNull()

            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = currentUser?.uid

            if (name.isNotEmpty() && calories != null && proteins != null && carbs != null && fats != null) {
                val database = Firebase.database.reference
                val product = Skladnik(
                    id = database.child("products").push().key,
                    name = name,
                    calories = calories,
                    protein = proteins,
                    carbs = carbs,
                    fat = fats,
                    userId = currentUserId
                )

                if (product.id != null) {
                    database.child("products").child(product.id!!).setValue(product)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Nowy składnik dodany pomyślnie",
                                Toast.LENGTH_SHORT
                            ).show()
                            nameOfProduct.text.clear()
                            caloriesEditText.text.clear()
                            proteinsEditText.text.clear()
                            carbsEditText.text.clear()
                            fatsEditText.text.clear()

                            var intent: Intent = Intent(applicationContext, ListOfSkladnikiActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Błąd podczas dodawania składnika: ${it.message}",
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
        profile = findViewById(R.id.profile_button)
        skladnikiArr = findViewById(R.id.skladniki_arr_btn)

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

        skladnikiArr.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(applicationContext, ListOfSkladnikiActivity::class.java)
            startActivity(intent)
            finish()
        })

    }
}