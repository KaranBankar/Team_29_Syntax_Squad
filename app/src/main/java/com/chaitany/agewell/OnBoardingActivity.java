package com.chaitany.agewell;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.widget.Button;

public class OnBoardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private OnboardingAdapter onboardingAdapter;
    private Button btnNext, btnSkip;
    private int currentPage = 0;
    private SharedPreferences sharedPreferences;

    private final int[] images = {
            R.drawable.img_onboard2,
            R.drawable.img_onboard1,
            R.drawable.onboarding4,
            R.drawable.img_onboard3,
            R.drawable.img_onboard5,
            R.drawable.img_onboard6
    };

    private final String[] titles = {
            "A Smarter Way to Care for Those Who Matter Most!",
           "Help at Your Fingertips!",
            "Find the Nearest Hospital Instantly!",
            "Never Miss a Dose or Checkup!",
            "Stay Active & Manage Your Day with Ease!",
            "Stay Connected with Loved Ones!"
    };

    private final String[] descriptions = {
            "Smart technology meets heartfelt care – ensuring safety, health, and well-being for seniors, effortlessly.",
            "One-tap SOS alert to caregivers & real-time emergency support.",
            "Locate nearby hospitals and get turn-by-turn navigation in emergencies.",
            "Smart medication reminders, BMI tracking, and doctor visit alerts.",
            "Personalized exercise routines, meal plans, and daily task management.",
            "Community chat, and social groups to combat loneliness."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get SharedPreferences
        sharedPreferences = getSharedPreferences("UserLogin", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLogged", false);

        // If user is logged in, skip onboarding and go to the main screen (Dashboard)
        if (isLoggedIn) {
            startActivity(new Intent(this, Dashboard.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_on_boarding);

        // Change Status Bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.teal_50)); // Change to your desired color
        }

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);

        // Initialize adapter
        onboardingAdapter = new OnboardingAdapter(this, images, titles, descriptions);
        viewPager.setAdapter(onboardingAdapter);

        btnNext.setOnClickListener(v -> {
            if (currentPage < images.length - 1) {
                currentPage++;
                viewPager.setCurrentItem(currentPage);
            } else {
                finishOnboarding();
            }
        });

        btnSkip.setOnClickListener(v -> finishOnboarding());

    }

    // Change navigation bar color
    private void finishOnboarding() {
        startActivity(new Intent(this, GetStartedActivity.class));
        finish();
    }
}
