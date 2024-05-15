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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.softwareengineering.adapter.SkladnikiToChooseAdapter
import com.example.softwareengineering.model.Posilki
import com.example.softwareengineering.model.Skladnik
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class EditDishActivity : AppCompatActivity() {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var dishName: EditText
    private lateinit var dishCategory: EditText
    private lateinit var dishQuantity: EditText
    private lateinit var addButton: ImageButton
    private lateinit var dialogButton: Button
    private lateinit var posilkiArr: TextView

    private var selectedProducts: List<Skladnik> = emptyList()
    private lateinit var adapter: SkladnikiToChooseAdapter

    private lateinit var imageView: ImageView
    private lateinit var chooseImageButton: Button
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private var photoUrl: String = ""

    private lateinit var database: DatabaseReference
    private lateinit var dishId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_dish)

        //Dialog window init.
        dialogButton = findViewById(R.id.dialog_btn)
        dialogButton.setOnClickListener{ showCustomDialog() }

        // Initialize views
        dishName = findViewById(R.id.name_edit_text)
        dishCategory = findViewById(R.id.kategorie_edit_text)
        dishQuantity = findViewById(R.id.ilosc_edit_text)
        addButton = findViewById(R.id.submit_btn)

        // Init. nav. buttons
        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)

        //Choose image from gallery init.

        imageView = findViewById<ImageView>(R.id.image_view)
        chooseImageButton = findViewById<Button>(R.id.choose_image_button)

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageView.setImageURI(uri)
                photoUrl = uri.toString()
            }
        }

        // Initialize Firebase database
        database = Firebase.database.reference

        // Get dish ID from intent
        dishId = intent.getStringExtra("posilek") ?: ""

        // Retrieve dish data from Firebase database
        database.child("dishes").child(dishId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Fill fields with dish data
                val dish = snapshot.getValue(Posilki::class.java)
                dishName.setText(dish?.name)
                dishCategory.setText(dish?.category)
                dishQuantity.setText(dish?.quantity.toString())
                Glide.with(applicationContext)
                    .load(dish?.photoUrl)
                    .into(imageView)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "loadDish:onCancelled", error.toException())
            }
        })

        // Save edited dish to Firebase database
        addButton.setOnClickListener {
            val name = dishName.text.toString()
            val category = dishCategory.text.toString()
            val quantityString = dishQuantity.text.toString()

            if (name.isNotEmpty() && category.isNotEmpty() && quantityString.isNotEmpty()) {
                val quantity = quantityString.toInt()

                val dish = Posilki(
                    id = dishId,
                    name = name,
                    category = category,
                    quantity = quantity,
                    products = selectedProducts,
                    photoUrl = photoUrl
                )

                database.child("dishes").child(dishId).setValue(dish)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Dish updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update dish", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please fill in all the required fields", Toast.LENGTH_SHORT).show()
            }
        }

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

        logout.setOnClickListener(View.OnClickListener {
            FirebaseAuth.getInstance().signOut()
            var intent: Intent = Intent(applicationContext, login::class.java)
            startActivity(intent)
            finish()
        })

        //Get "Posilki" array

        posilkiArr = findViewById(R.id.posilki_arr_btn)
        posilkiArr.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(applicationContext, ListOfPosilkiActivity::class.java)
            startActivity(intent)
            finish()
        })

        //Open gallery to choose image

        chooseImageButton.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
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


        builder.setTitle("Wybierz składniki")
            .setMessage("Klikni checkbpx'a żeby dodać skłądnik")
            .setView(dialogLayout)
            .setPositiveButton("OK") { dialog, which ->
                selectedProducts = adapter.getData().filter { it.checked }
                Log.d(TAG, "Selected products: $selectedProducts")
            }
            .setNegativeButton("Cancel") { dialog, which ->

            }
            .create()
            .show()
    }
}
