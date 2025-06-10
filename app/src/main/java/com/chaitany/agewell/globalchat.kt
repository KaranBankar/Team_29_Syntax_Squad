package com.chaitany.agewell

import ChatAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaitany.agewell.com.chaitany.agewell.DataStorer
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class globalchat : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val chatList = mutableListOf<ChatMessage>()
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private var userMobile = "9322067937"  // Get dynamically from user login

    // List of health-related words
    private val healthKeywords = listOf(
        // Common daily-use words
        "hi", "hello", "hey", "good morning", "good afternoon", "good evening", "good night",
        "how are you", "I am fine", "thank you", "welcome", "bye", "take care", "see you",
        "yes", "no", "okay", "ok", "sure", "please", "help", "support", "care", "friend",
        "health", "doctor", "medicine", "fitness", "exercise", "diet", "nutrition",
        "mental health", "stress", "yoga", "therapy", "hospital", "wellness",
        "hydration", "vitamin", "immune", "symptoms", "treatment", "blood pressure",
        "diabetes", "heart", "protein", "calories", "cholesterol", "sleep", "anxiety",
        "meditation", "relaxation", "hygiene", "sanitation", "vaccination", "virus",
        "infection", "flu", "cold", "fever", "cough", "sneeze", "fatigue", "allergy",
        "injury", "fracture", "pain", "headache", "migraine", "depression", "happiness",
        "smile", "energy", "metabolism", "immune system", "blood test", "checkup",
        "consultation", "ambulance", "first aid", "bandage", "ointment", "emergency",
        "ICU", "surgery", "operation", "recovery", "physiotherapy", "rehabilitation",
        "therapy", "medication", "antibiotic", "painkiller", "prescription", "pharmacy",
        "dose", "injection", "blood sugar", "insulin", "thyroid", "cancer", "tumor",
        "cardio", "workout", "gym", "stretching", "running", "walking", "cycling",
        "swimming", "sports", "marathon", "hydration", "water intake", "dehydration",
        "electrolytes", "balanced diet", "fiber", "iron", "calcium", "zinc", "potassium",
        "probiotics", "antioxidants", "herbs", "organic", "gluten-free", "vegan",
        "vegetarian", "weight loss", "weight gain", "obesity", "BMI", "body fat",
        "cardiovascular", "cholesterol level", "blood circulation", "pulse rate",
        "heart rate", "oxygen level", "breathing", "respiration", "lungs", "bronchitis",
        "asthma", "COPD", "smoking", "alcohol", "drug abuse", "detox", "liver", "kidney",
        "digestive system", "stomach", "gut health", "probiotic", "constipation",
        "diarrhea", "gastric", "acidity", "indigestion", "bloating", "nausea", "vomiting",
        "skin care", "hair care", "eye care", "oral hygiene", "dental checkup", "dentist",
        "cavity", "tooth decay", "gums", "brushing", "flossing", "vision", "glasses",
        "contact lenses", "hearing", "ear infection", "nosebleed", "sinus", "allergies",
        "seasonal flu", "vaccine", "covid", "pandemic", "quarantine", "isolation",
        "mental illness", "psychologist", "psychiatrist", "counseling", "therapy session",
        "emotional health", "self-care", "self-love", "mindfulness", "positivity",
        "motivation", "determination", "discipline", "healthy habits", "routine",
        "lifestyle", "balance", "happiness", "gratitude", "peace", "medication adherence"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_globalchat)
        userMobile = getUserMobile()

        DataStorer.context = this

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().getReference("chats")

        val backButton: ImageView = findViewById(R.id.ivMenu)
        backButton.setOnClickListener {
            onBackPressed()  // Navigate back when clicked
        }

        // Initialize UI
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatAdapter(chatList)
        chatRecyclerView.adapter = chatAdapter

        // Load messages from Firebase
        loadMessages()

        // Send message on button click
        sendButton.setOnClickListener {
            val message = messageEditText.text.toString().trim()

            if (message.isNotEmpty() && isHealthRelated(message)) {
                val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                val currentDate = sdfDate.format(Date())
                val currentTime = sdfTime.format(Date())

                // Retrieve user details from SharedPreferences
                val sharedPref = getSharedPreferences("UserLogin", MODE_PRIVATE)
                val name = sharedPref.getString("name", "User Name")

                val chatMessage = name?.let { ChatMessage(it, message, currentTime, currentDate) }
                database.push().setValue(chatMessage)

                messageEditText.text.clear()
            } else {
                showWarningDialog()
            }
        }
    }

    private fun loadMessages() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (data in snapshot.children) {
                    val chat = data.getValue(ChatMessage::class.java)
                    if (chat != null) chatList.add(chat)
                }
                chatAdapter.notifyDataSetChanged()
                chatRecyclerView.scrollToPosition(chatList.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getUserMobile(): String {
        val sharedPreferences = getSharedPreferences("UserLogin", MODE_PRIVATE)
        return sharedPreferences.getString("mobile", "") ?: ""  // Default to empty string if not found
    }

    // Function to check if the message contains health-related words
    private fun isHealthRelated(message: String): Boolean {
        val words = message.lowercase(Locale.getDefault()).split("\\s+".toRegex())
        return words.any { it in healthKeywords }
    }

    // Function to show a warning dialog
    private fun showWarningDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Warning")
            .setMessage("Your message is not related to health. Please keep the discussion relevant.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
