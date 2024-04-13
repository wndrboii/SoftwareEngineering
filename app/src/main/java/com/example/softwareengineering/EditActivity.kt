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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EditActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var productRef: DatabaseReference
    private lateinit var skladnikId: String
    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var nameOfProduct: EditText
    private lateinit var caloriesEditText: EditText
    private lateinit var proteinsEditText: EditText
    private lateinit var carbsEditText: EditText
    private lateinit var fatsEditText: EditText
    private lateinit var addButton: ImageButton
    private lateinit var skladnikiArr: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        database = FirebaseDatabase.getInstance()
        productRef = database.getReference("products")

        skladnikId = intent.getStringExtra("skladnik") ?: ""

        nameOfProduct = findViewById<EditText>(R.id.name_edit_text)
        caloriesEditText = findViewById<EditText>(R.id.kalorii_edit_text)
        proteinsEditText = findViewById<EditText>(R.id.protein_edit_text)
        carbsEditText = findViewById<EditText>(R.id.wegl_edit_text)
        fatsEditText = findViewById<EditText>(R.id.tł_edit_text)

        productRef.child(skladnikId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val product = snapshot.getValue(Skladnik::class.java)
                product?.let {
                    nameOfProduct.setText(product.name)
                    caloriesEditText.setText(product.calories.toString())
                    proteinsEditText.setText(product.protein.toString())
                    carbsEditText.setText(product.carbs.toString())
                    fatsEditText.setText(product.fat.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        val addButton = findViewById<ImageButton>(R.id.submit_btn)
        addButton.setOnClickListener {

            val name = nameOfProduct.text.toString()
            val calories = caloriesEditText.text.toString().toInt()
            val protein = proteinsEditText.text.toString().toInt()
            val carbs = carbsEditText.text.toString().toInt()
            val fat = fatsEditText.text.toString().toInt()

            if (name.isBlank()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val product = Skladnik(skladnikId, name, calories, protein, carbs, fat,)
            productRef.child(skladnikId).setValue(product).addOnSuccessListener {
                Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(
                    this,
                    "Error updating product: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
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
