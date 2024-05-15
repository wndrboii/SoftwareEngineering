package com.example.softwareengineering

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.softwareengineering.model.Measurement
import com.example.softwareengineering.model.Skladnik
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EditMeasurementActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var measRef: DatabaseReference
    private lateinit var measId: String
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
        setContentView(R.layout.activity_edit_measurement)

        database = FirebaseDatabase.getInstance()
        measRef = database.getReference("measurements")

        measId = intent.getStringExtra("pomiar") ?: ""

        height = findViewById<EditText>(R.id.height)
        weight = findViewById<EditText>(R.id.weight)
        shoulder = findViewById<EditText>(R.id.shoulder)
        arm = findViewById<EditText>(R.id.arm)
        chest = findViewById<EditText>(R.id.chest)
        waist = findViewById<EditText>(R.id.waist)
        hips = findViewById<EditText>(R.id.hips)
        thigh = findViewById<EditText>(R.id.thigh)
        calves = findViewById<EditText>(R.id.calves)

        measRef.child(measId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val meas = snapshot.getValue(Measurement::class.java)
                meas?.let {
                    height.setText(meas.height.toString())
                    weight.setText(meas.weight.toString())
                    shoulder.setText(meas.shoulder.toString())
                    arm.setText(meas.arm.toString())
                    chest.setText(meas.chest.toString())
                    waist.setText(meas.waist.toString())
                    hips.setText(meas.hips.toString())
                    thigh.setText(meas.thigh.toString())
                    calves.setText(meas.calves.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        val addButton = findViewById<ImageButton>(R.id.submit_btn)
        addButton.setOnClickListener {
            val Height = height.text.toString().toIntOrNull()
            val Weight = weight.text.toString().toIntOrNull()
            val Shoulder = shoulder.text.toString().toIntOrNull()
            val Arm = arm.text.toString().toIntOrNull()
            val Chest = chest.text.toString().toIntOrNull()
            val Waist = waist.text.toString().toIntOrNull()
            val Hips = hips.text.toString().toIntOrNull()
            val Thigh = thigh.text.toString().toIntOrNull()
            val Calves = calves.text.toString().toIntOrNull()

            if (Height == null || Weight == null || Shoulder == null || Arm == null || Chest == null ||
                Waist == null || Hips == null || Thigh == null || Calves == null
            ) {
                Toast.makeText(this, "Please fill out all fields with valid values", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val measurement = Measurement(measId, Height, Weight, Shoulder, Arm, Chest, Waist, Hips, Thigh, Calves)
            measRef.child(measId).setValue(measurement)
                .addOnSuccessListener {
                    Toast.makeText(this, "Measurement updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Error updating measurement: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
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
