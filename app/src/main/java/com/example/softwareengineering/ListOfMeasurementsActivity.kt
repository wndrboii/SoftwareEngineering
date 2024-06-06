package com.example.softwareengineering

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import model.Measurement
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ListOfMeasurementsActivity : AppCompatActivity(), MeasurementAdapter.MeasurementAdapterListener {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var profile: ImageButton

    private lateinit var goback: ImageButton
    private lateinit var remove: ImageButton
    private lateinit var measAdapter: MeasurementAdapter
    private lateinit var measList: MutableList<Measurement>
    private lateinit var measRecyclerView: RecyclerView
    private lateinit var database: FirebaseDatabase
    private lateinit var measRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_of_measurements)

        measRecyclerView = findViewById(R.id.measRecyclerView)
        measAdapter = MeasurementAdapter(mutableListOf(), this)
        measRecyclerView.adapter = measAdapter

        database = FirebaseDatabase.getInstance()
        measRef = database.getReference("measurements")

        measList = mutableListOf()

        measRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                measList.clear()
                val meases = mutableListOf<Measurement>()
                val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
                for (measSnapshot in snapshot.children) {
                    val meas = measSnapshot.getValue(Measurement::class.java)
                    meas?.let {
                        // Check if the measurement belongs to the current user
                        if (it.userId == currentUserID) {
                            meases.add(it)
                        }
                    }
                }
                measList.addAll(meases)
                measAdapter.updateData(meases)
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

//        goback.setOnClickListener(View.OnClickListener{
//            var intent : Intent = Intent(applicationContext, MeasurementsActivity::class.java)
//            startActivity(intent)
//            finish()
//        })

        measAdapter.setOnDeleteClickListener(object : MeasurementAdapter.OnDeleteClickListener {
            override fun onDeleteClick(position: Int) {
                val meas = measList[position]
                meas.id?.let {
                    val measRef = database.getReference("measurements/$it")
                    measRef.removeValue()
                }
            }
        })

    }

    override fun onDeleteClick(position: Int) {
        val meas = measList[position]
        meas.id?.let {
            val measRef = database.getReference("measurements/$it")
            measRef.removeValue().addOnSuccessListener {
                Toast.makeText(this, "Pomiar został pomyślnie usunięty", Toast.LENGTH_SHORT).show()
            }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Błąd podczas usuwania pomiaru: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onEditClick(position: Int) {
        val meas = measList[position]

        val intent = Intent(this, EditMeasurementActivity::class.java)
        intent.putExtra("pomiar", meas.id)
        startActivity(intent)
    }

}
