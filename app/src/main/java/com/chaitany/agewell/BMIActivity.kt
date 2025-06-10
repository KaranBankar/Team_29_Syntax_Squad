package com.chaitany.agewell

import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chaitany.agewell.databinding.ActivityBmiactivityBinding
import kotlin.math.pow

class BMIActivity : AppCompatActivity() {


    private lateinit var binding: ActivityBmiactivityBinding
    private lateinit var videoAdapter: VideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBmiactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.statusBarColor = getColor(android.R.color.white) // Set status bar color to white
        actionBar?.hide()


        binding.calculateButton.setOnClickListener {
            calculateBMI()
        }


        // Setup RecyclerView for videos
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        videoAdapter = VideoAdapter(emptyList()) // Initially empty
        binding.recyclerView.adapter = videoAdapter
    }

    private fun calculateBMI() {
        val heightStr = binding.heightInput.text.toString()
        val weightStr = binding.weightInput.text.toString()
        val ageStr = binding.ageInput.text.toString()

        if (heightStr.isEmpty() || weightStr.isEmpty() || ageStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val height = heightStr.toFloat() / 100 // Convert cm to m
        val weight = weightStr.toFloat()
        val age = ageStr.toInt()

        val bmi = weight / (height.pow(2))
        displayResult(bmi, age)
    }

    private fun displayResult(bmi: Float, age: Int) {
        binding.resultCard.visibility = View.VISIBLE

        val bmiCategory = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal weight"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }

        binding.bmiValueText.text = String.format("%.1f", bmi)
        binding.bmiCategoryText.text = bmiCategory

        val suggestion = when {
            bmi < 18.5 -> "Consider increasing your calorie intake and strength training."
            bmi < 25 -> "Maintain your current lifestyle with a balanced diet and regular exercise."
            bmi < 30 -> "Focus on portion control and increase physical activity."
            else -> "Consult with a healthcare professional for a personalized weight loss plan."
        }

        binding.suggestionText.text = suggestion

        // Adjust color based on BMI category
        val color = when {
            bmi < 18.5 -> getColor(R.color.underweight)
            bmi < 25 -> getColor(R.color.normal)
            bmi < 30 -> getColor(R.color.overweight)
            else -> getColor(R.color.obese)
        }
        binding.resultCard.setCardBackgroundColor(color)

        // Load and display videos for the category
        showVideos(bmiCategory)
    }

    private fun showVideos(category: String) {
        val videos = getYouTubeVideos(category)
        videoAdapter.updateVideos(videos)
        binding.recyclerView.visibility = View.VISIBLE
        binding.
        scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
    }

    private fun getYouTubeVideos(category: String): List<VideoItem> {
        return when (category) {
            "Underweight" -> listOf(
                VideoItem("Healthy Weight Gain Tips", "https://youtu.be/1Q6ij1qSrrw?si=H8a1UNpjthvk3zIp"),
                VideoItem("High-Calorie Foods for Weight Gain", "https://youtu.be/G5r-If3uJG0?si=_goH9X8Q8CKb8K5Q"),
                VideoItem("Best Protein Sources for Weight Gain", "https://youtu.be/Pcd28t9x8w8?si=vCS3Clxu4S0R0VcP"),
                VideoItem("Strength Training for Mass", "https://youtu.be/OPEDjl88P-4?si=v395uf7fqvUjYfnq"),
                VideoItem("How to Gain Weight Fast Naturally", "https://youtu.be/1dKTtXWASVk?si=PiI_0VKjrzUVGD5O")
            )
            "Normal weight" -> listOf(
                VideoItem("Maintaining a Healthy Lifestyle", "https://youtu.be/-_VhU5rqyko?si=XSsveL8B8KInkfM4"),
                VideoItem("Balanced Diet Explained", "https://youtu.be/81G22t2UHxA?si=WRSIzLoIxtOyZ7Gt"),
                VideoItem("Best Workouts for Fitness", "https://youtu.be/KeNObkhENKQ?si=8lQAwNIRiJwdA3Zh"),
                VideoItem("Daily Nutrition Guide", "https://youtu.be/-8QYDGpsG-M?si=7wwQgGi9tm1tFa9P"),
                VideoItem("How to Stay Fit Without Gym", "https://youtu.be/-8QYDGpsG-M?si=7wwQgGi9tm1tFa9P")
            )
            "Overweight" -> listOf(
                VideoItem("Effective Fat Loss Tips", "https://youtu.be/7Jm6yb1EaCw?si=du2WX-716ko-FIFy"),
                VideoItem("Low-Calorie Diet Plan", "https://youtu.be/1_fSurvDrFQ?si=aioP6MQkJiCyg24L"),
                VideoItem("Beginner Cardio Workouts", "https://youtu.be/_JUJ9647NbI?si=hTIp8O18Nx-5Idk7"),
                VideoItem("How to Reduce Belly Fat", "https://youtu.be/Ok-AZtt33Bo?si=pbvY5AFJ8kh0_8GM"),
                VideoItem("Intermittent Fasting for Fat Loss", "https://youtu.be/vr3CQEjSPdc?si=Vy_ZThh1Q0pWb1uw")
            )
            else -> listOf(
                VideoItem("Obesity Management Strategies", "https://youtu.be/2RaQ0b4F3P0?si=8L6mqTwcRW2RWVLi"),
                VideoItem("Healthy Meal Prep for Weight Loss", "https://youtu.be/F6ehz4iK3F8?si=kqu_r3r07rm6FbwF"),
                VideoItem("Best Exercises for Obese Individuals", "https://youtu.be/1UBuwKo3jvY?si=uj9egu4JrpwV23B3"),
                VideoItem("How to Start Losing Weight Safely", "https://youtu.be/z9XJue9aUqk?si=VkARyCh7lW7bkbJI"),
                VideoItem("How to Fix Your Metabolism", "https://youtu.be/ezflL54SdsA?si=-izUz_LbYMuEtU2D")
            )
        }

    }
}
