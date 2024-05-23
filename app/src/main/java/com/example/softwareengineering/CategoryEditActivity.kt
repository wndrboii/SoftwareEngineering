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
import com.example.softwareengineering.adapter.PosilkiToChooseAdapter
import com.example.softwareengineering.model.Posilki
import com.example.softwareengineering.model.ProductCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CategoryEditActivity : AppCompatActivity() {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var catName: EditText
    private lateinit var addButton: ImageButton
    private lateinit var dialogButton: Button
    private lateinit var kategorieArr: TextView

    private var selectedDishes: List<Posilki> = emptyList()
    private lateinit var adapter: PosilkiToChooseAdapter

    private lateinit var database: DatabaseReference
    private lateinit var categoryId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_edit)

        //Dialog window init.
        dialogButton = findViewById(R.id.dialog_btn)
        dialogButton.setOnClickListener{ showCustomDialog() }
        addButton = findViewById(R.id.submit_btn)

        catName = findViewById(R.id.name_edit_text)
        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)

        // Initialize Firebase database
        database = Firebase.database.reference

        // Get dish ID from intent
        categoryId = intent.getStringExtra("zestaw") ?: ""

        // Retrieve dish data from Firebase database
        database.child("sets").child(categoryId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Fill fields with dish data
                val cat = snapshot.getValue(ProductCategory::class.java)
                catName.setText(cat?.name)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "loadCat:onCancelled", error.toException())
            }
        })

        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid
        // Save edited dish to Firebase database
        addButton.setOnClickListener {
            val name = catName.text.toString()

            if (name.isNotEmpty()) {
                val idList = mutableListOf<String?>()

                for (obj in selectedDishes) {
                    idList.add(obj.id)
                }

                val cat = ProductCategory(
                    id = categoryId,
                    name = name,
                    dishesIds = idList,
                    userId = currentUserId
                )

                database.child("sets").child(categoryId).setValue(cat)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Set updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update set", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter a name for the set", Toast.LENGTH_SHORT).show()
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


        kategorieArr = findViewById(R.id.kategorie_arr_btn)
        kategorieArr.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(applicationContext, ListOfCategoriesActivity::class.java)
            startActivity(intent)
            finish()
        })
        }

    private fun showCustomDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null)
        val recyclerView = dialogLayout.findViewById<RecyclerView>(R.id.ingredients_rv)

        val database = FirebaseDatabase.getInstance()
        val dishesRef = database.getReference("dishes")
        val dishesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val dishes = dataSnapshot.children.mapNotNull { it.getValue(Posilki::class.java) }
                adapter.setData(dishes)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadIngredients:onCancelled", databaseError.toException())
            }
        }
        dishesRef.addValueEventListener(dishesListener)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val dishes = mutableListOf<Posilki>()
        adapter = PosilkiToChooseAdapter(dishes) { dish ->
            if (dish.checked) {
                dishes.add(dish)
            } else {
                dishes.remove(dish)
            }
        }

        recyclerView.adapter = adapter


        builder.setTitle("Wybierz posiłki")
            .setMessage("Kliknij checkbox'a żeby dodać posiłek")
            .setView(dialogLayout)
            .setPositiveButton("OK") { dialog, which ->
                selectedDishes = adapter.getData().filter { it.checked }
                Log.d(TAG, "Selected dishes: $selectedDishes")
            }
            .setNegativeButton("Cancel") { dialog, which ->

            }
            .create()
            .show()
    }
}
