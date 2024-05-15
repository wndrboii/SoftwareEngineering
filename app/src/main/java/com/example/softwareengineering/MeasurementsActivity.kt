package com.example.softwareengineering

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.softwareengineering.model.Measurement
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.softwareengineering.model.Skladnik

class MeasurementsActivity : AppCompatActivity() {


    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var height: EditText
    private lateinit var weight: EditText
    private lateinit var shoulder: EditText
    private lateinit var arm: EditText
    private lateinit var chest: EditText
    private lateinit var waist: EditText
    private lateinit var hips: EditText
    private lateinit var thigh: EditText
    private lateinit var calves: EditText

    private lateinit var addButton: ImageButton

    private lateinit var measurementsArr: TextView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measurements)

        val addButton = findViewById<ImageButton>(R.id.submit_btn)
        addButton.setOnClickListener {
            height = findViewById<EditText>(R.id.height)
            weight = findViewById<EditText>(R.id.weight)
            shoulder = findViewById<EditText>(R.id.shoulder)
            arm = findViewById<EditText>(R.id.arm)
            chest = findViewById<EditText>(R.id.chest)
            waist = findViewById<EditText>(R.id.waist)
            hips = findViewById<EditText>(R.id.hips)
            thigh = findViewById<EditText>(R.id.thigh)
            calves = findViewById<EditText>(R.id.calves)

            val Height = height.text.toString().toIntOrNull()
            val Weight = weight.text.toString().toIntOrNull()
            val Shoulder = shoulder.text.toString().toIntOrNull()
            val Arm = arm.text.toString().toIntOrNull()
            val Chest = chest.text.toString().toIntOrNull()
            val Waist = waist.text.toString().toIntOrNull()
            val Hips = hips.text.toString().toIntOrNull()
            val Thigh = thigh.text.toString().toIntOrNull()
            val Calves = calves.text.toString().toIntOrNull()

            if (Height != null && Weight != null && Shoulder != null && Arm != null && Chest != null &&
                Waist != null && Hips != null && Thigh != null && Calves != null
            ) {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val currentUserId = currentUser?.uid

                val database = Firebase.database.reference
                val meas = currentUserId?.let { userId ->
                    Measurement(
                        id = database.child("measurements").push().key,
                        height = Height,
                        weight = Weight,
                        shoulder = Shoulder,
                        arm = Arm,
                        chest = Chest,
                        waist = Waist,
                        hips = Hips,
                        thigh = Thigh,
                        calves = Calves,
                        userId = userId
                    )
                }

                if (meas != null && meas.id != null) {
                    database.child("measurements").child(meas.id!!).setValue(meas)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Nowy pomiar dodany pomyślnie", Toast.LENGTH_SHORT).show()
                            height.text.clear()
                            weight.text.clear()
                            shoulder.text.clear()
                            arm.text.clear()
                            chest.text.clear()
                            waist.text.clear()
                            hips.text.clear()
                            thigh.text.clear()
                            calves.text.clear()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Błąd podczas dodawania pomiaru: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            } else {
                Toast.makeText(this, "Please enter valid values for all fields", Toast.LENGTH_SHORT).show()
            }
        }

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        measurementsArr = findViewById(R.id.measurements_arr)

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

        measurementsArr.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(applicationContext, ListOfMeasurementsActivity::class.java)
            startActivity(intent)
            finish()
        })

    }
}