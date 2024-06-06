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
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import model.Posilki
import model.SkladPosilku
import model.Skladnik
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.concurrent.atomic.AtomicInteger


class FavouriteActivity : AppCompatActivity(), FavouriteAdapter.PosilkiAdapterListener {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var goback: ImageButton
    private lateinit var profile: ImageButton

    private lateinit var add: ImageView

    private lateinit var searchEditText: EditText
    private lateinit var dishAdapter: FavouriteAdapter
    private lateinit var dishList: MutableList<Posilki>
    private lateinit var dishRecyclerView: RecyclerView
    private lateinit var database: FirebaseDatabase
    private lateinit var dishRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_of_posilki)

        dishRecyclerView = findViewById(R.id.dishRecyclerView)
        dishAdapter = FavouriteAdapter(mutableListOf(), this)
        searchEditText = findViewById(R.id.search)
        dishRecyclerView.setHasFixedSize(true)
        dishRecyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        dishRecyclerView.adapter = dishAdapter

        database = FirebaseDatabase.getInstance()
        dishRef = database.getReference("dishes")

        dishList = mutableListOf()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid

        dishRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dishList.clear()
                val likedDishes = mutableListOf<Posilki>()

                for (posilekSnapshot in snapshot.children) {
                    val dish = posilekSnapshot.getValue(Posilki::class.java)
                    dish?.let {
                        if (currentUserId in it.liked) {
                            likedDishes.add(it)
                        }
                    }
                }

                dishList.addAll(likedDishes)
                dishAdapter.updateData(likedDishes)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchTerm = s.toString().lowercase()

                val database = FirebaseDatabase.getInstance()

                if (searchTerm.isEmpty()) {
                    dishAdapter.updateData(dishList)
                    return
                }

                val dishesMatchedByName =
                    dishList.filter { it.name.lowercase().contains(searchTerm) }.toMutableList()
                val dishesMatchedByProduct = mutableListOf<Posilki>()
                val asyncTasksCount = AtomicInteger(dishList.size)

                for (dish in dishList) {
                    // Listen for SkladPosilku data
                    val skladPosilkuRef =
                        database.getReference("composition").orderByChild("posilkiId")
                            .equalTo(dish.id)
                    skladPosilkuRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(skladPosilkuDataSnapshot: DataSnapshot) {
                            for (skladPosilkuSnapshot in skladPosilkuDataSnapshot.children) {
                                val skladPosilku =
                                    skladPosilkuSnapshot.getValue(SkladPosilku::class.java)
                                        ?: continue

                                // Listen for Skladnik data
                                val skladnikiRef =
                                    database.getReference("products").child(skladPosilku.skladnikId)
                                skladnikiRef.addListenerForSingleValueEvent(object :
                                    ValueEventListener {
                                    override fun onDataChange(skladnikDataSnapshot: DataSnapshot) {
                                        val skladnik =
                                            skladnikDataSnapshot.getValue(Skladnik::class.java)
                                                ?: return

                                        if (skladnik.name.lowercase()
                                                .contains(searchTerm) && !dishesMatchedByName.contains(
                                                dish
                                            )
                                        ) {
                                            dishesMatchedByProduct.add(dish)
                                        }

                                        if (asyncTasksCount.decrementAndGet() == 0) {
                                            val combinedList = (dishesMatchedByName + dishesMatchedByProduct).distinct().toMutableList()
                                            dishAdapter.updateData(combinedList)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        // Handle cancelled
                                    }
                                })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle cancelled
                        }
                    })
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        profile = findViewById(R.id.profile_button)
        goback = findViewById(R.id.goback_btn)
        add = findViewById(R.id.add_button)
        add.visibility = View.INVISIBLE

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

        profile.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(applicationContext, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        })
        goback.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(applicationContext, CategoriesActivity::class.java)
            startActivity(intent)
            finish()
        })
    }


    override fun onDishClick(position: Int) {
        val comment = dishList[position]

        val intent = Intent(this, DishDetailActivity::class.java)
        intent.putExtra("posilek", comment.id)
        startActivity(intent)
    }

    override fun onDeleteClick(position: Int) {
        val dish = dishList[position]
        val database = Firebase.database.reference
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid!!

        dish.id?.let {
            database.child("dishes").child(it).child("liked").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val isLiked = dish.isLikedByUser(currentUserId)
                    if (isLiked) {
                        dish.liked.remove(currentUserId)
                    }
                    // Update the dish's liked list in the database
                    val dishRef = database.child("dishes").child(dish.id)
                    dishRef.setValue(dish)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //...
                }
            })
        }
    }

    override fun onEditClick(position: Int) {
        val dish = dishList[position]

        val intent = Intent(this, EditDishActivity::class.java)
        intent.putExtra("posilek", dish.id)
        startActivity(intent)
    }
}
