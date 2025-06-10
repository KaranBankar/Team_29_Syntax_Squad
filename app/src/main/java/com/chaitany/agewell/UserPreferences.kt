
package com.example.yourapp.utils

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("UserLogin", Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        const val MOBILE_KEY = "mobile"
    }

    // Save mobile number to SharedPreferences
    fun saveMobileNumber(mobile: String) {
        editor.putString(MOBILE_KEY, mobile)
        editor.apply()
    }

    // Get mobile number from SharedPreferences
    fun getMobileNumber(): String? {
        return sharedPreferences.getString(MOBILE_KEY, null)
    }

    // Clear mobile number from SharedPreferences
    fun clearMobileNumber() {
        editor.remove(MOBILE_KEY)
        editor.apply()
    }
}
