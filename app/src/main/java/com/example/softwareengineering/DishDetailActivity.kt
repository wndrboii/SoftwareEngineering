package com.example.softwareengineering

import ProductAdapterDishDetails
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.softwareengineering.model.Comment
import com.example.softwareengineering.model.DailyNutrition
import com.example.softwareengineering.model.Posilki
import com.example.softwareengineering.model.Skladnik
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Math.round
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class DishDetailActivity : AppCompatActivity(), ProductAdapterDishDetails.ProductAdapterDishDetailsListener {

    private lateinit var logout: ImageButton
    private lateinit var home: ImageButton
    private lateinit var categories: ImageButton
    private lateinit var goback: ImageButton

    private lateinit var nameField: TextView
    private lateinit var average: TextView
    private lateinit var categoryField: TextView
    private lateinit var quantityField: TextView
    private lateinit var dishImage: ImageView
    private lateinit var kcal: TextView
    private lateinit var proteins: TextView
    private lateinit var carbs: TextView
    private lateinit var fats: TextView

    private lateinit var rating_spn: Spinner
    private lateinit var clock: ImageView

    private lateinit var productAdapter: ProductAdapterDishDetails
    private lateinit var productList: MutableList<Skladnik>
    private lateinit var productRecyclerView: RecyclerView
    private lateinit var database2: FirebaseDatabase
    private lateinit var productRef: DatabaseReference

    private lateinit var commentsRecyclerView: RecyclerView

    private lateinit var edit_text: EditText

    private var dishName: String? = ""
    private var dishCategory: String? = ""
    private var dishQuantity: Int? = 1
    private lateinit var dishCalories: TextView
    private lateinit var dishProteins: TextView
    private lateinit var dishCarbs: TextView
    private lateinit var dishFats: TextView

    private lateinit var commentRecyclerView: RecyclerView
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentList: MutableList<Comment>
    private lateinit var currentUserId: String

    //Average dishes rating
    fun calculateAverageRating(posilek: Posilki): Float {
        val comments = posilek.comments ?: return 0f

        var sum = 0f
        comments.values.forEach {
            sum += it.ocena
        }

        return if (comments.isNotEmpty()) {
            val average = sum / comments.size
            val roundedAverage = (average * 10).roundToInt() / 10f
            roundedAverage
        } else {
            0f
        }
    }

    @SuppressLint("SetTextI18n")
    fun calculateAverageMacro(posilek: Posilki) {
        val products = posilek.products

        var sumCalories: Int = 0
        var sumProteins: Int = 0
        var sumCarbs: Int = 0
        var sumFats: Int = 0
        products.forEach {
            sumCalories += it.calories
            sumProteins += it.protein
            sumCarbs += it.carbs
            sumFats += it.fat
        }

        dishCalories.text = "kcal: $sumCalories"
        dishProteins.text = "p: $sumProteins"
        dishCarbs.text = "c: $sumCarbs"
        dishFats.text = "f: $sumFats"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dish_detail)

        nameField = findViewById(R.id.dish_name)
        dishImage = findViewById(R.id.dish_image)
        kcal = findViewById(R.id.dish_kcal)
        proteins = findViewById(R.id.dish_proteins)
        carbs = findViewById(R.id.dish_carbs)
        fats = findViewById(R.id.dish_fats)

        dishCalories = findViewById(R.id.dish_kcal)
        dishProteins = findViewById(R.id.dish_proteins)
        dishCarbs = findViewById(R.id.dish_carbs)
        dishFats = findViewById(R.id.dish_fats)

        // Initialize Firebase database
        val database = Firebase.database.reference
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        // Get dish ID from intent
        val dishId = intent.getStringExtra("posilek") ?: ""
        var averageRating : Float? = 0f

        val context: Context = this
        val clockImageView: ImageView = findViewById(R.id.clock_btn)

        // Set a click listener on the clock ImageView
        clockImageView.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
            val currentMinute = currentTime.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(context, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val timeToEat = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                val database = FirebaseDatabase.getInstance()
                val dailyNutritionRef = database.getReference("scheduled")

                // Generate a unique key for the new dailyNutrition entry
                val newDailyNutritionKey = dailyNutritionRef.push().key

                val dailyNutrition = DailyNutrition(
                    id = newDailyNutritionKey,
                    time = timeToEat,
                    userId = currentUserId,
                    posilekId = dishId
                )
                if (newDailyNutritionKey != null) {
                    dailyNutritionRef.child(newDailyNutritionKey).setValue(dailyNutrition)
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "Posiłek dodany do listy na dzień pomyślnie", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                applicationContext,
                                "Błąd podczas dodawania posiłka: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }, currentHour, currentMinute, false)

            timePickerDialog.show()
        }

        //Reading dish
        database.child("dishes").child(dishId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val dish = snapshot.getValue(Posilki::class.java)
                if (dish != null) {
                    // Check if the user has liked the dish
                    val isLiked = dish.isLikedByUser(currentUserId)

                    // Toggle the heart icons based on liking
                    val likeButton = findViewById<ImageView>(R.id.like_btn)
                    if (isLiked) {
                        likeButton.setImageResource(R.drawable.ic_red_heart)
                    } else {
                        likeButton.setImageResource(R.drawable.ic_empty_heart)
                    }

                    // Set an OnClickListener to toggle the like status when the heart icon is clicked
                    likeButton.setOnClickListener {
                        if (isLiked) {
                            // User already liked the dish, so remove their ID from the liked list
                            dish.liked.remove(currentUserId)
                        } else {
                            // User hasn't liked the dish, so add their ID to the liked list
                            dish.liked.add(currentUserId)
                        }

                        // Update the heart icon based on the new liking status
                        if (dish.isLikedByUser(currentUserId)) {
                            likeButton.setImageResource(R.drawable.ic_red_heart)
                        } else {
                            likeButton.setImageResource(R.drawable.ic_empty_heart)
                        }

                        // Update the dish's liking status in the database
                        val dishRef = database.child("dishes").child(dishId)
                        dishRef.setValue(dish)
                    }
                }
                nameField.text = dish?.name
                dishName = dish?.name
                dishCategory = dish?.category
                dishQuantity = dish?.quantity
                Glide.with(applicationContext)
                    .load(dish?.photoUrl)
                    .into(dishImage)
                dishImage.background = null;
                averageRating = dish?.let { calculateAverageRating(it) }
                average = findViewById(R.id.average_rate)
                average.text = averageRating.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "loadDish:onCancelled", error.toException())
            }
        })

        //Initialise

        val posilekRef = database.child("dishes").child(dishId)
        posilekRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val posilek = snapshot.getValue(Posilki::class.java)

                if (posilek != null) {
                    calculateAverageMacro(posilek)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "loadPosilek:onCancelled", error.toException())
            }
        })



        //List of "składniki"
        productRecyclerView = findViewById(R.id.productRecyclerView)
        productAdapter = ProductAdapterDishDetails(mutableListOf(), this)
        productRecyclerView.adapter = productAdapter

        database2 = FirebaseDatabase.getInstance()
        productRef = database2.getReference("products")

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
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }

        })

        //Rating
        val ratings = arrayOf(1, 2, 3, 4, 5)
        var selectedItem = 1
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ratings)
        rating_spn = findViewById(R.id.ratingSpinner)
        rating_spn.adapter = adapter

        rating_spn.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {
                selectedItem = 1
            }

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedItem = rating_spn.selectedItem.toString().toInt()
            }
        }

        //nameField.text = dishName
        goback = findViewById<ImageButton>(R.id.goback_btn)

        //Menu navigation
        goback.setOnClickListener(View.OnClickListener{
            if (intent.getStringExtra("sourceActivity") == "ListOfPosilkiActivity") {
                intent = Intent(applicationContext, ListOfPosilkiActivity::class.java)
            } else if(intent.getStringExtra("sourceActivity") == "DishCatDetails"){
                intent = Intent(applicationContext, DishCatDetailsActivity::class.java)
            } else if(intent.getStringExtra("sourceActivity") == "SetDetail"){
                intent = Intent(applicationContext, SetDetailActivity::class.java)
            }else {
                intent = Intent(applicationContext, FavouriteActivity::class.java)
            }
            startActivity(intent)
            finish()
        })

        //Comment
        val addButton = findViewById<ImageButton>(R.id.submit_btn)
        addButton.setOnClickListener {

            edit_text = findViewById<EditText>(R.id.name_edit_text)

            val text = edit_text.text.toString()

            val database = Firebase.database.reference
            val commentId = database.child("dishes").child(dishId).child("comments").push().key
            val comment = currentUserId?.let { it1 ->
                Comment(
                    id = commentId,
                    text = text,
                    ocena = selectedItem,
                    userId = it1,
                    posilekId = dishId
                )
            }

            if (comment != null) {
                if (comment.id != null) {
                    val commentsRef = database.child("dishes").child(dishId).child("comments")
                    val newCommentRef = commentsRef.push()
                    newCommentRef.setValue(comment).addOnSuccessListener {
                        Toast.makeText(this, "Nowy komentarz dodany pomyślnie", Toast.LENGTH_SHORT).show()
                        edit_text.text.clear()

                        //Update average rating value after adding a comment
                        val posilekRef = database.child("dishes").child(dishId)
                        posilekRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val posilek = snapshot.getValue(Posilki::class.java)

                                if (posilek != null) {
                                    val averageRating = calculateAverageRating(posilek)
                                    average.text = averageRating.toString()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.w(ContentValues.TAG, "loadPosilek:onCancelled", error.toException())
                            }
                        })
                    }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Błąd podczas dodawania komentarza: ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }

        }

        //Comments list
        val commentsRef = FirebaseDatabase.getInstance().reference.child("dishes").child(dishId).child("comments")
        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val commentList = ArrayList<Comment>()
                for (snapshot in dataSnapshot.children) {
                    val comment = snapshot.getValue(Comment::class.java)
                    comment?.let { commentList.add(it) }
                }
                val adapter = CommentAdapter(commentList)
                commentsRecyclerView = findViewById<RecyclerView>(R.id.comments_recycler_view)
                commentsRecyclerView.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //Exceptions...
            }
        })
    }

    override fun onDeleteClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onEditClick(position: Int) {
        TODO("Not yet implemented")
    }
}