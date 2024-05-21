package com.example.softwareengineering

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var profile: ImageButton

    private lateinit var editTextFirstName: EditText
    private lateinit var editTextLastName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var spinnerGender: Spinner
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switchNotifications: Switch
    private lateinit var avatarImage: ImageView
    private lateinit var editImage: ImageView

    private lateinit var getContent: ActivityResultLauncher<String>
    private var photoUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //Field init.
        editTextFirstName = findViewById(R.id.editTextFirstName)
        editTextLastName = findViewById(R.id.editTextLastName)
        editTextEmail = findViewById(R.id.editTextEmail)
        spinnerGender = findViewById(R.id.spinnerGender)
        switchNotifications = findViewById(R.id.switchNotifications)
        avatarImage = findViewById(R.id.avatar_profile)

        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val database = Firebase.database.reference
        database.child("users").child(currentUser).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val firstName = dataSnapshot.child("firstName").value as? String
                val lastName = dataSnapshot.child("lastName").value as? String
                val email = dataSnapshot.child("email").value as? String
                val gender = dataSnapshot.child("gender").value as? String
                val notificationsEnabled = dataSnapshot.child("notificationsEnabled").value as? Boolean
                if (email != null) {
                    editTextFirstName.setText(firstName)
                    editTextLastName.setText(lastName)
                    editTextEmail.setText(email)
                    spinnerGender.setSelection(getGenderIndex(gender?: "Nie wybrana"))
                    switchNotifications.isChecked = notificationsEnabled?: true
                    Glide.with(applicationContext)
                        .load(dataSnapshot.child("photoUrl").value).apply(RequestOptions.circleCropTransform())
                        .into(avatarImage)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //...
            }
        })

        //Edit image
        editImage = findViewById<ImageView>(R.id.edit_avatar_btn)

        val storageRef = FirebaseStorage.getInstance().reference.child("images")

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                Glide.with(applicationContext)
                    .load(uri).apply(RequestOptions.circleCropTransform())
                    .into(avatarImage)
//                avatarImage.setImageURI(uri)
                val imageRef = storageRef.child(uri.lastPathSegment!!.substringAfterLast("/"))
                val uploadTask = contentResolver?.openInputStream(uri)?.readBytes()?.let { imageRef.putBytes(it) }
                uploadTask?.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        photoUrl = uri.toString()
                        Log.d(ContentValues.TAG, "Image URL: $photoUrl")

                        val user = FirebaseAuth.getInstance().currentUser
                        if (user == null) {
                            Toast.makeText(this, "Błąd: nie znaleziono użytkownika", Toast.LENGTH_SHORT).show()
                        }

                        val userId = user?.uid

                        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId?: "")
                        userRef.child("photoUrl").setValue(photoUrl)

                        Toast.makeText(this, "Profil został zapisany", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        editImage.setOnClickListener {
            getContent.launch("image/*")
        }

        val genderSpinner: MutableList<String> = mutableListOf()
        genderSpinner.add("Męska")
        genderSpinner.add("Żeńska")
        genderSpinner.add("Nie wybrana")

        val adapter = ArrayAdapter(this@ProfileActivity, R.layout.spinner_item_layout, genderSpinner)
        spinnerGender.adapter = adapter

        //On submit btn click
        val buttonSave: ImageButton = findViewById(R.id.buttonSave)
        buttonSave.setOnClickListener {
            saveProfile()
        }

        //Navigation
        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        profile = findViewById(R.id.profile_button)

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

        profile.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext,ProfileActivity::class.java)
            startActivity(intent)
            finish()
        })

        logout.setOnClickListener(View.OnClickListener{
            FirebaseAuth.getInstance().signOut()
            var intent : Intent = Intent(applicationContext, login::class.java)
            startActivity(intent)
            finish()
        })
    }

    private fun getGenderIndex(gender: String): Int {
        if(gender == "Żeńska"){
            return 1
        } else if(gender == "Męska"){
            return 0
        } else {
            return 2
        }
    }

    private fun saveProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Błąd: nie znaleziono użytkownika", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = user.uid

        val firstName = editTextFirstName.text.toString()
        val lastName = editTextLastName.text.toString()
        val email = editTextEmail.text.toString()
        val gender = spinnerGender.selectedItem.toString()
        val notificationsEnabled = switchNotifications.isChecked

        val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)
        userRef.child("firstName").setValue(firstName)
        userRef.child("lastName").setValue(lastName)
        userRef.child("email").setValue(email)
        userRef.child("gender").setValue(gender)
        userRef.child("notificationsEnabled").setValue(notificationsEnabled)

        Toast.makeText(this, "Profil został zapisany", Toast.LENGTH_SHORT).show()
    }


}