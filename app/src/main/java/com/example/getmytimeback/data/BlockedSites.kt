package com.example.getmytimeback.data

import com.example.getmytimeback.model.BlockedSite

object BlockedSites {
    val blockedSites = mutableMapOf<String, BlockedSite>().apply {
        put("x.com", BlockedSite("x.com", 1 * 10, 0))
        put("facebook.com", BlockedSite("facebook.com", 1 * 10, 0))
    }
}