package com.example.softwareengineering
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.softwareengineering.ui.theme.PieChartView
import com.example.softwareengineering.ui.theme.QuadLineChart
import model.Eaten
import model.Macros
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var profile: ImageButton
    private lateinit var categories: ImageButton

    private lateinit var carbsValueField: TextView
    private lateinit var proteinValueField: TextView
    private lateinit var fatValueField: TextView

    private lateinit var currentMonthField: TextView

    private lateinit var currentValue: TextView
    private lateinit var targetedValue: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var textView: TextView
    private var user: FirebaseUser? = null

    suspend fun fetchUserEatenEntries(userId: String): List<Eaten> = suspendCoroutine { cont ->
        val eatenRef = FirebaseDatabase.getInstance().getReference("eaten")
        eatenRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val eatenEntries = dataSnapshot.children.mapNotNull { it.getValue(Eaten::class.java) }
                cont.resume(eatenEntries)
            }
            override fun onCancelled(error: DatabaseError) {
                cont.resumeWithException(error.toException())
            }
        })
    }
    fun aggregateEatenEntriesByDay(eatenEntries: List<Eaten>): Map<String, Macros> {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val aggregatedEntries = mutableMapOf<String, Macros>()

        for (entry in eatenEntries) {
            val date = format.format(Date(entry.date))
            val existingMacros = aggregatedEntries[date]

            if (existingMacros == null) {
                aggregatedEntries[date] = Macros(
                    cals = entry.calories,
                    prots = entry.protein,
                    carbs = entry.carbs,
                    fats = entry.fat,
                    date = entry.date
                )
            } else {
                existingMacros.cals += entry.calories
                existingMacros.prots += entry.protein
                existingMacros.carbs += entry.carbs
                existingMacros.fats += entry.fat
            }
        }
        return aggregatedEntries
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        profile = findViewById(R.id.profile_button)

        //Pie Chart el. init.
        carbsValueField = findViewById<TextView>(R.id.carbs_value)
        proteinValueField = findViewById<TextView>(R.id.protein_value)
        fatValueField = findViewById<TextView>(R.id.fat_value)

        //Line Chart el. init.
        currentMonthField = findViewById<TextView>(R.id.currentMonthLabel)

        currentValue = findViewById<TextView>(R.id.current_value)
        targetedValue = findViewById<TextView>(R.id.targeted_value)

        //Current user email el. init.
        textView = findViewById(R.id.user)
        auth = Firebase.auth
        user = auth.currentUser
        if (user == null){
            var intent : Intent = Intent(applicationContext,login::class.java)
            startActivity(intent)
            finish()
        }
        else{
            textView.text=user?.email
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser?.uid ?: ""

        var proteinValue: Double = 0.0
        var carbsValue: Double = 0.0
        var fatValue: Double = 0.0
        var calsValue: Double = 0.0

        //Charts
        val currentDate = System.currentTimeMillis()
        println("Current date: $currentDate")

        val composeView = findViewById<ComposeView>(R.id.composeView)
        val composeView2 = findViewById<ComposeView>(R.id.composeView2)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val eatenEntries = fetchUserEatenEntries(currentUserId)
                val aggregatedMacros = aggregateEatenEntriesByDay(eatenEntries)

                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                var macrosForToday: Macros? = null

                for ((date, macros) in aggregatedMacros) {
                    if (date == today) {
                        macrosForToday = macros
                        proteinValue += macrosForToday.prots
                        carbsValue += macrosForToday.carbs
                        fatValue += macrosForToday.fats
                        calsValue += macrosForToday.cals
                        break
                    }
                }

                val currentMonth = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
                val monthDays = 1.rangeTo(31).toList()
                val listOfPairs = mutableListOf<Pair<Int, Double>>()

                for(day in monthDays){
                    if(day == 2){
                        listOfPairs.add(day to 800.0)
                    }else if(day == 3){
                        listOfPairs.add(day to 1100.0)
                    }
                    else if(day == 4){
                        listOfPairs.add(day to 600.0)
                    }else if(day == 8){
                        listOfPairs.add(day to 800.0)
                    }else if(day == 10){
                        listOfPairs.add(day to 400.0)
                    }else if(day == 12){
                        listOfPairs.add(day to 600.0)
                    }else {
                        listOfPairs.add(day to 0.0)
                    }
                }

                for ((date, macros) in aggregatedMacros) {
                    val month = date.substring(5, 7)

                    if (month == currentMonth) {
                        val day: Int = SimpleDateFormat("dd", Locale.getDefault()).format(Date(macros.date)).toInt()
                        val kcal: Double = macros.cals

                        for (i in listOfPairs.indices) {
                            if (listOfPairs[i].first == day) {
                                listOfPairs[i] = listOfPairs[i].first to kcal
                                break
                            }
                        }
                    }
                }

                val database = Firebase.database.reference
                if (currentUser != null) {
                    val maxValue = suspendCoroutine<Double> { continuation ->
                        database.child("users").child(currentUser.uid).addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val value = dataSnapshot.child("calsMax").getValue(Double::class.java)!!
                                targetedValue.text = value.toString()
                                continuation.resume(value)
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                continuation.resumeWithException(databaseError.toException())
                            }
                        })
                    }

                    withContext(Dispatchers.Main) {
                        carbsValueField.text = String.format("%.1f", carbsValue)
                        proteinValueField.text = String.format("%.1f", proteinValue)
                        fatValueField.text = String.format("%.1f", fatValue)

                        currentValue.text = String.format("%.1f", calsValue)

                        currentMonthField.text = "Miesiąc: ${SimpleDateFormat("MM", Locale.getDefault()).format(Date())}"

                        composeView.setContent {
                            PieChartView(data = mapOf(
                                Pair("Węglewodany", carbsValue.roundToInt()),
                                Pair("Białko", proteinValue.roundToInt()),
                                Pair("Tłuszcze", fatValue.roundToInt()),
                            ))
                        }

                        composeView2.setContent {
                            QuadLineChart(
                                data = listOfPairs,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                redLineValue = maxValue
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }


        home.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,MainActivity::class.java)
            startActivity(intent)
            finish()
        })

        categories.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,CategoriesActivity::class.java)
            startActivity(intent)
            finish()
        })

        profile.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,ProfileActivity::class.java)
            startActivity(intent)
            finish()
        })

        logout.setOnClickListener(View.OnClickListener{
            FirebaseAuth.getInstance().signOut()
            var intent : Intent = Intent(applicationContext,login::class.java)
            startActivity(intent)
            finish()
        })

    }

}
