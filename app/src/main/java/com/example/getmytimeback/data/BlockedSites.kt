package com.example.getmytimeback.data

import com.example.getmytimeback.model.BlockedSite

object BlockedSites {
    val blockedSites = mapOf(
        "x.com" to BlockedSite("x.com", 1*60,0),
        "facebook.com" to BlockedSite("facebook.com", 1*60,0),
    )
}