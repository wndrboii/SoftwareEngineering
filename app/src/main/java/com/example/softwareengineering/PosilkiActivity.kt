package com.example.softwareengineering

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareengineering.adapter.SkladnikiToChooseAdapter
import com.example.softwareengineering.model.Posilki
import com.google.firebase.auth.FirebaseAuth
import com.example.softwareengineering.model.Skladnik
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class PosilkiActivity : AppCompatActivity() {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var dishName: EditText
    private lateinit var dishCategory: EditText
    private lateinit var dishImage: EditText
    private lateinit var productsList: EditText
    private lateinit var dishQuantity: EditText
    private lateinit var addButton: ImageButton
    private lateinit var dialogButton: Button

    private lateinit var adapter: SkladnikiToChooseAdapter

    private var selectedProducts: List<Skladnik> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posilki)

        dialogButton = findViewById(R.id.dialog_btn)
        dialogButton.setOnClickListener{ showCustomDialog() }

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)

        addButton = findViewById(R.id.submit_btn)

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

    }

    private fun showCustomDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null)
        val recyclerView = dialogLayout.findViewById<RecyclerView>(R.id.ingredients_rv)

        val database = FirebaseDatabase.getInstance()
        val productsRef = database.getReference("products")
        val productsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val products = dataSnapshot.children.mapNotNull { it.getValue(Skladnik::class.java) }
                adapter.setData(products)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadIngredients:onCancelled", databaseError.toException())
            }
        }
        productsRef.addValueEventListener(productsListener)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val products = mutableListOf<Skladnik>()
        adapter = SkladnikiToChooseAdapter(products) { product ->
            if (product.checked) {
                products.add(product)
            } else {
                products.remove(product)
            }
        }

        recyclerView.adapter = adapter


        builder.setTitle("Custom Dialog")
            .setMessage("This is a custom dialog.")
            .setView(dialogLayout)
            .setPositiveButton("OK") { dialog, which ->
                selectedProducts = adapter.getData().filter { it.checked }
                Log.d(TAG, "Selected products: $selectedProducts")
            }
            .setNegativeButton("Cancel") { dialog, which ->

            }
            .create()
            .show()

        addButton.setOnClickListener {
            dishName = findViewById<EditText>(R.id.name_edit_text)
            dishCategory = findViewById<EditText>(R.id.category_edit_text)
            dishQuantity = findViewById<EditText>(R.id.ilosc_edit_text)
            dishImage = findViewById<EditText>(R.id.image_edit_text)

            val name = dishName.text.toString()
            val category = dishCategory.text.toString()
            val quantity = dishQuantity.text.toString().toInt()
            val image = dishImage.text.toString().trim()

            val database = Firebase.database.reference

            val dish = Posilki(
                id = database.child("dishes").push().key,
                name = name,
                category = category,
                quantity = quantity,
                products = selectedProducts,
                photoUrl = image
            )

            if (dish.id != null) {
                database.child("dishes").child(dish.id!!).setValue(dish).addOnSuccessListener {
                    Toast.makeText(this, "Nowy posiłek dodany pomyślnie", Toast.LENGTH_SHORT).show()
                    dishName.text.clear()
                    dishCategory.text.clear()
                    dishQuantity.text.clear()
                    dishImage.text.clear()
                }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Błąd podczas dodawania posiłka: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }
}
