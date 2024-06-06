package com.example.softwareengineering

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import model.*
import java.util.*


class DaylistActivity : AppCompatActivity(), DailyAdapter.DailyAdapterListener {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var profile: ImageButton

    private lateinit var monday: ImageButton
    private lateinit var tuesday: ImageButton
    private lateinit var wednesday: ImageButton
    private lateinit var thursday: ImageButton
    private lateinit var friday: ImageButton
    private lateinit var saturday: ImageButton
    private lateinit var sunday: ImageButton

    private lateinit var dishAdapter: DailyAdapter
    private lateinit var dishList: MutableList<DailyNutrition>
    private lateinit var dishRecyclerView: RecyclerView
    private lateinit var database: FirebaseDatabase
    private lateinit var dishRef: DatabaseReference
    private var selectedDay = 1


    // Function to handle day selection
    @SuppressLint("ResourceAsColor")
    private fun selectDay(day: Int, selectedButton: ImageButton) {

        val white = ColorStateList.valueOf(Color.WHITE)
        val green = ColorStateList.valueOf(R.color.yellow)

        monday.backgroundTintList = white
        tuesday.backgroundTintList = white
        wednesday.backgroundTintList = white
        thursday.backgroundTintList = white
        friday.backgroundTintList = white
        saturday.backgroundTintList = white
        sunday.backgroundTintList = white

        selectedButton.backgroundTintList = green

        selectedDay = day

        updateDishList()
    }

