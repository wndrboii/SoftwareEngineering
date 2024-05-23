package com.example.softwareengineering

import ProductAdapter
import TipsAdapter
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareengineering.model.Skladnik
import com.example.softwareengineering.model.Tip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class TipsActivity : AppCompatActivity(), TipsAdapter.TipAdapterListener {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
//    private lateinit var profile: ImageButton

    private lateinit var remove: ImageButton
    private lateinit var tipsAdapter: TipsAdapter
    private lateinit var tipList: MutableList<Tip>
    private lateinit var tipRecyclerView: RecyclerView
    private lateinit var database: FirebaseDatabase
    private lateinit var tipRef: DatabaseReference
    private lateinit var createTip: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips)

        tipRecyclerView = findViewById(R.id.tipRecyclerView)
        tipsAdapter = TipsAdapter(mutableListOf(), this)
        tipRecyclerView.adapter = tipsAdapter

        database = FirebaseDatabase.getInstance()
        tipRef = database.getReference("tips")

        tipList = mutableListOf()

        tipRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tipList.clear()
                val tips = mutableListOf<Tip>()
                for (tipSnapshot in snapshot.children) {
                    val tip = tipSnapshot.getValue(Tip::class.java)
                    tip?.let {
                        tips.add(it)
                    }
                }
                tipList.addAll(tips)
                tipsAdapter.updateData(tips)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }

        })

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
//        profile = findViewById(R.id.profile_button)

        createTip = findViewById(R.id.create_tip)

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

//        profile.setOnClickListener(View.OnClickListener{
//            var intent : Intent = Intent(applicationContext,ProfileActivity::class.java)
//            startActivity(intent)
//            finish()
//        })

        createTip.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(applicationContext, CreateTipActivity::class.java)
            startActivity(intent)
            finish()
        })

        tipsAdapter.setOnDeleteClickListener(object : TipsAdapter.OnDeleteClickListener {
            override fun onDeleteClick(position: Int) {
                val tip = tipList[position]
                tip.id?.let {
                    val tipRef = database.getReference("tips/$it")
                    tipRef.removeValue()
                }
            }
        })

    }

    override fun onDeleteClick(position: Int) {
        val tip = tipList[position]
        tip.id?.let {
            val tipRef = database.getReference("tips/$it")
            tipRef.removeValue().addOnSuccessListener {
                Toast.makeText(this, "Podpowiedź została pomyślnie usunięta", Toast.LENGTH_SHORT).show()
            }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Błąd podczas usuwania podpowiedzi: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onEditClick(position: Int) {
        val tip = tipList[position]

        val intent = Intent(this, EditTipActivity::class.java)
        intent.putExtra("podpowiedz", tip.id)
        startActivity(intent)
    }

}
