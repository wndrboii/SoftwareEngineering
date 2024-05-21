package com.example.softwareengineering

import NotificationUtils
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
import com.example.softwareengineering.model.DailyNutrition
import com.example.softwareengineering.model.Eaten
import com.example.softwareengineering.model.Posilki
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class DaylistActivity : AppCompatActivity(), DailyAdapter.PosilkiAdapterListener {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var profile: ImageButton


    private lateinit var dishAdapter: DailyAdapter
    private lateinit var dishList: MutableList<DailyNutrition>
    private lateinit var dishRecyclerView: RecyclerView
    private lateinit var database: FirebaseDatabase
    private lateinit var dishRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daylist)

        dishRecyclerView = findViewById(R.id.dishRecyclerView)
        val notificationUtils = NotificationUtils()
        dishAdapter = DailyAdapter(mutableListOf(), this, notificationUtils)

        dishRecyclerView.setHasFixedSize(true)
        dishRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        dishRecyclerView.adapter = dishAdapter

        database = FirebaseDatabase.getInstance()
        dishRef = database.getReference("scheduled")

        dishList = mutableListOf()


        dishRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dishList.clear()
                val posilki = mutableListOf<DailyNutrition>()
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                for (posilekSnapshot in snapshot.children) {
                    val product = posilekSnapshot.getValue(DailyNutrition::class.java)

                    if (product != null && product.userId == currentUserId) {
                        posilki.add(product)
                    }

                }

                posilki.sortBy { it.time }
                dishList.addAll(posilki)
                dishAdapter.updateData(posilki)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        profile = findViewById(R.id.profile_button)


        //goback = findViewById(R.id.goback_btn)

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


    }

    override fun onDeleteClick(position: Int) {
        val dish = dishList[position]
        dish.id?.let {
            val dishRef = database.getReference("scheduled/$it")
            dishRef.removeValue().addOnSuccessListener {
                Toast.makeText(this, "Posiłek usunięty z listy na dzień", Toast.LENGTH_SHORT).show()
            }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Błąd podczas usuwania posiłku: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onSuccessClick(position: Int) {
        val daily = dishList[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val database = Firebase.database.reference
        // Create a new instance of the Eaten data class
        val eaten = Eaten(
            id = database.child("eaten").push().key,
            date = System.currentTimeMillis(),
            userId = currentUserId.orEmpty(),
            posilekId = daily.posilekId
        )

        if (eaten.id != null) {
            database.child("eaten").child(eaten.id!!).setValue(eaten)
                .addOnSuccessListener {
                    Toast.makeText(this, "Posiłek spożyty", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Błąd podczas dodawania posiłku do spożytych: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onDishClick(position: Int) {
        val daily = dishList[position]

        val intent = Intent(this, DishDetailActivity::class.java)
        intent.putExtra("posilek", daily.posilekId)
        startActivity(intent)
    }

}