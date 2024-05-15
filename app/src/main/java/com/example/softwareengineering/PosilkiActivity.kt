package com.example.softwareengineering

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareengineering.adapter.SkladnikiToChooseAdapter
import com.example.softwareengineering.model.DishCategory
import com.example.softwareengineering.model.Posilki
import com.google.firebase.auth.FirebaseAuth
import com.example.softwareengineering.model.Skladnik
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.InputStream


class PosilkiActivity : AppCompatActivity() {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var goback: ImageButton

    private lateinit var dishName: EditText
    private lateinit var dishCategory: EditText
    private lateinit var dishQuantity: EditText
    private lateinit var addButton: ImageButton
    private lateinit var dialogButton: Button

    private lateinit var adapter: SkladnikiToChooseAdapter

    private var selectedProducts: List<Skladnik> = emptyList()

    private lateinit var imageView: ImageView
    private lateinit var chooseImageButton: Button
    private var photoUrl: String = ""

    private lateinit var getContent: ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posilki)

        dialogButton = findViewById(R.id.dialog_btn)
        dialogButton.setOnClickListener{ showCustomDialog() }

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        goback = findViewById(R.id.goback_btn)

        addButton = findViewById(R.id.submit_btn)

        val database = Firebase.database.reference

        //Choose image from gallery
        imageView = findViewById<ImageView>(R.id.image_view)
        chooseImageButton = findViewById<Button>(R.id.choose_image_button)

        val storageRef = FirebaseStorage.getInstance().reference.child("images")

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageView.setImageURI(uri)
                val imageRef = storageRef.child(uri.lastPathSegment!!.substringAfterLast("/"))
                val uploadTask = contentResolver?.openInputStream(uri)?.readBytes()?.let { imageRef.putBytes(it) }
                uploadTask?.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        photoUrl = uri.toString()
                        Log.d(TAG, "Image URL: $photoUrl")
                    }
                }
            }
        }

        chooseImageButton.setOnClickListener {
            getContent.launch("image/*")
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

        goback.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(applicationContext, ListOfPosilkiActivity::class.java)
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


        builder.setTitle("Wybierz składniki")
            .setMessage("Kliknij checkbox'a żeby dodać składnik \nNazwa | kalorii | białko | weglewodany | tłuszcz")
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

            val name = dishName.text.toString()
            val category = dishCategory.text.toString()
            val quantity = dishQuantity.text.toString().toIntOrNull()

            if (name.isBlank() || category.isBlank() || quantity == null || selectedProducts.isEmpty()) {
                Toast.makeText(this, "Wszystkie pola muszą być wypełnione poprawnie", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val database = Firebase.database.reference
            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = currentUser?.uid

            var categoryId: String
            val categoryRef = database.child("categories").child(category)
            categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val dishCat: DishCategory

                    if (dataSnapshot.exists()) {
                        // Category already exists, retrieve the ID
                        categoryId = dataSnapshot.key!!
                        dishCat = dataSnapshot.getValue(DishCategory::class.java)!!
                    } else {
                        // Category does not exist, create a new one
                        categoryId = categoryRef.push().key!!
                        dishCat = DishCategory(id = categoryId, name = category)
                        categoryRef.setValue(dishCat)
                    }
                    val dish = Posilki(
                        id = database.child("dishes").push().key,
                        name = name,
                        category = categoryId,
                        quantity = quantity,
                        products = selectedProducts,
                        photoUrl = photoUrl,
                        comments = null,
                        userId = currentUserId
                    )
                    if (dish.id != null) {
                        database.child("dishes").child(dish.id!!).setValue(dish)
                            .addOnSuccessListener {
                                Toast.makeText(applicationContext, "Nowy posiłek dodany pomyślnie", Toast.LENGTH_SHORT).show()
                                dishName.text.clear()
                                dishCategory.text.clear()
                                dishQuantity.text.clear()
                                imageView.setImageBitmap(null)

                                var intent: Intent = Intent(applicationContext, ListOfPosilkiActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    applicationContext,
                                    "Błąd podczas dodawania posiłka: ${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle the error case
                    println("Error retrieving category: ${databaseError.message}")
                }
            })
        }
    }
}
