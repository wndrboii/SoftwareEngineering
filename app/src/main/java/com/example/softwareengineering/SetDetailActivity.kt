package com.example.softwareengineering

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.softwareengineering.model.Posilki
import com.example.softwareengineering.model.ProductCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class SetDetailActivity : AppCompatActivity(), PosilkiAdapter.PosilkiAdapterListener {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var add : ImageButton

    private lateinit var dishAdapter: PosilkiAdapter
    private lateinit var dishList: MutableList<Posilki>
    private lateinit var dishRecyclerView: RecyclerView
    private lateinit var database: FirebaseDatabase
    private lateinit var dishRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_of_posilki)

        dishRecyclerView = findViewById(R.id.dishRecyclerView)
        dishAdapter = PosilkiAdapter(mutableListOf(), this)

        dishRecyclerView.setHasFixedSize(true)
        dishRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        dishRecyclerView.adapter = dishAdapter

        database = FirebaseDatabase.getInstance()
        dishRef = database.getReference("dishes")

        dishList = mutableListOf()

        dishRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dishList.clear()
                val posilki = mutableListOf<Posilki>()
                val categoryId = intent.getStringExtra("zestaw")

                val categoryRef = categoryId?.let {
                    database.getReference("sets").child(
                        it
                    )
                }

                categoryRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val category = dataSnapshot.getValue(ProductCategory::class.java)
                        category?.let {
                            val dishIds = category.dishesIds
                            for (posilekSnapshot in snapshot.children) {
                                val product = posilekSnapshot.getValue(Posilki::class.java)
                                product?.let {
                                    if (it.id in dishIds) {
                                        posilki.add(it)
                                    }
                                }
                            }
                            dishList.addAll(posilki)
                            dishAdapter.updateData(posilki)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle the error case
                        Log.e(TAG, "Failed to read value: ${databaseError.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        add = findViewById(R.id.add_button)
        add.visibility = View.INVISIBLE

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

    }

    override fun onDishClick(position: Int) {
        val comment = dishList[position]

        val intent = Intent(this, DishDetailActivity::class.java)
        intent.putExtra("posilek", comment.id)
        intent.putExtra("sourceActivity", "SetDetail")
        startActivity(intent)
    }

    override fun onDeleteClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onEditClick(position: Int) {
        TODO("Not yet implemented")
    }

}