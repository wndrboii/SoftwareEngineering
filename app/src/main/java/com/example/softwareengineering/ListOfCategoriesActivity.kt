package com.example.softwareengineering

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import model.ProductCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ListOfCategoriesActivity : AppCompatActivity(), CategoryAdapter.CategoryAdapterListener {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var profile: ImageButton

    private lateinit var goback: ImageButton
    private lateinit var searchEditText: EditText
    private lateinit var catAdapter: CategoryAdapter
    private lateinit var catList: MutableList<ProductCategory>
    private lateinit var catRecyclerView: RecyclerView
    private lateinit var database: FirebaseDatabase
    private lateinit var catRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_of_categories)

        catRecyclerView = findViewById(R.id.catRecyclerView)
        catAdapter = CategoryAdapter(mutableListOf(), this)
        catRecyclerView.adapter = catAdapter
        searchEditText = findViewById(R.id.search)
        database = FirebaseDatabase.getInstance()
        catRef = database.getReference("sets")

        catList = mutableListOf()

        catRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                catList.clear()
                val cats = mutableListOf<ProductCategory>()
                for (catSnapshot in snapshot.children) {
                    val cat = catSnapshot.getValue(ProductCategory::class.java)
                    cat?.let {
                        cats.add(it)
                    }
                }
                catList.addAll(cats)
                catAdapter.updateData(cats)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }

        })
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchTerm = s.toString().lowercase()
                val filteredList = catList.filter { it.name.lowercase().contains(searchTerm) }
                catAdapter.updateData(filteredList as MutableList<ProductCategory>)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        profile = findViewById(R.id.profile_button)
        goback = findViewById(R.id.goback_btn)

        home.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        })

        categories.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext, CategoriesActivity::class.java)
            startActivity(intent)
            finish()
        })

        logout.setOnClickListener(View.OnClickListener{
            FirebaseAuth.getInstance().signOut()
            var intent : Intent = Intent(applicationContext, login::class.java)
            startActivity(intent)
            finish()
        })

        profile.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,ProfileActivity::class.java)
            startActivity(intent)
            finish()
        })
        goback.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext, DishCategories::class.java)
            startActivity(intent)
            finish()
        })

        catAdapter.setOnDeleteClickListener(object : CategoryAdapter.OnDeleteClickListener {
            override fun onDeleteClick(position: Int) {
                val cat = catList[position]
                cat.id?.let {
                    val catRef = database.getReference("sets/$it")
                    catRef.removeValue()
                }
            }
        })

    }

    override fun onDeleteClick(position: Int) {
        val cat = catList[position]
        cat.id?.let {
            val catRef = database.getReference("sets/$it")
            catRef.removeValue().addOnSuccessListener {
                Toast.makeText(this, "Zestaw został pomyślnie usunięty", Toast.LENGTH_SHORT).show()
            }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Błąd podczas usuwania zestawu: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onEditClick(position: Int) {
        val cat = catList[position]

        val intent = Intent(this, CategoryEditActivity::class.java)
        intent.putExtra("zestaw", cat.id)
        startActivity(intent)
    }

    override fun onCatClick(position: Int) {
        val cat = catList[position]

        val intent = Intent(this, SetDetailActivity::class.java)
        intent.putExtra("zestaw", cat.id)
        startActivity(intent)
    }

}