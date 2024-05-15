package com.example.softwareengineering

import DishCatAdapter
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareengineering.model.DishCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DishCatActivity : AppCompatActivity(), DishCatAdapter.DishCatAdapterListener {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var catAdapter: DishCatAdapter
    private lateinit var catList: MutableList<DishCategory>
    private lateinit var catRecyclerView: RecyclerView
    private lateinit var database: FirebaseDatabase
    private lateinit var catRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dish_cat)

        catRecyclerView = findViewById(R.id.catRecyclerView)
        catAdapter = DishCatAdapter(mutableListOf(), this)
        catRecyclerView.adapter = catAdapter

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

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)

        home.setOnClickListener {
            startActivity(Intent(applicationContext, MainActivity::class.java))
            finish()
        }

        categories.setOnClickListener {
            startActivity(Intent(applicationContext, CategoriesActivity::class.java))
            finish()
        }

        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(applicationContext, login::class.java))
            finish()
        }
    }


    override fun onCatClick(position: Int) {
        val cat = catList[position]
        val intent = Intent(this, DishCatDetailsActivity::class.java)
        intent.putExtra("kategoria", cat.id)
        startActivity(intent)
    }

}