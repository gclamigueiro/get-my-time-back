package com.example.getmytimeback

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.getmytimeback.data.BlockedSites
import com.example.getmytimeback.model.BlockedSite
import com.example.getmytimeback.service.MyAccessibilityService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        displayBlockedSites()

        // Check if Accessibility Service is enabled
        checkAccessibilityService()
    }

    fun displayBlockedSites(){
        val textView = findViewById<TextView>(R.id.tv1)

        // Convert map to a readable format
        val sitesText = BlockedSites.blockedSites.entries.joinToString("\n") {
            "${it.key} - Allowed Time: ${it.value.allowedTime} min - Consumed: ${it.value.consumedTime}"
        }

        // Display the text in TextView
        textView.text = sitesText
    }

    /**
     * Check if MyAccessibilityService is enabled and guide the user to enable it if not.
     */
    private fun checkAccessibilityService() {
        if (!isAccessibilityServiceEnabled(MyAccessibilityService::class.java)) {
            // Open Accessibility Settings to enable the service
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Please enable MyAccessibilityService in the settings", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Accessibility Service is already enabled", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Check if the Accessibility Service is enabled.
     */
    private fun isAccessibilityServiceEnabled(serviceClass: Class<out MyAccessibilityService>): Boolean {
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        if (!TextUtils.isEmpty(enabledServices)) {
            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals("${packageName}/${serviceClass.name}", ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }
}
