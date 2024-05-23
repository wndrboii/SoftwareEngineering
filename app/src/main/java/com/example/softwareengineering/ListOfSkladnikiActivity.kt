package com.example.softwareengineering

import ProductAdapter
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareengineering.model.Skladnik
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ListOfSkladnikiActivity : AppCompatActivity(), ProductAdapter.ProductAdapterListener {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var profile: ImageButton

    private lateinit var goback: ImageButton
    private lateinit var verticalMenu: ImageButton
    private lateinit var searchEditText: EditText
    private lateinit var productAdapter: ProductAdapter
    private lateinit var productList: MutableList<Skladnik>
    private lateinit var productRecyclerView: RecyclerView
    private lateinit var database: FirebaseDatabase
    private lateinit var productRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_of_skladniki)

        productRecyclerView = findViewById(R.id.productRecyclerView)
        searchEditText = findViewById(R.id.search)
        productAdapter = ProductAdapter(mutableListOf(), this)
        productRecyclerView.adapter = productAdapter

        database = FirebaseDatabase.getInstance()
        productRef = database.getReference("products")

        productList = mutableListOf()

        productRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                val products = mutableListOf<Skladnik>()
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Skladnik::class.java)
                    product?.let {
                        products.add(it)
                    }
                }
                productList.addAll(products)
                productAdapter.updateData(products)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchTerm = s.toString().lowercase()
                val filteredList = productList.filter { it.name.lowercase().contains(searchTerm) }
                productAdapter.updateData(filteredList as MutableList<Skladnik>)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        logout = findViewById(R.id.logout_button)
        home = findViewById(R.id.home_button)
        categories = findViewById(R.id.categories_btn)
        profile = findViewById(R.id.profile_button)

        goback = findViewById(R.id.goback_btn)
        verticalMenu = findViewById(R.id.vertical_menu)

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

        goback.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext, CategoriesActivity::class.java)
            startActivity(intent)
            finish()
        })

        verticalMenu.setOnClickListener(View.OnClickListener{
            var intent : Intent = Intent(applicationContext, SkladnikiActivity::class.java)
            startActivity(intent)
            finish()
        })

        productAdapter.setOnDeleteClickListener(object : ProductAdapter.OnDeleteClickListener {
            override fun onDeleteClick(position: Int) {
                val product = productList[position]
                product.id?.let {
                    val productRef = database.getReference("products/$it")
                    productRef.removeValue()
                }
            }
        })

    }

    override fun onDeleteClick(position: Int) {
        val product = productList[position]
        product.id?.let {
            val productRef = database.getReference("products/$it")
            productRef.removeValue().addOnSuccessListener {
                Toast.makeText(this, "Składnik został pomyślnie usunięty", Toast.LENGTH_SHORT).show()
            }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Błąd podczas usuwania składnika: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onEditClick(position: Int) {
        val product = productList[position]

        val intent = Intent(this, EditActivity::class.java)
        intent.putExtra("skladnik", product.id)
        startActivity(intent)
    }

}
