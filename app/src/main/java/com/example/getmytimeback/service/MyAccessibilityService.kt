package com.example.getmytimeback.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.getmytimeback.MainActivity
import com.example.getmytimeback.data.BlockedSites
import com.example.getmytimeback.data.SupportedBrowsers
import com.example.getmytimeback.model.SupportedBrowserConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyAccessibilityService : AccessibilityService() {

    private val visitedSites = mutableMapOf<String, Long>()
    private var trackingJob: Job? = null // Stores the coroutine job

    val blockedSites = BlockedSites.blockedSites
    val supportedBrowserConfigs = SupportedBrowsers.supportedBrowserConfigs

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventType = event.eventType

        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // check this
                val parentNodeInfo = event.source ?: return

                val packageName = event.packageName.toString()

                val browserConfig: SupportedBrowserConfig? = supportedBrowserConfigs[packageName]
                if (browserConfig == null) { // validate if change browser, something like lastBrowser != ..
                    stopTracking() // User switched to another app
                    return
                }

                val browserUrl: String = captureUrl(parentNodeInfo, browserConfig) ?: return

                val blockedSiteKey = blockedSites.keys.firstOrNull { browserUrl.contains(it) }

                if (blockedSiteKey != null) {
                    startTrackingWebsite(blockedSiteKey)
                }else {
                    stopTracking(); // no estoy seguro pq si borra la url pero sigue en el sitio?
                }
            }
        }
    }

    private fun startTrackingWebsite(blockedSiteKey: String) {
        println("START TRACKING")

        val startTime = System.currentTimeMillis()
        visitedSites[blockedSiteKey] = startTime

        trackingJob?.cancel() // Cancel any previous tracking job
        trackingJob = CoroutineScope(Dispatchers.Main).launch {

                while (true) { //arreglar todo esto
                    delay(1000) // Wait for 1 second
                    var elapsedTime =
                        (System.currentTimeMillis() - visitedSites[blockedSiteKey]!!) / 1000
                    blockedSites[blockedSiteKey]!!.consumedTime += elapsedTime
                    println("TIME IN THE SITE:" + blockedSites[blockedSiteKey]!!.consumedTime)
                    if (blockedSites[blockedSiteKey]!!.consumedTime >= blockedSites[blockedSiteKey]!!.allowedTime) {
                        drawOnTop()
                        trackingJob?.cancel() // Stop tracking once the time limit is reached
                        break
                    }
                }
        }
    }

    private fun stopTracking() {
        trackingJob?.cancel() // Stop the coroutine
        trackingJob = null
        visitedSites.clear() // Clear tracking data
    }

    private fun captureUrl(info: AccessibilityNodeInfo, config: SupportedBrowserConfig): String? {
        val nodes = info.findAccessibilityNodeInfosByViewId(config.addressBarId)
        if (nodes == null || nodes.isEmpty()) {
            return null
        }

        val addressBarNodeInfo = nodes[0]
        var url: String? = null
        if (addressBarNodeInfo.text != null) {
            url = addressBarNodeInfo.text.toString()
        }

        addressBarNodeInfo.refresh()
        return url
    }

    private fun drawOnTop() {
        val lockIntent = Intent(
            this,
            MainActivity::class.java
        )
        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        lockIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        lockIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(lockIntent)
    }

    override fun onInterrupt() {
        // Handle interruptions
    }

}
