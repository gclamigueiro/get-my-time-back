package com.example.getmytimeback.data

import com.example.getmytimeback.model.BlockedSite
import com.example.getmytimeback.model.SupportedBrowserConfig

object SupportedBrowsers {
    val supportedBrowserConfigs = mapOf(
        "com.android.chrome" to  SupportedBrowserConfig(
            "com.android.chrome",
            "com.android.chrome:id/url_bar"
        ),
        "org.mozilla.firefox" to  SupportedBrowserConfig(
            "org.mozilla.firefox",
            "org.mozilla.firefox:id/mozac_browser_toolbar_url_view"
        ),
        "org.mozilla.firefox" to SupportedBrowserConfig(
            "org.mozilla.firefox",
            "org.mozilla.firefox:id/mozac_browser_toolbar_url_view"
        ),
        "com.opera.browser" to  SupportedBrowserConfig(
            "com.opera.browser",
            "com.opera.browser:id/url_field"
        ),
        "com.opera.mini.native" to  SupportedBrowserConfig(
            "com.opera.mini.native",
            "com.opera.mini.native:id/url_field"
        )
    )

}