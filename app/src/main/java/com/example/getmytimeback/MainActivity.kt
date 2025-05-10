package com.example.getmytimeback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.getmytimeback.data.BlockedSites
import com.example.getmytimeback.service.MyAccessibilityService
import com.example.getmytimeback.view.adapter.BlockedSiteAdapter
import com.example.getmytimeback.worker.ResetTimeWorker
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleDailyReset(this)
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

    override fun onResume() {
        super.onResume()
        displayBlockedSites()
    }

    private fun displayBlockedSites(){
        val recyclerView = findViewById<RecyclerView>(R.id.blockedSitesList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = BlockedSiteAdapter(BlockedSites.blockedSites.values.toList())
    }

    private fun scheduleDailyReset(context: Context) {
        val now = LocalDateTime.now()
        val targetTime = now.withHour(3).withMinute(0).withSecond(0).withNano(0)
        val initialDelay = Duration.between(now, targetTime).let {
            if (it.isNegative) it.plusDays(1) else it
        }

        val dailyWorkRequest = PeriodicWorkRequestBuilder<ResetTimeWorker>(
            1, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay.toMinutes(), TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "resetTimeWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
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
