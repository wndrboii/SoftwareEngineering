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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import model.Posilki
import model.SkladPosilku
import model.Skladnik
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import model.DishCategory

class EditDishActivity : AppCompatActivity() {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var profile: ImageButton
    private lateinit var dishName: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var addButton: ImageButton
    private lateinit var dialogButton: Button
    private lateinit var posilkiArr: ImageButton

    private lateinit var adapter: SkladnikiToChooseAdapter

    private lateinit var imageView: ImageView
    private lateinit var chooseImageButton: Button
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private var photoUrl: String = ""

    private lateinit var database: DatabaseReference
    private lateinit var dishId: String
    private lateinit var databaseReference: DatabaseReference
    private lateinit var categoryList: MutableList<DishCategory>
    private var selectedCategory: String = ""
    private var dish: Posilki? = null

    private lateinit var nameWrapper: ConstraintLayout
    private lateinit var selectedImageWrapper: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_dish)

        //Dialog window init.
        dialogButton = findViewById(R.id.dialog_btn)
        dialogButton.setOnClickListener { showCustomDialog() }

        // Initialize views
        dishName = findViewById(R.id.name_edit_text)
        categorySpinner = findViewById(R.id.categorySpinner)
        addButton = findViewById(R.id.submit_btn)

        // Init. nav. buttons
        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        profile = findViewById(R.id.profile_button)
        categories = findViewById(R.id.categories_btn)

        //Choose image from gallery init.

        imageView = findViewById<ImageView>(R.id.image_view)
        chooseImageButton = findViewById<Button>(R.id.choose_image_button)
        nameWrapper = findViewById<ConstraintLayout>(R.id.name_wrapper)
        selectedImageWrapper = findViewById<ConstraintLayout>(R.id.selected_image_wrapper)

        selectedImageWrapper.visibility = View.VISIBLE

        val layoutParams = nameWrapper.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.setMargins(0, 35, 0, 0)

        nameWrapper.layoutParams = layoutParams

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageView.setImageURI(uri)
                photoUrl = uri.toString()
            }
        }

        // Initialize Firebase database
        database = Firebase.database.reference

        databaseReference = FirebaseDatabase.getInstance().reference.child("categories")
        categoryList = mutableListOf()
        dishId = intent.getStringExtra("posilek") ?: ""
        val categoriesForSpinner: MutableList<String> = mutableListOf()
        val categoryIndices = mutableMapOf<String?, Int>()

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryList.clear()

                for (categorySnapshot in snapshot.children) {
                    val category = categorySnapshot.getValue(DishCategory::class.java)
                    category?.let {
                        categoryList.add(it)
                        categoriesForSpinner.add(it.name)
                        categoryIndices[it.id] = categoriesForSpinner.size - 1
                    }
                }

                val adapter = ArrayAdapter(
                    this@EditDishActivity,
                    R.layout.spinner_item_layout,
                    categoriesForSpinner
                )
                categorySpinner.adapter = adapter


                database.child("dishes").child(dishId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            dish = snapshot.getValue(Posilki::class.java)
                            dishName.setText(dish?.name)

                            val categoryId = dish?.category
                            val selectedCategoryIndex = categoryIndices[categoryId]
                            if (selectedCategoryIndex != null) {
                                categorySpinner.setSelection(selectedCategoryIndex)
                            }

                            Glide.with(applicationContext)
                                .load(dish?.photoUrl)
                                .into(imageView)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w(TAG, "loadDish:onCancelled", error.toException())
                        }
                    })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "loadCategories:onCancelled", error.toException())
            }
        })

        // Save edited dish to Firebase database
        addButton.setOnClickListener {
            val name = dishName.text.toString()
            val categoryName = categorySpinner.selectedItem.toString()
            val category = categoryList.find { it.name == categoryName }
            val categoryId = category?.id ?: ""
            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = currentUser?.uid ?: ""

            if (name.isNotEmpty()) {

                // If a new image isn't selected, use the existing photoUrl
                val newPhotoUrl = if (photoUrl.isBlank()) dish?.photoUrl ?: "" else photoUrl
                dish?.let { it1 ->
                    val updatedDish = Posilki(
                        id = dishId,
                        name = name,
                        category = categoryId,
                        photoUrl = newPhotoUrl,
                        userId = currentUserId,
                        comments = dish?.comments,
                        liked = it1.liked
                    )

                    database.child("dishes").child(dishId).setValue(updatedDish)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Posiłek pomyślnie zmieniony", Toast.LENGTH_SHORT)
                                .show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to update dish", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Wszystkie pola muszą być wypełnione", Toast.LENGTH_SHORT)
                    .show()
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
    val amountMap = mutableMapOf<String, Int>()
    private fun showCustomDialog() {
        amountMap.clear()
        val builder = AlertDialog.Builder(this,R.style.AlertDialogProducts)
        val dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null)
        val recyclerView = dialogLayout.findViewById<RecyclerView>(R.id.ingredients_rv)

        recyclerView.apply {
            val displayMetrics = context.resources.displayMetrics
            layoutParams.height = (displayMetrics.heightPixels * 0.5).toInt()
            requestLayout()
        }

        val database = FirebaseDatabase.getInstance()
        val skladPosilkuRef = database.getReference("composition")
        val skladPosilkuListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val skladPosilkuList =
                    dataSnapshot.children.mapNotNull { it.getValue(SkladPosilku::class.java) }
                val skladPosilkuMap = skladPosilkuList.associateBy { it.skladnikId }

                val productsRef = database.getReference("products")
                val productsListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val products =
                            dataSnapshot.children.mapNotNull { it.getValue(Skladnik::class.java) }
                        for (product in products) {
                            val skladPosilku = skladPosilkuMap[product.id]
                            if (skladPosilku != null) {
                                product.checked = true
                                adapter.amountMap[product.id] = skladPosilku.amount
                            }
                        }
                        adapter.setData(products)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(TAG, "loadIngredients:onCancelled", databaseError.toException())
                    }
                }
                productsRef.addValueEventListener(productsListener)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadSkladPosilku:onCancelled", databaseError.toException())
            }
        }
        skladPosilkuRef.addValueEventListener(skladPosilkuListener)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val products = mutableListOf<Skladnik>()
        adapter = SkladnikiToChooseAdapter(products, dishId) { product, amount ->
            if (product.checked) {
                products.add(product)
                val productId = product.id ?: ""
                amountMap[productId] = amount
            } else {
                products.remove(product)
            }
        }

        recyclerView.adapter = adapter

        builder.setTitle("Wybierz składniki")
            .setMessage("Kliknij checkbox'a żeby dodać skłądnik")
            .setView(dialogLayout)
            .setPositiveButton("OK") { dialog, which ->
                // On positive button click, iterate through the products
                for (product in products) {
                    val skladPosilkuRef = database.getReference("composition").child(dishId).child(product.id!!)
                    if (product.checked) {
                        // If the product is checked, update or create the SkladPosilku in Firebase
                        val amount = amountMap[product.id]
                        if (amount != null && amount > 0) {  // Make sure that the amount is not null and more than 0
                            val newSkladPosilku = SkladPosilku(dishId, product.id!!, amount)
                            skladPosilkuRef.setValue(newSkladPosilku)
                        } else {
                            // If amount is 0 or null, remove the SkladPosilku from Firebase
                            skladPosilkuRef.removeValue()
                        }
                    } else {
                        // If the product is not checked, remove the SkladPosilku from Firebase
                        skladPosilkuRef.removeValue()
                    }
                    // Trigger the ValueEventListener to update the Firebase data
                    skladPosilkuRef.keepSynced(true)
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // No action needed on cancel
            }
            .create()
            .show()
    }
}
