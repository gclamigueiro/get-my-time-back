package com.example.getmytimeback.data

import android.content.Context
import org.json.JSONArray
import com.example.getmytimeback.model.BlockedSite

object BlockedSites {
    val blockedSites = mutableMapOf<String, BlockedSite>()

    fun loadFromAssets(context: Context) {
        val json = context.assets.open("blocked_sites.json")
            .bufferedReader()
            .use { it.readText() }

        val jsonArray = JSONArray(json)
        blockedSites.clear()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val domain = obj.getString("domain")
            val allowedTime = obj.getInt("allowed_time")

            // check if domain already exists in blockedSites, then just update the allowed time
            if (blockedSites.containsKey(domain)) {
                blockedSites[domain]?.allowedTime = allowedTime * 60
                continue
            }

            blockedSites[domain] = BlockedSite(domain, allowedTime * 60, 0)
        }
    }

}