    // Function to update the dish list based on the selected day
    private fun updateDishList() {
        dishList.clear()
        val posilki = mutableListOf<DailyNutrition>()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        dishRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (posilekSnapshot in snapshot.children) {
                    val product = posilekSnapshot.getValue(DailyNutrition::class.java)

                    if (product != null && product.userId == currentUserId && product.day == selectedDay) {
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
    }


    fun calculateMacro(posilek: Posilki, callback: (MacroNutrients) -> Unit) {
        val posilkiId = posilek.id
        val databaseReference = FirebaseDatabase.getInstance().reference.child("composition")

        var sumCalories: Double = 0.0
        var sumProteins: Double = 0.0
        var sumCarbs: Double = 0.0
        var sumFats: Double = 0.0
        var amount: Int = 0

        databaseReference.orderByChild("posilkiId").equalTo(posilkiId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val totalIngredients = snapshot.childrenCount.toInt()
                    var processedIngredients = 0

                    for (skladPosilkiSnapshot in snapshot.children) {
                        val skladPosilki = skladPosilkiSnapshot.getValue(SkladPosilku::class.java)
                        skladPosilki?.let {
                            val skladnikId = it.skladnikId
                            val amountInGrams = it.amount
                            amount += amountInGrams

                            val skladnikReference = FirebaseDatabase.getInstance().reference.child("products").child(skladnikId)
                            skladnikReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(skladnikSnapshot: DataSnapshot) {
                                    val skladnik = skladnikSnapshot.getValue(Skladnik::class.java)
                                    skladnik?.let { skladnik ->
                                        val skladnikCaloriesPer100g = skladnik.calories
                                        val skladnikProteinsPer100g = skladnik.protein
                                        val skladnikCarbsPer100g = skladnik.carbs
                                        val skladnikFatsPer100g = skladnik.fat

                                        val skladnikCalories: Double = (skladnikCaloriesPer100g * amountInGrams) / 100
                                        val skladnikProteins: Double = (skladnikProteinsPer100g * amountInGrams) / 100
                                        val skladnikCarbs: Double = (skladnikCarbsPer100g * amountInGrams) / 100
                                        val skladnikFats: Double = (skladnikFatsPer100g * amountInGrams) / 100

                                        sumCalories += skladnikCalories
                                        sumProteins += skladnikProteins
                                        sumCarbs += skladnikCarbs
                                        sumFats += skladnikFats

                                        processedIngredients++
                                        if (processedIngredients == totalIngredients) {
                                            val nutrients = MacroNutrients(sumCalories, sumProteins, sumCarbs, sumFats, amount)
                                            callback(nutrients)
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Handle error
                                }
                            })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }
    companion object {
        private const val SHARED_PREFS = "sharedPrefs"
        private const val LAST_DAY = "lastDay"
    }
    fun getStartOfDayTimestamp(): Double {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis.toDouble()
    }

    fun getEndOfDayTimestamp(): Double {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis.toDouble()
    }

    private fun saveDay(day: Int) {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(LAST_DAY, day)
        editor.apply()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daylist)


        monday = findViewById(R.id.monday)
        tuesday = findViewById(R.id.tuesday)
        wednesday = findViewById(R.id.wednesday)
        thursday = findViewById(R.id.thursday)
        friday = findViewById(R.id.friday)
        saturday = findViewById(R.id.saturday)
        sunday = findViewById(R.id.sunday)


        dishRecyclerView = findViewById(R.id.dishRecyclerView)
        val notificationUtils = NotificationUtils()
        dishAdapter = DailyAdapter(mutableListOf(), this, notificationUtils)

        dishRecyclerView.setHasFixedSize(true)
        dishRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        dishRecyclerView.adapter = dishAdapter


        database = FirebaseDatabase.getInstance()
        dishRef = database.getReference("scheduled")
        dishList = mutableListOf()


        monday.setOnClickListener {
            val day = 1
            saveDay(day)
            selectDay(day, monday)
        }

        tuesday.setOnClickListener {
            val day = 2
            saveDay(day)
            selectDay(day, tuesday)
        }

        wednesday.setOnClickListener {
            val day = 3
            saveDay(day)
            selectDay(day, wednesday)
        }

        thursday.setOnClickListener {
            val day = 4
            saveDay(day)
            selectDay(day, thursday)
        }

        friday.setOnClickListener {
            val day = 5
            saveDay(day)
            selectDay(day, friday)
        }

        saturday.setOnClickListener {
            val day = 6
            saveDay(day)
            selectDay(day, saturday)
        }

        sunday.setOnClickListener {
            val day = 7
            saveDay(day)
            selectDay(day, sunday)
        }

        val sharedPreferences = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val lastDay = sharedPreferences.getInt(LAST_DAY, 1)

        when(lastDay) {
            1 -> selectDay(lastDay, monday)
            2 -> selectDay(lastDay, tuesday)
            3 -> selectDay(lastDay, wednesday)
            4 -> selectDay(lastDay, thursday)
            5 -> selectDay(lastDay, friday)
            6 -> selectDay(lastDay, saturday)
            7 -> selectDay(lastDay, sunday)
        }


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
        database.child("dishes").child(daily.posilekId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dish = snapshot.getValue(Posilki::class.java)
                    if (dish != null) {
                        calculateMacro(dish) { nutrients ->
                            val eaten = Eaten(
                                id = database.child("eaten").push().key,
                                date = System.currentTimeMillis(),
                                userId = currentUserId.orEmpty(),
                                calories = nutrients.calories,
                                protein = nutrients.proteins,
                                carbs = nutrients.carbs,
                                fat = nutrients.fats,
                                dishName = dish.name,
                                photoUrl = dish.photoUrl,
                                category = dish.category
                            )

                            if (eaten.id != null) {
                                database.child("eaten").child(eaten.id!!).setValue(eaten)
                                    .addOnSuccessListener {
                                        database.child("eaten")
                                            .orderByChild("date")
                                            .startAt(getStartOfDayTimestamp())
                                            .endAt(getEndOfDayTimestamp())
                                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    var totalCalories = 0.0
                                                    for (postSnapshot in snapshot.children) {
                                                        val eatenEntry = postSnapshot.getValue(Eaten::class.java)
                                                        totalCalories += eatenEntry?.calories ?: 0.0
                                                    }

                                                    database.child("users").child(currentUserId.orEmpty())
                                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                                val user = snapshot.getValue(User::class.java)
                                                                val maxCalories = user?.calsMax ?: 0.0

                                                                val message = if (totalCalories > maxCalories) {
                                                                    "Zjadłeś już dziś za dużo!"
                                                                } else {
                                                                    "Posiłek spożyty"
                                                                }

                                                                Toast.makeText(this@DaylistActivity, message, Toast.LENGTH_SHORT)
                                                                    .show()
                                                            }

                                                            override fun onCancelled(error: DatabaseError) {
                                                                Log.w(TAG, "loadUser:onCancelled", error.toException())
                                                            }
                                                        })
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Log.w(TAG, "loadEatenEntries:onCancelled", error.toException())
                                                }
                                            })
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            this@DaylistActivity,
                                            "Błąd podczas dodawania posiłku do spożytych: ${it.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "loadDish:onCancelled", error.toException())
                }
            })
    }
    override fun onDishClick(position: Int) {
        val daily = dishList[position]

        val intent = Intent(this, DishDetailActivity::class.java)
        intent.putExtra("posilek", daily.posilekId)
        intent.putExtra("sourceActivity", "DaylistActivity")
        startActivity(intent)
    }

}