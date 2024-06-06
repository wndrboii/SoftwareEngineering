package com.example.softwareengineering

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import model.DishCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DishCatActivity : AppCompatActivity(), DishCatAdapter.DishCatAdapterListener {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var goback: ImageButton
    private lateinit var profile: ImageButton

    private lateinit var catAdapter: DishCatAdapter
    private lateinit var catList: MutableList<DishCategory>
    private lateinit var catRecyclerView: RecyclerView
    private lateinit var database: FirebaseDatabase
    private lateinit var catRef: DatabaseReference
    private lateinit var kategorieArr: ImageButton
    private lateinit var searchEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dish_cat)

        catRecyclerView = findViewById(R.id.catRecyclerView)
        catAdapter = DishCatAdapter(mutableListOf(), this)
        catRecyclerView.adapter = catAdapter
        searchEditText = findViewById(R.id.search)

        database = FirebaseDatabase.getInstance()
        catRef = database.getReference("categories")

        catList = mutableListOf()

        catRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                catList.clear()
                val cats = mutableListOf<DishCategory>()
                for (catSnapshot in snapshot.children) {
                    val cat = catSnapshot.getValue(DishCategory::class.java)
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
                catAdapter.updateData(filteredList as MutableList<DishCategory>)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        goback = findViewById(R.id.goback_btn)
        profile = findViewById(R.id.profile_button)

        kategorieArr = findViewById(R.id.kategorie_arr_btn)

        kategorieArr.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(applicationContext, CreateCategoryActivity::class.java)
            startActivity(intent)
            finish()
        })

        home.setOnClickListener {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }

        categories.setOnClickListener {
            startActivity(Intent(applicationContext, CategoriesActivity::class.java))
            finish()
        }

        goback.setOnClickListener {
            startActivity(Intent(applicationContext, CategoriesActivity::class.java))
            finish()
        }

        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(applicationContext, login::class.java))
            finish()
        }

        profile.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,ProfileActivity::class.java)
            startActivity(intent)
            finish()
        })
    }


    override fun onCatClick(position: Int) {
        val cat = catList[position]
        val intent = Intent(this, DishCatDetailsActivity::class.java)
        intent.putExtra("kategoria", cat.id)
        startActivity(intent)
    }

}