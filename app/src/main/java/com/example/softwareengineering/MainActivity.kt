package com.example.softwareengineering

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var profile: ImageButton
    private lateinit var categories: ImageButton

    private lateinit var auth: FirebaseAuth
    private lateinit var textView: TextView
    private var user: FirebaseUser? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        profile = findViewById(R.id.profile_button)

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
