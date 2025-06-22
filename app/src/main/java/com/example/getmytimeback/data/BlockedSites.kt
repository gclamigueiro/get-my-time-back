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

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val domain = obj.getString("domain")
            val allowedTime = obj.getInt("allowed_time")

            // if domain already exists in blockedSites, don't do anything
            if (blockedSites.containsKey(domain)) {
                continue
            }
            blockedSites[domain] = BlockedSite(domain, allowedTime * 60, 0)
        }
    }

}