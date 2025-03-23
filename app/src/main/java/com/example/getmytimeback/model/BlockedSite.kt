package com.example.getmytimeback.model

data class BlockedSite(
    val site: String,
    val allowedTime: Int,
    var consumedTime: Long
)
