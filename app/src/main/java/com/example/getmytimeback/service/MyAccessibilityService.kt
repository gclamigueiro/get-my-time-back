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

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventType = event.eventType

        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {

                val parentNodeInfo = event.source
                if (parentNodeInfo == null) {
                    stopTracking()
                    return
                }

                val packageName = event.packageName.toString()
                val browserConfig: SupportedBrowserConfig? = supportedBrowserConfigs[packageName]
                if (browserConfig == null) {
                    stopTracking() // User switched to another app
                    return
                }

                val browserUrl: String = captureUrl(parentNodeInfo, browserConfig) ?: return

                val blockedSiteKey = BlockedSites.blockedSites.keys.firstOrNull { browserUrl.contains(it) }

                if (blockedSiteKey != null) {
                    startTrackingWebsite(blockedSiteKey)
                }else {
                    stopTracking()
                }
            }
        }
    }

    private fun startTrackingWebsite(blockedSiteKey: String) {

        // I am in the same site, all good
        if (currentSite != null && currentSite?.site == blockedSiteKey ) {
            return
        }
        currentSite = BlockedSites.blockedSites[blockedSiteKey]
        visitedSites[blockedSiteKey] = System.currentTimeMillis()

        trackingJob?.cancel() // Cancel any previous tracking job
        trackingJob = CoroutineScope(Dispatchers.Main).launch {

                // if all the time for the site is already consumed
               // give 2 seconds to close the tab and stop tracking
                if (checkConsumedTime(blockedSiteKey)) {
                    delay(2000)
                    drawOnTop()
                    stopTracking()
                }

                while (true) {
                    delay(5000) // add consumed time every 5 seconds
                    val currentTime = System.currentTimeMillis();

                    val elapsedTime =
                        ( currentTime - visitedSites[blockedSiteKey]!!) / 1000
                    BlockedSites.blockedSites[blockedSiteKey]!!.consumedTime += elapsedTime

                    visitedSites[blockedSiteKey] = currentTime

                    if (checkConsumedTime(blockedSiteKey)) {
                        stopTracking() // Stop tracking once the time limit is reached
                        drawOnTop()
                        break
                    }
                }
        }
    }

    private fun checkConsumedTime(blockedSiteKey: String) =
        BlockedSites.blockedSites[blockedSiteKey]!!.consumedTime >= BlockedSites.blockedSites[blockedSiteKey]!!.allowedTime

    private fun stopTracking() {
        if (currentSite == null) {
            return
        }
        currentSite = null;
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
