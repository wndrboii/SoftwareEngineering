package com.example.softwareengineering

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class register : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonReg: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar
    private lateinit var textView: TextView

    private lateinit var database: FirebaseDatabase

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            var intent: Intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        editTextEmail = findViewById(R.id.email)
        editTextPassword = findViewById(R.id.password)
        buttonReg = findViewById(R.id.btn_register)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.loginNow)
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance()

        textView.setOnClickListener(View.OnClickListener {
            var intent: Intent = Intent(applicationContext, login::class.java)
            startActivity(intent)
            finish()
        })

        buttonReg.setOnClickListener(View.OnClickListener {
            progressBar.visibility=View.VISIBLE
            var email: String = editTextEmail.text.toString()
            var password: String = editTextPassword.text.toString()

            if(TextUtils.isEmpty(email)){
                Toast.makeText(this,"Enter email", Toast.LENGTH_SHORT).show()
                return@OnClickListener
        }
            if(TextUtils.isEmpty(password)){
                Toast.makeText(this,"Enter password", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility=View.GONE
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val userId = user?.uid ?: ""
                        val database2 = Firebase.database.reference
                        val newUser = User(
                            id = userId,
                            email = email
                        )

                        database2.child("users").child(userId).setValue(newUser)

                        Toast.makeText(
                            this, "Registration successful!",
                            Toast.LENGTH_SHORT
                        ).show()
                        var intent : Intent = Intent(applicationContext,login::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this, "Registration failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
        })
    }
}