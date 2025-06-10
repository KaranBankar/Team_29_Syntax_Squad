package com.chaitany.agewell;


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.chaitany.agewell.Dashboard
import com.chaitany.agewell.R
import com.chaitany.agewell.databinding.ActivityAiBinding


class AiActivity : AppCompatActivity() {

    lateinit var binding: ActivityAiBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupKeyboardListener()
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                if (url.endsWith(".pdf")) {
                    // Open PDF in Google Docs or external PDF viewer
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(url), "application/pdf")
                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        // If no PDF viewer app is available, fall back to Google Docs
                        view.loadUrl("https://docs.google.com/viewer?url=$url")
                    }
                    return true
                }
                return false
            }
        }

        binding.webView.loadUrl("https://www.meta.ai/")


    }


    fun setupKeyboardListener() {
        val rootView = findViewById<View>(android.R.id.content)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            // Check if the keyboard is open
            val isKeyboardOpen = keypadHeight > screenHeight * 0.15
            handleKeyboardVisibility(isKeyboardOpen)
        }
    }

    private fun handleKeyboardVisibility(isKeyboardOpen: Boolean) {
        val webView = findViewById<WebView>(R.id.webView)

        if (isKeyboardOpen) {
            // Get screen height and width
            val metrics = resources.displayMetrics
            val screenHeight = metrics.heightPixels
            val screenWidth = metrics.widthPixels

            val newHeight = when {
                screenHeight <= 1280 && screenWidth <= 720 -> { // Small screens (e.g., 4" devices)
                    600 // Adjust height for small screens
                }
                screenHeight <= 1920 && screenWidth <= 1080 -> { // Normal screens (e.g., 5"-6" devices)
                    800 // Adjust height for normal screens
                }
                screenHeight <= 2560 && screenWidth <= 1440 -> { // Large screens (e.g., 6"-7" devices)
                    1200 // Adjust height for large screens
                }
                else -> { // Extra-large screens (e.g., tablets or foldables)
                    1400 // Adjust height for extra-large screens
                }
            }

            webView.layoutParams.height = newHeight
        } else {
            // Restore to MATCH_PARENT
            webView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        webView.requestLayout() // Apply the changes
    }






    override fun onBackPressed() {
        super.onBackPressed()
        var i= Intent(this,Dashboard::class.java)
        startActivity(i)
        finish()
    }
}



