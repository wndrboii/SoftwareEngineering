package com.example.softwareengineering

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import com.example.softwareengineering.model.Posilki
import com.google.firebase.auth.FirebaseAuth

class CategoriesActivity : AppCompatActivity() {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var skladniki: ImageButton
    private lateinit var posilki_btn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        skladniki = findViewById(R.id.skladniki_btn)
        posilki_btn = findViewById(R.id.posilki_btn)

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

        logout.setOnClickListener(View.OnClickListener{
            FirebaseAuth.getInstance().signOut()
            var intent : Intent = Intent(applicationContext,login::class.java)
            startActivity(intent)
            finish()
        })

        skladniki.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,SkladnikiActivity::class.java)
            startActivity(intent)
            finish()
        })

        posilki_btn.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,PosilkiActivity::class.java)
            startActivity(intent)
            finish()
        })

    }
}