package com.example.getmytimeback.data

import com.example.getmytimeback.model.BlockedSite

object BlockedSites {
    val blockedSites = mutableMapOf<String, BlockedSite>().apply {
        put("x.com", BlockedSite("x.com", 15 * 60, 0))
        put("facebook.com", BlockedSite("facebook.com", 5 * 60, 0))
    }
}