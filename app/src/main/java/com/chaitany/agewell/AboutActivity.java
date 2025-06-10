package com.chaitany.agewell;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class AboutActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    Button btn;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        btn=findViewById(R.id.ourfeedbacks);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set onClickListeners for social media icons
        setSocialMediaLinks();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AboutActivity.this, OurFeedbacks.class));
            }
        });
    }

    private void setSocialMediaLinks() {
        // Developer 1
        findViewById(R.id.instagram1).setOnClickListener(v -> openUrl("https://www.instagram.com/vishal_b__21/"));
        findViewById(R.id.github1).setOnClickListener(v -> openUrl("https://github.com/vishal-bhutekar21"));
        findViewById(R.id.linkedin1).setOnClickListener(v -> openUrl("https://www.linkedin.com/in/vishal-bhutekar-17552b283/"));

        // Developer 2
        findViewById(R.id.instagram2).setOnClickListener(v -> openUrl("https://www.instagram.com/chaitanyk_07/"));
        findViewById(R.id.github2).setOnClickListener(v -> openUrl("https://github.com/chaitanykakde"));
        findViewById(R.id.linkedin2).setOnClickListener(v -> openUrl("https://www.linkedin.com/in/chaitany-kakde-2a3ba62a8/"));

        // Developer 3
        findViewById(R.id.instagram3).setOnClickListener(v -> openUrl("https://www.instagram.com/satwik_mahajan_1"));
        findViewById(R.id.github3).setOnClickListener(v -> openUrl("https://github.com/SatwikMahajan01"));
        findViewById(R.id.linkedin3).setOnClickListener(v -> openUrl("https://www.linkedin.com/in/satwik-mahajan-173766218/ "));
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Intent intent;

        if (item.getItemId() == R.id.nav_logout) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLogged", false);
            editor.apply();
            intent = new Intent(this, Login.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.nav_profile) {
            intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_share) {
            shareApp();
        } else if (item.getItemId() == R.id.nav_feedback) {
            intent = new Intent(this, FeedbackActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_about) {
            intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        } else {
            return false;
        }






        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void shareApp() {
        String appLink = "https://play.google.com/store/apps/details?id=com.chaitany.agewell";
        String shareMessage = "Check out this amazing app that helps you and your older person to stay healthy and care! \n\n" +
                "Download it here: " + appLink + "\n\n" +
                "Stay healthy and fit!";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share Our App");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }
}