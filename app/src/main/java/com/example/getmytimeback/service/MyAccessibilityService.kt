package com.example.getmytimeback.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.getmytimeback.MainActivity
import com.example.getmytimeback.data.BlockedSites
import com.example.getmytimeback.data.SupportedBrowsers
import com.example.getmytimeback.model.BlockedSite
import com.example.getmytimeback.model.SupportedBrowserConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyAccessibilityService : AccessibilityService() {

    private val supportedBrowserConfigs = SupportedBrowsers.supportedBrowserConfigs

    private val visitedSites = mutableMapOf<String, Long>()
    private var trackingJob: Job? = null
    private var currentSite: BlockedSite? = null

    private var inactivityJob: Job? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventType = event.eventType
        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED,
            AccessibilityEvent.TYPE_VIEW_CLICKED,
            AccessibilityEvent.TYPE_VIEW_TARGETED_BY_SCROLL,
                 -> {

                val parentNodeInfo = event.source ?: return

                resetInactivityTimer()

                val packageName = event.packageName.toString()
                val browserConfig: SupportedBrowserConfig? = supportedBrowserConfigs[packageName]
                if (browserConfig == null) {
                    stopTracking(2) // User switched to another app
                    return
                }

                val browserUrl: String = captureUrl(parentNodeInfo, browserConfig) ?: return

                val blockedSiteKey = BlockedSites.blockedSites.keys.firstOrNull { browserUrl.contains(it) }

                if (blockedSiteKey != null) {
                    startTrackingWebsite(blockedSiteKey)
                }else {
                    stopTracking(3) // User switched to another not blocked site
                }
            }
        }
    }

    private fun startTrackingWebsite(blockedSiteKey: String) {

        // I am in the same site, all good
        if (currentSite != null && currentSite?.domain == blockedSiteKey ) {
            return
        }

        currentSite = BlockedSites.blockedSites[blockedSiteKey]
        println("START TRACKING: " + currentSite?.domain)

        trackingJob?.cancel() // Cancel any previous tracking job
        trackingJob = CoroutineScope(Dispatchers.Main).launch {

                // if all the time for the site is already consumed
               // give 2 seconds to close the tab and stop tracking
                if (checkConsumedTime(blockedSiteKey)) {
                    delay(2000)
                    drawOnTop()
                    stopTracking(4)
                }

            while (true) {
                delay(2000) // Add consumed time every 2 seconds

                val currentTime = System.currentTimeMillis()

                // Check if we have a valid starting point
                val lastVisitTime = visitedSites[blockedSiteKey]
                if (lastVisitTime == null) {
                    visitedSites[blockedSiteKey] = currentTime // Initialize if first visit
                    continue
                }

                // Calculate elapsed time in seconds
                val elapsedTime = (currentTime - lastVisitTime) / 1000 // in seconds
                BlockedSites.blockedSites[blockedSiteKey]!!.consumedTime += elapsedTime

                // Update the last visit time to the current time
                visitedSites[blockedSiteKey] = currentTime

                // Check if the consumed time exceeds the allowed time
                if (checkConsumedTime(blockedSiteKey)) {
                    stopTracking(5) // Stop tracking once the time limit is reached
                    drawOnTop()
                    break
                }
            }

        }
    }

    private fun checkConsumedTime(blockedSiteKey: String) =
        BlockedSites.blockedSites[blockedSiteKey]!!.consumedTime >= BlockedSites.blockedSites[blockedSiteKey]!!.allowedTime

    private fun stopTracking(pos: Int) {
        if (currentSite == null) {
            return
        }
        println("STOP TRACKING: "+ "pos: " + pos + " site: " + currentSite?.domain)

        currentSite = null;
        trackingJob?.cancel() // Stop the coroutine
        trackingJob = null
        visitedSites.clear() // Clear tracking data
        inactivityJob?.cancel()
        inactivityJob = null
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
        lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        lockIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        lockIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(lockIntent)
    }

    // find a better way to do this, the thing is when I close the browser any event it is triggered
    private fun resetInactivityTimer() {
        inactivityJob?.cancel()
        inactivityJob = CoroutineScope(Dispatchers.Default).launch {
            delay(1 * 60  * 1000) // one minute
            println("Inactivity timeout reached, stopping tracking...")
            stopTracking(6)
        }
    }

    override fun onInterrupt() {
        // Handle interruptions
    }

}
