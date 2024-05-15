package com.example.softwareengineering

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import com.google.firebase.auth.FirebaseAuth

class CategoriesActivity : AppCompatActivity() {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var skladniki: ImageButton
    private lateinit var posilki_btn: ImageButton
    private lateinit var share_btn: ImageButton
    private lateinit var water_btn: ImageButton
    private lateinit var chart_btn: ImageButton
    private lateinit var dishcat_btn: ImageButton
    private lateinit var measurements_btn: ImageButton
    private lateinit var tips_btn: ImageButton
    private lateinit var category_btn: ImageButton
    private lateinit var favourite_btn: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        skladniki = findViewById(R.id.skladniki_btn)
        posilki_btn = findViewById(R.id.posilki_btn)
        share_btn = findViewById(R.id.share_btn)
        water_btn = findViewById(R.id.water_btn)
        chart_btn = findViewById(R.id.chart_btn)
        dishcat_btn = findViewById(R.id.dishcat_btn)
        measurements_btn = findViewById(R.id.measurements_btn)
        tips_btn = findViewById(R.id.tips_btn)
        favourite_btn = findViewById(R.id.favourite_btn)
        category_btn = findViewById(R.id.categoty_btn)

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
            var intent : Intent = Intent(applicationContext,ListOfPosilkiActivity::class.java)
            startActivity(intent)
            finish()
        })

        water_btn.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,Water::class.java)
            startActivity(intent)
            finish()
        })

        category_btn.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,DishCategories::class.java)
            startActivity(intent)
            finish()
        })
        measurements_btn.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,MeasurementsActivity::class.java)
            startActivity(intent)
            finish()
        })
        chart_btn.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,ChartActivity::class.java)
            startActivity(intent)
            finish()
        })
        tips_btn.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,TipsActivity::class.java)
            startActivity(intent)
            finish()
        })
        favourite_btn.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,FavouriteActivity::class.java)
            startActivity(intent)
            finish()
        })
        dishcat_btn.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext, DishCatActivity::class.java)
            startActivity(intent)
            finish()
        })

        share_btn.setOnClickListener(View.OnClickListener{
            var shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this cool app!")
            shareIntent.putExtra(Intent.EXTRA_TEXT, "https://github.com/VrMonterrey/SoftwareEngineering")
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        })

    }
}