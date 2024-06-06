package com.example.softwareengineering

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
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import model.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class DishDetailActivity : AppCompatActivity(), ProductAdapterDishDetails.ProductAdapterDishDetailsListener {

    private lateinit var goback: ImageButton

    private lateinit var nameField: TextView
    private lateinit var average: TextView
    private lateinit var categoryField: TextView


    private lateinit var dishImage: ImageView

    private lateinit var rating_spn: Spinner

    private lateinit var productAdapter: ProductAdapterDishDetails
    private lateinit var productList: MutableList<Skladnik>
    private lateinit var productRecyclerView: RecyclerView
    private lateinit var database2: FirebaseDatabase
    private lateinit var productRef: DatabaseReference

    private lateinit var commentsRecyclerView: RecyclerView

    private lateinit var edit_text: EditText

    private var dishName: String? = ""
    private var dishCategory: String? = ""


    private var userPhotoUrl: String? = ""


    private lateinit var dishCalories: TextView
    private lateinit var dishProteins: TextView
    private lateinit var dishCarbs: TextView
    private lateinit var dishFats: TextView

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
    fun calculateAverageMacro(posilek: Posilki, callback: (amount: Int) -> Unit) {
        val posilkiId = posilek.id
        val databaseReference = FirebaseDatabase.getInstance().reference.child("composition")

        var sumCalories: Double = 0.0
        var sumProteins: Double = 0.0
        var sumCarbs: Double = 0.0
        var sumFats: Double = 0.0
        var amount: Int = 0
        val skladnikIds: MutableList<String> = mutableListOf()

        databaseReference.orderByChild("posilkiId").equalTo(posilkiId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (skladPosilkiSnapshot in snapshot.children) {
                        val skladPosilki = skladPosilkiSnapshot.getValue(SkladPosilku::class.java)
                        skladPosilki?.let {
                            val skladnikId = it.skladnikId
                            val amountInGrams = it.amount

                            skladnikIds.add(skladnikId)
                            amount += amountInGrams

                            val skladnikReference =
                                FirebaseDatabase.getInstance().reference.child("products")
                                    .child(skladnikId)
                            skladnikReference.addListenerForSingleValueEvent(object :
                                ValueEventListener {
                                override fun onDataChange(skladnikSnapshot: DataSnapshot) {
                                    val skladnik = skladnikSnapshot.getValue(Skladnik::class.java)
                                    skladnik?.let { skladnik ->
                                        val skladnikCaloriesPer100g = skladnik.calories
                                        val skladnikProteinsPer100g = skladnik.protein
                                        val skladnikCarbsPer100g = skladnik.carbs
                                        val skladnikFatsPer100g = skladnik.fat

                                        val skladnikCalories: Double =
                                            (skladnikCaloriesPer100g * amountInGrams) / 100
                                        val skladnikProteins: Double =
                                            (skladnikProteinsPer100g * amountInGrams) / 100
                                        val skladnikCarbs: Double =
                                            (skladnikCarbsPer100g * amountInGrams) / 100
                                        val skladnikFats: Double =
                                            (skladnikFatsPer100g * amountInGrams) / 100

                                        sumCalories += skladnikCalories
                                        sumProteins += skladnikProteins
                                        sumCarbs += skladnikCarbs
                                        sumFats += skladnikFats
                                    }

                                    val weightRatio = 100.0 / amount
                                    val caloriesPer100g: Double = sumCalories * weightRatio
                                    val proteinsPer100g: Double = sumProteins * weightRatio
                                    val carbsPer100g: Double = sumCarbs * weightRatio
                                    val fatsPer100g: Double = sumFats * weightRatio

                                    val roundedCalories: String =
                                        String.format("%.1f", caloriesPer100g)
                                    val roundedProteins: String =
                                        String.format("%.1f", proteinsPer100g)
                                    val roundedCarbs: String = String.format("%.1f", carbsPer100g)
                                    val roundedFats: String = String.format("%.1f", fatsPer100g)

                                    dishCalories.text = "kcal: $roundedCalories"
                                    dishProteins.text = "b: $roundedProteins"
                                    dishCarbs.text = "w: $roundedCarbs"
                                    dishFats.text = "tł: $roundedFats | 100g."

                                    callback(amount)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Handle error
                                }
                            })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    fun fetchCategory(categoryId: String, callback: (ProductCategory) -> Unit) {
        val categoryRef = FirebaseDatabase.getInstance().getReference("categories").child(categoryId)
        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val category = dataSnapshot.getValue(ProductCategory::class.java)
                if (category != null) {
                    callback(category)
                } else {
                    Log.e("Firebase", "Category not found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Error getting category", error.toException())
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dish_detail)


        nameField = findViewById(R.id.dish_name)
        dishImage = findViewById(R.id.dish_image)

        dishCalories = findViewById(R.id.dish_kcal)
        dishProteins = findViewById(R.id.dish_proteins)
        dishCarbs = findViewById(R.id.dish_carbs)
        dishFats = findViewById(R.id.dish_fats)
        categoryField = findViewById(R.id.category)
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

            val timePickerDialog = TimePickerDialog(context,R.style.MyTimePickerDialogTheme, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                val timeToEat = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)

                val daysOfWeek = arrayOf("Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela")

                val builder = AlertDialog.Builder(context, R.style.AlertDialogCustom)
                builder.setTitle("Wybierz dzień tygodnia")
                builder.setItems(daysOfWeek) { _, selectedDayIndex ->
                    val selectedDayOfWeek = selectedDayIndex + 1 // Adding 1 to make it 1-based index

                    val database = FirebaseDatabase.getInstance()
                    val dailyNutritionRef = database.getReference("scheduled")

                    // Generate a unique key for the new dailyNutrition entry
                    val newDailyNutritionKey = dailyNutritionRef.push().key

                    val dailyNutrition = DailyNutrition(
                        id = newDailyNutritionKey,
                        time = timeToEat,
                        day = selectedDayOfWeek,
                        userId = currentUserId,
                        posilekId = dishId
                    )

                    // Save the dailyNutrition object to the database
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
                }

                builder.create().show()
            }, currentHour, currentMinute, false)

            timePickerDialog.show()
        }

        //Reading dish
        database.child("dishes").child(dishId).addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                val dish = snapshot.getValue(Posilki::class.java)
                if (dish != null) {
                    // Check if the user has liked the dish

                    val likeButton = findViewById<ImageView>(R.id.like_btn)

                    likeButton.setOnClickListener {
                        var isLiked = dish.isLikedByUser(currentUserId)
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

                    database.child("dishes").child(dishId).child("liked").addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            var isLiked = dish.isLikedByUser(currentUserId)

                            // Toggle the heart icons based on liking
                            val likeButton = findViewById<ImageView>(R.id.like_btn)
                            if (isLiked) {
                                likeButton.setImageResource(R.drawable.ic_red_heart)
                            } else {
                                likeButton.setImageResource(R.drawable.ic_empty_heart)
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Обработка ошибки, если не удалось получить значение isLiked
                        }
                    })

                    // Set an OnClickListener to toggle the like status when the heart icon is clicked

                    calculateAverageMacro(dish) { amount ->
                        nameField.text = "${dish?.name}(${amount}g)"
                    }
                    dishName = dish?.name
                    dishCategory = dish?.category
                    Glide.with(applicationContext)
                        .load(dish?.photoUrl)
                        .into(dishImage)
                    dishImage.background = null;
                    averageRating = dish?.let { calculateAverageRating(it) }
                    average = findViewById(R.id.average_rate)
                    average.text = averageRating.toString()

                    fetchCategory(dish.category) { category ->
                        categoryField.text = category.name // assuming the Category object has a name property
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "loadDish:onCancelled", error.toException())
            }
        })




        productRecyclerView = findViewById(R.id.productRecyclerView)
        productAdapter = ProductAdapterDishDetails(mutableListOf(), mutableListOf(),this)
        productRecyclerView.adapter = productAdapter

        val compositionsRef = FirebaseDatabase.getInstance().getReference("composition")
        val query = compositionsRef.orderByChild("posilkiId").equalTo(dishId)

        productList = mutableListOf()

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks = mutableListOf<Task<*>>()
                val productAmountMap = mutableMapOf<Skladnik, Int>()

                for (skladPosilkiSnapshot in snapshot.children) {
                    val skladPosilki = skladPosilkiSnapshot.getValue(SkladPosilku::class.java)
                    skladPosilki?.let {
                        val skladnikId = it.skladnikId
                        val amountInGrams = it.amount

                        val skladnikRef = FirebaseDatabase.getInstance().getReference("products").child(skladnikId)
                        val task = skladnikRef.get().addOnSuccessListener { skladnikSnapshot ->
                            val skladnik = skladnikSnapshot.getValue(Skladnik::class.java)
                            skladnik?.let { productAmountMap[it] = amountInGrams }
                        }
                        tasks.add(task)
                    }
                }

                Tasks.whenAllComplete(tasks).addOnCompleteListener {
                    productList.clear()
                    productList.addAll(productAmountMap.keys)
                    val skladnikAmounts = productAmountMap.values.toMutableList()
                    productAdapter.updateData(productList, skladnikAmounts)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })


        //Rating
        val ratings = arrayOf(1, 2, 3, 4, 5)
        var selectedItem = 1
        val adapter = ArrayAdapter(this@DishDetailActivity, R.layout.spinner_item_layout, ratings)
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
            } else if(intent.getStringExtra("sourceActivity") == "DaylistActivity"){
                intent = Intent(applicationContext, DaylistActivity::class.java)
            }else if(intent.getStringExtra("sourceActivity") == "SetDetail"){
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

            if (text.isNotEmpty()) {
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
                } else {
                    Toast.makeText(this, "Należy wpisać tekst", Toast.LENGTH_SHORT).show()
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