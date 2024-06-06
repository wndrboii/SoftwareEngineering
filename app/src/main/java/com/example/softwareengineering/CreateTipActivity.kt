package com.example.softwareengineering

import android.content.Intent
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import model.Tip

class CreateTipActivity : AppCompatActivity() {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var profile: ImageButton

    private lateinit var topic: EditText
    private lateinit var text: EditText
    private lateinit var addButton: ImageButton

    private lateinit var goback: ImageButton
    private var userPhotoUrl: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_tip)
        val rootView = findViewById<ScrollView>(R.id.scroll_view)
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val r = Rect()
                rootView.getWindowVisibleDisplayFrame(r)
                val screenHeight = rootView.rootView.height

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                val keypadHeight = screenHeight - r.bottom

                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    // keyboard is opened
                    rootView.setPadding(0, 0, 0, keypadHeight-300)
                } else {
                    // keyboard is closed
                    rootView.setPadding(0, 0, 0, 0)
                }
            }
        })

        val addButton = findViewById<ImageButton>(R.id.submit_btn)
        addButton.setOnClickListener {
            topic = findViewById<EditText>(R.id.tip_topic)
            text = findViewById<EditText>(R.id.tip_text)

            val Topic = topic.text.toString()
            val Text = text.text.toString()

            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = currentUser?.uid



                if (Topic.isNotEmpty() && Text.isNotEmpty()) {
                    val database = Firebase.database.reference
                    val tip = Tip(
                        id = database.child("tips").push().key,
                        topic = Topic,
                        text = Text,
                        userId = currentUserId,
                    )

                    if (tip.id != null) {
                        database.child("tips").child(tip.id!!).setValue(tip)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Nowa podpowiedź dodana pomyślnie",
                                    Toast.LENGTH_SHORT
                                ).show()
                                topic.text.clear()
                                text.text.clear()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Błąd podczas dodawania podpowiedzi: ${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Wszystkie pola muszą być wypełnione poprawnie", Toast.LENGTH_SHORT).show()
                }
        }

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        profile = findViewById(R.id.profile_button)

        goback = findViewById(R.id.goback_btn)

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

        profile.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,ProfileActivity::class.java)
            startActivity(intent)
            finish()
        })

        logout.setOnClickListener(View.OnClickListener {
            FirebaseAuth.getInstance().signOut()
            var intent: Intent = Intent(applicationContext, login::class.java)
            startActivity(intent)
            finish()
        })

        goback.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext, TipsActivity::class.java)
            startActivity(intent)
            finish()
        })

    }
}